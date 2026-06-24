<template>
  <div>
    <div class="content-header">
      <div class="header-title">换寝申请管理</div>
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
            <th>学生姓名</th>
            <th>原寝室 & 床位</th>
            <th>申请更换至</th>
            <th>申请时间</th>
            <th>处理状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in changes" :key="item.id">
            <td><input type="checkbox" class="checkbox" :value="item.id" v-model="selectedIds"></td>
            <td>{{ index + 1 }}</td>
            <td>{{ item.studentA }}</td>
            <td>{{ item.oldDormitoryA }} - {{ item.oldBedA }}</td>
            <td>{{ item.newDormitoryA }} - {{ item.newBedA }}</td>
            <td>{{ new Date(item.changeTime).toLocaleString() }}</td>
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
            <td colspan="8" class="loading-text">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && changes.length === 0">
          <tr>
            <td colspan="8" class="loading-text">暂无换寝申请记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { roomChangeApi } from '../api/roomChangeApi';
import { ElMessage } from 'element-plus';

const changes = ref([]);
const loading = ref(false);
const selectedIds = ref([]);

const fetchData = async () => {
  loading.value = true;
  try {
    const data = await roomChangeApi.getAllRoomChanges();
    changes.value = data.sort((a, b) => new Date(b.changeTime) - new Date(a.changeTime));
  } catch (error) {
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);

const handleApprove = async (id) => {
  try {
    await roomChangeApi.approveChange(id);
    ElMessage.success('操作成功，已同意该申请');
    await fetchData(); // 重新加载数据以更新状态
  } catch (error) {
    const message = error.response?.data?.message || '操作失败';
    ElMessage.error(message);
  }
};

const handleReject = async (id) => {
  try {
    await roomChangeApi.rejectChange(id);
    ElMessage.success('操作成功，已拒绝该申请');
    await fetchData(); // 重新加载数据以更新状态
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
        await roomChangeApi.deleteRoomChange(id);
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
    selectedIds.value = changes.value.map(c => c.id);
  } else {
    selectedIds.value = [];
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待处理';
    case 'APPROVED': return '已同意';
    case 'REJECTED': return '已拒绝';
    default: return '未知';
  }
};

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'badge-warning';
    case 'APPROVED': return 'badge-success';
    case 'REJECTED': return 'badge-danger';
    default: return 'badge-default';
  }
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
</style>