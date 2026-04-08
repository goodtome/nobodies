import http, { unwrap } from './http';
import type {
  AuthResponse,
  LoginRequest,
  PasswordResetConfirmRequest,
  PasswordResetRequest,
  RegisterRequest,
} from '@/types/auth';

export function register(payload: RegisterRequest) {
  return unwrap<void>(http.post('/auth/register', payload));
}

export function login(payload: LoginRequest) {
  return unwrap<AuthResponse>(http.post('/auth/login', payload));
}

export function requestPasswordReset(payload: PasswordResetRequest) {
  return unwrap<void>(http.post('/auth/password-reset/request', payload));
}

export function confirmPasswordReset(payload: PasswordResetConfirmRequest) {
  return unwrap<void>(http.post('/auth/password-reset/confirm', payload));
}
