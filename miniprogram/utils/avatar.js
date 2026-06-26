const { normalizeResourceUrl } = require('./request');

const cache = {};

const extensionFromUrl = (url) => {
  const cleanUrl = String(url || '').split('?')[0];
  const match = cleanUrl.match(/\.(jpg|jpeg|png|gif|webp|bmp)$/i);
  return match ? `.${match[1].toLowerCase()}` : '.jpg';
};

const fileNameFromUrl = (url) => {
  let hash = 0;
  const text = String(url || '');
  for (let i = 0; i < text.length; i += 1) {
    hash = ((hash << 5) - hash) + text.charCodeAt(i);
    hash |= 0;
  }
  return `avatar_${Math.abs(hash)}${extensionFromUrl(url)}`;
};

const writeAvatarFile = (url, buffer) => new Promise((resolve, reject) => {
  if (!wx.env || !wx.env.USER_DATA_PATH) {
    reject(new Error('USER_DATA_PATH is unavailable'));
    return;
  }

  const filePath = `${wx.env.USER_DATA_PATH}/${fileNameFromUrl(url)}`;
  wx.getFileSystemManager().writeFile({
    filePath,
    data: buffer,
    success: () => {
      cache[url] = filePath;
      resolve(filePath);
    },
    fail: reject
  });
});

const loadAvatarUrl = (avatar) => new Promise((resolve) => {
  const url = normalizeResourceUrl(avatar);
  if (!url) {
    resolve('');
    return;
  }

  if (url.startsWith('data:') || url.startsWith('wxfile://')) {
    resolve(url);
    return;
  }

  if (!/^https?:\/\//i.test(url)) {
    resolve(url);
    return;
  }

  if (cache[url]) {
    resolve(cache[url]);
    return;
  }

  const requestUrl = `${url}${url.includes('?') ? '&' : '?'}_avatar_ts=${Date.now()}`;

  wx.request({
    url: requestUrl,
    method: 'GET',
    responseType: 'arraybuffer',
    success: async (res) => {
      if (res.statusCode < 200 || res.statusCode >= 300 || !res.data) {
        resolve(url);
        return;
      }

      try {
        const filePath = await writeAvatarFile(url, res.data);
        resolve(filePath);
      } catch (error) {
        resolve(url);
      }
    },
    fail: () => resolve(url)
  });
});

module.exports = {
  loadAvatarUrl
};
