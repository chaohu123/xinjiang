import { useUserStore } from '@/store/user'

// 检查用户是否已登录
export const isAuthenticated = (): boolean => {
  const userStore = useUserStore()
  return userStore.isLoggedIn
}

// 检查用户角色
export const hasRole = (role: string | string[]): boolean => {
  const userStore = useUserStore()
  if (!userStore.userInfo) return false

  const userRole = userStore.userInfo.role
  if (Array.isArray(role)) {
    return role.includes(userRole)
  }
  return userRole === role
}

// 检查是否为管理员
export const isAdmin = (): boolean => {
  return hasRole('admin')
}




































