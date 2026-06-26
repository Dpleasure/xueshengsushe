<template>
  <div>
    <div class="content-header">
      <div class="search-bar">
        <input 
          type="text" 
          class="search-input" 
          placeholder="请输入学生姓名"
          v-model="searchName"
        >
        <input 
          type="text" 
          class="search-input" 
          placeholder="请输入寝室名称"
          v-model="searchDormitory"
        >
        <button class="btn btn-primary" @click="handleSearch">查询</button>
        <button class="btn btn-default" @click="handleReset">重置</button>
      </div>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="handleAdd">新增</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table">
        <thead>
          <tr>
            <th><input type="checkbox" class="checkbox" @change="toggleSelectAll"></th>
            <th>序号</th>
            <th>学生姓名</th>
            <th>学号</th>
            <th>寝室号</th>
            <th>床位号</th>
            <th>宿舍楼</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in filteredAccommodations" :key="item.id">
            <td><input type="checkbox" class="checkbox" :value="item.id" v-model="selectedIds"></td>
            <td>{{ index + 1 }}</td>
            <td>{{ item.studentName }}</td>
            <td>{{ item.studentId }}</td>
            <td>{{ item.dormitory }}</td>
            <td>{{ item.bed }}</td>
            <td>{{ item.building }}</td>
            <td>
              <div class="table-actions">
                <button class="btn-link" @click="handleChangeRoom(item)">更换寝室 / 床位</button>
                <button class="btn-link danger" @click="handleDelete(item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-if="loading">
          <tr>
            <td colspan="7" style="text-align: center; padding: 20px;">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && filteredAccommodations.length === 0">
          <tr>
            <td colspan="7" style="text-align: center; padding: 20px;">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span>共 {{ filteredAccommodations.length }} 条</span>
        <div class="pagination-item active">1</div>
      </div>
    </div>

    <!-- 换寝对话框 -->
    <div v-if="showChangeDialog" class="modal-overlay" @click="closeChangeDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>更换寝室/床位</h3>
          <button class="close-btn" @click="closeChangeDialog">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>学生姓名</label>
            <input type="text" class="form-input" :value="currentStudent?.studentName" disabled>
          </div>
          <div class="form-group">
            <label>当前寝室</label>
            <input type="text" class="form-input" :value="currentStudent?.dormitory" disabled>
          </div>
          <div class="form-group">
            <label>当前床位</label>
            <input type="text" class="form-input" :value="currentStudent?.bed" disabled>
          </div>
          <div class="form-group">
            <label>新寝室号 <span style="color: red;">*</span></label>
            <input 
              type="text" 
              class="form-input" 
              v-model="newDormitory"
              placeholder="请输入新寝室号，例如：A-102"
            >
          </div>
          <div class="form-group">
            <label>新床位号 <span style="color: red;">*</span></label>
            <input 
              type="text" 
              class="form-input" 
              v-model="newBed"
              placeholder="请输入新床位号，例如：1号床"
            >
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="closeChangeDialog">取消</button>
          <button class="btn btn-primary" @click="confirmChangeRoom" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认更换' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 新增住宿信息对话框 -->
    <div v-if="showAddDialog" class="modal-overlay" @click="closeAddDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>新增住宿信息</h3>
          <button class="close-btn" @click="closeAddDialog">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>学生姓名 <span style="color: red;">*</span></label>
            <select class="form-input" v-model="newAccommodation.studentName" @change="onStudentNameChange">
              <option value="">请选择未入住的学生</option>
              <option v-for="student in unassignedStudents" :key="student.id" :value="student.name">
                {{ student.name }} ({{ student.username }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>学号 <span style="color: red;">*</span></label>
            <select class="form-input" v-model="newAccommodation.studentId" :disabled="!newAccommodation.studentName">
              <option value="">请先选择学生学号</option>
              <option v-for="sid in availableStudentIds" :key="sid" :value="sid">
                {{ sid }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>宿舍楼 <span style="color: red;">*</span></label>
            <select 
              class="form-input" 
              v-model="newAccommodation.building"
              @change="onBuildingChange"
            >
              <option value="">请选择宿舍楼</option>
              <option v-for="building in uniqueBuildings" :key="building" :value="building">
                {{ building }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>寝室号 <span style="color: red;">*</span></label>
            <select 
              class="form-input" 
              v-model="newAccommodation.dormitory"
              :disabled="!newAccommodation.building"
            >
              <option value="">请先选择宿舍楼</option>
              <option 
                v-for="dorm in filteredDormitories" 
                :key="dorm.id" 
                :value="dorm.number"
                :disabled="dorm.occupied >= dorm.capacity"
              >
                {{ dorm.number }} (已住: {{ dorm.occupied }}/{{ dorm.capacity }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>床位号 <span style="color: red;">*</span></label>
            <select 
              class="form-input" 
              v-model="newAccommodation.bed"
              :disabled="!newAccommodation.dormitory"
            >
              <option value="">请选择床位</option>
              <option 
                v-for="bed in availableBeds" 
                :key="bed" 
                :value="bed"
              >
                {{ bed }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>入住日期</label>
            <input 
              type="date" 
              class="form-input" 
              v-model="newAccommodation.checkInDate"
            >
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="closeAddDialog">取消</button>
          <button class="btn btn-primary" @click="confirmAdd" :disabled="submittingAdd">
            {{ submittingAdd ? '提交中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { accommodations, dormitories, students, loadData } from '../store/index.js'
import { accommodationApi } from '../api/accommodationApi.js'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { withApiBaseUrl } from '../api/baseUrl.js'

const searchName = ref('')
const searchDormitory = ref('')
const selectedIds = ref([])
const loading = ref(false)

// 换寝对话框相关
const showChangeDialog = ref(false)
const currentStudent = ref(null)
const newDormitory = ref('')
const newBed = ref('')
const submitting = ref(false)

// 新增对话框相关
const showAddDialog = ref(false)
const newAccommodation = ref({
  studentName: '',
  studentId: '',
  building: '',
  dormitory: '',
  bed: '',
  checkInDate: ''
})
const submittingAdd = ref(false)

const filteredAccommodations = computed(() => {
  return accommodations.value.filter(a => {
    const matchName = !searchName.value || a.studentName.includes(searchName.value)
    const matchDormitory = !searchDormitory.value || a.dormitory.includes(searchDormitory.value)
    return matchName && matchDormitory
  })
})

// 获取唯一的宿舍楼列表
const uniqueBuildings = computed(() => {
  const buildings = new Set()
  dormitories.value.forEach(dorm => {
    if (dorm.building) {
      buildings.add(dorm.building)
    }
  })
  return Array.from(buildings).sort()
})

// 未分配宿舍的学生列表
const unassignedStudents = computed(() => {
  // 不能用姓名判断是否已入住（会导致重名学生被错误过滤掉）
  // 改为用学号判断是否已入住
  const assignedStudentIds = new Set(accommodations.value.map(a => a.studentId).filter(Boolean));
  return students.value.filter(s => !assignedStudentIds.has(s.studentId) && s.role === 'STUDENT');
});

// 根据选择的宿舍楼过滤寝室列表
const filteredDormitories = computed(() => {
  if (!newAccommodation.value.building) {
    return []
  }
  return dormitories.value.filter(dorm => 
    dorm.building === newAccommodation.value.building
  )
})

// 当选择学生姓名后，只显示该姓名对应的学号选项（解决重名问题）
const availableStudentIds = computed(() => {
  const name = newAccommodation.value.studentName
  if (!name) return []

  // 只在“未入住学生”里找，避免已入住学生还能选
  return unassignedStudents.value
    .filter(s => s.name === name)
    .map(s => s.studentId || s.username)
    .filter(Boolean)
})

// 根据选择的寝室获取可用床位列表
const availableBeds = computed(() => {
  if (!newAccommodation.value.dormitory) return []

  const dorm = dormitories.value.find(d => d.number === newAccommodation.value.dormitory)
  if (!dorm) return []

  const occupiedBeds = accommodations.value
    .filter(a => a.dormitory === newAccommodation.value.dormitory)
    .map(a => a.bed)

  const allBeds = Array.from({ length: dorm.capacity || 0 }, (_, i) => `${i + 1}号床`)

  return allBeds.filter(bed => !occupiedBeds.includes(bed))
})

const handleSearch = () => {}

const handleReset = () => {
  searchName.value = ''
  searchDormitory.value = ''
}

const handleChangeRoom = (item) => {
  currentStudent.value = item
  newDormitory.value = ''
  newBed.value = ''
  showChangeDialog.value = true
}

const closeChangeDialog = () => {
  showChangeDialog.value = false
  currentStudent.value = null
  newDormitory.value = ''
  newBed.value = ''
}

const confirmChangeRoom = async () => {
  if (!newDormitory.value.trim()) {
    alert('请输入新寝室号')
    return
  }
  if (!newBed.value.trim()) {
    alert('请输入新床位号')
    return
  }

  try {
    submitting.value = true
    const token = localStorage.getItem('token')
    const response = await axios.post(
      withApiBaseUrl('/room-changes/change-room'),
      {
        accommodationId: currentStudent.value.id,
        newDormitory: newDormitory.value.trim(),
        newBed: newBed.value.trim()
      },
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )

    if (response.data.success) {
      alert('换寝成功！')
      closeChangeDialog()
      accommodations.value = await accommodationApi.getAllAccommodations()
    } else {
      alert('换寝失败：' + (response.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('换寝失败:', error)
    alert('换寝失败：' + (error.response?.data?.message || error.message || '网络错误'))
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  if (confirm('确定要删除吗？')) {
    try {
      loading.value = true
      await accommodationApi.deleteAccommodation(id)
      alert('删除成功！')
      accommodations.value = await accommodationApi.getAllAccommodations()
    } catch (error) {
      console.error('删除失败:', error)
    } finally {
      loading.value = false
    }
  }
}

// 新增住宿信息相关函数
const handleAdd = () => {
  newAccommodation.value = {
    studentName: '',
    studentId: '',
    building: '',
    dormitory: '',
    bed: '',
    checkInDate: ''
  }
  showAddDialog.value = true
}

const closeAddDialog = () => {
  showAddDialog.value = false
  newAccommodation.value = {
    studentName: '',
    studentId: '',
    building: '',
    dormitory: '',
    bed: '',
    checkInDate: ''
  }
}

const onBuildingChange = () => {
  newAccommodation.value.dormitory = ''
  newAccommodation.value.bed = ''
}

const onStudentNameChange = () => {
  // 选择姓名后，重置学号，避免重名时保留了上一次选择
  newAccommodation.value.studentId = ''
}

const confirmAdd = async () => {
  if (!newAccommodation.value.studentName?.trim()) {
    ElMessage.warning('请选择学生姓名')
    return
  }
  if (!newAccommodation.value.studentId?.trim()) {
    ElMessage.warning('请选择学号')
    return
  }
  if (!newAccommodation.value.building) {
    ElMessage.warning('请选择宿舍楼')
    return
  }
  if (!newAccommodation.value.dormitory) {
    ElMessage.warning('请选择寝室号')
    return
  }
  if (!newAccommodation.value.bed?.trim()) {
    ElMessage.warning('请选择床位号')
    return
  }

  const selectedDorm = dormitories.value.find(d => d.number === newAccommodation.value.dormitory)
  if (selectedDorm && selectedDorm.occupied >= selectedDorm.capacity) {
    ElMessage.error('该寝室已满，无法添加新成员')
    return
  }

  const isBedOccupied = accommodations.value.some(acc => 
    acc.dormitory === newAccommodation.value.dormitory && 
    acc.bed === newAccommodation.value.bed
  )
  if (isBedOccupied) {
    ElMessage.error('该床位已被占用，请选择其他床位')
    return
  }

  try {
    submittingAdd.value = true
    const accommodationData = {
      studentName: newAccommodation.value.studentName.trim(),
      studentId: newAccommodation.value.studentId?.trim() || null,
      building: newAccommodation.value.building,
      dormitory: newAccommodation.value.dormitory,
      bed: newAccommodation.value.bed.trim(),
      checkInDate: newAccommodation.value.checkInDate || null
    }

    await accommodationApi.addAccommodation(accommodationData)
    ElMessage.success('添加成功！')
    closeAddDialog()
    await loadData()
  } catch (error) {
    console.error('添加失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '添加失败'
    ElMessage.error(errorMessage)
  } finally {
    submittingAdd.value = false
  }
}

const toggleSelectAll = (e) => {
  if (e.target.checked) {
    selectedIds.value = filteredAccommodations.value.map(a => a.id)
  } else {
    selectedIds.value = []
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.form-input:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

select.form-input {
  cursor: pointer;
}

select.form-input option:disabled {
  color: #999;
  background-color: #f5f5f5;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
