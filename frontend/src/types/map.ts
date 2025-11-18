export interface MapPoi {
  id: number
  originType: 'CULTURE' | 'HERITAGE'
  originRefId?: number
  category: 'scenic' | 'relic' | 'museum' | 'heritage'
  title: string
  lat: number
  lng: number
  region?: string
  cover?: string
  tags?: string[]
  summary?: string
  views?: number
  favorites?: number
  contentType?: string
}

export interface MapCluster {
  lat: number
  lng: number
  count: number
  categories: Record<string, number>
}

export interface MapPoiResponse {
  pois: MapPoi[]
  clusters?: MapCluster[]
  stats?: Record<string, number>
  total?: number
  filtered?: number
  clustered?: boolean
}

export interface MapPoiQuery {
  keyword?: string
  category?: string
  north?: number
  south?: number
  east?: number
  west?: number
  cluster?: boolean
  zoom?: number
  limit?: number
}


