<template>
  <div class="register-container">
    <el-card class="register-card">
      <div class="register-header">
        <h1>注册账号</h1>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @keyup.enter="submit">
        <el-form-item label="选择身份" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="resident">社区居民</el-radio>
            <el-radio value="caregiver">社区护工</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="form.password" 
            type="password"
            placeholder="至少6位密码"
            prefix-icon="Lock"
            clearable
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            clearable
            show-password
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            v-model="form.realName" 
            placeholder="请输入真实姓名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="电话" prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入联系电话"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item v-if="form.role === 'caregiver'" label="工作单位（可选）" prop="companyName">
          <el-input 
            v-model="form.companyName" 
            placeholder="请输入所属机构或公司名称"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit" class="register-btn">注册</el-button>
        </el-form-item>
      </el-form>

      <div class="login-link">
        已有账户？<router-link to="/login">返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  role: 'resident',
  companyName: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码至少6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3位', trigger: 'blur' }
  ],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话号码', trigger: 'blur' }],
  companyName: [{ required: false, trigger: 'blur' }]
}

async function submit() {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
  } catch (error) {
    // 表单验证失败
    return
  }

  loading.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
      realName: form.realName,
      phone: form.phone,
      role: form.role
    }

    // 如果是服务提供商，添加公司名称
    if (form.role === 'service_provider' && form.companyName) {
      payload.companyName = form.companyName
    }

    await register(payload)
    ElMessage.success('注册成功，请登录')
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #ffffff;
}

.register-card {
  width: 100%;
  max-width: 460px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 24px 24px 8px;
}

.register-header {
  text-align: center;
  margin-bottom: 20px;
}

.register-header h1 {
  margin: 0;
  color: #333;
  font-size: 26px;
  font-weight: 700;
}

.el-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

:deep(.register-btn.el-button--primary) {
  width: 100%;
  height: 40px;
  font-size: 16px;
  background-color: #E4EFF1 !important;
  border-color: #E4EFF1 !important;
  color: #1f2937 !important;
  font-weight: 600;
}

:deep(.register-btn.el-button--primary:hover),
:deep(.register-btn.el-button--primary:focus) {
  background-color: #cfdfe2 !important;
  border-color: #cfdfe2 !important;
  color: #0f172a !important;
}

:deep(.register-btn.el-button--primary:active) {
  background-color: #b8cdd1 !important;
  border-color: #b8cdd1 !important;
}

/* 输入框去除蓝色背景 */
:deep(.register-card .el-input__wrapper) {
  background-color: #ffffff !important;
  box-shadow: none !important;
}

:deep(.register-card .el-input__wrapper:hover),
:deep(.register-card .el-input__wrapper.is-focus),
:deep(.register-card .el-input__wrapper.is-focus:hover) {
  background-color: #ffffff !important;
  box-shadow: 0 0 0 1px #dcdfe6 inset !important;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  cursor: pointer;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
