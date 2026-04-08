<template>
  <section class="upload-grid">
    <el-card v-if="['UPLOADER', 'ADMIN'].includes(authStore.role)" class="panel-card left-panel">
      <template #header>&#19978;&#20256;&#35270;&#39057;</template>
      <UploadDropzone @select="handleFileSelect" />
      <div class="toolbar">
        <el-button type="primary" :loading="uploadStore.isUploading" @click="handleUploadAll">&#24320;&#22987;&#25209;&#37327;&#19978;&#20256;</el-button>
      </div>
      <el-alert v-if="showRestoreHint" title="&#26816;&#27979;&#21040;&#26410;&#23436;&#25104;&#30340;&#19978;&#20256;&#20219;&#21153;&#65292;&#35831;&#37325;&#26032;&#36873;&#25321;&#21407;&#25991;&#20214;&#21518;&#32487;&#32493;&#19978;&#20256;&#12290;" type="warning" show-icon :closable="false" />
      <UploadTaskList
        v-if="uploadStore.tasks.length"
        :tasks="uploadStore.tasks"
        @update-title="handleUpdateTitle"
        @abort="handleAbort"
        @retry="handleRetry"
        @remove="handleRemove"
      />
      <el-empty v-else description="&#26242;&#26102;&#27809;&#26377;&#19978;&#20256;&#20219;&#21153;" />
    </el-card>

    <el-card v-else class="panel-card">
      <template #header>&#19978;&#20256;&#35270;&#39057;</template>
      <div class="permission-denied">
        <el-icon size="48"><WarningFilled /></el-icon>
        <p>&#26085;&#26399;&#8981;&#26723;&#27425;&#65292;&#26242;&#26102;&#26377;&#19978;&#20256;&#35270;&#39057;&#7684;&#6743;&#9650;</p>
      </div>
    </el-card>

    <el-card v-if="['UPLOADER', 'ADMIN'].includes(authStore.role)" class="panel-card">
      <template #header>&#19978;&#20256;&#25552;&#31034;</template>
      <ul class="tips">
        <li v-if="uploadStore.uploadConfig">
          &#25903;&#25345;&#31867;&#22411;&#65306;{{ uploadStore.uploadConfig.allowedTypes.map((type) => type.toUpperCase()).join('&#12289;') }}
        </li>
        <li v-if="uploadStore.uploadConfig">
          &#21333;&#25991;&#20214;&#22823;&#23567;&#19978;&#38480;&#65306;{{ formatBytes(uploadStore.uploadConfig.maxFileSize) }}
        </li>
        <li>&#19978;&#20256;&#37319;&#29992;&#20998;&#29255;&#26041;&#24335;&#65292;&#36866;&#21512;&#22823;&#25991;&#20214;&#20256;&#36755;&#12290;</li>
        <li>&#19978;&#20256;&#20013;&#26029;&#21518;&#21487;&#26681;&#25454;&#20250;&#35805;&#35760;&#24405;&#32487;&#32493;&#19978;&#20256;&#12290;</li>
        <li>&#21333;&#20010;&#20998;&#29255;&#22833;&#36133;&#20250;&#33258;&#21160;&#37325;&#35797;&#65292;&#22810;&#20219;&#21153;&#20250;&#25353;&#24182;&#21457;&#38431;&#21015;&#25191;&#34892;&#12290;</li>
      </ul>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { WarningFilled } from '@element-plus/icons-vue';
import UploadDropzone from '@/components/upload/UploadDropzone.vue';
import UploadTaskList from '@/components/upload/UploadTaskList.vue';
import { useUploadStore } from '@/store/upload';
import { useVideoStore } from '@/store/video';
import { useAuthStore } from '@/store/auth';

const uploadStore = useUploadStore();
const videoStore = useVideoStore();
const authStore = useAuthStore();

onMounted(async () => {
  try {
    await uploadStore.fetchUploadConfig();
  } catch (error) {
    ElMessage.warning(getUploadConfigErrorMessage(error));
  }
  uploadStore.restoreProgress();
});

