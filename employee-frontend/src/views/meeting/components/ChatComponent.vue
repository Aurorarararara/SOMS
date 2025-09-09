<template>
  <div class="chat-component">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-left">
        <el-icon><ChatDotRound /></el-icon>
        <span class="title">会议聊天</span>
        <el-badge :value="onlineCount" class="online-badge" />
      </div>
      <div class="header-right">
        <el-button 
          text 
          :icon="Setting" 
          @click="showSettings = !showSettings"
        />
        <el-button 
          text 
          :icon="Close" 
          @click="$emit('close')"
        />
      </div>
    </div>
    
    <!-- 聊天设置面板 -->
    <div class="chat-settings" v-if="showSettings">
      <div class="setting-item">
        <span>消息提醒</span>
        <el-switch v-model="notificationEnabled" />
      </div>
      <div class="setting-item">
        <span>显示时间戳</span>
        <el-switch v-model="showTimestamp" />
      </div>
      <div class="setting-item">
        <span>字体大小</span>
        <el-select v-model="fontSize" size="small" style="width: 80px">
          <el-option label="小" value="small" />
          <el-option label="中" value="medium" />
          <el-option label="大" value="large" />
        </el-select>
      </div>
    </div>
    
    <!-- 消息列表 -->
    <div class="message-list" ref="messageList" :class="`font-${fontSize}`">
      <div class="message-container">
        <!-- 系统消息 -->
        <div 
          v-for="message in messages" 
          :key="message.id"
          class="message-item"
          :class="{
            'system-message': message.type === 'system',
            'own-message': message.senderId === currentUserId,
            'other-message': message.senderId !== currentUserId && message.type !== 'system'
          }"
        >
          <!-- 系统消息 -->
          <div v-if="message.type === 'system'" class="system-content">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ message.content }}</span>
            <span v-if="showTimestamp" class="timestamp">{{ formatTime(message.timestamp) }}</span>
          </div>
          
          <!-- 用户消息 -->
          <div v-else class="user-message">
            <!-- 头像和用户名 -->
            <div class="message-header" v-if="message.senderId !== currentUserId">
              <el-avatar 
                :size="24" 
                :src="message.senderAvatar"
                :alt="message.senderName"
              >
                {{ message.senderName?.charAt(0) }}
              </el-avatar>
              <span class="sender-name">{{ message.senderName }}</span>
              <span v-if="showTimestamp" class="timestamp">{{ formatTime(message.timestamp) }}</span>
            </div>
            
            <!-- 消息内容 -->
            <div class="message-content">
              <!-- 文本消息 -->
              <div v-if="message.messageType === 'text'" class="text-content">
                <span v-html="formatMessageContent(message.content)"></span>
                <span v-if="showTimestamp && message.senderId === currentUserId" class="timestamp own-timestamp">
                  {{ formatTime(message.timestamp) }}
                </span>
              </div>
              
              <!-- 图片消息 -->
              <div v-else-if="message.messageType === 'image'" class="image-content">
                <el-image 
                  :src="message.content"
                  :preview-src-list="[message.content]"
                  fit="cover"
                  style="max-width: 200px; max-height: 200px; border-radius: 8px"
                />
                <span v-if="showTimestamp && message.senderId === currentUserId" class="timestamp own-timestamp">
                  {{ formatTime(message.timestamp) }}
                </span>
              </div>
              
              <!-- 文件消息 -->
              <div v-else-if="message.messageType === 'file'" class="file-content">
                <div class="file-info" @click="downloadFile(message)">
                  <el-icon><Document /></el-icon>
                  <div class="file-details">
                    <span class="file-name">{{ message.fileName }}</span>
                    <span class="file-size">{{ formatFileSize(message.fileSize) }}</span>
                  </div>
                  <el-icon class="download-icon"><Download /></el-icon>
                </div>
                <span v-if="showTimestamp && message.senderId === currentUserId" class="timestamp own-timestamp">
                  {{ formatTime(message.timestamp) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 正在输入指示器 -->
        <div v-if="typingUsers.length > 0" class="typing-indicator">
          <div class="typing-content">
            <div class="typing-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <span class="typing-text">
              {{ getTypingText() }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 文件上传进度 -->
      <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
        <el-progress :percentage="uploadProgress" :show-text="false" />
        <span class="progress-text">上传中... {{ uploadProgress }}%</span>
      </div>
      
      <!-- 输入框 -->
      <div class="input-container">
        <div class="input-tools">
          <!-- 表情按钮 -->
          <el-popover 
            placement="top-start" 
            :width="300" 
            trigger="click"
            popper-class="emoji-popover"
          >
            <template #reference>
              <el-button text :icon="Smile" size="small" />
            </template>
            <div class="emoji-panel">
              <div 
                v-for="emoji in emojis" 
                :key="emoji"
                class="emoji-item"
                @click="insertEmoji(emoji)"
              >
                {{ emoji }}
              </div>
            </div>
          </el-popover>
          
          <!-- 文件上传 -->
          <el-upload
            ref="fileUpload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeUpload"
            :on-progress="handleUploadProgress"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
            :auto-upload="true"
          >
            <el-button text :icon="Paperclip" size="small" />
          </el-upload>
          
          <!-- 图片上传 -->
          <el-upload
            ref="imageUpload"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :before-upload="beforeImageUpload"
            :on-progress="handleUploadProgress"
            :on-success="handleImageUploadSuccess"
            :on-error="handleUploadError"
            :show-file-list="false"
            :auto-upload="true"
            accept="image/*"
          >
            <el-button text :icon="Picture" size="small" />
          </el-upload>
        </div>
        
        <!-- 输入框 -->
        <el-input
          ref="messageInput"
          v-model="inputMessage"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入消息..."
          @keydown="handleKeyDown"
          @input="handleInput"
          @focus="handleInputFocus"
          @blur="handleInputBlur"
        />
        
        <!-- 发送按钮 -->
        <el-button 
          type="primary" 
          :icon="Promotion"
          :disabled="!inputMessage.trim()"
          @click="sendMessage"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Setting, Close, InfoFilled, Document, Download,
  Smile, Paperclip, Picture, Promotion
} from '@element-plus/icons-vue'
import meetingApi from '@/api/meeting'
import { useUserStore } from '@/stores/user'

export default {
  name: 'ChatComponent',
  components: {
    ChatDotRound, Setting, Close, InfoFilled, Document, Download,
    Smile, Paperclip, Picture, Promotion
  },
  props: {
    meetingId: {
      type: String,
      required: true
    },
    participants: {
      type: Array,
      default: () => []
    }
  },
  emits: ['close', 'new-message'],
  setup(props, { emit }) {
    const userStore = useUserStore()
    const currentUserId = computed(() => userStore.userInfo?.id)
    
    // 组件引用
    const messageList = ref()
    const messageInput = ref()
    const fileUpload = ref()
    const imageUpload = ref()
    
    // 数据状态
    const messages = ref([])
    const inputMessage = ref('')
    const typingUsers = ref([])
    const onlineCount = computed(() => props.participants.length)
    
    // 设置状态
    const showSettings = ref(false)
    const notificationEnabled = ref(true)
    const showTimestamp = ref(true)
    const fontSize = ref('medium')
    
    // 上传状态
    const uploadProgress = ref(0)
    const uploadUrl = computed(() => `/api/meeting/${props.meetingId}/upload`)
    const uploadHeaders = computed(() => ({
      'Authorization': `Bearer ${userStore.token}`
    }))
    
    // 输入状态
    const isTyping = ref(false)
    const typingTimer = ref(null)
    
    // 表情列表
    const emojis = [
      '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
      '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
      '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
      '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
      '😖', '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬',
      '🤯', '😳', '🥵', '🥶', '😱', '😨', '😰', '😥', '😓', '🤗',
      '🤔', '🤭', '🤫', '🤥', '😶', '😐', '😑', '😬', '🙄', '😯',
      '😦', '😧', '😮', '😲', '🥱', '😴', '🤤', '😪', '😵', '🤐',
      '🥴', '🤢', '🤮', '🤧', '😷', '🤒', '🤕', '🤑', '🤠', '😈',
      '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉',
      '👆', '🖕', '👇', '☝️', '👋', '🤚', '🖐️', '✋', '🖖', '👏',
      '🙌', '🤲', '🤝', '🙏', '✍️', '💪', '🦾', '🦿', '🦵', '🦶'
    ]
    
    // 格式化时间
    const formatTime = (timestamp) => {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (date.toDateString() === now.toDateString()) { // 今天
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else {
        return date.toLocaleString('zh-CN', { 
          month: '2-digit', 
          day: '2-digit', 
          hour: '2-digit', 
          minute: '2-digit' 
        })
      }
    }
    
    // 格式化消息内容（支持链接、表情等）
    const formatMessageContent = (content) => {
      // 转换链接
      content = content.replace(
        /(https?:\/\/[^\s]+)/g,
        '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>'
      )
      
      // 转换换行
      content = content.replace(/\n/g, '<br>')
      
      return content
    }
    
    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    // 获取正在输入的文本
    const getTypingText = () => {
      const count = typingUsers.value.length
      if (count === 1) {
        return `${typingUsers.value[0].name} 正在输入...`
      } else if (count === 2) {
        return `${typingUsers.value[0].name} 和 ${typingUsers.value[1].name} 正在输入...`
      } else {
        return `${typingUsers.value[0].name} 等 ${count} 人正在输入...`
      }
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        if (messageList.value) {
          messageList.value.scrollTop = messageList.value.scrollHeight
        }
      })
    }
    
    // 发送消息
    const sendMessage = async () => {
      const content = inputMessage.value.trim()
      if (!content) return
      
      try {
        const message = {
          content,
          messageType: 'text',
          meetingId: props.meetingId
        }
        
        await meetingApi.sendChatMessage(message)
        inputMessage.value = ''
        stopTyping()
      } catch (error) {
        console.error('发送消息失败:', error)
        ElMessage.error('发送消息失败')
      }
    }
    
    // 插入表情
    const insertEmoji = (emoji) => {
      const input = messageInput.value
      const textarea = input.$refs.textarea
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      
      inputMessage.value = inputMessage.value.substring(0, start) + 
                          emoji + 
                          inputMessage.value.substring(end)
      
      nextTick(() => {
        textarea.focus()
        textarea.setSelectionRange(start + emoji.length, start + emoji.length)
      })
    }
    
    // 处理键盘事件
    const handleKeyDown = (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        sendMessage()
      }
    }
    
    // 处理输入事件
    const handleInput = () => {
      if (!isTyping.value) {
        startTyping()
      }
      
      // 重置输入计时器
      if (typingTimer.value) {
        clearTimeout(typingTimer.value)
      }
      
      typingTimer.value = setTimeout(() => {
        stopTyping()
      }, 3000)
    }
    
    // 开始输入
    const startTyping = () => {
      isTyping.value = true
      // 发送输入状态到服务器
      meetingApi.sendTypingStatus(props.meetingId, true)
    }
    
    // 停止输入
    const stopTyping = () => {
      isTyping.value = false
      if (typingTimer.value) {
        clearTimeout(typingTimer.value)
        typingTimer.value = null
      }
      // 发送输入状态到服务器
      meetingApi.sendTypingStatus(props.meetingId, false)
    }
    
    // 输入框焦点事件
    const handleInputFocus = () => {
      // 标记消息为已读
      markMessagesAsRead()
    }
    
    const handleInputBlur = () => {
      stopTyping()
    }
    
    // 文件上传前检查
    const beforeUpload = (file) => {
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isLt10M) {
        ElMessage.error('文件大小不能超过 10MB!')
        return false
      }
      return true
    }
    
    // 图片上传前检查
    const beforeImageUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5
      
      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB!')
        return false
      }
      return true
    }
    
    // 上传进度
    const handleUploadProgress = (event) => {
      uploadProgress.value = Math.round((event.loaded * 100) / event.total)
    }
    
    // 文件上传成功
    const handleUploadSuccess = (response) => {
      uploadProgress.value = 0
      
      const message = {
        content: response.url,
        messageType: 'file',
        fileName: response.fileName,
        fileSize: response.fileSize,
        meetingId: props.meetingId
      }
      
      meetingApi.sendChatMessage(message)
      ElMessage.success('文件上传成功')
    }
    
    // 图片上传成功
    const handleImageUploadSuccess = (response) => {
      uploadProgress.value = 0
      
      const message = {
        content: response.url,
        messageType: 'image',
        meetingId: props.meetingId
      }
      
      meetingApi.sendChatMessage(message)
      ElMessage.success('图片上传成功')
    }
    
    // 上传失败
    const handleUploadError = (error) => {
      uploadProgress.value = 0
      console.error('上传失败:', error)
      ElMessage.error('上传失败')
    }
    
    // 下载文件
    const downloadFile = (message) => {
      const link = document.createElement('a')
      link.href = message.content
      link.download = message.fileName
      link.click()
    }
    
    // 标记消息为已读
    const markMessagesAsRead = () => {
      // 实现消息已读逻辑
    }
    
    // 加载历史消息
    const loadMessages = async () => {
      try {
        const response = await meetingApi.getChatMessages(props.meetingId)
        messages.value = response.data || []
        scrollToBottom()
      } catch (error) {
        console.error('加载消息失败:', error)
      }
    }
    
    // 监听新消息
    const handleNewMessage = (message) => {
      messages.value.push(message)
      scrollToBottom()
      
      // 发送新消息事件
      emit('new-message', message)
      
      // 显示通知
      if (notificationEnabled.value && message.senderId !== currentUserId.value) {
        if ('Notification' in window && Notification.permission === 'granted') {
          new Notification(`${message.senderName}:`, {
            body: message.content,
            icon: message.senderAvatar
          })
        }
      }
    }
    
    // 监听输入状态
    const handleTypingStatus = (data) => {
      const { userId, userName, isTyping: typing } = data
      
      if (typing) {
        if (!typingUsers.value.find(user => user.id === userId)) {
          typingUsers.value.push({ id: userId, name: userName })
        }
      } else {
        typingUsers.value = typingUsers.value.filter(user => user.id !== userId)
      }
    }
    
    // 监听消息变化，自动滚动
    watch(messages, () => {
      scrollToBottom()
    }, { deep: true })
    
    // 组件挂载
    onMounted(() => {
      loadMessages()
      
      // 请求通知权限
      if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission()
      }
    })
    
    // 组件卸载
    onUnmounted(() => {
      stopTyping()
    })
    
    return {
      messageList,
      messageInput,
      fileUpload,
      imageUpload,
      messages,
      inputMessage,
      typingUsers,
      onlineCount,
      currentUserId,
      showSettings,
      notificationEnabled,
      showTimestamp,
      fontSize,
      uploadProgress,
      uploadUrl,
      uploadHeaders,
      emojis,
      formatTime,
      formatMessageContent,
      formatFileSize,
      getTypingText,
      sendMessage,
      insertEmoji,
      handleKeyDown,
      handleInput,
      handleInputFocus,
      handleInputBlur,
      beforeUpload,
      beforeImageUpload,
      handleUploadProgress,
      handleUploadSuccess,
      handleImageUploadSuccess,
      handleUploadError,
      downloadFile,
      handleNewMessage,
      handleTypingStatus
    }
  }
}
</script>

