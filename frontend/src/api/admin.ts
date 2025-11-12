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
