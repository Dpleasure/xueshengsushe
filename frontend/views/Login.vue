<template>
  <div class="login-container">
    <!-- 卡通背景元素 -->
    <div class="cartoon-bg">
      <div class="cloud cloud-1">☁️</div>
      <div class="cloud cloud-2">☁️</div>
      <div class="sun">☀️</div>
      <div class="tree tree-1">🌲</div>
      <div class="tree tree-2">🌳</div>
    </div>

    <div class="login-box">
      <div class="cartoon-header">
        <span class="school-icon">🏫</span>
      </div>
      <h1 class="login-title">欢迎登录学生宿舍管理系统</h1>
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="input-group">
          <span class="input-icon">👤</span>
          <input
            type="text"
            class="login-input"
            placeholder="请输入账号"
            v-model="form.username"
          >
        </div>
        <div class="input-group">
          <span class="input-icon">🔒</span>
          <input
            type="password"
            class="login-input"
            placeholder="请输入密码"
            v-model="form.password"
          >
        </div>
        <div class="input-group">
          <span class="input-icon">🆔</span>
          <input
            type="text"
            class="login-input"
            placeholder="请输入工号或学号"
            v-model="form.studentId"
          >
        </div>
        <select class="login-select" v-model="form.role">
          <option value="">请选择角色</option>
          <option value="admin">管理员</option>
          <option value="student">学生</option>
        </select>
        <!-- 滑动验证码 -->
        <div ref="dragDiv" class="drag">
          <div class="drag_bg"></div>
          <div class="drag_text">{{ confirmWords }}</div>
          <div ref="moveDiv" :class="{'handler_ok_bg':confirmSuccess}" class="handler handler_bg"
               style="position: absolute;top: 0px;left: 0px;" @mousedown="mousedownFn($event)"></div>
        </div>
        <button type="submit" class="login-btn" :disabled="!confirmSuccess">登 录</button>
      </form>
      <div class="login-footer">
        <a href="#" @click.prevent="openForgotPasswordDialog" class="register-link">忘记密码？</a>
      </div>
    </div>
  </div>

    <!-- 忘记密码弹窗 -->
    <div v-if="showForgotPassword" class="modal-overlay" @click="closeForgotPasswordDialog">
      <div class="modal-content" @click.stop>
        <h2 class="login-title">重置管理员密码</h2>
        <form class="login-form" @submit.prevent="handleForgotPassword">
          <div class="input-group">
            <span class="input-icon">👤</span>
            <input type="text" class="login-input" placeholder="请输入管理员账号" v-model="forgotPasswordForm.username">
          </div>
          <div class="input-group">
            <span class="input-icon">✉️</span>
            <input type="email" class="login-input" placeholder="请输入邮箱" v-model="forgotPasswordForm.email">
          </div>
          <div class="input-group">
            <span class="input-icon">📞</span>
            <input type="text" class="login-input" placeholder="请输入手机号" v-model="forgotPasswordForm.phone">
          </div>
           <div class="input-group">
            <span class="input-icon">🔒</span>
            <input type="password" class="login-input" placeholder="请输入新密码" v-model="forgotPasswordForm.newPassword">
          </div>
          <button type="submit" class="login-btn">确认重置</button>
        </form>
      </div>
    </div>
</template>

<script setup>
import {reactive, ref, onMounted, onUnmounted, nextTick} from 'vue'
import { useRouter } from 'vue-router'
import {post} from "@/net/run";
import {ElMessage} from "element-plus";

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  studentId: '',
  role: ''
})

const showForgotPassword = ref(false)
const forgotPasswordForm = reactive({
  username: '',
  email: '',
  phone: '',
  newPassword: ''
})

// 滑动验证码相关
const dragDiv = ref(null)
const moveDiv = ref(null)
const beginClientX = ref(0)
const mouseMoveStata = ref(false)
const maxwidth = ref('')
const confirmWords = ref('拖动滑块验证')
const confirmSuccess = ref(false)

