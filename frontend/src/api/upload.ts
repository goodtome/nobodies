import http, { unwrap } from './http';
import type {
  InitUploadRequest,
  InitUploadResponse,
  UploadChunkResponse,
  UploadConfigResponse,
  UploadProgressResponse,
} from '@/types/upload';

export function getUploadConfig() {
  return unwrap<UploadConfigResponse>(http.get('/videos/upload/config'));
}

export function initUpload(payload: InitUploadRequest, signal?: AbortSignal) {
  return unwrap<InitUploadResponse>(http.post('/videos/upload/init', payload, { signal }));
}

export function uploadChunk(sessionId: number, chunkNumber: number, file: Blob, signal?: AbortSignal) {
  const formData = new FormData();
  formData.append('file', file);
  return unwrap<UploadChunkResponse>(
    http.put(`/videos/upload/${sessionId}/chunk`, formData, {
      params: { chunkNumber },
      headers: { 'Content-Type': 'multipart/form-data' },
      signal,
    }),
  );
}

export function getUploadProgress(sessionId: number) {
  return unwrap<UploadProgressResponse>(http.get(`/videos/upload/${sessionId}/progress`));
}

export function completeUpload(sessionId: number, title: string) {
  return unwrap<void>(http.post(`/videos/upload/${sessionId}/complete`, { title }));
}

export function abortUpload(sessionId: number) {
  return unwrap<void>(http.delete(`/videos/upload/${sessionId}/abort`));
}
