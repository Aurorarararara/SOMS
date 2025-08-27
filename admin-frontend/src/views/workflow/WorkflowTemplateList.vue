<template>
  <div class="workflow-template-container">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">审批流程配置</h1>
        <p class="page-description">管理系统中的所有审批流程模板，支持自定义审批节点和条件</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建流程模板
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="模板名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入模板名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select 
            v-model="searchForm.businessType" 
            placeholder="请选择业务类型"
            clearable
            style="width: 150px"
          >
            <el-option label="请假申请" value="leave_application" />
            <el-option label="费用报销" value="expense_claim" />
            <el-option label="采购申请" value="purchase_request" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select 
            v-model="searchForm.category" 
            placeholder="请选择分类"
            clearable
            style="width: 120px"
          >
            <el-option label="人事" value="hr" />
            <el-option label="财务" value="finance" />
            <el-option label="采购" value="purchase" />
            <el-option label="通用" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" value="active" />
            <el-option label="草稿" value="draft" />
            <el-option label="归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table 
        v-loading="loading"
        :data="tableData" 
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="name" label="模板名称" min-width="180" sortable="custom">
          <template #default="{ row }">
            <div class="template-name">
              <span class="name">{{ row.name }}</span>
              <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="businessType" label="业务类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTagType(row.businessType)" size="small">
              {{ getBusinessTypeName(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            {{ getCategoryName(row.category) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="version" label="版本" width="80" align="center">
          <template #default="{ row }">
            <span class="version-badge">v{{ row.version }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="审批节点" width="120">
          <template #default="{ row }">
            <span class="node-count">{{ row.nodes?.length || 0 }} 个节点</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="creatorName" label="创建者" width="120" />
        
        <el-table-column prop="createTime" label="创建时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link type="primary" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="primary" @click="handleCopy(row)">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
              <el-dropdown @command="(command) => handleMoreAction(command, row)">
                <el-button link type="primary">
                  更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      :command="`toggle-${row.id}`"
                      :disabled="row.status === 'archived'"
                    >
                      {{ row.isActive ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="`default-${row.id}`"
                      :disabled="row.isDefault || !row.isActive"
                    >
                      设为默认
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="`delete-${row.id}`"
                      :disabled="row.isDefault"
                    >
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <WorkflowTemplateDialog
      v-if="dialogVisible"
      :visible="dialogVisible"
      :template-data="currentTemplate"
      :mode="dialogMode"
      @close="handleDialogClose"
      @success="handleDialogSuccess"
    />

    <!-- 复制模板对话框 -->
    <el-dialog
      v-model="copyDialogVisible"
      title="复制流程模板"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="copyForm" :rules="copyRules" ref="copyFormRef" label-width="100px">
        <el-form-item label="新模板名称" prop="newName">
          <el-input v-model="copyForm.newName" placeholder="请输入新模板名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="copyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmCopy" :loading="copyLoading">
          确定复制
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, View, Edit, CopyDocument, ArrowDown
} from '@element-plus/icons-vue'
import { workflowTemplateApi } from '@/api/workflow'
import WorkflowTemplateDialog from './components/WorkflowTemplateDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentTemplate = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  businessType: '',
  category: '',
  status: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 复制对话框
const copyDialogVisible = ref(false)
const copyLoading = ref(false)
const copyFormRef = ref(null)
const copyForm = reactive({
  newName: ''
})
const copyRules = {
  newName: [
    { required: true, message: '请输入新模板名称', trigger: 'blur' }
  ]
}
const copyTemplateId = ref(null)

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    const { data } = await workflowTemplateApi.getTemplateList(params)
    tableData.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.current = 1
  loadData()
}

const handleCreate = () => {
  dialogMode.value = 'create'
  currentTemplate.value = null
  dialogVisible.value = true
}

const handleView = (row) => {
  dialogMode.value = 'view'
  currentTemplate.value = row
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  currentTemplate.value = row
  dialogVisible.value = true
}

const handleCopy = (row) => {
  copyForm.newName = `${row.name} - 副本`
  copyTemplateId.value = row.id
  copyDialogVisible.value = true
}

const handleConfirmCopy = async () => {
  if (!copyFormRef.value) return
  
  try {
    await copyFormRef.value.validate()
    copyLoading.value = true
    
    await workflowTemplateApi.copyTemplate(copyTemplateId.value, copyForm.newName)
    
    ElMessage.success('模板复制成功')
    copyDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '复制失败')
  } finally {
    copyLoading.value = false
  }
}

const handleMoreAction = async (command, row) => {
  const [action, id] = command.split('-')
  
  switch (action) {
    case 'toggle':
      await handleToggleStatus(row)
      break
    case 'default':
      await handleSetDefault(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.isActive ? '禁用' : '启用'}该模板吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await workflowTemplateApi.toggleTemplateStatus(row.id, !row.isActive)
    ElMessage.success(`${row.isActive ? '禁用' : '启用'}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleSetDefault = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将"${row.name}"设为${getBusinessTypeName(row.businessType)}的默认模板吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await workflowTemplateApi.setDefaultTemplate(row.id, row.businessType)
    ElMessage.success('设置默认模板成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '设置失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.name}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await workflowTemplateApi.deleteTemplate(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleDialogClose = () => {
  dialogVisible.value = false
  currentTemplate.value = null
}

const handleDialogSuccess = () => {
  dialogVisible.value = false
  loadData()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

const handleSortChange = ({ prop, order }) => {
  // TODO: 实现排序逻辑
  console.log('排序:', prop, order)
}

// 辅助方法
const getBusinessTypeName = (type) => {
  const types = {
    'leave_application': '请假申请',
    'expense_claim': '费用报销',
    'purchase_request': '采购申请',
    'other': '其他'
  }
  return types[type] || type
}

const getBusinessTypeTagType = (type) => {
  const types = {
    'leave_application': 'primary',
    'expense_claim': 'success',
    'purchase_request': 'warning',
    'other': 'info'
  }
  return types[type] || 'info'
}

const getCategoryName = (category) => {
  const categories = {
    'hr': '人事',
    'finance': '财务',
    'purchase': '采购',
    'general': '通用'
  }
  return categories[category] || category
}

const getStatusName = (status) => {
  const statuses = {
    'active': '启用',
    'draft': '草稿',
    'archived': '归档'
  }
  return statuses[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    'active': 'success',
    'draft': 'warning',
    'archived': 'info'
  }
  return types[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.workflow-template-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-description {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-card {
  margin-bottom: 20px;
}

.template-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-weight: 500;
  color: #1a1a1a;
}

.version-badge {
  background: #f0f9ff;
  color: #1e40af;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.node-count {
  color: #666;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .workflow-template-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>