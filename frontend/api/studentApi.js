import api from './axiosConfig'

/**
 * 学生 API 服务
 */
export const studentApi = {
  // 获取所有学生
  getAllStudents: async () => {
    const response = await api.get('/students')
    return response.data // 返回学生数组
  },

  // 根据ID获取学生
  getStudentById: async (id) => {
    const response = await api.get(`/students/${id}`)
    return response.data
  },

  // 搜索学生
  searchStudents: async (keyword) => {
    const response = await api.get('/students/search', {
      params: { keyword }
    })
    return response.data
  },

  // 添加学生
  addStudent: async (student) => {
    const response = await api.post('/students', student)
    return response.data
  },

  // 更新学生
  updateStudent: async (id, student) => {
    const response = await api.put(`/students/${id}`, student)
    return response.data
  },

  // 删除学生
  deleteStudent: async (id) => {
    const response = await api.delete(`/students/${id}`)
    return response.data
  },

  // 批量删除学生
  batchDeleteStudents: async (ids) => {
    const response = await api.delete('/students/batch', { data: ids })
    return response.data
  }
}

