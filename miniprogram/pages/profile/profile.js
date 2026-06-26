const api = require('../../utils/api');
const { loadAvatarUrl } = require('../../utils/avatar');
const { localFallback, pageBackground } = require('../../utils/backgrounds');
const { setTabBarSelected } = require('../../utils/tabbar');

const displayValue = (value) => (value === undefined || value === null || value === '' ? '-' : value);
const displayBed = (value) => {
  const text = displayValue(value);
  if (text === '-' || text.includes('\u5e8a')) return text;
  return `${text}\u53f7\u5e8a`;
};

Page({
  data: {
    bgUrl: pageBackground('profile'),
    localFallback,
    useLocalBg: false,
    userInfo: {},
    accommodation: null,
    accommodationDisplay: null,
    avatarText: '\u5b66',
    avatarUrl: '',
    uploading: false,
    displayName: '\u5b66\u751f',
    displayStudentId: '-',
    displayUsername: '-'
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  onShow() {
    if (!getApp().requireLogin()) return;
    setTabBarSelected(this, 4);
    const userInfo = wx.getStorageSync('userInfo') || {};
    const name = userInfo.name || userInfo.username || '\u5b66\u751f';
    this.setData({
      userInfo,
      avatarText: String(name).slice(0, 1),
      displayName: name,
      displayStudentId: displayValue(userInfo.studentId),
      displayUsername: displayValue(userInfo.username)
    });
    this.refreshAvatar(userInfo.avatar);
    this.fetchAccommodation(userInfo.studentId);
  },

  async refreshAvatar(avatar) {
    const avatarUrl = await loadAvatarUrl(avatar);
    this.setData({ avatarUrl });
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
          wx.showToast({ title: '\u56fe\u7247\u4e0d\u80fd\u8d85\u8fc7 2MB', icon: 'none' });
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
      this.setData({ userInfo: nextUserInfo });
      await this.refreshAvatar(nextUserInfo.avatar);
      wx.showToast({ title: '\u5934\u50cf\u5df2\u66f4\u65b0', icon: 'success' });
    } finally {
      this.setData({ uploading: false });
    }
  },

  logout() {
    wx.showModal({
      title: '\u9000\u51fa\u767b\u5f55',
      content: '\u786e\u5b9a\u8981\u9000\u51fa\u5f53\u524d\u8d26\u53f7\u5417\uff1f',
      success: (res) => {
        if (res.confirm) getApp().logout();
      }
    });
  }
});
