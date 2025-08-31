<template>
  <el-dialog
    v-model="visible"
    title="分配任务"
    width="500px"
    @close="handleClose"
  >
    <div v-if="task" class="assign-dialog">
      <div class="task-info">
        <h4>任务信息</h4>
        <div class="info-item">
          <label>任务标题：</label>
          <span>{{ task.title }}</span>
        </div>
        <div class="info-item">
          <label>当前负责人：</label>
          <span>{{ task.assigneeName || '未分配' }}</span>
        </div>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="margin-top: 20px;"
      >
        <el-form-item label="新负责人" prop="assigneeId">
          <el-select 
            v-model="form.assigneeId" 
            placeholder="请选择新的负责人" 
            style="width: 100%;"
            filterable
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="`${user.name} (${user.departmentName || '未知部门'})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="分配说明">
          <el-input
            v-model="form.assignNote"
            type="textarea"
            :rows="3"
            placeholder="请输入分配说明（可选）"
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="form.notifyAssignee">
            通知新负责人
          </el-checkbox>
        </el-form-item>
      </el-form>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          确认分配
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

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
  users: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'assign'])

// 响应式数据
const visible = ref(false)
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  assigneeId: null,
  assignNote: '',
  notifyAssignee: true
})

const rules = {
  assigneeId: [
    { required: true, message: '请选择新的负责人', trigger: 'change' }
  ]
}

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 方法
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    assigneeId: null,
    assignNote: '',
    notifyAssignee: true
  })
}

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (form.assigneeId === props.task.assigneeId) {
      ElMessage.warning('新负责人与当前负责人相同')
      return
    }
    
    loading.value = true
    
    const assignData = {
      taskId: props.task.id,
      assigneeId: form.assigneeId,
      assignNote: form.assignNote,
      notifyAssignee: form.notifyAssignee
    }
    
    emit('assign', assignData)
    
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.assign-dialog {
  padding: 10px 0;
}

.task-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.task-info h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
  margin-right: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
