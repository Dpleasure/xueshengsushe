<template>
  <div v-if="isAdmin" class="home-page">
    <section class="overview-panel">
      <div class="overview-copy">
        <el-tag class="section-tag" effect="plain">Dormitory Dashboard</el-tag>
        <h1>宿舍管理工作台</h1>
        <p>集中查看宿舍楼、房间、住宿学生和报修记录，快速进入高频管理任务。</p>
      </div>
      <div class="overview-actions">
        <el-button type="primary" round @click="goToPrimary">{{ primaryAction.label }}</el-button>
        <el-button round @click="goToSecondary">{{ secondaryAction.label }}</el-button>
      </div>
    </section>

    <el-row class="stats-row" :gutter="16">
      <el-col v-for="(stat, index) in statsDisplay" :key="stat.title" :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card" :class="`lift-${index}`" shadow="never">
          <div class="stat-content">
            <div>
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-value">{{ stat.value }}</div>
            </div>
            <div class="stat-icon" :class="stat.tone">
              <component :is="stat.icon" />
            </div>
          </div>
          <div class="stat-note">{{ stat.note }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row class="dashboard-row" :gutter="16">
      <el-col :xs="24" :lg="15">
        <el-card class="dashboard-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>常用入口</span>
              <el-tag size="small" effect="plain">{{ displayRole }}</el-tag>
            </div>
          </template>
          <div class="quick-grid">
            <router-link v-for="action in adminQuickActions" :key="action.path" :to="action.path" class="quick-action">
              <el-icon><component :is="action.icon" /></el-icon>
              <span>{{ action.label }}</span>
            </router-link>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="9">
        <el-card class="dashboard-card service-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>今日工作概览</span>
              <el-tag size="small" type="success" effect="plain">运行中</el-tag>
            </div>
          </template>
          <div class="service-summary">
            <div>
              <div class="summary-number">{{ totalResources }}</div>
              <div class="summary-label">系统资源总量</div>
            </div>
            <el-progress type="dashboard" :percentage="overviewPercent" :width="112" color="#4f7cff" />
          </div>
          <p class="service-note">
            优先处理待审核申请与未完成报修，保持住宿数据、来访登记和房间状态同步更新。
          </p>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <div v-else class="student-bento-page">
    <section class="bento-hero">
      <div>
        <span class="eyebrow">Student Console</span>
        <h1>{{ displayName }}，今天要办理什么？</h1>
      </div>
      <button class="profile-chip" type="button" @click="router.push('/profile')">
        <el-icon><User /></el-icon>
        个人信息
      </button>
    </section>

    <section class="bento-grid">
      <button
        v-for="feature in studentFeatures"
        :key="feature.key"
        class="bento-card feature-card"
        :class="[feature.size, { active: activeFeature === feature.key }]"
        type="button"
        @click="setActiveFeature(feature.key)"
      >
        <span class="feature-icon">
          <el-icon><component :is="feature.icon" /></el-icon>
        </span>
        <span class="feature-title">{{ feature.title }}</span>
        <span class="feature-meta">{{ feature.meta }}</span>
      </button>

      <div class="bento-card summary-card">
        <span class="eyebrow">Current Room</span>
        <h2>{{ myAccommodation?.dormitory || '暂无宿舍' }}</h2>
        <div class="summary-grid">
          <div>
            <strong>{{ myAccommodation?.bed || '-' }}</strong>
            <span>床位</span>
          </div>
          <div>
            <strong>{{ myAccommodation?.building || currentDormitory?.building || '-' }}</strong>
            <span>楼栋</span>
          </div>
        </div>
      </div>

      <div class="bento-card status-card">
        <span class="eyebrow">Records</span>
        <div class="mini-stat">
          <strong>{{ changeApplications.length }}</strong>
          <span>换宿</span>
        </div>
        <div class="mini-stat">
          <strong>{{ repairApplications.length }}</strong>
          <span>报修</span>
        </div>
        <div class="mini-stat">
          <strong>{{ visitApplications.length }}</strong>
          <span>来访</span>
        </div>
      </div>
    </section>

    <div v-if="studentLoading" class="bento-card empty-state">数据加载中...</div>

    <div v-else-if="!myAccommodation" class="bento-card empty-state">
      <h2>暂无住宿信息</h2>
      <p>当前账号还没有绑定宿舍，暂时不能办理换宿、报修和来访登记。</p>
    </div>

    <section v-else class="bento-card work-panel">
      <div class="panel-header">
        <div>
          <span class="eyebrow">{{ activeFeatureLabel.en }}</span>
          <h2>{{ activeFeatureLabel.zh }}</h2>
        </div>
        <el-tag effect="plain">{{ myAccommodation.dormitory }} / {{ myAccommodation.bed }}</el-tag>
      </div>

      <div v-if="activeFeature === 'change'" class="feature-panel">
        <div class="form-grid">
          <div class="form-block">
            <label>当前宿舍</label>
            <input class="bento-input" :value="myAccommodation.dormitory" disabled>
          </div>
          <div class="form-block">
            <label>当前床位</label>
            <input class="bento-input" :value="myAccommodation.bed" disabled>
          </div>
          <div class="form-block">
            <label>选择可换宿舍</label>
            <select v-model="selectedDormitoryNumber" class="bento-input" @change="selectedBed = ''">
              <option value="">请选择宿舍</option>
              <option v-for="dorm in availableDormitories" :key="dorm.id || dorm.number" :value="dorm.number">
                {{ dorm.building }} / {{ dorm.number }}（空 {{ getFreeCount(dorm) }}）
              </option>
            </select>
          </div>
        </div>

        <div v-if="selectedDormitory" class="bed-picker">
          <button
            v-for="bed in selectedRoomBeds"
            :key="bed.name"
            class="bed-cell"
            :class="[bed.status, { selected: selectedBed === bed.name }]"
            type="button"
            :disabled="bed.status !== 'free'"
            @click="selectedBed = bed.name"
          >
            <strong>{{ bed.name }}</strong>
            <span>{{ bed.label }}</span>
          </button>
        </div>
        <div v-else class="soft-empty">选择宿舍后可查看可申请床位。</div>

        <el-button class="panel-submit" type="primary" :loading="changeSubmitting" :disabled="!selectedBed" @click="submitChange">
          提交换宿申请
        </el-button>
        <record-list title="换宿记录" :records="changeRows" />
      </div>

      <div v-if="activeFeature === 'repair'" class="feature-panel">
        <div class="form-grid">
          <div class="form-block">
            <label>报修宿舍</label>
            <input class="bento-input" :value="myAccommodation.dormitory" disabled>
          </div>
          <div class="form-block wide">
            <label>报修说明</label>
            <textarea v-model="repairDescription" class="bento-input textarea" maxlength="500" placeholder="请详细描述报修内容"></textarea>
          </div>
          <div class="form-block wide">
            <label>上传图片（可选）</label>
            <input class="bento-input" type="file" accept="image/*" @change="handleRepairFileChange">
          </div>
        </div>
        <el-button class="panel-submit" type="primary" :loading="repairSubmitting" @click="submitRepair">
          提交报修申请
        </el-button>
        <record-list title="报修记录" :records="repairRows" />
      </div>

      <div v-if="activeFeature === 'visit'" class="feature-panel">
        <div class="form-grid">
          <div class="form-block">
            <label>来访人姓名</label>
            <input v-model.trim="visitForm.visitorName" class="bento-input" maxlength="50" placeholder="请输入姓名">
          </div>
          <div class="form-block">
            <label>来访时间</label>
            <input v-model="visitForm.visitTime" class="bento-input" type="datetime-local">
          </div>
          <div class="form-block wide">
            <label>来访说明</label>
            <textarea v-model="visitForm.description" class="bento-input textarea" maxlength="500" placeholder="请简要说明来访事由"></textarea>
          </div>
        </div>
        <el-button class="panel-submit" type="primary" :loading="visitSubmitting" @click="submitVisit">
          提交来访登记
        </el-button>
        <record-list title="来访记录" :records="visitRows" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  House,
  OfficeBuilding,
  School,
  Switch,
  Tickets,
  Tools,
  User
} from '@element-plus/icons-vue'
import { stats as globalStats } from '../store/index.js'
import { accommodationApi } from '../api/accommodationApi.js'
import { dormitoryApi } from '../api/dormitoryApi.js'
import { repairApi } from '../api/repairApi.js'
import { visitApi } from '../api/visitApi.js'

