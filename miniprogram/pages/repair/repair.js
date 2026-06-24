const api = require('../../utils/api');
const { formatDateTime, repairStatusText, repairStatusClass } = require('../../utils/format');
const { localFallback, pageBackground } = require('../../utils/backgrounds');

const displayValue = (value) => (value === undefined || value === null || value === '' ? '-' : value);

Page({
  data: {
    bgUrl: pageBackground('repair'),
    localFallback,
    useLocalBg: false,
    userInfo: {},
    accommodation: null,
    repairs: [],
    form: {
      dormitory: '',
      description: ''
    },
    submitting: false
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  onShow() {
    if (!getApp().requireLogin()) return;
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({ userInfo });
    this.fetchData();
  },

  onPullDownRefresh() {
    this.fetchData().finally(() => wx.stopPullDownRefresh());
  },

  async fetchData() {
    const { studentId } = this.data.userInfo;
    if (!studentId) return;
    try {
      const [accommodations, repairs] = await Promise.all([
        api.getAccommodationByStudentId(studentId),
        api.getRepairsByStudentId(studentId)
      ]);
      const accommodation = Array.isArray(accommodations) && accommodations.length ? accommodations[0] : null;
      const normalizedRepairs = (Array.isArray(repairs) ? repairs : [])
        .map((item) => ({
          ...item,
          displayDormitory: displayValue(item.dormitory),
          displayDescription: displayValue(item.description),
          statusText: repairStatusText(item.status),
          statusClass: repairStatusClass(item.status),
          repairTimeText: formatDateTime(item.repairTime || item.createdAt)
        }))
        .sort((a, b) => new Date(b.repairTime || b.createdAt || 0) - new Date(a.repairTime || a.createdAt || 0));

      this.setData({
        accommodation,
        repairs: normalizedRepairs,
        'form.dormitory': this.data.form.dormitory || (accommodation ? accommodation.dormitory : '')
      });
    } catch (error) {
      this.setData({ repairs: [] });
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: event.detail.value });
  },

  async submitRepair() {
    const { dormitory, description } = this.data.form;
    const userInfo = this.data.userInfo;
    if (!dormitory.trim() || !description.trim()) {
      wx.showToast({ title: '请填写宿舍和问题描述', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    try {
      await api.applyRepair({
        studentName: userInfo.name || userInfo.username || '',
        studentId: userInfo.studentId,
        dormitory: dormitory.trim(),
        description: description.trim()
      });
      wx.showToast({ title: '已提交', icon: 'success' });
      this.setData({ 'form.description': '' });
      await this.fetchData();
    } finally {
      this.setData({ submitting: false });
    }
  }
});
