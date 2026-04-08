import { defineStore } from 'pinia';
import { login as loginApi, register as registerApi } from '@/api/auth';
import type { LoginRequest, RegisterRequest } from '@/types/auth';
import { clearRole, clearToken, getRole, getToken, setRole, setToken } from '@/utils/token';

interface AuthState {
  token: string;
  role: string;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: getToken() ?? '',
    role: getRole() ?? '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async register(payload: RegisterRequest) {
      await registerApi(payload);
    },
    async login(payload: LoginRequest) {
      const data = await loginApi(payload);
      this.token = data.accessToken;
      this.role = data.role;
      setToken(data.accessToken);
      setRole(data.role);
    },
    logout() {
      this.token = '';
      this.role = '';
      clearToken();
      clearRole();
    },
  },
});
