export type CultureType = 'article' | 'exhibit' | 'video' | 'audio' | 'heritage'

export interface CultureResource {
  id: number
  type: CultureType
  resourceType?: string
  title: string
  description: string
  cover: string
  images?: string[]
  videoUrl?: string
  audioUrl?: string
  content?: string
  tags: string[]
  region: string
  location?: {
    lat: number
    lng: number
    address: string
  }
  views: number
  favorites: number
  createdAt: string
  updatedAt: string
  heritageLevel?: string
  category?: string
}

// 首页资源响应（可能来自文化资源或社区投稿）
export interface HomeResource {
  id: number
  title: string
  description: string
  cover: string
  images?: string[]
  videoUrl?: string
  audioUrl?: string
  content?: string
  tags: string[]
  region?: string
  views: number
  favorites?: number
  likes?: number
  comments?: number
  createdAt: string
  updatedAt: string
  source: 'CULTURE_RESOURCE' | 'COMMUNITY_POST' | 'HERITAGE_ITEM'
  resourceType?: string
}










