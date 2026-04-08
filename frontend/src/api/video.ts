import http, { unwrap } from './http';
import type { VideoDetail, VideoItem } from '@/types/video';

export function getVideos() {
  return unwrap<VideoItem[]>(http.get('/videos'));
}

export function getVideoDetail(id: number) {
  return unwrap<VideoDetail>(http.get(`/videos/${id}`));
}

export function deleteVideo(id: number) {
  return unwrap<void>(http.delete(`/videos/${id}`));
}
