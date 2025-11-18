export interface Event {
  id: number
  title: string
  description: string
  cover: string
  type: 'exhibition' | 'performance' | 'workshop' | 'tour'
  startDate: string
  endDate: string
  location?: {
    name: string
    address: string
    lat?: number
    lng?: number
  } | null
  capacity?: number
  registered: number
  price?: number
  status: 'upcoming' | 'ongoing' | 'past'
  createdAt: string
  images?: string[]
  videos?: string[]
  isRegistered?: boolean // 当前用户是否已报名
  registrationStatus?: 'PENDING' | 'APPROVED' | 'REJECTED' // 报名状态
}

export interface EventDetail extends Event {
  content: string
  organizer?: {
    name: string
    contact: string
  } | null
  schedule?: {
    time: string
    activity: string
  }[]
  requirements?: string[]
}

export interface EventCalendarResponse {
  month: string
  days: {
    day: number
    events: Event[]
  }[]
}
