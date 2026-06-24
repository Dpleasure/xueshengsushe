const api = require('../../utils/api');
const { localFallback, pageBackground } = require('../../utils/backgrounds');

const displayValue = (value) => (value === undefined || value === null || value === '' ? '-' : value);
const displayBed = (value) => {
  const text = displayValue(value);
  if (text === '-' || text.includes('床')) return text;
  return `${text}号床`;
};
const avatarInitial = (value) => {
  const text = value || '同学';
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
    displayName: '同学',
    displayStudentId: '-',
    avatarUrl: '',
    avatarText: '同'
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  onShow() {
    if (!getApp().requireLogin()) return;
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({
      userInfo,
      displayName: userInfo.name || userInfo.username || '同学',
      displayStudentId: displayValue(userInfo.studentId),
      avatarUrl: this.normalizeAvatar(userInfo.avatar),
      avatarText: avatarInitial(userInfo.name || userInfo.username || '同学')
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
  },

  normalizeAvatar(avatar) {
    if (!avatar || avatar === '👤') return '';
    if (/^https?:\/\//i.test(avatar) || avatar.startsWith('data:')) return avatar;
    return `${getApp().globalData.baseUrl}${avatar}`;
  }
});
