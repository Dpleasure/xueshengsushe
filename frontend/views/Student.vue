<template>
  <div>
    <div class="content-header">
      <div class="search-bar">
        <input v-model="searchKeyword" type="text" class="search-input" placeholder="请输入关键字查询">
        <button class="btn btn-primary" @click="handleSearch">查询</button>
        <button class="btn btn-default" @click="handleReset">重置</button>
      </div>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="handleAdd">新增</button>
        <button class="btn btn-danger" @click="handleBatchDelete">批量删除</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table student-table">
        <thead>
          <tr>
            <th class="select-cell"><input type="checkbox" class="checkbox" :checked="isCurrentPageAllSelected" :disabled="pagedStudents.length === 0" @change="toggleSelectAll"></th>
            <th>序号</th>
            <th class="avatar-cell">头像</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>学号</th>
            <th>入住状态</th>
            <th>电话</th>
            <th>邮箱</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in pagedStudents" :key="item.id">
            <td class="select-cell"><input v-model="selectedIds" type="checkbox" class="checkbox" :value="item.id"></td>
            <td>{{ pageStartIndex + index + 1 }}</td>
            <td class="avatar-cell">
              <div class="student-avatar">
                <img
                  v-if="avatarVisible(item)"
                  :src="normalizeAvatar(item.avatar || item.avatarUrl)"
                  alt="学生头像"
                  @error="markAvatarFailed(item)"
                >
                <span v-else>{{ avatarFallback(item) }}</span>
              </div>
            </td>
            <td>{{ item.username || '-' }}</td>
            <td>{{ item.name || '-' }}</td>
            <td>{{ item.studentId || '-' }}</td>
            <td>
              <span class="badge" :class="getAccommodationStatus(item) ? 'badge-success' : 'badge-default'">
                {{ getAccommodationStatus(item) ? '已入住' : '未入住' }}
              </span>
            </td>
            <td>{{ item.phone || '-' }}</td>
            <td>{{ item.email || '-' }}</td>
            <td>
              <div class="table-actions">
                <button class="btn-link" @click="handleEdit(item)">编辑</button>
                <button class="btn-link danger" @click="handleDelete(item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-if="loading">
          <tr>
            <td colspan="10" class="empty-cell">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && filteredStudents.length === 0">
          <tr>
            <td colspan="10" class="empty-cell">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span>共 {{ filteredStudents.length }} 条，每页 {{ pageSize }} 条</span>
        <div class="pagination-pages">
          <button type="button" class="pagination-item" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">上一页</button>
          <button
            v-for="page in totalPages"
            :key="page"
            type="button"
            class="pagination-item"
            :class="{ active: page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
          <button type="button" class="pagination-item" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">下一页</button>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ modalType === 'add' ? '新增学生' : '编辑学生' }}</div>
          <span class="modal-close" @click="showModal = false">x</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input v-model="currentItem.username" type="text" class="input" placeholder="请输入用户名">
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <input v-model="currentItem.password" type="password" class="input" placeholder="请输入密码">
          </div>
          <div class="form-group">
            <label class="form-label">姓名</label>
            <input v-model="currentItem.name" type="text" class="input" placeholder="请输入姓名">
          </div>
          <div class="form-group">
            <label class="form-label">学号</label>
            <input v-model="currentItem.studentId" type="text" class="input" placeholder="请输入学号">
          </div>
          <div class="form-group">
            <label class="form-label">电话</label>
            <input v-model="currentItem.phone" type="text" class="input" placeholder="请输入电话">
          </div>
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input v-model="currentItem.email" type="email" class="input" placeholder="请输入邮箱">
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="handleSave">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { students, accommodations } from '../store/index.js'
import { studentApi } from '../api/studentApi.js'
import { avatarFallback as getAvatarFallback, avatarKey, normalizeAvatar } from '../utils/avatar.js'
import { usePagination } from '../utils/pagination.js'

const searchKeyword = ref('')
const showModal = ref(false)
const modalType = ref('add')
const currentItem = ref({})
const selectedIds = ref([])
const loading = ref(false)
const failedAvatars = ref(new Set())

