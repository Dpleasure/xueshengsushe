<template>
  <div class="register-container">
    <div class="register-box">
      <h1 class="register-title">学生宿舍管理系统 - 注册</h1>
      <form class="register-form" @submit.prevent="handleRegister">
        <div class="input-group">
          <span class="input-icon">👤</span>
          <input 
            type="text" 
            class="register-input" 
            placeholder="请输入账号"
            v-model="formData.username"
            maxlength="20"
          >
        </div>
        <div class="input-group">
          <span class="input-icon">🔒</span>
          <input 
            type="password" 
            class="register-input" 
            placeholder="请输入密码"
            v-model="formData.password"
            maxlength="20"
          >
        </div>
        <div class="input-group">
          <span class="input-icon">🔑</span>
          <input 
            type="password" 
            class="register-input" 
            placeholder="请确认密码"
            v-model="formData.confirmPassword"
            maxlength="20"
          >
        </div>
        <div class="input-group">
          <span class="input-icon">📧</span>
          <input 
            type="email" 
            class="register-input" 
            placeholder="请输入邮箱"
            v-model="formData.email"
            maxlength="50"
          >
        </div>
        <div class="input-group">
          <span class="input-icon">👨‍🎓</span>
          <input 
            type="text" 
            class="register-input" 
            placeholder="请输入姓名"
            v-model="formData.name"
            maxlength="20"
          >
        </div>
        <div class="input-group">
          <span class="input-icon">📱</span>
          <input 
            type="tel" 
            class="register-input" 
            placeholder="请输入手机号"
            v-model="formData.phone"
            maxlength="11"
          >
        </div>
        <select class="register-select" v-model="formData.role">
          <option value="">请选择角色</option>
          <option value="student">学生</option>
        </select>
        <button type="submit" class="register-btn" :disabled="isLoading">
          {{ isLoading ? '注册中...' : '注 册' }}
        </button>
      </form>
      <div class="register-footer">
        已有账号？<router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../api/adminApi'

const router = useRouter()

const formData = ref({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  email: '',
  phone: '',
  role: ''
})

const isLoading = ref(false)

const handleRegister = async () => {
  // 表单验证
  if (!formData.value.username) {
    alert('请输入账号')
    return
  }
  if (formData.value.username.length < 3) {
    alert('账号长度至少3个字符')
    return
  }
  if (!formData.value.password) {
    alert('请输入密码')
    return
  }
  if (formData.value.password.length < 6) {
    alert('密码长度至少6个字符')
    return
  }
  if (!formData.value.confirmPassword) {
    alert('请确认密码')
    return
  }
  if (formData.value.password !== formData.value.confirmPassword) {
    alert('两次密码输入不一致')
    return
  }
  if (!formData.value.name) {
    alert('请输入姓名')
    return
  }
  if (!formData.value.email) {
    alert('请输入邮箱')
    return
  }
  if (!formData.value.phone) {
    alert('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(formData.value.phone)) {
    alert('请输入正确的手机号')
    return
  }
  if (!formData.value.role) {
    alert('请选择角色')
    return
  }

  try {
    isLoading.value = true
    
    // 准备注册数据
    const registerData = {
      username: formData.value.username,
      password: formData.value.password,
      name: formData.value.name,
      email: formData.value.email,
      phone: formData.value.phone,
      role: formData.value.role.toUpperCase() // 转换为大写，如 'student' -> 'STUDENT'
    }
    
    // 调用注册API - 修正方法名为 registerUser
    const response = await adminApi.registerUser(registerData)
    
    if (response.status === 200 || response.success) {
      alert('注册成功！请登录')
      // 跳转到登录页面
      router.push('/login')
    } else {
      alert(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    const msg = error.response?.data?.message || '注册失败，请稍后重试'
    alert(msg)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 450px;
  margin: 20px 0;
}

.register-title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
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

.register-input {
  width: 100%;
  padding: 12px 12px 12px 45px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  outline: none;
}

.register-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.register-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  cursor: pointer;
  transition: all 0.3s;
}

.register-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.register-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.login-link:hover {
  text-decoration: underline;
}
</style>
