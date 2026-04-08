<template>
  <div class="page-shell auth-page">
    <el-card class="auth-card form-card">
      <div class="auth-header">
        <h2>&#25214;&#22238;&#23494;&#30721;</h2>
        <p>&#36755;&#20837;&#27880;&#20876;&#37038;&#31665;&#65292;&#25105;&#20204;&#20250;&#21521;&#20320;&#21457;&#36865;&#19968;&#23553;&#37325;&#32622;&#23494;&#30721;&#37038;&#20214;&#12290;</p>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="&#37038;&#31665;">
          <el-input v-model="form.email" placeholder="&#35831;&#36755;&#20837;&#27880;&#20876;&#37038;&#31665;" />
        </el-form-item>
        <el-button type="primary" :loading="submitting" class="submit" @click="handleSubmit">&#21457;&#36865;&#37325;&#32622;&#37038;&#20214;</el-button>
      </el-form>
      <el-button link @click="router.push('/login')">&#36820;&#22238;&#30331;&#24405;</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { requestPasswordReset } from '@/api/auth';

const router = useRouter();
const submitting = ref(false);
const form = reactive({
  email: '',
});

async function handleSubmit() {
  submitting.value = true;
  try {
    await requestPasswordReset(form);
    ElMessage.success('\u5982\u679c\u8be5\u90ae\u7bb1\u5df2\u6ce8\u518c\uff0c\u6211\u4eec\u5df2\u53d1\u9001\u91cd\u7f6e\u5bc6\u7801\u90ae\u4ef6');
    router.push('/login');
  } catch (error) {
    ElMessage.error('\u53d1\u9001\u91cd\u7f6e\u90ae\u4ef6\u5931\u8d25');
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
</style>