<template>
  <div class="workflow-designer">
    <div class="designer-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" icon="Plus" @click="addNode">
          添加节点
        </el-button>
        <el-button icon="View" @click="previewFlow">
          预览流程
        </el-button>
        <el-button icon="Document" @click="validateFlow">
          验证流程
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          保存流程
        </el-button>
      </div>
    </div>

    <div class="designer-content">
      <!-- 节点面板 -->
      <div class="node-panel">
        <div class="panel-title">节点类型</div>
        <div class="node-types">
          <div 
            v-for="nodeType in nodeTypes"
            :key="nodeType.type"
            class="node-type-item"
            :class="{ active: selectedNodeType === nodeType.type }"
            @click="selectNodeType(nodeType.type)"
          >
            <div class="node-icon">
              <el-icon :size="20">
                <component :is="nodeType.icon" />
              </el-icon>
            </div>
            <div class="node-label">{{ nodeType.label }}</div>
          </div>
        </div>
      </div>

      <!-- 流程画布 -->
      <div class="flow-canvas" ref="canvasRef">
        <div class="canvas-grid">
          <!-- 开始节点 -->
          <div class="flow-node start-node">
            <div class="node-content">
              <el-icon :size="24"><Flag /></el-icon>
              <span>开始</span>
            </div>
          </div>

          <!-- 审批节点 -->
          <div 
            v-for="(node, index) in flowNodes"
            :key="node.id"
            class="flow-node approval-node"
            :class="{ active: selectedNode?.id === node.id }"
            @click="selectNode(node)"
          >
            <div class="node-content">
              <div class="node-header">
                <span class="node-title">{{ node.name }}</span>
                <div class="node-actions">
                  <el-button 
                    type="text" 
                    size="small" 
                    icon="Edit"
                    @click.stop="editNode(node)"
                  />
                  <el-button 
                    type="text" 
                    size="small" 
                    icon="Delete"
                    @click.stop="deleteNode(node.id)"
                  />
                </div>
              </div>
              <div class="node-info">
                <div class="approver-info">
                  <el-tag size="small" :type="getApproverTypeTag(node.approverType)">
                    {{ getApproverTypeLabel(node.approverType) }}
                  </el-tag>
                </div>
                <div class="strategy-info">
                  <span class="strategy-text">
                    {{ getStrategyLabel(node.approvalStrategy) }}
                  </span>
                </div>
              </div>
            </div>
            <!-- 连接线 -->
            <div v-if="index < flowNodes.length - 1" class="node-connector">
              <div class="connector-line"></div>
              <div class="connector-arrow">
                <el-icon><ArrowDown /></el-icon>
              </div>
            </div>
          </div>

          <!-- 结束节点 -->
          <div class="flow-node end-node">
            <div class="node-content">
              <el-icon :size="24"><CircleCheck /></el-icon>
              <span>结束</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 属性面板 -->
      <div class="property-panel">
        <div class="panel-title">节点属性</div>
        <div v-if="selectedNode" class="property-content">
          <el-form :model="selectedNode" label-width="80px" size="small">
            <el-form-item label="节点名称">
              <el-input v-model="selectedNode.name" placeholder="请输入节点名称" />
            </el-form-item>
            <el-form-item label="审批人">
              <el-tag 
                v-for="approver in getApproverNames(selectedNode)"
                :key="approver"
                size="small"
                style="margin: 2px"
              >
                {{ approver }}
              </el-tag>
            </el-form-item>
            <el-form-item label="审批策略">
              <el-text size="small">
                {{ getStrategyLabel(selectedNode.approvalStrategy) }}
              </el-text>
            </el-form-item>
            <el-form-item label="超时时间">
              <el-text size="small">
                {{ selectedNode.timeoutHours }} 小时
              </el-text>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="editNode(selectedNode)">
                编辑节点
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-else class="empty-selection">
          <el-text type="info">请选择一个节点查看属性</el-text>
        </div>
      </div>
    </div>

    <!-- 节点配置对话框 -->
    <NodeConfigDialog
      v-model="showNodeDialog"
      :node-data="currentNode"
      :is-edit="isEditMode"
      @confirm="handleNodeConfirm"
    />

    <!-- 流程预览对话框 -->
    <el-dialog
      v-model="showPreviewDialog"
      title="流程预览"
      width="800px"
    >
      <div class="flow-preview">
        <div class="preview-steps">
          <el-steps :active="flowNodes.length + 1" direction="vertical">
            <el-step title="开始" icon="Flag" />
            <el-step 
              v-for="(node, index) in flowNodes"
              :key="node.id"
              :title="node.name"
              :description="getNodeDescription(node)"
            />
            <el-step title="结束" icon="CircleCheck" />
          </el-steps>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Flag, CircleCheck, ArrowDown, User, 
  Setting, Document, Plus, Edit, Delete 
} from '@element-plus/icons-vue'
import NodeConfigDialog from './NodeConfigDialog.vue'

