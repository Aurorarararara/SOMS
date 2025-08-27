<template>
  <div class="reports-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">数据报表</h2>
      <el-button-group>
        <el-button type="primary" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </el-button-group>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="报表类型:">
          <el-select v-model="filterForm.reportType" placeholder="请选择报表类型" @change="changeReportType">
            <el-option label="考勤统计" value="attendance" />
            <el-option label="请假统计" value="leave" />
            <el-option label="部门统计" value="department" />
            <el-option label="员工统计" value="employee" />
          </el-select>
        </el-form-item>
        <el-form-item label="统计周期:">
          <el-select v-model="filterForm.period" placeholder="请选择周期" @change="loadReportData">
            <el-option label="本月" value="month" />
            <el-option label="本季度" value="quarter" />
            <el-option label="本年" value="year" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="filterForm.period === 'custom'" label="自定义时间:">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadReportData"
          />
        </el-form-item>
        <el-form-item v-if="filterForm.reportType === 'department'" label="选择部门:">
          <el-select v-model="filterForm.departmentId" placeholder="请选择部门" @change="loadReportData">
            <el-option label="全部部门" value="" />
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon primary-icon">
              <el-icon size="30"><User /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ overview.totalEmployees }}</div>
              <div class="overview-label">总员工数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon success-icon">
              <el-icon size="30"><CircleCheck /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ overview.attendanceRate }}%</div>
              <div class="overview-label">出勤率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon warning-icon">
              <el-icon size="30"><DocumentCopy /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ overview.leaveApplications }}</div>
              <div class="overview-label">请假申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon info-icon">
              <el-icon size="30"><OfficeBuilding /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ overview.departments }}</div>
              <div class="overview-label">部门数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">{{ getChartTitle('trend') }}</span>
            </div>
          </template>
          <div ref="trendChart" class="chart-container" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">{{ getChartTitle('pie') }}</span>
            </div>
          </template>
          <div ref="pieChart" class="chart-container" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="table-card">
      <template #header>
        <span class="table-title">{{ getTableTitle() }}</span>
      </template>
      <el-table :data="tableData" v-loading="tableLoading" stripe>
        <el-table-column 
          v-for="column in tableColumns" 
          :key="column.prop"
          :prop="column.prop" 
          :label="column.label" 
          :width="column.width"
          :min-width="column.minWidth"
        />
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTableData"
          @current-change="loadTableData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Download, Refresh, User, CircleCheck, DocumentCopy, OfficeBuilding } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 响应式数据
const chartLoading = ref(false)
const tableLoading = ref(false)
const departments = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 图表引用
const trendChart = ref(null)
const pieChart = ref(null)

// 筛选表单
const filterForm = reactive({
  reportType: 'attendance',
  period: 'month',
  dateRange: [],
  departmentId: ''
})

// 数据概览
const overview = reactive({
  totalEmployees: 156,
  attendanceRate: 95.2,
  leaveApplications: 28,
  departments: 12
})

// 表格数据
const tableData = ref([])
const tableColumns = ref([])

// 获取图表标题
const getChartTitle = (type) => {
  const titles = {
    attendance: {
      trend: '考勤趋势图',
      pie: '考勤状态分布'
    },
    leave: {
      trend: '请假趋势图',
      pie: '请假类型分布'
    },
    department: {
      trend: '部门人员变化',
      pie: '部门人员分布'
    },
    employee: {
      trend: '员工入职趋势',
      pie: '员工状态分布'
    }
  }
  return titles[filterForm.reportType]?.[type] || '数据图表'
}

// 获取表格标题
const getTableTitle = () => {
  const titles = {
    attendance: '详细考勤数据',
    leave: '详细请假数据',
    department: '详细部门数据',
    employee: '详细员工数据'
  }
  return titles[filterForm.reportType] || '详细数据'
}

// 切换报表类型
const changeReportType = () => {
  loadReportData()
  loadTableData()
}

// 加载报表数据
const loadReportData = async () => {
  chartLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 根据不同报表类型加载不同图表
    await loadTrendChart()
    await loadPieChart()
  } finally {
    chartLoading.value = false
  }
}

// 加载趋势图
const loadTrendChart = () => {
  nextTick(() => {
    if (trendChart.value) {
      const chart = echarts.init(trendChart.value)
      
      let option = {}
      
      if (filterForm.reportType === 'attendance') {
        option = {
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['出勤率', '迟到率', '缺勤率']
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: ['第1周', '第2周', '第3周', '第4周']
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: '{value}%'
            }
          },
          series: [
            {
              name: '出勤率',
              type: 'line',
              data: [95.2, 96.1, 94.8, 95.5],
              itemStyle: { color: '#67C23A' }
            },
            {
              name: '迟到率',
              type: 'line',
              data: [3.2, 2.8, 3.5, 3.1],
              itemStyle: { color: '#E6A23C' }
            },
            {
              name: '缺勤率',
              type: 'line',
              data: [1.6, 1.1, 1.7, 1.4],
              itemStyle: { color: '#F56C6C' }
            }
          ]
        }
      } else if (filterForm.reportType === 'leave') {
        option = {
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['年假', '病假', '事假']
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: ['第1周', '第2周', '第3周', '第4周']
          },
          yAxis: {
            type: 'value'
          },
          series: [
            {
              name: '年假',
              type: 'bar',
              data: [5, 8, 6, 9],
              itemStyle: { color: '#409EFF' }
            },
            {
              name: '病假',
              type: 'bar',
              data: [3, 2, 4, 3],
              itemStyle: { color: '#E6A23C' }
            },
            {
              name: '事假',
              type: 'bar',
              data: [2, 3, 1, 2],
              itemStyle: { color: '#909399' }
            }
          ]
        }
      }
      
      chart.setOption(option)
      
      // 响应式调整
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }
  })
}

