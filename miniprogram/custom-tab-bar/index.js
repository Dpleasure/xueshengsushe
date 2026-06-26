Component({
  data: {
    selected: 0,
    pressed: -1,
    tabs: [
      { pagePath: '/pages/home/home', text: '\u9996\u9875', icon: 'icon-home' },
      { pagePath: '/pages/repair/repair', text: '\u62a5\u4fee', icon: 'icon-repair' },
      { pagePath: '/pages/change/change', text: '\u6362\u5bbf', icon: 'icon-change' },
      { pagePath: '/pages/visit/visit', text: '\u6765\u8bbf', icon: 'icon-visit' },
      { pagePath: '/pages/profile/profile', text: '\u6211\u7684', icon: 'icon-profile' }
    ]
  },

  methods: {
    switchTab(event) {
      const index = Number(event.currentTarget.dataset.index);
      const tab = this.data.tabs[index];
      if (!tab) return;
      this.setData({ selected: index, pressed: index });
      wx.switchTab({ url: tab.pagePath });
      setTimeout(() => this.setData({ pressed: -1 }), 160);
    },

    onPress(event) {
      this.setData({ pressed: Number(event.currentTarget.dataset.index) });
    },

    clearPress() {
      this.setData({ pressed: -1 });
    }
  }
});
