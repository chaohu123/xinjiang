import request from '@/utils/axios'
import type { HomeResource } from '@/types/culture'

export const getRecommendationsFeed = (limit = 6) => {
  return request.get<HomeResource[]>('/recommendations', { params: { limit } })
}















