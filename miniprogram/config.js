const config = {
  // 真机局域网调试地址：必须是电脑在当前 Wi-Fi 下的 IPv4 地址。
  lanHosts: [
    'http://10.31.3.71:8080'
  ],

  // 广域网地址可填公网 IP 或 HTTPS 域名。局域网不可达时会继续尝试这里。
  wanHosts: [],

  // 仅微信开发者工具使用；真机上 localhost/127.0.0.1 会指向手机自身，不能用于调试后端。
  devOnlyHosts: [
    'http://127.0.0.1:8080',
    'http://localhost:8080'
  ]
};

module.exports = config;
