const { request, upload } = require('./request');

const login = (data) => request({
  url: '/api/auth/login',
  method: 'POST',
  data
});

const getAccommodationByStudentId = (studentId) => request({
  url: `/accommodations/search-by-id?studentId=${encodeURIComponent(studentId || '')}`
});

const getRepairsByStudentId = (studentId) => request({
  url: `/api/repairs/student/${encodeURIComponent(studentId || '')}`
});

const applyRepair = (formData, imagePath) => {
  if (imagePath) {
    return upload({
      url: '/api/repairs/apply',
      filePath: imagePath,
      formData
    });
  }

  return request({
    url: '/api/repairs/apply',
    method: 'POST',
    data: formData,
    header: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
};

const getRoomChangesByStudentName = (studentName) => request({
  url: `/room-changes/search?keyword=${encodeURIComponent(studentName || '')}`
});

const applyRoomChange = (data) => request({
  url: '/room-changes/apply-change',
  method: 'POST',
  data
});

const getVisitsByStudentId = (studentId) => request({
  url: `/api/visits/student/${encodeURIComponent(studentId || '')}`
});

const applyVisit = (data) => request({
  url: '/api/visits/apply',
  method: 'POST',
  data
});

const uploadAvatar = (filePath) => upload({
  url: '/api/user/avatar',
  filePath,
  name: 'avatar'
});

module.exports = {
  login,
  getAccommodationByStudentId,
  getRepairsByStudentId,
  applyRepair,
  getRoomChangesByStudentName,
  applyRoomChange,
  getVisitsByStudentId,
  applyVisit,
  uploadAvatar
};
