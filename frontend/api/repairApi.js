import api from './axiosConfig'

/**
 * 报修信息 API 服务
 */
export const repairApi = {
  // 获取所有报修记录
  getAllRepairs: async () => {
    const response = await api.get('/api/repairs')
    return response.data
  },

  // 根据学号获取报修记录
  getRepairsByStudentId: async (studentId) => {
    const response = await api.get(`/api/repairs/student/${studentId}`)
    return response.data
  },

  // 申请报修
  applyForRepair: async (formData) => {
    const response = await api.post('/api/repairs/apply', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  // 更新报修状态
  updateRepairStatus: async (id, status) => {
    const response = await api.put(`/api/repairs/${id}/status`, null, { params: { status } })
    return response.data
  },

  // 删除报修记录
  deleteRepair: async (id) => {
    const response = await api.delete(`/api/repairs/${id}`)
    return response.data
  },
}
