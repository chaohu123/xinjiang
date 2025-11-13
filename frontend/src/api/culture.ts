import request from '@/utils/axios'
import type { CultureResource, CultureType } from '@/types/culture'

export interface SearchParams {
  keyword?: string
  type?: CultureType
  tags?: string[]
  region?: string
  page?: number
  size?: number
}

export interface SearchResponse {
  list: CultureResource[]
  total: number
  page: number
  size: number
}

// 搜索文化资源
export const searchResources = (params: SearchParams) => {
  return request.get<SearchResponse>('/culture/search', { params })
}

// 获取资源详情
export const getResourceDetail = (type: CultureType, id: number) => {
  return request.get<CultureResource>(`/culture/${type}/${id}`)
}

// 获取热门资源
export const getHotResources = (limit = 10) => {
  return request.get<CultureResource[]>('/culture/hot', { params: { limit } })
}

// 获取推荐资源
export const getRecommendedResources = (limit = 10) => {
  return request.get<CultureResource[]>('/culture/recommended', {
    params: { limit },
  })
}

// 收藏资源
export const favoriteResource = (type: CultureType, id: number) => {
  return request.post(`/culture/${type}/${id}/favorite`)
}

// 取消收藏
export const unfavoriteResource = (type: CultureType, id: number) => {
  return request.delete(`/culture/${type}/${id}/favorite`)
}

// 获取收藏列表
export const getFavorites = (params: { page?: number; size?: number }) => {
  return request.get<SearchResponse>('/user/favorites', { params })
}

