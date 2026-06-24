const pad = (value) => String(value).padStart(2, '0');

const formatDateTime = (value) => {
  if (!value) return '未指定';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const toIsoFromLocal = (date, time) => {
  if (!date) return '';
  const localValue = `${date}T${time || '00:00'}:00`;
  const parsed = new Date(localValue);
  if (Number.isNaN(parsed.getTime())) return localValue;
  return parsed.toISOString();
};

const statusTextMap = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  COMPLETED: '已完成',
  APPROVED: '已同意',
  REJECTED: '已拒绝'
};

const statusClassMap = {
  PENDING: 'badge-pending',
  PROCESSING: 'badge-processing',
  COMPLETED: 'badge-completed',
  APPROVED: 'badge-approved',
  REJECTED: 'badge-rejected'
};

const statusText = (status) => statusTextMap[status] || status || '-';
const statusClass = (status) => statusClassMap[status] || 'badge-pending';

module.exports = {
  formatDateTime,
  toIsoFromLocal,
  repairStatusText: statusText,
  repairStatusClass: statusClass,
  changeStatusText: statusText,
  changeStatusClass: statusClass,
  visitStatusText: statusText,
  visitStatusClass: statusClass
};
