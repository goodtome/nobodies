export interface DashboardSummary {
  role: string;
  videoCount: number;
  storageUsedBytes: number;
  totalVideoCount?: number | null;
  totalStorageUsedBytes?: number | null;
}