const RecordList = defineComponent({
  name: 'RecordList',
  props: {
    title: { type: String, required: true },
    records: { type: Array, default: () => [] }
  },
  setup(props) {
    return () => h('section', { class: 'record-list' }, [
      h('div', { class: 'record-heading' }, [
        h('div', { class: 'record-heading-copy' }, [
          h('span', { class: 'record-kicker' }, 'Recent'),
          h('h3', props.title)
        ]),
        h('span', { class: 'record-count' }, `${props.records.length} 条`)
      ]),
      props.records.length
        ? h('div', { class: 'record-items' }, props.records.map((record, index) =>
            h('article', { class: `record-item ${record.tone || 'default'}`, key: `${record.title}-${index}` }, [
              h('span', { class: `record-marker ${record.tone || 'default'}` }),
              h('div', { class: 'record-copy' }, [
                h('strong', record.title),
                h('span', record.meta)
              ]),
              h('em', { class: `status ${record.tone || 'default'}` }, record.status)
            ])
          ))
        : h('div', { class: 'soft-empty' }, '暂无记录')
    ])
  }
})

const router = useRouter()

const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    return {}
  }
})

const isAdmin = computed(() => {
  const role = userInfo.value.role
  if (!role) return false
  const roleUpper = role.toUpperCase()
  return roleUpper === 'ADMIN' || roleUpper === 'ROLE_ADMIN'
})

