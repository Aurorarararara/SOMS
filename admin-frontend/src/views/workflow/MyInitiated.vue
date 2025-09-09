<template>
  <div class="my-initiated-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">我发起的流程</h1>
        <p class="page-description">管理您发起的所有审批流程</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="startNewWorkflow">
          发起新流程
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card total-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ instanceStats.total }}</div>
              <div class="stat-label">全部流程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card pending-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ instanceStats.pending }}</div>
              <div class="stat-label">审批中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card completed-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ instanceStats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card rejected-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ instanceStats.rejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 流程列表 -->
    <el-card class="instance-list-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">流程实例列表</span>
          <div class="header-actions">
            <el-button 
              type="text" 
              icon="Refresh" 
              @click="loadMyInstances"
              :loading="loading"
            >
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选器 -->
      <div class="instance-filters">
        <el-row :gutter="16">
          <el-col :span="4">
            <el-select v-model="filters.status" placeholder="流程状态" clearable>
              <el-option label="审批中" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已撤回" value="WITHDRAWN" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="filters.businessType" placeholder="业务类型" clearable>
              <el-option label="请假申请" value="LEAVE" />
              <el-option label="费用报销" value="EXPENSE" />
              <el-option label="采购申请" value="PURCHASE" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
            />
          </el-col>
          <el-col :span="6">
            <el-input 
              v-model="filters.keyword" 
              placeholder="搜索流程标题"
              prefix-icon="Search"
              clearable
            />
          </el-col>
          <el-col :span="4">
            <el-button type="primary" icon="Search" @click="handleSearch">
              搜索
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 流程实例列表 -->
      <div class="instance-list" v-loading="loading">
        <div 
          v-for="instance in filteredInstances" 
          :key="instance.id"
          class="instance-item"
        >
          <div class="instance-content">
            <div class="instance-header">
              <div class="instance-title">
                <h3>{{ instance.title }}</h3>
                <div class="instance-badges">
                  <el-tag 
                    :type="getStatusTag(instance.status)" 
                    size="small"
                  >
                    {{ getStatusLabel(instance.status) }}
                  </el-tag>
                  <el-tag 
                    :type="getBusinessTypeTag(instance.businessType)" 
                    size="small"
                  >
                    {{ getBusinessTypeLabel(instance.businessType) }}
                  </el-tag>
                </div>
              </div>
              <div class="instance-meta">
                <span class="instance-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(instance.createTime) }}
                </span>
                <span v-if="instance.endTime" class="instance-end-time">
                  完成: {{ formatTime(instance.endTime) }}
                </span>
              </div>
            </div>

            <div class="instance-body">
              <div class="instance-description">
                {{ instance.content || '暂无描述' }}
              </div>
              
              <div class="instance-info">
                <div class="info-item">
                  <span class="info-label">流程模板:</span>
                  <span class="info-value">{{ instance.templateName }}</span>
                </div>
                <div class="info-item" v-if="instance.currentNodeName">
                  <span class="info-label">当前节点:</span>
                  <span class="info-value">{{ instance.currentNodeName }}</span>
                </div>
                <div class="info-item" v-if="instance.duration">
                  <span class="info-label">处理时长:</span>
                  <span class="info-value">{{ instance.duration }}</span>
                </div>
              </div>
            </div>

            <!-- 进度条 -->
            <div v-if="instance.status === 'PENDING'" class="instance-progress">
              <div class="progress-info">
                <span class="progress-text">流程进度</span>
                <span class="progress-percent">{{ instance.progress }}%</span>
              </div>
              <el-progress 
                :percentage="instance.progress" 
                :color="getProgressColor(instance.progress)"
                :show-text="false"
              />
            </div>

            <div class="instance-actions">
              <el-button type="primary" size="small" @click="viewDetail(instance)">
                查看详情
              </el-button>
              <el-button type="info" size="small" @click="viewHistory(instance)">
                流程历史
              </el-button>
              <el-button 
                v-if="instance.status === 'PENDING'"
                type="warning" 
                size="small" 
                @click="handleWithdrawInstance(instance)"
              >
                撤回流程
              </el-button>
              <el-dropdown @command="handleMoreAction" class="more-actions">
                <el-button type="text" size="small">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="`copy-${instance.id}`">
                      复制流程
                    </el-dropdown-item>
                    <el-dropdown-item 
                      v-if="instance.status !== 'COMPLETED'"
                      :command="`print-${instance.id}`"
                    >
                      打印申请表
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <div v-if="filteredInstances.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无流程记录">
            <el-button type="primary" @click="startNewWorkflow">发起新流程</el-button>
          </el-empty>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 流程详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="流程详情"
      width="900px"
    >
      <div v-if="instanceDetail" class="instance-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="流程标题">{{ instanceDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="流程状态">
            <el-tag :type="getStatusTag(instanceDetail.status)">
              {{ getStatusLabel(instanceDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ getBusinessTypeLabel(instanceDetail.businessType) }}</el-descriptions-item>
          <el-descriptions-item label="流程模板">{{ instanceDetail.templateName }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ formatTime(instanceDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ instanceDetail.endTime ? formatTime(instanceDetail.endTime) : '未完成' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h4>申请内容</h4>
          <div class="content-box">
            {{ instanceDetail.content || '暂无内容' }}
          </div>
        </div>

        <div v-if="instanceDetail.status === 'PENDING'" class="detail-section">
          <h4>当前状态</h4>
          <div class="current-status">
            <p><strong>当前节点:</strong> {{ instanceDetail.currentNodeName }}</p>
            <p><strong>待处理人:</strong> {{ instanceDetail.currentApprovers || '获取中...' }}</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 流程历史对话框 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="流程历史"
      width="800px"
    >
      <div class="history-content">
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in workflowHistory"
            :key="index"
            :timestamp="formatTime(item.createTime)"
            placement="top"
            :color="getHistoryColor(item.action)"
          >
            <el-card>
              <div class="history-item">
                <div class="history-header">
                  <span class="history-action">{{ item.actionName }}</span>
                  <span class="history-operator">{{ item.operatorName || '系统' }}</span>
                </div>
                <div v-if="item.comment" class="history-comment">
                  {{ item.comment }}
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

    <!-- 发起新流程对话框 -->
    <StartWorkflowDialog
      v-model="showStartDialog"
      @success="handleWorkflowStarted"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, Clock, CircleCheck, CircleClose, 
  Plus, Search, Refresh, ArrowDown 
} from '@element-plus/icons-vue'
import { getMyInitiatedInstances, getInstanceHistory, withdrawInstance } from '@/api/workflow'
import StartWorkflowDialog from '@/components/workflow/StartWorkflowDialog.vue'
import { useUserStore } from '@/stores/user'

// 响应式数据
const loading = ref(false)
const instances = ref([])
const userStore = useUserStore()
const instanceStats = reactive({
  total: 0,
  pending: 0,
  completed: 0,
  rejected: 0
})

const filters = reactive({
  status: '',
  businessType: '',
  keyword: '',
  dateRange: []
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const showDetailDialog = ref(false)
const showHistoryDialog = ref(false)
const showStartDialog = ref(false)
const instanceDetail = ref(null)
const workflowHistory = ref([])

// 计算属性
const filteredInstances = computed(() => {
  let result = [...instances.value]
  
  if (filters.status) {
    result = result.filter(instance => instance.status === filters.status)
  }
  
  if (filters.businessType) {
    result = result.filter(instance => instance.businessType === filters.businessType)
  }
  
  if (filters.keyword) {
    const keyword = filters.keyword.toLowerCase()
    result = result.filter(instance => 
      instance.title.toLowerCase().includes(keyword)
    )
  }
  
  if (filters.dateRange && filters.dateRange.length === 2) {
    const [startDate, endDate] = filters.dateRange
    result = result.filter(instance => {
      const createDate = new Date(instance.createTime).toISOString().split('T')[0]
      return createDate >= startDate && createDate <= endDate
    })
  }
  
  return result
})

// 加载我发起的流程实例
const loadMyInstances = async () => {
  loading.value = true
  try {
    const response = await getMyInitiatedInstances()
    if (response.success) {
      instances.value = response.data || []
      updateInstanceStats()
    }
  } catch (error) {
    console.error('加载我发起的流程失败:', error)
    ElMessage.error('加载我发起的流程失败')
  } finally {
    loading.value = false
  }
}

// 更新流程统计
const updateInstanceStats = () => {
  instanceStats.total = instances.value.length
  instanceStats.pending = instances.value.filter(instance => instance.status === 'PENDING').length
  instanceStats.completed = instances.value.filter(instance => instance.status === 'COMPLETED').length
  instanceStats.rejected = instances.value.filter(instance => instance.status === 'REJECTED').length
}

// 查看详情
const viewDetail = (instance) => {
  instanceDetail.value = instance
  showDetailDialog.value = true
}

// 查看历史
const viewHistory = async (instance) => {
  try {
    const response = await getInstanceHistory(instance.id)
    if (response.success) {
      workflowHistory.value = response.data || []
      showHistoryDialog.value = true
    }
  } catch (error) {
    console.error('获取流程历史失败:', error)
    ElMessage.error('获取流程历史失败')
  }
}

// 撤回流程
const handleWithdrawInstance = async (instance) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入撤回原因', 
      '撤回流程', 
      {
        confirmButtonText: '确认撤回',
        cancelButtonText: '取消',
        inputPattern: /.{1,}/,
        inputErrorMessage: '撤回原因不能为空'
      }
    )
    
    const response = await withdrawInstance(instance.id, {
      userId: userStore.userInfo.id,
      reason: reason
    })
    
    if (response.success) {
      ElMessage.success('流程撤回成功')
      await loadMyInstances() // 刷新列表
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回流程失败:', error)
      ElMessage.error('撤回流程失败')
    }
  }
}

// 发起新流程
const startNewWorkflow = () => {
  showStartDialog.value = true
}

// 处理流程启动成功
const handleWorkflowStarted = () => {
  loadMyInstances()
}

// 处理更多操作
const handleMoreAction = (command) => {
  const [action, instanceId] = command.split('-')
  
  switch (action) {
    case 'copy':
      ElMessage.info('复制流程功能开发中')
      break
    case 'print':
      ElMessage.info('打印功能开发中')
      break
  }
}

// 搜索处理
const handleSearch = () => {
  // 触发计算属性重新计算
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  // TODO: 实现分页加载
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  // TODO: 实现分页加载
}

// 工具方法
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusTag = (status) => {
  const tagMap = {
    'PENDING': '',
    'COMPLETED': 'success',
    'REJECTED': 'danger',
    'WITHDRAWN': 'warning'
  }
  return tagMap[status] || 'info'
}

const getStatusLabel = (status) => {
  const labelMap = {
    'PENDING': '审批中',
    'COMPLETED': '已完成',
    'REJECTED': '已拒绝',
    'WITHDRAWN': '已撤回'
  }
  return labelMap[status] || status
}

const getBusinessTypeTag = (type) => {
  const tagMap = {
    'LEAVE': '',
    'EXPENSE': 'success',
    'PURCHASE': 'warning',
    'OTHER': 'info'
  }
  return tagMap[type] || 'info'
}

const getBusinessTypeLabel = (type) => {
  const labelMap = {
    'LEAVE': '请假申请',
    'EXPENSE': '费用报销',
    'PURCHASE': '采购申请',
    'OTHER': '其他'
  }
  return labelMap[type] || type
}

const getProgressColor = (progress) => {
  if (progress < 30) return '#f56c6c'
  if (progress < 70) return '#e6a23c'
  return '#67c23a'
}

const getHistoryColor = (action) => {
  const colorMap = {
    'START': '#409EFF',
    'APPROVED': '#67C23A',
    'REJECTED': '#F56C6C',
    'WITHDRAW': '#E6A23C',
    'COMPLETE': '#67C23A'
  }
  return colorMap[action] || '#909399'
}

// 组件挂载
onMounted(() => {
  loadMyInstances()
})
</script>

<style scoped>
.my-initiated-container {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 140px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-description {
  color: #6b7280;
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.total-card .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.pending-card .stat-icon {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
}

.completed-card .stat-icon {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.rejected-card .stat-icon {
  background: linear-gradient(135deg, #F56C6C 0%, #F78989 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.instance-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.instance-filters {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.instance-list {
  min-height: 400px;
}

.instance-item {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.instance-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.instance-content {
  padding: 20px;
}

.instance-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.instance-title h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.instance-badges {
  display: flex;
  gap: 8px;
}

.instance-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.instance-time, .instance-end-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.instance-body {
  margin-bottom: 16px;
}

.instance-description {
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 12px;
}

.instance-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-label {
  color: #6b7280;
}

.info-value {
  color: #1f2937;
  font-weight: 500;
}

.instance-progress {
  margin-bottom: 16px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 14px;
  color: #6b7280;
}

.progress-percent {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.instance-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.more-actions {
  margin-left: auto;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.instance-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin: 24px 0;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
}

.content-box {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  line-height: 1.6;
  color: #4b5563;
}

.current-status p {
  margin-bottom: 8px;
  line-height: 1.6;
}

.history-content {
  max-height: 500px;
  overflow-y: auto;
}

.history-item {
  padding: 8px 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-action {
  font-weight: 600;
  color: #1f2937;
}

.history-operator {
  color: #6b7280;
  font-size: 14px;
}

.history-comment {
  color: #4b5563;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-initiated-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-row {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .instance-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .instance-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .instance-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>