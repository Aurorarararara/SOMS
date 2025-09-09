<template>
  <div class="collaborative-editor">
    <div class="editor-header">
      <div class="document-info">
        <h3>{{ document?.title || '新建文档' }}</h3>
        <div class="document-meta">
          <span class="version">版本 {{ document?.version || 1 }}</span>
          <span class="owner">创建者: {{ document?.ownerName || '未知' }}</span>
          <span class="type">类型: 富文本</span>
        </div>
      </div>
      
      <div class="editor-actions">
        <button @click="saveDocument" :disabled="!hasChanges" class="save-btn">
          <i class="fa fa-save"></i> 保存
        </button>
        <button @click="shareDocument" class="share-btn">
          <i class="fa fa-share"></i> 分享
        </button>
        <button @click="exportDocument" class="export-btn">
          <i class="fa fa-download"></i> 导出
        </button>
      </div>
    </div>

    <!-- 用户在线状态和光标显示 -->
    <UserPresence 
      :online-users="onlineUsers"
      :current-user-id="sessionId"
      editor-container="#rich-text-editor"
      ref="userPresence"
    />

    <!-- 富文本编辑器 -->
    <div class="editor-container">
      <div id="rich-text-editor" class="editor-content"></div>
    </div>

    <!-- 协同编辑状态 -->
    <div class="editor-status">
      <div class="connection-status">
        <span :class="['status-indicator', connectionStatus]"></span>
        {{ connectionStatusText }}
      </div>
      <div class="auto-save-status" v-if="autoSaveStatus">
        {{ autoSaveStatus }}
      </div>
    </div>

    <!-- 分享对话框 -->
    <div v-if="showShareDialog" class="share-dialog-overlay" @click="closeShareDialog">
      <div class="share-dialog" @click.stop>
        <div class="dialog-header">
          <h4>分享文档</h4>
          <button @click="closeShareDialog" class="close-btn">&times;</button>
        </div>
        <div class="dialog-content">
          <div class="share-link">
            <label>分享链接:</label>
            <div class="link-input">
              <input v-model="shareLink" readonly />
              <button @click="copyShareLink" class="copy-btn">复制</button>
            </div>
          </div>
          <div class="share-permissions">
            <label>权限设置:</label>
            <select v-model="sharePermission">
              <option value="read">仅查看</option>
              <option value="write">可编辑</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import collaborativeApi from '@/api/collaborative'
import { ElMessage, ElMessageBox } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import UserPresence from '@/components/collaborative/UserPresence.vue'
import CursorTrackingService from '@/utils/cursorTracking.js'
import { useDebounceFn } from '@vueuse/core'
import { Client } from '@stomp/stompjs'
// 导入SockJS
import SockJS from 'sockjs-client'

// Props 和 Emits
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
const quillEditor = ref(null)
const hasChanges = ref(false)
const connectionStatus = ref('connected') // connected, connecting, disconnected
const autoSaveStatus = ref('')
const onlineUsers = ref([])
const showShareDialog = ref(false)
const shareLink = ref('')
const sharePermission = ref('read')

// WebSocket 连接
const websocket = ref(null)
const sessionId = ref(generateSessionId())
const isTyping = ref(false)
const lastContent = ref('')
const userPresence = ref(null)
const cursorTracker = ref(null)

// 计算属性
const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected': return '已连接'
    case 'connecting': return '连接中...'
    case 'disconnected': return '连接断开'
    default: return '未知状态'
  }
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
    // 初始化 Quill 编辑器
    const options = {
      theme: 'snow',
      readOnly: props.readOnly,
      modules: {
        toolbar: [
          [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
          ['bold', 'italic', 'underline', 'strike'],
          [{ 'color': [] }, { 'background': [] }],
          [{ 'list': 'ordered'}, { 'list': 'bullet' }],
          [{ 'indent': '-1'}, { 'indent': '+1' }],
          [{ 'align': [] }],
          ['link', 'image', 'video'],
          ['clean']
        ],
        history: {
          delay: 1000,
          maxStack: 50,
          userOnly: true
        }
      }
    }

    await nextTick()
    quillEditor.value = new Quill('#rich-text-editor', options)

    // 初始化光标跟踪服务
    cursorTracker.value = new CursorTrackingService()
    // 通过Quill实例获取编辑器DOM元素，避免重新查询可能失败的问题
    let editorElement = null;
    if (quillEditor.value && typeof quillEditor.value.root !== 'undefined') {
      editorElement = quillEditor.value.root;
    } else if (typeof document !== 'undefined' && document.querySelector && typeof document.querySelector === 'function') {
      // 如果通过Quill实例无法获取，再尝试使用document.querySelector
      editorElement = document.querySelector('#rich-text-editor');
    }
    
    if (editorElement) {
      cursorTracker.value.initialize('quill', quillEditor.value, editorElement)
    } else {
      console.error('无法找到编辑器元素')
      ElMessage.error('编辑器初始化失败：无法找到编辑器元素')
      return
    }
    
    // 设置光标变化回调
    cursorTracker.value.onCursorChange = (cursorData) => {
      sendCursorUpdate({
        ...cursorData,
        sessionId: sessionId.value,
        userId: userStore.user?.id,
        userName: userStore.user?.name,
        userColor: generateUserColor(userStore.user?.id)
      })
    }
    
    cursorTracker.value.onSelectionChange = (selectionData) => {
      sendSelectionUpdate({
        ...selectionData,
        sessionId: sessionId.value,
        userId: userStore.user?.id,
        userName: userStore.user?.name,
        userColor: generateUserColor(userStore.user?.id)
      })
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
    console.error('初始化编辑器失败:', error)
    ElMessage.error('编辑器初始化失败')
    emit('error', error)
  }
}