const displayRole = computed(() => isAdmin.value ? '管理员' : '学生')
const displayName = computed(() => userInfo.value.name || userInfo.value.username || '同学')

const studentLoading = ref(false)
const myAccommodation = ref(null)
const allAccommodations = ref([])
const allDormitories = ref([])
const activeFeature = ref('change')
const selectedDormitoryNumber = ref('')
const selectedBed = ref('')
const repairDescription = ref('')
const repairImage = ref(null)
const visitForm = ref({ visitorName: '', visitTime: '', description: '' })
const changeApplications = ref([])
const repairApplications = ref([])
const visitApplications = ref([])
const changeSubmitting = ref(false)
const repairSubmitting = ref(false)
const visitSubmitting = ref(false)

const statsDisplay = computed(() => [
  { title: '宿舍楼总数', value: globalStats.value.buildingCount, icon: OfficeBuilding, tone: 'blue', note: '楼栋档案集中维护' },
  { title: '宿舍总数', value: globalStats.value.dormitoryCount, icon: House, tone: 'violet', note: '房间状态实时查看' },
  { title: '入住学生', value: globalStats.value.studentCount, icon: School, tone: 'green', note: '住宿关系清晰追踪' },
  { title: '报修记录', value: globalStats.value.repairCount, icon: Tools, tone: 'orange', note: '维修事项统一流转' }
])

const totalResources = computed(() => statsDisplay.value.reduce((sum, stat) => sum + Number(stat.value || 0), 0))

const overviewPercent = computed(() => {
  if (!totalResources.value) return 0
  const handled = Number(globalStats.value.studentCount || 0) + Number(globalStats.value.repairCount || 0)
  return Math.min(100, Math.round((handled / totalResources.value) * 100))
})

const primaryAction = computed(() => ({ label: '查看住宿信息', path: '/accommodation' }))
const secondaryAction = computed(() => ({ label: '处理报修记录', path: '/repair' }))

const adminQuickActions = computed(() => [
  { label: '宿舍楼信息', path: '/building', icon: OfficeBuilding },
  { label: '住宿信息', path: '/accommodation', icon: User },
  { label: '换宿信息', path: '/change', icon: Switch },
  { label: '来访登记', path: '/visit', icon: Tickets }
])

