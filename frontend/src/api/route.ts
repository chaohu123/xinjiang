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
  duration: number
}) => {
  return request.post<RouteDetail>('/routes/generate', data)
}










