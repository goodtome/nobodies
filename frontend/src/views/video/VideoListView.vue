<template>
  <section>
    <div class="toolbar">
      <h2>视频列表</h2>
      <el-button @click="videoStore.fetchVideos()">刷新</el-button>
    </div>

    <el-empty v-if="!videoStore.loading && videoStore.items.length === 0" description="暂时还没有视频" />

    <div v-else class="video-grid">
      <VideoCard v-for="item in videoStore.items" :key="item.id" :video="item" @delete="handleDelete" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import VideoCard from '@/components/video/VideoCard.vue';
import { useVideoStore } from '@/store/video';

const videoStore = useVideoStore();

onMounted(() => {
  videoStore.fetchVideos();
});

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除这个视频吗？', '提示', { type: 'warning' });
  await videoStore.removeVideo(id);
  ElMessage.success('视频已删除');
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.video-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
}
</style>
