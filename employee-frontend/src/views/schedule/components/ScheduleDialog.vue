<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px"
      @submit.prevent
    >
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入日程标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="时间" prop="timeRange">
        <div class="time-range-container">
          <el-checkbox v-model="formData.allDay" @change="handleAllDayChange">
            全天
          </el-checkbox>
          <div class="time-inputs">
            <el-date-picker
              v-model="formData.startTime"
              :type="formData.allDay ? 'date' : 'datetime'"
              placeholder="开始时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleStartTimeChange"
            />
            <span class="time-separator">至</span>
            <el-date-picker
              v-model="formData.endTime"
              :type="formData.allDay ? 'date' : 'datetime'"
              placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledEndDate"
            />
          </div>
        </div>
      </el-form-item>

      <el-form-item label="地点">
        <el-input
          v-model="formData.location"
          placeholder="请输入地点"
          maxlength="200"
        >
          <template #prefix>
            <el-icon><Location /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入日程描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="分类">
            <el-select
              v-model="formData.categoryId"
              placeholder="选择分类"
              style="width: 100%"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              >
                <div class="category-option">
                  <div
                    class="category-color"
                    :style="{ backgroundColor: category.color }"
                  ></div>
                  <span>{{ category.name }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级">
            <el-select
              v-model="formData.priority"
              placeholder="选择优先级"
              style="width: 100%"
            >
              <el-option label="高" value="HIGH">
                <div class="priority-option high">
                  <el-icon><Warning /></el-icon>
                  高优先级
                </div>
              </el-option>
              <el-option label="中" value="MEDIUM">
                <div class="priority-option medium">
                  <el-icon><InfoFilled /></el-icon>
                  中优先级
                </div>
              </el-option>
              <el-option label="低" value="LOW">
                <div class="priority-option low">
                  <el-icon><Minus /></el-icon>
                  低优先级
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="颜色">
            <el-color-picker
              v-model="formData.color"
              :predefine="predefineColors"
              show-alpha
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="提醒">
            <el-select
              v-model="formData.reminderMinutes"
              placeholder="选择提醒时间"
              style="width: 100%"
            >
              <el-option label="不提醒" :value="0" />
              <el-option label="准时提醒" :value="0" />
              <el-option label="提前5分钟" :value="5" />
              <el-option label="提前15分钟" :value="15" />
              <el-option label="提前30分钟" :value="30" />
              <el-option label="提前1小时" :value="60" />
              <el-option label="提前2小时" :value="120" />
              <el-option label="提前1天" :value="1440" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="参与者">
        <el-select
          v-model="formData.participantIds"
          multiple
          filterable
          remote
          reserve-keyword
          placeholder="搜索并选择参与者"
          :remote-method="searchUsers"
          :loading="userSearchLoading"
          style="width: 100%"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.id"
            :label="user.real_name || user.username"
            :value="user.id"
          >
            <div class="user-option">
              <el-avatar :size="24" :src="user.avatar">
                {{ (user.real_name || user.username).charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ user.real_name || user.username }}</span>
              <span class="user-dept">{{ user.department_name }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="formData.isPrivate">
          私人日程（仅自己可见）
        </el-checkbox>
      </el-form-item>

      <!-- 冲突检查 -->
      <div v-if="conflicts.length > 0" class="conflict-warning">
        <el-alert
          title="时间冲突提醒"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>检测到以下时间冲突：</p>
            <ul>
              <li v-for="conflict in conflicts" :key="conflict.id">
                {{ conflict.title }} ({{ formatTime(conflict.start_time) }} - {{ formatTime(conflict.end_time) }})
              </li>
            </ul>
          </template>
        </el-alert>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          {{ mode === 'create' ? '创建' : '更新' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Location, 
  Warning, 
  InfoFilled, 
  Minus 
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { useScheduleStore } from '@/stores/schedule'
import { userApi } from '@/api/user'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  schedule: {
    type: Object,
    default: null
  },
  mode: {
    type: String,
    default: 'create' // create, edit
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

// Store
const scheduleStore = useScheduleStore()

// 响应式数据
const formRef = ref(null)
const submitLoading = ref(false)
const userSearchLoading = ref(false)
const userOptions = ref([])
const conflicts = ref([])

// 表单数据
const formData = ref({
  title: '',
  startTime: '',
  endTime: '',
  allDay: false,
  location: '',
  description: '',
  categoryId: null,
  priority: 'MEDIUM',
  color: '#409eff',
  reminderMinutes: 15,
  participantIds: [],
  isPrivate: false
})

// 预定义颜色
const predefineColors = [
  '#409eff',
  '#67c23a',
  '#e6a23c',
  '#f56c6c',
  '#909399',
  '#c71585',
  '#ff69b4',
  '#ba55d3',
  '#9370db',
  '#3cb371',
  '#32cd32',
  '#00fa9a',
  '#48d1cc',
  '#00bfff',
  '#1e90ff',
  '#6495ed',
  '#ffd700',
  '#ffa500',
  '#ff6347',
  '#dc143c'
]

// 分类选项
const categories = ref([
  { id: 1, name: '工作', color: '#409eff' },
  { id: 2, name: '会议', color: '#67c23a' },
  { id: 3, name: '学习', color: '#e6a23c' },
  { id: 4, name: '生活', color: '#f56c6c' },
  { id: 5, name: '其他', color: '#909399' }
])

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入日程标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  timeRange: [
    { validator: validateTimeRange, trigger: 'change' }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  return props.mode === 'create' ? '新建日程' : '编辑日程'
})

// 方法
const validateTimeRange = (rule, value, callback) => {
  if (!formData.value.startTime || !formData.value.endTime) {
    callback(new Error('请选择开始和结束时间'))
    return
  }
  
  const start = dayjs(formData.value.startTime)
  const end = dayjs(formData.value.endTime)
  
  if (end.isBefore(start)) {
    callback(new Error('结束时间不能早于开始时间'))
    return
  }
  
  if (end.diff(start, 'minute') < 15) {
    callback(new Error('日程时长不能少于15分钟'))
    return
  }
  
  callback()
}

const disabledEndDate = (date) => {
  if (!formData.value.startTime) return false
  const startDate = dayjs(formData.value.startTime)
  return dayjs(date).isBefore(startDate, 'day')
}

const handleAllDayChange = (allDay) => {
  if (allDay) {
    // 全天事件，设置为当天的开始和结束
    if (formData.value.startTime) {
      const date = dayjs(formData.value.startTime).format('YYYY-MM-DD')
      formData.value.startTime = date + ' 00:00:00'
      formData.value.endTime = date + ' 23:59:59'
    }
  } else {
    // 非全天事件，设置默认时间
    if (formData.value.startTime) {
      const date = dayjs(formData.value.startTime).format('YYYY-MM-DD')
      formData.value.startTime = date + ' 09:00:00'
      formData.value.endTime = date + ' 10:00:00'
    }
  }
}

const handleStartTimeChange = (value) => {
  if (value && !formData.value.endTime) {
    // 自动设置结束时间为开始时间后1小时
    const start = dayjs(value)
    formData.value.endTime = start.add(1, 'hour').format('YYYY-MM-DD HH:mm:ss')
  }
  
  // 检查冲突
  checkConflicts()
}

const checkConflicts = async () => {
  if (!formData.value.startTime || !formData.value.endTime) {
    conflicts.value = []
    return
  }
  
  try {
    const excludeId = props.mode === 'edit' && props.schedule ? props.schedule.id : null
    const result = await scheduleStore.checkScheduleConflict(
      formData.value.startTime,
      formData.value.endTime,
      excludeId
    )
    conflicts.value = result || []
  } catch (error) {
    console.error('检查冲突失败:', error)
  }
}

const searchUsers = async (query) => {
  if (!query) {
    userOptions.value = []
    return
  }
  
  try {
    userSearchLoading.value = true
    const response = await userApi.searchUsers({ keyword: query, limit: 20 })
    if (response.code === 200) {
      userOptions.value = response.data || []
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

const formatTime = (dateTime) => {
  return dayjs(dateTime).format('MM-DD HH:mm')
}

const initFormData = () => {
  if (props.schedule) {
    // 编辑模式，填充现有数据
    formData.value = {
      title: props.schedule.title || '',
      startTime: props.schedule.start_time || '',
      endTime: props.schedule.end_time || '',
      allDay: props.schedule.all_day || false,
      location: props.schedule.location || '',
      description: props.schedule.description || '',
      categoryId: props.schedule.category_id || null,
      priority: props.schedule.priority || 'MEDIUM',
      color: props.schedule.color || '#409eff',
      reminderMinutes: props.schedule.reminder_minutes || 15,
      participantIds: props.schedule.participant_ids || [],
      isPrivate: props.schedule.is_private || false
    }
  } else {
    // 创建模式，使用默认值
    const now = dayjs()
    formData.value = {
      title: '',
      startTime: now.format('YYYY-MM-DD HH:mm:ss'),
      endTime: now.add(1, 'hour').format('YYYY-MM-DD HH:mm:ss'),
      allDay: false,
      location: '',
      description: '',
      categoryId: null,
      priority: 'MEDIUM',
      color: '#409eff',
      reminderMinutes: 15,
      participantIds: [],
      isPrivate: false
    }
  }
}

const handleSubmit = async () => {
  try {
    // 表单验证
    const valid = await formRef.value.validate()
    if (!valid) return
    
    submitLoading.value = true
    
    // 构建提交数据
    const submitData = {
      ...formData.value,
      start_time: formData.value.startTime,
      end_time: formData.value.endTime,
      all_day: formData.value.allDay,
      category_id: formData.value.categoryId,
      reminder_minutes: formData.value.reminderMinutes,
      participant_ids: formData.value.participantIds,
      is_private: formData.value.isPrivate
    }
    
    // 删除不需要的字段
    delete submitData.startTime
    delete submitData.endTime
    delete submitData.allDay
    delete submitData.categoryId
    delete submitData.reminderMinutes
    delete submitData.participantIds
    delete submitData.isPrivate
    
    emit('confirm', submitData)
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败，请检查表单数据')
  } finally {
    submitLoading.value = false
  }
}

const handleClose = () => {
  emit('cancel')
}

// 监听器
watch(() => props.modelValue, (visible) => {
  if (visible) {
    initFormData()
    nextTick(() => {
      formRef.value?.clearValidate()
    })
  } else {
    conflicts.value = []
    userOptions.value = []
  }
})

watch(() => [formData.value.startTime, formData.value.endTime], () => {
  checkConflicts()
}, { deep: true })
</script>

<style scoped>
.time-range-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-separator {
  color: #909399;
  font-size: 14px;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.priority-option {
  display: flex;
  align-items: center;
  gap: 6px;
}

.priority-option.high {
  color: #f56c6c;
}

.priority-option.medium {
  color: #e6a23c;
}

.priority-option.low {
  color: #909399;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.user-dept {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.conflict-warning {
  margin-top: 16px;
}

.conflict-warning ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.conflict-warning li {
  margin-bottom: 4px;
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-color-picker) {
  width: 100%;
}

:deep(.el-select .el-input) {
  width: 100%;
}
</style>