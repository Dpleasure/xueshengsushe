import api from './axiosConfig'

/**
 * 管理员 API 服务
 */
export const adminApi = {
  // 获取所有管理员
  getAllAdmins: async () => {
    const response = await api.get('/admins')
    return response.data
  },

  // 根据ID获取管理员
  getAdminById: async (id) => {
    const response = await api.get(`/admins/${id}`)
    return response.data
  },

  // 搜索管理员
  searchAdmins: async (keyword) => {
    const response = await api.get('/admins/search', {
      params: { keyword }
    })
    return response.data
  },

  // 添加管理员
  addAdmin: async (admin) => {
    const response = await api.post('/admins', admin)
    return response.data
  },

  // 更新管理员
  updateAdmin: async (id, admin) => {
    const response = await api.put(`/admins/${id}`, admin)
    return response.data
  },

  // 删除管理员
  deleteAdmin: async (id) => {
    const response = await api.delete(`/admins/${id}`)
    return response.data
  },

  // 注册新用户（管理员或学生）
  registerUser: async (userData) => {
    const response = await api.post('/api/auth/register', userData)
    return response.data
  }
}

