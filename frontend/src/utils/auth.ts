import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import type { Router } from 'vue-router'

/**
 * 检查用户是否已登录，如果未登录则显示提示并跳转到登录页面
 * @param message 提示消息，默认为"请先登录"
 * @param router 可选的路由实例，如果不提供则使用window.location跳转
 * @returns 返回是否已登录
 */
export const requireAuth = (message: string = '请先登录', router?: Router): boolean => {
  const userStore = useUserStore()

  if (!userStore.isLoggedIn) {
    ElMessage.warning(message)
    if (router) {
      router.push('/login')
    } else {
      window.location.href = '/login'
    }
    return false
  }

  return true
}


