<template>
  <div class="login-page">
    <div class="panel">
      <h2 class="title">社区养老服务系统</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="login-btn" style="width: 100%;" @click="onSubmit">登录</el-button>
      </el-form>
      <div class="register-tip">
        还没有账户？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })

const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 1, message: '用户名不能为空', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 1, message: '密码不能为空', trigger: 'blur' }
  ]
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      console.log('Starting login...')
      await auth.login(form.value)
      console.log('Login completed, auth.token:', auth.token)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/'
      console.log('Redirecting to:', redirect)
      router.replace(redirect)
    } catch (err) {
      console.error('Login error:', err)
      ElMessage.error(err.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  color: #1f2937;
  padding: 24px;
}

.panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 32px;
  width: 360px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
}

h2 {
  margin: 0 0 24px;
  font-weight: 700;
  color: #111827;
}

.title {
  text-align: center;
}

/* 删除副标题：不再显示“请登录继续” */

.register-tip {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.register-tip a {
  color: #3b82f6;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s;
}

.register-tip a:hover {
  color: #2563eb;
}

.login-btn {
  background-color: #3b82f6 !important;
  border-color: #3b82f6 !important;
  color: #ffffff !important;
  font-weight: 500;
}

.login-btn:hover, .login-btn:focus {
  background-color: #60a5fa !important;
  border-color: #60a5fa !important;
  color: #ffffff !important;
}

.login-btn:active {
  background-color: #2563eb !important;
  border-color: #2563eb !important;
}
</style>
