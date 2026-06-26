<template>
  <main class="auth-choice-page">
    <section class="auth-hero">
      <div class="hero-copy">
        <p class="eyebrow">Dormitory Management</p>
        <h1>学生宿舍管理系统</h1>
        <p class="hero-text">
          <span class="typewriter">{{ typedIntro }}</span>
          <span class="caret" aria-hidden="true"></span>
        </p>

        <div class="signal-strip" aria-label="系统能力">
          <span>住宿管理</span>
          <span>报修处理</span>
          <span>来访登记</span>
          <span>换宿申请</span>
        </div>
      </div>

      <div class="choice-card">
        <div class="card-heading">
          <span class="status-dot"></span>
          <span>请选择进入方式</span>
        </div>
        <h2>欢迎回来</h2>
        <p>已有账号可直接登录，新用户先完成注册。系统会根据角色进入对应工作台。</p>

        <div class="choice-actions">
          <el-button type="primary" size="large" @click="router.push('/login')">
            登录系统
          </el-button>
          <el-button size="large" @click="router.push('/register')">
            注册账号
          </el-button>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const introText = '统一管理住宿、报修、来访与换宿申请，让学生和管理员从同一个入口开始。'
const typedIntro = ref('')
let typeTimer = null

const startTypewriter = () => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduceMotion) {
    typedIntro.value = introText
    return
  }

  let index = 0
  const type = () => {
    typedIntro.value = introText.slice(0, index)
    index += 1
    if (index <= introText.length) {
      typeTimer = window.setTimeout(type, 44)
    }
  }
  typeTimer = window.setTimeout(type, 260)
}

onMounted(startTypewriter)

onUnmounted(() => {
  if (typeTimer) window.clearTimeout(typeTimer)
})
</script>

<style scoped>
.auth-choice-page {
  --ink: #f8fbff;
  --muted: rgba(226, 235, 246, 0.84);
  --navy: #081b33;
  --blue: #1d6fe9;
  --cyan: #1db7a6;
  --gold: #f5b84b;
  min-height: 100vh;
  background:
    radial-gradient(circle at 18% 18%, rgba(245, 184, 75, 0.28), transparent 28%),
    radial-gradient(circle at 82% 76%, rgba(29, 183, 166, 0.24), transparent 30%),
    linear-gradient(90deg, rgba(8, 27, 51, 0.92), rgba(14, 42, 71, 0.56) 48%, rgba(8, 27, 51, 0.88)),
    url('@/assets/images/auth-campus-dusk.jpg') center / cover no-repeat;
  color: var(--ink);
}

.auth-hero {
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

.hero-copy {
  max-width: 660px;
  animation: riseIn 0.72s ease 0.08s both;
}

.eyebrow {
  margin: 0 0 16px;
  color: var(--gold);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(42px, 6vw, 74px);
  line-height: 1.05;
  font-weight: 850;
  letter-spacing: 0;
  text-shadow: 0 20px 54px rgba(0, 0, 0, 0.48);
}

.hero-text {
  min-height: 66px;
  max-width: 560px;
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

.signal-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 26px;
}

.signal-strip span {
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 12px 26px rgba(0, 0, 0, 0.16);
  color: rgba(248, 251, 255, 0.92);
  font-size: 13px;
  backdrop-filter: blur(10px);
  transition: transform 0.22s ease, border-color 0.22s ease, background 0.22s ease;
}

.signal-strip span:hover {
  transform: translateY(-3px);
  border-color: rgba(245, 184, 75, 0.7);
  background: rgba(245, 184, 75, 0.16);
}

.choice-card {
  width: 100%;
  padding: 34px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 8px;
  background: rgba(248, 251, 255, 0.88);
  box-shadow: 0 30px 90px rgba(4, 13, 28, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(20px);
  color: #142033;
  animation: cardIn 0.72s ease 0.18s both;
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.choice-card:hover {
  transform: translateY(-4px);
  border-color: rgba(245, 184, 75, 0.62);
  box-shadow: 0 38px 110px rgba(4, 13, 28, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.62);
}

.card-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #526174;
  font-size: 14px;
  font-weight: 800;
}

.status-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--cyan);
  animation: pulse 1.8s ease infinite;
}

.choice-card h2 {
  margin: 24px 0 10px;
  color: #111827;
  font-size: 30px;
  line-height: 1.2;
}

.choice-card p {
  margin: 0;
  color: #526174;
  line-height: 1.7;
}

.choice-actions {
  display: grid;
  gap: 14px;
  margin-top: 30px;
}

.choice-actions :deep(.el-button) {
  width: 100%;
  height: 46px;
  margin-left: 0;
  border-radius: 6px;
  font-weight: 800;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.choice-actions :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(29, 111, 233, 0.24);
}

.choice-actions :deep(.el-button--primary) {
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

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(29, 183, 166, 0.34); }
  70% { box-shadow: 0 0 0 10px rgba(29, 183, 166, 0); }
  100% { box-shadow: 0 0 0 0 rgba(29, 183, 166, 0); }
}

@keyframes blink {
  50% { opacity: 0; }
}

@media (max-width: 860px) {
  .auth-hero {
    min-height: auto;
    grid-template-columns: 1fr;
    gap: 32px;
    width: min(100% - 32px, 540px);
    padding: 56px 0 36px;
  }

  .hero-copy h1 {
    font-size: 42px;
  }

  .hero-text {
    min-height: 86px;
    font-size: 16px;
  }

  .choice-card {
    padding: 26px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-hero,
  .hero-copy,
  .choice-card,
  .status-dot,
  .caret {
    animation: none;
  }

  .choice-card,
  .signal-strip span,
  .choice-actions :deep(.el-button) {
    transition: none;
  }
}
</style>
