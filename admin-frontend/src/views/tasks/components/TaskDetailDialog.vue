<template>
  <el-dialog
    v-model="visible"
    title="任务详情"
    width="700px"
    @close="handleClose"
  >
    <div v-if="task" class="task-detail">
      <div class="detail-section">
        <h3>基本信息</h3>
        <div class="detail-grid">
          <div class="detail-item">
            <label>任务标题：</label>
            <span>{{ task.title }}</span>
          </div>
          <div class="detail-item">
            <label>任务状态：</label>
            <el-tag :type="getStatusType(task.status)">
              {{ getStatusText(task.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>优先级：</label>
            <el-tag :type="getPriorityType(task.priority)">
              {{ getPriorityText(task.priority) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>创建人：</label>
            <span>{{ task.creatorName || '未知' }}</span>
          </div>
          <div class="detail-item">
            <label>负责人：</label>
            <span>{{ task.assigneeName || '未分配' }}</span>
          </div>
          <div class="detail-item">
            <label>创建时间：</label>
            <span>{{ formatDate(task.createTime) }}</span>
          </div>
          <div class="detail-item">
            <label>截止时间：</label>
            <span>{{ task.dueDate ? formatDate(task.dueDate) : '无' }}</span>
          </div>
          <div class="detail-item">
            <label>完成时间：</label>
            <span>{{ task.completedTime ? formatDate(task.completedTime) : '未完成' }}</span>
          </div>
        </div>
      </div>
      
      <div class="detail-section">
        <h3>任务描述</h3>
        <div class="description">
          {{ task.description || '无描述' }}
        </div>
      </div>
      
      <div class="detail-section">
        <h3>工作量信息</h3>
        <div class="detail-grid">
          <div class="detail-item">
            <label>预计工时：</label>
            <span>{{ task.estimatedHours || 0 }} 小时</span>
          </div>
          <div class="detail-item">
            <label>实际工时：</label>
            <span>{{ task.actualHours || 0 }} 小时</span>
          </div>
          <div class="detail-item">
            <label>完成进度：</label>
            <el-progress :percentage="task.progress || 0" />
          </div>
        </div>
      </div>
      
      <div class="detail-section" v-if="task.tags && task.tags.length">
        <h3>标签</h3>
        <div class="tags">
          <el-tag v-for="tag in task.tags" :key="tag" size="small">
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleEdit" v-if="canEdit">
          编辑任务
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  task: {
    type: Object,
    default: () => ({})
  },
  canEdit: {
    type: Boolean,
    default: true
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'edit'])

// 响应式数据
const visible = ref(false)

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 方法
const handleClose = () => {
  visible.value = false
}

const handleEdit = () => {
  emit('edit', props.task)
  visible.value = false
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const types = {
    'pending': '',
    'processing': 'warning',
    'completed': 'success',
    'overdue': 'danger'
  }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    'pending': '待处理',
    'processing': '进行中',
    'completed': '已完成',
    'overdue': '已逾期'
  }
  return texts[status] || status
}

const getPriorityType = (priority) => {
  const types = {
    'low': 'info',
    'medium': '',
    'high': 'warning',
    'urgent': 'danger'
  }
  return types[priority] || ''
}

const getPriorityText = (priority) => {
  const texts = {
    'low': '低',
    'medium': '中',
    'high': '高',
    'urgent': '紧急'
  }
  return texts[priority] || priority
}
</script>

<style scoped>
.task-detail {
  max-height: 500px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
  margin-right: 10px;
}

.description {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
