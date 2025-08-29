<template>
  <div class="leave-records">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Calendar /></el-icon>
          {{ $t('nav.leaveRecords') }}
        </h1>
        <p class="page-subtitle">{{ $t('leaveRecords.subtitle') }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/leave')">
          <el-icon><Plus /></el-icon>
          {{ $t('leaveRecords.newLeave') }}
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <!-- 筛选条件 -->
      <el-card class="filter-card">
        <div class="filter-form">
          <el-form :model="filterForm" :inline="true" size="default">
            <el-form-item :label="$t('leaveRecords.applicationTime')">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                :range-separator="$t('common.to')"
                :start-placeholder="$t('leaveRecords.startDate')"
                :end-placeholder="$t('leaveRecords.endDate')"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 280px"
                @change="loadRecords"
              />
            </el-form-item>
            <el-form-item :label="$t('leaveRecords.leaveType')">
              <el-select v-model="filterForm.type" :placeholder="$t('leaveRecords.selectType')" style="width: 140px" @change="loadRecords">
                <el-option :label="$t('common.all')" value="" />
                <el-option :label="$t('leaveRecords.sickLeave')" value="sick" />
                <el-option :label="$t('leaveRecords.personalLeave')" value="personal" />
                <el-option :label="$t('leaveRecords.annualLeave')" value="annual" />
                <el-option :label="$t('leaveRecords.compensatoryLeave')" value="compensatory" />
                <el-option :label="$t('leaveRecords.marriageLeave')" value="marriage" />
                <el-option :label="$t('leaveRecords.maternityLeave')" value="maternity" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('leaveRecords.approvalStatus')">
              <el-select v-model="filterForm.status" :placeholder="$t('leaveRecords.selectStatus')" style="width: 140px" @change="loadRecords">
                <el-option :label="$t('common.all')" value="" />
                <el-option :label="$t('leaveRecords.pending')" value="pending" />
                <el-option :label="$t('leaveRecords.approved')" value="approved" />
                <el-option :label="$t('leaveRecords.rejected')" value="rejected" />
                <el-option :label="$t('leaveRecords.cancelled')" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRecords">
                <el-icon><Search /></el-icon>
                {{ $t('common.search') }}
              </el-button>
              <el-button @click="resetFilter">
                <el-icon><Refresh /></el-icon>
                {{ $t('common.reset') }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.totalDays }}</div>
            <div class="stat-label">{{ $t('leaveRecords.totalLeaveDays') }}</div>
          </div>
          <div class="stat-icon total">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.approvedCount }}</div>
            <div class="stat-label">{{ $t('leaveRecords.approvedApplications') }}</div>
          </div>
          <div class="stat-icon approved">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.pendingCount }}</div>
            <div class="stat-label">{{ $t('leaveRecords.pendingApplications') }}</div>
          </div>
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.remainingDays }}</div>
            <div class="stat-label">{{ $t('leaveRecords.remainingAnnualLeave') }}</div>
          </div>
          <div class="stat-icon remaining">
            <el-icon><Sunny /></el-icon>
          </div>
        </div>
      </div>

      <!-- 请假记录表格 -->
      <el-card class="table-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">{{ $t('leaveRecords.leaveRecords') }}</span>
            <span class="record-count">{{ $t('leaveRecords.totalRecords', { count: total }) }}</span>
          </div>
        </template>

        <el-table
          :data="records"
          v-loading="loading"
          style="width: 100%"
          :header-cell-style="{ background: '#f8f9fa', color: '#495057' }"
        >
          <el-table-column prop="type" :label="$t('leaveRecords.leaveType')" width="100">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)" size="small">
                {{ getTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" :label="$t('leaveRecords.startDate')" width="120" sortable />
          <el-table-column prop="endDate" :label="$t('leaveRecords.endDate')" width="120" sortable />
          <el-table-column prop="days" :label="$t('leaveRecords.leaveDays')" width="100" align="center">
            <template #default="scope">
              <span class="days-text">{{ scope.row.days }}{{ $t('leaveRecords.days') }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" :label="$t('leaveRecords.leaveReason')" min-width="200">
            <template #default="scope">
              <div class="reason-text">{{ scope.row.reason }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('leaveRecords.approvalStatus')" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="approver" :label="$t('leaveRecords.approver')" width="120">
            <template #default="scope">
              <span v-if="scope.row.approver">{{ scope.row.approver }}</span>
              <span v-else class="no-data">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="approveTime" :label="$t('leaveRecords.approveTime')" width="120">
            <template #default="scope">
              <span v-if="scope.row.approveTime">{{ formatDate(scope.row.approveTime) }}</span>
              <span v-else class="no-data">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" :label="$t('leaveRecords.applicationTime')" width="120" sortable>
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column :label="$t('common.actions')" width="120" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewDetail(scope.row)"
                link
              >
                {{ $t('common.view') }}
              </el-button>
              <el-button
                v-if="scope.row.status === 'pending'"
                type="danger"
                size="small"
                @click="cancelApplication(scope.row)"
                link
              >
                {{ $t('leaveRecords.withdraw') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="$t('leaveRecords.leaveApplicationDetails')"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentRecord" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.leaveType') }}：</span>
          <el-tag :type="getTypeTagType(currentRecord.type)">
            {{ getTypeText(currentRecord.type) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.leaveTime') }}：</span>
          <span>{{ currentRecord.startDate }} {{ $t('common.to') }} {{ currentRecord.endDate }} ({{ currentRecord.days }}{{ $t('leaveRecords.days') }})</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.leaveReason') }}：</span>
          <div class="reason-detail">{{ currentRecord.reason }}</div>
        </div>
        <div class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.applicationTime') }}：</span>
          <span>{{ formatDateTime(currentRecord.createTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.approvalStatus') }}：</span>
          <el-tag :type="getStatusTagType(currentRecord.status)">
            {{ getStatusText(currentRecord.status) }}
          </el-tag>
        </div>
        <div v-if="currentRecord.approver" class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.approver') }}：</span>
          <span>{{ currentRecord.approver }}</span>
        </div>
        <div v-if="currentRecord.approveTime" class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.approveTime') }}：</span>
          <span>{{ formatDateTime(currentRecord.approveTime) }}</span>
        </div>
        <div v-if="currentRecord.approveRemark" class="detail-row">
          <span class="detail-label">{{ $t('leaveRecords.approvalComment') }}：</span>
          <div class="remark-detail">{{ currentRecord.approveRemark }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, Plus, Search, Refresh, Check, Clock, Sunny
} from '@element-plus/icons-vue'
import { leaveApi } from '@/api/leave'

const router = useRouter()
const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const records = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentRecord = ref(null)

// 筛选表单
const filterForm = reactive({
  dateRange: [],
  type: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20
})

// 统计数据
const statistics = ref({
  totalDays: 0,
  approvedCount: 0,
  pendingCount: 0,
  remainingDays: 0
})

// 方法
const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    
    // 添加筛选条件
    if (filterForm.type) {
      params.leaveType = filterForm.type
    }
    if (filterForm.status) {
      params.status = filterForm.status
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    
    const response = await leaveApi.getLeaveRecords(params)
    if (response.data) {
      records.value = response.data.records || []
      total.value = response.data.total || 0
    }
    
    // 加载统计数据
    await loadStatistics()
    
  } catch (error) {
    console.error('加载请假记录失败:', error)
    ElMessage.error($t('leaveRecords.loadRecordsFailed'))
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const currentYear = new Date().getFullYear()
    const response = await leaveApi.getYearlyStatistics(currentYear)
    if (response.data) {
      statistics.value = {
        totalDays: response.data.totalDays || 0,
        approvedCount: response.data.approvedCount || 0,
        pendingCount: response.data.pendingCount || 0,
        remainingDays: response.data.remainingDays || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}



const getReasonByType = (type) => {
  const reasons = {
    sick: '身体不适，需要休息',
    personal: '家里有事需要处理',
    annual: '年假休息',
    compensatory: '加班调休',
    marriage: '结婚事宜',
    maternity: '产假休息'
  }
  return reasons[type] || '其他原因'
}

const resetFilter = () => {
  filterForm.dateRange = []
  filterForm.type = ''
  filterForm.status = ''
  loadRecords()
}

const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize
  pagination.currentPage = 1
  loadRecords()
}

const handleCurrentChange = (newPage) => {
  pagination.currentPage = newPage
  loadRecords()
}

const viewDetail = (record) => {
  currentRecord.value = record
  detailVisible.value = true
}

const cancelApplication = async (record) => {
  try {
    await ElMessageBox.confirm($t('leaveRecords.confirmWithdraw'), $t('leaveRecords.confirmWithdrawTitle'), {
      confirmButtonText: $t('common.confirm'),
      cancelButtonText: $t('common.cancel'),
      type: 'warning'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    record.status = 'cancelled'
    ElMessage.success($t('leaveRecords.withdrawSuccess'))
    loadRecords()

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error($t('leaveRecords.withdrawFailed'))
    }
  }
}

const getTypeTagType = (type) => {
  const typeMap = {
    sick: 'warning',
    personal: 'info',
    annual: 'success',
    compensatory: 'primary',
    marriage: 'danger',
    maternity: 'warning'
  }
  return typeMap[type] || 'info'
}

const getTypeText = (type) => {
  const textMap = {
    sick: $t('leaveRecords.sickLeave'),
    personal: $t('leaveRecords.personalLeave'),
    annual: $t('leaveRecords.annualLeave'),
    compensatory: $t('leaveRecords.compensatoryLeave'),
    marriage: $t('leaveRecords.marriageLeave'),
    maternity: $t('leaveRecords.maternityLeave')
  }
  return textMap[type] || $t('leaveRecords.other')
}

const getStatusTagType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: $t('leaveRecords.pending'),
    approved: $t('leaveRecords.approved'),
    rejected: $t('leaveRecords.rejected'),
    cancelled: $t('leaveRecords.cancelled')
  }
  return textMap[status] || $t('leaveRecords.unknown')
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.leave-records {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 8px;
  border-radius: 8px;
}

.page-subtitle {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.filter-form {
  padding: 8px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon.approved {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-icon.remaining {
  background: linear-gradient(135deg, #06b6d4, #0891b2);
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.record-count {
  font-size: 14px;
  color: #666;
}

.days-text {
  font-weight: 600;
  color: #667eea;
}

.reason-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-data {
  color: #999;
  font-style: italic;
}

.pagination-container {
  margin-top: 24px;
  text-align: right;
}

.detail-content {
  padding: 16px 0;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

.detail-label {
  min-width: 100px;
  font-weight: 600;
  color: #333;
}

.reason-detail,
.remark-detail {
  flex: 1;
  color: #666;
  line-height: 1.5;
  white-space: pre-wrap;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-table .el-table__cell) {
  padding: 12px 0;
}

:deep(.el-date-editor.el-input) {
  border-radius: 8px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .leave-records {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  .filter-form :deep(.el-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  
  .filter-form :deep(.el-date-picker) {
    width: 100% !important;
  }
  
  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-label {
    margin-bottom: 4px;
  }
}
</style>