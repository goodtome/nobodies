import { defineStore } from 'pinia';
import { abortUpload, completeUpload, getUploadConfig, getUploadProgress, initUpload, uploadChunk } from '@/api/upload';
import type {
  FileValidationResult,
  UploadConfigResponse,
  UploadProgressResponse,
  UploadTask,
} from '@/types/upload';
import { retryWithBackoff } from '@/utils/retry';

interface UploadState {
  tasks: UploadTask[];
  isUploading: boolean;
  uploadConfig?: UploadConfigResponse;
}

interface ActiveUploadSession {
  taskId: string;
  title: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  lastModified: number;
  sessionId: number;
}

const DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;
const FILE_CONCURRENCY = 2;
const CHUNK_CONCURRENCY = 3;
const CHUNK_RETRY_BASE_DELAY_MS = 500;
const ACTIVE_UPLOAD_SESSION_KEY = 'nobodies_active_upload_sessions';
const taskAbortControllers = new Map<string, AbortController>();

export const useUploadStore = defineStore('upload', {
  state: (): UploadState => ({
    tasks: [],
    isUploading: false,
    uploadConfig: undefined,
  }),
  getters: {
    activeTasks: (state) => state.tasks.filter((task) => task.status !== 'COMPLETED'),
    completedTasks: (state) => state.tasks.filter((task) => task.status === 'COMPLETED'),
  },
  actions: {
    async fetchUploadConfig() {
      this.uploadConfig = await getUploadConfig();
      return this.uploadConfig;
    },
    addFiles(files: File[]): FileValidationResult {
      const acceptedFiles: File[] = [];
      const rejectedMessages: string[] = [];
      const allowedTypes = new Set((this.uploadConfig?.allowedTypes ?? []).map((type) => type.toLowerCase()));
      const maxFileSize = this.uploadConfig?.maxFileSize;

      for (const file of files) {
        const fileType = file.name.split('.').pop()?.toLowerCase() ?? '';
        if (allowedTypes.size > 0 && !allowedTypes.has(fileType)) {
          rejectedMessages.push(`\u6587\u4ef6 ${file.name} \u7c7b\u578b\u4e0d\u652f\u6301\uff0c\u4ec5\u652f\u6301 ${Array.from(allowedTypes).join('\u3001')}`);
          continue;
        }
        if (typeof maxFileSize === 'number' && file.size > maxFileSize) {
          rejectedMessages.push(`\u6587\u4ef6 ${file.name} \u8d85\u8fc7\u5927\u5c0f\u9650\u5236\uff0c\u6700\u5927\u5141\u8bb8 ${this.formatBytes(maxFileSize)}`);
          continue;
        }
        acceptedFiles.push(file);
      }

      for (const file of acceptedFiles) {
        const taskId = this.buildTaskId(file.name, file.size, file.lastModified);
        const existingTask = this.tasks.find((task) => task.taskId === taskId);
        const defaultTitle = file.name.replace(/\.[^.]+$/, '');
        if (existingTask) {
          existingTask.file = file;
          existingTask.fileName = file.name;
          existingTask.fileSize = file.size;
          existingTask.fileType = file.name.split('.').pop()?.toLowerCase() ?? '';
          existingTask.lastModified = file.lastModified;
          existingTask.title = existingTask.title || defaultTitle;
          continue;
        }

        this.tasks.push({
          taskId,
          title: defaultTitle,
          fileName: file.name,
          fileSize: file.size,
          fileType: file.name.split('.').pop()?.toLowerCase() ?? '',
          lastModified: file.lastModified,
          file,
          sessionId: undefined,
          status: 'IDLE',
          progress: 0,
          uploadedChunks: [],
          chunkStatuses: [],
          errorMessage: '',
        });
      }

      return { acceptedFiles, rejectedMessages };
    },
    updateTaskTitle(taskId: string, value: string) {
      const task = this.tasks.find((item) => item.taskId === taskId);
      if (task) {
        task.title = value;
        this.persistActiveSessions();
      }
    },
    removeTask(taskId: string) {
      taskAbortControllers.get(taskId)?.abort();
      taskAbortControllers.delete(taskId);
      this.tasks = this.tasks.filter((task) => task.taskId !== taskId);
      this.persistActiveSessions();
    },
    async uploadAll() {
      const pendingTasks = this.tasks.filter((task) => task.file && ['IDLE', 'FAILED', 'ABORTED'].includes(task.status));
      if (pendingTasks.length === 0) {
        return;
      }

      this.isUploading = true;
      const queue = [...pendingTasks];
      const workers = Array.from({ length: Math.min(FILE_CONCURRENCY, queue.length) }, async () => {
        while (queue.length > 0) {
          const nextTask = queue.shift();
          if (!nextTask) {
            return;
          }
          await this.uploadSingleTask(nextTask.taskId);
        }
      });

      try {
        await Promise.all(workers);
      } finally {
        this.isUploading = false;
      }
    },
    async uploadSingleTask(taskId: string) {
      const task = this.tasks.find((item) => item.taskId === taskId);
      if (!task || !task.file) {
        return;
      }

      task.status = 'INITIATING';
      task.errorMessage = '';
      task.progress = task.uploadedChunks.length > 0 ? task.progress : 0;

      const abortController = new AbortController();
      taskAbortControllers.set(taskId, abortController);

      try {
        const chunkSize = Math.min(DEFAULT_CHUNK_SIZE, task.file.size);
        const initData = await initUpload({
          fileName: task.file.name,
          fileSize: task.file.size,
          fileType: task.fileType,
          chunkSize,
        }, abortController.signal);

        if (task.status === 'ABORTED' || abortController.signal.aborted) {
          return;
        }

        task.sessionId = initData.uploadSessionId;
        task.uploadedChunks = [...initData.uploadedChunks];
        task.chunkStatuses = [...initData.chunkStatuses];
        task.status = 'UPLOADING';
        this.persistActiveSessions();

        const uploadedChunkSet = new Set<number>(initData.uploadedChunks);
        const totalChunks = Math.ceil(task.file.size / chunkSize);
        task.progress = totalChunks === 0 ? 0 : Math.round((uploadedChunkSet.size / totalChunks) * 100);

        const pendingChunkNumbers: number[] = [];
        for (let chunkNumber = 1; chunkNumber <= totalChunks; chunkNumber += 1) {
          if (!uploadedChunkSet.has(chunkNumber)) {
            pendingChunkNumbers.push(chunkNumber);
          }
        }

        const workers = Array.from({ length: Math.min(CHUNK_CONCURRENCY, pendingChunkNumbers.length || 1) }, async () => {
          while (pendingChunkNumbers.length > 0) {
            if (task.status === 'ABORTED' || abortController.signal.aborted) {
              return;
            }
            const chunkNumber = pendingChunkNumbers.shift();
            if (!chunkNumber) {
              return;
            }
            const start = (chunkNumber - 1) * chunkSize;
            const end = Math.min(start + chunkSize, task.file!.size);
            const chunk = task.file!.slice(start, end);

            try {
              await retryWithBackoff(
                () => uploadChunk(initData.uploadSessionId, chunkNumber, chunk, abortController.signal),
                initData.chunkRetryTimes + 1,
                CHUNK_RETRY_BASE_DELAY_MS,
                { shouldContinue: () => task.status !== 'ABORTED' && !abortController.signal.aborted },
              );

              if (task.status === 'ABORTED' || abortController.signal.aborted) {
                return;
              }
              await this.syncProgress(initData.uploadSessionId);
            } catch (error) {
              if (task.status === 'ABORTED' || abortController.signal.aborted) {
                return;
              }
              task.status = 'FAILED';
              task.errorMessage = `\u5206\u7247 ${chunkNumber} \u4e0a\u4f20\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5`;
              throw error;
            }
          }
        });

        await Promise.all(workers);
        if (task.status === 'ABORTED' || abortController.signal.aborted) {
          return;
        }

        await completeUpload(initData.uploadSessionId, task.title || task.file.name);
        task.status = 'COMPLETED';
        task.progress = 100;
        task.errorMessage = '';
        task.chunkStatuses = task.chunkStatuses.map((chunk) => ({ ...chunk, status: 'UPLOADED', uploaded: true }));
      } catch (error) {
        if (task.status === 'ABORTED' || abortController.signal.aborted) {
          return;
        }
        throw error;
      } finally {
        if (taskAbortControllers.get(taskId) === abortController) {
          taskAbortControllers.delete(taskId);
        }
        this.persistActiveSessions();
      }
    },
    async syncProgress(sessionId: number) {
      const progressData: UploadProgressResponse = await getUploadProgress(sessionId);
      const task = this.tasks.find((item) => item.sessionId === sessionId);
      if (!task) {
        return;
      }
      task.status = progressData.status;
      task.uploadedChunks = progressData.uploadedChunks;
      task.chunkStatuses = progressData.chunkStatuses;
      task.progress = progressData.totalChunks === 0
        ? 0
        : Math.round((progressData.uploadedChunksCount / progressData.totalChunks) * 100);
      this.persistActiveSessions();
    },
    async restoreProgress() {
      const raw = localStorage.getItem(ACTIVE_UPLOAD_SESSION_KEY);
      if (!raw) {
        return;
      }
      try {
        const sessions: ActiveUploadSession[] = JSON.parse(raw);
        for (const session of sessions) {
          let task = this.tasks.find((item) => item.taskId === session.taskId);
          if (!task) {
            task = {
              taskId: session.taskId,
              title: session.title,
              fileName: session.fileName,
              fileSize: session.fileSize,
              fileType: session.fileType,
              lastModified: session.lastModified,
              file: undefined,
              sessionId: session.sessionId,
              status: 'INITIATED',
              progress: 0,
              uploadedChunks: [],
              chunkStatuses: [],
              errorMessage: '',
            };
            this.tasks.push(task);
          } else {
            task.sessionId = session.sessionId;
          }
          await this.syncProgress(session.sessionId);
        }
      } catch {
        localStorage.removeItem(ACTIVE_UPLOAD_SESSION_KEY);
      }
    },
    async cancelTask(taskId: string) {
      const task = this.tasks.find((item) => item.taskId === taskId);
      if (!task) {
        return;
      }

      taskAbortControllers.get(taskId)?.abort();
      taskAbortControllers.delete(taskId);
      task.status = 'ABORTED';
      task.errorMessage = '';
      this.persistActiveSessions();

      if (!task.sessionId) {
        return;
      }

      await abortUpload(task.sessionId);
      await this.syncProgress(task.sessionId);
    },
    buildTaskId(fileName: string, fileSize: number, lastModified: number) {
      return `${fileName}-${fileSize}-${lastModified}`;
    },
    formatBytes(value: number) {
      if (value < 1024) return `${value} B`;
      if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
      if (value < 1024 * 1024 * 1024) return `${(value / 1024 / 1024).toFixed(1)} MB`;
      return `${(value / 1024 / 1024 / 1024).toFixed(2)} GB`;
    },
    persistActiveSessions() {
      const sessions: ActiveUploadSession[] = this.tasks
        .filter((task) => task.sessionId && ['INITIATED', 'INITIATING', 'UPLOADING', 'FAILED'].includes(task.status))
        .map((task) => ({
          taskId: task.taskId,
          title: task.title,
          fileName: task.fileName,
          fileSize: task.fileSize,
          fileType: task.fileType,
          lastModified: task.lastModified,
          sessionId: task.sessionId!,
        }));

      if (sessions.length === 0) {
        localStorage.removeItem(ACTIVE_UPLOAD_SESSION_KEY);
        return;
      }
      localStorage.setItem(ACTIVE_UPLOAD_SESSION_KEY, JSON.stringify(sessions));
    },
  },
});