import api from './axiosConfig.js'

export const getCurrentUser = () => {
  return api.get('/api/user/me', { silent: true })
}

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('avatar', file)
  return api.post('/api/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
