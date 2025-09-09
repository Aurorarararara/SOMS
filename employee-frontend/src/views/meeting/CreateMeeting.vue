<template>
  <div class="create-meeting-page">
    <div class="page-header">
      <h2 class="page-title">创建会议</h2>
      <p class="page-description">设置会议信息并邀请参与者</p>
    </div>
    
    <el-card class="meeting-form-card">
      <el-form
        ref="meetingFormRef"
        :model="meetingForm"
        :rules="formRules"
        label-width="120px"
        size="large"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="会议主题" prop="title">
              <el-input
                v-model="meetingForm.title"
                placeholder="请输入会议主题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="会议类型" prop="type">
              <el-select v-model="meetingForm.type" placeholder="选择会议类型">
                <el-option label="即时会议" value="instant" />
                <el-option label="预约会议" value="scheduled" />
                <el-option label="周期会议" value="recurring" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="会议描述">
          <el-input
            v-model="meetingForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入会议描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-row :gutter="24" v-if="meetingForm.type !== 'instant'">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="meetingForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disabledDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="预计时长" prop="duration">
              <el-select v-model="meetingForm.duration" placeholder="选择时长">
                <el-option label="30分钟" :value="30" />
                <el-option label="1小时" :value="60" />
                <el-option label="1.5小时" :value="90" />
                <el-option label="2小时" :value="120" />
                <el-option label="3小时" :value="180" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item 
          label="自定义时长" 
          v-if="meetingForm.duration === 'custom'"
        >
          <el-input-number
            v-model="meetingForm.customDuration"
            :min="15"
            :max="480"
            :step="15"
            placeholder="分钟"
          />
          <span class="duration-unit">分钟</span>
        </el-form-item>
        
        <!-- 周期设置 -->
        <div v-if="meetingForm.type === 'recurring'" class="recurring-settings">
          <el-divider content-position="left">周期设置</el-divider>
          
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="重复频率" prop="recurringType">
                <el-select v-model="meetingForm.recurringType" placeholder="选择重复频率">
                  <el-option label="每天" value="daily" />
                  <el-option label="每周" value="weekly" />
                  <el-option label="每月" value="monthly" />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="结束日期" prop="recurringEndDate">
                <el-date-picker
                  v-model="meetingForm.recurringEndDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disabledEndDate"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 会议设置 -->
        <el-divider content-position="left">会议设置</el-divider>
        
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="会议密码">
              <el-input
                v-model="meetingForm.password"
                placeholder="设置会议密码（可选）"
                maxlength="20"
                show-password
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="最大参与人数">
              <el-input-number
                v-model="meetingForm.maxParticipants"
                :min="2"
                :max="500"
                placeholder="最大参与人数"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="会议选项">
          <el-checkbox-group v-model="meetingForm.options">
            <el-checkbox label="allowScreenShare">允许屏幕共享</el-checkbox>
            <el-checkbox label="allowRecording">允许录制</el-checkbox>
            <el-checkbox label="autoMuteNewJoiners">新加入者自动静音</el-checkbox>
            <el-checkbox label="waitingRoom">启用等候室</el-checkbox>
            <el-checkbox label="allowChat">允许聊天</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <!-- 邀请参与者 -->
        <el-divider content-position="left">邀请参与者</el-divider>
        
        <el-form-item label="搜索用户">
          <el-input
            v-model="userSearchKeyword"
            placeholder="输入姓名或邮箱搜索用户"
            @input="searchUsers"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <div class="user-search-results" v-if="searchResults.length > 0">
          <div class="search-results-title">搜索结果</div>
          <div class="user-list">
            <div 
              v-for="user in searchResults"
              :key="user.id"
              class="user-item"
              @click="addParticipant(user)"
            >
              <el-avatar :size="32" :src="user.avatar">
                {{ user.name?.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ user.name }}</div>
                <div class="user-department">{{ user.department }}</div>
              </div>
              <el-button 
                type="primary" 
                size="small"
                :disabled="isParticipantAdded(user.id)"
              >
                {{ isParticipantAdded(user.id) ? '已添加' : '添加' }}
              </el-button>
            </div>
          </div>
        </div>
        
        <el-form-item label="已邀请用户" v-if="meetingForm.participants.length > 0">
          <div class="participants-list">
            <el-tag 
              v-for="participant in meetingForm.participants"
              :key="participant.id"
              closable
              @close="removeParticipant(participant.id)"
              class="participant-tag"
            >
              {{ participant.name }}
            </el-tag>
          </div>
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item>
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button 
              type="primary" 
              @click="createMeeting"
              :loading="creating"
            >
              {{ meetingForm.type === 'instant' ? '立即开始' : '创建会议' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import meetingApi from '@/api/meeting'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

export default {
  name: 'CreateMeeting',
  components: {
    Search
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const meetingFormRef = ref()
    const creating = ref(false)
    const userSearchKeyword = ref('')
    const searchResults = ref([])
    
    // 表单数据
    const meetingForm = reactive({
      title: '',
      description: '',
      type: 'instant',
      startTime: '',
      duration: 60,
      customDuration: 60,
      recurringType: 'weekly',
      recurringEndDate: '',
      password: '',
      maxParticipants: 50,
      options: ['allowScreenShare', 'allowChat'],
      participants: []
    })
    
    // 表单验证规则
    const formRules = {
      title: [
        { required: true, message: '请输入会议主题', trigger: 'blur' },
        { min: 2, max: 100, message: '主题长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择会议类型', trigger: 'change' }
      ],
      startTime: [
        { 
          required: true, 
          message: '请选择开始时间', 
          trigger: 'change',
          validator: (rule, value, callback) => {
            if (meetingForm.type !== 'instant' && !value) {
              callback(new Error('请选择开始时间'))
            } else {
              callback()
            }
          }
        }
      ],
      duration: [
        { required: true, message: '请选择会议时长', trigger: 'change' }
      ],
      recurringType: [
        {
          required: true,
          message: '请选择重复频率',
          trigger: 'change',
          validator: (rule, value, callback) => {
            if (meetingForm.type === 'recurring' && !value) {
              callback(new Error('请选择重复频率'))
            } else {
              callback()
            }
          }
        }
      ],
      recurringEndDate: [
        {
          required: true,
          message: '请选择结束日期',
          trigger: 'change',
          validator: (rule, value, callback) => {
            if (meetingForm.type === 'recurring' && !value) {
              callback(new Error('请选择结束日期'))
            } else {
              callback()
            }
          }
        }
      ]
    }
    
    // 计算属性
    const actualDuration = computed(() => {
      return meetingForm.duration === 'custom' ? meetingForm.customDuration : meetingForm.duration
    })
    
    // 禁用过去的日期
    const disabledDate = (time) => {
      return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
    }
    
    // 禁用结束日期（必须在开始日期之后）
    const disabledEndDate = (time) => {
      if (!meetingForm.startTime) return false
      const startDate = new Date(meetingForm.startTime)
      return time.getTime() < startDate.getTime()
    }
    
    // 搜索用户
    const searchUsers = async () => {
      if (!userSearchKeyword.value.trim()) {
        searchResults.value = []
        return
      }
      
      try {
        const response = await meetingApi.searchUsers(userSearchKeyword.value)
        searchResults.value = response.data || []
      } catch (error) {
        console.error('搜索用户失败:', error)
      }
    }
    
    // 检查用户是否已添加
    const isParticipantAdded = (userId) => {
      return meetingForm.participants.some(p => p.id === userId)
    }
    
    // 添加参与者
    const addParticipant = (user) => {
      if (isParticipantAdded(user.id)) {
        ElMessage.warning('该用户已在邀请列表中')
        return
      }
      
      meetingForm.participants.push({
        id: user.id,
        name: user.name,
        email: user.email,
        avatar: user.avatar
      })
      
      ElMessage.success(`已添加 ${user.name}`)
    }
    
    // 移除参与者
    const removeParticipant = (userId) => {
      const index = meetingForm.participants.findIndex(p => p.id === userId)
      if (index > -1) {
        meetingForm.participants.splice(index, 1)
      }
    }
    
    // 创建会议
    const createMeeting = async () => {
      try {
        await meetingFormRef.value.validate()
        
        creating.value = true
        
        const meetingData = {
          title: meetingForm.title,
          description: meetingForm.description,
          type: meetingForm.type,
          password: meetingForm.password,
          maxParticipants: meetingForm.maxParticipants,
          settings: {
            allowScreenShare: meetingForm.options.includes('allowScreenShare'),
            allowRecording: meetingForm.options.includes('allowRecording'),
            autoMuteNewJoiners: meetingForm.options.includes('autoMuteNewJoiners'),
            waitingRoom: meetingForm.options.includes('waitingRoom'),
            allowChat: meetingForm.options.includes('allowChat')
          },
          participants: meetingForm.participants.map(p => p.id)
        }
        
        // 根据会议类型设置时间相关字段
        if (meetingForm.type === 'instant') {
          meetingData.startTime = dayjs().format('YYYY-MM-DD HH:mm:ss')
          meetingData.duration = actualDuration.value
        } else {
          meetingData.startTime = meetingForm.startTime
          meetingData.duration = actualDuration.value
          
          if (meetingForm.type === 'recurring') {
            meetingData.recurringType = meetingForm.recurringType
            meetingData.recurringEndDate = meetingForm.recurringEndDate
          }
        }
        
        const response = await meetingApi.createMeeting(meetingData)
        
        ElMessage.success('会议创建成功')
        
        // 如果是即时会议，直接跳转到会议室
        if (meetingForm.type === 'instant') {
          router.push(`/meeting/room/${response.data.id}`)
        } else {
          router.push('/meeting')
        }
        
      } catch (error) {
        console.error('创建会议失败:', error)
        ElMessage.error(error.message || '创建会议失败')
      } finally {
        creating.value = false
      }
    }
    
    // 返回
    const goBack = () => {
      router.back()
    }
    
    // 组件挂载
    onMounted(() => {
      // 设置默认开始时间为当前时间后1小时
      if (meetingForm.type !== 'instant') {
        meetingForm.startTime = dayjs().add(1, 'hour').format('YYYY-MM-DD HH:mm:ss')
      }
    })
    
    return {
      meetingFormRef,
      creating,
      userSearchKeyword,
      searchResults,
      meetingForm,
      formRules,
      actualDuration,
      disabledDate,
      disabledEndDate,
      searchUsers,
      isParticipantAdded,
      addParticipant,
      removeParticipant,
      createMeeting,
      goBack
    }
  }
}
</script>

<style scoped>
.create-meeting-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
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

.meeting-form-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.duration-unit {
  margin-left: 8px;
  color: #606266;
}

.recurring-settings {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin: 16px 0;
}

.user-search-results {
  margin-top: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.search-results-title {
  padding: 12px 16px;
  background: #f5f7fa;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
}

.user-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-item:last-child {
  border-bottom: none;
}

.user-info {
  flex: 1;
  margin-left: 12px;
}

.user-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.user-department {
  font-size: 12px;
  color: #909399;
}

.participants-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.participant-tag {
  margin: 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .create-meeting-page {
    padding: 16px;
  }
  
  .el-col {
    margin-bottom: 16px;
  }
  
  .form-actions {
    justify-content: center;
  }
}

/* 深色主题适配 */
.dark .create-meeting-page {
  background: #1a1a1a;
}

.dark .page-title {
  color: #e5e5e5;
}

.dark .page-description {
  color: #a0a0a0;
}

.dark .recurring-settings {
  background: #2a2a2a;
}

.dark .user-search-results {
  border-color: #3a3a3a;
}

.dark .search-results-title {
  background: #2a2a2a;
  border-bottom-color: #3a3a3a;
  color: #e5e5e5;
}

.dark .user-item {
  border-bottom-color: #3a3a3a;
}

.dark .user-item:hover {
  background: #2a2a2a;
}

.dark .user-name {
  color: #e5e5e5;
}
</style>