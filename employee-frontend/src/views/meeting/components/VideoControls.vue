<template>
  <div class="video-controls">
    <!-- 主要控制按钮 -->
    <div class="main-controls">
      <!-- 麦克风控制 -->
      <el-tooltip :content="audioEnabled ? '关闭麦克风' : '开启麦克风'" placement="top">
        <el-button
          :type="audioEnabled ? 'primary' : 'danger'"
          :icon="Microphone"
          circle
          size="large"
          @click="toggleAudio"
          :loading="audioLoading"
        />
      </el-tooltip>
      
      <!-- 摄像头控制 -->
      <el-tooltip :content="videoEnabled ? '关闭摄像头' : '开启摄像头'" placement="top">
        <el-button
          :type="videoEnabled ? 'primary' : 'danger'"
          :icon="VideoCamera"
          circle
          size="large"
          @click="toggleVideo"
          :loading="videoLoading"
        />
      </el-tooltip>
      
      <!-- 屏幕共享 -->
      <el-tooltip :content="screenSharing ? '停止共享' : '共享屏幕'" placement="top">
        <el-button
          :type="screenSharing ? 'warning' : 'default'"
          :icon="screenSharing ? Monitor : Share"
          circle
          size="large"
          @click="toggleScreenShare"
          :loading="screenShareLoading"
        />
      </el-tooltip>
      
      <!-- 聊天 -->
      <el-tooltip content="聊天" placement="top">
        <el-button
          :type="showChat ? 'primary' : 'default'"
          :icon="ChatDotRound"
          circle
          size="large"
          @click="$emit('toggle-chat')"
        >
          <el-badge 
            v-if="unreadCount > 0" 
            :value="unreadCount" 
            :max="99"
            class="chat-badge"
          />
        </el-button>
      </el-tooltip>
      
      <!-- 参与者 -->
      <el-tooltip content="参与者" placement="top">
        <el-button
          :type="showParticipants ? 'primary' : 'default'"
          :icon="User"
          circle
          size="large"
          @click="$emit('toggle-participants')"
        >
          <el-badge 
            :value="participantCount" 
            :max="99"
            class="participant-badge"
          />
        </el-button>
      </el-tooltip>
      
      <!-- 更多选项 -->
      <el-dropdown @command="handleCommand" placement="top">
        <el-button
          type="default"
          :icon="More"
          circle
          size="large"
        />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="whiteboard" :icon="Edit">
              白板
            </el-dropdown-item>
            <el-dropdown-item command="record" :icon="VideoCamera">
              {{ recording ? '停止录制' : '开始录制' }}
            </el-dropdown-item>
            <el-dropdown-item command="settings" :icon="Setting">
              设置
            </el-dropdown-item>
            <el-dropdown-item command="fullscreen" :icon="FullScreen">
              {{ isFullscreen ? '退出全屏' : '全屏' }}
            </el-dropdown-item>
            <el-dropdown-item divided command="leave" :icon="SwitchButton">
              离开会议
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      
      <!-- 挂断 -->
      <el-tooltip content="离开会议" placement="top">
        <el-button
          type="danger"
          :icon="PhoneSlash"
          circle
          size="large"
          @click="$emit('leave-meeting')"
        />
      </el-tooltip>
    </div>
    
    <!-- 次要控制 -->
    <div class="secondary-controls" v-if="showSecondaryControls">
      <!-- 音频设备选择 -->
      <div class="device-selector">
        <el-select
          v-model="selectedAudioInput"
          placeholder="选择麦克风"
          size="small"
          style="width: 150px"
          @change="changeAudioInput"
        >
          <el-option
            v-for="device in audioInputDevices"
            :key="device.deviceId"
            :label="device.label || '麦克风'"
            :value="device.deviceId"
          />
        </el-select>
        
        <el-select
          v-model="selectedAudioOutput"
          placeholder="选择扬声器"
          size="small"
          style="width: 150px"
          @change="changeAudioOutput"
        >
          <el-option
            v-for="device in audioOutputDevices"
            :key="device.deviceId"
            :label="device.label || '扬声器'"
            :value="device.deviceId"
          />
        </el-select>
      </div>
      
      <!-- 视频设备选择 -->
      <div class="device-selector">
        <el-select
          v-model="selectedVideoInput"
          placeholder="选择摄像头"
          size="small"
          style="width: 150px"
          @change="changeVideoInput"
        >
          <el-option
            v-for="device in videoInputDevices"
            :key="device.deviceId"
            :label="device.label || '摄像头'"
            :value="device.deviceId"
          />
        </el-select>
      </div>
      
      <!-- 音量控制 -->
      <div class="volume-control">
        <el-icon><VoiceOne /></el-icon>
        <el-slider
          v-model="volume"
          :min="0"
          :max="100"
          style="width: 100px; margin: 0 8px"
          @change="changeVolume"
        />
        <span class="volume-text">{{ volume }}%</span>
      </div>
    </div>
    
    <!-- 网络状态指示器 -->
    <div class="network-status">
      <el-tooltip :content="networkStatusText" placement="top">
        <div class="network-indicator" :class="networkStatus">
          <el-icon><Connection /></el-icon>
          <div class="signal-bars">
            <div class="bar" :class="{ active: networkQuality >= 1 }"></div>
            <div class="bar" :class="{ active: networkQuality >= 2 }"></div>
            <div class="bar" :class="{ active: networkQuality >= 3 }"></div>
            <div class="bar" :class="{ active: networkQuality >= 4 }"></div>
          </div>
        </div>
      </el-tooltip>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Microphone, VideoCamera,
  Monitor, Share, ChatDotRound, User, More, Edit, Setting,
  FullScreen, SwitchButton, PhoneSlash, VoiceOne, Connection
} from '@element-plus/icons-vue'

