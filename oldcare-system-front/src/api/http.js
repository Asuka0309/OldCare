import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

instance.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (!auth.initialized) {
    auth.hydrate()
  }
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res.code !== 'undefined') {
      if (res.code !== 0) {
        // 返回错误对象，让调用者决定如何处理
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      // 返回 data 字段的内容
      return res.data || res
    }
    return response.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export default instance
