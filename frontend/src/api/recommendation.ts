import request from '@/utils/axios'
import type { HomeResource } from '@/types/culture'

export const getRecommendationsFeed = (limit = 6) => {
  return request.get<HomeResource[]>('/recommendations', { params: { limit } })
}















<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
