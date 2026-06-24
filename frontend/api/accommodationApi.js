import api from './axiosConfig'

/**
 * 住宿信息 API 服务
 */
export const accommodationApi = {
  // 获取所有住宿信息
  getAllAccommodations: async () => {
    const response = await api.get('/accommodations')
    return response.data
  },

  // 根据ID获取住宿信息
  getAccommodationById: async (id) => {
    const response = await api.get(`/accommodations/${id}`)
    return response.data
  },

  // 搜索住宿信息（按姓名）
  searchAccommodations: async (studentName, dormitory) => {
    const response = await api.get('/accommodations/search', {
      params: { studentName, dormitory }
    })
    return response.data
  },

  // 根据学号搜索住宿信息
  searchAccommodationsByStudentId: async (studentId, dormitory) => {
    const response = await api.get('/accommodations/search-by-id', {
      params: { studentId, dormitory }
    })
    return response.data
  },

  // 添加住宿信息
  addAccommodation: async (accommodation) => {
    const response = await api.post('/accommodations', accommodation)
    return response.data
  },

  // 更新住宿信息
  updateAccommodation: async (id, accommodation) => {
    const response = await api.put(`/accommodations/${id}`, accommodation)
    return response.data
  },

  // 删除住宿信息
  deleteAccommodation: async (id) => {
    const response = await api.delete(`/accommodations/${id}`)
    return response.data
  },

  // 获取某个学生的所有换寝记录
  getChangeRecordsByStudent: async (studentName) => {
    const response = await api.get('/room-changes/search', {
      params: { keyword: studentName }
    });
    return response.data;
  },

  // 学生提交换寝申请
  applyForChange: async (applicationData) => {
    const response = await api.post('/room-changes/apply-change', applicationData);
    return response.data;
  }
}