const showRestoreHint = computed(() => uploadStore.tasks.some((task) => !task.file && task.sessionId));

async function handleFileSelect(files: File[]) {
  if (!uploadStore.uploadConfig) {
    try {
      await uploadStore.fetchUploadConfig();
    } catch (error) {
      ElMessage.warning(getUploadConfigErrorMessage(error));
    }
  }

  const result = uploadStore.addFiles(files);
  result.rejectedMessages.forEach((message) => ElMessage.warning(message));
}

function getUploadConfigErrorMessage(error: unknown) {
  const status = Number((error as { response?: { status?: number } })?.response?.status ?? 0);
  if (status === 401) {
    return '\u767b\u5f55\u72b6\u6001\u5df2\u8fc7\u671f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55';
  }
  if (status === 403) {
    return '\u5f53\u524d\u8d26\u53f7\u65e0\u4e0a\u4f20\u6743\u9650\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u5f00\u901a';
  }
  if (status >= 500) {
    return '\u4e0a\u4f20\u914d\u7f6e\u63a5\u53e3\u5f02\u5e38\uff0c\u5c06\u4f7f\u7528\u540e\u7aef\u6821\u9a8c';
  }
  return '\u4e0a\u4f20\u914d\u7f6e\u52a0\u8f7d\u5931\u8d25\uff0c\u5c06\u4f7f\u7528\u540e\u7aef\u6821\u9a8c';
}

function handleUpdateTitle(taskId: string, value: string) {
  uploadStore.updateTaskTitle(taskId, value);
}

async function handleUploadAll() {
  const hasFile = uploadStore.tasks.some((task) => task.file);
  if (!hasFile) {
    ElMessage.warning('\u8bf7\u5148\u9009\u62e9\u9700\u8981\u4e0a\u4f20\u7684\u6587\u4ef6');
    return;
  }

  try {
    await uploadStore.uploadAll();
    await videoStore.fetchVideos();
    
    const failedTasks = uploadStore.tasks.filter(task => task.status === 'FAILED');
    if (failedTasks.length > 0) {
      ElMessage.error(`\u90e8\u5206\u4e0a\u4f20\u4efb\u52a1\u5931\u8d25\uff0c\u5171${failedTasks.length}\u4e2a\u6587\u4ef6\u4e0a\u4f20\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u540e\u91cd\u8bd5`);
    } else {
      ElMessage.success('\u4e0a\u4f20\u4efb\u52a1\u5df2\u5b8c\u6210');
    }
  } catch {
    ElMessage.error('\u90e8\u5206\u4e0a\u4f20\u4efb\u52a1\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u540e\u91cd\u8bd5');
  }
}

async function handleAbort(taskId: string) {
  await uploadStore.cancelTask(taskId);
  ElMessage.info('\u4e0a\u4f20\u5df2\u53d6\u6d88');
}

async function handleRetry(taskId: string) {
  try {
    await uploadStore.uploadSingleTask(taskId);
    await videoStore.fetchVideos();
    ElMessage.success('\u4efb\u52a1\u91cd\u8bd5\u6210\u529f');
  } catch {
    ElMessage.error('\u4efb\u52a1\u91cd\u8bd5\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5');
  }
}

function handleRemove(taskId: string) {
  uploadStore.removeTask(taskId);
}

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  if (value < 1024 * 1024 * 1024) return `${(value / 1024 / 1024).toFixed(1)} MB`;
  return `${(value / 1024 / 1024 / 1024).toFixed(2)} GB`;
}
</script>

<style scoped>
.upload-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: 1.5fr 0.9fr;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  margin: 16px 0;
}

.tips {
  margin: 0;
  padding-left: 18px;
  color: #475467;
  line-height: 1.8;
}

.permission-denied {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
  text-align: center;
  gap: 16px;
}

@media (max-width: 900px) {
  .upload-grid {
    grid-template-columns: 1fr;
  }
}
</style>