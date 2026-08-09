import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        ElMessage.error('登录已过期,请重新登录')
      } else {
        ElMessage.error(res.message || '操作失败')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    ElMessage.error('网络异常,请稍后重试')
    return Promise.reject(error)
  }
)

export default request
