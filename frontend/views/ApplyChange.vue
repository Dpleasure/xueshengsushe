<template>
  <div>
    <div class="content-header">
      <div class="header-title">我的换寝申请</div>
      <div class="action-buttons">
        <button class="btn btn-primary" @click="openApplyDialog">换寝申请</button>
      </div>
    </div>

    <div class="content-body">
      <table class="table">
        <thead>
          <tr>
            <th>序号</th>
            <th>原寝室</th>
            <th>原床位</th>
            <th>申请更换至</th>
            <th>申请时间</th>
            <th>处理状态</th>
          </tr>
        </thead>
        <tbody v-if="!loading">
          <tr v-for="(item, index) in myApplications" :key="item.id">
            <td>{{ index + 1 }}</td>
            <td>{{ item.oldDormitoryA }}</td>
            <td>{{ item.oldBedA }}</td>
            <td>{{ item.newDormitoryA }} - {{ item.newBedA }}</td>
            <td>{{ new Date(item.changeTime).toLocaleString() }}</td>
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
            <td colspan="6" class="loading-text">暂无申请记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 换寝申请对话框 -->
    <div v-if="showDialog" class="modal-overlay" @click="closeApplyDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>换寝申请</h3>
          <button class="close-btn" @click="closeApplyDialog">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>原寝室</label>
            <input type="text" class="form-input" :value="myAccommodation ? myAccommodation.dormitory : '无'" disabled>
          </div>
          <div class="form-group">
            <label>原床位</label>
            <input type="text" class="form-input" :value="myAccommodation ? myAccommodation.bed : '无'" disabled>
          </div>

          <!-- 宿舍楼选择 -->
          <div class="form-group">
            <label>选择宿舍楼 <span style="color: red;">*</span></label>
            <select class="form-input" v-model="selectedBuilding" @change="handleBuildingChange">
              <option value="">请选择宿舍楼</option>
              <option v-for="building in buildings" :key="building.id" :value="building.name">
                {{ building.name }}
              </option>
            </select>
          </div>

          <!-- 宿舍选择 -->
          <div class="form-group">
            <label>选择新寝室 <span style="color: red;">*</span></label>
            <select class="form-input" v-model="newDormitory" @change="handleDormitoryChange" :disabled="!selectedBuilding">
              <option value="">请选择寝室</option>
              <option v-for="dorm in availableDormitories" :key="dorm.id" :value="dorm.number">
                {{ dorm.number }} (空闲: {{ dorm.capacity - dorm.occupied }}人)
              </option>
            </select>
            <div v-if="selectedBuilding && availableDormitories.length === 0" style="color: #999; font-size: 12px; margin-top: 4px;">
              该楼栋暂无空闲寝室
            </div>
          </div>

          <!-- 床位选择 -->
          <div class="form-group">
            <label>选择新床位 <span style="color: red;">*</span></label>
            <select class="form-input" v-model="newBed" :disabled="!newDormitory || loadingBeds">
              <option value="">{{ loadingBeds ? '加载床位中...' : '请选择床位' }}</option>
              <option v-for="bed in availableBeds" :key="bed" :value="bed">
                {{ bed }}
              </option>
            </select>
            <div v-if="newDormitory && !loadingBeds && availableBeds.length === 0" style="color: red; font-size: 12px; margin-top: 4px;">
              该寝室暂无空闲床位
            </div>
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
import { accommodationApi } from '../api/accommodationApi';
import { buildingApi } from '../api/buildingApi';
import { dormitoryApi } from '../api/dormitoryApi';
import { ElMessage } from 'element-plus';

const myApplications = ref([]);
const loading = ref(false);
const showDialog = ref(false);
const submitting = ref(false);
const myAccommodation = ref(null);

// 选择相关数据
const buildings = ref([]);
const availableDormitories = ref([]);
const selectedBuilding = ref('');
const newDormitory = ref('');
const newBed = ref('');
const currentDormCapacity = ref(4);
const availableBeds = ref([]); // 改为 ref，手动计算
const loadingBeds = ref(false);