function setupEditorEvents() {
  if (!quillEditor.value) return

  // 内容变化事件
  quillEditor.value.on('text-change', (delta, oldDelta, source) => {
    if (source === 'user') {
      hasChanges.value = true
      isTyping.value = true
      
      // 发送操作到其他用户
      if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
        sendOperation({
          type: 'text-change',
          delta: delta,
          source: 'user',
          sessionId: sessionId.value,
          timestamp: Date.now()
        })
      }

      // 防抖自动保存
      debounceAutoSave()
    }
  })

  // 选择变化事件（光标位置）
  quillEditor.value.on('selection-change', (range, oldRange, source) => {
    if (source === 'user' && websocket.value && websocket.value.readyState === WebSocket.OPEN) {
      sendCursorUpdate({
        position: range,
        sessionId: sessionId.value,
        userId: userStore.user?.id,
        userName: userStore.user?.name,
        timestamp: Date.now()
      })
    }
  })
}

async function loadDocument(docId) {
  try {
    const response = await collaborativeApi.getDocument(docId, userStore.user?.id)
    document.value = response.data

    // 设置编辑器内容
    if (document.value.content) {
      const content = typeof document.value.content === 'string' 
        ? JSON.parse(document.value.content) 
        : document.value.content
      
      quillEditor.value.setContents(content)
      lastContent.value = JSON.stringify(content)
    }

    hasChanges.value = false
    emit('documentLoaded', document.value)

  } catch (error) {
    console.error('加载文档失败:', error)
    ElMessage.error('加载文档失败')
    emit('error', error)
  }
}

async function saveDocument() {
  if (!hasChanges.value || !quillEditor.value) return

  try {
    autoSaveStatus.value = '保存中...'
    
    const content = quillEditor.value.getContents()
    const currentVersion = document.value?.version || 1

    if (document.value?.id) {
      // 更新现有文档
      await collaborativeApi.syncDocument(document.value.id, {
        content: JSON.stringify(content),
        version: currentVersion
      })
    } else {
      // 创建新文档
      const newDoc = await collaborativeApi.createDocument({
        title: '新建富文本文档',
        documentType: 'richtext',
        content: JSON.stringify(content),
        userId: userStore.user?.id
      })
      document.value = newDoc.data
      router.replace(`/collaborative/richtext/${document.value.id}`)
    }

    hasChanges.value = false
    lastContent.value = JSON.stringify(content)
    autoSaveStatus.value = '已保存'
    
    setTimeout(() => {
      autoSaveStatus.value = ''
    }, 2000)

    emit('documentSaved', document.value)
    ElMessage.success('文档保存成功')

  } catch (error) {
    console.error('保存文档失败:', error)
    autoSaveStatus.value = '保存失败'
    ElMessage.error('保存文档失败')
    emit('error', error)
  }
}

// 防抖自动保存
const debounceAutoSave = useDebounceFn(() => {
  if (hasChanges.value) {
    saveDocument()
  }
}, 5000)

function shareDocument() {
  if (!document.value?.id) {
    ElMessage.warning('请先保存文档')
    return
  }

  shareLink.value = `${window.location.origin}/collaborative/richtext/${document.value.id}`
  showShareDialog.value = true
}

function closeShareDialog() {
  showShareDialog.value = false
}

