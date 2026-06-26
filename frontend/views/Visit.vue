<template>
  <div>
    <div class="content-header">
      <div class="header-title">来访申请管理</div>
      <div class="action-buttons">
        <button class="btn btn-danger" @click="handleBatchDelete">批量删除已处理记录</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table">
        <thead>
          <tr>
            <th><input type="checkbox" class="checkbox" @change="toggleSelectAll"></th>
            <th>序号</th>
            <th>申请学生</th>
            <th>学号</th>
            <th>来访人</th>
            <th>来访寝室</th>
            <th>申请时间</th>
            <th>处理状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in visits" :key="item.id">
            <td><input type="checkbox" class="checkbox" :value="item.id" v-model="selectedIds"></td>
            <td>{{ index + 1 }}</td>
            <td>{{ item.studentName || 'N/A' }}</td> <!-- Assuming studentName is available -->
            <td>{{ item.studentId }}</td>
            <td>{{ item.visitorName }}</td>
            <td>{{ item.dormitory }}</td>
            <td>{{ new Date(item.visitTime).toLocaleString() }}</td>
            <td>
              <span class="badge" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
            </td>
            <td>
              <div v-if="item.status === 'PENDING'" class="table-actions">
                <button class="btn-link success" @click="handleApprove(item.id)">同意</button>
                <button class="btn-link danger" @click="handleReject(item.id)">拒绝</button>
              </div>
              <span v-else class="text-muted">已处理</span>
            </td>
          </tr>
        </tbody>
        <tbody v-if="loading">
          <tr>
            <td colspan="9" class="loading-text">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && visits.length === 0">
          <tr>
            <td colspan="9" class="loading-text">暂无来访申请记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { visitApi } from '../api/visitApi';
import { ElMessage } from 'element-plus';

const visits = ref([]);
const loading = ref(false);
const selectedIds = ref([]);

const fetchData = async () => {
  loading.value = true;
  try {
    const data = await visitApi.getAllVisits();
    visits.value = data.sort((a, b) => new Date(b.visitTime) - new Date(a.visitTime));
  } catch (error) {
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);

const handleApprove = async (id) => {
  try {
    await visitApi.approveVisit(id);
    ElMessage.success('操作成功，已同意该申请');
    await fetchData();
  } catch (error) {
    const message = error.response?.data?.message || '操作失败';
    ElMessage.error(message);
  }
};

const handleReject = async (id) => {
  try {
    await visitApi.rejectVisit(id);
    ElMessage.success('操作成功，已拒绝该申请');
    await fetchData();
  } catch (error) {
    const message = error.response?.data?.message || '操作失败';
    ElMessage.error(message);
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
        await visitApi.deleteVisit(id);
      }
      ElMessage.success('批量删除成功');
      await fetchData();
      selectedIds.value = [];
    } catch (error) {
      ElMessage.error('批量删除失败');
    }
  }
};

const toggleSelectAll = (e) => {
  if (e.target.checked) {
    selectedIds.value = visits.value.map(v => v.id);
  } else {
    selectedIds.value = [];
  }
};

const getStatusText = (status) => {
  const statusMap = { 'PENDING': '待处理', 'APPROVED': '已同意', 'REJECTED': '已拒绝' };
  return statusMap[status] || '未知';
};

const getStatusClass = (status) => {
  const classMap = { 'PENDING': 'badge-warning', 'APPROVED': 'badge-success', 'REJECTED': 'badge-danger' };
  return classMap[status] || 'badge-default';
};
</script>

<style scoped>
.header-title { font-size: 20px; font-weight: 600; }
.loading-text { text-align: center; padding: 20px; color: #999; }
.text-muted { color: #999; }
.badge { display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; color: white; }
.badge-warning { background-color: #faad14; }
.badge-success { background-color: #52c41a; }
.badge-danger { background-color: #f5222d; }
.badge-default { background-color: #d9d9d9; }
.btn-link.success { color: #52c41a; }
.btn-link.success:hover { color: #73d13d; }
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
