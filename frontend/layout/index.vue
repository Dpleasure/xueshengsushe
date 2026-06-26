<template>
  <div class="main-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">🏫</div>
        <div class="logo-text">学生宿舍管理系统</div>
      </div>
      <div class="sidebar-menu">
        <router-link to="/home" class="menu-item" active-class="active">
          <span class="menu-item-icon">🏠</span>
          <span>系统首页</span>
        </router-link>
        
        <!-- 管理员菜单 -->
        <template v-if="isAdmin">
          <!-- 信息管理 -->
          <div class="menu-group">
            <div class="menu-group-title" @click="toggleGroup('info')">
              <div class="menu-group-icon">
                <span>📋</span>
                <span>信息管理</span>
              </div>
              <span class="menu-group-arrow" :class="{expanded: menuExpanded.info}">▶</span>
            </div>
            <div class="menu-group-items" :class="{expanded: menuExpanded.info}">
              <router-link to="/building" class="menu-item" active-class="active">
                <span>宿舍楼信息</span>
              </router-link>
              <router-link to="/dormitory" class="menu-item" active-class="active">
                <span>宿舍信息</span>
              </router-link>
              <router-link to="/accommodation" class="menu-item" active-class="active">
                <span>住宿信息</span>
              </router-link>
              <router-link to="/change" class="menu-item" active-class="active">
                <span>换寝信息</span>
              </router-link>
              <router-link to="/repair" class="menu-item" active-class="active">
                <span>寝室报修</span>
              </router-link>
              <router-link to="/visit" class="menu-item" active-class="active">
                <span>来访登记</span>
              </router-link>
            </div>
          </div>

          <!-- 用户管理 -->
          <div class="menu-group">
            <div class="menu-group-title" @click="toggleGroup('user')">
              <div class="menu-group-icon">
                <span>👥</span>
                <span>用户管理</span>
              </div>
              <span class="menu-group-arrow" :class="{expanded: menuExpanded.user}">▶</span>
            </div>
            <div class="menu-group-items" :class="{expanded: menuExpanded.user}">
              <router-link to="/admin" class="menu-item" active-class="active">
                <span>管理员信息</span>
              </router-link>
              <router-link to="/student" class="menu-item" active-class="active">
                <span>学生信息</span>
              </router-link>
            </div>
          </div>
        </template>

        <!-- 学生菜单 -->
        <template v-else>
          <div class="menu-group">
            <div class="menu-group-title" @click="toggleGroup('info')">
              <div class="menu-group-icon">
                <span>📋</span>
                <span>信息管理</span>
              </div>
              <span class="menu-group-arrow" :class="{expanded: menuExpanded.info}">▶</span>
            </div>
            <div class="menu-group-items" :class="{expanded: menuExpanded.info}">
              <router-link to="/apply-change" class="menu-item" active-class="active">
                <span>申请换寝</span>
              </router-link>
              <router-link to="/apply-repair" class="menu-item" active-class="active">
                <span>申请报修</span>
              </router-link>
              <router-link to="/apply-visit" class="menu-item" active-class="active">
                <span>申请登记</span>
              </router-link>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航 -->
      <div class="header">
        <div class="header-left">
          <div class="breadcrumb">
            <span>首页</span>
            <span class="breadcrumb-separator">></span>
            <span>{{ currentTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="user-info" @click="handleLogout">
            <div class="user-avatar">👤</div>
            <span>{{ displayRole }}</span>
          </div>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { loadData } from '../store/index.js'
import {get} from "@/net/run";
import {ElMessage} from "element-plus";

const router = useRouter()
const route = useRoute()

// 页面加载时获取所有数据
onMounted(async () => {
  try {
    await loadData()
  } catch (error) {
    console.error('加载数据失败:', error)
  }
})

const menuExpanded = ref({
  info: true,
  user: true
})

const userInfo = ref((() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    return {}
  }
})())

const currentTitle = computed(() => {
  return route.meta.title || '系统首页'
})

// 显示角色名称的计算属性
const displayRole = computed(() => {
  const role = userInfo.value.role
  if (!role) return '学生'
  
  // 支持多种格式：ADMIN、ROLE_ADMIN、admin、ROLE_admin 等
  const roleUpper = role.toUpperCase()
  if (roleUpper === 'ADMIN' || roleUpper === 'ROLE_ADMIN') {
    return '管理员'
  }
  return '学生'
})

const isAdmin = computed(() => {
  const role = userInfo.value.role
  if (!role) return false
  const roleUpper = role.toUpperCase()
  return roleUpper === 'ADMIN' || roleUpper === 'ROLE_ADMIN'
})

const toggleGroup = (group) => {
  menuExpanded.value[group] = !menuExpanded.value[group]
}

const handleLogout = () => {
    get('/api/auth/logout',(message)=>{
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.success(message)
      router.push('/login')
    })


}
</script>

<style scoped>
.main-container {
  display: flex;
  width: 100%;
  height: 100vh;
}

/* 侧边栏样式 */
.sidebar {
  width: 250px;
  background: #001529;
  color: white;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-logo {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
}

.sidebar-menu {
  flex: 1;
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-item.active {
  background: #1890ff;
  color: white;
}

.menu-item-icon {
  font-size: 18px;
}

.menu-group {
  margin-bottom: 8px;
}

.menu-group-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  transition: all 0.3s;
}

.menu-group-title:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-group-icon {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.menu-group-icon span:first-child {
  font-size: 18px;
}

.menu-group-arrow {
  font-size: 12px;
  transition: transform 0.3s;
}

.menu-group-arrow.expanded {
  transform: rotate(90deg);
}

.menu-group-items {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s;
}

.menu-group-items.expanded {
  max-height: 500px;
}

.menu-group-items .menu-item {
  padding-left: 60px;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  overflow: hidden;
}

.header {
  height: 60px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.breadcrumb {
  color: #666;
  font-size: 14px;
}

.breadcrumb-separator {
  margin: 0 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f0f2f5;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
