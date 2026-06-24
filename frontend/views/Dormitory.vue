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
            <th>寝室号</th>
            <th>宿舍楼</th>
            <th>容纳数</th>
            <th>已住人数</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in filteredDormitories" :key="item.id">
            <td><input type="checkbox" class="checkbox" :value="item.id" v-model="selectedIds"></td>
            <td>{{ index + 1 }}</td>
            <td>{{ item.number }}</td>
            <td>{{ item.building }}</td>
            <td>{{ item.capacity }}</td>
            <td>{{ item.occupied }}</td>
            <td>
              <div class="table-actions">
                <button class="btn-link" @click="handleEdit(item)">编辑</button>
                <button class="btn-link danger" @click="handleDelete(item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span>共 {{ filteredDormitories.length }} 条</span>
        <div class="pagination-item active">1</div>
      </div>
    </div>

    <!-- 模态框 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ modalType === 'add' ? '新增宿舍' : '编辑宿舍' }}</div>
          <span class="modal-close" @click="showModal = false">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">寝室号</label>
            <input type="text" class="input" v-model="currentItem.number" placeholder="请输入寝室号（格式如：A-101）" :disabled="modalType === 'edit'">
          </div>
          <div class="form-group">
            <label class="form-label">宿舍楼</label>
            <select class="select" v-model="currentItem.building" :disabled="modalType === 'edit'">
              <option value="">请选择</option>
  <option v-for="b in buildings" :key="b.id" :value="b.name">
    {{ b.name }}
  </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">容纳数</label>
            <input type="number" class="input" v-model="currentItem.capacity" placeholder="请输入容纳数">
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
import { dormitories, buildings, addItem, updateItem, deleteItem, batchDeleteItems } from '../store/index.js'
import { dormitoryApi } from '../api/dormitoryApi.js'

const searchKeyword = ref('')
const showModal = ref(false)
const modalType = ref('add')
const currentItem = ref({})
const selectedIds = ref([])

const filteredDormitories = computed(() => {
  if (!searchKeyword.value) return dormitories.value
  return dormitories.value.filter(d => 
    d.number.includes(searchKeyword.value) || 
    d.building.includes(searchKeyword.value)
  )
})

const handleSearch = () => {}
const handleReset = () => { searchKeyword.value = '' }

const handleAdd = () => {
  modalType.value = 'add'
  currentItem.value = { number: '', building: '', capacity: '', occupied: 0 }
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
      await dormitoryApi.deleteDormitory(id)
      alert('删除成功！')
      dormitories.value = await dormitoryApi.getAllDormitories()
    } catch (error) {
      const msg = error?.response?.data?.message || error?.message
      alert(msg || '删除失败')
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
      await dormitoryApi.batchDeleteDormitories(selectedIds.value)
      alert('批量删除成功！')
      dormitories.value = await dormitoryApi.getAllDormitories()
      selectedIds.value = []
    } catch (error) {
      const msg = error?.response?.data?.message || error?.message
      alert(msg || '批量删除失败')
    }
  }
}

const handleSave = () => {
  if (!currentItem.value.number || !currentItem.value.building || !currentItem.value.capacity) {
    alert('请填写完整信息')
    return
  }

  // 新增时校验寝室号格式： (A-Z)-xxx，且前缀字母必须与宿舍楼字母一致（例如：A栋 -> A-101）
  if (modalType.value === 'add') {
    const dormNumber = String(currentItem.value.number).trim()
    const buildingName = String(currentItem.value.building).trim()

    const numberPattern = /^[A-Z]-\d{3}$/
    if (!numberPattern.test(dormNumber)) {
      alert('寝室号格式不正确，必须为 (A-Z)-xxx，例如：A-101')
      return
    }

    // 取宿舍楼的首字母（要求楼栋格式形如 A栋）
    const buildingLetter = buildingName.charAt(0)
    const dormLetter = dormNumber.charAt(0)

    if (!/^[A-Z]$/.test(buildingLetter)) {
      alert('宿舍楼名称格式不正确，无法校验寝室号，请先选择正确的宿舍楼（例如：A栋）')
      return
    }

    if (dormLetter !== buildingLetter) {
      alert(`寝室号前缀字母必须与宿舍楼对应：${buildingLetter}栋 只能添加 ${buildingLetter}-xxx`) 
      return
    }

    currentItem.value.number = dormNumber
    currentItem.value.building = buildingName
  }
  
  if (modalType.value === 'add') {
    dormitoryApi.addDormitory({
      number: currentItem.value.number,
      building: currentItem.value.building,
      capacity: Number(currentItem.value.capacity),
      occupied: 0
    })
    .then(async () => {
      alert('添加成功！')
      dormitories.value = await dormitoryApi.getAllDormitories()
      showModal.value = false
    })
    .catch((error) => {
      const msg = error?.response?.data?.message || error?.message
      alert(msg || '添加失败')
    })
    return
  } else {
    updateItem(dormitories, currentItem.value)
    alert('修改成功！')
    showModal.value = false
  }
}

const toggleSelectAll = (e) => {
  if (e.target.checked) {
    selectedIds.value = filteredDormitories.value.map(d => d.id)
  } else {
    selectedIds.value = []
  }
}
</script>