// Props
const props = defineProps({
  templateData: {
    type: Object,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['save', 'cancel'])

// 响应式数据
const saving = ref(false)
const canvasRef = ref(null)
const selectedNodeType = ref('APPROVAL')
const selectedNode = ref(null)
const showNodeDialog = ref(false)
const showPreviewDialog = ref(false)
const isEditMode = ref(false)
const currentNode = ref({})

// 流程节点数据
const flowNodes = ref([])

// 节点类型配置
const nodeTypes = [
  {
    type: 'APPROVAL',
    label: '审批节点',
    icon: 'User'
  },
  {
    type: 'CC',
    label: '抄送节点',
    icon: 'Document'
  },
  {
    type: 'CONDITION',
    label: '条件节点',
    icon: 'Setting'
  }
]

// 初始化流程数据
const initFlowData = () => {
  if (props.templateData.nodes && props.templateData.nodes.length > 0) {
    flowNodes.value = [...props.templateData.nodes]
  }
}

// 选择节点类型
const selectNodeType = (type) => {
  selectedNodeType.value = type
}

// 选择节点
const selectNode = (node) => {
  selectedNode.value = node
}

// 添加节点
const addNode = () => {
  currentNode.value = {
    type: selectedNodeType.value,
    name: '',
    approverType: 'USER',
    approverIds: [],
    approvalStrategy: 'ANY',
    required: true,
    timeoutHours: 24,
    timeoutAction: 'REMINDER_ONLY',
    sortOrder: flowNodes.value.length + 1
  }
  isEditMode.value = false
  showNodeDialog.value = true
}

// 编辑节点
const editNode = (node) => {
  currentNode.value = { ...node }
  isEditMode.value = true
  showNodeDialog.value = true
}

// 删除节点
const deleteNode = async (nodeId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个审批节点吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const index = flowNodes.value.findIndex(node => node.id === nodeId)
    if (index > -1) {
      flowNodes.value.splice(index, 1)
      // 重新排序
      flowNodes.value.forEach((node, idx) => {
        node.sortOrder = idx + 1
      })
      ElMessage.success('节点删除成功')
    }
    
    // 如果删除的是选中节点，清空选择
    if (selectedNode.value?.id === nodeId) {
      selectedNode.value = null
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 处理节点配置确认
const handleNodeConfirm = (nodeData) => {
  if (isEditMode.value) {
    // 更新现有节点
    const index = flowNodes.value.findIndex(node => node.id === currentNode.value.id)
    if (index > -1) {
      flowNodes.value[index] = { ...nodeData, id: currentNode.value.id }
    }
  } else {
    // 添加新节点
    const newNode = {
      ...nodeData,
      id: 'node_' + Date.now(),
      sortOrder: flowNodes.value.length + 1
    }
    flowNodes.value.push(newNode)
  }
}

// 预览流程
const previewFlow = () => {
  showPreviewDialog.value = true
}

// 验证流程
const validateFlow = () => {
  if (flowNodes.value.length === 0) {
    ElMessage.warning('流程至少需要包含一个审批节点')
    return false
  }
  
  // 验证每个节点配置
  for (let node of flowNodes.value) {
    if (!node.name) {
      ElMessage.warning(`节点 ${node.sortOrder} 缺少名称`)
      return false
    }
    
    if (node.approverType !== 'DYNAMIC' && (!node.approverIds || node.approverIds.length === 0)) {
      ElMessage.warning(`节点 "${node.name}" 缺少审批人配置`)
      return false
    }
    
    if (node.approverType === 'DYNAMIC' && !node.dynamicRule) {
      ElMessage.warning(`节点 "${node.name}" 缺少动态规则配置`)
      return false
    }
  }
  
  ElMessage.success('流程验证通过')
  return true
}

// 保存流程
const handleSave = async () => {
  if (!validateFlow()) {
    return
  }
  
  saving.value = true
  try {
    const flowData = {
      ...props.templateData,
      nodes: flowNodes.value
    }
    
    emit('save', flowData)
    ElMessage.success('流程保存成功')
  } catch (error) {
    console.error('保存流程失败:', error)
    ElMessage.error('保存流程失败')
  } finally {
    saving.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  emit('cancel')
}

// 获取审批人类型标签
const getApproverTypeTag = (type) => {
  const tagMap = {
    USER: '',
    ROLE: 'success',
    DEPARTMENT: 'warning',
    DYNAMIC: 'info'
  }
  return tagMap[type] || ''
}

// 获取审批人类型标签文本
const getApproverTypeLabel = (type) => {
  const labelMap = {
    USER: '指定用户',
    ROLE: '指定角色',
    DEPARTMENT: '指定部门',
    DYNAMIC: '动态指定'
  }
  return labelMap[type] || type
}

// 获取策略标签
const getStrategyLabel = (strategy) => {
  const labelMap = {
    ANY: '任意一人通过',
    ALL: '所有人通过',
    SEQUENTIAL: '按顺序审批'
  }
  return labelMap[strategy] || strategy
}

// 获取审批人名称列表
const getApproverNames = (node) => {
  // 这里应该根据实际的审批人ID获取名称
  // 暂时返回模拟数据
  if (node.approverType === 'DYNAMIC') {
    return [node.dynamicRule === 'DIRECT_MANAGER' ? '直属上级' : '部门负责人']
  }
  
  if (node.approverIds && node.approverIds.length > 0) {
    return node.approverIds.map(id => `审批人${id}`)
  }
  
  return ['未配置']
}

// 获取节点描述
const getNodeDescription = (node) => {
  const approverText = getApproverNames(node).join(', ')
  const strategyText = getStrategyLabel(node.approvalStrategy)
  return `${approverText} - ${strategyText}`
}

// 初始化
initFlowData()
</script>

<style scoped>
.workflow-designer {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.designer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

.designer-content {
  flex: 1;
  display: flex;
  height: calc(100vh - 200px);
}

.node-panel {
  width: 200px;
  background: white;
  border-right: 1px solid #e0e0e0;
  padding: 16px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.node-types {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.node-type-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.node-type-item:hover {
  background-color: #f8f9fa;
  border-color: #409eff;
}

.node-type-item.active {
  background-color: #e6f7ff;
  border-color: #409eff;
  color: #409eff;
}

.node-icon {
  margin-right: 8px;
}

.node-label {
  font-size: 12px;
  font-weight: 500;
}

.flow-canvas {
  flex: 1;
  padding: 24px;
  overflow: auto;
  background: linear-gradient(90deg, rgba(0,0,0,0.04) 1px, transparent 1px),
              linear-gradient(rgba(0,0,0,0.04) 1px, transparent 1px);
  background-size: 20px 20px;
}

.canvas-grid {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  min-height: 100%;
}

.flow-node {
  position: relative;
  min-width: 280px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.flow-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.flow-node.active {
  border: 2px solid #409eff;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1);
}

.start-node, .end-node {
  padding: 20px;
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
  color: white;
  text-align: center;
  font-weight: 600;
}

.end-node {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
}

.approval-node {
  padding: 16px;
}

.node-content {
  width: 100%;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.node-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.node-actions {
  display: flex;
  gap: 4px;
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.approver-info {
  display: flex;
  align-items: center;
}

.strategy-info {
  font-size: 12px;
  color: #666;
}

.node-connector {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.connector-line {
  width: 2px;
  height: 20px;
  background-color: #409eff;
}

.connector-arrow {
  color: #409eff;
}

.property-panel {
  width: 280px;
  background: white;
  border-left: 1px solid #e0e0e0;
  padding: 16px;
}

.property-content {
  margin-top: 16px;
}

.empty-selection {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #999;
}

.flow-preview {
  padding: 20px;
}

.preview-steps {
  max-height: 600px;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .node-panel {
    width: 160px;
  }
  
  .property-panel {
    width: 240px;
  }
  
  .flow-node {
    min-width: 240px;
  }
}

@media (max-width: 768px) {
  .designer-content {
    flex-direction: column;
    height: auto;
  }
  
  .node-panel {
    width: 100%;
    height: 120px;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .node-types {
    flex-direction: row;
    overflow-x: auto;
  }
  
  .property-panel {
    width: 100%;
    height: 200px;
    border-left: none;
    border-top: 1px solid #e0e0e0;
  }
  
  .flow-canvas {
    flex: none;
    height: 400px;
  }
}
</style>