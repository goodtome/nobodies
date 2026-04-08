<template>
  <section v-if="videoStore.current" class="detail-grid">
    <el-card class="panel-card">
      <template #header>{{ videoStore.current.title }}</template>
      <VideoPlayer :src="videoStore.current.playbackUrl" />
    </el-card>

    <el-card class="panel-card meta-card">
      <template #header>视频信息</template>
      <p><strong>文件名：</strong> {{ videoStore.current.fileName }}</p>
      <p><strong>文件大小：</strong> {{ videoStore.current.fileSize }} 字节</p>
      <p><strong>文件类型：</strong> {{ videoStore.current.fileType }}</p>
      <p><strong>状态：</strong> {{ videoStore.current.status }}</p>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import VideoPlayer from '@/components/video/VideoPlayer.vue';
import { useVideoStore } from '@/store/video';

const props = defineProps<{
  id: string;
}>();

const videoStore = useVideoStore();

onMounted(() => {
  videoStore.fetchVideoDetail(Number(props.id));
});
</script>

<style scoped>
.detail-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: 2fr 1fr;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
