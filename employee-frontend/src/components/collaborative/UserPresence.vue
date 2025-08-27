<template>
  <div class="user-presence">
    <!-- 在线用户列表 -->
    <div class="online-users-panel" v-if="showUserPanel">
      <div class="panel-header">
        <h4>在线用户 ({{ onlineUsers.length }})</h4>
        <button @click="toggleUserPanel" class="close-btn">×</button>
      </div>
      <div class="user-list">
        <div 
          v-for="user in onlineUsers" 
          :key="user.sessionId"
          class="user-item"
          :class="{ 'is-typing': user.isTyping }"
        >
          <div 
            class="user-avatar"
            :style="{ backgroundColor: user.userColor }"
          >
            {{ user.userName?.charAt(0)?.toUpperCase() || '?' }}
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.userName || '匿名用户' }}</div>
            <div class="user-status">
              <span v-if="user.isTyping" class="typing-indicator">正在输入...</span>
              <span v-else-if="user.lastSeen" class="last-seen">
                {{ formatLastSeen(user.lastSeen) }}
              </span>
              <span v-else class="online-status">在线</span>
            </div>
          </div>
          <div class="user-cursor-info" v-if="user.cursorPosition">
            <span class="cursor-position">
              行 {{ user.cursorPosition.line || 0 }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 紧凑的在线用户显示 -->
    <div class="users-compact" v-else>
      <div class="user-avatars">
        <div 
          v-for="user in displayUsers" 
          :key="user.sessionId"
          class="user-avatar"
          :style="{ backgroundColor: user.userColor }"
          :title="`${user.userName} ${user.isTyping ? '- 正在输入' : ''}`"
          :class="{ 'is-typing': user.isTyping }"
        >
          {{ user.userName?.charAt(0)?.toUpperCase() || '?' }}
        </div>
        <div 
          v-if="onlineUsers.length > maxDisplayUsers"
          class="more-users"
          :title="`还有 ${onlineUsers.length - maxDisplayUsers} 人在线`"
        >
          +{{ onlineUsers.length - maxDisplayUsers }}
        </div>
      </div>
      <button @click="toggleUserPanel" class="users-toggle">
        {{ onlineUsers.length }} 人在线
      </button>
    </div>

    <!-- 光标覆盖层 -->
    <div class="cursor-overlay" ref="cursorOverlay">
      <div 
        v-for="cursor in visibleCursors" 
        :key="cursor.sessionId"
        class="remote-cursor"
        :style="cursor.style"
      >
        <div class="cursor-line" :style="{ backgroundColor: cursor.userColor }"></div>
        <div class="cursor-label" :style="{ backgroundColor: cursor.userColor }">
          {{ cursor.userName }}
        </div>
      </div>
    </div>

    <!-- 选择范围覆盖层 -->
    <div class="selection-overlay" ref="selectionOverlay">
      <div 
        v-for="selection in visibleSelections" 
        :key="selection.sessionId"
        class="remote-selection"
        :style="selection.style"
      >
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

// Props
const props = defineProps({
  onlineUsers: {
    type: Array,
    default: () => []
  },
  currentUserId: {
    type: [String, Number],
    default: null
  },
  editorContainer: {
    type: [Element, String],
    default: null
  },
  maxDisplayUsers: {
    type: Number,
    default: 3
  }
})

// Emits
const emit = defineEmits(['userClick', 'cursorClick'])

// 响应式数据
const showUserPanel = ref(false)
const cursorOverlay = ref(null)
const selectionOverlay = ref(null)
const visibleCursors = ref([])
const visibleSelections = ref([])

// 计算属性
const displayUsers = computed(() => {
  return props.onlineUsers.slice(0, props.maxDisplayUsers)
})

// 监听用户变化，更新光标显示
watch(() => props.onlineUsers, (newUsers) => {
  updateCursorDisplay()
  updateSelectionDisplay()
}, { deep: true })

// 生命周期
onMounted(() => {
  setupCursorTracking()
})

onUnmounted(() => {
  cleanup()
})

// 方法
function toggleUserPanel() {
  showUserPanel.value = !showUserPanel.value
}

function formatLastSeen(timestamp) {
  if (!timestamp) return '在线'
  
  const now = Date.now()
  const diff = now - timestamp
  
  if (diff < 30000) return '刚刚'
  if (diff < 60000) return '1分钟前'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  
  return '离线'
}

function setupCursorTracking() {
  // 监听编辑器容器的变化
  if (props.editorContainer) {
    // 这里可以添加 MutationObserver 来监听编辑器内容变化
    observeEditorChanges()
  }
}

function observeEditorChanges() {
  if (!props.editorContainer) return
  
  // 确保传递给observe的是有效的DOM节点
  let targetElement = props.editorContainer;
  if (typeof props.editorContainer === 'string') {
    // 确保在浏览器环境中调用document.querySelector，并添加安全检查
    if (typeof document !== 'undefined' && document.querySelector && typeof document.querySelector === 'function') {
      targetElement = document.querySelector(props.editorContainer);
    } else {
      console.warn('document.querySelector is not available');
      return;
    }
  }
  
  // 检查targetElement是否为有效的DOM节点
  if (!targetElement || !(targetElement instanceof Node)) {
    console.warn('Invalid editor container provided to UserPresence component');
    return;
  }
  
  const observer = new MutationObserver(() => {
    updateCursorDisplay();
    updateSelectionDisplay();
  });
  
  observer.observe(targetElement, {
    childList: true,
    subtree: true,
    characterData: true
  });
  
  // 保存observer引用以便清理
  window.mutationObserver = observer;
}

function updateCursorDisplay() {
  const cursors = []
  
  props.onlineUsers.forEach(user => {
    if (user.sessionId !== props.currentUserId && user.cursorPosition) {
      const cursorStyle = calculateCursorPosition(user.cursorPosition)
      if (cursorStyle) {
        cursors.push({
          sessionId: user.sessionId,
          userName: user.userName,
          userColor: user.userColor,
          style: cursorStyle
        })
      }
    }
  })
  
  visibleCursors.value = cursors
}

function updateSelectionDisplay() {
  const selections = []
  
  props.onlineUsers.forEach(user => {
    if (user.sessionId !== props.currentUserId && user.selectionRange) {
      const selectionStyle = calculateSelectionStyle(user.selectionRange)
      if (selectionStyle) {
        selections.push({
          sessionId: user.sessionId,
          userName: user.userName,
          userColor: user.userColor,
          style: selectionStyle
        })
      }
    }
  })
  
  visibleSelections.value = selections
}

function calculateCursorPosition(cursorPosition) {
  // 根据光标位置计算在页面上的坐标
  // 这里需要根据具体的编辑器类型来实现
  try {
    if (cursorPosition.index !== undefined) {
      // Quill 编辑器的光标位置计算
      return calculateQuillCursorPosition(cursorPosition)
    } else if (cursorPosition.line !== undefined && cursorPosition.column !== undefined) {
      // Monaco 编辑器的光标位置计算
      return calculateMonacoCursorPosition(cursorPosition)
    }
  } catch (error) {
    console.warn('计算光标位置失败:', error)
  }
  return null
}

function calculateQuillCursorPosition(position) {
  if (!props.editorContainer) return null
  
  // 这里是 Quill 编辑器的光标位置计算逻辑
  // 需要根据 index 计算出实际的 DOM 位置
  const range = document.createRange()
  // 确保在浏览器环境中调用document.querySelector，并添加安全检查
  const editorElement = typeof props.editorContainer === 'string' 
    ? (typeof document !== 'undefined' && document.querySelector && typeof document.querySelector === 'function' 
       ? document.querySelector(props.editorContainer) 
       : null)
    : props.editorContainer
    
  // 检查editorElement是否为有效的DOM节点
  if (!editorElement || !(editorElement instanceof Element)) {
    console.warn('Invalid editor container provided to calculateQuillCursorPosition');
    return null;
  }
  
  try {
    // 简化的位置计算，实际实现需要更复杂的逻辑
    const rect = editorElement.getBoundingClientRect()
    return {
      position: 'absolute',
      left: `${position.index * 0.5 + 10}px`, // 简化计算
      top: `${position.index * 0.1 + 10}px`,
      zIndex: 1000
    }
  } catch (error) {
    return null
  }
}

function calculateMonacoCursorPosition(position) {
  if (!props.editorContainer) return null
  
  // Monaco 编辑器的光标位置计算逻辑
  // 确保在浏览器环境中调用document.querySelector，并添加安全检查
  const editorElement = typeof props.editorContainer === 'string' 
    ? (typeof document !== 'undefined' && document.querySelector && typeof document.querySelector === 'function' 
       ? document.querySelector(props.editorContainer) 
       : null)
    : props.editorContainer
    
  // 检查editorElement是否为有效的DOM节点
  if (!editorElement || !(editorElement instanceof Element)) {
    console.warn('Invalid editor container provided to calculateMonacoCursorPosition');
    return null;
  }
    
  try {
    const rect = editorElement.getBoundingClientRect()
    const lineHeight = 18 // 默认行高
    const charWidth = 7 // 默认字符宽度
    
    return {
      position: 'absolute',
      left: `${position.column * charWidth + 10}px`,
      top: `${position.line * lineHeight + 10}px`,
      zIndex: 1000
    }
  } catch (error) {
    return null
  }
}

function calculateSelectionStyle(selectionRange) {
  // 计算选择范围的样式
  try {
    if (!props.editorContainer || !selectionRange) return null
    
    // 确保在浏览器环境中调用document.querySelector，并添加安全检查
    const editorElement = typeof props.editorContainer === 'string' 
      ? (typeof document !== 'undefined' && document.querySelector && typeof document.querySelector === 'function' 
         ? document.querySelector(props.editorContainer) 
         : null)
      : props.editorContainer
      
    // 检查editorElement是否为有效的DOM节点
    if (!editorElement || !(editorElement instanceof Element)) {
      console.warn('Invalid editor container provided to calculateSelectionStyle');
      return null;
    }
    
    // 简化的选择范围计算
    return {
      position: 'absolute',
      left: `${selectionRange.start * 0.5 + 10}px`,
      top: `${selectionRange.start * 0.1 + 10}px`,
      width: `${(selectionRange.end - selectionRange.start) * 0.5}px`,
      height: '18px',
      backgroundColor: 'rgba(66, 165, 245, 0.2)',
      zIndex: 999
    }
  } catch (error) {
    return null
  }
}

function cleanup() {
  // 清理资源
  if (window.mutationObserver) {
    window.mutationObserver.disconnect();
    window.mutationObserver = null;
  }
}

// 暴露方法给父组件
defineExpose({
  updateCursorDisplay,
  updateSelectionDisplay
})
</script>

<style scoped>
.user-presence {
  position: relative;
}

/* 在线用户面板 */
.online-users-panel {
  position: fixed;
  top: 80px;
  right: 20px;
  width: 300px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1001;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
  border-radius: 8px 8px 0 0;
}

.panel-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.user-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.user-item:hover {
  background: #f8f9fa;
}

.user-item.is-typing {
  background: #e6f7ff;
}

.user-item:last-child {
  border-bottom: none;
}

/* 紧凑用户显示 */
.users-compact {
  display: flex;
  align-items: center;
  gap: 12px;
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
  transition: all 0.2s;
  position: relative;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.user-avatar.is-typing::after {
  content: '';
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #52c41a;
  border: 2px solid white;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

.more-users {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #d9d9d9;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  cursor: pointer;
}

.users-toggle {
  background: none;
  border: none;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.users-toggle:hover {
  background: #f0f0f0;
  color: #333;
}

/* 用户信息 */
.user-info {
  flex: 1;
  margin-left: 12px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.user-status {
  font-size: 12px;
  color: #666;
}

.typing-indicator {
  color: #52c41a;
  font-weight: 500;
}

.cursor-position {
  font-size: 10px;
  color: #999;
}

/* 光标覆盖层 */
.cursor-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1000;
}

.remote-cursor {
  position: absolute;
  pointer-events: none;
}

.cursor-line {
  width: 2px;
  height: 18px;
  animation: blink 1s infinite;
}

.cursor-label {
  position: absolute;
  top: -20px;
  left: 0;
  padding: 2px 6px;
  border-radius: 3px;
  color: white;
  font-size: 10px;
  font-weight: 500;
  white-space: nowrap;
  transform: translateX(-50%);
}

/* 选择范围覆盖层 */
.selection-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 999;
}

.remote-selection {
  position: absolute;
  pointer-events: none;
  border-radius: 2px;
}

/* 动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(0.9);
  }
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

/* 滚动条样式 */
.user-list::-webkit-scrollbar {
  width: 4px;
}

.user-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.user-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.user-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>