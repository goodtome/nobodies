export interface InitUploadRequest {
  fileName: string;
  fileSize: number;
  fileType: string;
  chunkSize: number;
}

export interface UploadChunkStatus {
  chunkNumber: number;
  status: string;
  chunkSize: number | null;
  retryCount: number;
  etag: string | null;
  uploadedAt?: string;
  uploaded: boolean;
}

export interface UploadConfigResponse {
  maxFileSize: number;
  allowedTypes: string[];
}

export interface FileValidationResult {
  acceptedFiles: File[];
  rejectedMessages: string[];
}

export interface InitUploadResponse {
  uploadSessionId: number;
  objectKey: string;
  ossUploadId: string;
  totalChunks: number;
  chunkRetryTimes: number;
  uploadedChunksCount: number;
  pendingChunksCount: number;
  abortedChunksCount: number;
  uploadedChunks: number[];
  chunkStatuses: UploadChunkStatus[];
}

export interface UploadChunkResponse {
  sessionId: number;
  chunkNumber: number;
  status: string;
  etag: string;
}

export interface UploadProgressResponse {
  sessionId: number;
  status: string;
  totalChunks: number;
  uploadedChunksCount: number;
  pendingChunksCount: number;
  abortedChunksCount: number;
  uploadedChunks: number[];
  chunkStatuses: UploadChunkStatus[];
}

export interface UploadTask {
  taskId: string;
  title: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  lastModified: number;
  file?: File;
  sessionId?: number;
  status: string;
  progress: number;
  uploadedChunks: number[];
  chunkStatuses: UploadChunkStatus[];
  errorMessage: string;
}
