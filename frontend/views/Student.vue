<template>
  <div>
    <div class="content-header">
      <div class="search-bar">
        <input 
          type="text" 
          class="search-input" 
          placeholder="请输入关键字查询"
          v-model="searchKeyword"
        >
        <button class="btn btn-primary" @click="handleSearch">查询</button>
        <button class="btn btn-default" @click="handleReset">重置</button>
      </div>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="handleAdd">新增</button>
        <button class="btn btn-danger" @click="handleBatchDelete">批量删除</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table">
        <thead>
          <tr>
            <th><input type="checkbox" class="checkbox" @change="toggleSelectAll"></th>
            <th>序号</th>
            <th>头像</th>
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
          <tr v-for="(item, index) in filteredStudents" :key="item.id">
            <td><input type="checkbox" class="checkbox" :value="item.id" v-model="selectedIds"></td>
            <td>{{ index + 1 }}</td>
            <td>
              <div class="avatar">{{ item.avatar }}</div>
            </td>
            <td>{{ item.username }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.studentId }}</td>
            <td>
              <span class="badge" :class="getAccommodationStatus(item) ? 'badge-success' : 'badge-default'">
                {{ getAccommodationStatus(item) ? '已入住' : '未入住' }}
              </span>
            </td>
            <td>{{ item.phone }}</td>
            <td>{{ item.email }}</td>
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
            <td colspan="10" style="text-align: center; padding: 20px;">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && filteredStudents.length === 0">
          <tr>
            <td colspan="10" style="text-align: center; padding: 20px;">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span>共 {{ filteredStudents.length }} 条</span>
        <div class="pagination-item active">1</div>
      </div>
    </div>

    <!-- 模态框 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ modalType === 'add' ? '新增学生' : '编辑学生' }}</div>
          <span class="modal-close" @click="showModal = false">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input type="text" class="input" v-model="currentItem.username" placeholder="请输入用户名">
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <input type="password" class="input" v-model="currentItem.password" placeholder="请输入密码">
          </div>
          <div class="form-group">
            <label class="form-label">姓名</label>
            <input type="text" class="input" v-model="currentItem.name" placeholder="请输入姓名">
          </div>
          <div class="form-group">
            <label class="form-label">学号</label>
            <input type="text" class="input" v-model="currentItem.studentId" placeholder="请输入学号">
          </div>
          <div class="form-group">
            <label class="form-label">电话</label>
            <input type="text" class="input" v-model="currentItem.phone" placeholder="请输入电话">
          </div>
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input type="email" class="input" v-model="currentItem.email" placeholder="请输入邮箱">
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
import { ref, computed } from 'vue'
import { students, accommodations } from '../store/index.js'
import { studentApi } from '../api/studentApi.js'

const searchKeyword = ref('')
const showModal = ref(false)
const modalType = ref('add')
const currentItem = ref({})
const selectedIds = ref([])
const loading = ref(false)

// 页面加载时不需要重复获取数据，因为 layout 已经加载了
// 如果需要在页面刷新时获取最新数据，可以保留这段代码
// onMounted(async () => {
//   try {
//     loading.value = true
//     students.value = await studentApi.getAllStudents()
//   } catch (error) {
//     console.error('加载学生数据失败:', error)
//   } finally {
//     loading.value = false
//   }
// })

const filteredStudents = computed(() => {
  if (!searchKeyword.value) return students.value
  return students.value.filter(s => 
    s.username.includes(searchKeyword.value) || 
    s.name.includes(searchKeyword.value) ||
    (s.studentId && s.studentId.includes(searchKeyword.value))
  )
})

const handleSearch = () => {}
const handleReset = () => { searchKeyword.value = '' }

const getAccommodationStatus = (student) => {
  // 不能用姓名判断（会导致重名学生互相影响），改用学号判断
  const studentId = student?.studentId;
  if (!studentId) return false;
  return accommodations.value.some(a => a.studentId === studentId);
};

const handleAdd = () => {
  modalType.value = 'add'
  currentItem.value = { username: '', password: '', name: '', studentId: '', phone: '', email: '', role: 'STUDENT' }
  showModal.value = true
}

const handleEdit = (item) => {
  modalType.value = 'edit'
  currentItem.value = { ...item } // 编辑时显示原密码
  showModal.value = true
}

const handleDelete = async (id) => {
  if (confirm('确定要删除吗？')) {
    try {
      loading.value = true
      await studentApi.deleteStudent(id)
      alert('删除成功！')
      // 重新加载数据
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
      // 重新加载数据
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
  // 基本字段验证
  if (!currentItem.value.username || !currentItem.value.name || !currentItem.value.studentId) {
    alert('请填写用户名、姓名和学号')
    return
  }
  
  // 新增时密码必填
  if (modalType.value === 'add' && !currentItem.value.password) {
    alert('新增学生时必须填写密码')
    return
  }
  
  try {
    loading.value = true
    if (modalType.value === 'add') {
      await studentApi.addStudent({
        avatar: '👤',
        role: 'STUDENT',
        ...currentItem.value
      })
      alert('添加成功！')
    } else {
      await studentApi.updateStudent(currentItem.value.id, currentItem.value)
      alert('修改成功！')
    }
    // 重新加载数据
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
    selectedIds.value = filteredStudents.value.map(s => s.id)
  } else {
    selectedIds.value = []
  }
}
</script>

<style scoped>
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.badge-default {
  background-color: #fafafa;
  color: #999;
  border: 1px solid #d9d9d9;
}
</style>

