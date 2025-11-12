import request from '@/utils/axios'
import type { UserInfo } from '@/types/user'

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

export interface RegisterData {
  username: string
  email: string
  password: string
  phone?: string
}

// 登录
export const login = (username: string, password: string): Promise<LoginResponse> => {
  return request.post<LoginResponse>('/auth/login', { username, password }).then((res: any) => {
    // 处理响应数据格式
    if (res.data) {
      return res.data
    }
    return res
  })
}

// 注册
export const register = (data: RegisterData): Promise<LoginResponse> => {
  return request.post<LoginResponse>('/auth/register', data).then((res: any) => {
    if (res.data) {
      return res.data
    }
    return res
  })
}

// 获取用户信息
export const getUserInfo = (): Promise<UserInfo> => {
  return request.get<UserInfo>('/user/info').then((res: any) => {
    if (res.data) {
      return res.data
    }
    return res
  })
}

// 更新用户信息
export const updateUserInfo = (data: Partial<UserInfo>): Promise<UserInfo> => {
  return request.put<UserInfo>('/user/info', data).then((res: any) => {
    if (res.data) {
      return res.data
    }
    return res
  })
}

// 修改密码
export const changePassword = (oldPassword: string, newPassword: string): Promise<void> => {
  return request.post('/user/change-password', { oldPassword, newPassword }).then(() => {
    return Promise.resolve()
  })
}
