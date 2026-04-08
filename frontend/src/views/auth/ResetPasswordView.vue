<template>
  <div class="page-shell auth-page">
    <el-card class="auth-card form-card">
      <div class="auth-header">
        <h2>&#37325;&#32622;&#23494;&#30721;</h2>
        <p>&#35774;&#32622;&#19968;&#20010;&#26032;&#30340;&#30331;&#24405;&#23494;&#30721;&#12290;&#23494;&#30721;&#33267;&#23569;&#38656;&#35201; 8 &#20301;&#12290;</p>
      </div>
      <el-alert v-if="!token" title="&#37325;&#32622;&#38142;&#25509;&#26080;&#25928;&#65292;&#35831;&#37325;&#26032;&#30003;&#35831;&#37325;&#32622;&#23494;&#30721;&#37038;&#20214;&#12290;" type="error" show-icon :closable="false" />
      <el-form v-else :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="&#26032;&#23494;&#30721;">
          <el-input v-model="form.password" type="password" show-password placeholder="&#35831;&#36755;&#20837;&#26032;&#23494;&#30721;" />
        </el-form-item>
        <el-form-item label="&#30830;&#35748;&#23494;&#30721;">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="&#35831;&#20877;&#27425;&#36755;&#20837;&#26032;&#23494;&#30721;" />
        </el-form-item>
        <el-button type="primary" :loading="submitting" class="submit" @click="handleSubmit">&#30830;&#35748;&#37325;&#32622;</el-button>
      </el-form>
      <el-button link @click="router.push('/login')">&#36820;&#22238;&#30331;&#24405;</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { confirmPasswordReset } from '@/api/auth';

const route = useRoute();
const router = useRouter();
const submitting = ref(false);
const token = computed(() => String(route.query.token ?? ''));
const form = reactive({
  password: '',
  confirmPassword: '',
});

async function handleSubmit() {
  if (!token.value) {
    ElMessage.error('\u91cd\u7f6e\u94fe\u63a5\u65e0\u6548');
    return;
  }
  if (form.password.length < 8) {
    ElMessage.error('\u5bc6\u7801\u81f3\u5c11\u9700\u8981 8 \u4f4d');
    return;
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.error('\u4e24\u6b21\u8f93\u5165\u7684\u5bc6\u7801\u4e0d\u4e00\u81f4');
    return;
  }

  submitting.value = true;
  try {
    await confirmPasswordReset({ token: token.value, password: form.password });
    ElMessage.success('\u5bc6\u7801\u91cd\u7f6e\u6210\u529f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55');
    router.push('/login');
  } catch (error) {
    ElMessage.error('\u5bc6\u7801\u91cd\u7f6e\u5931\u8d25\uff0c\u94fe\u63a5\u53ef\u80fd\u5df2\u5931\u6548');
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