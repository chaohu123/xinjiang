import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/store/user'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 120000, // 增加到 120 秒，AI 生成路线可能需要更长时间
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token && config.headers) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    // 如果是FormData，删除Content-Type让浏览器自动设置（包括boundary）
    if (config.data instanceof FormData && config.headers) {
      delete config.headers['Content-Type']
    }

    // 如果是生成路线的请求，输出请求数据到浏览器控制台
    if (config.url?.includes('/routes/generate')) {
      console.group('🚀 [智能旅游线路规划] 发送给后端的路线生成请求')
      console.log('📍 请求URL:', config.url)
      console.log('📍 请求方法:', config.method?.toUpperCase())
      console.log('📍 请求时间:', new Date().toLocaleString('zh-CN'))
      console.log('📦 请求数据:', config.data)

      // 详细解析请求数据
      if (config.data) {
        const data = config.data
        console.log('📋 请求参数详情:')
        console.log('  - 目的地:', data.destinations || data.startLocation + ' → ' + data.endLocation)
        console.log('  - 行程天数:', data.duration)
        console.log('  - 人数:', data.peopleCount)
        console.log('  - 预算:', data.totalBudget || data.dailyBudget || data.budget)
        console.log('  - 风格偏好:', data.stylePreferences || data.interests)
        console.log('  - 必看景点:', data.mustVisit || data.mustVisitLocations)
        console.log('  - 必须避开:', data.mustAvoid)
      }

      console.log('🔧 完整请求配置:', config)
      console.groupEnd()
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 如果是生成路线的响应，只检查是否成功
    if (response.config.url?.includes('/routes/generate')) {
      if (res && typeof res === 'object' && 'data' in res && res.data) {
        console.log('接收数据成功')
      } else {
        console.log('失败')
      }
    }

    // 如果返回的状态码不是200，说明有错误
    if (res && typeof res === 'object' && 'code' in res && res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // 如果返回的是 ApiResponse 格式，返回 data 字段
    if (res && typeof res === 'object' && 'data' in res) {
      return res.data
    }

    // 如果直接返回数据，直接返回
    return res
  },
  error => {
    const userStore = useUserStore()

    // 如果是生成路线的错误响应，只输出失败信息
    if (error.config?.url?.includes('/routes/generate')) {
      console.log('失败')
    }

    if (error.response) {
      const responseData = error.response.data
      let errorMessage = '请求失败'

      // 尝试从 ApiResponse 格式中提取错误消息
      if (responseData) {
        if (typeof responseData === 'object') {
          if (responseData.message) {
            errorMessage = responseData.message
          } else if (
            responseData.data &&
            typeof responseData.data === 'object' &&
            responseData.data.message
          ) {
            errorMessage = responseData.data.message
          }
        } else if (typeof responseData === 'string') {
          errorMessage = responseData
        }
      }

      switch (error.response.status) {
        case 400:
          // 400 错误通常是验证失败或业务逻辑错误
          ElMessage.error(errorMessage || '请求参数错误')
          break
        case 401:
          // 登录页面不需要自动跳转
          if (router.currentRoute.value.path !== '/login') {
            ElMessage.error('未授权，请重新登录')
            userStore.logout()
            router.push('/login')
          } else {
            ElMessage.error(errorMessage || '用户名或密码错误')
          }
          break
        case 403:
          ElMessage.error(errorMessage || '拒绝访问')
          break
        case 404:
          // 404错误：如果是管理员API路径，不显示错误消息，让组件自己处理
          const isAdminApi = error.config?.url?.includes('/admin/')
          if (!isAdminApi) {
            ElMessage.error(errorMessage || '请求错误，未找到该资源')
          }
          break
        case 500:
          // 500错误：如果是管理员API路径，不显示错误消息，让组件自己处理
          const isAdminApi500 = error.config?.url?.includes('/admin/')
          if (!isAdminApi500) {
            ElMessage.error(errorMessage || '服务器错误')
          }
          break
        default:
          ElMessage.error(errorMessage)
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    // 创建一个包含错误消息和response的 Error 对象，以便在 catch 块中使用
    const errorWithMessage: any = new Error(
      error.response?.data?.message || error.message || '请求失败'
    )
    // 保留原始错误信息，包括response
    errorWithMessage.response = error.response
    errorWithMessage.status = error.response?.status
    return Promise.reject(errorWithMessage)
  }
)

export function setupAxios() {
  // 可以在这里进行全局配置
}

export default service
