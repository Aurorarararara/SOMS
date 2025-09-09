<template>
  <div class="join-meeting-page">
    <div class="page-header">
      <h2 class="page-title">加入会议</h2>
      <p class="page-description">通过会议号或链接快速加入会议</p>
    </div>
    
    <el-card class="join-form-card">
      <el-tabs v-model="activeTab" class="join-tabs">
        <!-- 通过会议号加入 -->
        <el-tab-pane label="会议号" name="meetingId">
          <el-form
            ref="meetingIdFormRef"
            :model="meetingIdForm"
            :rules="meetingIdRules"
            label-width="120px"
            size="large"
          >
            <el-form-item label="会议号" prop="meetingId">
              <el-input
                v-model="meetingIdForm.meetingId"
                placeholder="请输入9-11位会议号"
                maxlength="11"
                clearable
              >
                <template #prefix>
                  <el-icon><VideoCamera /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="会议密码" prop="password">
              <el-input
                v-model="meetingIdForm.password"
                placeholder="请输入会议密码（如有）"
                show-password
                clearable
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="显示名称" prop="displayName">
              <el-input
                v-model="meetingIdForm.displayName"
                placeholder="请输入您的显示名称"
                maxlength="50"
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 通过链接加入 -->
        <el-tab-pane label="会议链接" name="meetingLink">
          <el-form
            ref="meetingLinkFormRef"
            :model="meetingLinkForm"
            :rules="meetingLinkRules"
            label-width="120px"
            size="large"
          >
            <el-form-item label="会议链接" prop="meetingLink">
              <el-input
                v-model="meetingLinkForm.meetingLink"
                placeholder="请粘贴会议链接"
                clearable
              >
                <template #prefix>
                  <el-icon><Link /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="显示名称" prop="displayName">
              <el-input
                v-model="meetingLinkForm.displayName"
                placeholder="请输入您的显示名称"
                maxlength="50"
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 设备检测 -->
      <el-divider content-position="left">设备检测</el-divider>
      
      <div class="device-check">
        <el-row :gutter="24">
          <el-col :span="12">
            <div class="device-item">
              <div class="device-header">
                <el-icon class="device-icon"><Microphone /></el-icon>
                <span class="device-label">麦克风</span>
                <el-switch 
                  v-model="deviceSettings.microphone"
                  @change="toggleMicrophone"
                />
              </div>
              <el-select 
                v-model="selectedMicrophone"
                placeholder="选择麦克风"
                :disabled="!deviceSettings.microphone"
                size="small"
                style="width: 100%; margin-top: 8px;"
              >
                <el-option
                  v-for="device in microphoneDevices"
                  :key="device.deviceId"
                  :label="device.label"
                  :value="device.deviceId"
                />
              </el-select>
              <div class="volume-indicator" v-if="deviceSettings.microphone">
                <div class="volume-bar">
                  <div 
                    class="volume-level"
                    :style="{ width: microphoneLevel + '%' }"
                  ></div>
                </div>
              </div>
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="device-item">
              <div class="device-header">
                <el-icon class="device-icon"><VideoCamera /></el-icon>
                <span class="device-label">摄像头</span>
                <el-switch 
                  v-model="deviceSettings.camera"
                  @change="toggleCamera"
                />
              </div>
              <el-select 
                v-model="selectedCamera"
                placeholder="选择摄像头"
                :disabled="!deviceSettings.camera"
                size="small"
                style="width: 100%; margin-top: 8px;"
              >
                <el-option
                  v-for="device in cameraDevices"
                  :key="device.deviceId"
                  :label="device.label"
                  :value="device.deviceId"
                />
              </el-select>
            </div>
          </el-col>
        </el-row>
        
        <!-- 视频预览 -->
        <div class="video-preview" v-if="deviceSettings.camera">
          <video 
            ref="previewVideo"
            autoplay
            muted
            playsinline
            class="preview-video"
          ></video>
          <div class="preview-overlay" v-if="!cameraStream">
            <el-icon class="preview-icon"><VideoCamera /></el-icon>
            <span>摄像头预览</span>
          </div>
        </div>
        
        <!-- 扬声器测试 -->
        <div class="speaker-test">
          <div class="device-header">
            <el-icon class="device-icon"><Microphone /></el-icon>
            <span class="device-label">扬声器</span>
            <el-button 
              size="small"
              @click="testSpeaker"
              :loading="speakerTesting"
            >
              {{ speakerTesting ? '测试中...' : '测试扬声器' }}
            </el-button>
          </div>
          <el-select 
            v-model="selectedSpeaker"
            placeholder="选择扬声器"
            size="small"
            style="width: 100%; margin-top: 8px;"
          >
            <el-option
              v-for="device in speakerDevices"
              :key="device.deviceId"
              :label="device.label"
              :value="device.deviceId"
            />
          </el-select>
        </div>
      </div>
      
      <!-- 加入设置 -->
      <el-divider content-position="left">加入设置</el-divider>
      
      <div class="join-settings">
        <el-checkbox-group v-model="joinSettings">
          <el-checkbox label="muteOnJoin">加入时静音</el-checkbox>
          <el-checkbox label="disableVideoOnJoin">加入时关闭视频</el-checkbox>
          <el-checkbox label="enableNotifications">启用通知</el-checkbox>
        </el-checkbox-group>
      </div>
      
      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button 
          type="primary" 
          @click="joinMeeting"
          :loading="joining"
          size="large"
        >
          加入会议
        </el-button>
      </div>
    </el-card>
    
    <!-- 最近会议 -->
    <el-card class="recent-meetings-card" v-if="recentMeetings.length > 0">
      <template #header>
        <div class="card-header">
          <span>最近的会议</span>
        </div>
      </template>
      
      <div class="recent-meetings-list">
        <div 
          v-for="meeting in recentMeetings"
          :key="meeting.id"
          class="recent-meeting-item"
          @click="quickJoinMeeting(meeting)"
        >
          <div class="meeting-info">
            <div class="meeting-title">{{ meeting.title }}</div>
            <div class="meeting-time">{{ formatTime(meeting.lastJoinTime) }}</div>
          </div>
          <div class="meeting-id">{{ meeting.meetingId }}</div>
          <el-button type="text" size="small">
            快速加入
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  VideoCamera, 
  Lock, 
  User, 
  Link, 
  Microphone
} from '@element-plus/icons-vue'
import meetingApi from '@/api/meeting'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

