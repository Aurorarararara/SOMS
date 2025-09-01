<template>
  <div class="task-statistics">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.pending || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon processing">
          <el-icon><Loading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.processing || 0 }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon completed">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.completed || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.overdue || 0 }}</div>
          <div class="stat-label">已逾期</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 任务完成趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>任务完成趋势</h3>
          <el-select v-model="trendDays" @change="loadTrendData" style="width: 120px;">
            <el-option label="7天" :value="7" />
            <el-option label="30天" :value="30" />
            <el-option label="90天" :value="90" />
          </el-select>
        </div>
        <div class="chart-content">
          <div ref="trendChartRef" class="chart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 优先级分布饼图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>优先级分布</h3>
        </div>
        <div class="chart-content">
          <div ref="priorityChartRef" class="chart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 工作量统计 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>工作量统计</h3>
        </div>
        <div class="chart-content">
          <div class="workload-stats">
            <div class="workload-item">
              <span class="label">总任务数</span>
              <span class="value">{{ workloadStats.total_tasks || 0 }}</span>
            </div>
            <div class="workload-item">
              <span class="label">预计工时</span>
              <span class="value">{{ workloadStats.total_estimated_hours || 0 }}h</span>
            </div>
            <div class="workload-item">
              <span class="label">实际工时</span>
              <span class="value">{{ workloadStats.total_actual_hours || 0 }}h</span>
            </div>
            <div class="workload-item">
              <span class="label">平均工时</span>
              <span class="value">{{ (workloadStats.avg_actual_hours || 0).toFixed(1) }}h</span>
            </div>
            <div class="workload-item">
              <span class="label">完成任务</span>
              <span class="value">{{ workloadStats.completed_tasks || 0 }}</span>
            </div>
            <div class="workload-item">
              <span class="label">完成率</span>
              <span class="value">{{ getCompletionRate() }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 效率分析 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>效率分析</h3>
          <el-select v-model="efficiencyDays" @change="loadEfficiencyData" style="width: 120px;">
            <el-option label="7天" :value="7" />
            <el-option label="30天" :value="30" />
            <el-option label="90天" :value="90" />
          </el-select>
        </div>
        <div class="chart-content">
          <div ref="efficiencyChartRef" class="chart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 月度趋势分析 -->
      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>月度趋势分析</h3>
          <el-select v-model="monthlyRange" @change="loadMonthlyTrendData" style="width: 120px;">
            <el-option label="3个月" :value="3" />
            <el-option label="6个月" :value="6" />
            <el-option label="12个月" :value="12" />
          </el-select>
        </div>
        <div class="chart-content">
          <div ref="monthlyTrendChartRef" class="chart" style="height: 400px;"></div>
        </div>
      </div>

      <!-- 工作量趋势分析 -->
      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>工作量趋势分析</h3>
          <el-select v-model="workloadTrendDays" @change="loadWorkloadTrendData" style="width: 120px;">
            <el-option label="7天" :value="7" />
            <el-option label="30天" :value="30" />
            <el-option label="90天" :value="90" />
          </el-select>
        </div>
        <div class="chart-content">
          <div ref="workloadTrendChartRef" class="chart" style="height: 400px;"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Loading, Check, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as taskApi from '@/api/taskApi'

// 响应式数据
const loading = ref(false)
const trendDays = ref(30)
const efficiencyDays = ref(30)
const monthlyRange = ref(6)
const workloadTrendDays = ref(30)
const taskStats = ref({})
const workloadStats = ref({})
const priorityStats = ref({})
const trendData = ref([])
const efficiencyStats = ref({})
const monthlyTrendData = ref([])
const workloadTrendData = ref([])

// 图表引用
const trendChartRef = ref(null)
const priorityChartRef = ref(null)
const efficiencyChartRef = ref(null)
const monthlyTrendChartRef = ref(null)
const workloadTrendChartRef = ref(null)

// 图表实例
let trendChart = null
let priorityChart = null
let efficiencyChart = null
let monthlyTrendChart = null
let workloadTrendChart = null

// 加载任务统计数据
const loadTaskStats = async () => {
  try {
    const response = await taskApi.getTaskStatsByStatus()
    if (response.code === 200) {
      taskStats.value = response.data
    }
  } catch (error) {
    console.error('加载任务统计失败:', error)
  }
}

// 加载工作量统计
const loadWorkloadStats = async () => {
  try {
    const response = await taskApi.getWorkloadStats()
    if (response.code === 200) {
      workloadStats.value = response.data
    }
  } catch (error) {
    console.error('加载工作量统计失败:', error)
  }
}

// 加载优先级统计
const loadPriorityStats = async () => {
  try {
    const response = await taskApi.getPriorityDistributionStats()
    if (response.code === 200) {
      priorityStats.value = response.data
      renderPriorityChart()
    }
  } catch (error) {
    console.error('加载优先级统计失败:', error)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const response = await taskApi.getTaskTrendStats(trendDays.value)
    if (response.code === 200) {
      trendData.value = response.data
      renderTrendChart()
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 加载效率统计
const loadEfficiencyStats = async () => {
  try {
    const response = await taskApi.getTaskEfficiencyStats()
    if (response.code === 200) {
      efficiencyStats.value = response.data
      renderEfficiencyChart()
    }
  } catch (error) {
    console.error('加载效率统计失败:', error)
  }
}

// 计算完成率
const getCompletionRate = () => {
  const total = workloadStats.value.total_tasks || 0
  const completed = workloadStats.value.completed_tasks || 0
  if (total === 0) return 0
  return ((completed / total) * 100).toFixed(1)
}

// 渲染趋势图表
const renderTrendChart = () => {
  if (!trendChartRef.value || !trendData.value.length) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  const dates = trendData.value.map(item => item.date)
  const createdData = trendData.value.map(item => item.created_count)
  const completedData = trendData.value.map(item => item.completed_count)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['创建任务', '完成任务']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '创建任务',
        type: 'line',
        data: createdData,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '完成任务',
        type: 'line',
        data: completedData,
        smooth: true,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 渲染优先级饼图
const renderPriorityChart = () => {
  if (!priorityChartRef.value) return
  
  if (priorityChart) {
    priorityChart.dispose()
  }
  
  priorityChart = echarts.init(priorityChartRef.value)
  
  const data = [
    { value: priorityStats.value.low_priority || 0, name: '低优先级' },
    { value: priorityStats.value.normal_priority || 0, name: '普通优先级' },
    { value: priorityStats.value.high_priority || 0, name: '高优先级' },
    { value: priorityStats.value.urgent_priority || 0, name: '紧急优先级' }
  ]
  
  const option = {
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
        name: '优先级分布',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  priorityChart.setOption(option)
}

// 渲染效率图表
const renderEfficiencyChart = () => {
  if (!efficiencyChartRef.value) return
  
  if (efficiencyChart) {
    efficiencyChart.dispose()
  }
  
  efficiencyChart = echarts.init(efficiencyChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: ['效率比率', '按时完成率', '平均完成时间(小时)']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '效率指标',
        type: 'bar',
        data: [
          (efficiencyStats.value.efficiency_ratio || 0) * 100,
          ((efficiencyStats.value.on_time_tasks || 0) / (efficiencyStats.value.total_completed || 1)) * 100,
          efficiencyStats.value.avg_completion_time || 0
        ],
        itemStyle: {
          color: function(params) {
            const colors = ['#409EFF', '#67C23A', '#E6A23C']
            return colors[params.dataIndex]
          }
        }
      }
    ]
  }
  
  efficiencyChart.setOption(option)
}

// 加载效率数据
const loadEfficiencyData = async () => {
  try {
    const response = await taskApi.getEfficiencyAnalysis(efficiencyDays.value)
    if (response.code === 200) {
      efficiencyStats.value = response.data
      renderEfficiencyChart()
    }
  } catch (error) {
    console.error('加载效率数据失败:', error)
  }
}

// 加载月度趋势数据
const loadMonthlyTrendData = async () => {
  try {
    const response = await taskApi.getMonthlyTaskTrendStats(monthlyRange.value)
    if (response.code === 200) {
      monthlyTrendData.value = response.data
      renderMonthlyTrendChart()
    }
  } catch (error) {
    console.error('加载月度趋势数据失败:', error)
  }
}

// 加载工作量趋势数据
const loadWorkloadTrendData = async () => {
  try {
    const response = await taskApi.getWorkloadTrendStats(workloadTrendDays.value)
    if (response.code === 200) {
      workloadTrendData.value = response.data
      renderWorkloadTrendChart()
    }
  } catch (error) {
    console.error('加载工作量趋势数据失败:', error)
  }
}

// 渲染月度趋势图表
const renderMonthlyTrendChart = () => {
  if (!monthlyTrendChartRef.value || !monthlyTrendData.value.length) return

  if (monthlyTrendChart) {
    monthlyTrendChart.dispose()
  }

  monthlyTrendChart = echarts.init(monthlyTrendChartRef.value)

  const months = monthlyTrendData.value.map(item => item.month)
  const createdData = monthlyTrendData.value.map(item => item.created_count)
  const completedData = monthlyTrendData.value.map(item => item.completed_count)
  const completionRates = monthlyTrendData.value.map(item => item.completion_rate)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['创建任务', '完成任务', '完成率']
    },
    xAxis: {
      type: 'category',
      data: months
    },
    yAxis: [
      {
        type: 'value',
        name: '任务数量',
        position: 'left'
      },
      {
        type: 'value',
        name: '完成率(%)',
        position: 'right',
        max: 100
      }
    ],
    series: [
      {
        name: '创建任务',
        type: 'bar',
        data: createdData,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '完成任务',
        type: 'bar',
        data: completedData,
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '完成率',
        type: 'line',
        yAxisIndex: 1,
        data: completionRates,
        smooth: true,
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }

  monthlyTrendChart.setOption(option)
}

// 渲染工作量趋势图表
const renderWorkloadTrendChart = () => {
  if (!workloadTrendChartRef.value || !workloadTrendData.value.length) return

  if (workloadTrendChart) {
    workloadTrendChart.dispose()
  }

  workloadTrendChart = echarts.init(workloadTrendChartRef.value)

  const dates = workloadTrendData.value.map(item => item.date)
  const estimatedHours = workloadTrendData.value.map(item => item.total_estimated_hours)
  const actualHours = workloadTrendData.value.map(item => item.total_actual_hours)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['预计工时', '实际工时']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '工时(小时)'
    },
    series: [
      {
        name: '预计工时',
        type: 'line',
        data: estimatedHours,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          opacity: 0.3
        }
      },
      {
        name: '实际工时',
        type: 'line',
        data: actualHours,
        smooth: true,
        itemStyle: {
          color: '#F56C6C'
        },
        areaStyle: {
          opacity: 0.3
        }
      }
    ]
  }

  workloadTrendChart.setOption(option)
}

// 初始化图表
const initCharts = async () => {
  await nextTick()
  renderTrendChart()
  renderPriorityChart()
  renderEfficiencyChart()
  renderMonthlyTrendChart()
  renderWorkloadTrendChart()
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  if (trendChart) trendChart.resize()
  if (priorityChart) priorityChart.resize()
  if (efficiencyChart) efficiencyChart.resize()
  if (monthlyTrendChart) monthlyTrendChart.resize()
  if (workloadTrendChart) workloadTrendChart.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadTaskStats(),
      loadWorkloadStats(),
      loadPriorityStats(),
      loadTrendData(),
      loadEfficiencyStats(),
      loadMonthlyTrendData(),
      loadWorkloadTrendData()
    ])
    await initCharts()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }

  window.addEventListener('resize', handleResize)
})

// 清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) trendChart.dispose()
  if (priorityChart) priorityChart.dispose()
  if (efficiencyChart) efficiencyChart.dispose()
  if (monthlyTrendChart) monthlyTrendChart.dispose()
  if (workloadTrendChart) workloadTrendChart.dispose()
})
</script>

<style scoped>
.task-statistics {
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.stat-icon.processing {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.overdue {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-content {
  position: relative;
}

.chart {
  width: 100%;
}

.workload-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  padding: 20px 0;
}

.workload-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.workload-item .label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.workload-item .value {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

@media (max-width: 768px) {
  .charts-container {
    grid-template-columns: 1fr;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .workload-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .workload-stats {
    grid-template-columns: 1fr;
  }
}
</style>
