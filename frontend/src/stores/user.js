import { defineStore } from 'pinia'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  actions: {
    // 注册
    async register(formData) {
      try {
        console.log('Registering with:', {
          ...formData,
          password: formData.password ? '[MASKED]' : undefined
        })
        
        // 确保所有必要字段都存在
        if (!formData.username || !formData.password || !formData.name || !formData.email || !formData.role) {
          ElMessage.error('请填写所有必要信息')
          return false
        }

        // 按照API文档的顺序构造请求体
        const requestBody = {
          username: formData.username.trim(),
          password: formData.password,
          name: formData.name.trim(),
          email: formData.email.trim(),
          role: formData.role
        }

        const res = await axios.post('/api/auth/register', requestBody)
        console.log('Register response:', res.data)

        if (res.data.code === 200) {
          ElMessage.success('注册成功，请登录')
          return true
        } else {
          ElMessage.error(res.data.message || '注册失败')
          return false
        }
      } catch (error) {
        console.error('Register error:', error)
        const errorMsg = error.response?.data?.message || '注册失败'
        console.error('Error message:', errorMsg)
        ElMessage.error(errorMsg)
        return false
      }
    },

    // 登录
    async login(username, password) {
      try {
        console.log('Attempting login with:', { username, password })
        const res = await axios.post('/api/auth/login', {
          username,
          password
        })
        console.log('Login response:', res.data)
        
        const { token, user } = res.data
        console.log('Extracted data:', { token, user })
        
        // 直接传入 token 和 user 对象
        this.setAuth(token, user)
        
        ElMessage.success('登录成功')
        router.push('/resources')
        return true
      } catch (error) {
        console.error('Login error:', error)
        ElMessage.error(error.response?.data?.message || '登录失败')
        return false
      }
    },

    // 设置认证信息
    setAuth(token, userData) {
      console.log('Setting auth with:', { token, userData })
      
      this.token = token
      this.userInfo = {
        id: userData.id,
        username: userData.username,
        name: userData.name,
        email: userData.email,
        role: userData.role
      }
      
      console.log('Updated userInfo:', this.userInfo)
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    // 初始化用户信息
    async initUserInfo() {
      console.log('Initializing user info...')
      if (this.token) {
        try {
          const res = await axios.get('/api/auth/user-info')
          console.log('User info response:', res.data)
          const user = res.data
          this.userInfo = {
            id: user.id,
            username: user.username,
            name: user.name,
            email: user.email,
            role: user.role
          }
          console.log('Updated userInfo:', this.userInfo)
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        } catch (error) {
          console.error('Failed to fetch user info:', error)
          this.clearAuth()
        }
      }
    },

    // 清除认证信息
    clearAuth() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 退出登录
    logout() {
      this.clearAuth()
      router.push('/login')
    }
  },

  getters: {
    isAuthenticated: (state) => !!state.token && !!state.userInfo,
    userRole: (state) => state.userInfo?.role || '',
    isTeacher: (state) => state.userInfo?.role === 'TEACHER',
    isStudent: (state) => state.userInfo?.role === 'STUDENT',
    isAdmin: (state) => state.userInfo?.role === 'ADMIN',
    canGrade: (state) => ['TEACHER', 'ADMIN'].includes(state.userInfo?.role),
    displayRole: (state) => {
      const roleMap = {
        'STUDENT': '学生',
        'TEACHER': '教师',
        'ADMIN': '管理员'
      }
      return roleMap[state.userInfo?.role] || '未知'
    }
  }
})
