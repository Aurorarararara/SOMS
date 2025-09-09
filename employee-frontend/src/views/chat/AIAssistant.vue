<template>
  <div class="ai-assistant">
    <div class="header">
      <el-icon><ChatDotRound /></el-icon>
      <span class="title">AI助手</span>
    </div>

    <el-card class="chat-card">
      <div class="messages" ref="messagesRef">
        <div v-for="(m, idx) in messages" :key="idx" class="message" :class="m.role">
          <div class="bubble">
            <span v-if="m.role==='assistant'" class="role-tag">AI</span>
            <span v-else class="role-tag">{{ userNameShort }}</span>
            <div class="content">{{ m.content }}</div>
          </div>
        </div>
        <div v-if="loading" class="message assistant">
          <div class="bubble typing">
            <span class="role-tag">AI</span>
            <div class="dots">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="composer">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          placeholder="请输入问题，回车发送，Shift+Enter 换行"
          @keydown.enter.exact.prevent="handleSend"
          @keydown.enter.shift.stop
        />
        <div class="actions">
          <el-select v-model="model" class="model-select" size="small">
            <el-option label="默认模型" value="" />
            <el-option label="gpt-4o-mini" value="gpt-4o-mini" />
            <el-option label="gpt-4o" value="gpt-4o" />
          </el-select>
          <el-slider v-model="temperature" :min="0" :max="1" :step="0.1" class="temp-slider" />
          <el-button type="primary" :loading="loading" @click="handleSend">
            <el-icon><Position /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Position } from '@element-plus/icons-vue'
import { chatAi } from '@/api/ai'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userNameShort = computed(() => (userStore?.userInfo?.realName || '我').slice(0,1))

const messages = ref([
  { role: 'assistant', content: '你好，我是AI助手，有什么可以帮你？' }
])
const input = ref('')
const loading = ref(false)
const model = ref('')
const temperature = ref(0.7)
const messagesRef = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const handleSend = async () => {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const payload = { message: text }
    if (model.value) payload.model = model.value
    if (typeof temperature.value === 'number') payload.temperature = temperature.value

    const { data } = await chatAi(payload)
    // 后端Result包装器，request封装已返回 {data, message}
    messages.value.push({ role: 'assistant', content: data || '（无返回内容）' })
  } catch (e) {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}
</script>

<style scoped>
.ai-assistant { padding: 16px; }
.header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.title { font-weight: 600; font-size: 18px; }
.chat-card { }
.messages { height: 50vh; overflow-y: auto; padding: 12px; background: #fafafa; border-radius: 8px; }
.message { display: flex; margin-bottom: 10px; }
.message.user { justify-content: flex-end; }
.bubble { max-width: 70%; padding: 10px 12px; border-radius: 10px; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.06); position: relative; }
.message.user .bubble { background: #eff6ff; }
.role-tag { font-size: 12px; color: #666; margin-bottom: 4px; display: inline-block; }
.content { white-space: pre-wrap; word-break: break-word; }
.composer { margin-top: 12px; }
.actions { margin-top: 8px; display: flex; align-items: center; gap: 12px; }
.model-select { width: 160px; }
.temp-slider { width: 160px; }
.typing .dots { display: inline-flex; gap: 4px; align-items: center; }
.typing .dots span { width: 6px; height: 6px; background: #999; border-radius: 50%; animation: blink 1.2s infinite; }
.typing .dots span:nth-child(2) { animation-delay: 0.2s; }
.typing .dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink { 0%, 80%, 100% { opacity: .2 } 40% { opacity: 1 } }
</style>