const openForgotPasswordDialog = () => {
  showForgotPassword.value = true
}

const closeForgotPasswordDialog = () => {
  showForgotPassword.value = false
}

const handleForgotPassword = () => {
  if (!forgotPasswordForm.username || !forgotPasswordForm.email || !forgotPasswordForm.phone || !forgotPasswordForm.newPassword) {
    ElMessage.warning('请填写所有字段');
    return;
  }

  post('http://localhost:8080/api/auth/forgot-password', {
    username: forgotPasswordForm.username,
    email: forgotPasswordForm.email,
    phone: forgotPasswordForm.phone,
    newPassword: forgotPasswordForm.newPassword
  }, (data) => {
    ElMessage.success(data); // 后端直接返回消息字符串
    closeForgotPasswordDialog();
  }, (error) => {
    ElMessage.error(error);
  });
}

// 滑动验证码方法
const mousedownFn = (e) => {
  if (!confirmSuccess.value) {
    e.preventDefault && e.preventDefault()
    // 确保 maxwidth 已计算
    if (!maxwidth.value && dragDiv.value && moveDiv.value) {
      maxwidth.value = dragDiv.value.clientWidth - moveDiv.value.clientWidth
    }
    mouseMoveStata.value = true
    beginClientX.value = e.clientX
  }
}

const successFunction = () => {
  confirmSuccess.value = true
  confirmWords.value = '验证通过'
  // 不再移除事件监听器，而是通过 confirmSuccess.value 来控制是否允许拖动
  const dragText = document.getElementsByClassName('drag_text')[0]
  const handler = document.getElementsByClassName('handler')[0]
  const dragBg = document.getElementsByClassName('drag_bg')[0]
  if (dragText) dragText.style.color = '#fff'
  if (handler) {
    handler.style.left = maxwidth.value + 'px'
    handler.classList.remove('handler_bg')
    handler.classList.add('handler_ok_bg')
  }
  if (dragBg) dragBg.style.width = maxwidth.value + 'px'
  // 停止拖动状态
  mouseMoveStata.value = false
}

const mouseMoveFn = (e) => {
  if (mouseMoveStata.value) {
    // 确保 maxwidth 已计算
    if (!maxwidth.value && dragDiv.value && moveDiv.value) {
      maxwidth.value = dragDiv.value.clientWidth - moveDiv.value.clientWidth
    }
    if (!maxwidth.value) return
    
    let width = e.clientX - beginClientX.value
    const handler = document.getElementsByClassName('handler')[0]
    const dragBg = document.getElementsByClassName('drag_bg')[0]
    if (width > 0 && width <= maxwidth.value) {
      if (handler) handler.style.left = width + 'px'
      if (dragBg) dragBg.style.width = width + 'px'
    } else if (width > maxwidth.value) {
      successFunction()
      console.log('验证成功')
    }
  }
}

const moseUpFn = (e) => {
  mouseMoveStata.value = false
  var width = e.clientX - beginClientX.value
  const handler = document.getElementsByClassName('handler')[0]
  const dragBg = document.getElementsByClassName('drag_bg')[0]
  if (width < maxwidth.value) {
    if (handler) handler.style.left = 0 + 'px'
    if (dragBg) dragBg.style.width = 0 + 'px'
  }
}

onMounted(() => {
  nextTick(() => {
    if (dragDiv.value && moveDiv.value) {
      maxwidth.value = dragDiv.value.clientWidth - moveDiv.value.clientWidth
    }
  })
  document.getElementsByTagName('html')[0].addEventListener('mousemove', mouseMoveFn)
  document.getElementsByTagName('html')[0].addEventListener('mouseup', moseUpFn)
})

onUnmounted(() => {
  document.getElementsByTagName('html')[0].removeEventListener('mousemove', mouseMoveFn)
  document.getElementsByTagName('html')[0].removeEventListener('mouseup', moseUpFn)
})

