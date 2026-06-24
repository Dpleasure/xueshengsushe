const api = require('../../utils/api');
const { localFallback, pageBackground } = require('../../utils/backgrounds');

Page({
  data: {
    bgUrl: pageBackground('login'),
    localFallback,
    useLocalBg: false,
    form: {
      username: '',
      password: '',
      studentId: '',
      role: 'student'
    },
    submitting: false
  },

  onLoad() {
    if (wx.getStorageSync('token')) {
      wx.switchTab({ url: '/pages/home/home' });
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({
      [`form.${field}`]: event.detail.value
    });
  },

  onBgError() {
    this.setData({ useLocalBg: true });
  },

  async handleLogin() {
    const { username, password, studentId } = this.data.form;
    if (!username.trim() || !password.trim() || !studentId.trim()) {
      wx.showToast({ title: '请填写账号、密码和学号', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    try {
      const response = await api.login({
        username: username.trim(),
        password,
        studentId: studentId.trim(),
        role: 'student'
      });

      const userInfo = {
        username: response.username,
        name: response.name || response.studentName || response.username,
        role: response.role,
        studentId: response.studentId || studentId.trim(),
        avatar: response.avatar || ''
      };

      wx.setStorageSync('token', response.token);
      wx.setStorageSync('userInfo', userInfo);
      getApp().globalData.token = response.token;
      getApp().globalData.userInfo = userInfo;

      wx.showToast({ title: '登录成功', icon: 'success' });
      wx.switchTab({ url: '/pages/home/home' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
