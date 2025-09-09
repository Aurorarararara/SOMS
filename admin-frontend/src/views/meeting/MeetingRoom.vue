<template>
  <div class="meeting-room">
    <!-- 会议头部信息 -->
    <div class="meeting-header">
      <div class="meeting-info">
        <h2>{{ meetingInfo.meetingTitle }}</h2>
        <span class="meeting-code">会议号: {{ meetingInfo.meetingCode }}</span>
        <span class="participant-count">参与者: {{ participants.length }}</span>
      </div>
      <div class="meeting-controls">
        <el-button @click="toggleMute" :type="isMuted ? 'danger' : 'primary'">
          <el-icon><Microphone v-if="!isMuted" /><MicrophoneSlash v-else /></el-icon>
          {{ isMuted ? '取消静音' : '静音' }}
        </el-button>
        <el-button @click="toggleVideo" :type="isVideoOn ? 'primary' : 'info'">
          <el-icon><VideoCamera v-if="isVideoOn" /><VideoCameraSlash v-else /></el-icon>
          {{ isVideoOn ? '关闭视频' : '开启视频' }}
        </el-button>
        <el-button @click="toggleScreenShare" :type="isScreenSharing ? 'warning' : 'primary'">
          <el-icon><Monitor /></el-icon>
          {{ isScreenSharing ? '停止共享' : '共享屏幕' }}
        </el-button>
        <el-button @click="toggleRecording" :type="isRecording ? 'danger' : 'success'">
          <el-icon><VideoPlay /></el-icon>
          {{ isRecording ? '停止录制' : '开始录制' }}
        </el-button>
        <el-button @click="showWhiteboard = !showWhiteboard" type="primary">
          <el-icon><Edit /></el-icon>
          白板
        </el-button>
        <el-button @click="showChat = !showChat" type="primary">
          <el-icon><ChatDotRound /></el-icon>
          聊天
        </el-button>
        <el-button @click="leaveMeeting" type="danger">
          <el-icon><Close /></el-icon>
          离开会议
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="meeting-content">
      <!-- 视频区域 -->
      <div class="video-area" :class="{ 'with-sidebar': showChat || showWhiteboard }">
        <!-- 主视频区域 -->
        <div class="main-video">
          <video
            ref="localVideo"
            :srcObject="localStream"
            autoplay
            muted
            playsinline
            class="local-video"
          ></video>
          <div class="video-overlay">
            <span class="user-name">{{ currentUser.name }} (我)</span>
            <div class="video-controls">
              <el-icon v-if="isMuted" class="muted-icon"><MicrophoneSlash /></el-icon>
              <el-icon v-if="!isVideoOn" class="video-off-icon"><VideoCameraSlash /></el-icon>
            </div>
          </div>
        </div>

        <!-- 远程视频网格 -->
        <div class="remote-videos">
          <div
            v-for="participant in remoteParticipants"
            :key="participant.userId"
            class="remote-video-container"
          >
            <video
              :ref="`remoteVideo_${participant.userId}`"
              autoplay
              playsinline
              class="remote-video"
            ></video>
            <div class="video-overlay">
              <span class="user-name">{{ participant.userName }}</span>
              <div class="video-controls">
                <el-icon v-if="participant.isMuted" class="muted-icon"><MicrophoneSlash /></el-icon>
                <el-icon v-if="!participant.isVideoOn" class="video-off-icon"><VideoCameraSlash /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 屏幕共享区域 -->
        <div v-if="screenShareStream" class="screen-share">
          <video
            ref="screenShareVideo"
            :srcObject="screenShareStream"
            autoplay
            playsinline
            class="screen-share-video"
          ></video>
          <div class="screen-share-overlay">
            <span>{{ screenShareUser }}正在共享屏幕</span>
            <el-button v-if="isScreenSharing" @click="stopScreenShare" size="small" type="danger">
              停止共享
            </el-button>
          </div>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div v-if="showChat || showWhiteboard" class="sidebar">
        <!-- 聊天面板 -->
        <div v-if="showChat" class="chat-panel">
          <div class="chat-header">
            <h3>聊天</h3>
            <el-button @click="showChat = false" size="small" text>
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
                <span class="sender-name">{{ message.userName }}</span>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>
              <div class="message-content">{{ message.message }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="newMessage"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button @click="sendMessage" type="primary">发送</el-button>
              </template>
            </el-input>
          </div>
        </div>

        <!-- 白板面板 -->
        <div v-if="showWhiteboard" class="whiteboard-panel">
          <div class="whiteboard-header">
            <h3>白板</h3>
            <div class="whiteboard-tools">
              <el-button-group>
                <el-button @click="setDrawingTool('pen')" :type="drawingTool === 'pen' ? 'primary' : 'default'" size="small">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button @click="setDrawingTool('eraser')" :type="drawingTool === 'eraser' ? 'primary' : 'default'" size="small">
                  <el-icon><Delete /></el-icon>
                </el-button>
                <el-button @click="clearWhiteboard" size="small">
                  <el-icon><RefreshLeft /></el-icon>
                </el-button>
              </el-button-group>
            </div>
            <el-button @click="showWhiteboard = false" size="small" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <canvas
            ref="whiteboardCanvas"
            class="whiteboard-canvas"
            @mousedown="startDrawing"
            @mousemove="draw"
            @mouseup="stopDrawing"
            @mouseleave="stopDrawing"
          ></canvas>
        </div>
      </div>
    </div>

    <!-- 参与者列表弹窗 -->
    <el-dialog v-model="showParticipants" title="参与者列表" width="400px">
      <div class="participants-list">
        <div
          v-for="participant in participants"
          :key="participant.userId"
          class="participant-item"
        >
          <el-avatar :src="participant.userAvatar" :size="32">
            {{ participant.userName.charAt(0) }}
          </el-avatar>
          <div class="participant-info">
            <span class="participant-name">{{ participant.userName }}</span>
            <span class="participant-role">{{ getRoleText(participant.role) }}</span>
          </div>
          <div class="participant-status">
            <el-icon v-if="participant.isMuted" class="status-icon muted"><MicrophoneSlash /></el-icon>
            <el-icon v-if="!participant.isVideoOn" class="status-icon video-off"><VideoCameraSlash /></el-icon>
            <span class="connection-status" :class="participant.connectionStatus">
              {{ getConnectionStatusText(participant.connectionStatus) }}
            </span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Microphone,
  MicrophoneSlash,
  VideoCamera,
  VideoCameraSlash,
  Monitor,
  VideoPlay,
  Edit,
  ChatDotRound,
  Close,
  Delete,
  RefreshLeft
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const meetingInfo = reactive({
  id: null,
  meetingTitle: '',
  meetingCode: '',
  hostId: null
})

const currentUser = reactive({
  id: null,
  name: '',
  avatar: ''
})

const participants = ref([])
const remoteParticipants = ref([])
const chatMessages = ref([])

// 媒体流
const localStream = ref(null)
const screenShareStream = ref(null)
const screenShareUser = ref('')

// 控制状态
const isMuted = ref(false)
const isVideoOn = ref(true)
const isScreenSharing = ref(false)
const isRecording = ref(false)
const showChat = ref(false)
const showWhiteboard = ref(false)
const showParticipants = ref(false)

// 聊天相关
const newMessage = ref('')

// 白板相关
const drawingTool = ref('pen')
const isDrawing = ref(false)

// WebRTC相关
let websocket = null
let peerConnections = new Map()
const iceServers = [
  { urls: 'stun:stun.l.google.com:19302' },
  { urls: 'stun:stun1.l.google.com:19302' }
]

// 组件引用
const localVideo = ref(null)
const screenShareVideo = ref(null)
const chatMessages_ref = ref(null)
const whiteboardCanvas = ref(null)

onMounted(async () => {
  await initializeMeeting()
  await setupMediaDevices()
  setupWebSocket()
  setupWhiteboard()
})

onUnmounted(() => {
  cleanup()
})

// 初始化会议
async function initializeMeeting() {
  try {
    const meetingId = route.params.id
    // 获取会议信息
    const response = await fetch(`/api/meetings/${meetingId}`)
    const result = await response.json()
    
    if (result.success) {
      Object.assign(meetingInfo, result.data)
      // 获取当前用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      Object.assign(currentUser, userInfo)
    } else {
      ElMessage.error('获取会议信息失败')
      router.push('/meetings')
    }
  } catch (error) {
    console.error('Initialize meeting error:', error)
    ElMessage.error('初始化会议失败')
  }
}

// 设置媒体设备
async function setupMediaDevices() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: true,
      audio: true
    })
    
    localStream.value = stream
    
    // 等待DOM更新后设置视频源
    await nextTick()
    if (localVideo.value) {
      localVideo.value.srcObject = stream
    }
  } catch (error) {
    console.error('Error accessing media devices:', error)
    ElMessage.error('无法访问摄像头和麦克风')
  }
}

