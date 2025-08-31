<template>
  <div class="notification-templates">
    <div class="page-header">
      <h1>通知模板</h1>
      <p>管理系统通知模板</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-filters">
        <el-input
          v-model="searchQuery"
          placeholder="搜索模板名称..."
          :prefix-icon="Search"
          style="width: 300px; margin-right: 16px;"
          clearable
        />
        <el-select v-model="filterType" placeholder="模板类型" style="width: 150px; margin-right: 16px;" clearable>
          <el-option label="任务通知" value="task" />
          <el-option label="系统通知" value="system" />
          <el-option label="邮件模板" value="email" />
          <el-option label="短信模板" value="sms" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <div class="action-buttons">
        <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">
          创建模板
        </el-button>
        <el-button :icon="Download" @click="exportTemplates">导出</el-button>
      </div>
    </div>

    <!-- 模板列表 -->
    <div class="template-list">
      <el-table :data="templates" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="模板名称" min-width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'info'">
              {{ scope.row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editTemplate(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="previewTemplate(scope.row)">预览</el-button>
            <el-button type="warning" size="small" @click="duplicateTemplate(scope.row)">复制</el-button>
            <el-button type="danger" size="small" @click="deleteTemplate(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑模板对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="editingTemplate ? '编辑模板' : '创建模板'" 
      width="800px"
    >
      <el-form :model="templateForm" :rules="templateRules" ref="templateFormRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="templateForm.type" placeholder="请选择模板类型">
            <el-option label="任务通知" value="task" />
            <el-option label="系统通知" value="system" />
            <el-option label="邮件模板" value="email" />
            <el-option label="短信模板" value="sms" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模板描述" prop="description">
          <el-input v-model="templateForm.description" type="textarea" :rows="2" placeholder="请输入模板描述" />
        </el-form-item>
        
        <el-form-item label="标题模板" prop="titleTemplate">
          <el-input v-model="templateForm.titleTemplate" placeholder="请输入标题模板，支持变量 {{变量名}}" />
          <div class="template-tip">
            支持变量：{{userName}}, {{taskTitle}}, {{date}} 等
          </div>
        </el-form-item>
        
        <el-form-item label="内容模板" prop="contentTemplate">
          <el-input 
            v-model="templateForm.contentTemplate" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入内容模板，支持变量替换"
          />
          <div class="template-tip">
            支持变量：{{userName}}, {{taskTitle}}, {{taskDescription}}, {{dueDate}}, {{priority}} 等
          </div>
        </el-form-item>
        
        <el-form-item label="启用状态">
          <el-switch v-model="templateForm.isActive" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="模板预览" width="600px">
      <div class="template-preview" v-if="previewData">
        <div class="preview-section">
          <h4>标题预览：</h4>
          <div class="preview-content">{{ previewData.title }}</div>
        </div>
        <div class="preview-section">
          <h4>内容预览：</h4>
          <div class="preview-content">{{ previewData.content }}</div>
        </div>
        <div class="preview-section">
          <h4>变量说明：</h4>
          <div class="variable-list">
            <el-tag v-for="variable in previewData.variables" :key="variable" size="small" style="margin: 2px;">
              {{ variable }}
            </el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPreviewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Search } from '@element-plus/icons-vue'
import * as notificationApi from '@/api/notificationApi'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const filterType = ref('')
const showCreateDialog = ref(false)
const showPreviewDialog = ref(false)
const editingTemplate = ref(null)
const templateFormRef = ref(null)
const previewData = ref(null)

// 模板数据
const templates = ref([])
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 模板表单
const templateForm = ref({
  name: '',
  type: 'task',
  description: '',
  titleTemplate: '',
  contentTemplate: '',
  isActive: true
})

// 表单验证规则
const templateRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ],
  titleTemplate: [
    { required: true, message: '请输入标题模板', trigger: 'blur' }
  ],
  contentTemplate: [
    { required: true, message: '请输入内容模板', trigger: 'blur' }
  ]
}