// 获取当前学生的住宿信息和申请记录
const fetchData = async () => {
  loading.value = true;
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (!userInfo.studentId || !userInfo.name) {
      ElMessage.error('无法获取用户信息，请重新登录');
      return;
    }

    // 获取住宿信息
    const accommodations = await accommodationApi.searchAccommodationsByStudentId(userInfo.studentId, '');
    if (accommodations.length > 0) {
      myAccommodation.value = accommodations[0];
      // 获取申请记录
      const applications = await accommodationApi.getChangeRecordsByStudent(userInfo.name);
      myApplications.value = applications.sort((a, b) => new Date(b.changeTime) - new Date(a.changeTime));
    } else {
      ElMessage.warning('您当前没有住宿信息，无法申请换寝');
    }
  } catch (error) {
    console.error('数据加载失败:', error);
    ElMessage.error('数据加载失败');
  } finally {
    loading.value = false;
  }
};

// 获取所有宿舍楼
const fetchBuildings = async () => {
  try {
    buildings.value = await buildingApi.getAllBuildings();
  } catch (error) {
    console.error('获取宿舍楼失败:', error);
  }
};

onMounted(() => {
  fetchData();
  fetchBuildings();
});

const openApplyDialog = () => {
  if (!myAccommodation.value) {
    ElMessage.warning('您没有住宿信息，无法申请换寝');
    return;
  }
  showDialog.value = true;
  // 重置选择
  selectedBuilding.value = '';
  newDormitory.value = '';
  newBed.value = '';
  availableDormitories.value = [];
  availableBeds.value = [];
};

const closeApplyDialog = () => {
  showDialog.value = false;
};

// 处理宿舍楼变化
const handleBuildingChange = async () => {
  newDormitory.value = '';
  newBed.value = '';
  availableDormitories.value = [];
  availableBeds.value = [];

  if (!selectedBuilding.value) return;

  try {
    // 获取所有宿舍
    const dorms = await dormitoryApi.getAllDormitories();
    // 筛选出该楼且有空位的宿舍
    availableDormitories.value = dorms.filter(d =>
      d.building === selectedBuilding.value &&
      d.occupied < d.capacity &&
      d.number !== myAccommodation.value.dormitory // 排除自己当前的宿舍
    );
  } catch (error) {
    console.error('获取宿舍失败:', error);
    ElMessage.error('获取宿舍列表失败');
  }
};

// 处理宿舍变化 - 核心逻辑：计算真实空闲床位
const handleDormitoryChange = async () => {
  newBed.value = '';
  availableBeds.value = [];

  if (!newDormitory.value) return;

  const selectedDorm = availableDormitories.value.find(d => d.number === newDormitory.value);
  if (selectedDorm) {
    currentDormCapacity.value = selectedDorm.capacity;
  }

  loadingBeds.value = true;
  try {
    // 1. 生成该宿舍所有可能的床位列表 (例如: 1号床, 2号床...)
    const allPossibleBeds = Array.from({ length: currentDormCapacity.value }, (_, i) => `${i + 1}号床`);

    // 2. 查询该宿舍当前的入住情况
    // 注意：这里使用 searchAccommodations 接口，传入 dormitory 参数
    const occupiedRecords = await accommodationApi.searchAccommodations(null, newDormitory.value);

    // 3. 提取已占用的床位
    const occupiedBeds = occupiedRecords.map(record => record.bed);

    // 4. 过滤出空闲床位
    availableBeds.value = allPossibleBeds.filter(bed => !occupiedBeds.includes(bed));

  } catch (error) {
    console.error('获取床位信息失败:', error);
    ElMessage.error('获取床位信息失败');
    // 降级处理：如果查询失败，显示所有床位
    availableBeds.value = Array.from({ length: currentDormCapacity.value }, (_, i) => `${i + 1}号床`);
  } finally {
    loadingBeds.value = false;
  }
};

const submitApplication = async () => {
  if (!newDormitory.value || !newBed.value) {
    ElMessage.warning('请选择完整的新寝室和床位信息');
    return;
  }

  submitting.value = true;
  try {
    await accommodationApi.applyForChange({
      accommodationId: myAccommodation.value.id,
      newDormitory: newDormitory.value,
      newBed: newBed.value,
    });
    ElMessage.success('申请已提交，请等待管理员审核');
    closeApplyDialog();
    await fetchData(); // 重新加载数据
  } catch (error) {
    const message = error.response?.data?.message || '申请失败，请稍后再试';
    ElMessage.error(message);
  } finally {
    submitting.value = false;
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
