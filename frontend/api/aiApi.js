import api from './axiosConfig'

export const aiApi = {
  chat: async (message) => {
    const response = await api.post('/api/ai/chat', { message }, {
      timeout: 45000,
      silent: true
    })
    return response.data
  }
}