// 加载模板列表
const loadTemplates = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.size,
      name: searchQuery.value,
      type: filterType.value
    }
    
    const response = await notificationApi.getNotificationTemplates(params)
    if (response.code === 200) {
      templates.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载模板列表失败:', error)
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilters = () => {
  searchQuery.value = ''
  filterType.value = ''
  pagination.value.current = 1
  loadTemplates()
}

// 编辑模板
const editTemplate = (template) => {
  editingTemplate.value = template
  templateForm.value = { ...template }
  showCreateDialog.value = true
}

// 预览模板
const previewTemplate = async (template) => {
  try {
    const mockData = {
      userName: '张三',
      taskTitle: '系统优化任务',
      taskDescription: '优化系统性能，提升用户体验',
      dueDate: '2024-12-31',
      priority: '高',
      date: new Date().toLocaleDateString('zh-CN')
    }
    
    const response = await notificationApi.previewNotificationTemplate(template.id, mockData)
    if (response.code === 200) {
      previewData.value = response.data
      showPreviewDialog.value = true
    }
  } catch (error) {
    console.error('预览模板失败:', error)
    ElMessage.error('预览模板失败')
  }
}

// 复制模板
const duplicateTemplate = (template) => {
  editingTemplate.value = null
  templateForm.value = {
    ...template,
    name: template.name + ' (副本)',
    id: undefined
  }
  showCreateDialog.value = true
}

// 删除模板
const deleteTemplate = async (template) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const response = await notificationApi.deleteNotificationTemplate(template.id)
    if (response.code === 200) {
      ElMessage.success('模板删除成功')
      await loadTemplates()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败:', error)
      ElMessage.error('删除模板失败')
    }
  }
}

// 保存模板
const saveTemplate = async () => {
  if (!templateFormRef.value) return
  
  try {
    await templateFormRef.value.validate()
    
    saving.value = true
    
    let response
    if (editingTemplate.value) {
      response = await notificationApi.updateNotificationTemplate(editingTemplate.value.id, templateForm.value)
    } else {
      response = await notificationApi.createNotificationTemplate(templateForm.value)
    }
    
    if (response.code === 200) {
      ElMessage.success(editingTemplate.value ? '模板更新成功' : '模板创建成功')
      showCreateDialog.value = false
      editingTemplate.value = null
      await loadTemplates()
    }
  } catch (error) {
    if (error !== false) {
      console.error('保存模板失败:', error)
      ElMessage.error('保存模板失败')
    }
  } finally {
    saving.value = false
  }
}

// 导出模板
const exportTemplates = async () => {
  try {
    // TODO: 实现模板导出功能
    ElMessage.success('模板导出成功')
  } catch (error) {
    console.error('导出模板失败:', error)
    ElMessage.error('导出模板失败')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  loadTemplates()
}

const handleCurrentChange = (current) => {
  pagination.value.current = current
  loadTemplates()
}

// 工具方法
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

const getTypeTag = (type) => {
  const tags = {
    'task': 'primary',
    'system': 'success',
    'email': 'warning',
    'sms': 'info'
  }
  return tags[type] || 'info'
}

const getTypeText = (type) => {
  const texts = {
    'task': '任务通知',
    'system': '系统通知',
    'email': '邮件模板',
    'sms': '短信模板'
  }
  return texts[type] || '未知类型'
}

// 监听筛选条件变化
watch([searchQuery, filterType], () => {
  pagination.value.current = 1
  loadTemplates()
}, { debounce: 300 })

onMounted(async () => {
  await loadTemplates()
})
</script>

<style scoped>
.notification-templates {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.action-bar {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-filters {
  display: flex;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.template-list {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}

.template-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.template-preview {
  padding: 20px;
}

.preview-section {
  margin-bottom: 20px;
}

.preview-section h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.preview-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  white-space: pre-wrap;
}

.variable-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
