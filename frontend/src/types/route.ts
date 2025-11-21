export interface Route {
  id: number
  title: string
  description: string
  cover: string
  theme: string
  duration: number // 天数
  distance: number // 公里
  estimatedBudget?: number
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
  favorited?: boolean
  timeline?: RouteTimelineItem[]
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
    priority?: string
    alternative?: string
    amapLink?: string
    googleMapsLink?: string // 保留以兼容旧数据
  }[]
  accommodation?: string
  meals?: string[]
  transportation?: string
  timeSchedule?: string
  dailyBudget?: string
}

export interface RouteResource {
  id: number
  type: string
  title: string
  cover: string
  order: number
}

export interface RouteTimelineItem {
  day: number
  label: string
  startTime: string
  endTime: string
  stopCount: number
  estimatedDistance: number
}

export interface RouteAnalyticsSummary {
  totalRoutes: number
  avgDuration: number
  avgDistance: number
  geoCoverageRate: number
  budgetCoverageRate: number
  generatedAt: string
}

export interface RouteThemeStat {
  theme: string
  label: string
  count: number
  percent: number
}

export interface RoutePreferenceStat {
  label: string
  count: number
  percent: number
}

export interface RouteHeatmapPoint {
  routeId: number
  routeTitle: string
  lat: number | null
  lng: number | null
  intensity: number
  views: number | null
  favorites: number | null
  theme: string | null
  duration: number | null
  startLocation: string | null
  endLocation: string | null
}

export interface RouteAnalytics {
  summary: RouteAnalyticsSummary
  themeStats: RouteThemeStat[]
  durationPreferences: RoutePreferenceStat[]
  budgetPreferences: RoutePreferenceStat[]
  heatmapPoints: RouteHeatmapPoint[]
}





















