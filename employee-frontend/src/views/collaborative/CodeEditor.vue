<template>
  <div class="code-editor">
    <div class="editor-header">
      <div class="document-info">
        <h3>{{ document?.title || '新建代码文档' }}</h3>
        <div class="document-meta">
          <span class="version">版本 {{ document?.version || 1 }}</span>
          <span class="language">语言: {{ currentLanguage }}</span>
          <span class="type">类型: 代码</span>
        </div>
      </div>
      
      <div class="editor-actions">
        <el-select v-model="currentLanguage" @change="changeLanguage" style="width: 150px; margin-right: 12px;">
          <el-option label="JavaScript" value="javascript"></el-option>
          <el-option label="TypeScript" value="typescript"></el-option>
          <el-option label="Python" value="python"></el-option>
          <el-option label="Java" value="java"></el-option>
          <el-option label="C++" value="cpp"></el-option>
          <el-option label="HTML" value="html"></el-option>
          <el-option label="CSS" value="css"></el-option>
          <el-option label="SQL" value="sql"></el-option>
          <el-option label="JSON" value="json"></el-option>
        </el-select>
        
        <button @click="formatCode" class="format-btn">
          <i class="fa fa-magic"></i> 格式化
        </button>
        <button @click="saveDocument" :disabled="!hasChanges" class="save-btn">
          <i class="fa fa-save"></i> 保存
        </button>
        <button @click="shareDocument" class="share-btn">
          <i class="fa fa-share"></i> 分享
        </button>
        <button @click="runCode" class="run-btn" v-if="canRunCode">
          <i class="fa fa-play"></i> 运行
        </button>
      </div>
    </div>

    <!-- 用户在线状态和光标显示 -->
    <UserPresence 
      :online-users="onlineUsers"
      :current-user-id="sessionId"
      :editor-container="'#monaco-editor'"
      ref="userPresence"
    />

    <!-- Monaco编辑器容器 -->
    <div class="editor-container">
      <div id="monaco-editor" class="monaco-editor-wrapper"></div>
    </div>

    <!-- 控制台输出（如果支持运行代码） -->
    <div v-if="showConsole" class="console-panel">
      <div class="console-header">
        <span>控制台输出</span>
        <button @click="clearConsole" class="clear-btn">清空</button>
      </div>
      <div class="console-content">
        <div 
          v-for="(log, index) in consoleLogs" 
          :key="index" 
          :class="['console-line', log.type]"
        >
          <span class="timestamp">{{ formatTime(log.timestamp) }}</span>
          <span class="message">{{ log.message }}</span>
        </div>
      </div>
    </div>

    <!-- 编辑器状态 -->
    <div class="editor-status">
      <div class="connection-status">
        <span :class="['status-indicator', connectionStatus]"></span>
        {{ connectionStatusText }}
      </div>
      <div class="editor-info">
        <span>行: {{ currentLine }}</span>
        <span>列: {{ currentColumn }}</span>
        <span>选中: {{ selectedText }}</span>
        <span>语言: {{ currentLanguage }}</span>
      </div>
      <div class="auto-save-status" v-if="autoSaveStatus">
        {{ autoSaveStatus }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import collaborativeApi from '@/api/collaborative'
import { ElMessage, ElSelect, ElOption } from 'element-plus'
import { useDebounceFn } from '@vueuse/core'
import * as monaco from 'monaco-editor'
import dayjs from 'dayjs'

import UserPresence from '@/components/collaborative/UserPresence.vue'

// Props
const props = defineProps({
  documentId: {
    type: [String, Number],
    default: null
  },
  readOnly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['documentLoaded', 'documentSaved', 'error'])

// 状态管理
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const document = ref(null)
const monacoEditor = ref(null)
const hasChanges = ref(false)
const currentLanguage = ref('javascript')
const connectionStatus = ref('connected')
const autoSaveStatus = ref('')
const onlineUsers = ref([])
const showConsole = ref(false)
const consoleLogs = ref([])
const currentLine = ref(1)
const currentColumn = ref(1)
const selectedText = ref('')

// WebSocket 连接
const websocket = ref(null)
const sessionId = ref(generateSessionId())
const isTyping = ref(false)
const lastContent = ref('')

// 计算属性
const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected': return '已连接'
    case 'connecting': return '连接中...'
    case 'disconnected': return '连接断开'
    default: return '未知状态'
  }
})

const canRunCode = computed(() => {
  return ['javascript', 'typescript', 'python'].includes(currentLanguage.value)
})

// 生命周期
onMounted(async () => {
  await initializeEditor()
})

onUnmounted(() => {
  cleanup()
})

// 监听路由变化
watch(() => route.params.id, async (newId) => {
  if (newId) {
    await loadDocument(newId)
  }
}, { immediate: true })

