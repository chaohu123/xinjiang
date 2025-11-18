import request from '@/utils/axios'
import type { Route, RouteDetail } from '@/types/route'

// 获取路线列表
export const getRoutes = (params?: { theme?: string; page?: number; size?: number }) => {
  return request.get<{ list: Route[]; total: number }>('/routes', { params })
}

// 获取路线详情
export const getRouteDetail = (id: number) => {
  return request.get<RouteDetail>(`/routes/${id}`)
}

// 生成自定义路线
export const generateRoute = (data: {
  startLocation: string
  endLocation: string
  interests: string[]
  peopleCount: number
  budget: number
  mustVisitLocations: string[]
  duration: number
}) => {
  // AI 生成路线可能需要较长时间，设置 180 秒超时
  return request.post<RouteDetail>('/routes/generate', data, {
    timeout: 180000 // 180 秒
  })
}

// 获取我的路线
export const getMyRoutes = (params?: { page?: number; size?: number }) => {
  return request.get<{ list: Route[]; total: number }>('/routes/my', { params })
}

// 删除我的路线
export const deleteMyRoute = (id: number) => {
  return request.delete(`/routes/${id}`)
}













