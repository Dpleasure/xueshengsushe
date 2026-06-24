import api from './axiosConfig'

/**
 * 宿舍楼 API 服务
 */
export const buildingApi = {
  // 获取所有宿舍楼
  getAllBuildings: async () => {
    const response = await api.get('/buildings')
    return response.data
  },

  // 根据ID获取宿舍楼
  getBuildingById: async (id) => {
    const response = await api.get(`/buildings/${id}`)
    return response.data
  },

  // 搜索宿舍楼
  searchBuildings: async (keyword) => {
    const response = await api.get('/buildings/search', {
      params: { keyword }
    })
    return response.data
  },

  // 添加宿舍楼
  addBuilding: async (building) => {
    const response = await api.post('/buildings', building)
    return response.data
  },

  // 更新宿舍楼
  updateBuilding: async (id, building) => {
    const response = await api.put(`/buildings/${id}`, building)
    return response.data
  },

  // 删除宿舍楼
  deleteBuilding: async (id) => {
    const response = await api.delete(`/buildings/${id}`)
    return response.data
  }
}