// 方法
async function initializeEditor() {
  try {
    // 初始化Monaco编辑器
    await nextTick()
    
    // 安全检查document对象和getElementById方法
    if (typeof document !== 'undefined' && document.getElementById && typeof document.getElementById === 'function') {
      monacoEditor.value = monaco.editor.create(document.getElementById('monaco-editor'), {
        value: '// 开始编写您的代码\nconsole.log("Hello, World!");',
        language: currentLanguage.value,
        theme: 'vs-dark',
        readOnly: props.readOnly,
        automaticLayout: true,
        minimap: { enabled: true },
        scrollBeyondLastLine: false,
        fontSize: 14,
        lineNumbers: 'on',
        renderWhitespace: 'selection',
        selectOnLineNumbers: true,
        roundedSelection: false,
        cursorStyle: 'line',
        accessibilitySupport: 'off'
      })
    } else {
      console.error('无法访问document.getElementById方法')
      ElMessage.error('编辑器初始化失败：无法访问DOM元素')
      return
    }

    // 设置编辑器事件
    setupEditorEvents()

    // 加载文档
    const docId = props.documentId || route.params.id
    if (docId) {
      await loadDocument(docId)
    }

    // 建立 WebSocket 连接
    if (!props.readOnly) {
      connectWebSocket()
    }

  } catch (error) {
    console.error('初始化代码编辑器失败:', error)
    ElMessage.error('编辑器初始化失败')
    emit('error', error)
  }
}

function setupEditorEvents() {
  if (!monacoEditor.value) return

  // 内容变化事件
  monacoEditor.value.onDidChangeModelContent((event) => {
    hasChanges.value = true
    isTyping.value = true
    
    // 发送变更到其他用户
    if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
      sendCodeChange({
        type: 'code-change',
        content: monacoEditor.value.getValue(),
        language: currentLanguage.value,
        sessionId: sessionId.value,
        timestamp: Date.now()
      })
    }

    // 防抖自动保存
    debouncedAutoSave()
  })

  // 光标位置变化事件
  monacoEditor.value.onDidChangeCursorPosition((event) => {
    currentLine.value = event.position.lineNumber
    currentColumn.value = event.position.column
    
    // 发送光标位置更新
    if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
      sendCursorUpdate({
        position: event.position,
        sessionId: sessionId.value,
        userId: userStore.user?.id,
        userName: userStore.user?.name,
        timestamp: Date.now()
      })
    }
  })

  // 选择变化事件
  monacoEditor.value.onDidChangeCursorSelection((event) => {
    const selection = monacoEditor.value.getModel().getValueInRange(event.selection)
    selectedText.value = selection.length > 0 ? `${selection.length} 字符` : ''
  })
}

async function loadDocument(docId) {
  try {
    const response = await collaborativeApi.getDocument(docId, userStore.user?.id)
    document.value = response.data

    // 设置编辑器内容和语言
    if (document.value.content) {
      monacoEditor.value.setValue(document.value.content)
      lastContent.value = document.value.content
    }

    if (document.value.language) {
      currentLanguage.value = document.value.language
      monaco.editor.setModelLanguage(monacoEditor.value.getModel(), currentLanguage.value)
    }

    hasChanges.value = false
    emit('documentLoaded', document.value)

  } catch (error) {
    console.error('加载代码文档失败:', error)
    ElMessage.error('加载文档失败')
    emit('error', error)
  }
}

async function saveDocument() {
  if (!hasChanges.value || !monacoEditor.value) return

  try {
    autoSaveStatus.value = '保存中...'
    
    const content = monacoEditor.value.getValue()
    const currentVersion = document.value?.version || 1

    if (document.value?.id) {
      // 更新现有文档
      await collaborativeApi.syncDocument(document.value.id, {
        content: content,
        language: currentLanguage.value,
        version: currentVersion
      })
    } else {
      // 创建新文档
      const newDoc = await collaborativeApi.createDocument({
        title: `新建${getLanguageDisplayName(currentLanguage.value)}文档`,
        documentType: 'code',
        content: content,
        language: currentLanguage.value,
        userId: userStore.user?.id
      })
      document.value = newDoc.data
      router.replace(`/collaborative/code/${document.value.id}`)
    }

    hasChanges.value = false
    lastContent.value = content
    autoSaveStatus.value = '已保存'
    
    setTimeout(() => {
      autoSaveStatus.value = ''
    }, 2000)

    emit('documentSaved', document.value)
    ElMessage.success('文档保存成功')

  } catch (error) {
    console.error('保存代码文档失败:', error)
    autoSaveStatus.value = '保存失败'
    ElMessage.error('保存文档失败')
    emit('error', error)
  }
}

