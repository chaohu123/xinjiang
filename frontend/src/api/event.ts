import request from '@/utils/axios'
import type { Event, EventDetail } from '@/types/event'

// 获取活动列表
export const getEvents = (params?: {
  month?: string
  status?: string // upcoming, ongoing, past
  type?: string
  page?: number
  size?: number
}) => {
  return request.get<{ list: Event[]; total: number }>('/events', { params })
}

// 获取活动详情
export const getEventDetail = (id: number) => {
  return request.get<EventDetail>(`/events/${id}`)
}

// 报名活动
export const registerEvent = (id: number) => {
  return request.post(`/events/${id}/register`)
}

// 取消报名
export const cancelEventRegistration = (id: number) => {
  return request.delete(`/events/${id}/register`)
}

// 获取我的已报名活动
export const getMyRegisteredEvents = (params?: {
  page?: number
  size?: number
}) => {
  return request.get<{ list: Event[]; total: number }>('/events/my-registrations', { params })
}

// 获取首页最新动态（一个月前后的活动）
export const getLatestEvents = (params?: {
  page?: number
  size?: number
}) => {
  return request.get<{ list: Event[]; total: number }>('/events/latest', { params })
}
