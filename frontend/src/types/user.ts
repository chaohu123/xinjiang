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

export type FavoriteResourceType = 'CULTURE' | 'ROUTE' | 'POST'

export interface FavoriteItem {
  id: number
  resourceType: FavoriteResourceType
  title: string
  description?: string
  cover?: string
  region?: string
  type?: string
  favorites?: number
  startLocation?: string
  endLocation?: string
  duration?: number
  distance?: number
  theme?: string
}
