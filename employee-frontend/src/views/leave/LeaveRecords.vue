<template>
  <div class="leave-records">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Calendar /></el-icon>
          请假记录
        </h1>
        <p class="page-subtitle">查看个人请假申请历史记录</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/leave')">
          <el-icon><Plus /></el-icon>
          新建请假
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <!-- 筛选条件 -->
      <el-card class="filter-card">
        <div class="filter-form">
          <el-form :model="filterForm" :inline="true" size="default">
            <el-form-item label="申请时间">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 280px"
                @change="loadRecords"
              />
            </el-form-item>
            <el-form-item label="请假类型">
              <el-select v-model="filterForm.type" placeholder="选择类型" style="width: 140px" @change="loadRecords">
                <el-option label="全部" value="" />
                <el-option label="病假" value="sick" />
                <el-option label="事假" value="personal" />
                <el-option label="年假" value="annual" />
                <el-option label="调休" value="compensatory" />
                <el-option label="婚假" value="marriage" />
                <el-option label="产假" value="maternity" />
              </el-select>
            </el-form-item>
            <el-form-item label="审批状态">
              <el-select v-model="filterForm.status" placeholder="选择状态" style="width: 140px" @change="loadRecords">
                <el-option label="全部" value="" />
                <el-option label="待审批" value="pending" />
                <el-option label="已通过" value="approved" />
                <el-option label="已拒绝" value="rejected" />
                <el-option label="已撤回" value="cancelled" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRecords">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetFilter">
                <el-icon><Refresh /></el-icon>
                重置
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
            <div class="stat-label">累计请假天数</div>
          </div>
          <div class="stat-icon total">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.approvedCount }}</div>
            <div class="stat-label">已通过申请</div>
          </div>
          <div class="stat-icon approved">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.pendingCount }}</div>
            <div class="stat-label">待审批申请</div>
          </div>
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.remainingDays }}</div>
            <div class="stat-label">剩余年假天数</div>
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
            <span class="card-title">请假记录</span>
            <span class="record-count">共 {{ total }} 条记录</span>
          </div>
        </template>

        <el-table
          :data="records"
          v-loading="loading"
          style="width: 100%"
          :header-cell-style="{ background: '#f8f9fa', color: '#495057' }"
        >
          <el-table-column prop="type" label="请假类型" width="100">
            <template #default="scope">
              <el-tag :type="getTypeTagType(scope.row.type)" size="small">
                {{ getTypeText(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="120" sortable />
          <el-table-column prop="endDate" label="结束日期" width="120" sortable />
          <el-table-column prop="days" label="请假天数" width="100" align="center">
            <template #default="scope">
              <span class="days-text">{{ scope.row.days }}天</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="请假原因" min-width="200">
            <template #default="scope">
              <div class="reason-text">{{ scope.row.reason }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="审批状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="approver" label="审批人" width="120">
            <template #default="scope">
              <span v-if="scope.row.approver">{{ scope.row.approver }}</span>
              <span v-else class="no-data">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="approveTime" label="审批时间" width="120">
            <template #default="scope">
              <span v-if="scope.row.approveTime">{{ formatDate(scope.row.approveTime) }}</span>
              <span v-else class="no-data">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="120" sortable>
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="viewDetail(scope.row)"
                link
              >
                查看
              </el-button>
              <el-button 
                v-if="scope.row.status === 'pending'"
                type="danger" 
                size="small" 
                @click="cancelApplication(scope.row)"
                link
              >
                撤回
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
      title="请假申请详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentRecord" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">请假类型：</span>
          <el-tag :type="getTypeTagType(currentRecord.type)">
            {{ getTypeText(currentRecord.type) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">请假时间：</span>
          <span>{{ currentRecord.startDate }} 至 {{ currentRecord.endDate }} ({{ currentRecord.days }}天)</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">请假原因：</span>
          <div class="reason-detail">{{ currentRecord.reason }}</div>
        </div>
        <div class="detail-row">
          <span class="detail-label">申请时间：</span>
          <span>{{ formatDateTime(currentRecord.createTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">审批状态：</span>
          <el-tag :type="getStatusTagType(currentRecord.status)">
            {{ getStatusText(currentRecord.status) }}
          </el-tag>
        </div>
        <div v-if="currentRecord.approver" class="detail-row">
          <span class="detail-label">审批人：</span>
          <span>{{ currentRecord.approver }}</span>
        </div>
        <div v-if="currentRecord.approveTime" class="detail-row">
          <span class="detail-label">审批时间：</span>
          <span>{{ formatDateTime(currentRecord.approveTime) }}</span>
        </div>
        <div v-if="currentRecord.approveRemark" class="detail-row">
          <span class="detail-label">审批意见：</span>
          <div class="remark-detail">{{ currentRecord.approveRemark }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, Plus, Search, Refresh, Check, Clock, Sunny
} from '@element-plus/icons-vue'
import { leaveApi } from '@/api/leave'

const router = useRouter()

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
    ElMessage.error('加载请假记录失败')
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
    await ElMessageBox.confirm('确定要撤回这个请假申请吗？', '确认撤回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    record.status = 'cancelled'
    ElMessage.success('撤回成功')
    loadRecords()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败')
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
    sick: '病假',
    personal: '事假',
    annual: '年假',
    compensatory: '调休',
    marriage: '婚假',
    maternity: '产假'
  }
  return textMap[type] || '其他'
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
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝',
    cancelled: '已撤回'
  }
  return textMap[status] || '未知'
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