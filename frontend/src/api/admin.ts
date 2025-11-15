import request from '@/utils/axios'
import type { UserInfo } from '@/types/user'
import type { CultureResource, CultureType } from '@/types/culture'
import type { Event, EventDetail } from '@/types/event'
import type { CommunityPost } from '@/types/community'

// ==================== 用户管理 ====================
export interface UserListResponse {
  list: UserInfo[]
  total: number
  page: number
  size: number
}

// 获取用户列表
export const getUsers = (params?: { page?: number; size?: number; keyword?: string }) => {
  return request.get<UserListResponse>('/admin/users', { params })
}

// 更新用户信息（管理员）
export const updateUser = (id: number, data: Partial<UserInfo>) => {
  return request.put<UserInfo>(`/admin/users/${id}`, data)
}

// 删除用户
export const deleteUser = (id: number) => {
  return request.delete(`/admin/users/${id}`)
}

// 启用/禁用用户
export const toggleUserStatus = (id: number, enabled: boolean) => {
  return request.put(`/admin/users/${id}/status`, { enabled })
}

// ==================== 文化资源管理 ====================
export interface CultureListResponse {
  list: CultureResource[]
  total: number
  page: number
  size: number
}

// 获取文化资源列表（管理员）
export const getAdminCultureResources = (params?: {
  page?: number
  size?: number
  keyword?: string
  type?: CultureType
}) => {
  return request.get<CultureListResponse>('/admin/culture', { params })
}

// 创建文化资源
export const createCultureResource = (data: any) => {
  return request.post<CultureResource>('/admin/culture', data)
}

// 更新文化资源
export const updateCultureResource = (id: number, data: any) => {
  return request.put<CultureResource>(`/admin/culture/${id}`, data)
}

// 删除文化资源
export const deleteCultureResource = (id: number) => {
  return request.delete(`/admin/culture/${id}`)
}

// 上传文化资源图片或视频
export const uploadCultureMedia = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string; type: string }>('/admin/culture/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// ==================== 活动管理 ====================
export interface EventListResponse {
  list: Event[]
  total: number
  page: number
  size: number
}

// 获取活动列表（管理员）
export const getAdminEvents = (params?: {
  page?: number
  size?: number
  keyword?: string
  status?: string
}) => {
  return request.get<EventListResponse>('/admin/events', { params })
}

// 创建活动
export const createEvent = (data: any) => {
  return request.post<EventDetail>('/admin/events', data)
}

// 更新活动
export const updateEvent = (id: number, data: any) => {
  return request.put<EventDetail>(`/admin/events/${id}`, data)
}

// 删除活动
export const deleteEvent = (id: number) => {
  return request.delete(`/admin/events/${id}`)
}

// 获取活动报名列表
export const getEventRegistrations = (eventId: number) => {
  return request.get(`/admin/events/${eventId}/registrations`)
}

// 审核活动报名
export const approveEventRegistration = (eventId: number, registrationId: number) => {
  return request.put(`/admin/events/${eventId}/registrations/${registrationId}/approve`)
}

export const rejectEventRegistration = (
  eventId: number,
  registrationId: number,
  reason?: string
) => {
  return request.put(`/admin/events/${eventId}/registrations/${registrationId}/reject`, {
    reason,
  })
}

// ==================== 社区投稿管理 ====================
export interface PostListResponse {
  list: CommunityPost[]
  total: number
  page: number
  size: number
}

// 获取社区投稿列表（管理员）
export const getAdminPosts = (params?: {
  page?: number
  size?: number
  keyword?: string
  status?: string
}) => {
  return request.get<PostListResponse>('/admin/posts', { params })
}

// 审核社区投稿
export const approvePost = (id: number) => {
  return request.put(`/admin/posts/${id}/approve`)
}

// 拒绝社区投稿
export const rejectPost = (id: number, reason?: string) => {
  return request.put(`/admin/posts/${id}/reject`, { reason })
}

// 删除社区投稿
export const deletePost = (id: number) => {
  return request.delete(`/admin/posts/${id}`)
}

// ==================== 仪表板数据 ====================

// 获取仪表板统计数据
export interface DashboardStats {
  users: number
  culture: number
  events: number
  posts: number
}

export const getDashboardStats = () => {
  return request.get<DashboardStats>('/admin/dashboard/stats')
}

// 获取待审核的社区投稿
export const getPendingPosts = (limit?: number) => {
  return request.get<CommunityPost[]>('/admin/dashboard/pending-posts', {
    params: { limit },
  })
}

// 获取正在进行的活动
export const getOngoingEvents = (limit?: number) => {
  return request.get<Event[]>('/admin/dashboard/ongoing-events', {
    params: { limit },
  })
}