export default {
  name: 'JoinMeeting',
  components: {
    VideoCamera,
    Lock,
    User,
    Link,
    Microphone
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    
    const activeTab = ref('meetingId')
    const joining = ref(false)
    const speakerTesting = ref(false)
    
    const meetingIdFormRef = ref()
    const meetingLinkFormRef = ref()
    const previewVideo = ref()
    
    // 表单数据
    const meetingIdForm = reactive({
      meetingId: '',
      password: '',
      displayName: userStore.userInfo?.name || ''
    })
    
    const meetingLinkForm = reactive({
      meetingLink: '',
      displayName: userStore.userInfo?.name || ''
    })
    
    // 设备相关
    const deviceSettings = reactive({
      microphone: true,
      camera: true
    })
    
    const microphoneDevices = ref([])
    const cameraDevices = ref([])
    const speakerDevices = ref([])
    
    const selectedMicrophone = ref('')
    const selectedCamera = ref('')
    const selectedSpeaker = ref('')
    
    const microphoneLevel = ref(0)
    const cameraStream = ref(null)
    const audioContext = ref(null)
    const analyser = ref(null)
    
    // 加入设置
    const joinSettings = ref(['muteOnJoin'])
    
    // 最近会议
    const recentMeetings = ref([])
    
    // 表单验证规则
    const meetingIdRules = {
      meetingId: [
        { required: true, message: '请输入会议号', trigger: 'blur' },
        { 
          pattern: /^\d{9,11}$/, 
          message: '会议号应为9-11位数字', 
          trigger: 'blur' 
        }
      ],
      displayName: [
        { required: true, message: '请输入显示名称', trigger: 'blur' },
        { min: 1, max: 50, message: '名称长度在 1 到 50 个字符', trigger: 'blur' }
      ]
    }
    
    const meetingLinkRules = {
      meetingLink: [
        { required: true, message: '请输入会议链接', trigger: 'blur' },
        { 
          pattern: /^https?:\/\/.+/, 
          message: '请输入有效的会议链接', 
          trigger: 'blur' 
        }
      ],
      displayName: [
        { required: true, message: '请输入显示名称', trigger: 'blur' },
        { min: 1, max: 50, message: '名称长度在 1 到 50 个字符', trigger: 'blur' }
      ]
    }
    
    // 获取媒体设备
    const getMediaDevices = async () => {
      try {
        const devices = await navigator.mediaDevices.enumerateDevices()
        
        microphoneDevices.value = devices.filter(device => device.kind === 'audioinput')
        cameraDevices.value = devices.filter(device => device.kind === 'videoinput')
        speakerDevices.value = devices.filter(device => device.kind === 'audiooutput')
        
        // 设置默认设备
        if (microphoneDevices.value.length > 0) {
          selectedMicrophone.value = microphoneDevices.value[0].deviceId
        }
        if (cameraDevices.value.length > 0) {
          selectedCamera.value = cameraDevices.value[0].deviceId
        }
        if (speakerDevices.value.length > 0) {
          selectedSpeaker.value = speakerDevices.value[0].deviceId
        }
        
      } catch (error) {
        console.error('获取媒体设备失败:', error)
        ElMessage.error('无法获取媒体设备')
      }
    }
    
    // 切换麦克风
    const toggleMicrophone = async (enabled) => {
      if (enabled) {
        await startMicrophoneTest()
      } else {
        stopMicrophoneTest()
      }
    }
    
    // 开始麦克风测试
    const startMicrophoneTest = async () => {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          audio: {
            deviceId: selectedMicrophone.value ? { exact: selectedMicrophone.value } : undefined
          }
        })
        
        // 创建音频分析器
        audioContext.value = new (window.AudioContext || window.webkitAudioContext)()
        analyser.value = audioContext.value.createAnalyser()
        const source = audioContext.value.createMediaStreamSource(stream)
        source.connect(analyser.value)
        
        analyser.value.fftSize = 256
        const bufferLength = analyser.value.frequencyBinCount
        const dataArray = new Uint8Array(bufferLength)
        
        // 监听音量变化
        const updateVolume = () => {
          analyser.value.getByteFrequencyData(dataArray)
          const average = dataArray.reduce((a, b) => a + b) / bufferLength
          microphoneLevel.value = (average / 255) * 100
          
          if (deviceSettings.microphone) {
            requestAnimationFrame(updateVolume)
          }
        }
        updateVolume()
        
      } catch (error) {
        console.error('麦克风测试失败:', error)
        ElMessage.error('无法访问麦克风')
        deviceSettings.microphone = false
      }
    }
    
    // 停止麦克风测试
    const stopMicrophoneTest = () => {
      if (audioContext.value) {
        audioContext.value.close()
        audioContext.value = null
      }
      microphoneLevel.value = 0
    }
    
    // 切换摄像头
    const toggleCamera = async (enabled) => {
      if (enabled) {
        await startCameraPreview()
      } else {
        stopCameraPreview()
      }
    }
    
    // 开始摄像头预览
    const startCameraPreview = async () => {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: {
            deviceId: selectedCamera.value ? { exact: selectedCamera.value } : undefined,
            width: { ideal: 640 },
            height: { ideal: 480 }
          }
        })
        
        cameraStream.value = stream
        
        await nextTick()
        if (previewVideo.value) {
          previewVideo.value.srcObject = stream
        }
        
      } catch (error) {
        console.error('摄像头预览失败:', error)
        ElMessage.error('无法访问摄像头')
        deviceSettings.camera = false
      }
    }
    
    // 停止摄像头预览
    const stopCameraPreview = () => {
      if (cameraStream.value) {
        cameraStream.value.getTracks().forEach(track => track.stop())
        cameraStream.value = null
      }
      
      if (previewVideo.value) {
        previewVideo.value.srcObject = null
      }
    }
    
    // 测试扬声器
    const testSpeaker = async () => {
      speakerTesting.value = true
      
      try {
        // 播放测试音频
        const audio = new Audio('/audio/test-tone.mp3')
        if (selectedSpeaker.value && audio.setSinkId) {
          await audio.setSinkId(selectedSpeaker.value)
        }
        
        audio.volume = 0.5
        await audio.play()
        
        setTimeout(() => {
          audio.pause()
          speakerTesting.value = false
          ElMessage.success('扬声器测试完成')
        }, 2000)
        
      } catch (error) {
        console.error('扬声器测试失败:', error)
        ElMessage.error('扬声器测试失败')
        speakerTesting.value = false
      }
    }
    
    // 加入会议
    const joinMeeting = async () => {
      try {
        let formRef, formData
        
        if (activeTab.value === 'meetingId') {
          formRef = meetingIdFormRef.value
          formData = meetingIdForm
        } else {
          formRef = meetingLinkFormRef.value
          formData = meetingLinkForm
        }
        
        await formRef.validate()
        
        joining.value = true
        
        // 构建加入参数
        const joinParams = {
          displayName: formData.displayName,
          settings: {
            microphone: deviceSettings.microphone,
            camera: deviceSettings.camera,
            muteOnJoin: joinSettings.value.includes('muteOnJoin'),
            disableVideoOnJoin: joinSettings.value.includes('disableVideoOnJoin'),
            enableNotifications: joinSettings.value.includes('enableNotifications')
          },
          devices: {
            microphone: selectedMicrophone.value,
            camera: selectedCamera.value,
            speaker: selectedSpeaker.value
          }
        }
        
        let meetingId
        
        if (activeTab.value === 'meetingId') {
          joinParams.meetingId = formData.meetingId
          joinParams.password = formData.password
          meetingId = formData.meetingId
        } else {
          // 从链接中提取会议ID
          const linkMatch = formData.meetingLink.match(/\/meeting\/room\/(\w+)/)
          if (!linkMatch) {
            throw new Error('无效的会议链接')
          }
          meetingId = linkMatch[1]
          joinParams.meetingId = meetingId
        }
        
        // 验证会议
        await meetingApi.validateMeeting(joinParams)
        
        // 保存到最近会议
        const recentMeeting = {
          id: meetingId,
          title: `会议 ${meetingId}`,
          meetingId: joinParams.meetingId,
          lastJoinTime: new Date().toISOString()
        }
        
        const recent = JSON.parse(localStorage.getItem('recentMeetings') || '[]')
        const existingIndex = recent.findIndex(m => m.id === meetingId)
        
        if (existingIndex > -1) {
          recent[existingIndex] = recentMeeting
        } else {
          recent.unshift(recentMeeting)
          if (recent.length > 5) {
            recent.pop()
          }
        }
        
        localStorage.setItem('recentMeetings', JSON.stringify(recent))
        
        // 跳转到会议室
        router.push({
          path: `/meeting/room/${meetingId}`,
          query: joinParams
        })
        
      } catch (error) {
        console.error('加入会议失败:', error)
        ElMessage.error(error.message || '加入会议失败')
      } finally {
        joining.value = false
      }
    }
    
    // 快速加入最近会议
    const quickJoinMeeting = (meeting) => {
      meetingIdForm.meetingId = meeting.meetingId
      activeTab.value = 'meetingId'
    }
    
    // 格式化时间
    const formatTime = (time) => {
      return dayjs(time).format('MM-DD HH:mm')
    }
    
    // 返回
    const goBack = () => {
      router.back()
    }
    
    // 加载最近会议
    const loadRecentMeetings = () => {
      const recent = JSON.parse(localStorage.getItem('recentMeetings') || '[]')
      recentMeetings.value = recent
    }
    
    // 组件挂载
    onMounted(async () => {
      await getMediaDevices()
      loadRecentMeetings()
      
      // 自动开始设备测试
      if (deviceSettings.microphone) {
        await startMicrophoneTest()
      }
      if (deviceSettings.camera) {
        await startCameraPreview()
      }
    })
    
    // 组件卸载
    onUnmounted(() => {
      stopMicrophoneTest()
      stopCameraPreview()
    })
    
    return {
      activeTab,
      joining,
      speakerTesting,
      meetingIdFormRef,
      meetingLinkFormRef,
      previewVideo,
      meetingIdForm,
      meetingLinkForm,
      meetingIdRules,
      meetingLinkRules,
      deviceSettings,
      microphoneDevices,
      cameraDevices,
      speakerDevices,
      selectedMicrophone,
      selectedCamera,
      selectedSpeaker,
      microphoneLevel,
      cameraStream,
      joinSettings,
      recentMeetings,
      toggleMicrophone,
      toggleCamera,
      testSpeaker,
      joinMeeting,
      quickJoinMeeting,
      formatTime,
      goBack
    }
  }
}
</script>

