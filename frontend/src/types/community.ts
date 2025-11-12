export interface CommunityPost {
  id: number
  title: string
  content: string
  images?: string[]
  author: {
    id: number
    username: string
    avatar?: string
  }
  tags: string[]
  likes: number
  comments: number
  views: number
  isLiked?: boolean
  status?: 'pending' | 'approved' | 'rejected'
  createdAt: string
  updatedAt: string
}

export interface CommunityPostDetail extends CommunityPost {
  comments: Comment[]
}

export interface Comment {
  id: number
  content: string
  author: {
    id: number
    username: string
    avatar?: string
  }
  createdAt: string
  replies?: Comment[]
}
