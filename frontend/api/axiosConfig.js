import axios from 'axios'
import { API_BASE_URL } from './baseUrl'

// 创建 axios 实例
const api = axios.create({
  baseURL: API_BASE_URL, // 后端 API 地址
  timeout: 10000, // 10秒超时
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加 token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理错误
api.interceptors.response.use(
  response => {
    // 后端返回的是 { status, success, message } 格式
    // message 字段包含实际数据
    if (response.data && response.data.success === true) {
      // 返回数据对象，data 字段是实际数据
      return {
        data: response.data.message,
        status: response.data.status,
        success: response.data.success
      }
    } else {
      // 业务错误
      const errorMessage = typeof response.data.message === 'string'
        ? response.data.message
        : '请求失败'
      alert(errorMessage)
      return Promise.reject(new Error(errorMessage))
    }
  },
  error => {
    // HTTP 错误
    if (error.response) {
      const status = error.response.status
      const errorData = error.response.data

      // 如果后端返回了明确的 message（比如 409 冲突），优先展示它
      if (typeof errorData === 'object' && typeof errorData.message === 'string' && errorData.message.trim()) {
        alert(errorData.message)
        return Promise.reject(error)
      }

      let message = '请求失败'

      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          localStorage.removeItem('token')
          window.location.href = '/auth'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = '请求失败'
      }
      alert(message)
    } else if (error.request) {
      alert('网络错误，请检查网络连接')
    } else {
      alert('请求失败：' + error.message)
    }
    return Promise.reject(error)
  }
)

export default api
