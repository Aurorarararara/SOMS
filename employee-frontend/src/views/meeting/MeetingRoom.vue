<template>
  <div class="meeting-room" :class="{ 'fullscreen': isFullscreen }">
    <!-- 会议头部工具栏 -->
    <div class="meeting-header" v-show="!isFullscreen">
      <div class="meeting-info">
        <h3>{{ meetingInfo.title }}</h3>
        <span class="meeting-time">{{ formatMeetingTime }}</span>
        <span class="participant-count">{{ participants.length }} 人参与</span>
      </div>
      <div class="header-actions">
        <el-button size="small" @click="toggleParticipantPanel">
          <el-icon><User /></el-icon>
          参与者
        </el-button>
        <el-button size="small" @click="toggleChatPanel">
          <el-icon><ChatDotRound /></el-icon>
          聊天
        </el-button>
        <el-button size="small" @click="toggleWhiteboard">
          <el-icon><Edit /></el-icon>
          白板
        </el-button>
        <el-button size="small" type="danger" @click="leaveMeeting">
          <el-icon><Close /></el-icon>
          离开
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="meeting-content">
      <!-- 视频区域 -->
      <div class="video-area" :class="{ 'with-sidebar': showSidebar }">
        <!-- 主视频窗口 -->
        <div class="main-video">
          <video 
            ref="localVideo" 
            :srcObject="localStream" 
            autoplay 
            muted 
            playsinline
            class="video-element"
          ></video>
          <div class="video-overlay">
            <div class="user-info">
              <span class="user-name">{{ currentUser.name }} (我)</span>
            </div>
            <div class="video-controls" v-show="showControls">
              <el-button 
                :type="isMuted ? 'danger' : 'primary'"
                circle 
                @click="toggleMute"
              >
                <el-icon><Microphone v-if="!isMuted" /></el-icon>
              </el-button>
              <el-button 
                :type="isVideoOff ? 'danger' : 'primary'"
                circle 
                @click="toggleVideo"
              >
                <el-icon><VideoCamera v-if="!isVideoOff" /></el-icon>
              </el-button>
              <el-button 
                type="info"
                circle 
                @click="toggleFullscreen"
              >
                <el-icon><FullScreen v-if="!isFullscreen" /><Aim v-else /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 其他参与者视频 -->
        <div class="participant-videos" v-if="remoteParticipants.length > 0">
          <div 
            v-for="participant in remoteParticipants" 
            :key="participant.id"
            class="participant-video"
            @click="switchMainVideo(participant)"
          >
            <video 
              :ref="`remoteVideo_${participant.id}`"
              autoplay 
              playsinline
              class="video-element"
            ></video>
            <div class="participant-overlay">
              <span class="participant-name">{{ participant.name }}</span>
              <div class="participant-status">
                  <!-- 状态图标已移除 -->
                </div>
            </div>
          </div>
        </div>

        <!-- 白板区域 -->
        <div class="whiteboard-area" v-show="showWhiteboard">
          <whiteboard-component 
            :meeting-id="meetingId"
            @close="toggleWhiteboard"
          />
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="sidebar" v-show="showSidebar">
        <!-- 参与者面板 -->
        <div class="participant-panel" v-show="showParticipantPanel">
          <div class="panel-header">
            <h4>参与者 ({{ participants.length }})</h4>
            <el-button size="small" text @click="toggleParticipantPanel">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="participant-list">
            <div 
              v-for="participant in participants" 
              :key="participant.id"
              class="participant-item"
            >
              <el-avatar :size="32" :src="participant.avatar">
                {{ participant.name.charAt(0) }}
              </el-avatar>
              <div class="participant-info">
                <span class="name">{{ participant.name }}</span>
                <div class="status">
                    <el-tag v-if="participant.isHost" size="small" type="warning">主持人</el-tag>
                    <!-- 状态图标已移除 -->
                  </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 聊天面板 -->
        <div class="chat-panel" v-show="showChatPanel">
          <div class="panel-header">
            <h4>聊天</h4>
            <el-button size="small" text @click="toggleChatPanel">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="chat-messages" ref="chatMessages">
            <div 
              v-for="message in chatMessages" 
              :key="message.id"
              class="chat-message"
              :class="{ 'own-message': message.userId === currentUser.id }"
            >
              <div class="message-header">
                <span class="sender-name">{{ message.senderName }}</span>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>
              <div class="message-content">{{ message.content }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input 
              v-model="newMessage"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button @click="sendMessage">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部控制栏 -->
    <div class="meeting-controls" v-show="!isFullscreen || showControls">
      <div class="control-group">
        <el-button 
          :type="isMuted ? 'danger' : 'primary'"
          size="large"
          circle
          @click="toggleMute"
        >
          <el-icon><Microphone v-if="!isMuted" /></el-icon>
        </el-button>
        <el-button 
          :type="isVideoOff ? 'danger' : 'primary'"
          size="large"
          circle
          @click="toggleVideo"
        >
          <el-icon><VideoCamera v-if="!isVideoOff" /></el-icon>
        </el-button>
        <el-button 
          type="info"
          size="large"
          circle
          @click="shareScreen"
        >
          <el-icon><Monitor /></el-icon>
        </el-button>
        <el-button 
          type="danger"
          size="large"
          @click="leaveMeeting"
        >
          <el-icon><Close /></el-icon>
          离开会议
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, ChatDotRound, Edit, Close, Microphone,
  VideoCamera, FullScreen, Aim, Promotion,
  Monitor
} from '@element-plus/icons-vue'
import meetingApi, { meetingWebSocket } from '@/api/meeting'
import WhiteboardComponent from './components/WhiteboardComponent.vue'
import { useUserStore } from '@/stores/user'

