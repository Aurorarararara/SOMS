<template>
  <div class="expense-statistics">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><DataAnalysis /></el-icon>
          报销统计分析
        </h2>
        <p class="page-description">查看报销申请的统计数据和趋势分析</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="exportStatistics">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 时间筛选 -->
    <div class="filter-section">
      <el-card shadow="never" class="filter-card">
        <div class="filter-content">
          <div class="filter-item">
            <label class="filter-label">统计周期：</label>
            <el-radio-group v-model="filterForm.period" @change="handlePeriodChange">
              <el-radio-button value="week">本周</el-radio-button>
              <el-radio-button value="month">本月</el-radio-button>
              <el-radio-button value="quarter">本季度</el-radio-button>
              <el-radio-button value="year">本年</el-radio-button>
              <el-radio-button value="custom">自定义</el-radio-button>
            </el-radio-group>
          </div>
          <div v-if="filterForm.period === 'custom'" class="filter-item">
            <label class="filter-label">时间范围：</label>
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadStatistics"
            />
          </div>
          <div class="filter-item">
            <label class="filter-label">部门：</label>
            <el-select v-model="filterForm.department" placeholder="全部部门" clearable @change="loadStatistics">
              <el-option label="全部部门" value="" />
              <el-option
                v-for="dept in departmentOptions"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalApplications }}</div>
                <div class="stat-label">总申请数</div>
                <div class="stat-trend" :class="getTrendClass(statistics.applicationsTrend)">
                  <el-icon><ArrowUp v-if="statistics.applicationsTrend > 0" /><ArrowDown v-else /></el-icon>
                  {{ Math.abs(statistics.applicationsTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon amount">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatAmount(statistics.totalAmount) }}</div>
                <div class="stat-label">总金额</div>
                <div class="stat-trend" :class="getTrendClass(statistics.amountTrend)">
                  <el-icon><ArrowUp v-if="statistics.amountTrend > 0" /><ArrowDown v-else /></el-icon>
                  {{ Math.abs(statistics.amountTrend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon approved">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.approvedCount }}</div>
                <div class="stat-label">已通过</div>
                <div class="stat-rate">{{ getApprovalRate() }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.pendingCount }}</div>
                <div class="stat-label">待审批</div>
                <div class="stat-rate">{{ getPendingRate() }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表分析 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 趋势图 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="card-header">
                <h3 class="card-title">申请趋势</h3>
                <el-radio-group v-model="trendChartType" size="small">
                  <el-radio-button value="count">数量</el-radio-button>
                  <el-radio-button value="amount">金额</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 状态分布 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <h3 class="card-title">状态分布</h3>
            </template>
            <div ref="statusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 部门统计 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <h3 class="card-title">部门统计</h3>
            </template>
            <div ref="departmentChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <!-- 类型分布 -->
        <el-col :xs="24" :lg="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <h3 class="card-title">报销类型分布</h3>
            </template>
            <div ref="typeChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <h3 class="card-title">详细统计</h3>
            <div class="table-actions">
              <el-input
                v-model="tableSearch"
                placeholder="搜索部门或类型"
                style="width: 200px;"
                clearable
                @input="handleTableSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </div>
        </template>
        
        <el-table
          :data="filteredTableData"
          stripe
          border
          style="width: 100%"
          :default-sort="{ prop: 'totalAmount', order: 'descending' }"
        >
          <el-table-column prop="department" label="部门" width="120" />
          <el-table-column prop="type" label="报销类型" width="120" />
          <el-table-column prop="totalCount" label="申请数量" width="100" sortable>
            <template #default="{ row }">
              <el-tag type="info">{{ row.totalCount }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="总金额" width="120" sortable>
            <template #default="{ row }">
              <span class="amount-text">¥{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="approvedCount" label="已通过" width="100" sortable>
            <template #default="{ row }">
              <el-tag type="success">{{ row.approvedCount }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rejectedCount" label="已拒绝" width="100" sortable>
            <template #default="{ row }">
              <el-tag type="danger">{{ row.rejectedCount }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="pendingCount" label="待审批" width="100" sortable>
            <template #default="{ row }">
              <el-tag type="warning">{{ row.pendingCount }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="通过率" width="100" sortable>
            <template #default="{ row }">
              <div class="rate-cell">
                <span>{{ getRowApprovalRate(row) }}%</span>
                <el-progress
                  :percentage="getRowApprovalRate(row)"
                  :stroke-width="4"
                  :show-text="false"
                  :color="getProgressColor(getRowApprovalRate(row))"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="avgAmount" label="平均金额" sortable>
            <template #default="{ row }">
              <span class="amount-text">¥{{ formatAmount(row.avgAmount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataAnalysis,
  Download,
  Refresh,
  Document,
  Money,
  Check,
  Clock,
  ArrowUp,
  ArrowDown,
  Search
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import expenseApi from '@/api/expense'
import departmentApi from '@/api/department'

// 响应式数据
const loading = ref(false)
const trendChartType = ref('count')
const tableSearch = ref('')
const departmentOptions = ref([])

// 图表引用
const trendChartRef = ref()
const statusChartRef = ref()
const departmentChartRef = ref()
const typeChartRef = ref()

// 图表实例
let trendChart = null
let statusChart = null
let departmentChart = null
let typeChart = null

// 筛选表单
const filterForm = reactive({
  period: 'month',
  dateRange: [],
  department: ''
})

// 统计数据
const statistics = reactive({
  totalApplications: 0,
  totalAmount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  pendingCount: 0,
  applicationsTrend: 0,
  amountTrend: 0
})

// 图表数据
const chartData = reactive({
  trend: [],
  status: [],
  department: [],
  type: []
})

// 表格数据
const tableData = ref([])

// 计算属性
const filteredTableData = computed(() => {
  if (!tableSearch.value) return tableData.value
  
  const search = tableSearch.value.toLowerCase()
  return tableData.value.filter(item => 
    item.department.toLowerCase().includes(search) ||
    item.type.toLowerCase().includes(search)
  )
})

// 监听器
watch(trendChartType, () => {
  updateTrendChart()
})

// 生命周期
onMounted(() => {
  loadDepartments()
  loadStatistics()
  initCharts()
})

// 方法
const loadDepartments = async () => {
  try {
    const response = await departmentApi.getDepartmentList()
    departmentOptions.value = response.data || []
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const params = {
      period: filterForm.period,
      startDate: filterForm.dateRange?.[0],
      endDate: filterForm.dateRange?.[1],
      department: filterForm.department
    }
    
    const response = await expenseApi.getStatistics(params)
    const data = response.data
    
    // 更新统计数据
    Object.assign(statistics, data.overview)
    
    // 更新图表数据
    Object.assign(chartData, data.charts)
    
    // 更新表格数据
    tableData.value = data.details || []
    
    // 更新图表
    await nextTick()
    updateAllCharts()
    
  } catch (error) {
    ElMessage.error('加载统计数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handlePeriodChange = () => {
  if (filterForm.period !== 'custom') {
    filterForm.dateRange = []
  }
  loadStatistics()
}

const handleTableSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const refreshData = () => {
  loadStatistics()
}

const exportStatistics = async () => {
  try {
    const params = {
      period: filterForm.period,
      startDate: filterForm.dateRange?.[0],
      endDate: filterForm.dateRange?.[1],
      department: filterForm.department
    }
    
    const response = await expenseApi.exportStatistics(params)
    
    // 创建下载链接
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `报销统计报告_${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + (error.message || '未知错误'))
  }
}

// 图表相关方法
const initCharts = async () => {
  await nextTick()
  
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
  }
  if (departmentChartRef.value) {
    departmentChart = echarts.init(departmentChartRef.value)
  }
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  trendChart?.resize()
  statusChart?.resize()
  departmentChart?.resize()
  typeChart?.resize()
}

const updateAllCharts = () => {
  updateTrendChart()
  updateStatusChart()
  updateDepartmentChart()
  updateTypeChart()
}

const updateTrendChart = () => {
  if (!trendChart || !chartData.trend.length) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    xAxis: {
      type: 'category',
      data: chartData.trend.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: trendChartType.value === 'count' ? '申请数量' : '申请金额',
      type: 'line',
      smooth: true,
      data: chartData.trend.map(item => 
        trendChartType.value === 'count' ? item.count : item.amount
      ),
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(64, 158, 255, 0.3)'
          }, {
            offset: 1, color: 'rgba(64, 158, 255, 0.1)'
          }]
        }
      }
    }]
  }
  
  trendChart.setOption(option)
}

const updateStatusChart = () => {
  if (!statusChart || !chartData.status.length) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '申请状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '18',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: chartData.status.map(item => ({
        name: item.status,
        value: item.count,
        itemStyle: {
          color: getStatusColor(item.status)
        }
      }))
    }]
  }
  
  statusChart.setOption(option)
}

const updateDepartmentChart = () => {
  if (!departmentChart || !chartData.department.length) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: chartData.department.map(item => item.name)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '申请数量',
      type: 'bar',
      data: chartData.department.map(item => item.count),
      itemStyle: {
        color: '#67C23A'
      }
    }]
  }
  
  departmentChart.setOption(option)
}

const updateTypeChart = () => {
  if (!typeChart || !chartData.type.length) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [{
      name: '报销类型',
      type: 'pie',
      radius: '70%',
      data: chartData.type.map(item => ({
        name: item.type,
        value: item.count
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  typeChart.setOption(option)
}

// 工具方法
const formatAmount = (amount) => {
  return Number(amount || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const getTrendClass = (trend) => {
  return trend > 0 ? 'trend-up' : 'trend-down'
}

const getApprovalRate = () => {
  if (statistics.totalApplications === 0) return 0
  return Math.round((statistics.approvedCount / statistics.totalApplications) * 100)
}

const getPendingRate = () => {
  if (statistics.totalApplications === 0) return 0
  return Math.round((statistics.pendingCount / statistics.totalApplications) * 100)
}

const getRowApprovalRate = (row) => {
  if (row.totalCount === 0) return 0
  return Math.round((row.approvedCount / row.totalCount) * 100)
}

const getProgressColor = (rate) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

const getStatusColor = (status) => {
  const colors = {
    '待审批': '#e6a23c',
    '已通过': '#67c23a',
    '已拒绝': '#f56c6c',
    '已撤回': '#909399'
  }
  return colors[status] || '#409eff'
}
</script>

<style scoped>
.expense-statistics {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-description {
  color: #606266;
  margin: 0;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-card {
  border: none;
}

.filter-content {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.overview-section {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}