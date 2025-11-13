export type CultureType = 'article' | 'exhibit' | 'video' | 'audio'

export interface CultureResource {
  id: number
  type: CultureType
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
}



