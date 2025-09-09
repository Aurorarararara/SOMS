import request from '@/utils/request'

// AI 聊天接口
// payload: { message: string, model?: string, temperature?: number }
export const chatAi = (payload) => {
  return request.post('/ai/chat', payload)
}

export default {
  chatAi
}