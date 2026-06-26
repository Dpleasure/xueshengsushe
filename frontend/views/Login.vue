<template>
  <main class="auth-page">
    <section class="auth-shell">
      <div class="brand-copy">
        <button class="back-link" type="button" @click="router.push('/auth')">返回入口</button>
        <p class="eyebrow">Student Residence</p>
        <h1>登录学生宿舍管理系统</h1>
        <p>
          <span class="typewriter">{{ typedIntro }}</span>
          <span class="caret" aria-hidden="true"></span>
        </p>
      </div>

      <section class="auth-card">
        <div class="card-title">
          <span class="card-mark"></span>
          <div>
            <h2>账号登录</h2>
            <p>请输入账号信息完成身份验证</p>
          </div>
        </div>

        <el-form ref="loginFormRef" :model="form" :rules="rules" label-position="top" class="auth-form">
          <el-form-item label="用户名" prop="username">
            <el-input v-model.trim="form.username" size="large" placeholder="请输入账号" clearable />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              size="large"
              placeholder="请输入密码"
              type="password"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="学号 / 工号" prop="studentId">
            <el-input v-model.trim="form.studentId" size="large" placeholder="请输入学号或工号" clearable />
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-select v-model="form.role" size="large" placeholder="请选择角色" class="full-width">
              <el-option label="管理员" value="admin" />
              <el-option label="学生" value="student" />
            </el-select>
          </el-form-item>

          <div class="verify-label">滑动验证</div>
          <div
            ref="dragDiv"
            class="drag-verify"
            :class="{ verified: confirmSuccess }"
            @mouseleave="handleMouseUp"
          >
            <div class="drag-bg" :style="{ width: `${dragWidth}px` }"></div>
            <div class="drag-text">{{ confirmWords }}</div>
            <button
              ref="moveDiv"
              class="drag-handler"
              :class="{ verified: confirmSuccess }"
              type="button"
              :style="{ transform: `translateX(${handlerLeft}px)` }"
              @mousedown="handleMouseDown"
              @touchstart.prevent="handleTouchStart"
            >
              {{ confirmSuccess ? '✓' : '›' }}
            </button>
          </div>

          <el-button
            type="primary"
            size="large"
            class="submit-button"
            :loading="isSubmitting"
            :disabled="!confirmSuccess"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <div class="card-footer">
          <el-link type="primary" :underline="false" @click="showForgotPassword = true">忘记密码？</el-link>
          <span>还没有账号？</span>
          <el-link type="primary" :underline="false" @click="router.push('/register')">立即注册</el-link>
        </div>
      </section>
    </section>

    <el-dialog v-model="showForgotPassword" title="重置管理员密码" width="420px" class="forgot-dialog">
      <el-form ref="forgotFormRef" :model="forgotPasswordForm" :rules="forgotRules" label-position="top">
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model.trim="forgotPasswordForm.username" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="forgotPasswordForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="forgotPasswordForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotPasswordForm.newPassword"
            placeholder="请输入新密码"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showForgotPassword = false">取消</el-button>
        <el-button type="primary" :loading="isResetting" @click="handleForgotPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { post } from '@/net/run'

const router = useRouter()

const loginFormRef = ref(null)
const forgotFormRef = ref(null)
const dragDiv = ref(null)
const moveDiv = ref(null)

const form = reactive({
  username: '',
  password: '',
  studentId: '',
  role: ''
})

const forgotPasswordForm = reactive({
  username: '',
  email: '',
  phone: '',
  newPassword: ''
})

const showForgotPassword = ref(false)
const isSubmitting = ref(false)
const isResetting = ref(false)
const beginClientX = ref(0)
const mouseMoving = ref(false)
const maxWidth = ref(0)
const handlerLeft = ref(0)
const confirmSuccess = ref(false)
const loginIntro = '处理住宿、报修、来访和换宿申请，从身份确认开始。'
const typedIntro = ref('')
let typeTimer = null

const confirmWords = computed(() => (confirmSuccess.value ? '验证通过' : '按住滑块拖动到最右侧'))
const dragWidth = computed(() => handlerLeft.value + (moveDiv.value?.offsetWidth || 0))

const startTypewriter = () => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduceMotion) {
    typedIntro.value = loginIntro
    return
  }

  let index = 0
  const type = () => {
    typedIntro.value = loginIntro.slice(0, index)
    index += 1
    if (index <= loginIntro.length) {
      typeTimer = window.setTimeout(type, 44)
    }
  }
  typeTimer = window.setTimeout(type, 260)
}

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号或工号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const forgotRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ]
}

const updateMaxWidth = () => {
  if (dragDiv.value && moveDiv.value) {
    maxWidth.value = dragDiv.value.clientWidth - moveDiv.value.clientWidth
  }
}

const handleMouseDown = (event) => {
  if (confirmSuccess.value) return
  updateMaxWidth()
  mouseMoving.value = true
  beginClientX.value = event.clientX - handlerLeft.value
}

