<template>
  <section class="dashboard-grid">
    <el-card class="panel-card stat-card">
      <template #header>&#24403;&#21069;&#35282;&#33394;</template>
      <strong>{{ roleText }}</strong>
    </el-card>
    <el-card class="panel-card stat-card">
      <template #header>&#35270;&#39057;&#24635;&#25968;</template>
      <strong>{{ summary?.videoCount ?? 0 }}</strong>
    </el-card>
    <el-card class="panel-card stat-card">
      <template #header>&#25105;&#30340;&#23384;&#20648;&#20351;&#29992;</template>
      <strong>{{ formatBytes(summary?.storageUsedBytes ?? 0) }}</strong>
    </el-card>
    <el-card v-if="isAdmin" class="panel-card stat-card">
      <template #header>&#20840;&#31449;&#23384;&#20648;&#20351;&#29992;</template>
      <strong>{{ formatBytes(summary?.totalStorageUsedBytes ?? 0) }}</strong>
    </el-card>
    <el-card class="panel-card hero-card">
      <h2>&#25903;&#25345;&#26029;&#28857;&#32493;&#20256;&#30340;&#22823;&#25991;&#20214;&#35270;&#39057;&#19978;&#20256;</h2>
      <p>&#24403;&#21069;&#38754;&#26495;&#24050;&#32463;&#25509;&#20837;&#21518;&#31471;&#26381;&#21153;&#12290;&#20320;&#21487;&#20197;&#22312;&#36825;&#37324;&#19978;&#20256;&#35270;&#39057;&#12289;&#26597;&#30475;&#35270;&#39057;&#21015;&#34920;&#65292;&#31649;&#29702;&#24179;&#21488;&#20869;&#23481;&#12290;</p>
      <div class="actions">
        <el-button type="primary" @click="router.push('/upload')">&#24320;&#22987;&#19978;&#20256;</el-button>
        <el-button @click="router.push('/videos')">&#26597;&#30475;&#35270;&#39057;</el-button>
        <el-button v-if="isAdmin" @click="router.push('/admin/users')">&#29992;&#25143;&#31649;&#29702;</el-button>
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getDashboardSummary } from '@/api/dashboard';
import { useAuthStore } from '@/store/auth';
import type { DashboardSummary } from '@/types/dashboard';

const router = useRouter();
const authStore = useAuthStore();
const summary = ref<DashboardSummary>();
const isAdmin = computed(() => (summary.value?.role || authStore.role) === 'ADMIN');
const roleMap: Record<string, string> = {
  ADMIN: '\u7ba1\u7406\u5458',
  UPLOADER: '\u4e0a\u4f20\u8005',
  VIEWER: '\u89c2\u770b\u8005',
};
const roleText = computed(() => roleMap[summary.value?.role || authStore.role] ?? '\u672a\u77e5');

onMounted(async () => {
  summary.value = await getDashboardSummary();
});

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  if (value < 1024 * 1024 * 1024) return `${(value / 1024 / 1024).toFixed(1)} MB`;
  return `${(value / 1024 / 1024 / 1024).toFixed(2)} GB`;
}
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.hero-card {
  grid-column: 1 / -1;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
