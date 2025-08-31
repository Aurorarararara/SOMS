<template>
  <div class="notification-management">
    <div class="page-header">
      <h1>通知管理</h1>
      <p>管理系统通知和用户通知设置</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-filters">
        <el-input
          v-model="searchQuery"
          placeholder="搜索通知内容..."
          :prefix-icon="Search"
          style="width: 300px; margin-right: 16px;"
          clearable
        />
        <el-select v-model="filterType" placeholder="通知类型" style="width: 150px; margin-right: 16px;" clearable>
          <el-option label="任务分配" value="task_assigned" />
          <el-option label="任务更新" value="task_updated" />
          <el-option label="任务完成" value="task_completed" />
          <el-option label="任务逾期" value="task_overdue" />
          <el-option label="系统通知" value="system_notice" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" style="width: 120px; margin-right: 16px;" clearable>
          <el-option label="未读" value="unread" />
          <el-option label="已读" value="read" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <div class="action-buttons">
        <el-button type="primary" :icon="Plus" @click="showSendDialog = true">
          发送通知
        </el-button>
        <el-button :icon="Download" @click="exportNotifications">导出</el-button>
        <el-button :icon="Operation" @click="showBatchActions = !showBatchActions">
          批量操作
        </el-button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="showBatchActions" class="batch-actions">
      <el-button type="success" size="small" @click="batchMarkAsRead">批量标记已读</el-button>
      <el-button type="danger" size="small" @click="batchDelete">批量删除</el-button>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <el-table 
        :data="notifications" 
        v-loading="loading" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="通知标题" min-width="200">
          <template #default="scope">
            <div class="notification-title-cell">
              <span class="notification-title">{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.priority === 'urgent'" type="danger" size="small">紧急</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="notificationType" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getNotificationTypeTag(scope.row.notificationType)" size="small">
              {{ getNotificationTypeText(scope.row.notificationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recipientName" label="接收人" width="120" />
        <el-table-column prop="isRead" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isRead ? 'success' : 'warning'" size="small">
              {{ scope.row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="readTime" label="阅读时间" width="160">
          <template #default="scope">
            {{ scope.row.readTime ? formatTime(scope.row.readTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewNotification(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="resendNotification(scope.row)">重发</el-button>
            <el-button type="danger" size="small" @click="deleteNotification(scope.row)">删除</el-button>
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

    <!-- 发送通知对话框 -->
    <el-dialog v-model="showSendDialog" title="发送通知" width="600px">
      <el-form :model="sendForm" label-width="100px">
        <el-form-item label="通知标题">
          <el-input v-model="sendForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="sendForm.notificationType" placeholder="请选择通知类型">
            <el-option label="系统通知" value="system_notice" />
            <el-option label="任务通知" value="task_assigned" />
            <el-option label="紧急通知" value="urgent_notice" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收人">
          <el-select v-model="sendForm.recipientIds" multiple placeholder="请选择接收人">
            <el-option label="全体用户" value="all" />
            <el-option label="张三" value="1" />
            <el-option label="李四" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="sendForm.priority" placeholder="请选择优先级">
            <el-option label="普通" value="normal" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSendDialog = false">取消</el-button>
        <el-button type="primary" @click="sendNotification" :loading="sending">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Operation, Plus, Download, Search
} from '@element-plus/icons-vue'
import * as notificationApi from '@/api/notificationApi'

// 响应式数据
const loading = ref(false)
const sending = ref(false)
const searchQuery = ref('')
const filterType = ref('')
const filterStatus = ref('')
const showBatchActions = ref(false)
const showSendDialog = ref(false)
const selectedNotifications = ref([])

// 通知数据
const notifications = ref([])
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 发送表单
const sendForm = ref({
  title: '',
  content: '',
  notificationType: 'system_notice',
  recipientIds: [],
  priority: 'normal'
})

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.size,
      title: searchQuery.value,
      notificationType: filterType.value,
      isRead: filterStatus.value === 'read' ? true : filterStatus.value === 'unread' ? false : undefined
    }
    
    const response = await notificationApi.getAllNotifications(params)
    if (response.code === 200) {
      notifications.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载通知列表失败:', error)
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

// 事件处理方法
const resetFilters = () => {
  searchQuery.value = ''
  filterType.value = ''
  filterStatus.value = ''
  pagination.value.current = 1
  loadNotifications()
}

const handleSelectionChange = (selection) => {
  selectedNotifications.value = selection
}

const viewNotification = (notification) => {
  // TODO: 实现查看通知详情
  ElMessage.info('查看通知详情功能开发中')
}

const resendNotification = async (notification) => {
  try {
    // TODO: 实现重发通知API
    ElMessage.success('通知重发成功')
  } catch (error) {
    console.error('重发通知失败:', error)
    ElMessage.error('重发通知失败')
  }
}

const deleteNotification = async (notification) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const response = await notificationApi.adminBatchDeleteNotifications([notification.id])
    if (response.code === 200) {
      ElMessage.success('通知删除成功')
      await loadNotifications()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
    }
  }
}

const sendNotification = async () => {
  sending.value = true
  try {
    const response = await notificationApi.sendNotification(sendForm.value)
    if (response.code === 200) {
      ElMessage.success('通知发送成功')
      showSendDialog.value = false
      await loadNotifications()
    }
  } catch (error) {
    console.error('发送通知失败:', error)
    ElMessage.error('发送通知失败')
  } finally {
    sending.value = false
  }
}

const batchMarkAsRead = async () => {
  if (selectedNotifications.value.length === 0) {
    ElMessage.warning('请先选择要标记的通知')
    return
  }
  
  try {
    const notificationIds = selectedNotifications.value.map(n => n.id)
    const response = await notificationApi.batchMarkAsRead(notificationIds)
    if (response.code === 200) {
      ElMessage.success('批量标记已读成功')
      await loadNotifications()
    }
  } catch (error) {
    console.error('批量标记已读失败:', error)
    ElMessage.error('批量标记已读失败')
  }
}

const batchDelete = async () => {
  if (selectedNotifications.value.length === 0) {
    ElMessage.warning('请先选择要删除的通知')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedNotifications.value.length} 条通知吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const notificationIds = selectedNotifications.value.map(n => n.id)
    const response = await notificationApi.adminBatchDeleteNotifications(notificationIds)
    if (response.code === 200) {
      ElMessage.success('批量删除成功')
      await loadNotifications()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const exportNotifications = async () => {
  try {
    const params = {
      title: searchQuery.value,
      notificationType: filterType.value,
      isRead: filterStatus.value === 'read' ? true : filterStatus.value === 'unread' ? false : undefined
    }
    
    const response = await notificationApi.exportNotifications(params, 'excel')
    
    const blob = new Blob([response], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `notifications_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('通知导出成功')
  } catch (error) {
    console.error('导出通知失败:', error)
    ElMessage.error('导出通知失败')
  }
}

// 分页处理方法
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  loadNotifications()
}

const handleCurrentChange = (current) => {
  pagination.value.current = current
  loadNotifications()
}

// 工具方法
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

const getNotificationTypeTag = (type) => {
  const tags = {
    'task_assigned': 'primary',
    'task_updated': 'warning',
    'task_completed': 'success',
    'task_overdue': 'danger',
    'system_notice': 'info'
  }
  return tags[type] || 'info'
}

const getNotificationTypeText = (type) => {
  const texts = {
    'task_assigned': '任务分配',
    'task_updated': '任务更新',
    'task_completed': '任务完成',
    'task_overdue': '任务逾期',
    'system_notice': '系统通知'
  }
  return texts[type] || '未知类型'
}

// 监听筛选条件变化
watch([searchQuery, filterType, filterStatus], () => {
  pagination.value.current = 1
  loadNotifications()
}, { debounce: 300 })

onMounted(async () => {
  await loadNotifications()
})
</script>
