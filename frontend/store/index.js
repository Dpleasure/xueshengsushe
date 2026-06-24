// 全局数据存储 - 使用 Vue 3 Composition API
import { ref, computed } from 'vue'

// 引入 API 服务
import { studentApi } from '../api/studentApi.js'
import { buildingApi } from '../api/buildingApi.js'
import { dormitoryApi } from '../api/dormitoryApi.js'
import { accommodationApi } from '../api/accommodationApi.js'
import { repairApi } from '../api/repairApi.js'
import { roomChangeApi } from '../api/roomChangeApi.js'
import { visitApi } from '../api/visitApi.js'
import { adminApi } from '../api/adminApi.js'

// 初始化空数组 - 数据将从 API 获取
export const buildings = ref([])
export const dormitories = ref([])
export const students = ref([])
export const accommodations = ref([])
export const changes = ref([])
export const repairs = ref([])
export const visits = ref([])
export const admins = ref([])

// 加载状态
export const loadingStates = ref({
  buildings: false,
  dormitories: false,
  students: false,
  accommodations: false,
  changes: false,
  repairs: false,
  visits: false,
  admins: false
})

// 计算属性 - 统计数据
export const stats = computed(() => {
  // 计算入住学生数（STUDENT 角色的数量）
  const studentCount = students.value.filter(s => s.role === 'STUDENT').length
  
  return {
    buildingCount: buildings.value.length,
    dormitoryCount: dormitories.value.length,
    studentCount: studentCount,
    repairCount: repairs.value.length
  }
})

// 数据加载函数
export const loadData = async () => {
  try {
    loadingStates.value.buildings = true
    buildings.value = await buildingApi.getAllBuildings()
  } catch (error) {
    console.error('加载宿舍楼数据失败:', error)
  } finally {
    loadingStates.value.buildings = false
  }
  
  try {
    loadingStates.value.students = true
    students.value = await studentApi.getAllStudents()
  } catch (error) {
    console.error('加载学生数据失败:', error)
  } finally {
    loadingStates.value.students = false
  }
  
  try {
    loadingStates.value.dormitories = true
    dormitories.value = await dormitoryApi.getAllDormitories()
  } catch (error) {
    console.error('加载宿舍数据失败:', error)
  } finally {
    loadingStates.value.dormitories = false
  }
  
  try {
    loadingStates.value.accommodations = true
    accommodations.value = await accommodationApi.getAllAccommodations()
  } catch (error) {
    console.error('加载住宿信息失败:', error)
  } finally {
    loadingStates.value.accommodations = false
  }
  
  try {
    loadingStates.value.repairs = true
    repairs.value = await repairApi.getAllRepairs()
  } catch (error) {
    console.error('加载报修记录失败:', error)
  } finally {
    loadingStates.value.repairs = false
  }
  
  try {
    loadingStates.value.changes = true
    changes.value = await roomChangeApi.getAllRoomChanges()
  } catch (error) {
    console.error('加载换寝记录失败:', error)
  } finally {
    loadingStates.value.changes = false
  }
  
  try {
    loadingStates.value.visits = true
    visits.value = await visitApi.getAllVisits()
  } catch (error) {
    console.error('加载来访记录失败:', error)
  } finally {
    loadingStates.value.visits = false
  }
  
  try {
    loadingStates.value.admins = true
    admins.value = await adminApi.getAllAdmins()
  } catch (error) {
    console.error('加载管理员数据失败:', error)
  } finally {
    loadingStates.value.admins = false
  }
}

// 工具函数 - 保持兼容性的添加函数（现在主要用于前端临时数据）
export const addItem = (list, item) => {
  list.value.push({
    id: Date.now(),
    ...item
  })
}

// 工具函数 - 保持兼容性的更新函数
export const updateItem = (list, item) => {
  const index = list.value.findIndex(i => i.id === item.id)
  if (index !== -1) {
    list.value.splice(index, 1, { ...item })
  }
}

// 工具函数 - 保持兼容性的删除函数
export const deleteItem = (list, id) => {
  const index = list.value.findIndex(i => i.id === id)
  if (index !== -1) {
    list.value.splice(index, 1)
  }
}

// 工具函数 - 保持兼容性的批量删除函数
export const batchDeleteItems = (list, ids) => {
  for (let i = list.value.length - 1; i >= 0; i--) {
    if (ids.includes(list.value[i].id)) {
      list.value.splice(i, 1)
    }
  }
}