export default {
  name: 'MeetingRoom',
  components: {
    WhiteboardComponent,
    User, ChatDotRound, Edit, Close, Microphone,
    VideoCamera, FullScreen, Aim, Promotion,
    Monitor
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    
    const meetingId = route.params.id
    const currentUser = computed(() => userStore.userInfo)
    
    // 响应式数据
    const meetingInfo = reactive({
      id: meetingId,
      title: '',
      startTime: null,
      endTime: null
    })
    
    const participants = ref([])
    const chatMessages = ref([])
    const newMessage = ref('')
    
    // 媒体流和WebRTC
    const localStream = ref(null)
    const localVideo = ref(null)
    const peerConnections = reactive({})
    const ws = ref(null)
    
    // UI状态
    const isFullscreen = ref(false)
    const showControls = ref(true)
    const showSidebar = ref(false)
    const showParticipantPanel = ref(false)
    const showChatPanel = ref(false)
    const showWhiteboard = ref(false)
    const isMuted = ref(false)
    const isVideoOff = ref(false)
    
    // 计算属性
    const remoteParticipants = computed(() => 
      participants.value.filter(p => p.id !== currentUser.value.id)
    )
    
    const formatMeetingTime = computed(() => {
      if (!meetingInfo.startTime) return ''
      const start = new Date(meetingInfo.startTime)
      return start.toLocaleTimeString()
    })
    
    // 初始化媒体设备
    const initializeMedia = async () => {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: true,
          audio: true
        })
        localStream.value = stream
        
        await nextTick()
        if (localVideo.value) {
          localVideo.value.srcObject = stream
        }
      } catch (error) {
        console.error('获取媒体设备失败:', error)
        ElMessage.error('无法访问摄像头或麦克风')
      }
    }
    
    // 初始化WebSocket连接
    const initializeWebSocket = () => {
      ws.value = meetingWebSocket.createConnection(meetingId, currentUser.value.id)
      
      ws.value.onopen = () => {
        console.log('WebSocket连接已建立')
      }
      
      ws.value.onmessage = (event) => {
        const data = JSON.parse(event.data)
        handleWebSocketMessage(data)
      }
      
      ws.value.onclose = () => {
        console.log('WebSocket连接已关闭')
      }
      
      ws.value.onerror = (error) => {
        console.error('WebSocket错误:', error)
      }
    }
    
    // 处理WebSocket消息
    const handleWebSocketMessage = (data) => {
      switch (data.type) {
        case 'participant-joined':
          participants.value.push(data.participant)
          break
        case 'participant-left':
          participants.value = participants.value.filter(p => p.id !== data.participantId)
          break
        case 'chat-message':
          chatMessages.value.push(data.message)
          scrollChatToBottom()
          break
        case 'participant-status-changed':
          updateParticipantStatus(data.participantId, data.status)
          break
      }
    }
    
    // 更新参与者状态
    const updateParticipantStatus = (participantId, status) => {
      const participant = participants.value.find(p => p.id === participantId)
      if (participant) {
        Object.assign(participant, status)
      }
    }
    
    // 控制功能
    const toggleMute = async () => {
      if (localStream.value) {
        const audioTrack = localStream.value.getAudioTracks()[0]
        if (audioTrack) {
          audioTrack.enabled = isMuted.value
          isMuted.value = !isMuted.value
          
          // 通知其他参与者
          await meetingApi.updateParticipantStatus(meetingId, {
            isMuted: isMuted.value
          })
        }
      }
    }
    
    const toggleVideo = async () => {
      if (localStream.value) {
        const videoTrack = localStream.value.getVideoTracks()[0]
        if (videoTrack) {
          videoTrack.enabled = isVideoOff.value
          isVideoOff.value = !isVideoOff.value
          
          // 通知其他参与者
          await meetingApi.updateParticipantStatus(meetingId, {
            isVideoOff: isVideoOff.value
          })
        }
      }
    }
    
    const toggleFullscreen = () => {
      isFullscreen.value = !isFullscreen.value
      if (isFullscreen.value) {
        document.documentElement.requestFullscreen()
      } else {
        document.exitFullscreen()
      }
    }
    
    const shareScreen = async () => {
      try {
        const screenStream = await navigator.mediaDevices.getDisplayMedia({
          video: true,
          audio: true
        })
        // TODO: 实现屏幕共享逻辑
        ElMessage.success('屏幕共享已开始')
      } catch (error) {
        console.error('屏幕共享失败:', error)
        ElMessage.error('屏幕共享失败')
      }
    }
    
    // 面板切换
    const toggleParticipantPanel = () => {
      showParticipantPanel.value = !showParticipantPanel.value
      showChatPanel.value = false
      showSidebar.value = showParticipantPanel.value
    }
    
    const toggleChatPanel = () => {
      showChatPanel.value = !showChatPanel.value
      showParticipantPanel.value = false
      showSidebar.value = showChatPanel.value
    }
    
    const toggleWhiteboard = () => {
      showWhiteboard.value = !showWhiteboard.value
    }
    
    // 聊天功能
    const sendMessage = async () => {
      if (!newMessage.value.trim()) return
      
      try {
        await meetingApi.sendMeetingMessage(meetingId, newMessage.value)
        newMessage.value = ''
      } catch (error) {
        console.error('发送消息失败:', error)
        ElMessage.error('发送消息失败')
      }
    }
    
    const scrollChatToBottom = () => {
      nextTick(() => {
        const chatContainer = document.querySelector('.chat-messages')
        if (chatContainer) {
          chatContainer.scrollTop = chatContainer.scrollHeight
        }
      })
    }
    
    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleTimeString()
    }
    
    // 离开会议
    const leaveMeeting = async () => {
      try {
        await ElMessageBox.confirm(
          '确定要离开会议吗？',
          '确认离开',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // 停止本地媒体流
        if (localStream.value) {
          localStream.value.getTracks().forEach(track => track.stop())
        }
        
        // 关闭WebSocket连接
        if (ws.value) {
          ws.value.close()
        }
        
        // 调用离开会议API
        await meetingApi.leaveMeeting(meetingId)
        
        router.push('/meeting')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('离开会议失败:', error)
        }
      }
    }
    
    // 加载会议信息
    const loadMeetingInfo = async () => {
      try {
        const response = await meetingApi.getMeetingDetail(meetingId)
        Object.assign(meetingInfo, response.data)
        
        const participantsResponse = await meetingApi.getMeetingParticipants(meetingId)
        participants.value = participantsResponse.data || []
        
        const messagesResponse = await meetingApi.getMeetingMessages(meetingId)
        chatMessages.value = messagesResponse.data || []
      } catch (error) {
        console.error('加载会议信息失败:', error)
        ElMessage.error('加载会议信息失败')
      }
    }
    
    // 组件挂载
    onMounted(async () => {
      await loadMeetingInfo()
      await initializeMedia()
      initializeWebSocket()
      
      // 加入会议
      try {
        await meetingApi.joinMeeting(meetingId)
      } catch (error) {
        console.error('加入会议失败:', error)
        ElMessage.error('加入会议失败')
      }
    })
    
    // 组件卸载
    onUnmounted(() => {
      if (localStream.value) {
        localStream.value.getTracks().forEach(track => track.stop())
      }
      if (ws.value) {
        ws.value.close()
      }
    })
    
    return {
      meetingId,
      meetingInfo,
      participants,
      remoteParticipants,
      chatMessages,
      newMessage,
      currentUser,
      localStream,
      localVideo,
      isFullscreen,
      showControls,
      showSidebar,
      showParticipantPanel,
      showChatPanel,
      showWhiteboard,
      isMuted,
      isVideoOff,
      formatMeetingTime,
      toggleMute,
      toggleVideo,
      toggleFullscreen,
      shareScreen,
      toggleParticipantPanel,
      toggleChatPanel,
      toggleWhiteboard,
      sendMessage,
      formatTime,
      leaveMeeting
    }
  }
}
</script>

