<template>
  <div class="attendance-statistics-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.attendanceStatistics') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportStatistics">
          <el-icon><Download /></el-icon>
          {{ $t('attendanceStatistics.exportStatistics') }}
        </el-button>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="statistics-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon success">
                <el-icon size="24"><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.normalCount }}</div>
                <div class="stat-label">{{ $t('attendanceStatistics.normalAttendance') }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon warning">
                <el-icon size="24"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.lateCount }}</div>
                <div class="stat-label">{{ $t('attendanceStatistics.lateCount') }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon danger">
                <el-icon size="24"><WarningFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.absentCount }}</div>
                <div class="stat-label">{{ $t('attendanceStatistics.absentCount') }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon info">
                <el-icon size="24"><DocumentRemove /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewData.leaveCount }}</div>
                <div class="stat-label">{{ $t('attendanceStatistics.leaveCount') }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item :label="$t('attendanceStatistics.statisticsRange') + ':'">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="monthrange"
            range-separator="至"
            start-placeholder="开始月份"
            end-placeholder="结束月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
          />
        </el-form-item>
        <el-form-item label="部门:">
          <el-select v-model="filterForm.departmentId" placeholder="请选择部门" clearable>
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStatistics">查询统计</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计表格 -->
    <el-card class="table-card">
      <el-table :data="statistics" v-loading="loading" stripe>
        <el-table-column prop="employeeName" label="员工姓名" width="120" />
        <el-table-column prop="employeeCode" label="工号" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="normalDays" label="正常出勤" width="100" align="center" />
        <el-table-column prop="lateDays" label="迟到次数" width="100" align="center" />
        <el-table-column prop="earlyDays" label="早退次数" width="100" align="center" />
        <el-table-column prop="absentDays" label="旷工次数" width="100" align="center" />
        <el-table-column prop="leaveDays" label="请假天数" width="100" align="center" />
        <el-table-column prop="workDays" label="应出勤" width="100" align="center" />
        <el-table-column prop="actualWorkDays" label="实际出勤" width="100" align="center" />
        <el-table-column prop="attendanceRate" label="出勤率" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 
              'high-rate': row.attendanceRate >= 95, 
              'medium-rate': row.attendanceRate >= 85 && row.attendanceRate < 95,
              'low-rate': row.attendanceRate < 85
            }">
              {{ row.attendanceRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetails(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadStatistics"
          @current-change="loadStatistics"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Download, CircleCheck, Clock, WarningFilled, DocumentRemove } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const statistics = ref([])
const departments = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 概览数据
const overviewData = reactive({
  normalCount: 1234,
  lateCount: 56,
  absentCount: 12,
  leaveCount: 89
})

// 筛选表单
const filterForm = reactive({
  dateRange: [],
  departmentId: ''
})

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    statistics.value = [
      {
        employeeName: '张三',
        employeeCode: 'EMP001',
        departmentName: '技术部',
        normalDays: 20,
        lateDays: 2,
        earlyDays: 1,
        absentDays: 0,
        leaveDays: 1,
        workDays: 22,
        actualWorkDays: 21,
        attendanceRate: 95.5
      },
      {
        employeeName: '李四',
        employeeCode: 'EMP002',
        departmentName: '市场部',
        normalDays: 18,
        lateDays: 3,
        earlyDays: 2,
        absentDays: 1,
        leaveDays: 2,
        workDays: 22,
        actualWorkDays: 19,
        attendanceRate: 86.4
      },
      {
        employeeName: '王五',
        employeeCode: 'EMP003',
        departmentName: '销售部',
        normalDays: 19,
        lateDays: 1,
        earlyDays: 0,
        absentDays: 0,
        leaveDays: 2,
        workDays: 22,
        actualWorkDays: 20,
        attendanceRate: 90.9
      }
    ]
    total.value = 156
  } finally {
    loading.value = false
  }
}

// 加载部门列表
const loadDepartments = async () => {
  departments.value = [
    { id: 1, name: '技术部' },
    { id: 2, name: '市场部' },
    { id: 3, name: '销售部' },
    { id: 4, name: '财务部' }
  ]
}

// 重置筛选
const resetFilter = () => {
  Object.assign(filterForm, {
    dateRange: [],
    departmentId: ''
  })
  loadStatistics()
}

// 查看详情
const viewDetails = (row) => {
  ElMessage.info(`查看 ${row.employeeName} 的考勤详情`)
}

// 导出统计
const exportStatistics = () => {
  ElMessage.success('导出功能开发中...')
}

// 组件挂载
onMounted(() => {
  loadStatistics()
  loadDepartments()
})
</script>

<style scoped>
.attendance-statistics-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.statistics-overview {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #f0c474);
}

.stat-icon.danger {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stat-icon.info {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.high-rate {
  color: #67c23a;
  font-weight: 600;
}

.medium-rate {
  color: #e6a23c;
  font-weight: 600;
}

.low-rate {
  color: #f56c6c;
  font-weight: 600;
}
</style>