// 设置WebSocket连接
function setupWebSocket() {
  const wsUrl = `ws://localhost:8080/ws/meeting`
  websocket = new WebSocket(wsUrl)
  
  websocket.onopen = () => {
    console.log('WebSocket connected')
    // 加入会议
    sendWebSocketMessage({
      type: 'join',
      meetingId: meetingInfo.id.toString(),
      userId: currentUser.id.toString(),
      userName: currentUser.name
    })
  }
  
  websocket.onmessage = (event) => {
    const message = JSON.parse(event.data)
    handleWebSocketMessage(message)
  }
  
  websocket.onclose = () => {
    console.log('WebSocket disconnected')
    // 尝试重连
    setTimeout(setupWebSocket, 3000)
  }
  
  websocket.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
}

// 处理WebSocket消息
async function handleWebSocketMessage(message) {
  switch (message.type) {
    case 'user-joined':
      handleUserJoined(message)
      break
    case 'user-left':
      handleUserLeft(message)
      break
    case 'offer':
      await handleOffer(message)
      break
    case 'answer':
      await handleAnswer(message)
      break
    case 'ice-candidate':
      await handleIceCandidate(message)
      break
    case 'user-muted':
      handleUserMuted(message)
      break
    case 'user-video-toggled':
      handleUserVideoToggled(message)
      break
    case 'screen-share':
      handleScreenShare(message)
      break
    case 'chat-message':
      handleChatMessage(message)
      break
    case 'whiteboard':
      handleWhiteboardUpdate(message)
      break
    case 'participants-list':
      participants.value = message.participants
      break
    case 'error':
      ElMessage.error(message.message)
      break
  }
}

