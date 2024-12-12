import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const instance = axios.create({
  baseURL: 'http://localhost:8080',  // 添加后端API的基础URL
  timeout: 5000
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('Current request:', config.url)
    console.log('Token exists:', !!token)
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
      console.log('Authorization header:', config.headers['Authorization'])
    }
    return config
  },
  error => {
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response) {
      const userStore = useUserStore()
      
      switch (error.response.status) {
        case 401:
          // token过期或无效，清除认证信息并跳转登录页
          ElMessage.error('登录已过期，请重新登录')
          userStore.clearAuth()
          break
        case 403:
          ElMessage.error('没有权限进行此操作')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default instance
