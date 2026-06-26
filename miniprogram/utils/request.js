const getAppConfig = () => getApp().globalData || {};

const isLoopbackHost = (host) => /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/i.test(host);

const isDevTools = () => {
  try {
    return wx.getSystemInfoSync().platform === 'devtools';
  } catch (error) {
    return false;
  }
};

const uniqueHosts = (hosts) => Array.from(new Set(
  hosts
    .filter(Boolean)
    .map((host) => host.replace(/\/$/, ''))
));

const getBackendHosts = () => {
  const appConfig = getAppConfig();
  const hosts = [
    appConfig.baseUrl,
    ...(appConfig.lanHosts || []),
    ...(appConfig.wanHosts || []),
    ...(isDevTools() ? (appConfig.devOnlyHosts || []) : [])
  ];

  return uniqueHosts(hosts).filter((host) => isDevTools() || !isLoopbackHost(host));
};

const getReachableBaseUrl = () => {
  const appConfig = getAppConfig();
  if (appConfig.baseUrl && (!isLoopbackHost(appConfig.baseUrl) || isDevTools())) {
    return appConfig.baseUrl.replace(/\/$/, '');
  }
  return getBackendHosts()[0] || '';
};

const buildCandidateUrls = (url) => {
  if (/^https?:\/\//i.test(url)) return [url];
  return getBackendHosts().map((host) => `${host}${url}`);
};

const rememberBaseUrl = (requestUrl) => {
  const matchedHost = requestUrl.match(/^https?:\/\/[^/]+/i);
  if (matchedHost && (isDevTools() || !isLoopbackHost(matchedHost[0]))) {
    getApp().globalData.baseUrl = matchedHost[0];
  }
};

const normalizeResourceUrl = (url) => {
  if (!url || url === '👤') return '';
  const baseUrl = getReachableBaseUrl();
  if (!baseUrl) return url;

  if (/^https?:\/\//i.test(url)) {
    if (isLoopbackHost(url.match(/^https?:\/\/[^/]+/i)?.[0] || '')) {
      return url.replace(/^https?:\/\/[^/]+/i, baseUrl);
    }
    return url;
  }

  if (url.startsWith('data:') || url.startsWith('wxfile://')) return url;
  return `${baseUrl}${url.startsWith('/') ? url : `/${url}`}`;
};

const showError = (message) => {
  wx.showToast({
    title: message || '\u8bf7\u6c42\u5931\u8d25',
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
    const candidateUrls = buildCandidateUrls(url);
    let index = 0;

    const send = () => {
      const requestUrl = candidateUrls[index];

      wx.request({
        url: requestUrl,
        method,
        data: isFormRequest ? encodeForm(data) : data,
        header: headers,
        success: (res) => {
          rememberBaseUrl(requestUrl);

          const body = res.data || {};
          if (res.statusCode === 401 || body.status === 401) {
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            showError('\u767b\u5f55\u5df2\u5931\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55');
            wx.reLaunch({ url: '/pages/login/login' });
            reject(body);
            return;
          }

          if (body.success === true) {
            resolve(body.message);
            return;
          }

          const message = typeof body.message === 'string' ? body.message : `\u8bf7\u6c42\u5931\u8d25(${res.statusCode})`;
          showError(message);
          reject(body);
        },
        fail: (error) => {
          index += 1;
          if (index < candidateUrls.length) {
            send();
            return;
          }
          showError('\u65e0\u6cd5\u8fde\u63a5\u540e\u7aef\u670d\u52a1');
          reject(error);
        }
      });
    };

    send();
  });
};

const upload = ({ url, filePath, name = 'image', formData = {} }) => {
  const token = wx.getStorageSync('token');
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    const candidateUrls = buildCandidateUrls(url);
    let index = 0;

    const send = () => {
      const uploadUrl = candidateUrls[index];

      wx.uploadFile({
        url: uploadUrl,
        filePath,
        name,
        formData,
        header: headers,
        success: (res) => {
          rememberBaseUrl(uploadUrl);

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
          showError(typeof body.message === 'string' ? body.message : '\u4e0a\u4f20\u5931\u8d25');
          reject(body);
        },
        fail: (error) => {
          index += 1;
          if (index < candidateUrls.length) {
            send();
            return;
          }
          showError('\u4e0a\u4f20\u5931\u8d25');
          reject(error);
        }
      });
    };

    send();
  });
};

module.exports = {
  request,
  upload,
  getBackendHosts,
  getReachableBaseUrl,
  normalizeResourceUrl
};
