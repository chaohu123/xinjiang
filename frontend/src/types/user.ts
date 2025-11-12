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
}
