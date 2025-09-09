<template>
  <el-dialog
    v-model="dialogVisible"
    title="加入会议"
    width="450px"
    :before-close="handleClose"
  >
    <div class="join-meeting-content">
      <div class="join-methods">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="会议号" name="code">
            <el-form
              ref="codeFormRef"
              :model="codeForm"
              :rules="codeRules"
              label-width="0"
              @submit.prevent
            >
              <el-form-item prop="meetingCode">
                <el-input
                  v-model="codeForm.meetingCode"
                  placeholder="请输入会议号"
                  size="large"
                  maxlength="20"
                  clearable
                >
                  <template #prepend>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="password">
                <el-input
                  v-model="codeForm.password"
                  placeholder="会议密码（如需要）"
                  size="large"
                  type="password"
                  show-password
                  clearable
                >
                  <template #prepend>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="会议链接" name="link">
            <el-form
              ref="linkFormRef"
              :model="linkForm"
              :rules="linkRules"
              label-width="0"
              @submit.prevent
            >
              <el-form-item prop="meetingLink">
                <el-input
                  v-model="linkForm.meetingLink"
                  placeholder="请粘贴会议链接"
                  size="large"
                  type="textarea"
                  :rows="3"
                  clearable
                >
                </el-input>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 设备检测 -->
      <div class="device-check">
        <h4>设备检测</h4>
        <div class="device-status">
          <div class="device-item">
            <el-icon :class="{ 'success': cameraStatus, 'error': !cameraStatus }">
              <VideoCamera v-if="cameraStatus" />
            </el-icon>
            <span>摄像头</span>
            <el-button 
              size="small" 
              text 
              @click="testCamera"
              :loading="testingCamera"
            >
              {{ cameraStatus ? '正常' : '检测' }}
            </el-button>
          </div>
          
          <div class="device-item">
            <el-icon :class="{ 'success': microphoneStatus, 'error': !microphoneStatus }">
              <Microphone v-if="microphoneStatus" />
            </el-icon>
            <span>麦克风</span>
            <el-button 
              size="small" 
              text 
              @click="testMicrophone"
              :loading="testingMicrophone"
            >
              {{ microphoneStatus ? '正常' : '检测' }}
            </el-button>
          </div>
        </div>
        
        <!-- 预览窗口 -->
        <div class="preview-container" v-show="showPreview">
          <video 
            ref="previewVideo" 
            autoplay 
            muted 
            playsinline
            class="preview-video"
          ></video>
          <div class="preview-controls">
            <el-button size="small" @click="togglePreviewMute">
          <el-icon><Microphone v-if="!previewMuted" /></el-icon>
        </el-button>
            <el-button size="small" @click="togglePreviewVideo">
          <el-icon><VideoCamera v-if="!previewVideoOff" /></el-icon>
        </el-button>
            <el-button size="small" @click="closePreview">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 加入设置 -->
      <div class="join-settings">
        <h4>加入设置</h4>
        <div class="settings-options">
          <el-checkbox v-model="joinSettings.muteOnJoin">
            加入时静音
          </el-checkbox>
          <el-checkbox v-model="joinSettings.videoOffOnJoin">
            加入时关闭视频
          </el-checkbox>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="joining"
          @click="handleJoin"
        >
          加入会议
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Key, Lock, VideoCamera, 
  Microphone, Close
} from '@element-plus/icons-vue'
import meetingApi from '@/api/meeting'

