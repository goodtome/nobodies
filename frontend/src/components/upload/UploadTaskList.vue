<template>
  <div class="task-list">
    <el-card v-for="task in tasks" :key="task.taskId" class="task-card">
      <div class="task-header">
        <div>
          <h3>{{ task.title }}</h3>
          <p>{{ task.fileName }} &middot; {{ formatBytes(task.fileSize) }}</p>
        </div>
        <el-tag :type="statusTagType(task.status)">{{ statusText(task.status) }}</el-tag>
      </div>
      <el-form label-position="top">
        <el-form-item label="&#35270;&#39057;&#26631;&#39064;">
          <el-input :model-value="task.title" @update:model-value="emit('update-title', task.taskId, $event)" />
        </el-form-item>
      </el-form>
      <el-progress :percentage="task.progress" :status="task.status === 'COMPLETED' ? 'success' : task.status === 'FAILED' ? 'exception' : undefined" />
      <p v-if="task.sessionId">&#20250;&#35805; ID&#65306;{{ task.sessionId }}</p>
      <p v-if="task.uploadedChunks.length">&#24050;&#19978;&#20256;&#20998;&#29255;&#65306;{{ task.uploadedChunks.join(', ') }}</p>
      <div v-if="task.chunkStatuses.length" class="chunk-grid">
        <el-tag v-for="chunk in task.chunkStatuses" :key="chunk.chunkNumber" :type="chunkTagType(chunk.status)">
          &#20998;&#29255; {{ chunk.chunkNumber }} &middot; {{ chunkStatusText(chunk.status) }}
        </el-tag>
      </div>
      <el-alert v-if="task.errorMessage" :title="task.errorMessage" type="error" show-icon :closable="false" />
      <div class="actions">
        <el-button v-if="task.status === 'UPLOADING'" @click="emit('abort', task.taskId)">&#21462;&#28040;</el-button>
        <el-button v-if="task.status === 'FAILED'" type="primary" @click="emit('retry', task.taskId)">&#37325;&#35797;&#19978;&#20256;</el-button>
        <el-button text type="danger" @click="emit('remove', task.taskId)">&#31227;&#38500;</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { UploadTask } from '@/types/upload';

const emit = defineEmits<{
  (event: 'update-title', taskId: string, value: string): void;
  (event: 'abort', taskId: string): void;
  (event: 'retry', taskId: string): void;
  (event: 'remove', taskId: string): void;
}>();

defineProps<{
  tasks: UploadTask[];
}>();

function statusText(status: string) {
  const map: Record<string, string> = {
    IDLE: '\u672a\u5f00\u59cb',
    INITIATING: '\u521d\u59cb\u5316\u4e2d',
    INITIATED: '\u5df2\u521b\u5efa',
    UPLOADING: '\u4e0a\u4f20\u4e2d',
    COMPLETED: '\u5df2\u5b8c\u6210',
    FAILED: '\u5931\u8d25',
    ABORTED: '\u5df2\u53d6\u6d88',
    IN_PROGRESS: '\u8fdb\u884c\u4e2d',
  };
  return map[status] ?? status;
}

function chunkStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '\u5f85\u4e0a\u4f20',
    UPLOADED: '\u5df2\u4e0a\u4f20',
    FAILED: '\u5931\u8d25',
    ABORTED: '\u5df2\u53d6\u6d88',
    IN_PROGRESS: '\u8fdb\u884c\u4e2d',
  };
  return map[status] ?? status;
}

function statusTagType(status: string) {
  const map: Record<string, 'info' | 'success' | 'warning' | 'danger'> = {
    IDLE: 'info',
    INITIATING: 'warning',
    INITIATED: 'warning',
    UPLOADING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    ABORTED: 'info',
    IN_PROGRESS: 'warning',
  };
  return map[status] ?? 'info';
}

function chunkTagType(status: string) {
  const map: Record<string, 'info' | 'success' | 'warning' | 'danger'> = {
    PENDING: 'info',
    UPLOADED: 'success',
    FAILED: 'danger',
    ABORTED: 'info',
    IN_PROGRESS: 'warning',
  };
  return map[status] ?? 'info';
}

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  if (value < 1024 * 1024 * 1024) return `${(value / 1024 / 1024).toFixed(1)} MB`;
  return `${(value / 1024 / 1024 / 1024).toFixed(2)} GB`;
}
</script>

<style scoped>
.task-list {
  display: grid;
  gap: 16px;
}

.task-card {
  border-radius: 16px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.task-header h3 {
  margin: 0 0 6px;
}

.task-header p {
  margin: 0;
  color: #667085;
}

.chunk-grid {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>