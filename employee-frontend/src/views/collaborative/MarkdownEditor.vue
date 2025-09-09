<template>
  <div class="markdown-editor">
    <div class="editor-header">
      <div class="document-info">
        <h3>{{ document?.title || '新建Markdown文档' }}</h3>
        <div class="document-meta">
          <span class="version">版本 {{ document?.version || 1 }}</span>
          <span class="owner">创建者: {{ document?.ownerName || '未知' }}</span>
          <span class="type">类型: Markdown</span>
        </div>
      </div>
      
      <div class="editor-actions">
        <button @click="togglePreview" :class="['preview-btn', { active: showPreview }]">
          <i class="fa fa-eye"></i> {{ showPreview ? '隐藏预览' : '预览' }}
        </button>
        <button @click="saveDocument" :disabled="!hasChanges" class="save-btn">
          <i class="fa fa-save"></i> 保存
        </button>
        <button @click="shareDocument" class="share-btn">
          <i class="fa fa-share"></i> 分享
        </button>
      </div>
    </div>

    <!-- 用户在线状态和光标显示 -->
    <UserPresence 
      :online-users="onlineUsers"
      :current-user-id="sessionId"
      :editor-container="markdownEditor"
      ref="userPresence"
    />

    <!-- Markdown工具栏 -->
    <div class="markdown-toolbar">
      <div class="toolbar-group">
        <button @click="insertMarkdown('**', '**')" title="粗体">
          <i class="fa fa-bold"></i>
        </button>
        <button @click="insertMarkdown('*', '*')" title="斜体">
          <i class="fa fa-italic"></i>
        </button>
        <button @click="insertMarkdown('# ', '')" title="标题">H</button>
      </div>
      
      <div class="toolbar-group">
        <button @click="insertMarkdown('- ', '')" title="列表">
          <i class="fa fa-list-ul"></i>
        </button>
        <button @click="insertMarkdown('``', '`')" title="代码">
          <i class="fa fa-code"></i>
        </button>
        <button @click="insertLink" title="链接">
          <i class="fa fa-link"></i>
        </button>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-main" :class="{ 'split-view': showPreview }">
      <!-- Markdown编辑区 -->
      <div class="editor-pane">
        <textarea
          ref="markdownEditor"
          v-model="content"
          @input="handleInput"
          placeholder="开始编写您的Markdown文档..."
          class="markdown-textarea"
        ></textarea>
      </div>
      
      <!-- 预览区 -->
      <div v-if="showPreview" class="preview-pane">
        <div class="markdown-preview" v-html="renderedMarkdown"></div>
      </div>
    </div>

    <!-- 编辑器状态 -->
    <div class="editor-status">
      <div class="connection-status">
        <span :class="['status-indicator', connectionStatus]"></span>
        {{ connectionStatusText }}
      </div>
      <div class="editor-info">
        <span>字符: {{ characterCount }}</span>
        <span>单词: {{ wordCount }}</span>
      </div>
      <div class="auto-save-status" v-if="autoSaveStatus">
        {{ autoSaveStatus }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import collaborativeApi from '@/api/collaborative'
import { ElMessage } from 'element-plus'
import { useDebounceFn } from '@vueuse/core'
import { Client } from '@stomp/stompjs'
// 导入SockJS
import SockJS from 'sockjs-client'

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
const markdownEditor = ref(null)
const content = ref('')
const hasChanges = ref(false)
const showPreview = ref(true)
const connectionStatus = ref('connected')
const autoSaveStatus = ref('')
const onlineUsers = ref([])
const websocket = ref(null)
const sessionId = ref(generateSessionId())

// 计算属性
const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected': return '已连接'
    case 'connecting': return '连接中...'
    case 'disconnected': return '连接断开'
    default: return '未知状态'
  }
})

const renderedMarkdown = computed(() => {
  try {
    // 使用marked库渲染Markdown
    return marked(content.value, { breaks: true, gfm: true })
  } catch (error) {
    return '<p>Markdown渲染失败</p>'
  }
})

const characterCount = computed(() => content.value.length)
const wordCount = computed(() => {
  return content.value.trim() ? content.value.trim().split(/\s+/).length : 0
})

// 生命周期
onMounted(async () => {
  await initializeEditor()
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
    const docId = props.documentId || route.params.id
    if (docId) {
      await loadDocument(docId)
    }
    if (!props.readOnly) {
      connectWebSocket()
    }
  } catch (error) {
    console.error('初始化Markdown编辑器失败:', error)
    ElMessage.error('编辑器初始化失败')
    emit('error', error)
  }
}

async function loadDocument(docId) {
  try {
    const response = await collaborativeApi.getDocument(docId, userStore.user?.id)
    document.value = response.data
    if (document.value.content) {
      content.value = document.value.content
    }
    hasChanges.value = false
    emit('documentLoaded', document.value)
  } catch (error) {
    console.error('加载Markdown文档失败:', error)
    ElMessage.error('加载文档失败')
    emit('error', error)
  }
}

function handleInput() {
  hasChanges.value = true
  debouncedAutoSave()
  
  if (websocket.value && websocket.value.connected) {
    sendTextChange()
  }
}

