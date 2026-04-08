import { createRouter, createWebHistory } from 'vue-router';
import BasicLayout from '@/layouts/BasicLayout.vue';
import LoginView from '@/views/auth/LoginView.vue';
import RegisterView from '@/views/auth/RegisterView.vue';
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue';
import ResetPasswordView from '@/views/auth/ResetPasswordView.vue';
import DashboardView from '@/views/dashboard/DashboardView.vue';
import UploadView from '@/views/upload/UploadView.vue';
import VideoListView from '@/views/video/VideoListView.vue';
import VideoDetailView from '@/views/video/VideoDetailView.vue';
import AdminUsersView from '@/views/admin/AdminUsersView.vue';
import { useAuthStore } from '@/store/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/register', component: RegisterView, meta: { public: true } },
    { path: '/forgot-password', component: ForgotPasswordView, meta: { public: true } },
    { path: '/reset-password', component: ResetPasswordView, meta: { public: true } },
    {
      path: '/',
      component: BasicLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: DashboardView },
        { path: 'upload', component: UploadView, meta: { uploaderOnly: true } },
        { path: 'videos', component: VideoListView },
        { path: 'videos/:id', component: VideoDetailView, props: true },
        { path: 'admin/users', component: AdminUsersView, meta: { adminOnly: true } },
      ],
    },
  ],
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.meta.public) {
    return true;
  }
  if (!authStore.isAuthenticated) {
    return '/login';
  }
  if (to.meta.adminOnly && authStore.role !== 'ADMIN') {
    return '/dashboard';
  }
  if (to.meta.uploaderOnly && !['UPLOADER', 'ADMIN'].includes(authStore.role)) {
    return '/dashboard';
  }
  return true;
});

export default router;
