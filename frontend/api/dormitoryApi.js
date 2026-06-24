import api from './axiosConfig'

/**
 * 宿舍 API 服务
 */
export const dormitoryApi = {
  // 获取所有宿舍
  getAllDormitories: async () => {
    const response = await api.get('/dormitories')
    return response.data
  },

  // 根据ID获取宿舍
  getDormitoryById: async (id) => {
    const response = await api.get(`/dormitories/${id}`)
    return response.data
  },

  // 搜索宿舍
  searchDormitories: async (keyword) => {
    const response = await api.get('/dormitories/search', {
      params: { keyword }
    })
    return response.data
  },

  // 添加宿舍
  addDormitory: async (dormitory) => {
    const response = await api.post('/dormitories', dormitory)
    return response.data
  },

  // 更新宿舍
  updateDormitory: async (id, dormitory) => {
    const response = await api.put(`/dormitories/${id}`, dormitory)
    return response.data
  },

  // 删除宿舍
  deleteDormitory: async (id) => {
    const response = await api.delete(`/dormitories/${id}`)
    return response.data
  },

  // 批量删除宿舍
  batchDeleteDormitories: async (ids) => {
    const response = await api.delete('/dormitories/batch', { data: ids })
    return response.data
  }
}

