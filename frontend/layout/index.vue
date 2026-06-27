<template>
  <div class="main-container" :class="{ 'student-home-layout': isStudentHome }">
    <aside v-if="!isStudentHome" class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <House />
        </div>
        <div class="logo-copy">
          <div class="logo-text">学生宿舍管理系统</div>
          <div class="logo-subtitle">Dormitory Console</div>
        </div>
      </div>

      <nav class="sidebar-menu">
        <router-link to="/home" class="menu-item" active-class="active">
          <el-icon><DataBoard /></el-icon>
          <span>系统首页</span>
        </router-link>

        <router-link to="/ai-assistant" class="menu-item" active-class="active">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能助手</span>
        </router-link>

        <template v-if="isAdmin">
          <div class="menu-group">
            <button class="menu-group-title" type="button" @click="toggleGroup('info')">
              <span class="menu-group-label">
                <el-icon><Files /></el-icon>
                <span>信息管理</span>
              </span>
              <el-icon class="menu-group-arrow" :class="{ expanded: menuExpanded.info }">
                <ArrowRight />
              </el-icon>
            </button>
            <div class="menu-group-items" :class="{ expanded: menuExpanded.info }">
              <router-link to="/building" class="menu-item nested" active-class="active">
                <span>宿舍楼信息</span>
              </router-link>
              <router-link to="/dormitory" class="menu-item nested" active-class="active">
                <span>宿舍信息</span>
              </router-link>
              <router-link to="/accommodation" class="menu-item nested" active-class="active">
                <span>住宿信息</span>
              </router-link>
              <router-link to="/change" class="menu-item nested" active-class="active">
                <span>换宿信息</span>
              </router-link>
              <router-link to="/repair" class="menu-item nested" active-class="active">
                <span>宿舍报修</span>
              </router-link>
              <router-link to="/visit" class="menu-item nested" active-class="active">
                <span>来访登记</span>
              </router-link>
            </div>
          </div>

          <div class="menu-group">
            <button class="menu-group-title" type="button" @click="toggleGroup('user')">
              <span class="menu-group-label">
                <el-icon><UserFilled /></el-icon>
                <span>用户管理</span>
              </span>
              <el-icon class="menu-group-arrow" :class="{ expanded: menuExpanded.user }">
                <ArrowRight />
              </el-icon>
            </button>
            <div class="menu-group-items" :class="{ expanded: menuExpanded.user }">
              <router-link to="/admin" class="menu-item nested" active-class="active">
                <span>管理员信息</span>
              </router-link>
              <router-link to="/student" class="menu-item nested" active-class="active">
                <span>学生信息</span>
              </router-link>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="menu-group">
            <button class="menu-group-title" type="button" @click="toggleGroup('info')">
              <span class="menu-group-label">
                <el-icon><Files /></el-icon>
                <span>申请管理</span>
              </span>
              <el-icon class="menu-group-arrow" :class="{ expanded: menuExpanded.info }">
                <ArrowRight />
              </el-icon>
            </button>
            <div class="menu-group-items" :class="{ expanded: menuExpanded.info }">
              <router-link to="/apply-change" class="menu-item nested" active-class="active">
                <span>申请换宿</span>
              </router-link>
              <router-link to="/apply-repair" class="menu-item nested" active-class="active">
                <span>申请报修</span>
              </router-link>
              <router-link to="/apply-visit" class="menu-item nested" active-class="active">
                <span>来访登记</span>
              </router-link>
            </div>
          </div>
        </template>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="header-actions">
          <el-tooltip content="查看个人信息" placement="bottom">
            <button class="user-info" type="button" @click="goProfile">
              <span class="user-avatar">
                <img v-if="avatarUrl && !avatarLoadFailed" :src="avatarUrl" alt="头像" @error="avatarLoadFailed = true">
                <span v-else>{{ avatarFallbackText }}</span>
              </span>
              <span class="user-role">{{ displayRole }}</span>
            </button>
          </el-tooltip>
          <el-button class="logout-button" type="danger" plain @click="handleLogout">
            退出
          </el-button>
        </div>
      </header>

      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowRight,
  ChatDotRound,
  DataBoard,
  Files,
  House,
  UserFilled
} from '@element-plus/icons-vue'
import { loadData } from '../store/index.js'
import { get } from '@/net/run'
import { ElMessage } from 'element-plus'
import { avatarFallback, normalizeAvatar, readUserInfo } from '../utils/avatar.js'

