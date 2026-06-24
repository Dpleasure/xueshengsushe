<template>
  <div>
    <div class="stats-grid">
      <div v-for="stat in statsDisplay" :key="stat.title" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-title">{{ stat.title }}</div>
            <div class="stat-value" :style="{color: stat.color}">{{ stat.value }}</div>
          </div>
          <div class="stat-icon">{{ stat.icon }}</div>
        </div>
      </div>
    </div>
    
    <div class="welcome-card">
      <h3>欢迎使用学生宿舍管理系统</h3>
      <p>
        本系统提供宿舍楼信息管理、宿舍信息管理、住宿信息管理、换寝申请、寝室报修、来访登记等功能。
        请从左侧菜单选择相应的功能模块进行操作。
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { stats as globalStats } from '../store/index.js'

// 使用全局统计数据，动态绑定
const statsDisplay = computed(() => [
  { title: '宿舍楼总数', value: globalStats.value.buildingCount, icon: '🏢', color: '#1890ff' },
  { title: '宿舍总数', value: globalStats.value.dormitoryCount, icon: '🏠', color: '#52c41a' },
  { title: '入住学生', value: globalStats.value.studentCount, icon: '👨‍🎓', color: '#faad14' },
  { title: '报修记录', value: globalStats.value.repairCount, icon: '🔧', color: '#f5222d' }
])
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-title {
  color: #999;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
}

.welcome-card {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.welcome-card h3 {
  margin-bottom: 16px;
  color: #333;
}

.welcome-card p {
  color: #666;
  line-height: 1.8;
}
</style>

