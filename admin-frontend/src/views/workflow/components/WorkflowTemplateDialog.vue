<template>
  <el-dialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleClose"
    width="90%"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    destroy-on-close
  >
    <div class="workflow-template-dialog">
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        :disabled="mode === 'view'"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入流程模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务类型" prop="businessType">
              <el-select v-model="form.businessType" placeholder="请选择业务类型" style="width: 100%">
                <el-option label="请假申请" value="leave_application" />
                <el-option label="费用报销" value="expense_claim" />
                <el-option label="采购申请" value="purchase_request" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option label="人事" value="hr" />
                <el-option label="财务" value="finance" />
                <el-option label="采购" value="purchase" />
                <el-option label="通用" value="general" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="启用" value="active" />
                <el-option label="草稿" value="draft" />
                <el-option label="归档" value="archived" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="模板描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入流程模板描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="是否启用">
              <el-switch v-model="form.isActive" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否默认">
              <el-switch v-model="form.isDefault" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 审批节点配置 -->
      <div class="nodes-section">
        <div class="section-header">
          <h3>审批节点配置</h3>
          <el-button v-if="mode !== 'view'" type="primary" size="small" @click="addNode">
            <el-icon><Plus /></el-icon>
            添加节点
          </el-button>
        </div>

        <div class="nodes-container">
          <div v-if="form.nodes.length === 0" class="empty-nodes">
            <el-empty description="暂无审批节点" />
          </div>
          
          <div v-else class="nodes-list">
            <div 
              v-for="(node, index) in form.nodes" 
              :key="node.tempId || node.id"
              class="node-item"
            >
              <div class="node-header">
                <div class="node-info">
                  <span class="node-order">{{ index + 1 }}</span>
                  <span class="node-name">{{ node.nodeName || '未命名节点' }}</span>
                  <el-tag v-if="node.isRequired" type="danger" size="small">必经</el-tag>
                </div>
                <div v-if="mode !== 'view'" class="node-actions">
                  <el-button link type="primary" @click="editNode(index)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button link type="danger" @click="removeNode(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <div class="node-details">
                <div class="detail-item">
                  <span class="label">审批人类型：</span>
                  <span class="value">{{ getApproverTypeName(node.approverType) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">审批模式：</span>
                  <span class="value">{{ getApprovalModeName(node.approvalMode) }}</span>
                </div>
                <div v-if="node.timeoutHours" class="detail-item">
                  <span class="label">超时时间：</span>
                  <span class="value">{{ node.timeoutHours }} 小时</span>
                </div>
                <div v-if="node.description" class="detail-item">
                  <span class="label">节点描述：</span>
                  <span class="value">{{ node.description }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button v-if="mode !== 'view'" type="primary" @click="handleSave" :loading="saving">
        保存
      </el-button>
    </template>

    <!-- 节点配置对话框 -->
    <NodeConfigDialog
      v-if="nodeDialogVisible"
      :visible="nodeDialogVisible"
      :node-data="currentNode"
      :node-index="currentNodeIndex"
      @close="handleNodeDialogClose"
      @save="handleNodeSave"
    />
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { workflowTemplateApi } from '@/api/workflow'
import NodeConfigDialog from './NodeConfigDialog.vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  templateData: {
    type: Object,
    default: null
  },
  mode: {
    type: String,
    default: 'create' // create, edit, view
  }
})

// Emits
const emit = defineEmits(['close', 'success'])

// 响应式数据
const formRef = ref(null)
const saving = ref(false)
const nodeDialogVisible = ref(false)
const currentNode = ref(null)
const currentNodeIndex = ref(-1)

const form = reactive({
  id: null,
  name: '',
  description: '',
  businessType: '',
  category: 'general',
  status: 'active',
  isActive: true,
  isDefault: false,
  sortOrder: 0,
  nodes: [],
  conditions: []
})

const rules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  businessType: [
    { required: true, message: '请选择业务类型', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  const titles = {
    create: '新建流程模板',
    edit: '编辑流程模板',
    view: '查看流程模板'
  }
  return titles[props.mode] || '流程模板'
})

// 监听器
watch(() => props.visible, (visible) => {
  if (visible) {
    initForm()
  }
})

watch(() => props.templateData, (data) => {
  if (data) {
    initForm()
  }
})

// 方法
const initForm = async () => {
  if (props.templateData) {
    // 编辑或查看模式
    if (props.mode === 'edit' || props.mode === 'view') {
      try {
        const { data } = await workflowTemplateApi.getTemplateDetail(props.templateData.id)
        Object.assign(form, {
          id: data.id,
          name: data.name,
          description: data.description,
          businessType: data.businessType,
          category: data.category,
          status: data.status,
          isActive: data.isActive,
          isDefault: data.isDefault,
          sortOrder: data.sortOrder,
          nodes: data.nodes || [],
          conditions: data.conditions || []
        })
      } catch (error) {
        ElMessage.error('获取模板详情失败')
        handleClose()
      }
    } else {
      // 复制模式
      Object.assign(form, props.templateData)
    }
  } else {
    // 新建模式
    resetForm()
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    businessType: '',
    category: 'general',
    status: 'active',
    isActive: true,
    isDefault: false,
    sortOrder: 0,
    nodes: [],
    conditions: []
  })
}

