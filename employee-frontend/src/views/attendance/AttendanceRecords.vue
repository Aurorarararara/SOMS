<template>
  <div class="attendance-records">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Clock /></el-icon>
          考勤记录
        </h1>
        <p class="page-subtitle">查看个人考勤历史记录</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="exportRecords">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <!-- 筛选条件 -->
      <el-card class="filter-card">
        <div class="filter-form">
          <el-form :model="filterForm" :inline="true" size="default">
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 280px"
                @change="handleDateChange"
              />
            </el-form-item>
            <el-form-item label="考勤状态">
              <el-select v-model="filterForm.status" placeholder="选择状态" style="width: 140px" @change="loadRecords">
                <el-option label="全部" value="" />
                <el-option label="正常" value="normal" />
                <el-option label="迟到" value="late" />
                <el-option label="早退" value="early" />
                <el-option label="缺勤" value="absent" />
                <el-option label="请假" value="leave" />
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
            <div class="stat-number">{{ statistics.normalDays }}</div>
            <div class="stat-label">正常天数</div>
          </div>
          <div class="stat-icon normal">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.lateDays }}</div>
            <div class="stat-label">迟到次数</div>
          </div>
          <div class="stat-icon late">
            <el-icon><Clock /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.earlyDays }}</div>
            <div class="stat-label">早退次数</div>
          </div>
          <div class="stat-icon early">
            <el-icon><Timer /></el-icon>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ statistics.absentDays }}</div>
            <div class="stat-label">缺勤天数</div>
          </div>
          <div class="stat-icon absent">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
      </div>

      <!-- 考勤记录表格 -->
      <el-card class="table-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">考勤记录</span>
            <span class="record-count">共 {{ total }} 条记录</span>
          </div>
        </template>

        <el-table
          :data="records"
          v-loading="loading"
          style="width: 100%"
          :header-cell-style="{ background: '#f8f9fa', color: '#495057' }"
        >
          <el-table-column prop="date" label="日期" width="120" sortable>
            <template #default="scope">
              <span>{{ formatDate(scope.row.date) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="workDay" label="星期" width="80">
            <template #default="scope">
              <span>{{ getWeekDay(scope.row.date) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="checkInTime" label="上班打卡" width="120">
            <template #default="scope">
              <span v-if="scope.row.checkInTime" :class="getTimeClass(scope.row, 'in')">
                {{ scope.row.checkInTime }}
              </span>
              <span v-else class="no-record">未打卡</span>
            </template>
          </el-table-column>
          <el-table-column prop="checkOutTime" label="下班打卡" width="120">
            <template #default="scope">
              <span v-if="scope.row.checkOutTime" :class="getTimeClass(scope.row, 'out')">
                {{ scope.row.checkOutTime }}
              </span>
              <span v-else class="no-record">未打卡</span>
            </template>
          </el-table-column>
          <el-table-column prop="workHours" label="工作时长" width="100">
            <template #default="scope">
              <span v-if="scope.row.workHours">{{ scope.row.workHours }}小时</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="考勤状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="打卡地点" min-width="150">
            <template #default="scope">
              <div v-if="scope.row.checkInLocation || scope.row.checkOutLocation">
                <div v-if="scope.row.checkInLocation" class="location-item">
                  <el-icon class="location-icon"><MapLocation /></el-icon>
                  上班：{{ scope.row.checkInLocation }}
                </div>
                <div v-if="scope.row.checkOutLocation" class="location-item">
                  <el-icon class="location-icon"><MapLocation /></el-icon>
                  下班：{{ scope.row.checkOutLocation }}
                </div>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120">
            <template #default="scope">
              <span v-if="scope.row.remark">{{ scope.row.remark }}</span>
              <span v-else>-</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Clock, Download, Search, Refresh, Check, Timer, Warning, MapLocation
} from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/attendance'

// 响应式数据
const loading = ref(false)
const records = ref([])
const total = ref(0)

// 筛选表单
const filterForm = reactive({
  dateRange: [],
  status: ''
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20
})

// 统计数据
const statistics = ref({
  normalDays: 0,
  lateDays: 0,
  earlyDays: 0,
  absentDays: 0
})

// 计算属性
const dateRangeText = computed(() => {
  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    return `${filterForm.dateRange[0]} 至 ${filterForm.dateRange[1]}`
  }
  return '最近30天'
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
    if (filterForm.status) {
      params.status = filterForm.status
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    
    const response = await attendanceApi.getRecords(params)
    if (response.data) {
      records.value = response.data.records || []
      total.value = response.data.total || 0
    }
    
    // 加载统计数据
    await loadStatistics()
    
  } catch (error) {
    console.error('加载考勤记录失败:', error)
    ElMessage.error('加载考勤记录失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const now = new Date()
    const response = await attendanceApi.getMonthlyStats(now.getFullYear(), now.getMonth() + 1)
    if (response.data) {
      statistics.value = {
        normalDays: response.data.normalDays || 0,
        lateDays: response.data.lateDays || 0,
        earlyDays: response.data.earlyDays || 0,
        absentDays: response.data.absentDays || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}



const handleDateChange = () => {
  loadRecords()
}

const resetFilter = () => {
  filterForm.dateRange = []
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

const exportRecords = () => {
  ElMessage.success('导出功能开发中...')
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

const getWeekDay = (dateStr) => {
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const date = new Date(dateStr)
  return weekDays[date.getDay()]
}

const getTimeClass = (record, type) => {
  if (type === 'in' && record.status === 'late') return 'late-time'
  if (type === 'out' && record.status === 'early') return 'early-time'
  return 'normal-time'
}

const getStatusType = (status) => {
  const typeMap = {
    normal: 'success',
    late: 'warning',
    early: 'warning',
    absent: 'danger',
    leave: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    normal: '正常',
    late: '迟到',
    early: '早退',
    absent: '缺勤',
    leave: '请假'
  }
  return textMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  // 设置默认日期范围为最近30天
  const today = new Date()
  const thirtyDaysAgo = new Date(today)
  thirtyDaysAgo.setDate(today.getDate() - 30)
  
  filterForm.dateRange = [
    thirtyDaysAgo.toISOString().split('T')[0],
    today.toISOString().split('T')[0]
  ]
  
  loadRecords()
})
</script>

<style scoped>
.attendance-records {
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

.stat-icon.normal {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.late {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-icon.early {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.stat-icon.absent {
  background: linear-gradient(135deg, #ef4444, #dc2626);
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

.normal-time {
  color: #10b981;
  font-weight: 500;
}

.late-time {
  color: #f59e0b;
  font-weight: 500;
}

.early-time {
  color: #8b5cf6;
  font-weight: 500;
}

.no-record {
  color: #999;
  font-style: italic;
}

.location-item {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
  font-size: 12px;
  color: #666;
}

.location-icon {
  font-size: 12px;
  color: #999;
}

.pagination-container {
  margin-top: 24px;
  text-align: right;
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
  .attendance-records {
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
}
</style>