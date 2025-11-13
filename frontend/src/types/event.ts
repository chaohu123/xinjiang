export interface Event {
  id: number
  title: string
  description: string
  cover: string
  type: 'exhibition' | 'performance' | 'workshop' | 'tour'
  startDate: string
  endDate: string
  location: {
    name: string
    address: string
    lat?: number
    lng?: number
  }
  capacity?: number
  registered: number
  price?: number
  status: 'upcoming' | 'ongoing' | 'past'
  createdAt: string
  images?: string[]
  videos?: string[]
}

export interface EventDetail extends Event {
  content: string
  organizer: {
    name: string
    contact: string
  }
  schedule?: {
    time: string
    activity: string
  }[]
  requirements?: string[]
}
