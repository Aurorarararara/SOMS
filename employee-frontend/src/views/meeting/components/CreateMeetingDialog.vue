<template>
  <el-dialog
    v-model="dialogVisible"
    title="创建会议"
    width="600px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      @submit.prevent
    >
      <el-form-item label="会议主题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入会议主题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="会议描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入会议描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="form.startTime"
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
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="会议类型">
        <el-radio-group v-model="form.type">
          <el-radio label="instant">立即开始</el-radio>
          <el-radio label="scheduled">预约会议</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="会议设置">
        <el-checkbox-group v-model="form.settings">
          <el-checkbox label="requirePassword">需要密码</el-checkbox>
          <el-checkbox label="allowRecording">允许录制</el-checkbox>
          <el-checkbox label="muteOnJoin">加入时静音</el-checkbox>
          <el-checkbox label="waitingRoom">启用等候室</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      
      <el-form-item 
        v-if="form.settings.includes('requirePassword')"
        label="会议密码"
        prop="password"
      >
        <el-input
          v-model="form.password"
          placeholder="请输入会议密码"
          maxlength="20"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="参与者">
        <div class="participants-section">
          <el-select
            v-model="selectedParticipants"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="搜索并添加参与者"
            :remote-method="searchUsers"
            :loading="searchLoading"
            style="width: 100%; margin-bottom: 12px"
          >
            <el-option
              v-for="user in searchResults"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">
                  {{ user.name.charAt(0) }}
                </el-avatar>
                <span class="user-name">{{ user.name }}</span>
                <span class="user-dept">{{ user.department }}</span>
              </div>
            </el-option>
          </el-select>
          
          <div v-if="selectedParticipants.length > 0" class="selected-participants">
            <h5>已选择的参与者：</h5>
            <div class="participant-tags">
              <el-tag
                v-for="participantId in selectedParticipants"
                :key="participantId"
                closable
                @close="removeParticipant(participantId)"
              >
                {{ getParticipantName(participantId) }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-form-item>
      
      <el-form-item label="重复设置" v-if="form.type === 'scheduled'">
        <el-select v-model="form.recurrence" placeholder="选择重复频率">
          <el-option label="不重复" value="none" />
          <el-option label="每天" value="daily" />
          <el-option label="每周" value="weekly" />
          <el-option label="每月" value="monthly" />
        </el-select>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ form.type === 'instant' ? '立即开始' : '创建会议' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import meetingApi from '@/api/meeting'
import { userApi } from '@/api/user'

export default {
  name: 'CreateMeetingDialog',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'created'],
  setup(props, { emit }) {
    const formRef = ref()
    const submitting = ref(false)
    const searchLoading = ref(false)
    const searchResults = ref([])
    const selectedParticipants = ref([])
    const allUsers = ref([])
    
    const dialogVisible = computed({
      get: () => props.modelValue,
      set: (value) => emit('update:modelValue', value)
    })
    
    const form = reactive({
      title: '',
      description: '',
      startTime: '',
      endTime: '',
      type: 'scheduled',
      settings: [],
      password: '',
      recurrence: 'none'
    })
    
    const rules = {
      title: [
        { required: true, message: '请输入会议主题', trigger: 'blur' },
        { min: 2, max: 100, message: '主题长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' }
      ],
      password: [
        { 
          validator: (rule, value, callback) => {
            if (form.settings.includes('requirePassword') && !value) {
              callback(new Error('请输入会议密码'))
            } else if (value && value.length < 4) {
              callback(new Error('密码长度至少4位'))
            } else {
              callback()
            }
          }, 
          trigger: 'blur' 
        }
      ]
    }
    
    // 禁用过去的日期
    const disabledDate = (time) => {
      return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
    }
    
    // 搜索用户
    const searchUsers = async (query) => {
      if (!query) {
        searchResults.value = []
        return
      }
      
      searchLoading.value = true
      try {
        const response = await userApi.searchUsers({ keyword: query })
        searchResults.value = response.data || []
        // 合并到所有用户列表中
        response.data?.forEach(user => {
          if (!allUsers.value.find(u => u.id === user.id)) {
            allUsers.value.push(user)
          }
        })
      } catch (error) {
        console.error('搜索用户失败:', error)
      } finally {
        searchLoading.value = false
      }
    }
    
    // 获取参与者姓名
    const getParticipantName = (participantId) => {
      const user = allUsers.value.find(u => u.id === participantId)
      return user ? user.name : '未知用户'
    }
    
    // 移除参与者
    const removeParticipant = (participantId) => {
      const index = selectedParticipants.value.indexOf(participantId)
      if (index > -1) {
        selectedParticipants.value.splice(index, 1)
      }
    }
    
    // 重置表单
    const resetForm = () => {
      Object.assign(form, {
        title: '',
        description: '',
        startTime: '',
        endTime: '',
        type: 'scheduled',
        settings: [],
        password: '',
        recurrence: 'none'
      })
      selectedParticipants.value = []
      formRef.value?.resetFields()
    }
    
    // 处理提交
    const handleSubmit = async () => {
      try {
        await formRef.value.validate()
        
        // 验证时间
        if (form.type === 'scheduled') {
          if (new Date(form.endTime) <= new Date(form.startTime)) {
            ElMessage.error('结束时间必须晚于开始时间')
            return
          }
        }
        
        submitting.value = true
        
        const meetingData = {
          ...form,
          participants: selectedParticipants.value,
          startNow: form.type === 'instant'
        }
        
        if (form.type === 'instant') {
          meetingData.startTime = new Date().toISOString()
          meetingData.endTime = new Date(Date.now() + 60 * 60 * 1000).toISOString() // 默认1小时
        }
        
        const response = await meetingApi.createMeeting(meetingData)
        
        ElMessage.success('会议创建成功')
        emit('created', {
          ...response.data,
          startNow: form.type === 'instant'
        })
        
        dialogVisible.value = false
        resetForm()
      } catch (error) {
        console.error('创建会议失败:', error)
        ElMessage.error('创建会议失败')
      } finally {
        submitting.value = false
      }
    }
    
    // 处理关闭
    const handleClose = () => {
      dialogVisible.value = false
      resetForm()
    }
    
    // 监听会议类型变化
    watch(() => form.type, (newType) => {
      if (newType === 'instant') {
        form.startTime = ''
        form.endTime = ''
        form.recurrence = 'none'
      }
    })
    
    // 监听对话框显示状态
    watch(dialogVisible, (visible) => {
      if (visible) {
        // 设置默认时间
        if (form.type === 'scheduled' && !form.startTime) {
          const now = new Date()
          const start = new Date(now.getTime() + 30 * 60 * 1000) // 30分钟后
          const end = new Date(start.getTime() + 60 * 60 * 1000) // 1小时后
          
          form.startTime = start.toISOString().slice(0, 19).replace('T', ' ')
          form.endTime = end.toISOString().slice(0, 19).replace('T', ' ')
        }
      }
    })
    
    return {
      formRef,
      dialogVisible,
      form,
      rules,
      submitting,
      searchLoading,
      searchResults,
      selectedParticipants,
      disabledDate,
      searchUsers,
      getParticipantName,
      removeParticipant,
      handleSubmit,
      handleClose
    }
  }
}
</script>

<style scoped>
.participants-section {
  width: 100%;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
}

.user-dept {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.selected-participants {
  margin-top: 12px;
}

.selected-participants h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.participant-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.el-checkbox) {
  margin-right: 0;
}
</style>