const handleLogin = () => {
  if (!form.username) {
    alert('请输入账号')
    return
  }
  if (!form.password) {
    alert('请输入密码')
    return
  }
  if (!form.studentId) {
    alert('请输入工号或学号')
    return
  }
  if (!form.role) {
    alert('请选择角色')
    return
  }
  if (!confirmSuccess.value) {
    alert('请先完成滑动验证')
    return
  }

  post('http://localhost:8080/api/auth/login', {
    username: form.username,
    password: form.password,
    studentId: form.studentId,
    role: form.role
  },(responseData, status)=>{  // success回调：responseData是response Map（包含token、username、role、message等）
    // 保存token和用户信息
    localStorage.setItem('token', responseData.token)
    localStorage.setItem('userInfo', JSON.stringify({
      username: responseData.username,
      name: responseData.name, // 存储学生姓名
      role: responseData.role,
      studentId:responseData.studentId
    }))
    ElMessage.success(responseData.message || '登录成功')
    router.push('/home')
  }, (message, status) => {  // failure回调：处理登录失败（包括角色不匹配）
    ElMessage.error(message || '登录失败，请检查用户名、密码和角色')
    // 登录失败时重置验证码状态
    resetCaptcha()
  })
}

// 重置验证码的函数
const resetCaptcha = () => {
  confirmSuccess.value = false
  confirmWords.value = '拖动滑块验证'
  mouseMoveStata.value = false
  
  // 重置滑块位置和样式
  const handler = document.getElementsByClassName('handler')[0]
  const dragBg = document.getElementsByClassName('drag_bg')[0]
  const dragText = document.getElementsByClassName('drag_text')[0]
  
  if (handler) {
    handler.style.left = '0px'
    handler.classList.remove('handler_ok_bg')
    handler.classList.add('handler_bg')
  }
  if (dragBg) dragBg.style.width = '0px'
  if (dragText) dragText.style.color = ''
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 修改为清新的蓝色渐变背景，更符合正式界面风格 */
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  position: relative;
  overflow: hidden;
}

/* 卡通背景元素样式 */
.cartoon-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.cloud {
  position: absolute;
  font-size: 60px;
  opacity: 0.8;
  animation: float 6s ease-in-out infinite;
}

.cloud-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.cloud-2 {
  top: 20%;
  right: 15%;
  animation-delay: 2s;
}

.sun {
  position: absolute;
  top: 5%;
  right: 5%;
  font-size: 80px;
  animation: spin 20s linear infinite;
}

.tree {
  position: absolute;
  bottom: 0;
  font-size: 80px;
}

.tree-1 {
  left: 5%;
}

.tree-2 {
  right: 5%;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(24, 144, 255, 0.15);
  width: 90%;
  max-width: 400px;
  z-index: 1;
  border: 1px solid #e6f7ff;
}

.cartoon-header {
  text-align: center;
  margin-bottom: 10px;
}

