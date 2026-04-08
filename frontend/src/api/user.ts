import http, { unwrap } from './http';
import type { UserSummary } from '@/types/user';

export function getUsers() {
  return unwrap<UserSummary[]>(http.get('/admin/users'));
}

export function updateUserRole(id: number, role: string) {
  return unwrap<void>(http.patch(`/admin/users/${id}/role`, { role }));
}
