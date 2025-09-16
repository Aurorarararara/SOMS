<template>
  <el-dialog
    title="节点配置"
    :model-value="visible"
    @update:model-value="handleClose"
    width="600px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="节点名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入节点名称" />
      </el-form-item>
      
      <el-form-item label="节点类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择节点类型" style="width: 100%">
          <el-option label="开始节点" value="start" />
          <el-option label="审批节点" value="approval" />
          <el-option label="抄送节点" value="cc" />
          <el-option label="结束节点" value="end" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="处理人" prop="assignee" v-if="form.type === 'approval'">
        <el-select v-model="form.assignee" placeholder="请选择处理人" style="width: 100%">
          <el-option label="发起人" value="starter" />
          <el-option label="部门主管" value="manager" />
          <el-option label="指定人员" value="specific" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入节点描述"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  nodeData: {
    type: Object,
    default: null
  },
  nodeIndex: {
    type: Number,
    default: -1
  }
})

// Emits
const emit = defineEmits(['close', 'save'])

// 响应式数据
const formRef = ref(null)
const saving = ref(false)

const form = reactive({
  name: '',
  type: '',
  assignee: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入节点名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择节点类型', trigger: 'change' }
  ]
}

// 监听节点数据变化
watch(() => props.nodeData, (newData) => {
  if (newData) {
    Object.assign(form, {
      name: newData.name || '',
      type: newData.type || '',
      assignee: newData.assignee || '',
      description: newData.description || ''
    })
  } else {
    // 重置表单
    Object.assign(form, {
      name: '',
      type: '',
      assignee: '',
      description: ''
    })
  }
}, { immediate: true })

// 关闭对话框
const handleClose = () => {
  emit('close')
}

// 保存节点配置
const handleSave = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    saving.value = true
    
    // 构建节点数据
    const nodeData = {
      ...form,
      index: props.nodeIndex
    }
    
    emit('save', nodeData)
    ElMessage.success('节点配置保存成功')
    
  } catch (error) {
    console.error('保存节点配置失败:', error)
    ElMessage.error('保存节点配置失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.el-form {
  padding: 20px 0;
}
</style>