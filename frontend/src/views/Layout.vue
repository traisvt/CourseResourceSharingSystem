<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">课程资源共享系统</div>
      <div class="user-info">
        <span>{{ userStore.userInfo?.username }}</span>
        <el-button type="text" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    
    <el-container>
      <el-aside width="200px">
        <el-menu
          :router="true"
          :default-active="route.path"
          class="menu"
        >
          <el-menu-item index="/resources">
            <el-icon><Document /></el-icon>
            <span>资源管理</span>
          </el-menu-item>
          <el-menu-item index="/assignments">
            <el-icon><List /></el-icon>
            <span>作业管理</span>
          </el-menu-item>
          <el-menu-item index="/submissions">
            <el-icon><Tickets /></el-icon>
            <span>作业提交</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Document, List, Tickets } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu {
  height: 100%;
  border-right: none;
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