const addNode = () => {
  const newNode = {
    tempId: Date.now(),
    nodeName: '',
    nodeCode: '',
    nodeType: 'approval',
    nodeOrder: form.nodes.length + 1,
    approverType: 'user',
    approverConfig: '{}',
    approvalMode: 'single',
    nodeConditions: null,
    timeoutHours: 72,
    autoApprove: false,
    isRequired: true,
    description: ''
  }
  
  currentNode.value = newNode
  currentNodeIndex.value = form.nodes.length
  nodeDialogVisible.value = true
}

const editNode = (index) => {
  currentNode.value = { ...form.nodes[index] }
  currentNodeIndex.value = index
  nodeDialogVisible.value = true
}

const removeNode = (index) => {
  form.nodes.splice(index, 1)
  // 重新设置节点顺序
  form.nodes.forEach((node, idx) => {
    node.nodeOrder = idx + 1
  })
}

const handleNodeDialogClose = () => {
  nodeDialogVisible.value = false
  currentNode.value = null
  currentNodeIndex.value = -1
}

const handleNodeSave = (nodeData) => {
  if (currentNodeIndex.value >= 0 && currentNodeIndex.value < form.nodes.length) {
    // 编辑现有节点
    form.nodes[currentNodeIndex.value] = nodeData
  } else {
    // 添加新节点
    form.nodes.push(nodeData)
  }
  
  handleNodeDialogClose()
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (form.nodes.length === 0) {
      ElMessage.error('请至少添加一个审批节点')
      return
    }
    
    saving.value = true
    
    const submitData = {
      ...form,
      nodes: form.nodes.map((node, index) => ({
        ...node,
        nodeOrder: index + 1
      }))
    }
    
    if (props.mode === 'create') {
      await workflowTemplateApi.createTemplate(submitData)
      ElMessage.success('创建成功')
    } else {
      await workflowTemplateApi.updateTemplate(form.id, submitData)
      ElMessage.success('更新成功')
    }
    
    emit('success')
  } catch (error) {
    if (error.errors) {
      // 表单验证错误
      return
    }
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleClose = () => {
  emit('close')
}

// 辅助方法
const getApproverTypeName = (type) => {
  const types = {
    'user': '指定用户',
    'role': '指定角色',
    'dept': '指定部门',
    'manager': '直接上级',
    'custom': '自定义'
  }
  return types[type] || type
}

const getApprovalModeName = (mode) => {
  const modes = {
    'single': '单人审批',
    'all': '全部同意',
    'any': '任意一人',
    'majority': '多数同意'
  }
  return modes[mode] || mode
}
</script>

<style scoped>
.workflow-template-dialog {
  max-height: 70vh;
  overflow-y: auto;
}

.nodes-section {
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.nodes-container {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  min-height: 200px;
}

.empty-nodes {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.nodes-list {
  padding: 16px;
}

.node-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.node-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  border-radius: 8px 8px 0 0;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.node-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.node-name {
  font-weight: 500;
  color: #1a1a1a;
}

.node-actions {
  display: flex;
  gap: 8px;
}

.node-details {
  padding: 16px;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.label {
  min-width: 100px;
  color: #666;
  font-size: 14px;
}

.value {
  color: #1a1a1a;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .node-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .detail-item {
    flex-direction: column;
  }
  
  .label {
    min-width: auto;
    margin-bottom: 4px;
  }
}
</style>