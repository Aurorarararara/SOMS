<template>
  <div class="chat-room">
    <div class="chat-header">
      <h2>群聊</h2>
      <div class="header-actions">
        <div class="connection-status" :class="connectionStatus">
          <span class="status-indicator"></span>
          <span class="status-text">{{ connectionStatusText }}</span>
        </div>
        <el-button 
          type="primary" 
          size="small" 
          @click="showCreateGroupDialog"
        >
          创建群聊
        </el-button>
        <el-button 
          type="primary" 
          size="small" 
          @click="refreshChat"
          :loading="refreshing"
        >
          刷新
        </el-button>
      </div>
    </div>

    <div class="chat-container">
      <!-- 群聊列表 -->
      <div class="user-list">
        <div class="user-list-header">
          <h3>我的群聊 ({{ chatRooms.length }})</h3>
        </div>
        <div class="user-list-content">
          <div 
            v-for="room in chatRooms" 
            :key="room.roomId"
            class="user-item"
            @click="selectChatRoom(room)"
            :class="{ active: currentRoom?.roomId === room.roomId }"
          >
            <div class="user-avatar" :style="{ backgroundColor: getRoomColor(room.roomId) }">
              {{ room.roomName?.charAt(0) || '?' }}
            </div>
            <div class="user-info">
              <div class="user-name">{{ room.roomName }}</div>
              <div class="user-status">
                <span v-if="room.roomType === 'group'">群聊</span>
                <span v-else>一对一</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-main">
        <template v-if="currentRoom">
          <!-- 聊天室头部 -->
          <div class="chat-room-header">
            <div class="room-info">
              <h3>{{ currentRoom.roomName }}</h3>
              <div class="room-meta">
                <span v-if="currentRoom.roomType === 'group'">
                  {{ currentRoom.members?.length || 0 }} 位成员
                </span>
                <span v-else>
                  <span class="status-dot" :class="currentRoom.targetUserStatus || 'online'"></span>
                  {{ getUserStatusText(currentRoom.targetUserStatus || 'online') }}
                </span>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="message-list" ref="messageListRef">
            <div 
              v-for="message in messages" 
              :key="message.clientMsgID"
              class="message-item"
              :class="{ 'own-message': message.sendID === currentUser?.userID }"
            >
              <div class="message-avatar" :style="{ backgroundColor: getUserColor(message.sendID) }">
                {{ message.senderNickname?.charAt(0) || message.sendID?.charAt(0) || '?' }}
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="sender-name">{{ message.senderNickname || message.sendID }}</span>
                  <span class="message-time">{{ formatTime(message.createTime) }}</span>
                </div>
                <div class="message-text">{{ message.text }}</div>
              </div>
            </div>
            <!-- 消息加载提示 -->
            <div v-if="loadingMessages" class="loading-messages">
              <el-skeleton animated>
                <template #template>
                  <el-skeleton-item variant="circle" style="width: 36px; height: 36px;" />
                  <el-skeleton-item variant="text" style="margin-left: 12px; width: 60%;" />
                </template>
              </el-skeleton>
            </div>
          </div>

          <!-- 消息输入 -->
          <div class="message-input">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息... (按Enter发送，Shift+Enter换行)"
              @keydown.enter.exact.prevent="handleSendMessage"
              @keydown.enter.shift.exact.prevent="newMessage += '\n'"
            />
            <div class="input-actions">
              <el-button 
                type="primary" 
                @click="handleSendMessage" 
                :disabled="!newMessage.trim() || connectionStatus !== 'connected'"
                :loading="sendingMessage"
              >
                发送 (Enter)
              </el-button>
            </div>
          </div>
        </template>
        
        <!-- 未选择聊天室时的提示 -->
        <div v-else class="no-room-selected">
          <div class="empty-state">
            <el-icon :size="64"><ChatLineRound /></el-icon>
            <h3>选择一个群聊开始聊天</h3>
            <p>或创建一个新的群聊</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建群聊对话框 -->
    <el-dialog
      v-model="createGroupDialogVisible"
      title="创建群聊"
      width="500px"
    >
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="群名称">
          <el-input v-model="groupForm.roomName" placeholder="请输入群名称"></el-input>
        </el-form-item>
        <el-form-item label="群成员">
          <el-select
            v-model="groupForm.memberIds"
            multiple
            filterable
            placeholder="请选择群成员"
            style="width: 100%;"
          >
            <el-option
              v-for="user in onlineUsers"
              :key="user.userID"
              :label="user.nickname"
              :value="user.userID"
            >
              <div style="display: flex; align-items: center;">
                <span 
                  class="status-dot" 
                  :class="user.status"
                  style="margin-right: 8px;"
                ></span>
                {{ user.nickname }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createGroupDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createGroup" :loading="creatingGroup">创建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElNotification } from 'element-plus'
import { ChatLineRound } from '@element-plus/icons-vue'
import imApi from '@/api/im'
import dayjs from 'dayjs'

// 响应式数据
const userStore = useUserStore()
const messageListRef = ref(null)
const newMessage = ref('')
const messages = ref([])
const onlineUsers = ref([])
const chatRooms = ref([]) // 群聊列表
const currentUser = ref(null)
const currentRoom = ref(null) // 当前选中的聊天室
const connectionStatus = ref('disconnected')
const connectionStatusText = ref('未连接')
const currentUserStatus = ref('online')
const refreshing = ref(false)
const loadingMessages = ref(false)
const sendingMessage = ref(false)
const createGroupDialogVisible = ref(false)
const creatingGroup = ref(false)

// 群聊表单
const groupForm = reactive({
  roomName: '',
  memberIds: []
})

// 用户状态文本映射
const statusTextMap = {
  'online': '在线',
  'away': '离开',
  'busy': '忙碌',
  'offline': '离线'
}

// 获取用户状态文本
function getUserStatusText(status) {
  return statusTextMap[status] || status
}

// 颜色生成函数
function getUserColor(userId) {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9']
  let hash = 0
  for (let i = 0; i < userId.length; i++) {
    hash = userId.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

// 聊天室颜色生成函数
function getRoomColor(roomId) {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9']
  let hash = 0
  for (let i = 0; i < roomId.length; i++) {
    hash = roomId.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

// 格式化时间
function formatTime(timestamp) {
  return dayjs(timestamp * 1000).format('HH:mm:ss')
}

// 滚动到最新消息
function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 选择聊天室
async function selectChatRoom(room) {
  currentRoom.value = room
  await fetchChatMessages(room.roomId)
}

// 更新用户状态
async function updateUserStatus(status) {
  if (!currentUser.value || connectionStatus.value !== 'connected') {
    return
  }

  try {
    await imApi.updateUserStatus(currentUser.value.userID, status)
    ElMessage.success('状态更新成功')
    
    // 更新本地用户状态
    const userIndex = onlineUsers.value.findIndex(user => user.userID === currentUser.value.userID)
    if (userIndex !== -1) {
      // 创建新的用户对象以确保响应性
      const updatedUser = { ...onlineUsers.value[userIndex], status: status }
      onlineUsers.value.splice(userIndex, 1, updatedUser)
    }
    
    // 同时更新当前用户状态
    currentUser.value.status = status
  } catch (error) {
    console.error('更新用户状态失败:', error)
    ElMessage.error('状态更新失败: ' + error.message)
  }
}

// 处理消息发送
async function handleSendMessage() {
  if (!newMessage.value.trim() || !currentRoom.value || connectionStatus.value !== 'connected') {
    return
  }

  try {
    sendingMessage.value = true
    
    // 创建临时消息对象
    const tempMessage = {
      clientMsgID: 'temp_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9),
      sendID: currentUser.value.userID,
      senderNickname: currentUser.value.nickname,
      text: newMessage.value.trim(),
      createTime: Math.floor(Date.now() / 1000)
    }
    
    // 立即在前端显示消息
    messages.value.push(tempMessage)
    scrollToBottom()
    
    // 发送消息到服务器
    await imApi.sendMessage({
      text: newMessage.value.trim(),
      senderId: currentUser.value.userID,
      senderName: currentUser.value.nickname,
      roomId: currentRoom.value.roomId,
      receiverId: currentRoom.value.roomType === 'single' ? currentRoom.value.targetUserId : undefined
    })
    
    // 清空输入框
    newMessage.value = ''
    
    ElMessage.success('消息发送成功')
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('消息发送失败: ' + error.message)
  } finally {
    sendingMessage.value = false
  }
}

// 刷新聊天
async function refreshChat() {
  if (refreshing.value) return
  
  try {
    refreshing.value = true
    await Promise.all([
      fetchOnlineUsers(),
      fetchUserChatRooms()
    ])
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败: ' + error.message)
  } finally {
    refreshing.value = false
  }
}

// 显示创建群聊对话框
function showCreateGroupDialog() {
  // 重置表单
  groupForm.roomName = ''
  groupForm.memberIds = []
  
  // 默认添加当前用户
  if (currentUser.value) {
    groupForm.memberIds.push(currentUser.value.userID)
  }
  
  createGroupDialogVisible.value = true
}

// 创建群聊
async function createGroup() {
  if (!groupForm.roomName.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  
  if (groupForm.memberIds.length < 2) {
    ElMessage.warning('群聊至少需要2个成员')
    return
  }
  
  try {
    creatingGroup.value = true
    
    // 创建群聊
    const response = await imApi.createGroupChatRoom({
      roomName: groupForm.roomName,
      creatorId: currentUser.value.userID,
      memberIds: groupForm.memberIds.map(id => parseInt(id))
    })
    
    if (response.data) {
      ElMessage.success('群聊创建成功')
      createGroupDialogVisible.value = false
      // 刷新聊天室列表
      await fetchUserChatRooms()
      
      // 自动选择新创建的聊天室
      selectChatRoom(response.data)
    }
  } catch (error) {
    console.error('创建群聊失败:', error)
    ElMessage.error('创建群聊失败: ' + error.message)
  } finally {
    creatingGroup.value = false
  }
}

// 初始化聊天
async function initChat() {
  try {
    // 获取用户信息
    const userInfo = userStore.userInfo
    
    // 添加防异常处理，确保存在用户信息
    if (!userInfo || !userInfo.id) {
      // 如果用户信息不存在，尝试重新获取
      try {
        await userStore.getUserInfoAction(true); // 强制刷新
        const refreshedUserInfo = userStore.userInfo;
        
        if (!refreshedUserInfo || !refreshedUserInfo.id) {
          // 创建默认用户
          currentUser.value = {
            userID: '0',
            nickname: '游客用户'
          }
        } else {
          currentUser.value = {
            userID: refreshedUserInfo.id.toString(),
            nickname: refreshedUserInfo.realName || '用户' + refreshedUserInfo.id
          }
        }
      } catch (refreshError) {
        console.error('刷新用户信息失败:', refreshError);
        // 创建默认用户
        currentUser.value = {
          userID: '0',
          nickname: '游客用户'
        }
      }
    } else {
      currentUser.value = {
        userID: userInfo.id.toString(),
        nickname: userInfo.realName || '用户' + userInfo.id
      }
    }
    
    try {
      // 获取IM Token
      const tokenResponse = await imApi.getUserToken(currentUser.value.userID)
      console.log('获取IM Token:', tokenResponse.data)
    } catch (error) {
      console.error('获取IM Token失败，将使用默认模式:', error)
    }
    
    // 即使获取Token失败，也尝试获取用户列表和聊天室
    try {
      await Promise.all([
        fetchOnlineUsers(),
        fetchUserChatRooms()
      ])
    } catch (error) {
      console.error('获取聊天数据失败，将使用默认数据:', error)
    }
    
    
    connectionStatus.value = 'connected'
    connectionStatusText.value = '已连接'
    
    // 模拟定期更新在线用户状态
    setInterval(fetchOnlineUsers, 30000) // 每30秒更新一次
    
    ElNotification({
      title: '聊天室',
      message: '欢迎来到聊天室！',
      type: 'success',
      duration: 3000
    })
  } catch (error) {
    console.error('初始化聊天失败:', error)
    // 即使出错也连接，但显示为离线状态
    connectionStatus.value = 'disconnected'
    connectionStatusText.value = '连接失败'
    
    // 确保currentUser已定义
    if (!currentUser.value) {
      currentUser.value = {
        userID: '0',
        nickname: '游客用户'
      }
    }
    
    // 使用默认数据提供基本功能
    onlineUsers.value = [
      { userID: '1', nickname: '张三', status: 'online' },
      { userID: '2', nickname: '李四', status: 'away' },
      { userID: currentUser.value?.userID || '0', nickname: currentUser.value?.nickname || '游客用户', status: 'online' }
    ]
    
    messages.value = [
      {
        clientMsgID: 'msg1',
        sendID: '1',
        senderNickname: '张三',
        text: '大家好，欢迎来到聊天室！',
        createTime: Math.floor(Date.now() / 1000) - 300
      },
      {
        clientMsgID: 'msg2',
        sendID: '2',
        senderNickname: '李四',
        text: '聊天服务器连接失败，当前处于离线模式',
        createTime: Math.floor(Date.now() / 1000) - 5
      }
    ]
    
    ElMessage({
      message: '连接聊天服务器失败，将使用离线模式',
      type: 'warning',
      duration: 5000
    })
  }
}

// 获取在线用户列表
async function fetchOnlineUsers() {
  try {
    const response = await imApi.getOnlineUsers()
    console.log('获取在线用户列表响应:', response)
    onlineUsers.value = response.data.users || []
    
    // 更新当前用户状态
    const currentUserInList = onlineUsers.value.find(user => user.userID === currentUser.value?.userID)
    if (currentUserInList) {
      currentUserStatus.value = currentUserInList.status
    }
  } catch (error) {
    console.error('获取在线用户列表失败:', error)
    // 使用示例数据
    onlineUsers.value = [
      { userID: '1', nickname: '张三', status: 'online' },
      { userID: '2', nickname: '李四', status: 'away' },
      { userID: currentUser.value?.userID || '3', nickname: currentUser.value?.nickname || '我', status: 'online' }
    ]
  }
}

// 获取用户聊天室列表
async function fetchUserChatRooms() {
  try {
    const response = await imApi.getUserChatRooms(currentUser.value.userID)
    console.log('获取用户聊天室列表响应:', response)
    chatRooms.value = response.data || []
    
    // 如果还没有选中聊天室且有聊天室列表，自动选择第一个
    if (!currentRoom.value && chatRooms.value.length > 0) {
      selectChatRoom(chatRooms.value[0])
    }
  } catch (error) {
    console.error('获取用户聊天室列表失败:', error)
    chatRooms.value = []
  }
}

// 获取聊天记录
async function fetchChatMessages(roomId) {
  try {
    loadingMessages.value = true
    const response = await imApi.getChatMessages(roomId)
    console.log('获取聊天记录响应:', response)
    messages.value = response.data.messages || []
    scrollToBottom()
  } catch (error) {
    console.error('获取聊天记录失败:', error)
    // 使用示例数据
    messages.value = [
      {
        clientMsgID: 'msg1',
        sendID: '1',
        senderNickname: '张三',
        text: '大家好，欢迎来到聊天室！',
        createTime: Math.floor(Date.now() / 1000) - 300
      },
      {
        clientMsgID: 'msg2',
        sendID: '2',
        senderNickname: '李四',
        text: '你好，很高兴加入！',
        createTime: Math.floor(Date.now() / 1000) - 240
      }
    ]
    scrollToBottom()
  } finally {
    loadingMessages.value = false
  }
}

// 生命周期
onMounted(async () => {
  try {
    // 检查用户是否登录
    if (!userStore.isLoggedIn) {
      ElMessage.warning('您尚未登录，请先登录')
      return
    }
    
    // 如果用户信息未加载，尝试加载
    if (!userStore.userInfo || !userStore.userInfo.id) {
      try {
        await userStore.getUserInfoAction()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果有500错误，使用默认用户信息继续
        if (error.response?.status !== 500) {
          ElMessage.error('用户信息加载失败，请重新登录')
          return
        }
        // 对于500错误，使用默认用户继续
        ElMessage.warning('使用测试用户信息进入聊天室')
      }
    }
    
    // 初始化聊天
    await initChat()
    scrollToBottom()
  } catch (error) {
    console.error('聊天室初始化错误:', error)
    ElMessage.error('聊天室初始化错误: ' + error.message)
  }
})

onUnmounted(() => {
  // 清理工作
  console.log('聊天室组件卸载')
})
</script>

<style scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #ebeef5;
}

.chat-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
}

.connection-status.connected {
  background: #f0f9ff;
  color: #1890ff;
}

.connection-status.connecting {
  background: #fffbe6;
  color: #faad14;
}

.connection-status.disconnected {
  background: #fff2f0;
  color: #ff4d4f;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.connection-status.connected .status-indicator {
  background: #52c41a;
}

.connection-status.connecting .status-indicator {
  background: #faad14;
}

.connection-status.disconnected .status-indicator {
  background: #ff4d4f;
}

.chat-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.user-list {
  width: 240px;
  background: white;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.user-list-header {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-list-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.user-status-selector {
  width: 100px;
}

.user-list-content {
  flex: 1;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-item.active {
  background: #e6f7ff;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  margin-right: 12px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.user-status {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.status-dot.online {
  background: #52c41a;
}

.status-dot.away {
  background: #faad14;
}

.status-dot.busy {
  background: #ff4d4f;
}

.status-dot.offline {
  background: #bfbfbf;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #ebeef5;
}

.chat-room-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.room-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  align-items: center;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.own-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.own-message .message-avatar {
  margin-left: 12px;
  margin-right: 0;
}

.message-content {
  max-width: 70%;
  margin: 0 12px;
}

.own-message .message-content {
  text-align: right;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  gap: 8px;
}

.sender-name {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.message-time {
  font-size: 12px;
  color: #c0c4cc;
}

.message-text {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  word-break: break-word;
  text-align: left;
}

.own-message .message-text {
  background: #1890ff;
  color: white;
}

.loading-messages {
  padding: 16px;
}

.no-room-selected {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.empty-state {
  text-align: center;
  color: #909399;
}

.empty-state h3 {
  margin: 16px 0 8px;
  font-weight: 500;
}

.empty-state p {
  margin: 0;
}

.message-input {
  padding: 16px;
  background: white;
  border-top: 1px solid #ebeef5;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>