.school-icon {
  font-size: 64px;
  display: inline-block;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.login-title {
  text-align: center;
  color: #1890ff; /* 使用主题蓝 */
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: bold;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  font-size: 20px;
  pointer-events: none;
}

.login-input {
  width: 100%;
  padding: 12px 12px 12px 45px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  outline: none;
  background: #fff;
}

.login-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.login-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.login-select:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #1890ff; /* 使用主题蓝 */
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.login-btn:hover {
  background: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(24, 144, 255, 0.3);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.register-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 600;
}

.register-link:hover {
  text-decoration: underline;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 400px;
}

/* 滑动验证码样式 */
.drag {
  position: relative;
  background-color: #f5f5f5;
  width: 100%;
  height: 34px;
  line-height: 34px;
  text-align: center;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #d9d9d9;
}

.handler {
  width: 40px;
  height: 32px;
  border: 1px solid #ccc;
  cursor: move;
  z-index: 2;
}

.handler_bg {
  background: #fff url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3hpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNS1jMDIxIDc5LjE1NTc3MiwgMjAxNC8wMS8xMy0xOTo0NDowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo0ZDhlNWY5My05NmI0LTRlNWQtOGFjYi03ZTY4OGYyMTU2ZTYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NTEyNTVEMURGMkVFMTFFNEI5NDBCMjQ2M0ExMDQ1OUYiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NTEyNTVEMUNGMkVFMTFFNEI5NDBCMjQ2M0ExMDQ1OUYiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTQgKE1hY2ludG9zaCkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo2MTc5NzNmZS02OTQxLTQyOTYtYTIwNi02NDI2YTNkOWU5YmUiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6NGQ4ZTVmOTMtOTZiNC00ZTVkLThhY2ItN2U2ODhmMjE1NmU2Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+YiRG4AAAALFJREFUeNpi/P//PwMlgImBQkA9A+bOnfsIiBOxKcInh+yCaCDuByoswaIOpxwjciACFegBqZ1AvBSIS5OTk/8TkmNEjwWgQiUgtQuIjwAxUF3yX3xyGIEIFLwHpKyAWB+I1xGSwxULIGf9A7mQkBwTlhBXAFLHgPgqEAcTkmNCU6AL9d8WII4HOvk3ITkWJAXWUMlOoGQHmsE45ViQ2KuBuASoYC4Wf+OUYxz6mQkgwAAN9mIrUReCXgAAAABJRU5ErkJggg==") no-repeat center;
}

.handler_ok_bg {
  background: #fff url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3hpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNS1jMDIxIDc5LjE1NTc3MiwgMjAxNC8wMS8xMy0xOTo0NDowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo0ZDhlNWY5My05NmI0LTRlNWQtOGFjYi03ZTY4OGYyMTU2ZTYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NDlBRDI3NjVGMkQ2MTFFNEI5NDBCMjQ2M0ExMDQ1OUYiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NDlBRDI3NjRGMkQ2MTFFNEI5NDBCMjQ2M0ExMDQ1OUYiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTQgKE1hY2ludG9zaCkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDphNWEzMWNhMC1hYmViLTQxNWEtYTEwZS04Y2U5NzRlN2Q4YTEiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6NGQ4ZTVmOTMtOTZiNC00ZTVkLThhY2ItN2U2ODhmMjE1NmU2Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+k+sHwwAAASZJREFUeNpi/P//PwMyKD8uZw+kUoDYEYgloMIvgHg/EM/ptHx0EFk9I8wAoEZ+IDUPiIMY8IN1QJwENOgj3ACo5gNAbMBAHLgAxA4gQ5igAnNJ0MwAVTsX7IKyY7L2UNuJAf+AmAmJ78AEDTBiwGYg5gbifCSxFCZoaBMCy4A4GOjnH0D6DpK4IxNSVIHAfSDOAeLraJrjgJp/AwPbHMhejiQnwYRmUzNQ4VQgDQqXK0ia/0I17wJiPmQNTNBEAgMlQIWiQA2vgWw7QppBekGxsAjIiEUSBNnsBDWEAY9mEFgMMgBk00E0iZtA7AHEctDQ58MRuA6wlLgGFMoMpIG1QFeGwAIxGZo8GUhIysmwQGSAZgwHaEZhICIzOaBkJkqyM0CAAQDGx279Jf50AAAAAABJRU5ErkJggg==") no-repeat center;
}

.drag_bg {
  background-color: #52c41a; /* 匹配Home.vue的绿色 */
  height: 34px;
  width: 0px;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.drag_text {
  position: absolute;
  top: 0px;
  width: 100%;
  text-align: center;
  -moz-user-select: none;
  -webkit-user-select: none;
  user-select: none;
  -o-user-select: none;
  -ms-user-select: none;
  z-index: 0;
  color: #666;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  background: #d9d9d9;
}
</style>
