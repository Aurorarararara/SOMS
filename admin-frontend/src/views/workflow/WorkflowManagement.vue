<template>
  <div class="workflow-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h1>审批流程管理</h1>
        <p>自定义审批流程，灵活配置审批顺序和规则</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="createTemplate">
          新建流程模板
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 流程设计器模式 -->
      <div v-if="showDesigner" class="designer-container">
        <WorkflowDesigner
          :template-data="currentTemplate"
          @save="handleDesignerSave"
          @cancel="handleDesignerCancel"
        />
      </div>
      
      <!-- 流程列表模式 -->
      <div v-else class="list-container">
        <WorkflowTemplateList
          @create="handleCreateTemplate"
          @edit="handleEditTemplate"
          @design="handleDesignTemplate"
          @delete="handleDeleteTemplate"
        />
      </div>
    </div>

    <!-- 流程模板配置对话框 -->
    <WorkflowTemplateDialog
      v-model="showTemplateDialog"
      :template-data="currentTemplate"
      :is-edit="isEditMode"
      @confirm="handleTemplateConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import WorkflowTemplateList from './WorkflowTemplateList.vue'
import WorkflowTemplateDialog from './components/WorkflowTemplateDialog.vue'
import WorkflowDesigner from '@/components/workflow/WorkflowDesigner.vue'
import { createWorkflowTemplate, updateWorkflowTemplate, deleteWorkflowTemplate } from '@/api/workflow'

// 响应式数据
const showDesigner = ref(false)
const showTemplateDialog = ref(false)
const isEditMode = ref(false)
const currentTemplate = ref({})

// 页面状态管理
const pageState = reactive({
  loading: false,
  templates: []
})

// 新建流程模板
const createTemplate = () => {
  currentTemplate.value = {
    name: '',
    businessType: '',
    description: '',
    isActive: true,
    isDefault: false
  }
  isEditMode.value = false
  showTemplateDialog.value = true
}

// 处理创建模板
const handleCreateTemplate = () => {
  createTemplate()
}

// 处理编辑模板
const handleEditTemplate = (template) => {
  currentTemplate.value = { ...template }
  isEditMode.value = true
  showTemplateDialog.value = true
}

// 处理设计流程
const handleDesignTemplate = (template) => {
  currentTemplate.value = { ...template }
  showDesigner.value = true
}

// 处理删除模板
const handleDeleteTemplate = async (templateId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个流程模板吗？删除后将无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteWorkflowTemplate(templateId)
    ElMessage.success('流程模板删除成功')
    
    // 刷新列表
    await refreshTemplateList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败:', error)
      ElMessage.error('删除模板失败')
    }
  }
}

// 处理模板配置确认
const handleTemplateConfirm = async (templateData) => {
  try {
    let response
    if (isEditMode.value) {
      response = await updateWorkflowTemplate(currentTemplate.value.id, templateData)
      ElMessage.success('流程模板更新成功')
    } else {
      response = await createWorkflowTemplate(templateData)
      ElMessage.success('流程模板创建成功')
    }
    
    // 关闭对话框
    showTemplateDialog.value = false
    
    // 如果是新建的模板，直接进入设计器
    if (!isEditMode.value && response?.data) {
      currentTemplate.value = response.data
      showDesigner.value = true
    } else {
      // 刷新列表
      await refreshTemplateList()
    }
  } catch (error) {
    console.error('保存模板失败:', error)
    ElMessage.error('保存模板失败')
  }
}

// 处理设计器保存
const handleDesignerSave = async (flowData) => {
  try {
    await updateWorkflowTemplate(flowData.id, flowData)
    ElMessage.success('流程设计保存成功')
    showDesigner.value = false
    await refreshTemplateList()
  } catch (error) {
    console.error('保存流程设计失败:', error)
    ElMessage.error('保存流程设计失败')
  }
}

// 处理设计器取消
const handleDesignerCancel = () => {
  showDesigner.value = false
}

// 刷新模板列表
const refreshTemplateList = async () => {
  // 触发列表组件刷新
  // 这里可以通过事件或者其他方式通知列表组件刷新
  // 或者在列表组件中监听路由变化等方式自动刷新
}

// 设置页面标题
document.title = '审批流程管理 - 办公管理系统'
</script>

<style scoped>
.workflow-management {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
}

.header-title h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.header-title p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.main-content {
  flex: 1;
  overflow: hidden;
}

.designer-container {
  height: 100%;
}

.list-container {
  height: 100%;
  padding: 24px;
  overflow: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .header-actions .el-button {
    width: 100%;
  }
  
  .list-container {
    padding: 16px;
  }
}
</style>