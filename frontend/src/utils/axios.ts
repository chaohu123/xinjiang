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

    // 如果是生成路线的响应，输出完整数据到浏览器控制台
    if (response.config.url?.includes('/routes/generate')) {
      console.group('✅ [智能旅游线路规划] 后端返回的路线生成响应')
      console.log('📍 响应时间:', new Date().toLocaleString('zh-CN'))
      console.log('📍 响应状态码:', response.status)
      console.log('📍 响应状态文本:', response.statusText)
      console.log('📦 原始响应数据:', response.data)

      // 如果返回的是 ApiResponse 格式，也输出解析后的数据
      if (res && typeof res === 'object' && 'data' in res) {
        console.log('🎯 解析后的路线数据:', res.data)

        // 检查是否包含调试信息（后端可能返回的deepseek API调用状态）
        if (res.debugInfo) {
          console.group('🔍 DeepSeek API 调用调试信息')
          console.log('  - API调用状态:', res.debugInfo.apiCallStatus || '未知')
          console.log('  - 是否成功调用:', res.debugInfo.apiCallSuccess || false)
          console.log('  - API响应时间:', res.debugInfo.apiResponseTime || '未知')
          console.log('  - 使用的AI Provider:', res.debugInfo.aiProvider || '未知')
          if (res.debugInfo.apiError) {
            console.error('  - API错误信息:', res.debugInfo.apiError)
          }
          console.groupEnd()
        }

        console.log('📋 路线详情:')
        if (res.data) {
          const routeData = res.data
          console.log('  ✅ 路线ID:', routeData.id)
          console.log('  ✅ 路线标题:', routeData.title)
          console.log('  ✅ 路线描述:', routeData.description)
          console.log('  ✅ 行程天数:', routeData.duration)
          console.log('  ✅ 起点:', routeData.startLocation)
          console.log('  ✅ 终点:', routeData.endLocation)

          if (routeData.itinerary && Array.isArray(routeData.itinerary)) {
            console.log('  📅 行程安排 (', routeData.itinerary.length, '天):')
            routeData.itinerary.forEach((item: any, index: number) => {
              console.group(`  📍 第${item.day}天: ${item.title}`)
              console.log('    描述:', item.description)

              if (item.timeSchedule) {
                console.log('    ⏰ 时间安排:', item.timeSchedule)
              }

              if (item.transportation) {
                console.log('    🚗 交通信息:', item.transportation)
              }

              if (item.dailyBudget) {
                console.log('    💰 每日预算:', item.dailyBudget)
              }

              if (item.locations && Array.isArray(item.locations)) {
                console.log('    🏛️ 景点数量:', item.locations.length)
                item.locations.forEach((loc: any, locIndex: number) => {
                  console.log(`      ${locIndex + 1}. ${loc.name}`)
                  if (loc.lat && loc.lng) {
                    console.log(`         坐标: (${loc.lat}, ${loc.lng})`)
                  } else {
                    console.warn('         ⚠️ 缺少坐标信息')
                  }
                  if (loc.description) {
                    console.log('         描述:', loc.description.substring(0, 100) + (loc.description.length > 100 ? '...' : ''))
                  }
                })
              } else {
                console.warn('    ⚠️ 没有景点信息')
              }

              if (item.accommodation) {
                console.log('    🏨 住宿:', item.accommodation.substring(0, 100) + (item.accommodation.length > 100 ? '...' : ''))
              }

              if (item.meals && Array.isArray(item.meals)) {
                console.log('    🍽️ 餐饮 (', item.meals.length, '餐):')
                item.meals.forEach((meal: string, mealIndex: number) => {
                  console.log(`      ${mealIndex + 1}. ${meal.substring(0, 80)}${meal.length > 80 ? '...' : ''}`)
                })
              }

              console.groupEnd()
            })
          } else {
            console.warn('  ⚠️ 没有行程安排数据')
          }

          if (routeData.tips && Array.isArray(routeData.tips)) {
            console.log('  💡 提示信息 (', routeData.tips.length, '条):')
            routeData.tips.forEach((tip: string, index: number) => {
              console.log(`    ${index + 1}. ${tip.substring(0, 100)}${tip.length > 100 ? '...' : ''}`)
            })
          } else {
            console.warn('  ⚠️ 没有提示信息')
          }

          // 数据完整性检查
          console.group('🔍 数据完整性检查')
          const hasTitle = !!routeData.title && routeData.title !== 'null'
          const hasDescription = !!routeData.description
          const hasItinerary = routeData.itinerary && routeData.itinerary.length > 0
          const hasLocations = routeData.itinerary?.some((item: any) => item.locations && item.locations.length > 0)

          console.log('  - 标题:', hasTitle ? '✅' : '❌', routeData.title || '缺失')
          console.log('  - 描述:', hasDescription ? '✅' : '❌', hasDescription ? '存在' : '缺失')
          console.log('  - 行程安排:', hasItinerary ? '✅' : '❌', hasItinerary ? `${routeData.itinerary.length}天` : '缺失')
          console.log('  - 景点信息:', hasLocations ? '✅' : '❌', hasLocations ? '存在' : '缺失')

          if (!hasTitle || !hasDescription || !hasItinerary || !hasLocations) {
            console.warn('  ⚠️ 警告: 路线数据不完整，可能DeepSeek API未正确返回数据')
          } else {
            console.log('  ✅ 路线数据完整')
          }
          console.groupEnd()
        } else {
          console.error('  ❌ 响应数据为空')
        }
      } else {
        console.warn('  ⚠️ 响应格式不符合预期')
      }

      console.log('🔧 完整响应对象:', response)
      console.groupEnd()
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

    // 如果是生成路线的错误响应，输出详细错误信息到浏览器控制台
    if (error.config?.url?.includes('/routes/generate')) {
      console.group('❌ [智能旅游线路规划] 路线生成请求失败')
      console.error('📍 错误时间:', new Date().toLocaleString('zh-CN'))
      console.error('📍 错误状态码:', error.response?.status || '无响应')
      console.error('📍 错误信息:', error.message)

      if (error.response) {
        console.error('📦 错误响应数据:', error.response.data)
        console.error('🔧 完整错误响应:', error.response)

        // 尝试解析错误信息
        const errorData = error.response.data
        if (errorData && typeof errorData === 'object') {
          if (errorData.message) {
            console.error('💬 错误消息:', errorData.message)
          }
          if (errorData.debugInfo) {
            console.group('🔍 DeepSeek API 调用调试信息')
            console.error('  - API调用状态:', errorData.debugInfo.apiCallStatus || '未知')
            console.error('  - 是否成功调用:', errorData.debugInfo.apiCallSuccess || false)
            if (errorData.debugInfo.apiError) {
              console.error('  - API错误信息:', errorData.debugInfo.apiError)
            }
            console.groupEnd()
          }
        }
      } else if (error.request) {
        console.error('📡 请求已发送但未收到响应')
        console.error('🔧 请求对象:', error.request)
      } else {
        console.error('⚠️ 请求配置错误')
        console.error('🔧 错误配置:', error.config)
      }
      console.groupEnd()
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
