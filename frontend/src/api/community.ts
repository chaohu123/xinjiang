import request from '@/utils/axios'
import type { CommunityPost, CommunityPostDetail, MyComment } from '@/types/community'

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

// 更新帖子
export const updatePost = (
  id: number,
  data: {
    title: string
    content: string
    images?: string[]
    tags?: string[]
  }
) => {
  return request.put<CommunityPostDetail>(`/community/posts/${id}`, data)
}

// 删除帖子
export const deletePost = (id: number) => {
  return request.delete(`/community/posts/${id}`)
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

// 获取我的评论
export const getMyComments = (params?: { page?: number; size?: number }) => {
  return request.get<{ list: MyComment[]; total: number }>('/community/posts/comments/my', {
    params,
  })
}

// 更新评论
export const updateMyComment = (id: number, content: string) => {
  return request.put(`/community/posts/comments/${id}`, { content })
}

// 删除评论
export const deleteMyComment = (id: number) => {
  return request.delete(`/community/posts/comments/${id}`)
}

// 获取我的帖子
export const getMyPosts = (params?: { page?: number; size?: number }) => {
  return request.get<{ list: CommunityPost[]; total: number }, { list: CommunityPost[]; total: number }>(
    '/community/posts/my',
    {
      params,
    },
  )
}

// 获取点赞的帖子
export const getLikedPosts = (params?: { page?: number; size?: number }) => {
  return request.get<{ list: CommunityPost[]; total: number }>('/community/posts/liked', {
    params,
  })
}

// 获取评论的帖子
export const getCommentedPosts = (params?: { page?: number; size?: number }) => {
  return request.get<{ list: CommunityPost[]; total: number }>('/community/posts/commented', {
    params,
  })
}

// 获取收藏的帖子
export const getFavoritePosts = (params?: { page?: number; size?: number }) => {
  return request.get<{ list: CommunityPost[]; total: number }>('/community/posts/favorites', {
    params,
  })
}

// 收藏帖子
export const favoritePost = (id: number) => {
  return request.post(`/community/posts/${id}/favorite`)
}

// 取消收藏帖子
export const unfavoritePost = (id: number) => {
  return request.delete(`/community/posts/${id}/favorite`)
}

// 上传图片
export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string; type: string }>('/admin/events/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}




