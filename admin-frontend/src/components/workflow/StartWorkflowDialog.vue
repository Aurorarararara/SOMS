<template>
  <el-dialog
    v-model="visible"
    title="发起新流程"
    width="600px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="流程模板" prop="templateId">
        <el-select
          v-model="formData.templateId"
          placeholder="请选择流程模板"
          style="width: 100%"
          @change="handleTemplateChange"
        >
          <el-option
            v-for="template in availableTemplates"
            :key="template.id"
            :label="template.name"
            :value="template.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="业务类型" prop="businessType">
        <el-select
          v-model="formData.businessType"
          placeholder="请选择业务类型"
          style="width: 100%"
          @change="handleBusinessTypeChange"
        >
          <el-option label="请假申请" value="LEAVE" />
          <el-option label="费用报销" value="EXPENSE" />
          <el-option label="采购申请" value="PURCHASE" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>

      <el-form-item label="流程标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入流程标题"
        />
      </el-form-item>

      <el-form-item label="申请内容" prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="4"
          placeholder="请输入申请内容"
        />
      </el-form-item>

      <el-form-item label="紧急程度">
        <el-radio-group v-model="formData.urgency">
          <el-radio label="LOW">低</el-radio>
          <el-radio label="MEDIUM">中</el-radio>
          <el-radio label="HIGH">高</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          发起流程
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableTemplates, startWorkflow } from '@/api/workflow'

// 定义属性
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const visible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const availableTemplates = ref([])

const formData = reactive({
  templateId: '',
  businessType: '',
  title: '',
  content: '',
  urgency: 'MEDIUM'
})

const formRules = {
  templateId: [
    { required: true, message: '请选择流程模板', trigger: 'change' }
  ],
  businessType: [
    { required: true, message: '请选择业务类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入流程标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入申请内容', trigger: 'blur' }
  ]
}

// 监听modelValue变化
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      loadAvailableTemplates()
    }
  }
)

// 监听visible变化
watch(
  () => visible.value,
  (val) => {
    emit('update:modelValue', val)
    if (!val) {
      resetForm()
    }
  }
)

// 生命周期
onMounted(() => {
  visible.value = props.modelValue
})

// 方法
const loadAvailableTemplates = async () => {
  try {
    const response = await getAvailableTemplates()
    if (response.success) {
      availableTemplates.value = response.data
    }
  } catch (error) {
    console.error('获取可用模板失败:', error)
    ElMessage.error('获取可用模板失败')
  }
}

const handleTemplateChange = (templateId) => {
  const template = availableTemplates.value.find(t => t.id === templateId)
  if (template) {
    formData.title = `${template.name}-申请`
  }
}

const handleBusinessTypeChange = (businessType) => {
  const typeLabels = {
    LEAVE: '请假申请',
    EXPENSE: '费用报销',
    PURCHASE: '采购申请',
    OTHER: '其他申请'
  }
  if (!formData.title) {
    formData.title = `${typeLabels[businessType] || '流程'}申请`
  }
}

const resetForm = () => {
  formData.templateId = ''
  formData.businessType = ''
  formData.title = ''
  formData.content = ''
  formData.urgency = 'MEDIUM'
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleClose = () => {
  visible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const data = {
        templateId: formData.templateId,
        businessType: formData.businessType,
        title: formData.title,
        content: formData.content,
        urgency: formData.urgency
      }
      
      const response = await startWorkflow(data)
      if (response.success) {
        ElMessage.success('流程发起成功')
        visible.value = false
        emit('success')
      } else {
        ElMessage.error(response.message || '流程发起失败')
      }
    } catch (error) {
      console.error('发起流程失败:', error)
      ElMessage.error('发起流程失败')
    } finally {
      submitLoading.value = false
    }
  })
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>