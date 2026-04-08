<template>
  <el-card class="panel-card video-card">
    <h3>{{ video.title }}</h3>
    <p>{{ video.fileName }}</p>
    <p>&#29366;&#24577;&#65306;{{ statusText }}</p>
    <div class="actions">
      <el-button type="primary" link @click="openDetail">&#26597;&#30475;</el-button>
      <el-button type="danger" link @click="$emit('delete', video.id)">&#21024;&#38500;</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import type { VideoItem } from '@/types/video';

const props = defineProps<{
  video: VideoItem;
}>();

defineEmits<{
  (event: 'delete', id: number): void;
}>();

const router = useRouter();

const statusMap: Record<string, string> = {
  INITIATED: '\u5df2\u521b\u5efa',
  UPLOADING: '\u4e0a\u4f20\u4e2d',
  IN_PROGRESS: '\u8fdb\u884c\u4e2d',
  AVAILABLE: '\u53ef\u7528',
  PROCESSING: '\u5904\u7406\u4e2d',
  COMPLETED: '\u5df2\u5b8c\u6210',
  DELETED: '\u5df2\u5220\u9664',
  FAILED: '\u5931\u8d25',
};

const statusText = computed(() => statusMap[props.video.status] ?? props.video.status);

function openDetail() {
  router.push(`/videos/${props.video.id}`);
}
</script>

<style scoped>
.video-card h3 {
  margin-top: 0;
}

.actions {
  display: flex;
  gap: 12px;
}
</style>
