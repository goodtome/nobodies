import { defineStore } from 'pinia';
import { deleteVideo, getVideoDetail, getVideos } from '@/api/video';
import type { VideoDetail, VideoItem } from '@/types/video';

interface VideoState {
  items: VideoItem[];
  current?: VideoDetail;
  loading: boolean;
}

export const useVideoStore = defineStore('video', {
  state: (): VideoState => ({
    items: [],
    current: undefined,
    loading: false,
  }),
  actions: {
    async fetchVideos() {
      this.loading = true;
      try {
        this.items = await getVideos();
      } finally {
        this.loading = false;
      }
    },
    async fetchVideoDetail(id: number) {
      this.current = await getVideoDetail(id);
    },
    async removeVideo(id: number) {
      await deleteVideo(id);
      this.items = this.items.filter((item: VideoItem) => item.id !== id);
    },
  },
});
