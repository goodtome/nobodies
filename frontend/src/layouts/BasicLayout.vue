<template>
  <div class="page-shell layout-shell">
    <aside class="sidebar">
      <div>
        <h1 class="brand">Nobodies</h1>
        <p class="subtitle">&#35270;&#39057;&#24179;&#21488;&#25511;&#21046;&#21488;</p>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/dashboard">&#20202;&#34920;&#30424;</el-menu-item>
        <el-menu-item v-if="['UPLOADER', 'ADMIN'].includes(authStore.role)" index="/upload">&#19978;&#20256;&#35270;&#39057;</el-menu-item>
        <el-menu-item index="/videos">&#35270;&#39057;&#21015;&#34920;</el-menu-item>
        <el-menu-item v-if="authStore.role === 'ADMIN'" index="/admin/users">&#29992;&#25143;&#31649;&#29702;</el-menu-item>
      </el-menu>
    </aside>

    <main class="content">
      <header class="header">
        <el-button type="danger" plain @click="handleLogout" class="logout-button">&#36864;&#20986;&#30331;&#24405;</el-button>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import http from '@/api/http';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

async function handleLogout() {
  try {
    await http.post('/auth/logout');
  } finally {
    authStore.logout();
    router.push('/login');
  }
}
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: 260px 1fr;
}

.sidebar {
  min-height: 100vh;
  padding: 32px 20px;
  background: #e0f2fe;
  color: #0f1728;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand {
  margin: 0;
  font-size: 28px;
}

.subtitle {
  margin: 8px 0 0;
  color: rgba(15, 23, 40, 0.7);
}

.menu {
  border: none;
  background: transparent;
  color: #0f1728;
}

.menu :deep(.el-menu-item) {
  color: #0f1728;
}

.menu :deep(.el-menu-item.is-active) {
  color: #0369a1;
}

.content {
  padding: 0;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  padding: 16px 32px;
  display: flex;
  justify-content: flex-end;
  border-bottom: 1px solid #e5e7eb;
}

.logout-button {
  align-self: flex-start;
}

.content > *:not(.header) {
  padding: 32px;
}

@media (max-width: 900px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    min-height: auto;
  }
}
</style>
