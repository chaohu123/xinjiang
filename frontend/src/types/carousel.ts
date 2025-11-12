export interface CarouselItem {
  id: number
  image: string
  title?: string
  subtitle?: string
  link?: string
  buttonText?: string
  order: number
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface CarouselCreateRequest {
  image: string
  title?: string
  subtitle?: string
  link?: string
  buttonText?: string
  order?: number
  enabled?: boolean
}

export interface CarouselUpdateRequest {
  image?: string
  title?: string
  subtitle?: string
  link?: string
  buttonText?: string
  order?: number
  enabled?: boolean
}
