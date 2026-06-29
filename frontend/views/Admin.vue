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
        <button class="btn btn-primary" @click="handleAdd">新增用户</button>
        <button class="btn btn-danger" @click="handleBatchDelete">批量删除</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table admin-table">
        <thead>
          <tr>
            <th class="select-cell">
              <input
                type="checkbox"
                class="checkbox"
                :checked="isCurrentPageAllSelected"
                :disabled="pagedUsers.length === 0"
                @change="toggleSelectAll"
              >
            </th>
            <th>序号</th>
            <th class="avatar-cell">头像</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>工号</th>
            <th>角色</th>
            <th>电话</th>
            <th>邮箱</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in pagedUsers" :key="item.id">
            <td class="select-cell"><input type="checkbox" class="checkbox" :value="item.id" v-model="selectedIds"></td>
            <td>{{ pageStartIndex + index + 1 }}</td>
            <td class="avatar-cell">
              <div class="admin-avatar">
                <img
                  v-if="avatarVisible(item)"
                  :src="normalizeAvatar(item.avatar || item.avatarUrl)"
                  alt="管理员头像"
                  @error="markAvatarFailed(item)"
                >
                <span v-else>{{ avatarFallback(item) }}</span>
              </div>
            </td>
            <td>{{ item.username || '-' }}</td>
            <td>{{ item.name || '-' }}</td>
            <td>{{ item.studentId || '-' }}</td>
            <td>
              <span class="badge" :class="item.role === 'ADMIN' ? 'badge-danger' : 'badge-success'">{{ item.role === 'ADMIN' ? '管理员' : '学生' }}</span>
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
          <tr v-if="pagedUsers.length === 0">
            <td colspan="10" class="empty-cell">暂无管理员数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <span>
          共 {{ filteredUsers.length }} 条，每页 {{ pageSize }} 条
        </span>
        <div class="pagination-pages">
          <button
            type="button"
            class="pagination-item"
            :disabled="currentPage === 1"
            @click="goToPage(currentPage - 1)"
          >
            上一页
          </button>
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
          <button
            type="button"
            class="pagination-item"
            :disabled="currentPage === totalPages"
            @click="goToPage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 模态框 -->
    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <div class="modal-title">{{ modalType === 'add' ? '新增用户' : '编辑用户' }}</div>
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
            <label class="form-label">学号/工号</label>
            <input type="text" class="input" v-model="currentItem.studentId" placeholder="请输入工号">
          </div>
          <div class="form-group">
            <label class="form-label">角色</label>
            <input type="text" class="input" value="管理员" disabled style="background-color: #f5f5f5;">
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
import { ref, computed, watch } from 'vue'
import { admins, loadData } from '../store/index.js'
import { adminApi } from '../api/adminApi.js'
import { ElMessage } from 'element-plus'
import { avatarFallback as getAvatarFallback, avatarKey, normalizeAvatar } from '../utils/avatar.js'

const searchKeyword = ref('')
const showModal = ref(false)
const modalType = ref('add')
const currentItem = ref({})
const selectedIds = ref([])
const failedAvatars = ref(new Set())
const currentPage = ref(1)
const pageSize = 10

// 只显示管理员数据
const filteredUsers = computed(() => {
  let users = admins.value;
  if (searchKeyword.value) {
    users = users.filter(user => 
      String(user.username || '').includes(searchKeyword.value) ||
      String(user.name || '').includes(searchKeyword.value) ||
      String(user.studentId || '').includes(searchKeyword.value)
    );
  }
  return users;
});

const totalPages = computed(() => Math.max(1, Math.ceil(filteredUsers.value.length / pageSize)))
const pageStartIndex = computed(() => (currentPage.value - 1) * pageSize)
const pagedUsers = computed(() => {
  return filteredUsers.value.slice(pageStartIndex.value, pageStartIndex.value + pageSize)
})
const isCurrentPageAllSelected = computed(() => {
  return pagedUsers.value.length > 0 && pagedUsers.value.every(user => selectedIds.value.includes(user.id))
})

