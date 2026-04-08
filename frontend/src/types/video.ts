export interface VideoItem {
  id: number;
  title: string;
  fileName: string;
  fileSize: number;
  status: string;
}

export interface VideoDetail extends VideoItem {
  fileType: string;
  playbackUrl: string;
}
