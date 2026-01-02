<template>
  <div style="padding: 20px; font-family: monospace; background: #f5f5f5;">
    <h1>系统调试信息</h1>
    
    <div style="background: white; padding: 10px; margin: 10px 0; border: 1px solid #ccc;">
      <h3>认证状态</h3>
      <p>Token: {{ auth.token ? '✅ 已设置' : '❌ 未设置' }}</p>
      <p>User: {{ auth.user ? JSON.stringify(auth.user) : '❌ 未设置' }}</p>
      <p>Initialized: {{ auth.initialized }}</p>
    </div>

    <div style="background: white; padding: 10px; margin: 10px 0; border: 1px solid #ccc;">
      <h3>当前路由</h3>
      <p>Path: {{ route.path }}</p>
      <p>Name: {{ route.name }}</p>
    </div>

    <div style="background: white; padding: 10px; margin: 10px 0; border: 1px solid #ccc;">
      <h3>操作</h3>
      <button @click="goToLogin" style="padding: 10px 20px; cursor: pointer;">
        前往登录页
      </button>
    </div>

    <div style="background: white; padding: 10px; margin: 10px 0; border: 1px solid #ccc;">
      <h3>后端连接</h3>
      <button @click="testBackend" style="padding: 10px 20px; cursor: pointer;">
        测试后端API
      </button>
      <p v-if="backendStatus">{{ backendStatus }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const backendStatus = ref('')

const goToLogin = () => {
  router.push('/login')
}

const testBackend = async () => {
  try {
    backendStatus.value = '⏳ 测试中...'
    const response = await fetch('http://localhost:8080/api/health-records', {
      headers: {
        'Authorization': auth.token ? `Bearer ${auth.token}` : ''
      }
    })
    backendStatus.value = `✅ 后端响应: ${response.status} ${response.statusText}`
  } catch (err) {
    backendStatus.value = `❌ 连接失败: ${err.message}`
  }
}
</script>
