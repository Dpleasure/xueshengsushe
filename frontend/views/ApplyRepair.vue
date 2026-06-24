<template>
  <div>
    <div class="content-header">
      <div class="header-title">我的报修申请</div>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="openApplyDialog">申请报修</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table">
        <thead>
          <tr>
            <th>序号</th>
            <th>报修寝室</th>
            <th>报修说明</th>
            <th>报修图片</th>
            <th>申请时间</th>
            <th>处理状态</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in myApplications" :key="item.id">
            <td>{{ index + 1 }}</td>
            <td>{{ item.dormitory }}</td>
            <td>{{ item.description }}</td>
            <td>
              <img v-if="item.images" :src="getImageUrl(item.images)" alt="报修图片" class="repair-image" @click="previewImage(getImageUrl(item.images))">
              <span v-else>无</span>
            </td>
            <td>{{ new Date(item.repairTime).toLocaleString() }}</td>
            <td>
              <span class="badge" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
            </td>
          </tr>
        </tbody>
        <tbody v-if="loading">
          <tr>
            <td colspan="6" class="loading-text">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && myApplications.length === 0">
          <tr>
            <td colspan="6" class="loading-text">暂无报修记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 报修申请对话框 -->
    <div v-if="showDialog" class="modal-overlay" @click="closeApplyDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>申请报修</h3>
          <button class="close-btn" @click="closeApplyDialog">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>报修寝室</label>
            <input type="text" class="form-input" :value="myAccommodation ? myAccommodation.dormitory : '无'" disabled>
          </div>
          <div class="form-group">
            <label>报修说明 <span style="color: red;">*</span></label>
            <textarea class="form-input" v-model="description" placeholder="请详细描述报修内容" rows="4" maxlength="500"></textarea>
            <div style="text-align: right; font-size: 12px; color: #999;">{{ description.length }}/500</div>
          </div>
          <div class="form-group">
            <label>上传图片 (可选)</label>
            <input type="file" @change="handleFileChange" accept="image/*">
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="closeApplyDialog">取消</button>
          <button class="btn btn-primary" @click="submitApplication" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认申请' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 图片预览模态框 -->
    <div v-if="showImagePreview" class="modal-overlay" @click="showImagePreview = false">
      <div class="image-preview-modal" @click.stop>
        <span class="modal-close" @click="showImagePreview = false">×</span>
        <img :src="previewImageUrl" alt="预览图片" class="preview-image">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { repairApi } from '../api/repairApi'; // 假设已创建
import { accommodationApi } from '../api/accommodationApi';
import { ElMessage } from 'element-plus';

const myApplications = ref([]);
const loading = ref(false);
const showDialog = ref(false);
const description = ref('');
const imageFile = ref(null);
const submitting = ref(false);
const myAccommodation = ref(null);
const showImagePreview = ref(false);
const previewImageUrl = ref('');

// 获取当前学生的住宿信息和报修记录
const fetchData = async () => {
  loading.value = true;
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (!userInfo.studentId) {
      ElMessage.error('无法获取用户信息，请重新登录');
      return;
    }

    // 获取住宿信息
    const accommodations = await accommodationApi.searchAccommodationsByStudentId(userInfo.studentId, '');
    if (accommodations.length > 0) {
      myAccommodation.value = accommodations[0];
      // 获取该生的报修记录
      const applications = await repairApi.getRepairsByStudentId(userInfo.studentId);
      myApplications.value = applications.sort((a, b) => new Date(b.repairTime) - new Date(a.repairTime));
    } else {
      ElMessage.warning('您当前没有住宿信息，无法申请报修');
    }
  } catch (error) {
    console.error('数据加载失败:', error);
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);

const openApplyDialog = () => {
  if (!myAccommodation.value) {
    ElMessage.warning('您没有住宿信息，无法申请报修');
    return;
  }
  showDialog.value = true;
};

const closeApplyDialog = () => {
  showDialog.value = false;
  description.value = '';
  imageFile.value = null;
};

const handleFileChange = (event) => {
  imageFile.value = event.target.files[0];
};

const submitApplication = async () => {
  if (!description.value.trim()) {
    ElMessage.warning('请填写报修说明');
    return;
  }

  submitting.value = true;
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const formData = new FormData();
    formData.append('studentName', myAccommodation.value.studentName);
    formData.append('studentId', userInfo.studentId);
    formData.append('dormitory', myAccommodation.value.dormitory);
    formData.append('description', description.value.trim());
    if (imageFile.value) {
      formData.append('image', imageFile.value);
    }

    await repairApi.applyForRepair(formData);
    ElMessage.success('报修申请已提交');
    closeApplyDialog();
    await fetchData(); // 重新加载数据
  } catch (error) {
    const message = error.response?.data?.message || '申请失败，请稍后再试';
    ElMessage.error(message);
  } finally {
    submitting.value = false;
  }
};

const previewImage = (url) => {
  previewImageUrl.value = url;
  showImagePreview.value = true;
};

const getStatusText = (status) => {
  const statusMap = { 'PENDING': '待处理', 'PROCESSING': '处理中', 'COMPLETED': '已完成' };
  return statusMap[status] || '未知';
};

const getStatusClass = (status) => {
  const classMap = { 'PENDING': 'badge-warning', 'PROCESSING': 'badge-primary', 'COMPLETED': 'badge-success' };
  return classMap[status] || 'badge-default';
};

const getImageUrl = (imagePath) => {
  if (!imagePath) return '';
  // 如果已经是完整URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath;
  }
  // 如果是相对路径，拼接后端地址
  return `http://localhost:8080${imagePath}`;
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
.form-input { width: 100%; padding: 8px 12px; border: 1px solid #d9d9d9; border-radius: 4px; resize: vertical; }
.form-input:disabled { background-color: #f5f5f5; cursor: not-allowed; }
.image-preview-modal { position: relative; max-width: 90vw; max-height: 90vh; background: white; border-radius: 8px; padding: 20px; }
.preview-image { max-width: 100%; max-height: 80vh; }
.modal-close { position: absolute; top: 10px; right: 10px; font-size: 32px; color: #666; cursor: pointer; }
</style>