// 防抖自动保存
const debouncedAutoSave = useDebounceFn(() => {
  if (hasChanges.value) {
    saveDocument()
  }
}, 5000)

function changeLanguage(newLanguage) {
  if (monacoEditor.value) {
    monaco.editor.setModelLanguage(monacoEditor.value.getModel(), newLanguage)
    hasChanges.value = true
  }
}

function formatCode() {
  if (monacoEditor.value) {
    monacoEditor.value.getAction('editor.action.formatDocument').run()
    ElMessage.success('代码格式化完成')
  }
}

function shareDocument() {
  if (!document.value?.id) {
    ElMessage.warning('请先保存文档')
    return
  }

  const shareUrl = `${window.location.origin}/collaborative/code/${document.value.id}`
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('分享链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

function runCode() {
  if (!canRunCode.value) {
    ElMessage.warning('当前语言不支持在线运行')
    return
  }

  showConsole.value = true
  const code = monacoEditor.value.getValue()
  
  try {
    // 模拟代码执行（实际项目中需要后端支持）
    if (currentLanguage.value === 'javascript') {
      executeJavaScript(code)
    } else {
      addConsoleLog('info', `${currentLanguage.value} 代码运行功能开发中...`)
    }
  } catch (error) {
    addConsoleLog('error', `运行错误: ${error.message}`)
  }
}

function executeJavaScript(code) {
  // 重写console方法以捕获输出
  const originalConsole = {
    log: console.log,
    error: console.error,
    warn: console.warn
  }
  
  console.log = (...args) => {
    addConsoleLog('info', args.join(' '))
    originalConsole.log(...args)
  }
  
  console.error = (...args) => {
    addConsoleLog('error', args.join(' '))
    originalConsole.error(...args)
  }
  
  console.warn = (...args) => {
    addConsoleLog('warning', args.join(' '))
    originalConsole.warn(...args)
  }
  
  try {
    // 在安全的环境中执行代码（实际项目中应使用沙箱）
    eval(code)
    addConsoleLog('success', '代码执行完成')
  } catch (error) {
    addConsoleLog('error', `执行错误: ${error.message}`)
  } finally {
    // 恢复原始console方法
    console.log = originalConsole.log
    console.error = originalConsole.error
    console.warn = originalConsole.warn
  }
}

function addConsoleLog(type, message) {
  consoleLogs.value.push({
    type,
    message,
    timestamp: new Date()
  })
  
  // 限制日志数量
  if (consoleLogs.value.length > 100) {
    consoleLogs.value.shift()
  }
}

function clearConsole() {
  consoleLogs.value = []
}

// WebSocket 相关方法
function connectWebSocket() {
  try {
    // 使用正确的WebSocket URL，后端服务运行在8081端口
    const wsUrl = `ws://localhost:8081/ws-collaboration`
    websocket.value = new WebSocket(wsUrl)

    websocket.value.onopen = () => {
      connectionStatus.value = 'connected'
      console.log('代码编辑器 WebSocket 连接已建立')
      
      if (document.value?.id) {
        joinCollaboration()
      }
    }

    websocket.value.onmessage = (event) => {
      handleWebSocketMessage(JSON.parse(event.data))
    }

    websocket.value.onclose = () => {
      connectionStatus.value = 'disconnected'
      console.log('代码编辑器 WebSocket 连接已断开')
      
      setTimeout(() => {
        if (connectionStatus.value === 'disconnected') {
          connectWebSocket()
        }
      }, 3000)
    }

    websocket.value.onerror = (error) => {
      console.error('代码编辑器 WebSocket 错误:', error)
      connectionStatus.value = 'disconnected'
    }

  } catch (error) {
    console.error('代码编辑器 WebSocket 连接失败:', error)
    connectionStatus.value = 'disconnected'
  }
}

function joinCollaboration() {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN && document.value?.id) {
    const message = {
      type: 'join',
      documentId: document.value.id,
      userId: userStore.user?.id,
      userName: userStore.user?.name,
      sessionId: sessionId.value,
      userColor: generateUserColor(userStore.user?.id)
    }
    websocket.value.send(JSON.stringify(message))
  }
}

function sendCodeChange(change) {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    const message = {
      type: 'code-change',
      documentId: document.value?.id,
      change: change
    }
    websocket.value.send(JSON.stringify(message))
  }
}

function sendCursorUpdate(cursorData) {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    const message = {
      type: 'cursor',
      documentId: document.value?.id,
      cursor: cursorData
    }
    websocket.value.send(JSON.stringify(message))
  }
}

function handleWebSocketMessage(message) {
  switch (message.type) {
    case 'user_joined':
      handleUserJoined(message)
      break
    case 'user_left':
      handleUserLeft(message)
      break
    case 'code-change':
      handleRemoteCodeChange(message)
      break
    case 'cursor_update':
      handleCursorUpdate(message)
      break
    default:
      console.log('未知消息类型:', message.type)
  }
}