watch(searchKeyword, () => {
  currentPage.value = 1
})

watch(totalPages, (pages) => {
  if (currentPage.value > pages) {
    currentPage.value = pages
  }
})

const goToPage = (page) => {
  currentPage.value = Math.min(Math.max(page, 1), totalPages.value)
}

const handleSearch = () => {
  goToPage(1)
};
const handleReset = () => {
  searchKeyword.value = ''
  goToPage(1)
};

function avatarVisible(user) {
  const url = normalizeAvatar(user?.avatar || user?.avatarUrl)
  return Boolean(url && !failedAvatars.value.has(avatarKey(user)))
}

function markAvatarFailed(user) {
  failedAvatars.value = new Set([...failedAvatars.value, avatarKey(user)])
}

function avatarFallback(user) {
  return getAvatarFallback(user, '管')
}

const handleAdd = () => {
  modalType.value = 'add';
  currentItem.value = { username: '', password: '', name: '', studentId: '', role: 'ADMIN', phone: '', email: '' };
  showModal.value = true;
};

const handleEdit = (item) => {
  modalType.value = 'edit';
  currentItem.value = { ...item }; // 编辑时显示原密码
  showModal.value = true;
};

const handleDelete = async (id) => {
  if (confirm('确定要删除吗？')) {
    try {
      await adminApi.deleteAdmin(id);
      await loadData(); // 重新加载所有数据
      ElMessage.success('删除成功！');
    } catch (error) {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message));
    }
  }
};

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的项');
    return;
  }
  if (confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`)) {
    try {
      for (const id of selectedIds.value) {
        await adminApi.deleteAdmin(id);
      }
      await loadData();
      selectedIds.value = [];
      ElMessage.success('批量删除成功！');
    } catch (error) {
      ElMessage.error('批量删除失败: ' + (error.response?.data?.message || error.message));
    }
  }
};

const handleSave = async () => {
  if (!currentItem.value.username || !currentItem.value.name || !currentItem.value.studentId) {
    ElMessage.warning('请填写用户名、姓名和学号/工号');
    return;
  }
  if (modalType.value === 'add' && !currentItem.value.password) {
    ElMessage.warning('新增用户时必须填写密码');
    return;
  }

  try {
    if (modalType.value === 'add') {
      // 使用注册接口添加管理员，确保角色为 ADMIN
      const userData = { ...currentItem.value, role: 'ADMIN' };
      await adminApi.registerUser(userData);
      ElMessage.success('添加成功！');
    } else {
      // 更新管理员信息，确保角色为 ADMIN
      const userData = { ...currentItem.value, role: 'ADMIN' };
      await adminApi.updateAdmin(currentItem.value.id, userData);
      ElMessage.success('修改成功！');
    }
    await loadData();
    showModal.value = false;
  } catch (error) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message));
  }
};

const toggleSelectAll = (e) => {
  if (e.target.checked) {
    selectedIds.value = Array.from(new Set([...selectedIds.value, ...pagedUsers.value.map(u => u.id)]));
  } else {
    const currentPageIds = new Set(pagedUsers.value.map(u => u.id));
    selectedIds.value = selectedIds.value.filter(id => !currentPageIds.has(id));
  }
};
</script>

<style scoped>
.admin-table {
  table-layout: fixed;
}

.select-cell {
  width: 48px;
}

.avatar-cell {
  width: 86px;
}

.admin-avatar {
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

.admin-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-cell {
  padding: 32px 16px;
  color: #98a2b3;
  text-align: center;
}

.pagination-pages {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.pagination-item {
  background: #fff;
}

.pagination-item:disabled {
  color: #98a2b3;
  cursor: not-allowed;
  background: #f8fafc;
}

@media (max-width: 640px) {
  .pagination {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .pagination-pages {
    justify-content: flex-start;
  }
}
</style>
