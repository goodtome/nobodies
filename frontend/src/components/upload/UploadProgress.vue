<template>
  <div>
    <el-progress :percentage="progress" :status="status === 'COMPLETED' ? 'success' : status === 'FAILED' ? 'exception' : undefined" />
    <p class="meta">&#29366;&#24577;&#65306;{{ statusText }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  progress: number;
  status: string;
}>();

const statusMap: Record<string, string> = {
  IDLE: '\u672a\u5f00\u59cb',
  INITIATING: '\u521d\u59cb\u5316\u4e2d',
  INITIATED: '\u5df2\u521b\u5efa',
  UPLOADING: '\u4e0a\u4f20\u4e2d',
  COMPLETED: '\u5df2\u5b8c\u6210',
  FAILED: '\u5931\u8d25',
  ABORTED: '\u5df2\u4e2d\u6b62',
  IN_PROGRESS: '\u8fdb\u884c\u4e2d',
};

const statusText = computed(() => statusMap[props.status] ?? props.status);
</script>

<style scoped>
.meta {
  margin-top: 10px;
  color: #667085;
}
</style>
