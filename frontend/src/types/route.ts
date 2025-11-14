export interface Route {
  id: number
  title: string
  description: string
  cover: string
  theme: string
  duration: number // 天数
  distance: number // 公里
  startLocation: string
  endLocation: string
  waypoints: number
  views: number
  favorites: number
  createdAt: string
}

export interface RouteDetail extends Route {
  itinerary: ItineraryItem[]
  resources: RouteResource[]
  tips?: string[]
}

export interface ItineraryItem {
  day: number
  title: string
  description: string
  locations: {
    name: string
    lat: number
    lng: number
    description?: string
  }[]
  accommodation?: string
  meals?: string[]
}

export interface RouteResource {
  id: number
  type: string
  title: string
  cover: string
  order: number
}