const handleTouchStart = (event) => {
  if (confirmSuccess.value) return
  updateMaxWidth()
  mouseMoving.value = true
  beginClientX.value = event.touches[0].clientX - handlerLeft.value
}

const moveHandler = (clientX) => {
  if (!mouseMoving.value || confirmSuccess.value) return
  const nextLeft = Math.min(Math.max(clientX - beginClientX.value, 0), maxWidth.value)
  handlerLeft.value = nextLeft

  if (nextLeft >= maxWidth.value - 2) {
    confirmSuccess.value = true
    handlerLeft.value = maxWidth.value
    mouseMoving.value = false
  }
}

const handleMouseMove = (event) => moveHandler(event.clientX)
const handleTouchMove = (event) => moveHandler(event.touches[0].clientX)

const handleMouseUp = () => {
  if (confirmSuccess.value) return
  mouseMoving.value = false
  handlerLeft.value = 0
}

const resetCaptcha = () => {
  confirmSuccess.value = false
  mouseMoving.value = false
  handlerLeft.value = 0
  nextTick(updateMaxWidth)
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!confirmSuccess.value) {
    ElMessage.warning('请先完成滑动验证')
    return
  }

  isSubmitting.value = true
  post(
    '/api/auth/login',
    {
      username: form.username,
      password: form.password,
      studentId: form.studentId,
      role: form.role
    },
    (responseData) => {
      localStorage.setItem('token', responseData.token)
      localStorage.setItem(
        'userInfo',
        JSON.stringify({
          username: responseData.username,
          name: responseData.name,
          role: responseData.role,
          studentId: responseData.studentId
        })
      )
      ElMessage.success(responseData.message || '登录成功')
      router.push('/home')
      isSubmitting.value = false
    },
    (message) => {
      ElMessage.error(message || '登录失败，请检查用户名、密码和角色')
      resetCaptcha()
      isSubmitting.value = false
    },
    (error) => {
      ElMessage.error(error?.response?.data?.message || '登录请求失败，请稍后重试')
      resetCaptcha()
      isSubmitting.value = false
    }
  )
}

const handleForgotPassword = async () => {
  const valid = await forgotFormRef.value?.validate().catch(() => false)
  if (!valid) return

  isResetting.value = true
  post(
    '/api/auth/forgot-password',
    { ...forgotPasswordForm },
    (data) => {
      ElMessage.success(data || '密码已重置')
      showForgotPassword.value = false
      isResetting.value = false
    },
    (message) => {
      ElMessage.error(message || '密码重置失败')
      isResetting.value = false
    },
    (error) => {
      ElMessage.error(error?.response?.data?.message || '密码重置请求失败')
      isResetting.value = false
    }
  )
}

onMounted(() => {
  startTypewriter()
  nextTick(updateMaxWidth)
  window.addEventListener('resize', updateMaxWidth)
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseup', handleMouseUp)
  window.addEventListener('touchmove', handleTouchMove)
  window.addEventListener('touchend', handleMouseUp)
})

onUnmounted(() => {
  if (typeTimer) window.clearTimeout(typeTimer)
  window.removeEventListener('resize', updateMaxWidth)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseup', handleMouseUp)
  window.removeEventListener('touchmove', handleTouchMove)
  window.removeEventListener('touchend', handleMouseUp)
})
</script>

<style scoped>
.auth-page {
  --ink: #f8fbff;
  --muted: rgba(226, 235, 246, 0.84);
  --blue: #1d6fe9;
  --cyan: #1db7a6;
  --gold: #f5b84b;
  min-height: 100vh;
  background:
    radial-gradient(circle at 18% 18%, rgba(245, 184, 75, 0.26), transparent 28%),
    radial-gradient(circle at 82% 76%, rgba(29, 183, 166, 0.22), transparent 30%),
    linear-gradient(90deg, rgba(8, 27, 51, 0.92), rgba(14, 42, 71, 0.56) 46%, rgba(8, 27, 51, 0.86)),
    url('@/assets/images/auth-campus-dusk.jpg') center / cover no-repeat;
  color: var(--ink);
}

.auth-shell {
  min-height: 100vh;
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 430px;
  align-items: center;
  gap: 64px;
  padding: 40px 0;
  animation: pageIn 0.7s ease both;
}

.brand-copy {
  max-width: 610px;
  animation: riseIn 0.72s ease 0.08s both;
}

.back-link {
  height: 36px;
  padding: 0 14px;
  margin-bottom: 54px;
  border: 1px solid rgba(255, 255, 255, 0.36);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.12);
  color: #f8fafc;
  cursor: pointer;
  backdrop-filter: blur(12px);
  transition: transform 0.2s ease, border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.back-link:hover {
  transform: translateY(-2px);
  border-color: rgba(245, 184, 75, 0.72);
  background: rgba(245, 184, 75, 0.14);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.18);
}

