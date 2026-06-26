const api = require('../../utils/api');
const { formatDateTime, changeStatusText, changeStatusClass } = require('../../utils/format');
const { localFallback, pageBackground } = require('../../utils/backgrounds');
const { setTabBarSelected } = require('../../utils/tabbar');

const displayValue = (value) => (value === undefined || value === null || value === '' ? '-' : value);
const displayBed = (value) => {
  const text = displayValue(value);
  if (text === '-' || text.includes('床')) return text;
  return `${text}号床`;
};

Page({
  data: {
    bgUrl: pageBackground('change'),
    localFallback,
    useLocalBg: false,
    userInfo: {},
    accommodation: null,
    accommodationDisplay: null,
    changes: [],
    form: {
      newDormitory: '',
      newBed: ''
    },
    submitting: false
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  onShow() {
    if (!getApp().requireLogin()) return;
    setTabBarSelected(this, 2);
    this.setData({ userInfo: wx.getStorageSync('userInfo') || {} });
    this.fetchData();
  },

  onPullDownRefresh() {
    this.fetchData().finally(() => wx.stopPullDownRefresh());
  },

  async fetchData() {
    const userInfo = this.data.userInfo;
    if (!userInfo.studentId) return;
    try {
      const accommodations = await api.getAccommodationByStudentId(userInfo.studentId);
      const accommodation = Array.isArray(accommodations) && accommodations.length ? accommodations[0] : null;
      const studentName = accommodation ? accommodation.studentName : (userInfo.name || userInfo.username || '');
      const changes = studentName ? await api.getRoomChangesByStudentName(studentName) : [];
      const normalizedChanges = (Array.isArray(changes) ? changes : [])
        .map((item) => ({
          ...item,
          displayRoute: `${displayValue(item.oldDormitoryA)} 至 ${displayValue(item.newDormitoryA)}`,
          displayOldBed: displayBed(item.oldBedA),
          displayNewBed: displayBed(item.newBedA),
          statusText: changeStatusText(item.status),
          statusClass: changeStatusClass(item.status),
          changeTimeText: formatDateTime(item.changeTime || item.createdAt)
        }))
        .sort((a, b) => new Date(b.changeTime || b.createdAt || 0) - new Date(a.changeTime || a.createdAt || 0));

      this.setData({
        accommodation,
        accommodationDisplay: accommodation ? {
          dormitory: displayValue(accommodation.dormitory),
          bed: displayBed(accommodation.bed)
        } : null,
        changes: normalizedChanges
      });
    } catch (error) {
      this.setData({ changes: [], accommodationDisplay: null });
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: event.detail.value });
  },

  async submitChange() {
    const { accommodation, form } = this.data;
    if (!accommodation) {
      wx.showToast({ title: '未查询到住宿信息', icon: 'none' });
      return;
    }
    if (!form.newDormitory.trim() || !form.newBed.trim()) {
      wx.showToast({ title: '请填写新宿舍和新床位', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    try {
      await api.applyRoomChange({
        accommodationId: accommodation.id,
        newDormitory: form.newDormitory.trim(),
        newBed: form.newBed.trim()
      });
      wx.showToast({ title: '已提交', icon: 'success' });
      this.setData({
        'form.newDormitory': '',
        'form.newBed': ''
      });
      await this.fetchData();
    } finally {
      this.setData({ submitting: false });
    }
  }
});