// 处理用户加入
function handleUserJoined(message) {
  ElMessage.success(`${message.userName} 加入了会议`)
  // 创建新的peer connection
  createPeerConnection(message.userId)
}

// 处理用户离开
function handleUserLeft(message) {
  ElMessage.info(`用户离开了会议`)
  // 关闭peer connection
  const pc = peerConnections.get(message.userId)
  if (pc) {
    pc.close()
    peerConnections.delete(message.userId)
  }
  // 移除远程视频
  remoteParticipants.value = remoteParticipants.value.filter(p => p.userId !== message.userId)
}

// 创建WebRTC连接
function createPeerConnection(userId) {
  const pc = new RTCPeerConnection({ iceServers })
  
  // 添加本地流
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => {
      pc.addTrack(track, localStream.value)
    })
  }
  
  // 处理远程流
  pc.ontrack = (event) => {
    const [remoteStream] = event.streams
    // 添加到远程参与者列表
    const participant = remoteParticipants.value.find(p => p.userId === userId)
    if (participant) {
      participant.stream = remoteStream
    } else {
      remoteParticipants.value.push({
        userId,
        userName: `用户${userId}`,
        stream: remoteStream,
        isMuted: false,
        isVideoOn: true
      })
    }
    
    // 设置视频元素
    nextTick(() => {
      const videoElement = document.querySelector(`[data-user-id="${userId}"]`)
      if (videoElement) {
        videoElement.srcObject = remoteStream
      }
    })
  }
  
  // 处理ICE候选
  pc.onicecandidate = (event) => {
    if (event.candidate) {
      sendWebSocketMessage({
        type: 'ice-candidate',
        meetingId: meetingInfo.id.toString(),
        targetUserId: userId,
        candidate: event.candidate
      })
    }
  }
  
  peerConnections.set(userId, pc)
  return pc
}

