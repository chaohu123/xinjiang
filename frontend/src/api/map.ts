import request from '@/utils/axios'
import type { MapPoiResponse, MapPoiQuery } from '@/types/map'

export const getMapPois = (params?: MapPoiQuery) => {
  return request.get<MapPoiResponse>('/map/pois', { params })
}












<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
