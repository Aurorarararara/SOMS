<template>
  <div class="chat-container">
    <!-- 左侧联系人列表 -->
    <div class="contacts-panel">
      <div class="panel-header">
        <h2>一对一聊天</h2>
        <div class="header-actions">
          <el-button type="primary" size="small" @click="showCreateGroupDialog">
            <el-icon><Plus /></el-icon>
            新建群聊
          </el-button>
        </div>
      </div>
      
      <!-- 联系人搜索 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人或群组"
          prefix-icon="el-icon-search"
          clearable
        />
      </div>
      
      <!-- 联系人列表 -->
      <div class="contacts-list">
        <div class="list-header">
          <span>联系人</span>
        </div>
        <div
          v-for="user in filteredContacts"
          :key="user.userID"
          class="contact-item"
          @click="startChat(user)"
        >
          <div 
            class="avatar" 
            :style="{ backgroundColor: getUserColor(user.userID) }"
          >
            {{ user.nickname?.charAt(0) || user.userID?.charAt(0) || '?' }}
          </div>
          <div class="contact-info">
            <div class="contact-name">{{ user.nickname }}</div>
            <div class="status">
              <span class="status-dot" :class="user.status"></span>
              <span>{{ getUserStatusText(user.status) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-panel">
      <template v-if="currentRoom">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="room-info">
            <h3>{{ currentRoom.roomName }}</h3>
            <div class="room-meta">
              <span v-if="currentRoom.roomType === 'single'">
                <span class="status-dot" :class="currentRoom.targetUserStatus || 'online'"></span>
                {{ getUserStatusText(currentRoom.targetUserStatus || 'online') }}
              </span>
              <span v-else>
                {{ currentRoom.members?.length || 0 }} 位成员
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
            <div 
              class="message-avatar" 
              :style="{ backgroundColor: getUserColor(message.sendID) }"
            >
              {{ message.senderNickname?.charAt(0) || message.sendID?.charAt(0) || '?' }}
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="sender-name">{{ message.senderNickname }}</span>
                <span class="message-time">{{ formatTime(message.createTime) }}</span>
              </div>
              <div class="message-text">{{ message.text }}</div>
            </div>
          </div>
          
          <!-- 加载中提示 -->
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
              :disabled="!newMessage.trim()"
              :loading="sendingMessage"
            >
              发送
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 未选择聊天室时的提示 -->
      <div v-else class="no-chat-selected">
        <div class="empty-state">
          <el-icon :size="64"><ChatLineRound /></el-icon>
          <h3>选择一个联系人开始聊天</h3>
        </div>
      </div>
    </div>

    <!-- 创建群聊对话框 -->
    <el-dialog
      title="创建群聊"
      v-model="createGroupDialogVisible"
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
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElNotification } from 'element-plus'
import { Plus, ChatLineRound } from '@element-plus/icons-vue'
import imApi from '@/api/im'
import dayjs from 'dayjs'

// 响应式数据
const userStore = useUserStore()
const messageListRef = ref(null)
const newMessage = ref('')
const messages = ref([])
const onlineUsers = ref([])
const currentUser = ref(null)
const currentRoom = ref(null)
const searchKeyword = ref('')
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
  if (!userId) return colors[0]
  
  let hash = 0
  for (let i = 0; i < userId.toString().length; i++) {
    hash = userId.toString().charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) return ''
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

// 过滤后的联系人列表
const filteredContacts = computed(() => {
  if (!searchKeyword.value) return onlineUsers.value
  
  return onlineUsers.value.filter(user => 
    user.nickname.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 开始一对一聊天
async function startChat(user) {
  if (!currentUser.value || currentUser.value.userID === user.userID) {
    return
  }
  
  try {
    // 创建或获取一对一聊天室
    const response = await imApi.getOrCreateSingleChatRoom(
      parseInt(currentUser.value.userID),
      parseInt(user.userID)
    )
    
    if (response.data) {
      currentRoom.value = response.data
      await loadChatMessages(response.data.roomId)
    }
  } catch (error) {
    console.error('创建聊天室失败:', error)
    ElMessage.error('创建聊天室失败: ' + error.message)
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
      currentRoom.value = response.data
      await loadChatMessages(response.data.roomId)
      
      ElMessage.success('群聊创建成功')
      createGroupDialogVisible.value = false
    }
  } catch (error) {
    console.error('创建群聊失败:', error)
    ElMessage.error('创建群聊失败: ' + error.message)
  } finally {
    creatingGroup.value = false
  }
}

// 加载聊天消息
async function loadChatMessages(roomId) {
  try {
    loadingMessages.value = true
    const response = await imApi.getChatMessages(roomId)
    
    if (response.data && response.data.messages) {
      messages.value = response.data.messages
      scrollToBottom()
    } else {
      messages.value = []
    }
  } catch (error) {
    console.error('加载聊天消息失败:', error)
    messages.value = []
  } finally {
    loadingMessages.value = false
  }
}

// 加载在线用户列表
async function loadOnlineUsers() {
  try {
    const response = await imApi.getOnlineUsers()
    
    if (response.data && response.data.users) {
      // 过滤掉当前用户
      onlineUsers.value = response.data.users.filter(
        user => user.userID !== currentUser.value?.userID
      )
    }
  } catch (error) {
    console.error('加载在线用户列表失败:', error)
    onlineUsers.value = []
  }
}

// 处理消息发送
async function handleSendMessage() {
  if (!newMessage.value.trim() || !currentRoom.value) {
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
    
    // 准备消息数据
    const messageData = {
      roomId: currentRoom.value.roomId,
      senderId: currentUser.value.userID,
      senderName: currentUser.value.nickname,
      text: newMessage.value.trim()
    }
    
    // 如果是一对一聊天，添加接收者ID
    if (currentRoom.value.roomType === 'single' && currentRoom.value.targetUserId) {
      messageData.receiverId = currentRoom.value.targetUserId
    }
    
    // 发送消息到服务器
    await imApi.sendMessage(messageData)
    
    // 清空输入框
    newMessage.value = ''
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('消息发送失败: ' + error.message)
  } finally {
    sendingMessage.value = false
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
        await userStore.getUserInfoAction(true) // 强制刷新
        const refreshedUserInfo = userStore.userInfo
        
        if (!refreshedUserInfo || !refreshedUserInfo.id) {
          ElMessage.error('无法获取用户信息，请重新登录')
          return
        } else {
          currentUser.value = {
            userID: refreshedUserInfo.id.toString(),
            nickname: refreshedUserInfo.realName || '用户' + refreshedUserInfo.id
          }
        }
      } catch (error) {
        console.error('刷新用户信息失败:', error)
        ElMessage.error('无法获取用户信息，请重新登录')
        return
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
      
      // 加载在线用户列表
      await loadOnlineUsers()
      
      ElNotification({
        title: '聊天',
        message: '欢迎使用聊天功能！',
        type: 'success',
        duration: 3000
      })
    } catch (error) {
      console.error('初始化聊天失败:', error)
      ElMessage.error('初始化聊天失败: ' + error.message)
    }
  } catch (error) {
    console.error('聊天初始化错误:', error)
    ElMessage.error('聊天初始化错误: ' + error.message)
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
    
    // 初始化聊天
    await initChat()
    
    // 设置定时刷新
    setInterval(() => {
      loadOnlineUsers()
      if (currentRoom.value) {
        loadChatMessages(currentRoom.value.roomId)
      }
    }, 30000) // 每30秒刷新一次
    
  } catch (error) {
    console.error('聊天初始化错误:', error)
    ElMessage.error('聊天初始化错误: ' + error.message)
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100%;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
}

/* 左侧联系人面板 */
.contacts-panel {
  width: 280px;
  background: white;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e6e6e6;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h2 {
  margin: 0;
  font-size: 18px;
}

.search-bar {
  padding: 12px 16px;
  border-bottom: 1px solid #e6e6e6;
}

.list-header {
  padding: 12px 16px;
  font-size: 14px;
  color: #909399;
  border-bottom: 1px solid #f2f2f2;
  margin-top: 8px;
}

/* 联系人列表 */
.contacts-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f2f2f2;
  cursor: pointer;
  transition: background 0.2s;
}

.contact-item:hover {
  background: #f5f7fa;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  flex-shrink: 0;
}

.contact-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
}

.contact-name {
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
}

.status-dot.online {
  background: #67c23a;
}

.status-dot.away {
  background: #e6a23c;
}

.status-dot.busy {
  background: #f56c6c;
}

.status-dot.offline {
  background: #909399;
}

/* 聊天面板 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-info h3 {
  margin: 0;
  font-size: 16px;
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
  padding: 16px;
  overflow-y: auto;
  background: #f5f7fa;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  max-width: 80%;
}

.message-item.own-message {
  margin-left: auto;
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
  font-weight: bold;
  flex-shrink: 0;
}

.message-content {
  margin: 0 12px;
  background: white;
  padding: 8px 12px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-item.own-message .message-content {
  background: #e6f7ff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.sender-name {
  font-weight: 500;
  font-size: 14px;
  color: #606266;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-text {
  word-break: break-word;
  line-height: 1.5;
}

.message-input {
  padding: 16px;
  border-top: 1px solid #e6e6e6;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.no-chat-selected {
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

.loading-messages {
  padding: 16px 0;
}
</style>