export default {
  name: 'VideoControls',
  components: {
    Microphone, VideoCamera,
    Monitor, Share, ChatDotRound, User, More, Edit, Setting,
    FullScreen, SwitchButton, PhoneSlash, VoiceOne, Connection
  },
  props: {
    audioEnabled: {
      type: Boolean,
      default: true
    },
    videoEnabled: {
      type: Boolean,
      default: true
    },
    screenSharing: {
      type: Boolean,
      default: false
    },
    showChat: {
      type: Boolean,
      default: false
    },
    showParticipants: {
      type: Boolean,
      default: false
    },
    unreadCount: {
      type: Number,
      default: 0
    },
    participantCount: {
      type: Number,
      default: 1
    },
    recording: {
      type: Boolean,
      default: false
    },
    networkQuality: {
      type: Number,
      default: 4
    }
  },
  emits: [
    'toggle-audio', 'toggle-video', 'toggle-screen-share',
    'toggle-chat', 'toggle-participants', 'toggle-whiteboard',
    'toggle-recording', 'toggle-settings', 'toggle-fullscreen',
    'leave-meeting', 'change-audio-input', 'change-audio-output',
    'change-video-input', 'change-volume'
  ],
  setup(props, { emit }) {
    // 加载状态
    const audioLoading = ref(false)
    const videoLoading = ref(false)
    const screenShareLoading = ref(false)
    
    // 设备列表
    const audioInputDevices = ref([])
    const audioOutputDevices = ref([])
    const videoInputDevices = ref([])
    
    // 选中的设备
    const selectedAudioInput = ref('')
    const selectedAudioOutput = ref('')
    const selectedVideoInput = ref('')
    
    // 音量控制
    const volume = ref(80)
    
    // 显示次要控制
    const showSecondaryControls = ref(false)
    
    // 全屏状态
    const isFullscreen = ref(false)
    
    // 网络状态
    const networkStatus = computed(() => {
      if (props.networkQuality >= 4) return 'excellent'
      if (props.networkQuality >= 3) return 'good'
      if (props.networkQuality >= 2) return 'fair'
      return 'poor'
    })
    
    const networkStatusText = computed(() => {
      const statusMap = {
        excellent: '网络状况：优秀',
        good: '网络状况：良好',
        fair: '网络状况：一般',
        poor: '网络状况：较差'
      }
      return statusMap[networkStatus.value]
    })
    
    // 获取媒体设备
    const getMediaDevices = async () => {
      try {
        const devices = await navigator.mediaDevices.enumerateDevices()
        
        audioInputDevices.value = devices.filter(device => device.kind === 'audioinput')
        audioOutputDevices.value = devices.filter(device => device.kind === 'audiooutput')
        videoInputDevices.value = devices.filter(device => device.kind === 'videoinput')
        
        // 设置默认设备
        if (audioInputDevices.value.length > 0 && !selectedAudioInput.value) {
          selectedAudioInput.value = audioInputDevices.value[0].deviceId
        }
        if (audioOutputDevices.value.length > 0 && !selectedAudioOutput.value) {
          selectedAudioOutput.value = audioOutputDevices.value[0].deviceId
        }
        if (videoInputDevices.value.length > 0 && !selectedVideoInput.value) {
          selectedVideoInput.value = videoInputDevices.value[0].deviceId
        }
      } catch (error) {
        console.error('获取媒体设备失败:', error)
        ElMessage.error('获取媒体设备失败')
      }
    }
    
    // 切换音频
    const toggleAudio = async () => {
      audioLoading.value = true
      try {
        emit('toggle-audio')
      } finally {
        audioLoading.value = false
      }
    }
    
    // 切换视频
    const toggleVideo = async () => {
      videoLoading.value = true
      try {
        emit('toggle-video')
      } finally {
        videoLoading.value = false
      }
    }
    
    // 切换屏幕共享
    const toggleScreenShare = async () => {
      screenShareLoading.value = true
      try {
        emit('toggle-screen-share')
      } finally {
        screenShareLoading.value = false
      }
    }
    
    // 处理下拉菜单命令
    const handleCommand = async (command) => {
      switch (command) {
        case 'whiteboard':
          emit('toggle-whiteboard')
          break
        case 'record':
          if (props.recording) {
            try {
              await ElMessageBox.confirm(
                '确定要停止录制吗？录制内容将被保存。',
                '停止录制',
                {
                  confirmButtonText: '停止',
                  cancelButtonText: '取消',
                  type: 'warning'
                }
              )
              emit('toggle-recording')
            } catch {
              // 用户取消
            }
          } else {
            emit('toggle-recording')
          }
          break
        case 'settings':
          showSecondaryControls.value = !showSecondaryControls.value
          break
        case 'fullscreen':
          toggleFullscreen()
          break
        case 'leave':
          try {
            await ElMessageBox.confirm(
              '确定要离开会议吗？',
              '离开会议',
              {
                confirmButtonText: '离开',
                cancelButtonText: '取消',
                type: 'warning'
              }
            )
            emit('leave-meeting')
          } catch {
            // 用户取消
          }
          break
      }
    }
    
    // 切换全屏
    const toggleFullscreen = () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen()
        isFullscreen.value = true
      } else {
        document.exitFullscreen()
        isFullscreen.value = false
      }
    }
    
    // 更改音频输入设备
    const changeAudioInput = (deviceId) => {
      emit('change-audio-input', deviceId)
    }
    
    // 更改音频输出设备
    const changeAudioOutput = (deviceId) => {
      emit('change-audio-output', deviceId)
    }
    
    // 更改视频输入设备
    const changeVideoInput = (deviceId) => {
      emit('change-video-input', deviceId)
    }
    
    // 更改音量
    const changeVolume = (value) => {
      emit('change-volume', value / 100)
    }
    
    // 监听全屏状态变化
    const handleFullscreenChange = () => {
      isFullscreen.value = !!document.fullscreenElement
    }
    
    // 监听设备变化
    const handleDeviceChange = () => {
      getMediaDevices()
    }
    
    // 组件挂载
    onMounted(() => {
      getMediaDevices()
      document.addEventListener('fullscreenchange', handleFullscreenChange)
      navigator.mediaDevices?.addEventListener('devicechange', handleDeviceChange)
    })
    
    // 组件卸载
    onUnmounted(() => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange)
      navigator.mediaDevices?.removeEventListener('devicechange', handleDeviceChange)
    })
    
    return {
      audioLoading,
      videoLoading,
      screenShareLoading,
      audioInputDevices,
      audioOutputDevices,
      videoInputDevices,
      selectedAudioInput,
      selectedAudioOutput,
      selectedVideoInput,
      volume,
      showSecondaryControls,
      isFullscreen,
      networkStatus,
      networkStatusText,
      toggleAudio,
      toggleVideo,
      toggleScreenShare,
      handleCommand,
      changeAudioInput,
      changeAudioOutput,
      changeVideoInput,
      changeVolume
    }
  }
}
</script>

