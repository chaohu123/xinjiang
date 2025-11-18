import request from '@/utils/axios'
import type { HeritageItem } from '@/types/heritage'

export interface HeritageQuery {
  keyword?: string
  region?: string
  category?: string
  heritageLevel?: string
  tags?: string[]
  page?: number
  size?: number
}

export const getHeritageList = (params?: HeritageQuery) => {
  return request.get<{ list: HeritageItem[]; total: number }, { list: HeritageItem[]; total: number }>(
    '/heritage',
    { params },
  )
}

export const getHeritageDetail = (id: number) => {
  return request.get<HeritageItem>(`/heritage/${id}`)
}

export const getHeritageRecommendations = (limit = 6) => {
  return request.get<HeritageItem[]>('/heritage/recommend', { params: { limit } })
}


