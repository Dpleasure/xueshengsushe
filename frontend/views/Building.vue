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
            <th><input type="checkbox" class="checkbox" :checked="isCurrentPageAllSelected" :disabled="pagedBuildings.length === 0" @change="toggleSelectAll"></th>
            <th>序号</th>
            <th>名称</th>
            <th>位置</th>
            <th>宿舍数</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in pagedBuildings" :key="item.id">
            <td><input type="checkbox" class="checkbox" :value="item.id" v-model="selectedIds"></td>
            <td>{{ pageStartIndex + index + 1 }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.location }}</td>
            <td>{{ item.capacity }}</td>
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
            <td colspan="6" style="text-align: center; padding: 20px;">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && filteredBuildings.length === 0">
          <tr>
            <td colspan="6" style="text-align: center; padding: 20px;">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span>共 {{ filteredBuildings.length }} 条，每页 {{ pageSize }} 条</span>
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

    <!-- 模态框 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ modalType === 'add' ? '新增宿舍楼' : '编辑宿舍楼' }}</div>
          <span class="modal-close" @click="showModal = false">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">名称</label>
            <input type="text" class="input" v-model="currentItem.name" placeholder="请输入名称（例如：A栋）">
            <div style="margin-top: 6px; font-size: 12px; color: #999;">格式要求：只能是 A-Z 的大写字母 + “栋”，例如：A栋</div>
          </div>
          <div class="form-group">
            <label class="form-label">位置</label>
            <select class="select" v-model="currentItem.location">
              <option value="">请选择位置</option>
              <option v-for="loc in locationOptions" :key="loc" :value="loc">{{ loc }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">宿舍数</label>
            <input type="number" class="input" v-model="currentItem.capacity" placeholder="请输入宿舍数">
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
import { buildings } from '../store/index.js'
import { buildingApi } from '../api/buildingApi.js'
import { usePagination } from '../utils/pagination.js'

const searchKeyword = ref('')
const showModal = ref(false)
const modalType = ref('add')
const currentItem = ref({})
const selectedIds = ref([])
const loading = ref(false)

const locationOptions = [
  '北区生活区',
  '南区生活区',
  '西区生活区',
  '东区生活区'
]

const buildingNamePattern = /^[A-Z]栋$/

// 页面加载时不需要重复获取数据，因为 layout 已经加载了

const filteredBuildings = computed(() => {
  if (!searchKeyword.value) return buildings.value
  return buildings.value.filter(b => 
    b.name.includes(searchKeyword.value) || 
    b.location.includes(searchKeyword.value)
  )
})

const {
  currentPage,
  pageSize,
  totalPages,
  pageStartIndex,
  pagedItems: pagedBuildings,
  goToPage,
  resetPage
} = usePagination(filteredBuildings, 10)

const isCurrentPageAllSelected = computed(() => {
  return pagedBuildings.value.length > 0 && pagedBuildings.value.every(b => selectedIds.value.includes(b.id))
})

watch(searchKeyword, resetPage)

const handleSearch = () => {
  // 搜索已通过计算属性实现
  resetPage()
}

const handleReset = () => {
  searchKeyword.value = ''
  resetPage()
}

const handleAdd = () => {
  modalType.value = 'add'
  currentItem.value = { name: '', location: '', capacity: '' }
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
      await buildingApi.deleteBuilding(id)
      alert('删除成功！')
      // 重新加载数据
      buildings.value = await buildingApi.getAllBuildings()
    } catch (error) {
      const msg = error?.response?.data?.message || error?.message
      if (msg) {
        alert(msg)
      }
      console.error('删除失败:', error)
    } finally {
      loading.value = false
    }
  }
}

const handleBatchDelete = () => {
  // 批量删除暂不实现，单个删除每个调用
  alert('批量删除功能请逐个删除')
}

const handleSave = async () => {
  if (!currentItem.value.name || !currentItem.value.location || !currentItem.value.capacity) {
    alert('请填写完整信息')
    return
  }

  const name = String(currentItem.value.name).trim()
  if (!buildingNamePattern.test(name)) {
    alert('名称格式不正确：只能是 A-Z 的大写字母 + “栋”，例如：A栋（a栋不允许）')
    return
  }
  currentItem.value.name = name

  try {
    loading.value = true
    if (modalType.value === 'add') {
      await buildingApi.addBuilding(currentItem.value)
      alert('添加成功！')
    } else {
      await buildingApi.updateBuilding(currentItem.value.id, currentItem.value)
      alert('修改成功！')
    }
    // 重新加载数据
    buildings.value = await buildingApi.getAllBuildings()
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
    selectedIds.value = Array.from(new Set([...selectedIds.value, ...pagedBuildings.value.map(b => b.id)]))
  } else {
    const currentPageIds = new Set(pagedBuildings.value.map(b => b.id))
    selectedIds.value = selectedIds.value.filter(id => !currentPageIds.has(id))
  }
}
</script>

