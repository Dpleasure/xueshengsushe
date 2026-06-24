import api from './axiosConfig'

/**
 * 换寝信息 API 服务
 */
export const roomChangeApi = {
  // 获取所有换寝记录
  getAllRoomChanges: async () => {
    const response = await api.get('/room-changes')
    return response.data
  },

  // 根据ID获取换寝记录
  getRoomChangeById: async (id) => {
    const response = await api.get(`/room-changes/${id}`)
    return response.data
  },

  // 搜索换寝记录
  searchRoomChanges: async (keyword) => {
    const response = await api.get('/room-changes/search', {
      params: { keyword }
    })
    return response.data
  },

  // 添加换寝记录
  addRoomChange: async (roomChange) => {
    const response = await api.post('/room-changes', roomChange)
    return response.data
  },

  // 更新换寝记录
  updateRoomChange: async (id, roomChange) => {
    const response = await api.put(`/room-changes/${id}`, roomChange)
    return response.data
  },

  // 删除换寝记录
  deleteRoomChange: async (id) => {
    const response = await api.delete(`/room-changes/${id}`)
    return response.data
  },

  // 同意换寝申请
  approveChange: async (id) => {
    const response = await api.post(`/room-changes/${id}/approve`);
    return response.data;
  },

  // 拒绝换寝申请
  rejectChange: async (id) => {
    const response = await api.post(`/room-changes/${id}/reject`);
    return response.data;
  }
}

