import { withApiBaseUrl } from '../api/baseUrl.js'

export function normalizeAvatar(avatar) {
  if (!avatar || avatar === '👤') return ''
  if (/^https?:\/\//i.test(avatar) || avatar.startsWith('data:')) return avatar
  return withApiBaseUrl(avatar)
}

export function avatarFallback(user, fallback = '用') {
  const name = user?.name || user?.username || fallback
  return String(name).slice(0, 1)
}

export function avatarKey(user) {
  return String(user?.id || user?.studentId || user?.username || '')
}

export function persistUserInfo(user) {
  localStorage.setItem('userInfo', JSON.stringify(user || {}))
  window.dispatchEvent(new Event('user-info-updated'))
}

export function readUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    return {}
  }
}
