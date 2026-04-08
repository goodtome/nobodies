<template>
  <div class="page-shell auth-page">
    <el-card class="auth-card form-card">
      <div class="auth-header">
        <h2>创建账号</h2>
        <p>注册一个新的上传账号以便测试使用。</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-button type="primary" :loading="submitting" class="submit" @click="handleSubmit">注册</el-button>
      </el-form>
      <el-button link @click="router.push('/login')">返回登录</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const authStore = useAuthStore();
const submitting = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({
  email: '',
  password: '',
  confirmPassword: '',
});

const rules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
});

async function handleSubmit() {
  if (!formRef.value) return;
  
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        await authStore.register(form);
        ElMessage.success('注册成功，请登录');
        router.push('/login');
      } catch (error: any) {
        ElMessage.error(error.message || '注册失败');
      } finally {
        submitting.value = false;
      }
    }
  });
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
