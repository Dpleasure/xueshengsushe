const api = require('../../utils/api');
const { formatDateTime, toIsoFromLocal, visitStatusText, visitStatusClass } = require('../../utils/format');
const { localFallback, pageBackground } = require('../../utils/backgrounds');

const displayValue = (value) => (value === undefined || value === null || value === '' ? '-' : value);

const today = () => {
  const date = new Date();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
};

Page({
  data: {
    bgUrl: pageBackground('visit'),
    localFallback,
    useLocalBg: false,
    userInfo: {},
    accommodation: null,
    visits: [],
    form: {
      visitorName: '',
      dormitory: '',
      visitDate: '',
      visitTime: '09:00',
      description: ''
    },
    visitDateText: '选择日期',
    visitTimeText: '09:00',
    submitting: false
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  onShow() {
    if (!getApp().requireLogin()) return;
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
      const [accommodations, visits] = await Promise.all([
        api.getAccommodationByStudentId(userInfo.studentId),
        api.getVisitsByStudentId(userInfo.studentId)
      ]);
      const accommodation = Array.isArray(accommodations) && accommodations.length ? accommodations[0] : null;
      const normalizedVisits = (Array.isArray(visits) ? visits : [])
        .map((item) => ({
          ...item,
          displayVisitorName: displayValue(item.visitorName),
          displayDormitory: displayValue(item.dormitory),
          displayDescription: displayValue(item.description),
          statusText: visitStatusText(item.status),
          statusClass: visitStatusClass(item.status),
          visitTimeText: formatDateTime(item.visitTime || item.createdAt)
        }))
        .sort((a, b) => new Date(b.visitTime || b.createdAt || 0) - new Date(a.visitTime || a.createdAt || 0));
      const visitDate = this.data.form.visitDate || today();
      const visitTime = this.data.form.visitTime || '09:00';

      this.setData({
        accommodation,
        visits: normalizedVisits,
        'form.dormitory': this.data.form.dormitory || (accommodation ? accommodation.dormitory : ''),
        'form.visitDate': visitDate,
        'form.visitTime': visitTime,
        visitDateText: visitDate || '选择日期',
        visitTimeText: visitTime || '选择时间'
      });
    } catch (error) {
      this.setData({ visits: [] });
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: event.detail.value });
  },

  onDateChange(event) {
    this.setData({
      'form.visitDate': event.detail.value,
      visitDateText: event.detail.value || '选择日期'
    });
  },

  onTimeChange(event) {
    this.setData({
      'form.visitTime': event.detail.value,
      visitTimeText: event.detail.value || '选择时间'
    });
  },

  async submitVisit() {
    const form = this.data.form;
    const userInfo = this.data.userInfo;
    const studentName = this.data.accommodation ? this.data.accommodation.studentName : (userInfo.name || userInfo.username || '');
    if (!form.visitorName.trim() || !form.dormitory.trim() || !form.visitDate || !form.visitTime || !form.description.trim()) {
      wx.showToast({ title: '请填写完整来访信息', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    try {
      await api.applyVisit({
        visitorName: form.visitorName.trim(),
        dormitory: form.dormitory.trim(),
        description: form.description.trim(),
        visitTime: toIsoFromLocal(form.visitDate, form.visitTime),
        studentId: userInfo.studentId,
        studentName
      });
      wx.showToast({ title: '已提交', icon: 'success' });
      this.setData({
        'form.visitorName': '',
        'form.description': ''
      });
      await this.fetchData();
    } finally {
      this.setData({ submitting: false });
    }
  }
});