<style scoped>
.video-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(0, 0, 0, 0.8);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.main-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main-controls .el-button {
  width: 48px;
  height: 48px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.main-controls .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.main-controls .el-button.is-circle {
  position: relative;
}

.chat-badge,
.participant-badge {
  position: absolute;
  top: -8px;
  right: -8px;
}

.secondary-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.device-selector {
  display: flex;
  gap: 8px;
  align-items: center;
}

.volume-control {
  display: flex;
  align-items: center;
  color: white;
}

.volume-text {
  font-size: 12px;
  color: white;
  min-width: 35px;
}

.network-status {
  position: absolute;
  top: 16px;
  right: 16px;
}

.network-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  color: white;
  font-size: 14px;
}

.signal-bars {
  display: flex;
  gap: 2px;
  align-items: flex-end;
}

.bar {
  width: 3px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 1px;
  transition: background-color 0.3s;
}

.bar:nth-child(1) { height: 6px; }
.bar:nth-child(2) { height: 9px; }
.bar:nth-child(3) { height: 12px; }
.bar:nth-child(4) { height: 15px; }

.bar.active {
  background: #67c23a;
}

.network-indicator.excellent .bar.active {
  background: #67c23a;
}

.network-indicator.good .bar.active {
  background: #e6a23c;
}

.network-indicator.fair .bar.active {
  background: #f56c6c;
}

.network-indicator.poor .bar.active {
  background: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .video-controls {
    padding: 12px;
  }
  
  .main-controls {
    gap: 8px;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .main-controls .el-button {
    width: 40px;
    height: 40px;
  }
  
  .secondary-controls {
    flex-direction: column;
    gap: 12px;
  }
  
  .device-selector {
    flex-direction: column;
    width: 100%;
  }
  
  .device-selector .el-select {
    width: 100% !important;
  }
  
  .network-status {
    position: static;
    margin-top: 8px;
  }
}

/* 深色主题适配 */
.dark .video-controls {
  background: rgba(0, 0, 0, 0.9);
}

.dark .secondary-controls {
  background: rgba(255, 255, 255, 0.05);
}

/* 动画效果 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
  }
}

.main-controls .el-button.is-loading {
  animation: pulse 2s infinite;
}
</style>