<style scoped>
.join-meeting-page {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-description {
  color: #606266;
  margin: 0;
}

.join-form-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.join-tabs {
  margin-bottom: 24px;
}

.device-check {
  margin: 24px 0;
}

.device-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.device-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.device-icon {
  margin-right: 8px;
  color: #409eff;
}

.device-label {
  flex: 1;
  font-weight: 500;
}

.volume-indicator {
  margin-top: 8px;
}

.volume-bar {
  width: 100%;
  height: 4px;
  background: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
}

.volume-level {
  height: 100%;
  background: linear-gradient(to right, #67c23a, #e6a23c, #f56c6c);
  transition: width 0.1s;
}

.video-preview {
  position: relative;
  width: 100%;
  height: 200px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 16px;
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
}

.preview-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.speaker-test {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.join-settings {
  margin: 24px 0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.recent-meetings-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.recent-meetings-list {
  max-height: 300px;
  overflow-y: auto;
}

.recent-meeting-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recent-meeting-item:hover {
  background: #f5f7fa;
  margin: 0 -16px;
  padding: 12px 16px;
  border-radius: 4px;
}

.recent-meeting-item:last-child {
  border-bottom: none;
}

.meeting-info {
  flex: 1;
}

.meeting-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.meeting-time {
  font-size: 12px;
  color: #909399;
}

.meeting-id {
  font-family: monospace;
  color: #606266;
  margin-right: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .join-meeting-page {
    padding: 16px;
  }
  
  .el-col {
    margin-bottom: 16px;
  }
  
  .device-item {
    margin-bottom: 12px;
  }
  
  .video-preview {
    height: 150px;
  }
}

/* 深色主题适配 */
.dark .join-meeting-page {
  background: #1a1a1a;
}

.dark .page-title {
  color: #e5e5e5;
}

.dark .page-description {
  color: #a0a0a0;
}

.dark .device-item {
  border-color: #3a3a3a;
  background: #2a2a2a;
}

.dark .speaker-test {
  border-color: #3a3a3a;
  background: #2a2a2a;
}

.dark .recent-meeting-item {
  border-bottom-color: #3a3a3a;
}

.dark .recent-meeting-item:hover {
  background: #2a2a2a;
}

.dark .meeting-title {
  color: #e5e5e5;
}
</style>