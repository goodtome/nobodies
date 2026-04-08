<template>
  <div class="page-shell auth-page">
    <el-card class="auth-card form-card">
      <div class="auth-header">
        <h2>&#27426;&#36814;&#22238;&#26469;</h2>
        <p>&#30331;&#24405;&#21518;&#21363;&#21487;&#31649;&#29702;&#35270;&#39057;&#19978;&#20256;&#21644;&#35270;&#39057;&#20869;&#23481;&#12290;</p>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="&#37038;&#31665;">
          <el-input v-model="form.email" placeholder="&#35831;&#36755;&#20837;&#37038;&#31665;" />
        </el-form-item>
        <el-form-item label="&#23494;&#30721;">
          <el-input v-model="form.password" type="password" show-password placeholder="&#35831;&#36755;&#20837;&#23494;&#30721;" />
        </el-form-item>
        <el-button type="primary" :loading="submitting" class="submit" @click="handleSubmit">&#30331;&#24405;</el-button>
      </el-form>
      <div class="auth-links">
        <el-button link @click="router.push('/forgot-password')">&#24536;&#35760;&#23494;&#30721;</el-button>
        <el-button link @click="router.push('/register')">&#21019;&#24314;&#36134;&#21495;</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const authStore = useAuthStore();
const submitting = ref(false);
const form = reactive({
  email: 'uploader@example.com',
  password: 'Password123',
});

async function handleSubmit() {
  submitting.value = true;
  try {
    await authStore.login(form);
    ElMessage.success('\u767b\u5f55\u6210\u529f');
    router.push('/dashboard');
  } catch (error) {
    ElMessage.error('\u767b\u5f55\u5931\u8d25');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.auth-page {
  display: grid;
  place-items: center;
  padding: 24px;
}

.form-card {
  width: min(440px, 100%);
  padding: 12px;
}

.auth-header h2 {
  margin: 0 0 8px;
}

.auth-header p {
  margin: 0 0 24px;
  color: #667085;
}

.submit {
  width: 100%;
}

.auth-links {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}
</style>