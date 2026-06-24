const api = require('../../utils/api');
const { localFallback, pageBackground } = require('../../utils/backgrounds');

const displayValue = (value) => (value === undefined || value === null || value === '' ? '-' : value);
const displayBed = (value) => {
  const text = displayValue(value);
  if (text === '-' || text.includes('床')) return text;
  return `${text}号床`;
};

Page({
  data: {
    bgUrl: pageBackground('profile'),
    localFallback,
    useLocalBg: false,
    userInfo: {},
    accommodation: null,
    accommodationDisplay: null,
    avatarText: '学',
    avatarUrl: '',
    uploading: false,
    displayName: '学生',
    displayStudentId: '-',
    displayUsername: '-'
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  onShow() {
    if (!getApp().requireLogin()) return;
    const userInfo = wx.getStorageSync('userInfo') || {};
    const name = userInfo.name || userInfo.username || '学生';
    this.setData({
      userInfo,
      avatarText: String(name).slice(0, 1),
      avatarUrl: this.normalizeAvatar(userInfo.avatar),
      displayName: name,
      displayStudentId: displayValue(userInfo.studentId),
      displayUsername: displayValue(userInfo.username)
    });
    this.fetchAccommodation(userInfo.studentId);
  },

  async fetchAccommodation(studentId) {
    if (!studentId) return;
    try {
      const list = await api.getAccommodationByStudentId(studentId);
      const accommodation = Array.isArray(list) && list.length ? list[0] : null;
      this.setData({
        accommodation,
        accommodationDisplay: accommodation ? {
          studentName: displayValue(accommodation.studentName),
          building: displayValue(accommodation.building),
          dormitory: displayValue(accommodation.dormitory),
          bed: displayBed(accommodation.bed)
        } : null
      });
    } catch (error) {
      this.setData({ accommodation: null, accommodationDisplay: null });
    }
  },

  chooseAvatar() {
    if (this.data.uploading) return;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['compressed'],
      success: async (res) => {
        const file = res.tempFiles && res.tempFiles[0];
        if (!file || !file.tempFilePath) return;
        if (file.size && file.size > 2 * 1024 * 1024) {
          wx.showToast({ title: '图片不能超过 2MB', icon: 'none' });
          return;
        }
        await this.uploadAvatar(file.tempFilePath);
      }
    });
  },

  async uploadAvatar(filePath) {
    this.setData({ uploading: true });
    try {
      const data = await api.uploadAvatar(filePath);
      const nextUserInfo = {
        ...this.data.userInfo,
        ...data,
        avatar: data.avatar
      };
      wx.setStorageSync('userInfo', nextUserInfo);
      getApp().globalData.userInfo = nextUserInfo;
      this.setData({
        userInfo: nextUserInfo,
        avatarUrl: this.normalizeAvatar(nextUserInfo.avatar)
      });
      wx.showToast({ title: '头像已更新', icon: 'success' });
    } finally {
      this.setData({ uploading: false });
    }
  },

  normalizeAvatar(avatar) {
    if (!avatar || avatar === '👤') return '';
    if (/^https?:\/\//i.test(avatar) || avatar.startsWith('data:')) return avatar;
    return `${getApp().globalData.baseUrl}${avatar}`;
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      success: (res) => {
        if (res.confirm) getApp().logout();
      }
    });
  }
});
