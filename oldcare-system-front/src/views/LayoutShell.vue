<template>
  <el-container class="layout-shell">
    <el-aside width="240px" class="aside">
      <div class="brand">
        <div class="brand-icon">🏥</div>
        <div class="brand-text">
          <div class="brand-title">OldCare</div>
          <div class="brand-subtitle">智慧养老管理平台</div>
        </div>
      </div>
      <el-menu :default-active="active" class="menu" router>
        <el-menu-item v-for="item in visibleMenuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <div class="page-title">{{ pageTitle }}</div>
          <div class="breadcrumb">
            <span class="breadcrumb-home">首页</span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-current">{{ pageTitle }}</span>
          </div>
        </div>
        <div class="user-box">
          <el-badge :value="0" :hidden="true" class="notification-badge">
            <el-icon :size="20" style="cursor: pointer;"><Bell /></el-icon>
          </el-badge>
          <el-dropdown>
            <div class="user-info">
              <el-avatar :size="36" style="background: #E4EFF1; color: #1f2937; font-weight: 600;">
                {{ (auth.user?.realName || auth.user?.username || 'U').charAt(0) }}
              </el-avatar>
              <div class="user-details">
                <div class="user-name">{{ auth.user?.realName || auth.user?.username }}</div>
                <div class="user-role">{{ roleText(auth.user?.role) }}</div>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ auth.user?.username }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <Suspense>
          <template #default>
            <RouterView />
          </template>
          <template #fallback>
            <div class="view-loading">正在加载模块…</div>
          </template>
        </Suspense>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { User, UserFilled, List, Calendar, Monitor, CreditCard, Setting, ChatDotSquare, Bell, SwitchButton, Files, HomeFilled, DataLine, Plus, Phone, TrendCharts } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => route.path)

// 菜单配置：定义不同角色可见的菜单项
const adminMenuItems = [
  { path: '/', icon: HomeFilled, title: '首页', roles: ['admin'] },
  { path: '/elderly', icon: User, title: '老人管理', roles: ['admin'] },
  { path: '/caregivers', icon: UserFilled, title: '员工管理', roles: ['admin'] },
  { path: '/services', icon: List, title: '服务项目', roles: ['admin', 'resident'] },
  { path: '/appointments', icon: Calendar, title: '服务预约', roles: ['admin', 'resident'] },
  { path: '/health-records', icon: Monitor, title: '健康记录', roles: ['admin'] },
  { path: '/activities', icon: Calendar, title: '社区活动', roles: ['admin'] },
  { path: '/fees', icon: CreditCard, title: '费用记录', roles: ['admin', 'resident'] },
  { path: '/users', icon: Setting, title: '用户管理', roles: ['admin'] },
  { path: '/evaluations', icon: ChatDotSquare, title: '评价反馈', roles: ['admin', 'resident'] },
  { path: '/income', icon: TrendCharts, title: '收入统计', roles: ['admin', 'caregiver'] }
]

const residentMenuItems = [
  { path: '/', icon: HomeFilled, title: '首页', roles: ['resident'] },
  { path: '/appointments', icon: Calendar, title: '服务预约', roles: ['resident'] },
  { path: '/health-records', icon: Monitor, title: '健康记录', roles: ['resident'] },
  { path: '/emergency-help', icon: Phone, title: '紧急求助', roles: ['resident'] },
  { path: '/activities', icon: Calendar, title: '社区活动', roles: ['resident'] },
  { path: '/fees', icon: CreditCard, title: '费用记录', roles: ['resident'] },
  { path: '/evaluations', icon: ChatDotSquare, title: '服务评价', roles: ['resident'] }
]

// 已移除服务提供商相关菜单

const caregiverMenuItems = [
  { path: '/', icon: HomeFilled, title: '首页', roles: ['caregiver'] },
  { path: '/notifications', icon: Bell, title: '我的通知', roles: ['caregiver'] },
  { path: '/appointments', icon: Calendar, title: '预约管理', roles: ['caregiver'] },
  { path: '/health-records', icon: Monitor, title: '健康记录', roles: ['caregiver'] },
  { path: '/activities', icon: Calendar, title: '社区活动', roles: ['caregiver'] },
  { path: '/fees', icon: CreditCard, title: '费用记录', roles: ['caregiver'] },
  { path: '/evaluations', icon: ChatDotSquare, title: '评价反馈', roles: ['caregiver'] },
  { path: '/income', icon: TrendCharts, title: '我的收入', roles: ['caregiver'] }
]

// 根据用户角色返回相应的菜单项
const visibleMenuItems = computed(() => {
  const userRole = auth.user?.role
  
  if (userRole === 'admin') {
    return adminMenuItems.filter(item => item.roles.includes(userRole))
  } else if (userRole === 'resident') {
    return residentMenuItems.filter(item => item.roles.includes(userRole))
  } else if (userRole === 'caregiver') {
    return caregiverMenuItems.filter(item => item.roles.includes(userRole))
  }
  
  return []
})

const titleMap = {
  '/': '首页',
  '/elderly': '老人管理',
  '/caregivers': '员工管理',
  '/services': '服务浏览',
  '/appointments': '服务预约',
  '/health-records': '健康记录',
  '/fees': '费用记录',
  '/users': '用户管理',
  '/evaluations': '评价反馈',
  // 已移除报价管理
  '/health-records': '健康记录',
  '/emergency-help': '紧急求助',
  '/activities': '社区活动',
  '/notifications': '我的通知',
  '/income': '收入统计'
}

const pageTitle = computed(() => titleMap[route.path] || 'OldCare')

function roleText(role) {
  const map = {
    admin: '系统管理员',
    resident: '社区居民',
    // 已移除服务提供商角色
    caregiver: '员工'
  }
  return map[role] || role
}

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
  background: #E4EFF1;
}

.aside {
  background: #E4EFF1;
  position: relative;
  overflow: hidden;
}

.aside::before {
  display: none;
}

.brand {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.brand-icon {
  font-size: 32px;
  line-height: 1;
}

.brand-text {
  flex: 1;
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
  letter-spacing: 0.3px;
}

.menu {
  border-right: none;
  background: transparent;
  padding: 12px 0;
  position: relative;
  z-index: 1;
}

.menu :deep(.el-menu-item) {
  color: #4b5563;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  height: 48px;
  line-height: 48px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(0, 0, 0, 0.03);
  color: #1f2937;
}

.menu :deep(.el-menu-item.is-active) {
  background: #cfdfe2;
  color: #0f172a;
  font-weight: 600;
  box-shadow: none;
}

.menu :deep(.el-icon) {
  font-size: 18px;
  margin-right: 8px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  background: #E4EFF1;
  height: 64px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.2;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #9ca3af;
}

.breadcrumb-home {
  cursor: pointer;
  transition: color 0.2s;
}

.breadcrumb-home:hover {
  color: #3b82f6;
}

.breadcrumb-separator {
  color: #d1d5db;
}

.breadcrumb-current {
  color: #6b7280;
  font-weight: 500;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  cursor: pointer;
  color: #6b7280;
  transition: color 0.2s;
}

.notification-badge:hover {
  color: #3b82f6;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #E4EFF1;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.2;
}

.main {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
  border-top-left-radius: 30px;
  box-shadow: inset 4px 4px 12px rgba(0, 0, 0, 0.02);
}

.view-loading {
  min-height: calc(100vh - 64px - 48px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
