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
const avatarInitial = (value) => {
  const text = value || '\u540c\u5b66';
  return text.substring(0, 1);
};

Page({
  data: {
    bgUrl: pageBackground('home'),
    localFallback,
    useLocalBg: false,
    userInfo: {},
    accommodation: null,
    accommodationDisplay: null,
    displayName: '\u540c\u5b66',
    displayStudentId: '-',
    avatarUrl: '',
    avatarText: '\u540c'
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  onShow() {
    if (!getApp().requireLogin()) return;
    setTabBarSelected(this, 0);
    const userInfo = wx.getStorageSync('userInfo') || {};
    const displayName = userInfo.name || userInfo.username || '\u540c\u5b66';
    this.setData({
      userInfo,
      displayName,
      displayStudentId: displayValue(userInfo.studentId),
      avatarText: avatarInitial(displayName)
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
          dormitory: displayValue(accommodation.dormitory),
          bed: displayBed(accommodation.bed),
          building: displayValue(accommodation.building)
        } : null
      });
    } catch (error) {
      this.setData({ accommodation: null, accommodationDisplay: null });
    }
  },

  goTab(event) {
    wx.switchTab({ url: event.currentTarget.dataset.url });
  }
});