// 加载饼图
const loadPieChart = () => {
  nextTick(() => {
    if (pieChart.value) {
      const chart = echarts.init(pieChart.value)
      
      let option = {}
      
      if (filterForm.reportType === 'attendance') {
        option = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [
            {
              name: '考勤状态',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['60%', '50%'],
              data: [
                { value: 142, name: '正常出勤', itemStyle: { color: '#67C23A' } },
                { value: 8, name: '迟到', itemStyle: { color: '#E6A23C' } },
                { value: 3, name: '早退', itemStyle: { color: '#409EFF' } },
                { value: 3, name: '缺勤', itemStyle: { color: '#F56C6C' } }
              ]
            }
          ]
        }
      } else if (filterForm.reportType === 'department') {
        option = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c}人 ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [
            {
              name: '部门人员',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['60%', '50%'],
              data: [
                { value: 35, name: '技术部' },
                { value: 28, name: '市场部' },
                { value: 25, name: '销售部' },
                { value: 20, name: '财务部' },
                { value: 18, name: '人事部' },
                { value: 15, name: '行政部' },
                { value: 15, name: '其他部门' }
              ]
            }
          ]
        }
      }
      
      chart.setOption(option)
      
      // 响应式调整
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }
  })
}

// 加载表格数据
const loadTableData = async () => {
  tableLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    
    if (filterForm.reportType === 'attendance') {
      tableColumns.value = [
        { prop: 'employeeName', label: '员工姓名', width: 100 },
        { prop: 'departmentName', label: '部门', width: 120 },
        { prop: 'attendanceDays', label: '出勤天数', width: 100 },
        { prop: 'lateDays', label: '迟到次数', width: 100 },
        { prop: 'absentDays', label: '缺勤天数', width: 100 },
        { prop: 'attendanceRate', label: '出勤率', width: 100 }
      ]
      
      tableData.value = [
        {
          employeeName: '张三',
          departmentName: '技术部',
          attendanceDays: 22,
          lateDays: 1,
          absentDays: 0,
          attendanceRate: '95.6%'
        },
        {
          employeeName: '李四',
          departmentName: '市场部',
          attendanceDays: 21,
          lateDays: 2,
          absentDays: 1,
          attendanceRate: '91.3%'
        }
      ]
    } else if (filterForm.reportType === 'leave') {
      tableColumns.value = [
        { prop: 'employeeName', label: '员工姓名', width: 100 },
        { prop: 'departmentName', label: '部门', width: 120 },
        { prop: 'leaveType', label: '请假类型', width: 100 },
        { prop: 'leaveDays', label: '请假天数', width: 100 },
        { prop: 'leaveReason', label: '请假原因', minWidth: 200 },
        { prop: 'applyTime', label: '申请时间', width: 150 }
      ]
      
      tableData.value = [
        {
          employeeName: '王五',
          departmentName: '销售部',
          leaveType: '年假',
          leaveDays: 3,
          leaveReason: '回家看望父母',
          applyTime: '2025-08-20'
        }
      ]
    }
    
    total.value = 156
  } finally {
    tableLoading.value = false
  }
}

// 加载部门列表
const loadDepartments = () => {
  departments.value = [
    { id: 1, name: '技术部' },
    { id: 2, name: '市场部' },
    { id: 3, name: '销售部' },
    { id: 4, name: '财务部' },
    { id: 5, name: '人事部' },
    { id: 6, name: '行政部' }
  ]
}

// 刷新数据
const refreshData = () => {
  loadReportData()
  loadTableData()
  ElMessage.success('数据已刷新')
}

// 导出报表
const exportReport = () => {
  ElMessage.info('导出功能开发中...')
}

// 组件挂载
onMounted(() => {
  loadDepartments()
  loadReportData()
  loadTableData()
})
</script>

<style scoped>
.reports-container {
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

.filter-card {
  margin-bottom: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  border-radius: 8px;
}

.overview-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.overview-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
}

.primary-icon { background-color: #409EFF; }
.success-icon { background-color: #67C23A; }
.warning-icon { background-color: #E6A23C; }
.info-icon { background-color: #909399; }

.overview-info {
  flex: 1;
}

.overview-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  color: #666;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  height: 350px;
}

.table-card {
  border-radius: 8px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reports-container {
    padding: 10px;
  }
  
  .overview-value {
    font-size: 20px;
  }
  
  .chart-container {
    height: 280px;
  }
}
</style>