.eyebrow {
  margin: 0 0 16px;
  color: var(--gold);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.brand-copy h1 {
  margin: 0;
  font-size: clamp(40px, 5.4vw, 68px);
  line-height: 1.08;
  font-weight: 800;
  letter-spacing: 0;
  text-shadow: 0 18px 48px rgba(0, 0, 0, 0.4);
}

.brand-copy p {
  min-height: 66px;
  max-width: 520px;
  margin: 24px 0 0;
  color: var(--muted);
  font-size: 18px;
  line-height: 1.8;
}

.typewriter {
  background: linear-gradient(90deg, #ffffff, #dff8ff 45%, #ffe5a8);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.caret {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 3px;
  transform: translateY(3px);
  background: var(--gold);
  animation: blink 0.8s steps(2, start) infinite;
}

.auth-card {
  width: 100%;
  padding: 30px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 8px;
  background: rgba(248, 251, 255, 0.88);
  box-shadow: 0 30px 90px rgba(4, 13, 28, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(20px);
  color: #172033;
  animation: cardIn 0.72s ease 0.18s both;
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.auth-card:hover {
  transform: translateY(-4px);
  border-color: rgba(245, 184, 75, 0.62);
  box-shadow: 0 38px 110px rgba(4, 13, 28, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.62);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.card-mark {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--blue), var(--cyan) 54%, var(--gold));
  box-shadow: 0 14px 32px rgba(29, 111, 233, 0.3);
}

.card-title h2 {
  margin: 0 0 4px;
  color: #121826;
  font-size: 26px;
}

.card-title p {
  margin: 0;
  color: #5b6475;
  font-size: 14px;
}

.auth-form :deep(.el-form-item__label) {
  color: #263244;
  font-weight: 700;
}

.auth-form :deep(.el-input__wrapper),
.auth-form :deep(.el-select__wrapper) {
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.auth-form :deep(.el-input__wrapper:hover),
.auth-form :deep(.el-select__wrapper:hover),
.auth-form :deep(.el-input__wrapper.is-focus),
.auth-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--blue) inset, 0 8px 20px rgba(29, 111, 233, 0.12);
}

.full-width {
  width: 100%;
}

.verify-label {
  margin: 2px 0 8px;
  color: #263244;
  font-size: 14px;
  font-weight: 700;
}

.drag-verify {
  position: relative;
  height: 42px;
  margin-bottom: 22px;
  overflow: hidden;
  border: 1px solid #d8dee8;
  border-radius: 6px;
  background: rgba(248, 250, 252, 0.92);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.drag-verify:hover {
  border-color: rgba(29, 111, 233, 0.52);
  box-shadow: 0 10px 24px rgba(29, 111, 233, 0.12);
}

.drag-bg {
  position: absolute;
  inset: 0 auto 0 0;
  width: 0;
  background: linear-gradient(90deg, var(--blue), var(--cyan));
}

.drag-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #687386;
  font-size: 14px;
  user-select: none;
}

.drag-verify.verified .drag-text {
  color: #ffffff;
  font-weight: 700;
}

.drag-handler {
  position: absolute;
  top: 2px;
  left: 2px;
  z-index: 2;
  width: 38px;
  height: 36px;
  border: 1px solid #d8dee8;
  border-radius: 5px;
  background: #ffffff;
  color: #1f6feb;
  font-size: 24px;
  line-height: 1;
  cursor: grab;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s ease;
}

.drag-handler:hover {
  box-shadow: 0 8px 18px rgba(29, 111, 233, 0.18);
}

.drag-handler.verified {
  color: #18a058;
  cursor: default;
}

.submit-button {
  width: 100%;
  height: 46px;
  border-radius: 6px;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(29, 111, 233, 0.24);
}

.submit-button:deep(.el-button) {
  width: 100%;
}

.card-footer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  color: #687386;
  font-size: 14px;
}

:deep(.el-button--primary) {
  --el-button-bg-color: var(--blue);
  --el-button-border-color: var(--blue);
  --el-button-hover-bg-color: #165bd2;
  --el-button-hover-border-color: #165bd2;
}

@keyframes pageIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes blink {
  50% { opacity: 0; }
}

@media (max-width: 860px) {
  .auth-shell {
    min-height: auto;
    grid-template-columns: 1fr;
    gap: 28px;
    width: min(100% - 32px, 520px);
    padding: 42px 0 30px;
  }

  .back-link {
    margin-bottom: 34px;
  }

  .brand-copy h1 {
    font-size: 38px;
  }

  .brand-copy p {
    min-height: 86px;
    font-size: 16px;
  }

  .auth-card {
    padding: 24px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-shell,
  .brand-copy,
  .auth-card,
  .caret {
    animation: none;
  }

  .auth-card,
  .back-link,
  .drag-verify,
  .drag-handler,
  .submit-button,
  .auth-form :deep(.el-input__wrapper),
  .auth-form :deep(.el-select__wrapper) {
    transition: none;
  }
}
</style>