// 发送WebSocket消息
function sendWebSocketMessage(message) {
  if (websocket && websocket.readyState === WebSocket.OPEN) {
    websocket.send(JSON.stringify(message))
  }
}

// 切换静音
function toggleMute() {
  isMuted.value = !isMuted.value
  if (localStream.value) {
    localStream.value.getAudioTracks().forEach(track => {
      track.enabled = !isMuted.value
    })
  }
  
  sendWebSocketMessage({
    type: 'mute',
    meetingId: meetingInfo.id.toString(),
    userId: currentUser.id.toString(),
    isMuted: isMuted.value
  })
}

// 切换视频
function toggleVideo() {
  isVideoOn.value = !isVideoOn.value
  if (localStream.value) {
    localStream.value.getVideoTracks().forEach(track => {
      track.enabled = isVideoOn.value
    })
  }
  
  sendWebSocketMessage({
    type: 'video-toggle',
    meetingId: meetingInfo.id.toString(),
    userId: currentUser.id.toString(),
    isVideoOn: isVideoOn.value
  })
}

// 切换屏幕共享
async function toggleScreenShare() {
  if (isScreenSharing.value) {
    stopScreenShare()
  } else {
    await startScreenShare()
  }
}

// 开始屏幕共享
async function startScreenShare() {
  try {
    const stream = await navigator.mediaDevices.getDisplayMedia({
      video: true,
      audio: true
    })
    
    screenShareStream.value = stream
    isScreenSharing.value = true
    screenShareUser.value = currentUser.name
    
    // 通知其他用户
    sendWebSocketMessage({
      type: 'screen-share',
      meetingId: meetingInfo.id.toString(),
      userId: currentUser.id.toString(),
      isSharing: true
    })
    
    // 监听屏幕共享结束
    stream.getVideoTracks()[0].onended = () => {
      stopScreenShare()
    }
  } catch (error) {
    console.error('Error starting screen share:', error)
    ElMessage.error('无法开始屏幕共享')
  }
}

// 停止屏幕共享
function stopScreenShare() {
  if (screenShareStream.value) {
    screenShareStream.value.getTracks().forEach(track => track.stop())
    screenShareStream.value = null
  }
  
  isScreenSharing.value = false
  screenShareUser.value = ''
  
  sendWebSocketMessage({
    type: 'screen-share',
    meetingId: meetingInfo.id.toString(),
    userId: currentUser.id.toString(),
    isSharing: false
  })
}

// 切换录制
function toggleRecording() {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

// 开始录制
async function startRecording() {
  try {
    const response = await fetch(`/api/meetings/${meetingInfo.id}/recording/start`, {
      method: 'POST'
    })
    const result = await response.json()
    
    if (result.success) {
      isRecording.value = true
      ElMessage.success('开始录制')
    } else {
      ElMessage.error('开始录制失败')
    }
  } catch (error) {
    console.error('Error starting recording:', error)
    ElMessage.error('开始录制失败')
  }
}

// 停止录制
async function stopRecording() {
  try {
    const response = await fetch(`/api/meetings/${meetingInfo.id}/recording/stop`, {
      method: 'POST'
    })
    const result = await response.json()
    
    if (result.success) {
      isRecording.value = false
      ElMessage.success('录制已停止')
    } else {
      ElMessage.error('停止录制失败')
    }
  } catch (error) {
    console.error('Error stopping recording:', error)
    ElMessage.error('停止录制失败')
  }
}

// 发送聊天消息
function sendMessage() {
  if (!newMessage.value.trim()) return
  
  sendWebSocketMessage({
    type: 'chat',
    meetingId: meetingInfo.id.toString(),
    userId: currentUser.id.toString(),
    userName: currentUser.name,
    message: newMessage.value
  })
  
  newMessage.value = ''
}

// 处理聊天消息
function handleChatMessage(message) {
  chatMessages.value.push({
    id: Date.now(),
    userId: message.userId,
    userName: message.userName,
    message: message.message,
    timestamp: message.timestamp
  })
  
  // 滚动到底部
  nextTick(() => {
    if (chatMessages_ref.value) {
      chatMessages_ref.value.scrollTop = chatMessages_ref.value.scrollHeight
    }
  })
}

// 设置白板
function setupWhiteboard() {
  // 白板初始化逻辑
}

// 离开会议
async function leaveMeeting() {
  try {
    await ElMessageBox.confirm('确定要离开会议吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 发送离开消息
    sendWebSocketMessage({
      type: 'leave',
      meetingId: meetingInfo.id.toString(),
      userId: currentUser.id.toString()
    })
    
    // 清理资源
    cleanup()
    
    // 跳转到会议列表
    router.push('/meetings')
  } catch {
    // 用户取消
  }
}

// 清理资源
function cleanup() {
  // 停止所有媒体流
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop())
  }
  
  if (screenShareStream.value) {
    screenShareStream.value.getTracks().forEach(track => track.stop())
  }
  
  // 关闭所有peer connections
  peerConnections.forEach(pc => pc.close())
  peerConnections.clear()
  
  // 关闭WebSocket
  if (websocket) {
    websocket.close()
  }
}

