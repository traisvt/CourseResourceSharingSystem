<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2 class="login-title">课程资源共享系统</h2>
          <p class="login-subtitle">欢迎回来</p>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        @submit.prevent="handleLogin"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名"
            :prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :prefix-icon="Lock" 
            placeholder="密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
          <div class="register-link">
            <span>还没有账号？</span>
            <el-button
              type="text"
              @click="router.push('/register')"
              class="register-button"
            >
              立即注册
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import axios from '../utils/axios'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    const loginData = {
      username: form.username.trim(),
      password: form.password
    }
    console.log('Attempting login with:', loginData)
    
    const response = await axios.post('/api/auth/login', loginData)
    console.log('Login response:', response.data)

    const { token, user } = response.data
    // 分别传入 token 和 user 对象
    userStore.setAuth(token, user)
    
    ElMessage.success('登录成功')
    router.push('/resources')
  } catch (error) {
    console.error('Login error:', error)
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--bg-color) 100%);
}

.login-card {
  width: 420px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
  border-radius: var(--border-radius) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12) !important;
}

.login-title {
  text-align: center;
  margin: 0;
  color: var(--primary-color);
  font-size: 24px;
  font-weight: 600;
}

:deep(.el-form) {
  padding: 20px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  padding: 8px 15px;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-input__prefix-inner) {
  font-size: 18px;
  color: var(--text-secondary);
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(45deg, var(--primary-color), #66b1ff);
  border: none;
  margin-bottom: 16px;
}

.login-button:hover {
  background: linear-gradient(45deg, #66b1ff, var(--primary-color));
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.register-link {
  text-align: center;
  color: var(--text-regular);
  font-size: 14px;
}

.register-button {
  font-weight: 500;
  padding: 0 4px;
}

.register-button:hover {
  color: var(--primary-color);
  text-decoration: underline;
}

/* 响应式设计 */
@media screen and (max-width: 480px) {
  .login-card {
    width: 90%;
    margin: 0 20px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 14px;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-card {
  animation: fadeIn 0.6s ease-out;
}

.card-header {
  text-align: center;
  padding: 0 20px;
}

.login-subtitle {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.login-form {
  padding: 20px 30px 10px;
}

:deep(.el-input__wrapper) {
  background: var(--bg-color);
  border-radius: 8px !important;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  background: white;
}

.login-button {
  height: 44px;
  border-radius: 8px !important;
}
</style>