const studentFeatures = computed(() => [
  { key: 'change', title: '换宿舍', meta: `${availableDormitories.value.length} 间可换`, icon: Switch, size: 'large' },
  { key: 'repair', title: '申请报修', meta: `${repairApplications.value.length} 条记录`, icon: Tools, size: 'medium' },
  { key: 'visit', title: '来访登记', meta: `${visitApplications.value.length} 条记录`, icon: Tickets, size: 'small' }
])

const activeFeatureLabel = computed(() => {
  const map = {
    change: { zh: '换宿舍', en: 'Room Change' },
    repair: { zh: '申请报修', en: 'Repair' },
    visit: { zh: '来访登记', en: 'Visit' }
  }
  return map[activeFeature.value]
})

const currentDormitory = computed(() => {
  if (!myAccommodation.value) return null
  return allDormitories.value.find(d => d.number === myAccommodation.value.dormitory) || null
})

const availableDormitories = computed(() => {
  if (!myAccommodation.value) return []
  return allDormitories.value
    .filter(dorm => dorm.number !== myAccommodation.value.dormitory && getFreeCount(dorm) > 0)
    .sort((a, b) => String(a.building || '').localeCompare(String(b.building || ''), 'zh-CN') || String(a.number || '').localeCompare(String(b.number || ''), 'zh-CN'))
})

const selectedDormitory = computed(() => {
  return availableDormitories.value.find(dorm => dorm.number === selectedDormitoryNumber.value) || null
})

const selectedRoomBeds = computed(() => {
  if (!selectedDormitory.value) return []
  return buildBeds(selectedDormitory.value)
})

const changeRows = computed(() => changeApplications.value.map(item => ({
  title: `${item.oldDormitoryA || '-'} -> ${item.newDormitoryA || '-'}`,
  meta: `${item.oldBedA || '-'} / ${item.newBedA || '-'} · ${formatDate(item.changeTime)}`,
  status: getChangeStatusText(item.status),
  tone: getStatusTone(item.status)
})))

const repairRows = computed(() => repairApplications.value.map(item => ({
  title: item.dormitory || '报修申请',
  meta: `${item.description || '-'} · ${formatDate(item.repairTime)}`,
  status: getRepairStatusText(item.status),
  tone: getStatusTone(item.status)
})))

const visitRows = computed(() => visitApplications.value.map(item => ({
  title: item.visitorName || '来访申请',
  meta: `${item.dormitory || '-'} · ${formatDate(item.visitTime || item.createdAt)}`,
  status: getVisitStatusText(item.status),
  tone: getStatusTone(item.status)
})))

onMounted(() => {
  if (!isAdmin.value) {
    fetchStudentHomeData()
  }
})

function setActiveFeature(key) {
  activeFeature.value = key
}

async function fetchStudentHomeData() {
  studentLoading.value = true
  try {
    const studentId = userInfo.value.studentId
    if (!studentId) {
      myAccommodation.value = null
      return
    }

    const [myRooms, dormitories, accommodations] = await Promise.all([
      accommodationApi.searchAccommodationsByStudentId(studentId, ''),
      dormitoryApi.getAllDormitories(),
      accommodationApi.getAllAccommodations()
    ])

    myAccommodation.value = myRooms[0] || null
    allDormitories.value = dormitories || []
    allAccommodations.value = accommodations || []

    if (myAccommodation.value) {
      await fetchChangeApplications()
      await fetchRepairApplications()
      await fetchVisitApplications()
      selectedDormitoryNumber.value = availableDormitories.value[0]?.number || ''
      selectedBed.value = ''
      initDefaultVisitTime()
    }
  } catch (error) {
    console.error('学生首页数据加载失败:', error)
    ElMessage.error('学生首页数据加载失败')
  } finally {
    studentLoading.value = false
  }
}

async function fetchChangeApplications() {
  const studentName = userInfo.value.name || myAccommodation.value?.studentName
  if (!studentName) {
    changeApplications.value = []
    return
  }
  const applications = await accommodationApi.getChangeRecordsByStudent(studentName)
  changeApplications.value = sortByTime(applications, 'changeTime')
}

