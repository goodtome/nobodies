<template>
  <section>
    <div class="toolbar">
      <h2>&#29992;&#25143;&#31649;&#29702;</h2>
      <el-button @click="fetchUsers">&#21047;&#26032;</el-button>
    </div>

    <el-table :data="users" class="panel-card" v-loading="loading">
      <el-table-column prop="email" label="&#37038;&#31665;" min-width="220" />
      <el-table-column label="&#35282;&#33394;" width="120">
        <template #default="scope">{{ roleMap[scope.row.role] ?? scope.row.role }}</template>
      </el-table-column>
      <el-table-column prop="videoCount" label="&#35270;&#39057;&#25968;" width="100" />
      <el-table-column label="&#23384;&#20648;&#29992;&#37327;" width="160">
        <template #default="scope">{{ formatBytes(scope.row.storageUsedBytes) }}</template>
      </el-table-column>
      <el-table-column label="&#20462;&#25913;&#35282;&#33394;" min-width="220">
        <template #default="scope">
          <div class="role-actions">
            <el-select v-model="scope.row.role" style="width: 140px">
              <el-option label="&#19978;&#20256;&#32773;" value="UPLOADER" />
              <el-option label="&#35266;&#30475;&#32773;" value="VIEWER" />
              <el-option label="&#31649;&#29702;&#21592;" value="ADMIN" />
            </el-select>
            <el-button type="primary" @click="saveRole(scope.row)">&#20445;&#23384;</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getUsers, updateUserRole } from '@/api/user';
import type { UserSummary } from '@/types/user';

const users = ref<UserSummary[]>([]);
const loading = ref(false);
const roleMap: Record<string, string> = {
  ADMIN: '\u7ba1\u7406\u5458',
  UPLOADER: '\u4e0a\u4f20\u8005',
  VIEWER: '\u89c2\u770b\u8005',
};

onMounted(fetchUsers);

async function fetchUsers() {
  loading.value = true;
  try {
    users.value = await getUsers();
  } finally {
    loading.value = false;
  }
}

async function saveRole(user: UserSummary) {
  await updateUserRole(user.id, user.role);
  ElMessage.success('\u89d2\u8272\u66f4\u65b0\u6210\u529f');
  await fetchUsers();
}

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`;
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`;
  if (value < 1024 * 1024 * 1024) return `${(value / 1024 / 1024).toFixed(1)} MB`;
  return `${(value / 1024 / 1024 / 1024).toFixed(2)} GB`;
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.role-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
