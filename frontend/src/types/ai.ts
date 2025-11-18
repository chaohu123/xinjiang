export interface AiExplainPayload {
  query: string
  imageUrl?: string
  context?: string
  audience?: string
  tone?: string
  length?: 'short' | 'medium' | 'detailed'
  focusPoints?: string[]
}

export interface AiExplainResult {
  title: string
  summary: string
  highlights: string[]
  references: string[]
  mediaInsight: string
}