<style scoped>
.meeting-room {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1a1a1a;
  color: white;
}

.meeting-room.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.8);
  border-bottom: 1px solid #333;
}

.meeting-info h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
}

.meeting-time,
.participant-count {
  font-size: 12px;
  color: #ccc;
  margin-right: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.meeting-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.video-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.video-area.with-sidebar {
  flex: 0 0 calc(100% - 300px);
}

.main-video {
  flex: 1;
  position: relative;
  background: #000;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 20px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.3));
}

.user-info {
  background: rgba(0, 0, 0, 0.6);
  padding: 8px 12px;
  border-radius: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.video-controls {
  display: flex;
  gap: 12px;
}

.participant-videos {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.5);
  overflow-x: auto;
}

.participant-video {
  width: 120px;
  height: 80px;
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.participant-video:hover {
  border-color: #409eff;
}

.participant-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  padding: 4px 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.participant-name {
  font-size: 12px;
  color: white;
}

.participant-status {
  display: flex;
  gap: 4px;
}

.whiteboard-area {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 100;
}

.sidebar {
  width: 300px;
  background: rgba(0, 0, 0, 0.9);
  border-left: 1px solid #333;
  display: flex;
  flex-direction: column;
}

.participant-panel,
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #333;
}

.panel-header h4 {
  margin: 0;
  font-size: 14px;
}

.participant-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.participant-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 4px;
  margin-bottom: 4px;
}

.participant-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.participant-info {
  flex: 1;
}

.participant-info .name {
  display: block;
  font-size: 14px;
  margin-bottom: 2px;
}

.participant-info .status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 12px;
}

.status-icon.muted {
  color: #f56c6c;
}

.status-icon.video-off {
  color: #e6a23c;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.chat-message {
  margin-bottom: 12px;
  padding: 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
}

.chat-message.own-message {
  background: rgba(64, 158, 255, 0.2);
  margin-left: 20px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
  color: #ccc;
}

.sender-name {
  font-weight: 500;
}

.message-content {
  font-size: 14px;
  line-height: 1.4;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #333;
}

.meeting-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.8);
  border-top: 1px solid #333;
}

.control-group {
  display: flex;
  gap: 16px;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .video-area.with-sidebar {
    flex: 1;
  }
  
  .sidebar {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 200;
  }
  
  .participant-videos {
    flex-direction: column;
    width: 100px;
    height: auto;
  }
  
  .participant-video {
    width: 80px;
    height: 60px;
  }
}
</style>