async function fetchRepairApplications() {
  try {
    repairApplications.value = sortByTime(await repairApi.getRepairsByStudentId(userInfo.value.studentId), 'repairTime')
  } catch (error) {
    console.error('报修记录加载失败:', error)
    repairApplications.value = []
  }
}

async function fetchVisitApplications() {
  try {
    visitApplications.value = sortByTime(await visitApi.getVisitsByStudentId(userInfo.value.studentId), 'visitTime')
  } catch (error) {
    console.error('来访记录加载失败:', error)
    visitApplications.value = []
  }
}

function sortByTime(list = [], field) {
  return [...(list || [])].sort((a, b) => new Date(b[field] || b.createdAt || 0) - new Date(a[field] || a.createdAt || 0))
}

function getFreeCount(dormitory) {
  return Math.max(0, Number(dormitory?.capacity || 0) - getOccupiedCount(dormitory))
}

function getOccupiedCount(dormitory) {
  const recordCount = allAccommodations.value.filter(item => item.dormitory === dormitory?.number).length
  return Math.max(recordCount, Number(dormitory?.occupied || 0))
}

function buildBeds(dormitory) {
  const capacity = Number(dormitory?.capacity || 0)
  const occupiedBeds = new Set(
    allAccommodations.value
      .filter(item => item.dormitory === dormitory?.number)
      .map(item => item.bed)
  )

  return Array.from({ length: capacity }, (_, index) => {
    const name = `${index + 1}号床`
    if (occupiedBeds.has(name)) {
      return { name, status: 'occupied', label: '已占用' }
    }
    return { name, status: 'free', label: '可选择' }
  })
}

async function submitChange() {
  if (!myAccommodation.value || !selectedDormitory.value || !selectedBed.value) {
    ElMessage.warning('请选择可换宿舍和空闲床位')
    return
  }

  changeSubmitting.value = true
  try {
    await accommodationApi.applyForChange({
      accommodationId: myAccommodation.value.id,
      newDormitory: selectedDormitory.value.number,
      newBed: selectedBed.value
    })
    ElMessage.success('换宿申请已提交')
    selectedBed.value = ''
    await fetchChangeApplications()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '换宿申请失败')
  } finally {
    changeSubmitting.value = false
  }
}

function handleRepairFileChange(event) {
  repairImage.value = event.target.files?.[0] || null
}

async function submitRepair() {
  if (!repairDescription.value.trim()) {
    ElMessage.warning('请填写报修说明')
    return
  }

  repairSubmitting.value = true
  try {
    const formData = new FormData()
    formData.append('studentName', myAccommodation.value.studentName)
    formData.append('studentId', userInfo.value.studentId)
    formData.append('dormitory', myAccommodation.value.dormitory)
    formData.append('description', repairDescription.value.trim())
    if (repairImage.value) {
      formData.append('image', repairImage.value)
    }
    await repairApi.applyForRepair(formData)
    ElMessage.success('报修申请已提交')
    repairDescription.value = ''
    repairImage.value = null
    await fetchRepairApplications()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '报修申请失败')
  } finally {
    repairSubmitting.value = false
  }
}

function initDefaultVisitTime() {
  const nextHour = new Date()
  nextHour.setHours(nextHour.getHours() + 1, 0, 0, 0)
  const pad = value => String(value).padStart(2, '0')
  visitForm.value.visitTime = `${nextHour.getFullYear()}-${pad(nextHour.getMonth() + 1)}-${pad(nextHour.getDate())}T${pad(nextHour.getHours())}:${pad(nextHour.getMinutes())}`
}

async function submitVisit() {
  if (!visitForm.value.visitorName || !visitForm.value.visitTime || !visitForm.value.description) {
    ElMessage.warning('请填写完整来访信息')
    return
  }

  visitSubmitting.value = true
  try {
    await visitApi.applyForVisit({
      visitorName: visitForm.value.visitorName,
      dormitory: myAccommodation.value.dormitory,
      description: visitForm.value.description,
      visitTime: new Date(visitForm.value.visitTime).toISOString(),
      studentId: userInfo.value.studentId,
      studentName: userInfo.value.name || myAccommodation.value.studentName || ''
    })
    ElMessage.success('来访登记已提交')
    visitForm.value.visitorName = ''
    visitForm.value.description = ''
    initDefaultVisitTime()
    await fetchVisitApplications()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '来访登记失败')
  } finally {
    visitSubmitting.value = false
  }
}

