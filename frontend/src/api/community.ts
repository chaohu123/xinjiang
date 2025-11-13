import request from '@/utils/axios'
import type { CommunityPost, CommunityPostDetail } from '@/types/community'

// 获取社区帖子列表
export const getCommunityPosts = (params?: {
  page?: number
  size?: number
  sort?: 'latest' | 'hot'
}) => {
  return request.get<{ list: CommunityPost[]; total: number }>('/community/posts', {
    params,
  })
}

// 获取帖子详情
export const getPostDetail = (id: number) => {
  return request.get<CommunityPostDetail>(`/community/posts/${id}`)
}

// 创建帖子
export const createPost = (data: {
  title: string
  content: string
  images?: string[]
  tags?: string[]
}) => {
  return request.post<CommunityPostDetail>('/community/posts', data)
}

// 点赞帖子
export const likePost = (id: number) => {
  return request.post(`/community/posts/${id}/like`)
}

// 取消点赞
export const unlikePost = (id: number) => {
  return request.delete(`/community/posts/${id}/like`)
}

// 评论帖子
export const commentPost = (id: number, content: string) => {
  return request.post(`/community/posts/${id}/comments`, { content })
}


