import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

import Login from '../views/LoginView.vue'
import Register from '../views/RegisterView.vue'
import Layout from '../views/LayoutShell.vue'
import Dashboard from '../views/DashboardView.vue'
import Elderly from '../views/ElderlyView.vue'
import Caregivers from '../views/CaregiverView.vue'
import Services from '../views/ServiceView.vue'
import Appointments from '../views/AppointmentView.vue'
import Fees from '../views/FeeRecordView.vue'
import Users from '../views/UserView.vue'
import Evaluations from '../views/EvaluationView.vue'
import HealthRecord from '../views/HealthRecordView.vue'
import EmergencyHelp from '../views/EmergencyHelpView.vue'
import CommunityActivity from '../views/CommunityActivityView.vue'
import Notification from '../views/NotificationView.vue'
import Income from '../views/IncomeView.vue'
import Feedback from '../views/FeedbackView.vue'
import Debug from '../views/DebugView.vue'
import Test from '../views/TestView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { 
      path: '/login', 
      name: 'login', 
      component: Login, 
      meta: { public: true },
      beforeEnter: (to, from, next) => {
        const auth = useAuthStore()
        if (auth.token) {
          next('/')
        } else {
          next()
        }
      }
    },
    { path: '/register', name: 'register', component: Register, meta: { public: true } },
    { path: '/debug', name: 'debug', component: Debug, meta: { public: true } },
    { path: '/test', name: 'test', component: Test, meta: { public: true } },
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', name: 'dashboard', component: Dashboard, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        // 管理员页面
        { path: 'elderly', name: 'elderly', component: Elderly, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        { path: 'caregivers', name: 'caregivers', component: Caregivers, meta: { roles: ['admin'] } },
        { path: 'services', name: 'services', component: Services, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        { path: 'appointments', name: 'appointments', component: Appointments, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        { path: 'fees', name: 'fees', component: Fees, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        { path: 'evaluations', name: 'evaluations', component: Evaluations, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        { path: 'users', name: 'users', component: Users, meta: { roles: ['admin'] } },
        // 健康记录（统一功能，管理员、护工、居民都使用）
        { path: 'health-records', name: 'healthRecords', component: HealthRecord, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        // 已移除发布需求功能
        // 已移除报价管理（服务提供商）
        { path: 'emergency-help', name: 'emergencyHelp', component: EmergencyHelp, meta: { roles: ['resident'] } },
        { path: 'activities', name: 'activities', component: CommunityActivity, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        { path: 'notifications', name: 'notifications', component: Notification, meta: { roles: ['admin', 'resident', 'caregiver'] } },
        { path: 'income', name: 'income', component: Income, meta: { roles: ['admin', 'caregiver'] } },
        { path: 'feedback', name: 'feedback', component: Feedback, meta: { roles: ['admin', 'resident', 'caregiver'] } }
      ]
    }
    ,
    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

// 根据角色获取默认路由
const getDefaultRoute = (role) => {
  switch(role) {
    case 'admin':
      return '/'
    case 'resident':
      return '/'
    // 移除服务提供商默认路由
    default:
      return '/login'
  }
}

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (!auth.initialized) {
    auth.hydrate()
  }
  if (to.meta.public) {
    next()
    return
  }
  if (!auth.token) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }
  
  // 角色权限检查 - 如果 user 还没加载，等待或重新初始化
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRole = auth.user?.role
    if (!userRole) {
      // user 可能还没完全加载，尝试重新 hydrate
      auth.hydrate()
      const retryRole = auth.user?.role
      if (!retryRole) {
        // 如果还是没有，强制重新登录
        auth.logout()
        next({ name: 'login', query: { redirect: to.fullPath } })
        return
      }
      if (!to.meta.roles.includes(retryRole)) {
        const defaultRoute = getDefaultRoute(retryRole)
        next({ path: defaultRoute })
        return
      }
    } else if (!to.meta.roles.includes(userRole)) {
      const defaultRoute = getDefaultRoute(userRole)
      next({ path: defaultRoute })
      return
    }
  }
  
  next()
})

export default router