function getChangeStatusText(status) {
  const map = { PENDING: '待处理', APPROVED: '已同意', REJECTED: '已拒绝' }
  return map[status] || '未知'
}

function getRepairStatusText(status) {
  const map = { PENDING: '待处理', PROCESSING: '处理中', COMPLETED: '已完成' }
  return map[status] || '未知'
}

function getVisitStatusText(status) {
  const map = { PENDING: '待处理', APPROVED: '已同意', REJECTED: '已拒绝' }
  return map[status] || '未知'
}

function getStatusTone(status) {
  const map = { PENDING: 'pending', PROCESSING: 'pending', APPROVED: 'approved', COMPLETED: 'approved', REJECTED: 'rejected' }
  return map[status] || 'default'
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

function goToPrimary() {
  router.push(primaryAction.value.path)
}

function goToSecondary() {
  router.push(secondaryAction.value.path)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@600;700;800&family=Plus+Jakarta+Sans:wght@400;500&display=swap');

.home-page {
  min-width: 0;
  padding-bottom: 8px;
  color: #172033;
  font-family: 'Plus Jakarta Sans', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  animation: fadeIn 600ms ease both;
}

.student-bento-page {
  min-width: 0;
  min-height: 100%;
  padding: 16px;
  padding-bottom: 12px;
  color: #1d1d1f;
  background: #f5f5f7;
  border-radius: 24px;
  font-family: 'SF Pro Text', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
}

.overview-panel,
.stat-card,
.dashboard-card {
  border: 1px solid #dde7f6;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 36px rgba(41, 56, 86, 0.08);
  transition: background 300ms ease, border-color 300ms ease, box-shadow 300ms ease, transform 300ms ease;
}

.overview-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  min-height: 178px;
  padding: 28px 30px;
  overflow: hidden;
}

.overview-panel::after {
  content: '';
  position: absolute;
  right: -70px;
  top: -90px;
  width: 260px;
  height: 260px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(79, 124, 255, 0.2), transparent 68%);
  pointer-events: none;
}

.section-tag {
  color: #4f7cff;
  background: #eef4ff;
  border-color: #d7e4ff;
  border-radius: 999px;
}

