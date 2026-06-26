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
    </div>

    <div class="content-body">
      <table class="table">
        <thead>
          <tr>
            <th>序号</th>
            <th>报修人</th>
            <th>学号</th>
            <th>报修寝室</th>
            <th>报修说明</th>
            <th>报修图片</th>
            <th>报修时间</th>
            <th>处理状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in filteredRepairs" :key="item.id">
            <td>{{ index + 1 }}</td>
            <td>{{ item.reporter }}</td>
            <td>{{ item.studentId }}</td>
            <td>{{ item.dormitory }}</td>
            <td>{{ item.description }}</td>
            <td>
              <div class="image-gallery">
                <img 
                  v-if="item.images"
                  :src="getImageUrl(item.images)" 
                  :alt="`报修图片`"
                  class="repair-image"
                  @click="previewImage(getImageUrl(item.images))"
                >
                <span v-else class="no-image">暂无图片</span>
              </div>
            </td>
            <td>{{ new Date(item.repairTime).toLocaleString() }}</td>
            <td>
              <span class="badge" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>
              <button 
                class="btn-link"
                @click="handleChangeStatus(item)"
                :disabled="item.status === 'COMPLETED'"
              >
                更改状态
              </button>
            </td>
          </tr>
        </tbody>
        <tbody v-if="loading">
          <tr>
            <td colspan="9" class="loading-text">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && filteredRepairs.length === 0">
          <tr>
            <td colspan="9" class="loading-text">暂无报修记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 状态更改模态框 -->
    <div class="modal-overlay" v-if="showStatusModal" @click.self="showStatusModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>更改报修状态</h3>
          <button class="close-btn" @click="showStatusModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">当前状态：{{ getStatusText(currentItem.status) }}</label>
          </div>
          <div class="form-group">
            <label class="form-label">选择新状态</label>
            <select class="form-input" v-model="newStatus">
              <option value="PENDING">待处理</option>
              <option value="PROCESSING">处理中</option>
              <option value="COMPLETED">已完成</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showStatusModal = false">取消</button>
          <button class="btn btn-primary" @click="confirmStatusChange">确定</button>
        </div>
      </div>
    </div>

    <!-- 图片预览模态框 -->
    <div class="modal-overlay" v-if="showImagePreview" @click="showImagePreview = false">
      <div class="image-preview-modal" @click.stop>
        <span class="modal-close" @click="showImagePreview = false">×</span>
        <img :src="previewImageUrl" alt="预览图片" class="preview-image">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { repairApi } from '../api/repairApi';
import { ElMessage } from 'element-plus';
import { withApiBaseUrl } from '../api/baseUrl.js';

const searchKeyword = ref('');
const repairs = ref([]);
const loading = ref(false);
const showStatusModal = ref(false);
const showImagePreview = ref(false);
const previewImageUrl = ref('');
const currentItem = ref({});
const newStatus = ref('');

const filteredRepairs = computed(() => {
  if (!searchKeyword.value) return repairs.value;
  return repairs.value.filter(r => 
    (r.reporter && r.reporter.includes(searchKeyword.value)) || 
    (r.dormitory && r.dormitory.includes(searchKeyword.value)) ||
    (r.studentId && r.studentId.includes(searchKeyword.value))
  );
});

const fetchData = async () => {
  loading.value = true;
  try {
    const data = await repairApi.getAllRepairs();
    repairs.value = data.sort((a, b) => new Date(b.repairTime) - new Date(a.repairTime));
  } catch (error) {
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);

const getStatusText = (status) => {
  const statusMap = { 'PENDING': '待处理', 'PROCESSING': '处理中', 'COMPLETED': '已完成' };
  return statusMap[status] || '未知';
};

const getStatusClass = (status) => {
  const classMap = { 'PENDING': 'badge-warning', 'PROCESSING': 'badge-primary', 'COMPLETED': 'badge-success' };
  return classMap[status] || 'badge-default';
};

const previewImage = (imageUrl) => {
  previewImageUrl.value = imageUrl;
  showImagePreview.value = true;
};

const handleChangeStatus = (item) => {
  currentItem.value = { ...item };
  newStatus.value = item.status;
  showStatusModal.value = true;
};

const confirmStatusChange = async () => {
  if (newStatus.value === currentItem.value.status) {
    ElMessage.warning('状态未改变');
    return;
  }
  
  try {
    await repairApi.updateRepairStatus(currentItem.value.id, newStatus.value);
    ElMessage.success(`状态已更改为：${getStatusText(newStatus.value)}`);
    showStatusModal.value = false;
    await fetchData(); // Refresh data
  } catch (error) {
    ElMessage.error('状态更新失败');
  }
};

const handleSearch = () => {
  // The computed property `filteredRepairs` handles filtering automatically.
  // This function can be used if a manual search trigger is preferred.
};

const handleReset = () => { searchKeyword.value = ''; };

const getImageUrl = (imagePath) => {
  if (!imagePath) return '';
  // 如果已经是完整URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath;
  }
  // 如果是相对路径，拼接后端地址
  return withApiBaseUrl(imagePath);
};
</script>

<style scoped>
.content-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-title { font-size: 20px; font-weight: 600; }
.loading-text { text-align: center; padding: 20px; color: #999; }
.repair-image { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; cursor: pointer; }
.badge { display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; color: white; }
.badge-warning { background-color: #faad14; }
.badge-primary { background-color: #1890ff; }
.badge-success { background-color: #52c41a; }
.badge-default { background-color: #d9d9d9; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; border-radius: 8px; width: 90%; max-width: 500px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid #f0f0f0; }
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 28px; color: #999; cursor: pointer; }
.modal-body { padding: 24px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px; border-top: 1px solid #f0f0f0; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
.form-input { width: 100%; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 4px; }
.image-preview-modal { position: relative; max-width: 90vw; max-height: 90vh; background: white; border-radius: 8px; padding: 20px; }
.preview-image { max-width: 100%; max-height: 80vh; }
.modal-close { position: absolute; top: 10px; right: 10px; font-size: 32px; color: #666; cursor: pointer; }
</style>
