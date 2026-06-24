import api from './axiosConfig';

/**
 * 来访登记 API 服务
 */
export const visitApi = {
  // 获取所有来访记录 (管理员)
  getAllVisits: async () => {
    const response = await api.get('/api/visits');
    return response.data;
  },

  // 根据学生学号获取来访记录 (学生)
  getVisitsByStudentId: async (studentId) => {
    const response = await api.get(`/api/visits/student/${studentId}`);
    return response.data;
  },

  // 提交来访申请 (学生)
  applyForVisit: async (visitData) => {
    const response = await api.post('/api/visits/apply', visitData);
    return response.data;
  },

  // 同意来访申请 (管理员)
  approveVisit: async (id) => {
    const response = await api.post(`/api/visits/${id}/approve`);
    return response.data;
  },

  // 拒绝来访申请 (管理员)
  rejectVisit: async (id) => {
    const response = await api.post(`/api/visits/${id}/reject`);
    return response.data;
  },

  // 删除来访记录 (管理员)
  deleteVisit: async (id) => {
    const response = await api.delete(`/api/visits/${id}`);
    return response.data;
  },
};
