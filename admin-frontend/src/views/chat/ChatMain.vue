<template>
  <div class="chat-main">
    <!-- 聊天侧边栏 -->
    <div class="chat-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话、联系人"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 功能标签页 -->
      <div class="tab-bar">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <el-icon>
            <component :is="tab.icon" />
          </el-icon>
          <span v-if="!sidebarCollapsed">{{ tab.label }}</span>
          <el-badge
            v-if="tab.badge && tab.badge > 0"
            :value="tab.badge"
            class="tab-badge"
          />
        </div>
      </div>

      <!-- 会话列表 -->
      <div v-if="activeTab === 'conversations'" class="conversation-list">
        <div
          v-for="conversation in filteredConversations"
          :key="conversation.conversationId"
          class="conversation-item"
          :class="{ 
            active: currentConversation?.conversationId === conversation.conversationId,
            pinned: conversation.isPinned,
            muted: conversation.isMuted
          }"
          @click="selectConversation(conversation)"
        >
          <div class="avatar-container">
            <el-avatar
              :size="40"
              :src="conversation.targetAvatar"
              :alt="conversation.targetName"
            >
              {{ conversation.targetName?.charAt(0) }}
            </el-avatar>
            <div
              v-if="conversation.conversationType === 'single' && conversation.isOnline"
              class="online-indicator"
            />
          </div>
          
          <div class="conversation-info">
            <div class="conversation-header">
              <span class="conversation-name">{{ conversation.targetName }}</span>
              <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
            </div>
            <div class="conversation-preview">
              <span class="last-message">
                <span v-if="conversation.lastSenderName && conversation.conversationType === 'group'">
                  {{ conversation.lastSenderName }}:
                </span>
                {{ getMessagePreview(conversation) }}
              </span>
              <div class="conversation-badges">
                <el-badge
                  v-if="conversation.unreadCount > 0"
                  :value="conversation.unreadCount"
                  :max="99"
                  class="unread-badge"
                />
                <el-icon v-if="conversation.isMuted" class="mute-icon">
                  <BellFilled />
                </el-icon>
                <el-icon v-if="conversation.isPinned" class="pin-icon">
                  <Top />
                </el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 联系人列表 -->
      <div v-else-if="activeTab === 'contacts'" class="contact-list">
        <div class="contact-section">
          <div class="section-header">
            <span>好友</span>
            <el-button size="small" text @click="showAddFriendDialog = true">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
          <div
            v-for="friend in filteredFriends"
            :key="friend.id"
            class="contact-item"
            @click="startChat(friend)"
          >
            <el-avatar :size="32" :src="friend.avatar">
              {{ friend.name?.charAt(0) }}
            </el-avatar>
            <div class="contact-info">
              <span class="contact-name">{{ friend.nickname || friend.name }}</span>
              <span class="contact-status" :class="{ online: friend.isOnline }">
                {{ friend.isOnline ? '在线' : '离线' }}
              </span>
            </div>
          </div>
        </div>

        <div class="contact-section">
          <div class="section-header">
            <span>群组</span>
            <el-button size="small" text @click="showCreateGroupDialog = true">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
          <div
            v-for="group in filteredGroups"
            :key="group.id"
            class="contact-item"
            @click="startGroupChat(group)"
          >
            <el-avatar :size="32" :src="group.groupAvatar">
              {{ group.groupName?.charAt(0) }}
            </el-avatar>
            <div class="contact-info">
              <span class="contact-name">{{ group.groupName }}</span>
              <span class="contact-status">{{ group.memberCount }}人</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 通知列表 -->
      <div v-else-if="activeTab === 'notifications'" class="notification-list">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon">
            <el-icon>
              <component :is="getNotificationIcon(notification.notificationType)" />
            </el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-text">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天主区域 -->
    <div class="chat-content">
      <div v-if="!currentConversation" class="empty-state">
        <div class="empty-icon">
          <el-icon size="64"><ChatDotRound /></el-icon>
        </div>
        <div class="empty-text">选择一个会话开始聊天</div>
      </div>

      <div v-else class="chat-window">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-info">
            <el-avatar
              :size="36"
              :src="currentConversation.targetAvatar"
              :alt="currentConversation.targetName"
            >
              {{ currentConversation.targetName?.charAt(0) }}
            </el-avatar>
            <div class="chat-title">
              <div class="chat-name">{{ currentConversation.targetName }}</div>
              <div class="chat-status">
                <span v-if="currentConversation.conversationType === 'single'">
                  {{ currentConversation.isOnline ? '在线' : '离线' }}
                </span>
                <span v-else>{{ currentConversation.memberCount }}人</span>
              </div>
            </div>
          </div>
          
          <div class="chat-actions">
            <el-button
              v-if="currentConversation.conversationType === 'group'"
              circle
              @click="showGroupInfo = true"
            >
              <el-icon><User /></el-icon>
            </el-button>
            <el-button circle @click="startVideoCall">
              <el-icon><VideoCamera /></el-icon>
            </el-button>
            <el-button circle @click="startVoiceCall">
              <el-icon><Phone /></el-icon>
            </el-button>
            <el-dropdown @command="handleChatCommand">
              <el-button circle>
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="search">搜索消息</el-dropdown-item>
                  <el-dropdown-item command="files">查看文件</el-dropdown-item>
                  <el-dropdown-item command="pin" :divided="true">
                    {{ currentConversation.isPinned ? '取消置顶' : '置顶会话' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="mute">
                    {{ currentConversation.isMuted ? '取消免打扰' : '免打扰' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="clear" divided>清空聊天记录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 消息列表 -->
        <div ref="messageContainer" class="message-container" @scroll="handleScroll">
          <div v-if="loadingMessages" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载更多消息...</span>
          </div>

          <div
            v-for="message in messages"
            :key="message.messageId"
            class="message-item"
            :class="{
              'message-self': message.senderId === currentUserId,
              'message-system': message.messageType === 'system'
            }"
          >
            <!-- 时间分隔线 -->
            <div v-if="shouldShowTimeBreak(message)" class="time-break">
              {{ formatMessageTime(message.createdAt) }}
            </div>

            <!-- 系统消息 -->
            <div v-if="message.messageType === 'system'" class="system-message">
              {{ message.content }}
            </div>

            <!-- 普通消息 -->
            <div v-else class="message-wrapper">
              <el-avatar
                v-if="message.senderId !== currentUserId"
                :size="32"
                :src="message.senderAvatar"
                class="message-avatar"
              >
                {{ message.senderName?.charAt(0) }}
              </el-avatar>

              <div class="message-content">
                <!-- 发送者名称（群聊中显示） -->
                <div
                  v-if="currentConversation.conversationType === 'group' && message.senderId !== currentUserId"
                  class="sender-name"
                >
                  {{ message.senderName }}
                </div>

                <!-- 回复消息 -->
                <div v-if="message.replyToMessage" class="reply-message">
                  <div class="reply-content">
                    <span class="reply-sender">{{ message.replyToMessage.senderName }}:</span>
                    {{ getMessagePreview(message.replyToMessage) }}
                  </div>
                </div>

                <!-- 消息气泡 -->
                <div class="message-bubble" :class="`message-${message.messageType}`">
                  <!-- 文本消息 -->
                  <div v-if="message.messageType === 'text'" class="text-message">
                    <div v-html="formatTextMessage(message.content)"></div>
                  </div>

                  <!-- 图片消息 -->
                  <div v-else-if="message.messageType === 'image'" class="image-message">
                    <el-image
                      :src="message.fileUrl"
                      :preview-src-list="[message.fileUrl]"
                      fit="cover"
                      class="message-image"
                    />
                  </div>

                  <!-- 文件消息 -->
                  <div v-else-if="message.messageType === 'file'" class="file-message">
                    <div class="file-info">
                      <el-icon class="file-icon"><Document /></el-icon>
                      <div class="file-details">
                        <div class="file-name">{{ message.fileName }}</div>
                        <div class="file-size">{{ formatFileSize(message.fileSize) }}</div>
                      </div>
                      <el-button size="small" @click="downloadFile(message)">
                        下载
                      </el-button>
                    </div>
                  </div>

                  <!-- 语音消息 -->
                  <div v-else-if="message.messageType === 'voice'" class="voice-message">
                    <el-button
                      circle
                      :icon="playingVoice === message.messageId ? Pause : VideoPlay"
                      @click="playVoice(message)"
                    />
                    <div class="voice-duration">{{ message.duration }}''</div>
                  </div>

                  <!-- 撤回消息 -->
                  <div v-if="message.isRecalled" class="recalled-message">
                    <el-icon><Delete /></el-icon>
                    <span>消息已撤回</span>
                  </div>
                </div>

                <!-- 消息状态 -->
                <div class="message-status">
                  <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                  <span
                    v-if="message.senderId === currentUserId && currentConversation.conversationType === 'group'"
                    class="read-status"
                    @click="showReadStatus(message)"
                  >
                    {{ message.readCount }}/{{ currentConversation.memberCount - 1 }}已读
                  </span>
                </div>
              </div>

              <!-- 消息操作菜单 -->
              <el-dropdown
                v-if="!message.isRecalled"
                trigger="contextmenu"
                @command="handleMessageCommand"
              >
                <div class="message-menu-trigger"></div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="`reply_${message.messageId}`">回复</el-dropdown-item>
                    <el-dropdown-item :command="`forward_${message.messageId}`">转发</el-dropdown-item>
                    <el-dropdown-item :command="`copy_${message.messageId}`">复制</el-dropdown-item>
                    <el-dropdown-item
                      v-if="message.senderId === currentUserId"
                      :command="`recall_${message.messageId}`"
                      divided
                    >
                      撤回
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <!-- 回复消息预览 -->
          <div v-if="replyingMessage" class="replying-preview">
            <div class="reply-info">
              <span>回复 {{ replyingMessage.senderName }}:</span>
              <span>{{ getMessagePreview(replyingMessage) }}</span>
            </div>
            <el-button size="small" text @click="cancelReply">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>

          <!-- 工具栏 -->
          <div class="input-toolbar">
            <el-button circle @click="showEmojiPicker = !showEmojiPicker">
              <el-icon><Sunny /></el-icon>
            </el-button>
            <el-upload
              :show-file-list="false"
              :before-upload="handleFileUpload"
              accept="image/*"
            >
              <el-button circle>
                <el-icon><Picture /></el-icon>
              </el-button>
            </el-upload>
            <el-upload
              :show-file-list="false"
              :before-upload="handleFileUpload"
            >
              <el-button circle>
                <el-icon><Folder /></el-icon>
              </el-button>
            </el-upload>
            <el-button circle @click="startVoiceRecord">
              <el-icon><Microphone /></el-icon>
            </el-button>
          </div>

          <!-- 输入框 -->
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              @keydown="handleInputKeydown"
              @input="handleInputChange"
            />
            <el-button
              type="primary"
              :disabled="!inputMessage.trim()"
              @click="sendMessage"
            >
              发送
            </el-button>
          </div>

          <!-- 表情选择器 -->
          <div v-if="showEmojiPicker" class="emoji-picker">
            <div class="emoji-grid">
              <span
                v-for="emoji in emojis"
                :key="emoji"
                class="emoji-item"
                @click="insertEmoji(emoji)"
              >
                {{ emoji }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 群组信息侧边栏 -->
    <div v-if="showGroupInfo && currentConversation?.conversationType === 'group'" class="group-info-sidebar">
      <div class="group-info-header">
        <h3>群组信息</h3>
        <el-button text @click="showGroupInfo = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="group-info-content">
        <div class="group-basic-info">
          <el-avatar :size="60" :src="currentConversation.targetAvatar">
            {{ currentConversation.targetName?.charAt(0) }}
          </el-avatar>
          <h4>{{ currentConversation.targetName }}</h4>
          <p>{{ currentConversation.memberCount }}名成员</p>
        </div>

        <div class="group-members">
          <div class="section-title">群成员</div>
          <div class="member-list">
            <div
              v-for="member in groupMembers"
              :key="member.userId"
              class="member-item"
            >
              <el-avatar :size="32" :src="member.userAvatar">
                {{ member.userName?.charAt(0) }}
              </el-avatar>
              <div class="member-info">
                <span class="member-name">{{ member.nickname || member.userName }}</span>
                <span class="member-role">{{ getRoleText(member.role) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建群组对话框 -->
    <el-dialog v-model="showCreateGroupDialog" title="创建群组" width="500px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="群组名称">
          <el-input v-model="groupForm.groupName" placeholder="请输入群组名称" />
        </el-form-item>
        <el-form-item label="群组描述">
          <el-input
            v-model="groupForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群组描述"
          />
        </el-form-item>
        <el-form-item label="选择成员">
          <el-select
            v-model="groupForm.memberIds"
            multiple
            placeholder="选择群成员"
            style="width: 100%"
          >
            <el-option
              v-for="user in allUsers"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="createGroup">创建</el-button>
      </template>
    </el-dialog>

    <!-- 添加好友对话框 -->
    <el-dialog v-model="showAddFriendDialog" title="添加好友" width="400px">
      <el-form :model="friendForm" label-width="80px">
        <el-form-item label="用户搜索">
          <el-input
            v-model="friendForm.keyword"
            placeholder="输入用户名或邮箱搜索"
            @input="searchUsers"
          />
        </el-form-item>
        <el-form-item v-if="searchResults.length > 0" label="搜索结果">
          <div class="search-results">
            <div
              v-for="user in searchResults"
              :key="user.id"
              class="search-result-item"
            >
              <el-avatar :size="32" :src="user.avatar">
                {{ user.name?.charAt(0) }}
              </el-avatar>
              <span>{{ user.name }}</span>
              <el-button size="small" @click="sendFriendRequest(user.id)">
                添加
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="申请消息">
          <el-input
            v-model="friendForm.message"
            placeholder="请输入申请消息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddFriendDialog = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  ChatDotRound,
  User,
  Bell,
  Plus,
  VideoCamera,
  Phone,
  More,
  Loading,
  Document,
  VideoPlay,
  Pause,
  Delete,
  Close,
  Sunny,
  Picture,
  Folder,
  Microphone,
  BellFilled,
  Top
} from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const sidebarCollapsed = ref(false)
const activeTab = ref('conversations')
const searchKeyword = ref('')
const currentConversation = ref(null)
const currentUserId = ref(1) // 从用户状态获取
const messages = ref([])
const conversations = ref([])
const friends = ref([])
const groups = ref([])
const notifications = ref([])
const groupMembers = ref([])

// 输入相关
const inputMessage = ref('')
const replyingMessage = ref(null)
const showEmojiPicker = ref(false)
const playingVoice = ref(null)

// 对话框状态
const showGroupInfo = ref(false)
const showCreateGroupDialog = ref(false)
const showAddFriendDialog = ref(false)

// 表单数据
const groupForm = reactive({
  groupName: '',
  description: '',
  memberIds: []
})

const friendForm = reactive({
  keyword: '',
  message: '我是你的同事，请通过好友申请'
})

// 其他状态
const loadingMessages = ref(false)
const searchResults = ref([])
const allUsers = ref([])

// 标签页配置
const tabs = computed(() => [
  {
    key: 'conversations',
    label: '会话',
    icon: 'ChatDotRound',
    badge: conversations.value.reduce((sum, conv) => sum + conv.unreadCount, 0)
  },
  {
    key: 'contacts',
    label: '联系人',
    icon: 'User',
    badge: 0
  },
  {
    key: 'notifications',
    label: '通知',
    icon: 'Bell',
    badge: notifications.value.filter(n => !n.isRead).length
  }
])

// 表情符号
const emojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩'
]

// 计算属性
const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  return conversations.value.filter(conv =>
    conv.targetName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const filteredFriends = computed(() => {
  if (!searchKeyword.value) return friends.value
  return friends.value.filter(friend =>
    friend.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const filteredGroups = computed(() => {
  if (!searchKeyword.value) return groups.value
  return groups.value.filter(group =>
    group.groupName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 组件挂载
onMounted(() => {
  loadConversations()
  loadFriends()
  loadGroups()
  loadNotifications()
  loadAllUsers()
  
  // 初始化WebSocket连接
  initWebSocket()
})

// 监听当前会话变化
watch(currentConversation, (newConv) => {
  if (newConv) {
    loadMessages(newConv.conversationId)
    if (newConv.conversationType === 'group') {
      loadGroupMembers(newConv.targetId)
    }
  }
})

// 方法实现
async function loadConversations() {
  try {
    const response = await fetch(`/api/chat/conversations?userId=${currentUserId.value}`)
    const result = await response.json()
    if (result.success) {
      conversations.value = result.data
    }
  } catch (error) {
    console.error('Load conversations error:', error)
  }
}

async function loadMessages(conversationId, page = 1) {
  if (loadingMessages.value) return
  
  loadingMessages.value = true
  try {
    const response = await fetch(
      `/api/chat/messages?conversationId=${conversationId}&page=${page}&size=20`
    )
    const result = await response.json()
    if (result.success) {
      if (page === 1) {
        messages.value = result.data.reverse()
      } else {
        messages.value.unshift(...result.data.reverse())
      }
      
      // 滚动到底部（新消息）或保持位置（历史消息）
      if (page === 1) {
        await nextTick()
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('Load messages error:', error)
  } finally {
    loadingMessages.value = false
  }
}

async function sendMessage() {
  if (!inputMessage.value.trim() || !currentConversation.value) return

  const messageData = {
    conversationId: currentConversation.value.conversationId,
    messageType: 'text',
    content: inputMessage.value.trim(),
    replyToMessageId: replyingMessage.value?.messageId
  }

  try {
    const response = await fetch('/api/chat/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(messageData)
    })

    const result = await response.json()
    if (result.success) {
      // 清空输入
      inputMessage.value = ''
      replyingMessage.value = null
      showEmojiPicker.value = false
      
      // 添加到消息列表
      messages.value.push(result.data)
      await nextTick()
      scrollToBottom()
      
      // 更新会话列表
      updateConversationLastMessage(result.data)
    }
  } catch (error) {
    console.error('Send message error:', error)
    ElMessage.error('发送消息失败')
  }
}

function selectConversation(conversation) {
  currentConversation.value = conversation
  showGroupInfo.value = false
  
  // 标记会话已读
  if (conversation.unreadCount > 0) {
    markConversationRead(conversation.conversationId)
  }
}

function startChat(friend) {
  // 创建或获取单聊会话
  createConversation('single', friend.id)
}

function startGroupChat(group) {
  // 创建或获取群聊会话
  createConversation('group', group.id)
}

async function createConversation(type, targetId) {
  try {
    const response = await fetch('/api/chat/conversations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: currentUserId.value,
        conversationType: type,
        targetId: targetId
      })
    })

    const result = await response.json()
    if (result.success) {
      currentConversation.value = result.data
      activeTab.value = 'conversations'
    }
  } catch (error) {
    console.error('Create conversation error:', error)
  }
}

// 工具函数
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

function formatMessageTime(time) {
  return new Date(time).toLocaleString()
}

function formatFileSize(size) {
  if (size < 1024) return size + 'B'
  if (size < 1048576) return (size / 1024).toFixed(1) + 'KB'
  return (size / 1048576).toFixed(1) + 'MB'
}

function getMessagePreview(message) {
  if (!message) return ''
  
  switch (message.messageType) {
    case 'text':
      return message.content
    case 'image':
      return '[图片]'
    case 'file':
      return `[文件] ${message.fileName}`
    case 'voice':
      return '[语音]'
    case 'video':
      return '[视频]'
    default:
      return '[消息]'
  }
}

function formatTextMessage(content) {
  // 处理表情、链接、@提及等
  return content
    .replace(/:\w+:/g, (match) => {
      // 表情符号转换
      return match
    })
    .replace(/@(\w+)/g, '<span class="mention">@$1</span>')
    .replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank">$1</a>')
}

function shouldShowTimeBreak(message) {
  const index = messages.value.findIndex(m => m.messageId === message.messageId)
  if (index === 0) return true
  
  const prevMessage = messages.value[index - 1]
  const timeDiff = new Date(message.createdAt) - new Date(prevMessage.createdAt)
  return timeDiff > 300000 // 5分钟
}

function scrollToBottom() {
  const container = document.querySelector('.message-container')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

function insertEmoji(emoji) {
  inputMessage.value += emoji
  showEmojiPicker.value = false
}

function handleInputKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// WebSocket连接
function initWebSocket() {
  // 实现WebSocket连接逻辑
  console.log('Initializing WebSocket connection...')
}

// 其他方法的实现...
// (由于篇幅限制，这里省略了部分方法的具体实现)

</script>

<style scoped>
.chat-main {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

.chat-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.chat-sidebar.collapsed {
  width: 60px;
}

.search-bar {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tab-bar {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.tab-item:hover {
  background: #f5f5f5;
}

.tab-item.active {
  color: #409eff;
  background: #ecf5ff;
}

.conversation-list,
.contact-list,
.notification-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.3s ease;
}

.conversation-item:hover {
  background: #f5f5f5;
}

.conversation-item.active {
  background: #e6f7ff;
}

.conversation-item.pinned {
  background: #fffbe6;
}

.avatar-container {
  position: relative;
  margin-right: 12px;
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #52c41a;
  border: 2px solid white;
  border-radius: 50%;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-name {
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  font-size: 12px;
  color: #999;
}

.conversation-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conversation-badges {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.empty-text {
  margin-top: 16px;
  font-size: 16px;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.chat-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-title {
  display: flex;
  flex-direction: column;
}

.chat-name {
  font-weight: 500;
  color: #333;
}

.chat-status {
  font-size: 12px;
  color: #999;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-item {
  margin-bottom: 16px;
}

.time-break {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin: 16px 0;
}

.system-message {
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  margin: 8px auto;
  max-width: 300px;
}

.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message-self .message-wrapper {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.message-self .message-content {
  align-items: flex-end;
}

.sender-name {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.message-bubble {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 8px 12px;
  position: relative;
  word-wrap: break-word;
}

.message-self .message-bubble {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.message-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
  color: #999;
}

.input-area {
  border-top: 1px solid #e8e8e8;
  background: white;
}

.replying-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
}

.input-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px;
}

.input-container :deep(.el-textarea) {
  flex: 1;
}

.emoji-picker {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 8px;
}

.emoji-item {
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  text-align: center;
  transition: background 0.3s ease;
}

.emoji-item:hover {
  background: #e6f7ff;
}

.group-info-sidebar {
  width: 300px;
  background: white;
  border-left: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.group-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.group-info-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.group-basic-info {
  text-align: center;
  margin-bottom: 24px;
}

.group-basic-info h4 {
  margin: 12px 0 4px;
}

.section-title {
  font-weight: 500;
  margin-bottom: 12px;
  color: #333;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 4px;
  transition: background 0.3s ease;
}

.member-item:hover {
  background: #f5f5f5;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.member-name {
  font-weight: 500;
  color: #333;
}

.member-role {
  font-size: 12px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-sidebar {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .chat-sidebar.show {
    transform: translateX(0);
  }

  .group-info-sidebar {
    position: absolute;
    right: 0;
    top: 0;
    height: 100%;
    z-index: 1000;
    transform: translateX(100%);
    transition: transform 0.3s ease;
  }

  .group-info-sidebar.show {
    transform: translateX(0);
  }
}
</style>