function handleUserJoined(message) {
  const user = {
    sessionId: message.sessionId,
    userId: message.userId,
    userName: message.userName,
    userColor: message.userColor
  }
  
  if (!onlineUsers.value.find(u => u.sessionId === user.sessionId)) {
    onlineUsers.value.push(user)
  }
}

function handleUserLeft(message) {
  onlineUsers.value = onlineUsers.value.filter(u => u.sessionId !== message.sessionId)
}

function handleRemoteCodeChange(message) {
  if (message.change.sessionId !== sessionId.value && monacoEditor.value) {
    // 应用远程代码变更
    const currentPosition = monacoEditor.value.getPosition()
    monacoEditor.value.setValue(message.change.content)
    
    // 如果语言发生变化，更新语言
    if (message.change.language !== currentLanguage.value) {
      currentLanguage.value = message.change.language
      monaco.editor.setModelLanguage(monacoEditor.value.getModel(), currentLanguage.value)
    }
    
    // 尝试恢复光标位置
    monacoEditor.value.setPosition(currentPosition)
  }
}

function handleCursorUpdate(message) {
  // 更新其他用户的光标位置
  console.log('代码编辑器光标更新:', message)
}

function cleanup() {
  if (websocket.value) {
    websocket.value.close()
  }
  
  if (monacoEditor.value) {
    monacoEditor.value.dispose()
  }
}

// 工具函数
function generateSessionId() {
  return 'code_session_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now()
}

function generateUserColor(userId) {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8']
  return colors[userId % colors.length]
}

function getLanguageDisplayName(lang) {
  const nameMap = {
    javascript: 'JavaScript',
    typescript: 'TypeScript',
    python: 'Python',
    java: 'Java',
    cpp: 'C++',
    html: 'HTML',
    css: 'CSS',
    sql: 'SQL',
    json: 'JSON'
  }
  return nameMap[lang] || lang
}

function formatTime(date) {
  return dayjs(date).format('HH:mm:ss')
}
</script>

<style scoped>
.code-editor {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #1e1e1e;
  color: #d4d4d4;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #3e3e3e;
  background: #252525;
}

.document-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #d4d4d4;
}

.document-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #858585;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.editor-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.format-btn {
  background: #0078d4;
  color: white;
}

.format-btn:hover {
  background: #106ebe;
}

.save-btn {
  background: #1890ff;
  color: white;
}

.save-btn:hover:not(:disabled) {
  background: #40a9ff;
}

.save-btn:disabled {
  background: #3e3e3e;
  color: #666;
  cursor: not-allowed;
}

.share-btn {
  background: #52c41a;
  color: white;
}

.share-btn:hover {
  background: #73d13d;
}

.run-btn {
  background: #f5222d;
  color: white;
}

.run-btn:hover {
  background: #ff4d4f;
}

.online-users {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 24px;
  background: #2d2d30;
  border-bottom: 1px solid #3e3e3e;
}

.user-avatars {
  display: flex;
  gap: 4px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.online-count {
  font-size: 12px;
  color: #858585;
}

.editor-container {
  flex: 1;
  overflow: hidden;
}

.monaco-editor-wrapper {
  width: 100%;
  height: 100%;
}

.console-panel {
  height: 200px;
  border-top: 1px solid #3e3e3e;
  background: #1e1e1e;
  display: flex;
  flex-direction: column;
}

.console-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #252525;
  border-bottom: 1px solid #3e3e3e;
  font-size: 14px;
  font-weight: 600;
}

.clear-btn {
  padding: 4px 8px;
  background: #3e3e3e;
  border: none;
  border-radius: 3px;
  color: #d4d4d4;
  cursor: pointer;
  font-size: 12px;
}

.clear-btn:hover {
  background: #4e4e4e;
}

.console-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
}

.console-line {
  margin-bottom: 4px;
  display: flex;
  gap: 8px;
}

.console-line.info {
  color: #d4d4d4;
}

.console-line.success {
  color: #73d13d;
}

.console-line.warning {
  color: #faad14;
}

.console-line.error {
  color: #ff4d4f;
}

.timestamp {
  color: #858585;
  font-size: 11px;
  flex-shrink: 0;
}

.message {
  flex: 1;
}

.editor-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 24px;
  background: #252525;
  border-top: 1px solid #3e3e3e;
  font-size: 12px;
  color: #858585;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-indicator.connected {
  background: #52c41a;
}

.status-indicator.connecting {
  background: #faad14;
}

.status-indicator.disconnected {
  background: #ff4d4f;
}

.editor-info {
  display: flex;
  gap: 16px;
}