const router = useRouter()
const route = useRoute()
const avatarLoadFailed = ref(false)

onMounted(async () => {
  window.addEventListener('storage', refreshUserInfo)
  window.addEventListener('user-info-updated', refreshUserInfo)
  try {
    await loadData()
  } catch (error) {
    console.error('加载数据失败:', error)
  }
})

onUnmounted(() => {
  window.removeEventListener('storage', refreshUserInfo)
  window.removeEventListener('user-info-updated', refreshUserInfo)
})

const menuExpanded = ref({
  info: true,
  user: true
})

const userInfo = ref(readUserInfo())

const titleMap = {
  Home: '系统首页',
  Building: '宿舍楼信息',
  Dormitory: '宿舍信息',
  Accommodation: '住宿信息',
  Change: '换宿信息',
  Repair: '宿舍报修',
  Visit: '来访登记',
  Admin: '管理员信息',
  Student: '学生信息',
  Profile: '个人信息',
  ApplyChange: '申请换宿',
  ApplyRepair: '申请报修',
  ApplyVisit: '来访登记'
}

const currentTitle = computed(() => {
  return titleMap[route.name] || route.meta.title || '系统首页'
})

const isAdmin = computed(() => {
  const role = userInfo.value.role
  if (!role) return false
  const roleUpper = role.toUpperCase()
  return roleUpper === 'ADMIN' || roleUpper === 'ROLE_ADMIN'
})

const displayRole = computed(() => isAdmin.value ? '管理员' : '学生')

const isStudentHome = computed(() => !isAdmin.value && route.name === 'Home')

const displayName = computed(() => {
  return userInfo.value.name || userInfo.value.username || displayRole.value
})

const avatarFallbackText = computed(() => {
  return avatarFallback(userInfo.value, displayName.value || '用')
})

const avatarUrl = computed(() => {
  return normalizeAvatar(userInfo.value.avatar || userInfo.value.avatarUrl)
})

function refreshUserInfo() {
  userInfo.value = readUserInfo()
  avatarLoadFailed.value = false
}

const toggleGroup = (group) => {
  menuExpanded.value[group] = !menuExpanded.value[group]
}

const goProfile = () => {
  router.push('/profile')
}

const handleLogout = () => {
  get('/api/auth/logout', (message) => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success(message)
    router.push('/login')
  })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@600;700;800&family=Plus+Jakarta+Sans:wght@400;500&display=swap');

