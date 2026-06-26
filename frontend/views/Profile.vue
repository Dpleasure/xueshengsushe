<template>
  <div class="grid grid-cols-1 gap-6 pb-10 lg:grid-cols-3">
    <aside class="flex flex-col items-center rounded-2xl border border-slate-800/80 bg-slate-900/55 p-6 text-center shadow-lg backdrop-blur-xl lg:col-span-1">
      <div class="mb-6 flex w-full justify-around border-b border-slate-800/80 pb-4 text-sm">
        <span class="-mb-4 border-b-2 border-sky-400 pb-3 font-bold text-sky-400">资料概览</span>
        <button class="-mb-4 border-0 bg-transparent pb-3 text-slate-400 transition hover:text-slate-200" type="button" @click="router.push('/home')">
          工作台
        </button>
      </div>

      <button
        class="group relative h-28 w-28 overflow-hidden rounded-full border-2 border-slate-700/70 shadow-md transition hover:scale-105"
        type="button"
        @click="triggerFileInput"
      >
        <img v-if="avatarUrl" :src="avatarUrl" class="h-full w-full object-cover" alt="头像">
        <span v-else class="flex h-full w-full items-center justify-center bg-gradient-to-tr from-sky-400 to-teal-300 text-3xl font-black text-slate-950">
          {{ avatarFallback }}
        </span>
        <span class="absolute inset-0 flex flex-col items-center justify-center bg-black/60 opacity-0 transition group-hover:opacity-100">
          <span class="mb-1 text-[10px] font-bold uppercase tracking-wider text-white">
            {{ uploading ? '上传中...' : '更换头像' }}
          </span>
          <span class="text-[8px] text-slate-300">Max 2MB</span>
        </span>
      </button>
      <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="handleFileChange">

      <h2 class="mt-5 text-xl font-bold tracking-wide text-white">{{ displayName }}</h2>
      <span class="mt-2 inline-flex items-center gap-1.5 rounded-full border border-sky-500/20 bg-sky-500/10 px-3 py-1 text-xs font-semibold text-sky-300">
        <span class="h-1.5 w-1.5 rounded-full bg-sky-400"></span>
        {{ displayRole }}
      </span>

      <div class="my-6 w-full border-t border-slate-800/70"></div>

      <div class="grid w-full grid-cols-3 gap-2 text-center text-xs">
        <div class="border-r border-slate-800">
          <strong class="block truncate text-sm font-semibold text-white">{{ userInfo.studentId || '-' }}</strong>
          <span class="mt-1 block text-[10px] font-medium text-slate-500">学号/工号</span>
        </div>
        <div class="border-r border-slate-800">
          <strong class="block truncate text-sm font-semibold text-white">{{ userInfo.username || '-' }}</strong>
          <span class="mt-1 block text-[10px] font-medium text-slate-500">系统账号</span>
        </div>
        <div>
          <strong class="block truncate text-sm font-semibold text-white">{{ displayRole }}</strong>
          <span class="mt-1 block text-[10px] font-medium text-slate-500">角色身份</span>
        </div>
      </div>

      <div class="my-6 w-full border-t border-slate-800/70"></div>

      <div class="grid w-full grid-cols-2 gap-3">
        <el-button class="!h-10 !w-full !border-slate-800 !bg-slate-950/30 !text-slate-300 hover:!bg-slate-800/40" @click="router.push('/home')">
          系统首页
        </el-button>
        <el-button class="!h-10 !w-full !border-slate-800 !bg-slate-950/30 !text-slate-300 hover:!bg-slate-800/40" @click="router.push(isAdmin ? '/repair' : '/apply-repair')">
          报修中心
        </el-button>
        <el-button class="!h-10 !w-full !border-slate-800 !bg-slate-950/30 !text-slate-300 hover:!bg-slate-800/40" @click="router.push(isAdmin ? '/visit' : '/apply-visit')">
          来访登记
        </el-button>
        <el-button class="!h-10 !w-full !border-slate-800 !bg-slate-950/30 !text-slate-300 hover:!bg-slate-800/40" @click="router.push(isAdmin ? '/accommodation' : '/apply-change')">
          住宿服务
        </el-button>
      </div>
    </aside>

    <section class="flex flex-col rounded-2xl border border-slate-800/80 bg-slate-900/55 p-6 shadow-lg backdrop-blur-xl sm:p-8 lg:col-span-2">
      <div class="mb-6">
        <span class="text-xs font-bold uppercase tracking-wider text-sky-400">Profile Details</span>
        <h3 class="mt-1 text-xl font-bold tracking-wide text-white">个人详细资料</h3>
      </div>

      <el-descriptions :column="descriptionColumns" border class="custom-descriptions">
        <el-descriptions-item label="系统账号" label-class-name="desc-label" class-name="desc-value">
          {{ displayValue(userInfo.username) }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名" label-class-name="desc-label" class-name="desc-value">
          {{ displayValue(userInfo.name) }}
        </el-descriptions-item>
        <el-descriptions-item label="学号 / 工号" label-class-name="desc-label" class-name="desc-value">
          <el-tag size="small" class="!border-sky-500/20 !bg-sky-500/10 !text-sky-300">
            {{ displayValue(userInfo.studentId) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="手机号码" label-class-name="desc-label" class-name="desc-value">
          {{ displayValue(userInfo.phone) }}
        </el-descriptions-item>
        <el-descriptions-item label="电子邮箱" label-class-name="desc-label" class-name="desc-value">
          {{ displayValue(userInfo.email) }}
        </el-descriptions-item>
        <el-descriptions-item label="角色权限" label-class-name="desc-label" class-name="desc-value">
          <el-tag size="small" class="!border-teal-500/20 !bg-teal-500/10 !text-teal-300">
            {{ displayRole }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="mt-6 flex items-start gap-3 rounded-xl border border-slate-800 bg-slate-950/40 p-4">
        <span class="select-none text-lg text-sky-400">i</span>
        <div class="text-xs leading-relaxed text-slate-400">
          <span class="mb-0.5 block font-bold text-slate-300">安全与资料说明</span>
          基本账号资料无法在客户端直接修改，如需变更姓名、学号或电话，请联系学校宿舍管理人员处理。头像会同步到 Web 和小程序。
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadAvatar } from '../api/userApi.js'
import { withApiBaseUrl } from '../api/baseUrl.js'

const router = useRouter()
const userInfo = ref(readUserInfo())
const fileInput = ref(null)
const uploading = ref(false)
const windowWidth = ref(window.innerWidth)

onMounted(() => {
  window.addEventListener('resize', updateWidth)
  window.addEventListener('storage', refreshUserInfo)
  window.addEventListener('user-info-updated', refreshUserInfo)
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
const avatarFallback = computed(() => String(displayName.value || '学').slice(0, 1))
const avatarUrl = computed(() => normalizeAvatar(userInfo.value.avatar || userInfo.value.avatarUrl))
const descriptionColumns = computed(() => (windowWidth.value > 640 ? 2 : 1))

function readUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    return {}
  }
}

function refreshUserInfo() {
  userInfo.value = readUserInfo()
}

function updateWidth() {
  windowWidth.value = window.innerWidth
}

function displayValue(value) {
  return value === undefined || value === null || value === '' ? '未设置' : value
}

function normalizeAvatar(avatar) {
  if (!avatar || avatar === '👤') return ''
  if (/^https?:\/\//i.test(avatar) || avatar.startsWith('data:')) return avatar
  return withApiBaseUrl(avatar)
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
    const nextUser = { ...userInfo.value, ...response.data, avatar: response.data.avatar }
    userInfo.value = nextUser
    localStorage.setItem('userInfo', JSON.stringify(nextUser))
    window.dispatchEvent(new Event('user-info-updated'))
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
