import http, { unwrap } from './http';
import type { DashboardSummary } from '@/types/dashboard';

export function getDashboardSummary() {
  return unwrap<DashboardSummary>(http.get('/dashboard/summary'));
}
