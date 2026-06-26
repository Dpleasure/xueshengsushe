<template>
  <div class="profile-page">
    <aside class="profile-card profile-summary">
      <div class="profile-tabs">
        <span class="profile-tab active">资料概览</span>
        <button class="profile-tab" type="button" @click="router.push('/home')">
          工作台
        </button>
      </div>

      <button
        class="profile-avatar-button"
        type="button"
        @click="triggerFileInput"
      >
        <img v-if="avatarUrl && !avatarLoadFailed" :src="avatarUrl" class="profile-avatar-img" alt="头像" @error="avatarLoadFailed = true">
        <span v-else class="profile-avatar-text">
          {{ avatarFallback }}
        </span>
        <span class="profile-avatar-mask">
          <span class="profile-avatar-action">
            {{ uploading ? '上传中...' : '更换头像' }}
          </span>
          <span class="profile-avatar-limit">Max 2MB</span>
        </span>
      </button>
      <input ref="fileInput" type="file" accept="image/*" class="file-input" @change="handleFileChange">

      <h2 class="profile-name">{{ displayName }}</h2>
      <span class="profile-role">
        <span class="role-dot"></span>
        {{ displayRole }}
      </span>

      <div class="profile-divider"></div>

      <div class="profile-stats">
        <div class="profile-stat">
          <strong>{{ userInfo.studentId || '-' }}</strong>
          <span>学号/工号</span>
        </div>
        <div class="profile-stat">
          <strong>{{ userInfo.username || '-' }}</strong>
          <span>系统账号</span>
        </div>
        <div class="profile-stat">
          <strong>{{ displayRole }}</strong>
          <span>角色身份</span>
        </div>
      </div>

      <div class="profile-divider"></div>

      <div class="profile-actions">
        <el-button @click="router.push('/home')">
          系统首页
        </el-button>
        <el-button @click="router.push(isAdmin ? '/repair' : '/apply-repair')">
          报修中心
        </el-button>
        <el-button @click="router.push(isAdmin ? '/visit' : '/apply-visit')">
          来访登记
        </el-button>
        <el-button @click="router.push(isAdmin ? '/accommodation' : '/apply-change')">
          住宿服务
        </el-button>
      </div>
    </aside>

    <section class="profile-card profile-detail">
      <div class="profile-section-title">
        <span>Profile Details</span>
        <h3>个人详细资料</h3>
      </div>

      <el-descriptions :column="descriptionColumns" border class="custom-descriptions">
        <el-descriptions-item label="系统账号">
          {{ displayValue(userInfo.username) }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ displayValue(userInfo.name) }}
        </el-descriptions-item>
        <el-descriptions-item label="学号 / 工号">
          <el-tag size="small" type="primary">
            {{ displayValue(userInfo.studentId) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手机号码">
          {{ displayValue(userInfo.phone) }}
        </el-descriptions-item>
        <el-descriptions-item label="电子邮箱">
          {{ displayValue(userInfo.email) }}
        </el-descriptions-item>
        <el-descriptions-item label="角色权限">
          <el-tag size="small" type="success">
            {{ displayRole }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="profile-note">
        <span class="profile-note-icon">i</span>
        <div>
          <span class="profile-note-title">安全与资料说明</span>
          基本账号资料无法在客户端直接修改，如需变更姓名、学号或电话，请联系学校宿舍管理人员处理。头像会同步到 Web 端各个页面。
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCurrentUser, uploadAvatar } from '../api/userApi.js'
import { admins, students } from '../store/index.js'
import {
  avatarFallback as getAvatarFallback,
  normalizeAvatar,
  persistUserInfo,
  readUserInfo
} from '../utils/avatar.js'

const router = useRouter()
const userInfo = ref(readUserInfo())
const fileInput = ref(null)
const uploading = ref(false)
const windowWidth = ref(window.innerWidth)
const avatarLoadFailed = ref(false)

onMounted(async () => {
  window.addEventListener('resize', updateWidth)
  window.addEventListener('storage', refreshUserInfo)
  window.addEventListener('user-info-updated', refreshUserInfo)
  await loadCurrentUser()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateWidth)
  window.removeEventListener('storage', refreshUserInfo)
  window.removeEventListener('user-info-updated', refreshUserInfo)
})

const isAdmin = computed(() => {
  const role = String(userInfo.value.role || '').toUpperCase()
  return role === 'ADMIN' || role === 'ROLE_ADMIN'
})
const displayRole = computed(() => (isAdmin.value ? '管理员' : '学生'))
const displayName = computed(() => userInfo.value.name || userInfo.value.username || '未命名用户')
const avatarFallback = computed(() => getAvatarFallback(userInfo.value, '学'))
const avatarUrl = computed(() => normalizeAvatar(userInfo.value.avatar || userInfo.value.avatarUrl))
const descriptionColumns = computed(() => (windowWidth.value > 640 ? 2 : 1))

function refreshUserInfo() {
  userInfo.value = readUserInfo()
  avatarLoadFailed.value = false
}

function updateWidth() {
  windowWidth.value = window.innerWidth
}

function displayValue(value) {
  return value === undefined || value === null || value === '' ? '未设置' : value
}

async function loadCurrentUser() {
  try {
    const response = await getCurrentUser()
    applyUserInfo(response.data)
  } catch (error) {
    console.error('获取个人信息失败:', error)
    userInfo.value = readUserInfo()
  }
}

function applyUserInfo(nextUser) {
  userInfo.value = nextUser
  avatarLoadFailed.value = false
  persistUserInfo(nextUser)
  syncUserList(students.value, nextUser)
  syncUserList(admins.value, nextUser)
}

function syncUserList(list, nextUser) {
  const index = list.findIndex(item => item.id === nextUser.id || item.username === nextUser.username)
  if (index !== -1) {
    list.splice(index, 1, { ...list[index], ...nextUser })
  }
}

function triggerFileInput() {
  if (!uploading.value) fileInput.value?.click()
}

async function handleFileChange(event) {
  const file = event.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片文件')
    event.target.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    event.target.value = ''
    return
  }

  uploading.value = true
  try {
    const response = await uploadAvatar(file)
    applyUserInfo({ ...userInfo.value, ...response.data })
    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}
</script>

<style scoped>
.profile-page {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
  padding-bottom: 24px;
}

.profile-card {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid #dde7f6;
  border-radius: 18px;
  box-shadow: 0 16px 36px rgba(41, 56, 86, 0.08);
}

.profile-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 22px;
  text-align: center;
}

