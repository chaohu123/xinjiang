import request from '@/utils/axios'
import type { AiExplainPayload, AiExplainResult } from '@/types/ai'

export const explainCulture = (payload: AiExplainPayload) => {
  return request.post<AiExplainResult>('/ai/explain', payload)
}















