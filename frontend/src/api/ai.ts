import request from '@/utils/axios'
import type { AiExplainPayload, AiExplainResult } from '@/types/ai'

export const explainCulture = (payload: AiExplainPayload) => {
  return request.post<AiExplainResult>('/ai/explain', payload)
}















<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