export default {
  name: 'JoinMeetingDialog',
  components: {
    Key, Lock, VideoCamera,
    Microphone, Close
  },
  props: {
    modelValue: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'joined'],
  setup(props, { emit }) {
    const codeFormRef = ref()
    const linkFormRef = ref()
    const previewVideo = ref()
    const activeTab = ref('code')
    const joining = ref(false)
    
    // 设备状态
    const cameraStatus = ref(false)
    const microphoneStatus = ref(false)
    const testingCamera = ref(false)
    const testingMicrophone = ref(false)
    const showPreview = ref(false)
    const previewMuted = ref(false)
    const previewVideoOff = ref(false)
    const previewStream = ref(null)
    
    const dialogVisible = computed({
      get: () => props.modelValue,
      set: (value) => emit('update:modelValue', value)
    })
    
    // 表单数据
    const codeForm = reactive({
      meetingCode: '',
      password: ''
    })
    
    const linkForm = reactive({
      meetingLink: ''
    })
    
    const joinSettings = reactive({
      muteOnJoin: false,
      videoOffOnJoin: false
    })
    
    // 表单验证规则
    const codeRules = {
      meetingCode: [
        { required: true, message: '请输入会议号', trigger: 'blur' },
        { min: 6, max: 20, message: '会议号长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    }
    
    const linkRules = {
      meetingLink: [
        { required: true, message: '请输入会议链接', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (value && !value.includes('meeting')) {
              callback(new Error('请输入有效的会议链接'))
            } else {
              callback()
            }
          }, 
          trigger: 'blur' 
        }
      ]
    }
    
    // 测试摄像头
    const testCamera = async () => {
      testingCamera.value = true
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ 
          video: true, 
          audio: false 
        })
        
        cameraStatus.value = true
        showPreview.value = true
        previewStream.value = stream
        
        await nextTick()
        if (previewVideo.value) {
          previewVideo.value.srcObject = stream
        }
        
        ElMessage.success('摄像头检测成功')
      } catch (error) {
        console.error('摄像头检测失败:', error)
        cameraStatus.value = false
        ElMessage.error('摄像头检测失败，请检查设备权限')
      } finally {
        testingCamera.value = false
      }
    }
    
    // 测试麦克风
    const testMicrophone = async () => {
      testingMicrophone.value = true
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ 
          video: false, 
          audio: true 
        })
        
        microphoneStatus.value = true
        
        // 停止音频流（仅用于检测）
        stream.getTracks().forEach(track => track.stop())
        
        ElMessage.success('麦克风检测成功')
      } catch (error) {
        console.error('麦克风检测失败:', error)
        microphoneStatus.value = false
        ElMessage.error('麦克风检测失败，请检查设备权限')
      } finally {
        testingMicrophone.value = false
      }
    }
    
    // 切换预览静音
    const togglePreviewMute = () => {
      previewMuted.value = !previewMuted.value
      if (previewStream.value) {
        const audioTrack = previewStream.value.getAudioTracks()[0]
        if (audioTrack) {
          audioTrack.enabled = !previewMuted.value
        }
      }
    }
    
    // 切换预览视频
    const togglePreviewVideo = () => {
      previewVideoOff.value = !previewVideoOff.value
      if (previewStream.value) {
        const videoTrack = previewStream.value.getVideoTracks()[0]
        if (videoTrack) {
          videoTrack.enabled = !previewVideoOff.value
        }
      }
    }
    
    // 关闭预览
    const closePreview = () => {
      showPreview.value = false
      if (previewStream.value) {
        previewStream.value.getTracks().forEach(track => track.stop())
        previewStream.value = null
      }
    }
    
    // 解析会议链接
    const parseMeetingLink = (link) => {
      try {
        const url = new URL(link)
        const pathParts = url.pathname.split('/')
        const meetingId = pathParts[pathParts.length - 1]
        const password = url.searchParams.get('password') || ''
        
        return { meetingId, password }
      } catch (error) {
        throw new Error('无效的会议链接')
      }
    }
    
    // 处理加入会议
    const handleJoin = async () => {
      try {
        let formRef, meetingCode, password
        
        if (activeTab.value === 'code') {
          formRef = codeFormRef.value
          await formRef.validate()
          meetingCode = codeForm.meetingCode
          password = codeForm.password
        } else {
          formRef = linkFormRef.value
          await formRef.validate()
          
          const parsed = parseMeetingLink(linkForm.meetingLink)
          meetingCode = parsed.meetingId
          password = parsed.password
        }
        
        joining.value = true
        
        // 调用加入会议API
        const response = await meetingApi.joinMeetingByCode(meetingCode, password)
        
        if (response.data) {
          ElMessage.success('加入会议成功')
          emit('joined', response.data.meetingId || response.data.id)
          
          dialogVisible.value = false
          resetForm()
        }
      } catch (error) {
        console.error('加入会议失败:', error)
        if (error.message === '无效的会议链接') {
          ElMessage.error(error.message)
        } else {
          ElMessage.error('加入会议失败，请检查会议号或密码')
        }
      } finally {
        joining.value = false
      }
    }
    
    // 处理标签页切换
    const handleTabChange = (tabName) => {
      // 清除表单验证状态
      if (tabName === 'code') {
        linkFormRef.value?.clearValidate()
      } else {
        codeFormRef.value?.clearValidate()
      }
    }
    
    // 重置表单
    const resetForm = () => {
      Object.assign(codeForm, {
        meetingCode: '',
        password: ''
      })
      
      Object.assign(linkForm, {
        meetingLink: ''
      })
      
      Object.assign(joinSettings, {
        muteOnJoin: false,
        videoOffOnJoin: false
      })
      
      // 重置设备状态
      cameraStatus.value = false
      microphoneStatus.value = false
      closePreview()
      
      // 清除表单验证
      codeFormRef.value?.resetFields()
      linkFormRef.value?.resetFields()
    }
    
    // 处理关闭
    const handleClose = () => {
      dialogVisible.value = false
      resetForm()
    }
    
    return {
      codeFormRef,
      linkFormRef,
      previewVideo,
      activeTab,
      joining,
      dialogVisible,
      codeForm,
      linkForm,
      joinSettings,
      codeRules,
      linkRules,
      cameraStatus,
      microphoneStatus,
      testingCamera,
      testingMicrophone,
      showPreview,
      previewMuted,
      previewVideoOff,
      testCamera,
      testMicrophone,
      togglePreviewMute,
      togglePreviewVideo,
      closePreview,
      handleJoin,
      handleTabChange,
      handleClose
    }
  }
}
</script>

<style scoped>
.join-meeting-content {
  padding: 8px 0;
}

.join-methods {
  margin-bottom: 24px;
}

:deep(.el-tabs__header) {
  margin-bottom: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

.device-check {
  margin-bottom: 24px;
}

.device-check h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.device-status {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  flex: 1;
}

.device-item .el-icon {
  font-size: 16px;
}

.device-item .el-icon.success {
  color: #67c23a;
}

.device-item .el-icon.error {
  color: #f56c6c;
}

.device-item span {
  flex: 1;
  font-size: 13px;
  color: #606266;
}

.preview-container {
  position: relative;
  width: 200px;
  height: 150px;
  margin: 0 auto;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-controls {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.preview-controls .el-button {
  background: rgba(0, 0, 0, 0.6);
  border: none;
  color: white;
}

.preview-controls .el-button:hover {
  background: rgba(0, 0, 0, 0.8);
}

.join-settings h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.settings-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-input-group__prepend) {
  background: #f5f7fa;
  border-color: #dcdfe6;
}

:deep(.el-textarea__inner) {
  resize: none;
}
</style>