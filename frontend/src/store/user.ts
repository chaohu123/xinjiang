import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/user'
import { login, register, getUserInfo, updateUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  // 初始化用户信息
  const initUser = async () => {
    if (token.value) {
      try {
        const info = await getUserInfo()
        userInfo.value = info
      } catch (error) {
        // Token失效，清除
        token.value = null
        localStorage.removeItem('token')
      }
    }
  }

  // 登录
  const loginUser = async (username: string, password: string) => {
    const response = await login(username, password)
    token.value = response.token
    userInfo.value = response.userInfo
    localStorage.setItem('token', response.token)
    return response
  }

  // 注册
  const registerUser = async (data: {
    username: string
    email: string
    password: string
    phone?: string
  }) => {
    const response = await register(data)
    token.value = response.token
    userInfo.value = response.userInfo
    localStorage.setItem('token', response.token)
    return response
  }

  // 更新用户信息
  const updateUser = async (data: Partial<UserInfo>) => {
    const updated = await updateUserInfo(data)
    userInfo.value = { ...userInfo.value, ...updated }
    return updated
  }

  // 登出
  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    initUser,
    loginUser,
    registerUser,
    updateUser,
    logout,
  }
})


