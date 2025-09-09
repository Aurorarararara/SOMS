<template>
  <div class="table-editor">
    <div class="editor-header">
      <div class="document-info">
        <h3>{{ document?.title || '新建表格文档' }}</h3>
        <div class="document-meta">
          <span class="version">版本 {{ document?.version || 1 }}</span>
          <span class="type">类型: 表格</span>
        </div>
      </div>
      
      <div class="editor-actions">
        <button @click="saveDocument" :disabled="!hasChanges" class="save-btn">
          <i class="fa fa-save"></i> 保存
        </button>
        <button @click="shareDocument" class="share-btn">
          <i class="fa fa-share"></i> 分享
        </button>
        <button @click="exportTable" class="export-btn">
          <i class="fa fa-download"></i> 导出
        </button>
      </div>
    </div>

    <!-- 用户在线状态和光标显示 -->
    <UserPresence 
      :online-users="onlineUsers"
      :current-user-id="sessionId"
      :editor-container="tableContainer"
      ref="userPresence"
    />

    <!-- 表格工具栏 -->
    <div class="table-toolbar">
      <div class="toolbar-group">
        <button @click="addRow" title="添加行">
          <i class="fa fa-plus"></i> 行
        </button>
        <button @click="addColumn" title="添加列">
          <i class="fa fa-plus"></i> 列
        </button>
        <button @click="deleteRow" title="删除行" :disabled="tableData.length <= 1">
          <i class="fa fa-minus"></i> 行
        </button>
        <button @click="deleteColumn" title="删除列" :disabled="tableData[0]?.length <= 1">
          <i class="fa fa-minus"></i> 列
        </button>
      </div>
    </div>

    <!-- 表格容器 -->
    <div class="table-container">
      <table class="spreadsheet">
        <thead>
          <tr>
            <th class="row-header"></th>
            <th v-for="(col, colIndex) in tableData[0] || []" :key="colIndex" class="column-header">
              {{ getColumnLabel(colIndex) }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, rowIndex) in tableData" :key="rowIndex">
            <td class="row-header">{{ rowIndex + 1 }}</td>
            <td 
              v-for="(cell, colIndex) in row" 
              :key="colIndex"
              class="cell"
              :class="{ selected: selectedCell.row === rowIndex && selectedCell.col === colIndex }"
              @click="selectCell(rowIndex, colIndex)"
              @input="updateCell(rowIndex, colIndex, $event)"
              contenteditable="true"
            >
              {{ cell }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 状态栏 -->
    <div class="editor-status">
      <div class="connection-status">
        <span :class="['status-indicator', connectionStatus]"></span>
        {{ connectionStatusText }}
      </div>
      <div class="editor-info">
        <span>选中: {{ selectedCellAddress }}</span>
        <span>行数: {{ tableData.length }}</span>
        <span>列数: {{ tableData[0]?.length || 0 }}</span>
      </div>
      <div class="auto-save-status" v-if="autoSaveStatus">
        {{ autoSaveStatus }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import collaborativeApi from '@/api/collaborative'
import { ElMessage } from 'element-plus'
import { useDebounceFn } from '@vueuse/core'

import UserPresence from '@/components/collaborative/UserPresence.vue'

// Props
const props = defineProps({
  documentId: { type: [String, Number], default: null },
  readOnly: { type: Boolean, default: false }
})

const emit = defineEmits(['documentLoaded', 'documentSaved', 'error'])

// 状态管理
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const document = ref(null)
const tableData = ref([
  ['A1', 'B1', 'C1'],
  ['A2', 'B2', 'C2'],
  ['A3', 'B3', 'C3']
])
const selectedCell = ref({ row: 0, col: 0 })
const hasChanges = ref(false)
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

const selectedCellAddress = computed(() => {
  return getColumnLabel(selectedCell.value.col) + (selectedCell.value.row + 1)
})

// 生命周期
onMounted(async () => {
  await initializeEditor()
})

// 监听路由变化
watch(() => route.params.id, async (newId) => {
  if (newId) await loadDocument(newId)
}, { immediate: true })

// 方法
async function initializeEditor() {
  try {
    const docId = props.documentId || route.params.id
    if (docId) await loadDocument(docId)
    if (!props.readOnly) connectWebSocket()
  } catch (error) {
    console.error('初始化表格编辑器失败:', error)
    ElMessage.error('编辑器初始化失败')
    emit('error', error)
  }
}

async function loadDocument(docId) {
  try {
    const response = await collaborativeApi.getDocument(docId, userStore.user?.id)
    document.value = response.data
    if (document.value.content) {
      const data = JSON.parse(document.value.content)
      if (data.tableData) {
        tableData.value = data.tableData
      }
    }
    hasChanges.value = false
    emit('documentLoaded', document.value)
  } catch (error) {
    console.error('加载表格文档失败:', error)
    ElMessage.error('加载文档失败')
    emit('error', error)
  }
}

async function saveDocument() {
  if (!hasChanges.value) return

  try {
    autoSaveStatus.value = '保存中...'
    const content = JSON.stringify({ tableData: tableData.value })

    if (document.value?.id) {
      await collaborativeApi.syncDocument(document.value.id, {
        content: content,
        version: document.value.version
      })
    } else {
      const newDoc = await collaborativeApi.createDocument({
        title: '新建表格文档',
        documentType: 'table',
        content: content,
        userId: userStore.user?.id
      })
      document.value = newDoc.data
      router.replace(`/collaborative/table/${document.value.id}`)
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

function selectCell(row, col) {
  selectedCell.value = { row, col }
}

function updateCell(row, col, event) {
  tableData.value[row][col] = event.target.textContent
  hasChanges.value = true
  debouncedAutoSave()
  
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    sendCellChange({ row, col, value: event.target.textContent })
  }
}

function addRow() {
  const newRow = new Array(tableData.value[0]?.length || 3).fill('')
  tableData.value.push(newRow)
  hasChanges.value = true
}

function addColumn() {
  tableData.value.forEach(row => row.push(''))
  hasChanges.value = true
}

function deleteRow() {
  if (tableData.value.length > 1) {
    tableData.value.pop()
    hasChanges.value = true
  }
}

function deleteColumn() {
  if (tableData.value[0]?.length > 1) {
    tableData.value.forEach(row => row.pop())
    hasChanges.value = true
  }
}

function shareDocument() {
  if (!document.value?.id) {
    ElMessage.warning('请先保存文档')
    return
  }
  const shareUrl = `${window.location.origin}/collaborative/table/${document.value.id}`
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('分享链接已复制')
  })
}

function exportTable() {
  try {
    const csv = tableData.value.map(row => row.join(',')).join('\n')
    const blob = new Blob([csv], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${document.value?.title || '表格文档'}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// WebSocket相关
function connectWebSocket() {
  try {
    // 使用正确的WebSocket URL，后端服务运行在8081端口
    const wsUrl = `ws://localhost:8081/ws-collaboration`
    websocket.value = new WebSocket(wsUrl)

    websocket.value.onopen = () => {
      connectionStatus.value = 'connected'
      if (document.value?.id) joinCollaboration()
    }

    websocket.value.onmessage = (event) => {
      handleWebSocketMessage(JSON.parse(event.data))
    }

    websocket.value.onclose = () => {
      connectionStatus.value = 'disconnected'
      setTimeout(() => {
        if (connectionStatus.value === 'disconnected') connectWebSocket()
      }, 3000)
    }
  } catch (error) {
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

function sendCellChange(change) {
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    const message = {
      type: 'cell-change',
      documentId: document.value?.id,
      change: { ...change, sessionId: sessionId.value }
    }
    websocket.value.send(JSON.stringify(message))
  }
}

function handleWebSocketMessage(message) {
  switch (message.type) {
    case 'user_joined':
      const user = {
        sessionId: message.sessionId,
        userId: message.userId,
        userName: message.userName,
        userColor: message.userColor
      }
      if (!onlineUsers.value.find(u => u.sessionId === user.sessionId)) {
        onlineUsers.value.push(user)
      }
      break
    case 'user_left':
      onlineUsers.value = onlineUsers.value.filter(u => u.sessionId !== message.sessionId)
      break
    case 'cell-change':
      if (message.change.sessionId !== sessionId.value) {
        const { row, col, value } = message.change
        if (tableData.value[row] && tableData.value[row][col] !== undefined) {
          tableData.value[row][col] = value
        }
      }
      break
  }
}

// 工具函数
function getColumnLabel(colIndex) {
  let label = ''
  while (colIndex >= 0) {
    label = String.fromCharCode(65 + (colIndex % 26)) + label
    colIndex = Math.floor(colIndex / 26) - 1
  }
  return label
}

function generateSessionId() {
  return 'table_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now()
}

function generateUserColor(userId) {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8']
  return colors[userId % colors.length]
}
</script>

<style scoped>
.table-editor {
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

.export-btn {
  background: #faad14;
  color: white;
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

.table-toolbar {
  display: flex;
  gap: 8px;
  padding: 12px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.toolbar-group {
  display: flex;
  gap: 4px;
}

.toolbar-group button {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.toolbar-group button:hover:not(:disabled) {
  background: #f0f0f0;
}

.toolbar-group button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.table-container {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.spreadsheet {
  border-collapse: collapse;
  width: 100%;
  min-width: 600px;
}

.spreadsheet th,
.spreadsheet td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
  min-width: 80px;
  height: 32px;
}

.spreadsheet th {
  background: #f5f5f5;
  font-weight: 600;
  text-align: center;
}

.row-header,
.column-header {
  background: #f0f0f0 !important;
  color: #666;
  font-size: 12px;
  user-select: none;
}

.cell {
  cursor: text;
  outline: none;
}

.cell.selected {
  background: #e6f7ff;
  border: 2px solid #1890ff;
}

.cell:focus {
  background: #f9f9f9;
  border: 2px solid #40a9ff;
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

.status-indicator.disconnected {
  background: #ff4d4f;
}

.editor-info {
  display: flex;
  gap: 16px;
}
</style>