const getBaseUrl = () => getApp().globalData.baseUrl;

const normalizeUrl = (url) => {
  if (/^https?:\/\//i.test(url)) return url;
  return `${getBaseUrl()}${url}`;
};

const showError = (message) => {
  wx.showToast({
    title: message || '请求失败',
    icon: 'none',
    duration: 2200
  });
};

const encodeForm = (data) => Object.keys(data || {})
  .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(data[key] == null ? '' : data[key])}`)
  .join('&');

const request = ({ url, method = 'GET', data = {}, header = {} }) => {
  const token = wx.getStorageSync('token');
  const headers = {
    'Content-Type': 'application/json',
    ...header
  };
  const isFormRequest = headers['Content-Type'] === 'application/x-www-form-urlencoded';

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: normalizeUrl(url),
      method,
      data: isFormRequest ? encodeForm(data) : data,
      header: headers,
      success: (res) => {
        const body = res.data || {};
        if (res.statusCode === 401 || body.status === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          showError('登录已失效，请重新登录');
          wx.reLaunch({ url: '/pages/login/login' });
          reject(body);
          return;
        }

        if (body.success === true) {
          resolve(body.message);
          return;
        }

        const message = typeof body.message === 'string' ? body.message : `请求失败(${res.statusCode})`;
        showError(message);
        reject(body);
      },
      fail: (error) => {
        showError('无法连接后端服务');
        reject(error);
      }
    });
  });
};

const upload = ({ url, filePath, name = 'image', formData = {} }) => {
  const token = wx.getStorageSync('token');
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: normalizeUrl(url),
      filePath,
      name,
      formData,
      header: headers,
      success: (res) => {
        let body = {};
        try {
          body = JSON.parse(res.data || '{}');
        } catch (error) {
          reject(error);
          return;
        }
        if (body.success === true) {
          resolve(body.message);
          return;
        }
        showError(typeof body.message === 'string' ? body.message : '上传失败');
        reject(body);
      },
      fail: (error) => {
        showError('上传失败');
        reject(error);
      }
    });
  });
};

module.exports = {
  request,
  upload
};