.profile-tabs {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 8px;
  padding-bottom: 14px;
  margin-bottom: 22px;
  border-bottom: 1px solid #edf2f7;
}

.profile-tab {
  padding: 0 0 8px;
  color: #667085;
  background: transparent;
  border: 0;
  cursor: pointer;
  font: inherit;
  font-weight: 700;
}

.profile-tab.active {
  color: #2563eb;
  border-bottom: 2px solid #4f7cff;
}

.profile-avatar-button {
  position: relative;
  width: 112px;
  height: 112px;
  padding: 0;
  overflow: hidden;
  border: 3px solid #e5edfb;
  border-radius: 999px;
  background: linear-gradient(135deg, #4f7cff, #7c5cff);
  cursor: pointer;
  box-shadow: 0 16px 28px rgba(79, 124, 255, 0.2);
}

.profile-avatar-button:hover .profile-avatar-mask {
  opacity: 1;
}

.profile-avatar-img,
.profile-avatar-text {
  width: 100%;
  height: 100%;
}

.profile-avatar-img {
  display: block;
  object-fit: cover;
}

.profile-avatar-text {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36px;
  font-weight: 900;
}

.profile-avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  color: #fff;
  background: rgba(15, 23, 42, 0.62);
  opacity: 0;
  transition: opacity 200ms ease;
}

.profile-avatar-action {
  font-size: 12px;
  font-weight: 800;
}

.profile-avatar-limit {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.78);
}

.file-input {
  display: none;
}

.profile-name {
  margin: 18px 0 8px;
  color: #172033;
  font-size: 22px;
  font-weight: 900;
}

.profile-role {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 12px;
  color: #2563eb;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 800;
}

.role-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: #4f7cff;
}

.profile-divider {
  width: 100%;
  margin: 22px 0;
  border-top: 1px solid #edf2f7;
}

.profile-stats {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.profile-stat {
  min-width: 0;
  padding: 0 8px;
  border-right: 1px solid #edf2f7;
}

.profile-stat:last-child {
  border-right: 0;
}

.profile-stat strong {
  display: block;
  overflow: hidden;
  color: #172033;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-stat span {
  display: block;
  margin-top: 5px;
  color: #667085;
  font-size: 11px;
  font-weight: 700;
}

.profile-actions {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.profile-actions :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

.profile-detail {
  min-width: 0;
  padding: 24px;
}

.profile-section-title {
  margin-bottom: 20px;
}

.profile-section-title span {
  color: #2563eb;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.profile-section-title h3 {
  margin: 6px 0 0;
  color: #172033;
  font-size: 22px;
  font-weight: 900;
}

.custom-descriptions :deep(.el-descriptions__label) {
  color: #344054;
  background: #f8fafc;
  font-weight: 800;
}

.custom-descriptions :deep(.el-descriptions__content) {
  color: #172033;
  font-weight: 600;
}

.profile-note {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 22px;
  padding: 14px;
  color: #667085;
  background: #f8fbff;
  border: 1px solid #dde7f6;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.7;
}

.profile-note-icon {
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #4f7cff;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.profile-note-title {
  display: block;
  margin-bottom: 3px;
  color: #344054;
  font-weight: 900;
}

@media (max-width: 960px) {
  .profile-page {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .profile-summary,
  .profile-detail {
    padding: 18px;
  }

  .profile-stats,
  .profile-actions {
    grid-template-columns: 1fr;
  }

  .profile-stat {
    padding: 10px 0;
    border-right: 0;
    border-bottom: 1px solid #edf2f7;
  }

  .profile-stat:last-child {
    border-bottom: 0;
  }
}
</style>
