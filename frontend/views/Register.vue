<template>
  <main class="auth-page">
    <section class="auth-shell">
      <div class="brand-copy">
        <button class="back-link" type="button" @click="router.push('/auth')">返回入口</button>
        <p class="eyebrow">Create Account</p>
        <h1>注册宿舍管理账号</h1>
        <p>
          <span class="typewriter">{{ typedIntro }}</span>
          <span class="caret" aria-hidden="true"></span>
        </p>
      </div>

      <section class="auth-card register-card">
        <div class="card-title">
          <span class="card-mark"></span>
          <div>
            <h2>账号注册</h2>
            <p>请填写真实信息，便于后续住宿业务匹配</p>
          </div>
        </div>

        <el-form ref="registerFormRef" :model="formData" :rules="rules" label-position="top" class="auth-form">
          <div class="form-grid">
            <el-form-item label="用户名" prop="username">
              <el-input v-model.trim="formData.username" size="large" placeholder="3-20 位账号" maxlength="20" clearable />
            </el-form-item>

            <el-form-item label="姓名" prop="name">
              <el-input v-model.trim="formData.name" size="large" placeholder="请输入姓名" maxlength="20" clearable />
            </el-form-item>

            <el-form-item label="学号 / 工号" prop="studentId">
              <el-input
                v-model.trim="formData.studentId"
                size="large"
                placeholder="请输入学号或工号"
                maxlength="30"
                clearable
              />
            </el-form-item>

            <el-form-item label="角色" prop="role">
              <el-select v-model="formData.role" size="large" placeholder="请选择角色" class="full-width">
                <el-option label="学生" value="student" />
              </el-select>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="formData.phone" size="large" placeholder="请输入手机号" maxlength="11" clearable />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model.trim="formData.email" size="large" placeholder="请输入邮箱" maxlength="50" clearable />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="formData.password"
                size="large"
                placeholder="至少 6 位"
                type="password"
                show-password
                maxlength="20"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="formData.confirmPassword"
                size="large"
                placeholder="请再次输入密码"
                type="password"
                show-password
                maxlength="20"
              />
            </el-form-item>
          </div>

          <el-button type="primary" size="large" class="submit-button" :loading="isLoading" @click="handleRegister">
            注册账号
          </el-button>
        </el-form>

        <div class="card-footer">
          <span>已有账号？</span>
          <el-link type="primary" :underline="false" @click="router.push('/login')">立即登录</el-link>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '../api/adminApi'

const router = useRouter()
const registerFormRef = ref(null)
const isLoading = ref(false)
const registerIntro = '学生先完成账号登记，之后即可登录提交报修、来访登记和换宿申请。'
const typedIntro = ref('')
let typeTimer = null

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  studentId: '',
  email: '',
  phone: '',
  role: 'student'
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
    return
  }
  if (value !== formData.password) {
    callback(new Error('两次密码输入不一致'))
    return
  }
  callback()
}

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: ['blur', 'change'] }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号或工号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const startTypewriter = () => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduceMotion) {
    typedIntro.value = registerIntro
    return
  }

  let index = 0
  const type = () => {
    typedIntro.value = registerIntro.slice(0, index)
    index += 1
    if (index <= registerIntro.length) {
      typeTimer = window.setTimeout(type, 44)
    }
  }
  typeTimer = window.setTimeout(type, 260)
}

onMounted(startTypewriter)

onUnmounted(() => {
  if (typeTimer) window.clearTimeout(typeTimer)
})

const handleRegister = async () => {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    isLoading.value = true
    const registerData = {
      username: formData.username,
      password: formData.password,
      name: formData.name,
      studentId: formData.studentId,
      email: formData.email,
      phone: formData.phone,
      role: formData.role.toUpperCase()
    }

    const response = await adminApi.registerUser(registerData)

    if (response.status === 200 || response.success || typeof response === 'string') {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    const message = error.response?.data?.message || error.message || '注册失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    isLoading.value = false
  }
}
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
  width: min(1220px, calc(100% - 48px));
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 560px;
  align-items: center;
  gap: 58px;
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
  background: rgba(248, 251, 255, 0.9);
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
  margin-bottom: 22px;
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.full-width {
  width: 100%;
}

.submit-button {
  width: 100%;
  height: 46px;
  margin-top: 4px;
  border-radius: 6px;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(29, 111, 233, 0.24);
}

.card-footer {
  display: flex;
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

@media (max-width: 960px) {
  .auth-shell {
    min-height: auto;
    grid-template-columns: 1fr;
    gap: 28px;
    width: min(100% - 32px, 620px);
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
}

@media (max-width: 620px) {
  .auth-card {
    padding: 24px;
  }

  .form-grid {
    grid-template-columns: 1fr;
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
  .submit-button,
  .auth-form :deep(.el-input__wrapper),
  .auth-form :deep(.el-select__wrapper) {
    transition: none;
  }
}
</style>
