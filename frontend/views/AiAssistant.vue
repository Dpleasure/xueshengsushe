<template>
  <div class="ai-page">
    <section class="assistant-shell">
      <header class="assistant-header">
        <div>
          <span class="eyebrow">DeepSeek Assistant</span>
          <h1>智能助手</h1>
          <p>咨询宿舍入住、报修、来访登记、换宿申请和系统使用问题。</p>
        </div>
        <el-tag effect="plain" type="success">已接入后端代理</el-tag>
      </header>

      <div ref="messageListRef" class="message-list">
        <article
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="message.role"
        >
          <div class="message-avatar">{{ message.role === 'assistant' ? 'AI' : userInitial }}</div>
          <div class="message-bubble">
            <div class="message-role">{{ message.role === 'assistant' ? '智能助手' : '我' }}</div>
            <p>{{ message.content }}</p>
          </div>
        </article>

        <article v-if="loading" class="message-row assistant">
          <div class="message-avatar">AI</div>
          <div class="message-bubble typing">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </article>
      </div>

      <form class="composer" @submit.prevent="sendMessage">
        <el-input
          v-model="draft"
          class="composer-input"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 5 }"
          maxlength="2000"
          show-word-limit
          resize="none"
          placeholder="例如：我想提交报修申请，应该怎么操作？"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" :loading="loading" :disabled="!canSend" native-type="submit">
          发送
        </el-button>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '../api/aiApi.js'
import { readUserInfo } from '../utils/avatar.js'

const userInfo = readUserInfo()
const userInitial = computed(() => {
  const name = userInfo.name || userInfo.username || '我'
  return String(name).trim().slice(0, 1).toUpperCase()
})

const messages = ref([
  {
    id: 1,
    role: 'assistant',
    content: '你好，我可以回答宿舍管理系统相关问题，比如报修、来访登记、换宿申请、入住信息和个人资料维护。'
  }
])
const draft = ref('')
const loading = ref(false)
const messageListRef = ref(null)

const canSend = computed(() => draft.value.trim().length > 0 && !loading.value)

async function sendMessage() {
  const text = draft.value.trim()
  if (!text || loading.value) return

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: text
  })
  draft.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const result = await aiApi.chat(text)
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: result?.answer || 'DeepSeek 暂时没有返回内容。'
    })
  } catch (error) {
    const fallback = getErrorMessage(error)
    ElMessage.error(fallback)
    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: fallback
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

function getErrorMessage(error) {
  const payload = error?.response?.data
  const message = payload?.message

  if (typeof message === 'string' && message.trim()) {
    return message
  }
  if (message && typeof message.error === 'string' && message.error.trim()) {
    return message.error
  }
  if (error?.code === 'ECONNABORTED') {
    return 'AI 请求超时，请稍后重试。'
  }
  if (error?.message && error.message !== 'Request failed with status code 500') {
    return error.message
  }
  return '智能助手请求失败，请检查后端日志或 DeepSeek API Key。'
}

async function scrollToBottom() {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}
</script>

<style scoped>
.ai-page {
  min-height: 100%;
  padding: 2px;
  color: #172033;
}

.assistant-shell {
  display: grid;
  grid-template-rows: auto minmax(320px, 1fr) auto;
  height: calc(100vh - 116px);
  min-height: 560px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #dde7f6;
  border-radius: 20px;
  box-shadow: 0 16px 36px rgba(41, 56, 86, 0.08);
}

.assistant-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 26px;
  border-bottom: 1px solid #edf2f7;
  background:
    linear-gradient(135deg, rgba(79, 124, 255, 0.09), rgba(22, 155, 98, 0.07)),
    #ffffff;
}

.eyebrow {
  display: inline-block;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.assistant-header h1 {
  margin: 8px 0 8px;
  font-family: 'Outfit', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  font-size: 34px;
  line-height: 1.1;
  font-weight: 800;
}

.assistant-header p {
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 1.7;
}

.message-list {
  min-height: 0;
  padding: 24px;
  overflow-y: auto;
  background:
    linear-gradient(180deg, #fbfdff 0%, #f6f8fc 100%);
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  background: linear-gradient(135deg, #4f7cff, #169b62);
  border-radius: 14px;
  font-size: 12px;
  font-weight: 800;
}

.message-row.user .message-avatar {
  background: linear-gradient(135deg, #172033, #4f7cff);
}

.message-bubble {
  max-width: min(720px, 78%);
  padding: 13px 15px;
  color: #172033;
  background: #ffffff;
  border: 1px solid #e5edf8;
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(41, 56, 86, 0.07);
}

.message-row.user .message-bubble {
  color: #ffffff;
  background: #2563eb;
  border-color: #2563eb;
}

.message-role {
  margin-bottom: 6px;
  color: #667085;
  font-size: 12px;
  font-weight: 800;
}

.message-row.user .message-role {
  color: rgba(255, 255, 255, 0.78);
}

.message-bubble p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.75;
}

.typing {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 44px;
}

.typing span {
  width: 7px;
  height: 7px;
  background: #4f7cff;
  border-radius: 999px;
  animation: pulse 900ms infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 120ms;
}

.typing span:nth-child(3) {
  animation-delay: 240ms;
}

.composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 96px;
  gap: 12px;
  align-items: end;
  padding: 18px;
  border-top: 1px solid #edf2f7;
  background: #ffffff;
}

.composer :deep(.el-textarea__inner) {
  border-radius: 14px;
  box-shadow: none;
}

.composer .el-button {
  min-height: 54px;
  border-radius: 14px;
  font-weight: 800;
}

@keyframes pulse {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.35;
  }
  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@media (max-width: 760px) {
  .assistant-shell {
    height: auto;
    min-height: calc(100vh - 96px);
  }

  .assistant-header {
    flex-direction: column;
    padding: 20px;
  }

  .message-list {
    padding: 18px;
  }

  .message-bubble {
    max-width: 86%;
  }

  .composer {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 1ms !important;
    transition-duration: 1ms !important;
  }
}
</style>
