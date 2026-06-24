<template>
  <div>
    <div class="content-header">
      <div class="header-title">我的来访申请</div>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="openApplyDialog">申请来访</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table">
        <thead>
          <tr>
            <th>序号</th>
            <th>来访人姓名</th>
            <th>来访寝室</th>
            <th>来访说明</th>
            <th>来访时间</th>
            <th>申请时间</th>
            <th>处理状态</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in myApplications" :key="item.id">
            <td>{{ index + 1 }}</td>
            <td>{{ item.visitorName }}</td>
            <td>{{ item.dormitory }}</td>
            <td>{{ item.description }}</td>
            <td>{{ item.visitTime ? new Date(item.visitTime).toLocaleString() : '未指定' }}</td>
            <td>{{ new Date(item.createdAt || item.visitTime).toLocaleString() }}</td>
            <td>
              <span class="badge" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
            </td>
          </tr>
        </tbody>
        <tbody v-if="loading">
          <tr>
            <td colspan="7" class="loading-text">加载中...</td>
          </tr>
        </tbody>
        <tbody v-if="!loading && myApplications.length === 0">
          <tr>
            <td colspan="7" class="loading-text">暂无申请记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 申请对话框 -->
    <div v-if="showDialog" class="modal-overlay" @click="closeApplyDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>申请来访</h3>
          <button class="close-btn" @click="closeApplyDialog">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>来访人姓名 <span style="color: red;">*</span></label>
            <input type="text" class="form-input" v-model="visitForm.visitorName" placeholder="请输入来访人姓名" maxlength="50">
          </div>
          <div class="form-group">
            <label>来访寝室</label>
            <input type="text" class="form-input" :value="myDormitory || '无'" disabled>
          </div>
          <div class="form-group">
            <label>来访时间 <span style="color: red;">*</span></label>
            <input type="datetime-local" class="form-input" v-model="visitForm.visitTime">
          </div>
          <div class="form-group">
            <label>来访说明 <span style="color: red;">*</span></label>
            <textarea class="form-input" v-model="visitForm.description" placeholder="请简要说明来访事由" rows="3" maxlength="500"></textarea>
            <div style="text-align: right; font-size: 12px; color: #999;">{{ visitForm.description.length }}/500</div>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { visitApi } from '../api/visitApi';
import { accommodationApi } from '../api/accommodationApi';
import { ElMessage } from 'element-plus';

const myApplications = ref([]);
const loading = ref(false);
const showDialog = ref(false);
const submitting = ref(false);
const myDormitory = ref('');
const visitForm = ref({
  visitorName: '',
  dormitory: '',
  description: '',
  visitTime: '',
  studentId: '',
  studentName: '',
});

const fetchData = async () => {
  loading.value = true;
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (!userInfo.studentId) {
      ElMessage.error('无法获取用户信息，请重新登录');
      return;
    }

    // 获取学生住宿信息以便自动填充
    try {
      const accommodations = await accommodationApi.searchAccommodationsByStudentId(userInfo.studentId, '');
      if (accommodations.length > 0) {
        myDormitory.value = accommodations[0].dormitory;
      }
    } catch (e) {
      console.error('获取住宿信息失败', e);
    }

    const applications = await visitApi.getVisitsByStudentId(userInfo.studentId);
    myApplications.value = applications.sort((a, b) => new Date(b.visitTime || b.createdAt) - new Date(a.visitTime || a.createdAt));
  } catch (error) {
    console.error('数据加载失败:', error);
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);

const openApplyDialog = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  if (!userInfo.studentId) {
    ElMessage.error('无法获取用户信息，请重新登录');
    return;
  }

  if (!myDormitory.value) {
    ElMessage.warning('您没有住宿信息，无法申请来访');
    return;
  }

  // 设置默认时间为当前时间后一小时
  const now = new Date();
  now.setHours(now.getHours() + 1);
  now.setMinutes(0);
  now.setSeconds(0);
  now.setMilliseconds(0);
  // 格式化为 datetime-local 所需格式: YYYY-MM-DDThh:mm
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const defaultTime = `${year}-${month}-${day}T${hours}:${minutes}`;

  visitForm.value = {
    visitorName: '',
    dormitory: myDormitory.value, // 自动填充寝室号
    description: '',
    visitTime: defaultTime,
    studentId: userInfo.studentId,
    studentName: userInfo.name || userInfo.studentName || '',
  };
  showDialog.value = true;
};



const closeApplyDialog = () => {
  showDialog.value = false;
};

const submitApplication = async () => {
  if (!visitForm.value.visitorName || !visitForm.value.dormitory || !visitForm.value.description || !visitForm.value.visitTime) {
    ElMessage.warning('请填写所有必填项');
    return;
  }

  submitting.value = true;
  try {
    // 转换时间格式为 ISO 字符串，确保后端能解析
    const submitData = {
      ...visitForm.value,
      visitTime: new Date(visitForm.value.visitTime).toISOString()
    };

    await visitApi.applyForVisit(submitData);
    ElMessage.success('申请已提交，请等待管理员审核');
    closeApplyDialog();
    await fetchData();
  } catch (error) {
    const message = error.response?.data?.message || '申请失败，请稍后再试';
    ElMessage.error(message);
  } finally {
    submitting.value = false;
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
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-title {
  font-size: 20px;
  font-weight: 600;
}
.loading-text {
  text-align: center;
  padding: 20px;
  color: #999;
}
.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}
.badge-warning { background-color: #faad14; }
.badge-success { background-color: #52c41a; }
.badge-danger { background-color: #f5222d; }
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
.form-input:disabled { background-color: #f5f5f5; cursor: not-allowed; }
</style>
