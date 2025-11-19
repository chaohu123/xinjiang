import request from '@/utils/axios'
import type { MapPoiResponse, MapPoiQuery } from '@/types/map'

export const getMapPois = (params?: MapPoiQuery) => {
  return request.get<MapPoiResponse>('/map/pois', { params })
}