.overview-copy {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.overview-copy h1 {
  margin: 14px 0 10px;
  font-family: 'Outfit', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: clamp(30px, 4vw, 44px);
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: 0;
}

.overview-copy p {
  max-width: 640px;
  margin: 0;
  color: #667085;
  font-size: 15px;
  line-height: 1.8;
  font-weight: 500;
}

.overview-actions {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.stats-row,
.dashboard-row {
  margin-top: 16px;
}

.stat-card {
  min-height: 158px;
}

.stat-card:hover,
.dashboard-card:hover,
.overview-panel:hover {
  border-color: rgba(79, 124, 255, 0.38);
  box-shadow: 0 22px 44px rgba(79, 124, 255, 0.13);
  transform: translateY(-2px);
}

.lift-1,
.lift-3 {
  margin-top: 10px;
}

.stat-content,
.card-header,
.service-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.stat-title,
.stat-note,
.summary-label,
.service-note {
  color: #667085;
}

.stat-title {
  font-size: 14px;
  font-weight: 600;
}

.stat-value,
.summary-number {
  font-family: 'Outfit', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  color: #172033;
}

.stat-value {
  margin-top: 8px;
  font-size: 40px;
  line-height: 1;
}

.stat-icon {
  width: 52px;
  height: 52px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 27px;
}

.stat-icon.blue { color: #2563eb; background: #eaf1ff; }
.stat-icon.violet { color: #6d5dfc; background: #f0edff; }
.stat-icon.green { color: #169b62; background: #e9f8f0; }
.stat-icon.orange { color: #d97706; background: #fff3df; }

.stat-note {
  margin-top: 20px;
  font-size: 13px;
}

.dashboard-card {
  min-height: 230px;
}

.dashboard-card :deep(.el-card__header) {
  border-bottom-color: #edf2f7;
}

.card-header {
  font-family: 'Outfit', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 18px;
  font-weight: 800;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.quick-action {
  min-height: 96px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #344054;
  text-decoration: none;
  background: #f8fbff;
  border: 1px solid #e3ebf7;
  border-radius: 16px;
  transition: color 300ms ease, background 300ms ease, border-color 300ms ease, box-shadow 300ms ease, transform 300ms ease;
}

.quick-action:hover {
  color: #255ee8;
  background: linear-gradient(135deg, #f7fbff, #eef4ff);
  border-color: rgba(79, 124, 255, 0.38);
  box-shadow: 0 14px 28px rgba(79, 124, 255, 0.12);
  transform: translateY(-2px);
}

.quick-action .el-icon {
  font-size: 28px;
}

.summary-number {
  font-size: 46px;
  line-height: 1;
}

.summary-label {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 600;
}

.service-note {
  margin: 22px 0 0;
  line-height: 1.8;
  font-weight: 500;
}

.bento-hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.eyebrow {
  display: inline-block;
  color: #0071e3;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.bento-hero h1 {
  margin: 8px 0 0;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
  font-size: clamp(32px, 4.8vw, 56px);
  line-height: 1.04;
  font-weight: 700;
}

.profile-chip {
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  color: #0071e3;
  background: #fff;
  border: 0;
  border-radius: 999px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  font: inherit;
  font-weight: 700;
}

.bento-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  grid-auto-rows: minmax(150px, auto);
  gap: 16px;
}

.bento-card {
  background: #fff;
  border: 0;
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: transform 180ms ease, box-shadow 180ms ease;
}

.bento-card:hover,
.profile-chip:hover {
  transform: translateY(-3px);
}

.bento-card:active,
.profile-chip:active,
.feature-card:active {
  transform: scale(0.985);
}

.feature-card {
  min-height: 166px;
  padding: 22px;
  color: #1d1d1f;
  cursor: pointer;
  text-align: left;
}

.feature-card.large {
  grid-column: span 2;
  grid-row: span 2;
}

.feature-card.medium,
.feature-card.small {
  grid-column: span 1;
}

.feature-card.active {
  outline: 2px solid #0071e3;
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
  color: #fff;
  background: #0071e3;
  border-radius: 16px;
  font-size: 24px;
}

.feature-title {
  display: block;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
  font-size: 28px;
  line-height: 1.08;
  font-weight: 700;
}

.feature-meta {
  display: block;
  margin-top: 10px;
  color: #6e6e73;
  font-size: 14px;
}

.summary-card,
.status-card {
  padding: 22px;
}

.summary-card h2 {
  margin: 12px 0 18px;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
  font-size: 34px;
  line-height: 1;
  font-weight: 700;
}

.summary-grid,
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.summary-grid div {
  min-width: 0;
}

.summary-grid strong,
.mini-stat strong {
  display: block;
  color: #1d1d1f;
  font-size: 18px;
  font-weight: 700;
}

.summary-grid span,
.mini-stat span {
  display: block;
  margin-top: 4px;
  color: #6e6e73;
  font-size: 12px;
}

.status-card {
  display: grid;
  gap: 12px;
}

.mini-stat {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.work-panel {
  margin-top: 16px;
  padding: 24px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.panel-header h2 {
  margin: 6px 0 0;
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif;
  font-size: 32px;
  font-weight: 700;
}

.feature-panel {
  display: grid;
  gap: 16px;
}

.form-block {
  display: grid;
  gap: 8px;
}

.form-block.wide {
  grid-column: 1 / -1;
}

.form-block label {
  color: #6e6e73;
  font-size: 13px;
  font-weight: 700;
}

.bento-input {
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  color: #1d1d1f;
  background: #f5f5f7;
  border: 1px solid transparent;
  border-radius: 14px;
  font: inherit;
  outline: none;
}

.bento-input:focus {
  border-color: #0071e3;
  background: #fff;
}

.bento-input:disabled {
  color: #6e6e73;
  cursor: not-allowed;
}

.textarea {
  min-height: 104px;
  resize: vertical;
}

.bed-picker {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(132px, 1fr));
  gap: 12px;
}

.bed-cell {
  min-height: 78px;
  padding: 12px;
  background: #f5f5f7;
  border: 1px solid transparent;
  border-radius: 18px;
  cursor: pointer;
  text-align: left;
}

.bed-cell strong,
.bed-cell span {
  display: block;
}

.bed-cell strong {
  font-size: 18px;
}

.bed-cell span {
  margin-top: 6px;
  color: #6e6e73;
  font-size: 12px;
}

.bed-cell.occupied {
  opacity: 0.45;
  cursor: not-allowed;
}

.bed-cell.free:hover,
.bed-cell.selected {
  border-color: #0071e3;
  background: #eff6ff;
}

.panel-submit {
  justify-self: start;
  min-width: 160px;
}

:deep(.record-list) {
  margin-top: 6px;
}

:deep(.record-heading) {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

:deep(.record-heading-copy) {
  min-width: 0;
}

:deep(.record-kicker) {
  display: block;
  margin-bottom: 3px;
  color: #0071e3;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

:deep(.record-list h3) {
  margin: 0;
  color: #1d1d1f;
  font-family: "SF Pro Display", "Inter", "Segoe UI", system-ui, sans-serif;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

:deep(.record-count) {
  flex: 0 0 auto;
  padding: 5px 11px;
  border-radius: 999px;
  background: #f5f5f7;
  color: #6e6e73;
  font-size: 12px;
  font-weight: 700;
}

:deep(.record-items) {
  display: grid;
  gap: 12px;
}

:deep(.record-item) {
  display: grid;
  grid-template-columns: 12px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfbfd 100%);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

:deep(.record-item:hover) {
  border-color: rgba(0, 113, 227, 0.14);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

:deep(.record-marker) {
  width: 10px;
  height: 34px;
  border-radius: 999px;
  background: #d2d2d7;
}

:deep(.record-marker.pending) {
  background: #ff9f0a;
}

:deep(.record-marker.approved) {
  background: #34c759;
}

:deep(.record-marker.rejected) {
  background: #ff3b30;
}

:deep(.record-copy) {
  min-width: 0;
}

:deep(.record-copy strong),
:deep(.record-copy span) {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.record-copy strong) {
  color: #1d1d1f;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.35;
}

:deep(.record-copy span) {
  margin-top: 4px;
  color: #6e6e73;
  font-size: 13px;
  line-height: 1.4;
}

:deep(.status) {
  justify-self: end;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
  white-space: nowrap;
}

:deep(.status.pending) { color: #b45309; background: #fff7ed; }
:deep(.status.approved) { color: #16845b; background: #ecfdf3; }
:deep(.status.rejected) { color: #d92d20; background: #fef3f2; }
:deep(.status.default) { color: #6e6e73; background: #f5f5f7; }

.soft-empty,
.empty-state {
  color: #6e6e73;
  text-align: center;
}

.soft-empty {
  padding: 22px;
  background: #f5f5f7;
  border-radius: 18px;
}

.empty-state {
  margin-top: 16px;
  padding: 36px;
}

.empty-state h2 {
  margin: 0 0 8px;
  color: #1d1d1f;
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

@media (max-width: 1180px) {
  .overview-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .overview-actions {
    justify-content: flex-start;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .lift-1,
  .lift-3 {
    margin-top: 0;
  }

  .bento-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .bento-hero,
  .panel-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .bento-grid,
  .summary-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .feature-card.large,
  .feature-card.medium,
  .feature-card.small {
    grid-column: span 1;
    grid-row: span 1;
  }

  :deep(.record-item) {
    grid-template-columns: 10px minmax(0, 1fr);
    align-items: flex-start;
  }

  :deep(.record-marker) {
    height: 42px;
  }

  :deep(.status) {
    grid-column: 2;
    justify-self: start;
  }

  :deep(.record-heading) {
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .overview-panel,
  .work-panel,
  .bento-card {
    padding: 20px;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }

  .service-summary {
    align-items: flex-start;
    flex-direction: column;
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
