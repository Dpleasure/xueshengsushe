const config = require('./config');

App({
  globalData: {
    lanHosts: config.lanHosts,
    wanHosts: config.wanHosts,
    devOnlyHosts: config.devOnlyHosts,
    baseUrl: [...config.lanHosts, ...config.wanHosts][0]
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    this.globalData.token = token || '';
    this.globalData.userInfo = userInfo || null;
  },

  requireLogin() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.reLaunch({ url: '/pages/login/login' });
      return false;
    }
    return true;
  },

  logout() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    this.globalData.token = '';
    this.globalData.userInfo = null;
    wx.reLaunch({ url: '/pages/login/login' });
  }
});