async function saveDocument() {
  if (!hasChanges.value) return

  try {
    autoSaveStatus.value = '保存中...'
    
    if (document.value?.id) {
      await collaborativeApi.syncDocument(document.value.id, {
        content: content.value,
        version: document.value.version
      })
    } else {
      const newDoc = await collaborativeApi.createDocument({
        title: '新建Markdown文档',
        documentType: 'markdown',
        content: content.value,
        userId: userStore.user?.id
      })
      document.value = newDoc.data
      router.replace(`/collaborative/markdown/${document.value.id}`)
    }

    hasChanges.value = false
    autoSaveStatus.value = '已保存'
    setTimeout(() => { autoSaveStatus.value = '' }, 2000)
    emit('documentSaved', document.value)
    ElMessage.success('文档保存成功')

  } catch (error) {
    console.error('保存失败:', error)
    autoSaveStatus.value = '保存失败'
    ElMessage.error('保存失败')
    emit('error', error)
  }
}

const debouncedAutoSave = useDebounceFn(() => {
  if (hasChanges.value) {
    saveDocument()
  }
}, 5000)

function togglePreview() {
  showPreview.value = !showPreview.value
}

function insertMarkdown(prefix, suffix = '') {
  if (!markdownEditor.value) return
  
  const textarea = markdownEditor.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = content.value.substring(start, end)
  
  const newText = prefix + selectedText + suffix
  content.value = content.value.substring(0, start) + newText + content.value.substring(end)
  hasChanges.value = true
  
  nextTick(() => {
    const newPos = start + prefix.length + selectedText.length + suffix.length
    textarea.setSelectionRange(newPos, newPos)
    textarea.focus()
  })
}

function insertLink() {
  insertMarkdown('[链接文字](', ')')
}

function shareDocument() {
  if (!document.value?.id) {
    ElMessage.warning('请先保存文档')
    return
  }
  const shareUrl = `${window.location.origin}/collaborative/markdown/${document.value.id}`
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('分享链接已复制')
  })
}

// WebSocket相关
function connectWebSocket() {
  try {
    console.log('尝试连接WebSocket...');
    connectionStatus.value = 'connecting';
    
    // 创建STOMP客户端，使用相对路径让Vite代理处理
    websocket.value = new Client({
      // 使用相对路径连接，让Vite代理处理
      brokerURL: '/ws-collaboration',
      connectHeaders: {},
      debug: function (str) {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    // 连接成功的回调
    websocket.value.onConnect = (frame) => {
      console.log('Connected: ' + frame);
      connectionStatus.value = 'connected';
      
      // 订阅文档相关的主题
      if (document.value?.id) {
        // 订阅用户加入/离开通知
        websocket.value.subscribe(`/topic/document/${document.value.id}/users`, (message) => {
          handleWebSocketMessage(JSON.parse(message.body));
        });
        
        // 订阅文档内容更新
        websocket.value.subscribe(`/topic/document/${document.value.id}/content`, (message) => {
          handleWebSocketMessage(JSON.parse(message.body));
        });
        
        // 加入文档协同编辑
        joinCollaboration();
      }
    };

    // 连接错误的回调
    websocket.value.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
      connectionStatus.value = 'disconnected';
    };

    // WebSocket断开连接的回调
    websocket.value.onDisconnect = () => {
      console.log('Disconnected');
      connectionStatus.value = 'disconnected';
    };

    // 启动连接
    websocket.value.activate();
  } catch (error) {
    console.error('WebSocket连接配置失败:', error);
    connectionStatus.value = 'disconnected';
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
    };
    
    websocket.value.send(JSON.stringify(message));
  }
}

function sendTextChange() {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    const message = {
      type: 'text-change',
      documentId: document.value?.id,
      content: content.value,
      sessionId: sessionId.value
    };
    
    websocket.value.send(JSON.stringify(message));
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
    case 'text-change':
      if (message.sessionId !== sessionId.value) {
        content.value = message.content
      }
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

function generateSessionId() {
  return 'md_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now()
}

function generateUserColor(userId) {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8']
  return colors[userId % colors.length]
}
</script>

<style scoped>
.markdown-editor {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.document-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.document-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #666;
}

.editor-actions {
  display: flex;
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

.preview-btn {
  background: #722ed1;
  color: white;
}

.preview-btn:hover, .preview-btn.active {
  background: #9254de;
}

.save-btn {
  background: #1890ff;
  color: white;
}

.save-btn:hover:not(:disabled) {
  background: #40a9ff;
}

.save-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.share-btn {
  background: #52c41a;
  color: white;
}

.share-btn:hover {
  background: #73d13d;
}

.online-users {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 24px;
  background: #f0f9ff;
  border-bottom: 1px solid #e8e8e8;
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
}

.online-count {
  font-size: 12px;
  color: #666;
}

.markdown-toolbar {
  display: flex;
  gap: 8px;
  padding: 12px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.toolbar-group {
  display: flex;
  gap: 4px;
  padding: 0 8px;
  border-right: 1px solid #ddd;
}

.toolbar-group:last-child {
  border-right: none;
}

.toolbar-group button {
  padding: 6px 10px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #333;
  transition: all 0.2s;
}

.toolbar-group button:hover {
  background: #f0f0f0;
}

.editor-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.editor-main.split-view .editor-pane {
  width: 50%;
}

.editor-pane {
  width: 100%;
  border-right: 1px solid #e8e8e8;
}

.markdown-textarea {
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  padding: 20px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  background: #fafafa;
}

.preview-pane {
  width: 50%;
  overflow-y: auto;
  background: white;
}

.markdown-preview {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
  line-height: 1.6;
  color: #333;
}

.editor-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 24px;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  font-size: 12px;
  color: #666;
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
</style>