async function copyShareLink() {
  try {
    await navigator.clipboard.writeText(shareLink.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

async function exportDocument() {
  if (!document.value?.id) {
    ElMessage.warning('请先保存文档')
    return
  }

  try {
    // 这里可以实现导出功能
    ElMessage.info('导出功能开发中...')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// WebSocket 连接和操作
function connectWebSocket() {
  try {
    console.log('尝试连接WebSocket...');
    connectionStatus.value = 'connecting';
    
    // 创建STOMP客户端，使用SockJS作为后备选项
    websocket.value = new Client({
      // 使用相对路径连接，让Vite代理处理
      brokerURL: '/ws-collaboration',
      connectHeaders: {
        // 可以添加认证信息
      },
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
        
        // 订阅文档操作
        websocket.value.subscribe(`/topic/document/${document.value.id}/operations`, (message) => {
          handleWebSocketMessage(JSON.parse(message.body));
        });
        
        // 订阅光标更新
        websocket.value.subscribe(`/topic/document/${document.value.id}/cursors`, (message) => {
          handleWebSocketMessage(JSON.parse(message.body));
        });
        
        // 订阅选择更新
        websocket.value.subscribe(`/topic/document/${document.value.id}/selections`, (message) => {
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

function sendOperation(operation) {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    const message = {
      type: 'operation',
      documentId: document.value?.id,
      operation: operation
    };
    
    websocket.value.send(JSON.stringify(message));
  }
}

function sendCursorUpdate(cursorData) {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    const message = {
      type: 'cursor',
      documentId: document.value?.id,
      cursor: cursorData
    };
    
    websocket.value.send(JSON.stringify(message));
  }
}

function sendSelectionUpdate(selectionData) {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    const message = {
      type: 'selection',
      documentId: document.value?.id,
      selection: selectionData
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
    case 'operation':
      handleRemoteOperation(message)
      break
    case 'cursor_update':
      handleCursorUpdate(message)
      break
    case 'selection_update':
      handleSelectionUpdate(message)
      break
    case 'user_typing':
      handleUserTyping(message)
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
    userColor: message.userColor,
    isTyping: false,
    lastSeen: Date.now(),
    cursorPosition: null,
    selectionRange: null
  }
  
  const existingIndex = onlineUsers.value.findIndex(u => u.sessionId === user.sessionId)
  if (existingIndex >= 0) {
    onlineUsers.value[existingIndex] = user
  } else {
    onlineUsers.value.push(user)
  }
  
  ElMessage.success(`${user.userName} 加入协同编辑`)
}

function handleUserLeft(message) {
  const user = onlineUsers.value.find(u => u.sessionId === message.sessionId)
  if (user) {
    onlineUsers.value = onlineUsers.value.filter(u => u.sessionId !== message.sessionId)
    
    // 移除该用户的光标和选择
    if (cursorTracker.value) {
      cursorTracker.value.removeRemoteCursor(message.sessionId)
    }
    
    ElMessage.info(`${user.userName} 离开了协同编辑`)
  }
}

function handleCursorUpdate(message) {
  const user = onlineUsers.value.find(u => u.sessionId === message.cursor.sessionId)
  if (user && message.cursor.sessionId !== sessionId.value) {
    user.cursorPosition = message.cursor.position || message.cursor.range
    user.lastSeen = Date.now()
    
    // 更新光标显示
    if (cursorTracker.value) {
      cursorTracker.value.updateRemoteCursor(message.cursor.sessionId, {
        ...message.cursor,
        userName: user.userName,
        userColor: user.userColor
      })
    }
  }
}

function handleSelectionUpdate(message) {
  const user = onlineUsers.value.find(u => u.sessionId === message.selection.sessionId)
  if (user && message.selection.sessionId !== sessionId.value) {
    user.selectionRange = message.selection.range
    user.lastSeen = Date.now()
    
    // 更新选择显示
    if (cursorTracker.value) {
      cursorTracker.value.updateRemoteSelection(message.selection.sessionId, {
        ...message.selection,
        userName: user.userName,
        userColor: user.userColor
      })
    }
  }
}

function handleUserTyping(message) {
  const user = onlineUsers.value.find(u => u.sessionId === message.sessionId)
  if (user && message.sessionId !== sessionId.value) {
    user.isTyping = message.isTyping
    user.lastSeen = Date.now()
    
    // 设置输入状态自动清除
    if (message.isTyping) {
      setTimeout(() => {
        if (user.isTyping) {
          user.isTyping = false
        }
      }, 3000)
    }
  }
}

function handleRemoteOperation(message) {
  if (message.operation.sessionId !== sessionId.value && quillEditor.value) {
    // 应用远程操作
    quillEditor.value.updateContents(message.operation.delta, 'api')
  }
}

function cleanup() {
  if (websocket.value) {
    websocket.value.deactivate();
  }
  
  if (cursorTracker.value) {
    cursorTracker.value.destroy();
  }
  
  if (quillEditor.value) {
    quillEditor.value = null;
  }
}

// 工具函数
function generateSessionId() {
  return 'session_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now()
}

function generateUserColor(userId) {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8']
  return colors[userId % colors.length]
}
</script>

<style scoped>
.collaborative-editor {
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

.export-btn {
  background: #faad14;
  color: white;
}

.export-btn:hover {
  background: #ffc53d;
}

.editor-container {
  flex: 1;
  overflow: hidden;
}

.editor-content {
  height: 100%;
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

.share-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.share-dialog {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.dialog-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.dialog-content {
  padding: 24px;
}

.share-link, .share-permissions {
  margin-bottom: 20px;
}

.share-link label, .share-permissions label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.link-input {
  display: flex;
  gap: 8px;
}

.link-input input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.copy-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.copy-btn:hover {
  background: #40a9ff;
}

.share-permissions select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

/* Quill 编辑器样式覆盖 */
:deep(.ql-editor) {
  font-size: 14px;
  line-height: 1.6;
}

:deep(.ql-toolbar) {
  border-left: none;
  border-right: none;
  border-top: none;
}

:deep(.ql-container) {
  border-left: none;
  border-right: none;
  border-bottom: none;
  height: calc(100% - 42px);
}
</style>