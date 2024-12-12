<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2 class="register-title">注册账号</h2>
          <p class="register-subtitle">欢迎加入课程资源共享系统</p>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        @submit.prevent="handleRegister"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            :prefix-icon="User"
            placeholder="用户名"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :prefix-icon="Lock"
            placeholder="密码"
          />
        </el-form-item>

        <el-form-item prop="name">
          <el-input 
            v-model="form.name"
            :prefix-icon="UserFilled"
            placeholder="姓名"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input 
            v-model="form.email"
            :prefix-icon="Message"
            placeholder="邮箱"
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-select 
            v-model="form.role" 
            placeholder="选择角色"
            class="role-select"
          >
            <el-option label="学生" value="STUDENT">
              <div class="role-option">
                <el-icon><User /></el-icon>
                <span>学生</span>
              </div>
            </el-option>
            <el-option label="教师" value="TEACHER">
              <div class="role-option">
                <el-icon><UserFilled /></el-icon>
                <span>教师</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            立即注册
          </el-button>
          <div class="login-link">
            <span>已有账号？</span>
            <el-button
              type="text"
              @click="router.push('/login')"
              class="login-button"
            >
              立即登录
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
import { User, UserFilled, Lock, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  name: '',
  email: '',
  role: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true
    // 按照API文档的顺序构造请求数据
    const registerData = {
      username: form.username,
      password: form.password,
      name: form.name,
      email: form.email,
      role: form.role
    }
    
    const success = await userStore.register(registerData)
    if (success) {
      ElMessage.success('注册成功，即将跳转到登录页面')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    }
  } catch (error) {
    console.error('Form validation error:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--bg-color) 100%);
  padding: 20px;
}

.register-card {
  width: 420px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--border-radius) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12) !important;
}

.card-header {
  text-align: center;
  padding: 0 20px;
}

.register-title {
  margin: 0;
  color: var(--primary-color);
  font-size: 24px;
  font-weight: 600;
}

.register-subtitle {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.register-form {
  padding: 20px 30px 10px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  transition: all 0.3s ease;
  padding: 8px 15px;
  border-radius: 8px !important;
  background: var(--bg-color);
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08) !important;
  background: white;
}

:deep(.el-input__prefix-inner) {
  font-size: 18px;
  color: var(--text-secondary);
}

.role-select {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  background: var(--bg-color);
}

.role-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.role-option .el-icon {
  font-size: 16px;
  color: var(--text-regular);
}

.role-option span {
  font-size: 14px;
  color: var(--text-primary);
}

:deep(.el-select-dropdown__item.selected) {
  .role-option {
    color: var(--primary-color);
    .el-icon {
      color: var(--primary-color);
    }
    span {
      color: var(--primary-color);
    }
  }
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  background: linear-gradient(45deg, var(--primary-color), #66b1ff);
  border: none;
  margin-bottom: 16px;
  border-radius: 8px !important;
}

.register-button:hover {
  background: linear-gradient(45deg, #66b1ff, var(--primary-color));
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.login-link {
  text-align: center;
  color: var(--text-regular);
  font-size: 14px;
}

.login-button {
  font-weight: 500;
  padding: 0 4px;
}

.login-button:hover {
  color: var(--primary-color);
  text-decoration: underline;
}

/* 响应式设计 */
@media screen and (max-width: 480px) {
  .register-card {
    width: 100%;
  }
  
  .register-form {
    padding: 20px 20px 10px;
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

.register-card {
  animation: fadeIn 0.6s ease-out;
}
</style>
