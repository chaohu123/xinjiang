import request from '@/utils/axios'
import type { CarouselItem, CarouselCreateRequest, CarouselUpdateRequest } from '@/types/carousel'

// 获取所有轮播图（公开）
export const getCarousels = () => {
  return request.get<CarouselItem[]>('/carousel')
}

// 获取所有轮播图（管理员）
export const getAllCarousels = () => {
  return request.get<CarouselItem[]>('/carousel/all')
}

// 创建轮播图
export const createCarousel = (data: CarouselCreateRequest) => {
  return request.post<CarouselItem>('/carousel', data)
}

// 更新轮播图
export const updateCarousel = (id: number, data: CarouselUpdateRequest) => {
  return request.put<CarouselItem>(`/carousel/${id}`, data)
}

// 删除轮播图
export const deleteCarousel = (id: number) => {
  return request.delete(`/carousel/${id}`)
}

// 上传轮播图图片
export const uploadCarouselImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string }>('/carousel/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
