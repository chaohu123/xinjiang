import type { Event } from '@/types/event'

export interface UserBadge {
  id: string | number
  label: string
  description?: string
  earnedAt?: string
  icon?: string
}

export interface UserInfo {
  id: number
  username: string
  email: string
  phone?: string
  avatar?: string
  nickname?: string
  bio?: string
  role: 'USER' | 'ADMIN' | 'user' | 'admin' // 支持大小写，后端返回大写
  enabled?: boolean
  createdAt: string
  updatedAt: string
  cover?: string
  gallery?: string[]
  badges?: UserBadge[]
  registeredEvents?: Event[]
}
