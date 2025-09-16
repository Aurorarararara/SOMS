<template>
  <div class="expense-approval">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><DocumentChecked /></el-icon>
          报销审批管理
        </h1>
        <p class="page-description">审批员工报销申请，查看申请详情和审批历史</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="stat-card pending">
            <div class="stat-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.pendingCount }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="stat-card approved">
            <div class="stat-icon">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.approvedCount }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="stat-card rejected">
            <div class="stat-icon">
              <el-icon><Close /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ statistics.rejectedCount }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="stat-card total">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">¥{{ formatAmount(statistics.totalAmount) }}</div>
              <div class="stat-label">总金额</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :model="filters" inline class="filter-form">
        <el-form-item label="申请状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="报销类型">
          <el-select v-model="filters.type" placeholder="全部类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="差旅费" value="TRAVEL" />
            <el-option label="餐费" value="MEAL" />
            <el-option label="交通费" value="TRANSPORT" />
            <el-option label="办公用品" value="OFFICE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请人">
          <el-input
            v-model="filters.applicantName"
            placeholder="输入申请人姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
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
    </div>

    <!-- 申请列表 -->
    <div class="application-list">
      <el-table
        v-loading="loading"
        :data="applications"
        stripe
        class="application-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="applicationNo" label="申请编号" width="150" />
        <el-table-column prop="title" label="申请标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="type" label="报销类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="urgency" label="紧急程度" width="100">
          <template #default="{ row }">
            <el-tag :type="getUrgencyTagType(row.urgency)">{{ getUrgencyText(row.urgency) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="success"
              @click="approveApplication(row)"
            >
              <el-icon><Check /></el-icon>
              审批
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedApplications.length > 0" class="batch-actions">
        <el-alert
          :title="`已选择 ${selectedApplications.length} 项`"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <div class="batch-buttons">
              <el-button size="small" type="success" @click="batchApprove">
                <el-icon><Check /></el-icon>
                批量通过
              </el-button>
              <el-button size="small" type="danger" @click="batchReject">
                <el-icon><Close /></el-icon>
                批量拒绝
              </el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 申请详情对话框 -->
    <ApprovalDetailDialog
      v-model="showDetailDialog"
      :application-data="selectedApplication"
      @approved="handleApprovalSuccess"
      @rejected="handleApprovalSuccess"
    />

    <!-- 审批对话框 -->
    <ApprovalDialog
      v-model="showApprovalDialog"
      :application-data="selectedApplication"
      @success="handleApprovalSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentChecked,
  Clock,
  Check,
  Close,
  Money,
  Search,
  Refresh,
  View
} from '@element-plus/icons-vue'
import expenseApi from '@/api/expense'
import ApprovalDetailDialog from './components/ApprovalDetailDialog.vue'
import ApprovalDialog from './components/ApprovalDialog.vue'

// 响应式数据
const loading = ref(false)
const applications = ref([])
const selectedApplications = ref([])
const selectedApplication = ref(null)
const showDetailDialog = ref(false)
const showApprovalDialog = ref(false)

// 统计数据
const statistics = reactive({
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  totalAmount: 0
})

// 筛选条件
const filters = reactive({
  status: '',
  type: '',
  applicantName: '',
  dateRange: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const searchParams = computed(() => {
  const params = {
    page: pagination.page,
    size: pagination.size,
    status: filters.status,
    type: filters.type,
    applicantName: filters.applicantName
  }
  
  if (filters.dateRange && filters.dateRange.length === 2) {
    params.startDate = filters.dateRange[0]
    params.endDate = filters.dateRange[1]
  }
  
  return params
})

// 生命周期
onMounted(() => {
  loadApplications()
  loadStatistics()
})

// 方法
const loadApplications = async () => {
  loading.value = true
  try {
    const response = await expenseApi.getApplicationsForApproval(searchParams.value)
    applications.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await expenseApi.getApprovalStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadApplications()
}

const handleReset = () => {
  Object.assign(filters, {
    status: '',
    type: '',
    applicantName: '',
    dateRange: null
  })
  pagination.page = 1
  loadApplications()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadApplications()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadApplications()
}

const handleSelectionChange = (selection) => {
  selectedApplications.value = selection
}

const viewDetail = (application) => {
  selectedApplication.value = application
  showDetailDialog.value = true
}

const approveApplication = (application) => {
  selectedApplication.value = application
  showApprovalDialog.value = true
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量通过选中的 ${selectedApplications.value.length} 个申请吗？`,
      '批量审批',
      { type: 'warning' }
    )
    
    const applicationIds = selectedApplications.value.map(app => app.id)
    await expenseApi.batchApproveApplications({
      applicationIds,
      comment: '批量审批通过'
    })
    
    ElMessage.success('批量审批成功')
    loadApplications()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审批失败')
    }
  }
}

const batchReject = async () => {
  try {
    const { value: comment } = await ElMessageBox.prompt(
      `确定要批量拒绝选中的 ${selectedApplications.value.length} 个申请吗？请输入拒绝理由：`,
      '批量审批',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入拒绝理由'
      }
    )
    
    const applicationIds = selectedApplications.value.map(app => app.id)
    await expenseApi.batchRejectApplications({
      applicationIds,
      comment
    })
    
    ElMessage.success('批量审批成功')
    loadApplications()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审批失败')
    }
  }
}

const handleApprovalSuccess = () => {
  loadApplications()
  loadStatistics()
}

// 工具方法
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'WITHDRAWN': '已撤回'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'WITHDRAWN': 'info'
  }
  return typeMap[status] || 'info'
}

const getTypeText = (type) => {
  const typeMap = {
    'TRAVEL': '差旅费',
    'MEAL': '餐费',
    'TRANSPORT': '交通费',
    'OFFICE': '办公用品',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    'TRAVEL': 'primary',
    'MEAL': 'success',
    'TRANSPORT': 'warning',
    'OFFICE': 'info',
    'OTHER': 'default'
  }
  return typeMap[type] || 'default'
}

const getUrgencyText = (urgency) => {
  const urgencyMap = {
    'LOW': '普通',
    'MEDIUM': '紧急',
    'HIGH': '非常紧急'
  }
  return urgencyMap[urgency] || urgency
}

const getUrgencyTagType = (urgency) => {
  const typeMap = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return typeMap[urgency] || 'info'
}

const formatAmount = (amount) => {
  return Number(amount || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.expense-approval {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-description {
  color: #606266;
  margin: 0;
  font-size: 14px;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card.pending .stat-icon {
  background: #e6a23c;
}

.stat-card.approved .stat-icon {
  background: #67c23a;
}

.stat-card.rejected .stat-icon {
  background: #f56c6c;
}

.stat-card.total .stat-icon {
  background: #409eff;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.filter-form {
  margin: 0;
}

.application-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.application-table {
  width: 100%;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.batch-actions {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.batch-buttons {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .expense-approval {
    padding: 12px;
  }
  
  .header-content {
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .filter-bar {
    padding: 16px;
  }
  
  .filter-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>