// 工具函数
function formatTime(timestamp) {
  return new Date(timestamp).toLocaleTimeString()
}

function getRoleText(role) {
  const roleMap = {
    host: '主持人',
    'co-host': '联合主持人',
    participant: '参与者'
  }
  return roleMap[role] || '参与者'
}

function getConnectionStatusText(status) {
  const statusMap = {
    connected: '已连接',
    disconnected: '已断开',
    reconnecting: '重连中'
  }
  return statusMap[status] || '未知'
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

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #2d2d2d;
  border-bottom: 1px solid #404040;
}

.meeting-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.meeting-code,
.participant-count {
  margin-right: 16px;
  color: #999;
  font-size: 14px;
}

.meeting-controls {
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
  padding: 16px;
}

.video-area.with-sidebar {
  flex: 0 0 70%;
}

.main-video {
  position: relative;
  flex: 1;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.local-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  bottom: 8px;
  left: 8px;
  right: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  gap: 8px;
}

.muted-icon,
.video-off-icon {
  color: #f56565;
}

.remote-videos {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.remote-video-container {
  position: relative;
  aspect-ratio: 16/9;
  background: #000;
  border-radius: 4px;
  overflow: hidden;
}

.remote-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.screen-share {
  position: relative;
  flex: 1;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.screen-share-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.screen-share-overlay {
  position: absolute;
  top: 8px;
  left: 8px;
  right: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  padding: 8px 12px;
  border-radius: 4px;
}

.sidebar {
  width: 30%;
  background: #2d2d2d;
  border-left: 1px solid #404040;
  display: flex;
  flex-direction: column;
}

.chat-panel,
.whiteboard-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header,
.whiteboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #404040;
}

.chat-header h3,
.whiteboard-header h3 {
  margin: 0;
  font-size: 16px;
}

.whiteboard-tools {
  display: flex;
  gap: 8px;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.chat-message {
  margin-bottom: 16px;
}

.chat-message.own-message {
  text-align: right;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
  color: #999;
}

.chat-message.own-message .message-header {
  flex-direction: row-reverse;
}

.message-content {
  background: #404040;
  padding: 8px 12px;
  border-radius: 8px;
  display: inline-block;
  max-width: 80%;
}

.chat-message.own-message .message-content {
  background: #007bff;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #404040;
}

.whiteboard-canvas {
  flex: 1;
  background: white;
  cursor: crosshair;
}

.participants-list {
  max-height: 400px;
  overflow-y: auto;
}

.participant-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.participant-item:last-child {
  border-bottom: none;
}

.participant-info {
  flex: 1;
  margin-left: 12px;
}

.participant-name {
  display: block;
  font-weight: 500;
}

.participant-role {
  font-size: 12px;
  color: #666;
}

.participant-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-icon {
  font-size: 16px;
}

.status-icon.muted {
  color: #f56565;
}

.status-icon.video-off {
  color: #ed8936;
}

.connection-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.connection-status.connected {
  background: #c6f6d5;
  color: #22543d;
}

.connection-status.disconnected {
  background: #fed7d7;
  color: #742a2a;
}

.connection-status.reconnecting {
  background: #feebc8;
  color: #7b341e;
}
</style>