.main-container {
  --sidebar: #24324b;
  --accent: #4f7cff;
  --accent-2: #7c5cff;
  --text-main: #172033;
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  color: var(--text-main);
  background:
    radial-gradient(circle at 86% 8%, rgba(79, 124, 255, 0.14), transparent 28%),
    linear-gradient(135deg, #f6f8fc 0%, #eef3ff 48%, #f8fafc 100%);
  font-family: 'Plus Jakarta Sans', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

.sidebar {
  position: relative;
  width: 260px;
  min-width: 260px;
  margin: 16px 0 16px 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(180deg, var(--sidebar) 0%, #1d293f 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  box-shadow: 0 22px 48px rgba(29, 41, 63, 0.18);
  animation: fadeIn 600ms ease both;
}

.sidebar::after {
  content: '';
  position: absolute;
  right: -70px;
  bottom: -70px;
  width: 180px;
  height: 180px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(79, 124, 255, 0.28), transparent 66%);
  pointer-events: none;
}

.sidebar-logo {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  border-radius: 14px;
  box-shadow: 0 10px 26px rgba(79, 124, 255, 0.28);
}

.logo-copy {
  min-width: 0;
}

.logo-text {
  font-family: 'Outfit', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.25;
}

.logo-subtitle {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.58);
  font-size: 12px;
}

.sidebar-menu {
  position: relative;
  z-index: 1;
  flex: 1;
  padding: 14px 12px 20px;
  overflow-y: auto;
}

.menu-item,
.menu-group-title,
.user-info,
.logout-button {
  transition:
    color 300ms ease,
    background 300ms ease,
    border-color 300ms ease,
    box-shadow 300ms ease,
    transform 300ms ease;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 10px 12px;
  margin: 4px 0;
  color: rgba(255, 255, 255, 0.72);
  border: 1px solid transparent;
  border-radius: 12px;
  text-decoration: none;
}

.menu-item:hover {
  color: #fff;
  background: linear-gradient(135deg, rgba(79, 124, 255, 0.24), rgba(124, 92, 255, 0.16));
}

.menu-item.active {
  color: #fff;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  box-shadow: 0 12px 24px rgba(79, 124, 255, 0.24);
}

.menu-item.nested {
  min-height: 36px;
  padding-left: 38px;
  font-size: 13px;
}

.menu-group {
  margin-top: 8px;
}

.menu-group-title {
  width: 100%;
  min-height: 42px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  color: rgba(255, 255, 255, 0.66);
  background: transparent;
  border: 1px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  font: inherit;
}

.menu-group-title:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.06);
}

.menu-group-label {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.menu-group-arrow {
  font-size: 13px;
  transition: transform 300ms ease;
}

.menu-group-arrow.expanded {
  transform: rotate(90deg);
}

.menu-group-items {
  max-height: 0;
  overflow: hidden;
  transition: max-height 300ms ease;
}

.menu-group-items.expanded {
  max-height: 420px;
}

.main-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px;
}

.student-home-layout .main-content {
  padding: 14px;
}

.header {
  height: 64px;
  flex: 0 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 18px 0 22px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(216, 226, 245, 0.9);
  border-radius: 18px;
  box-shadow: 0 14px 32px rgba(41, 56, 86, 0.08);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  animation: fadeIn 600ms ease 80ms both;
}

.header :deep(.el-breadcrumb__inner),
.header :deep(.el-breadcrumb__separator) {
  color: #667085;
  font-weight: 500;
}

.header :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1f2937;
  font-weight: 700;
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 5px 12px 5px 5px;
  color: #1f2937;
  background: #fff;
  border: 1px solid #dbe5f5;
  border-radius: 999px;
  cursor: pointer;
  font: inherit;
  box-shadow: 0 8px 20px rgba(41, 56, 86, 0.08);
}

.user-info:hover {
  border-color: rgba(79, 124, 255, 0.5);
  box-shadow: 0 12px 28px rgba(79, 124, 255, 0.16);
  transform: translateY(-1px);
}

.user-avatar {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(135deg, var(--accent), var(--accent-2));
  border-radius: 999px;
  font-weight: 800;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-role {
  font-weight: 700;
}

.logout-button {
  min-height: 42px;
  border-radius: 999px;
  font-weight: 700;
}

.content {
  flex: 1;
  min-width: 0;
  min-height: 0;
  margin-top: 16px;
  overflow: auto;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 920px) {
  .main-container {
    height: auto;
    min-height: 100vh;
    flex-direction: column;
    overflow: auto;
  }

  .sidebar {
    width: auto;
    min-width: 0;
    margin: 12px;
  }

  .sidebar-menu {
    max-height: 320px;
  }

  .main-content {
    padding: 0 12px 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 1ms !important;
    transition-duration: 1ms !important;
  }
}
</style>