<style scoped>
.chat-component {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-weight: 600;
  color: #303133;
}

.online-badge {
  margin-left: 4px;
}

.header-right {
  display: flex;
  gap: 4px;
}

.chat-settings {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-list.font-small {
  font-size: 12px;
}

.message-list.font-medium {
  font-size: 14px;
}

.message-list.font-large {
  font-size: 16px;
}

.message-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  flex-direction: column;
}

.system-message {
  align-items: center;
}

.system-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 16px;
  font-size: 12px;
  color: #0369a1;
}

.own-message {
  align-items: flex-end;
}

.other-message {
  align-items: flex-start;
}

.user-message {
  max-width: 80%;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.sender-name {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.timestamp {
  font-size: 11px;
  color: #909399;
}

.message-content {
  position: relative;
}

.text-content {
  padding: 8px 12px;
  border-radius: 12px;
  background: #f0f0f0;
  color: #303133;
  word-wrap: break-word;
  line-height: 1.4;
}

.own-message .text-content {
  background: #409eff;
  color: white;
}

.own-timestamp {
  position: absolute;
  bottom: -16px;
  right: 0;
}

.image-content {
  position: relative;
}

.file-content {
  position: relative;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.file-info:hover {
  background: #e0e0e0;
}

.own-message .file-info {
  background: #409eff;
  color: white;
}

.own-message .file-info:hover {
  background: #337ecc;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
}

.file-size {
  font-size: 12px;
  opacity: 0.8;
}

.download-icon {
  opacity: 0.6;
}

.typing-indicator {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.typing-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f0f0f0;
  border-radius: 12px;
}

.typing-dots {
  display: flex;
  gap: 2px;
}

.typing-dots span {
  width: 4px;
  height: 4px;
  background: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

.typing-text {
  font-size: 12px;
  color: #606266;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.input-area {
  border-top: 1px solid #e4e7ed;
  background: white;
}

.upload-progress {
  padding: 8px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.progress-text {
  font-size: 12px;
  color: #606266;
  margin-left: 8px;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px 16px;
}

.input-tools {
  display: flex;
  gap: 4px;
}

.input-container .el-textarea {
  flex: 1;
}

.emoji-panel {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.emoji-item:hover {
  background: #f0f0f0;
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-header {
    padding: 12px;
  }
  
  .message-list {
    padding: 12px;
  }
  
  .user-message {
    max-width: 90%;
  }
  
  .input-container {
    padding: 8px 12px;
  }
  
  .emoji-panel {
    grid-template-columns: repeat(6, 1fr);
  }
}

/* 深色主题适配 */
.dark .chat-component {
  background: #1a1a1a;
  color: #e5e5e5;
}

.dark .chat-header {
  background: #2a2a2a;
  border-bottom-color: #3a3a3a;
}

.dark .chat-settings {
  background: #2a2a2a;
  border-bottom-color: #3a3a3a;
}

.dark .text-content {
  background: #3a3a3a;
  color: #e5e5e5;
}

.dark .file-info {
  background: #3a3a3a;
}

.dark .file-info:hover {
  background: #4a4a4a;
}

.dark .typing-content {
  background: #3a3a3a;
}

.dark .input-area {
  background: #1a1a1a;
  border-top-color: #3a3a3a;
}
</style>