const filteredStudents = computed(() => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return students.value
  return students.value.filter(s =>
    String(s.username || '').includes(keyword) ||
    String(s.name || '').includes(keyword) ||
    String(s.studentId || '').includes(keyword)
  )
})

const {
  currentPage,
  pageSize,
  totalPages,
  pageStartIndex,
  pagedItems: pagedStudents,
  goToPage,
  resetPage
} = usePagination(filteredStudents, 10)

const isCurrentPageAllSelected = computed(() => {
  return pagedStudents.value.length > 0 && pagedStudents.value.every(s => selectedIds.value.includes(s.id))
})

watch(searchKeyword, resetPage)

const handleSearch = () => resetPage()
const handleReset = () => {
  searchKeyword.value = ''
  resetPage()
}

const getAccommodationStatus = (student) => {
  const studentId = student?.studentId
  if (!studentId) return false
  return accommodations.value.some(a => a.studentId === studentId)
}

function avatarVisible(student) {
  const url = normalizeAvatar(student?.avatar || student?.avatarUrl)
  return Boolean(url && !failedAvatars.value.has(avatarKey(student)))
}

function markAvatarFailed(student) {
  failedAvatars.value = new Set([...failedAvatars.value, avatarKey(student)])
}

function avatarFallback(student) {
  return getAvatarFallback(student, '学')
}

const handleAdd = () => {
  modalType.value = 'add'
  currentItem.value = { username: '', password: '', name: '', studentId: '', phone: '', email: '', role: 'STUDENT' }
  showModal.value = true
}

const handleEdit = (item) => {
  modalType.value = 'edit'
  currentItem.value = { ...item }
  showModal.value = true
}

const handleDelete = async (id) => {
  if (confirm('确定要删除吗？')) {
    try {
      loading.value = true
      await studentApi.deleteStudent(id)
      alert('删除成功！')
      students.value = await studentApi.getAllStudents()
    } catch (error) {
      console.error('删除失败:', error)
    } finally {
      loading.value = false
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    alert('请选择要删除的项')
    return
  }
  if (confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`)) {
    try {
      loading.value = true
      await studentApi.batchDeleteStudents(selectedIds.value)
      alert('批量删除成功！')
      students.value = await studentApi.getAllStudents()
      selectedIds.value = []
    } catch (error) {
      console.error('批量删除失败:', error)
    } finally {
      loading.value = false
    }
  }
}

const handleSave = async () => {
  if (!currentItem.value.username || !currentItem.value.name || !currentItem.value.studentId) {
    alert('请填写用户名、姓名和学号')
    return
  }

  if (modalType.value === 'add' && !currentItem.value.password) {
    alert('新增学生时必须填写密码')
    return
  }

  try {
    loading.value = true
    if (modalType.value === 'add') {
      await studentApi.addStudent({
        avatar: '',
        role: 'STUDENT',
        ...currentItem.value
      })
      alert('添加成功！')
    } else {
      await studentApi.updateStudent(currentItem.value.id, currentItem.value)
      alert('修改成功！')
    }
    students.value = await studentApi.getAllStudents()
    showModal.value = false
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message
    if (msg) {
      alert(msg)
    }
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

const toggleSelectAll = (e) => {
  if (e.target.checked) {
    selectedIds.value = Array.from(new Set([...selectedIds.value, ...pagedStudents.value.map(s => s.id)]))
  } else {
    const currentPageIds = new Set(pagedStudents.value.map(s => s.id))
    selectedIds.value = selectedIds.value.filter(id => !currentPageIds.has(id))
  }
}
</script>

<style scoped>
.student-table {
  table-layout: fixed;
}

.select-cell {
  width: 48px;
}

.avatar-cell {
  width: 86px;
}

.student-avatar {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(135deg, #4f7cff, #7c5cff);
  border-radius: 999px;
  font-weight: 800;
  box-shadow: 0 8px 16px rgba(79, 124, 255, 0.18);
}

.student-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-cell {
  padding: 28px 20px;
  color: #667085;
  text-align: center;
}

.badge {
  display: inline-block;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.badge-success {
  color: #16845b;
  background-color: #ecfdf3;
  border: 1px solid #bbf7d0;
}

.badge-default {
  color: #667085;
  background-color: #f8fafc;
  border: 1px solid #e4e7ec;
}
</style>
