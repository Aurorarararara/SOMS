<template>
  <div class="analytics-dashboard">
    <!-- 顶部概览卡片 -->
    <div class="overview-cards">
      <el-row :gutter="20">
        <el-col :span="6" v-for="card in overviewCards" :key="card.key">
          <el-card class="overview-card" :class="card.type">
            <div class="card-content">
              <div class="card-icon">
                <el-icon :size="32"><component :is="card.icon" /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">{{ card.title }}</div>
                <div class="card-value">{{ card.value }}</div>
                <div class="card-trend" :class="card.trend">
                  <el-icon><component :is="card.trendIcon" /></el-icon>
                  <span>{{ card.trendText }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主要图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 员工效率趋势图 -->
        <el-col :span="12">
          <el-card title="员工效率趋势">
            <template #header>
              <div class="card-header">
                <span>员工效率趋势</span>
                <el-select v-model="efficiencyPeriod" size="small" @change="loadEfficiencyTrend">
                  <el-option label="最近7天" value="7d" />
                  <el-option label="最近30天" value="30d" />
                  <el-option label="最近90天" value="90d" />
                </el-select>
              </div>
            </template>
            <div ref="efficiencyChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 部门绩效对比 -->
        <el-col :span="12">
          <el-card title="部门绩效对比">
            <template #header>
              <div class="card-header">
                <span>部门绩效对比</span>
                <el-select v-model="performanceMetric" size="small" @change="loadDepartmentPerformance">
                  <el-option label="任务完成率" value="task_completion" />
                  <el-option label="出勤率" value="attendance" />
                  <el-option label="协作活跃度" value="collaboration" />
                </el-select>
              </div>
            </template>
            <div ref="performanceChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 离职风险预警 -->
        <el-col :span="8">
          <el-card title="离职风险预警">
            <div class="risk-alerts">
              <div v-for="alert in riskAlerts" :key="alert.id" class="risk-item" :class="alert.level">
                <div class="risk-header">
                  <span class="employee-name">{{ alert.employeeName }}</span>
                  <el-tag :type="getRiskTagType(alert.level)" size="small">{{ alert.levelText }}</el-tag>
                </div>
                <div class="risk-details">
                  <div class="risk-score">风险指数: {{ alert.riskScore }}%</div>
                  <div class="risk-factors">{{ alert.riskFactors }}</div>
                </div>
                <div class="risk-actions">
                  <el-button size="small" type="primary" @click="viewRiskDetails(alert)">查看详情</el-button>
                  <el-button size="small" @click="createRetentionPlan(alert)">制定挽留计划</el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 实时活动监控 -->
        <el-col :span="8">
          <el-card title="实时活动监控">
            <div class="activity-monitor">
              <div class="activity-stats">
                <div class="stat-item">
                  <span class="stat-label">在线用户</span>
                  <span class="stat-value online">{{ activityStats.onlineUsers }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">进行中会议</span>
                  <span class="stat-value meeting">{{ activityStats.activeMeetings }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">协作文档</span>
                  <span class="stat-value document">{{ activityStats.activeDocuments }}</span>
                </div>
              </div>
              <div class="activity-chart">
                <div ref="activityChart" class="mini-chart"></div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 智能洞察 -->
        <el-col :span="8">
          <el-card title="智能洞察">
            <div class="insights-list">
              <div v-for="insight in intelligentInsights" :key="insight.id" class="insight-item">
                <div class="insight-icon">
                  <el-icon><component :is="insight.icon" /></el-icon>
                </div>
                <div class="insight-content">
                  <div class="insight-title">{{ insight.title }}</div>
                  <div class="insight-description">{{ insight.description }}</div>
                  <div class="insight-action">
                    <el-button size="small" type="text" @click="viewInsightDetails(insight)">
                      查看详情 <el-icon><ArrowRight /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- KPI指标面板 -->
    <div class="kpi-section">
      <el-card title="关键绩效指标">
        <template #header>
          <div class="card-header">
            <span>关键绩效指标</span>
            <div class="header-actions">
              <el-button size="small" @click="refreshKpiData">刷新数据</el-button>
              <el-button size="small" type="primary" @click="showKpiConfig">配置指标</el-button>
            </div>
          </div>
        </template>
        <div class="kpi-grid">
          <div v-for="kpi in kpiMetrics" :key="kpi.id" class="kpi-card" :class="kpi.status">
            <div class="kpi-header">
              <span class="kpi-name">{{ kpi.name }}</span>
              <el-tag :type="getKpiStatusType(kpi.status)" size="small">{{ kpi.statusText }}</el-tag>
            </div>
            <div class="kpi-value">
              <span class="current-value">{{ kpi.currentValue }}</span>
              <span class="unit">{{ kpi.unit }}</span>
            </div>
            <div class="kpi-target">
              目标: {{ kpi.targetValue }}{{ kpi.unit }}
            </div>
            <div class="kpi-progress">
              <el-progress 
                :percentage="kpi.progressPercentage" 
                :color="getKpiProgressColor(kpi.status)"
                :show-text="false"
                :stroke-width="6"
              />
            </div>
            <div class="kpi-trend">
              <el-icon :class="kpi.trendDirection"><component :is="getTrendIcon(kpi.trendDirection)" /></el-icon>
              <span>{{ kpi.trendText }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 数据导出对话框 -->
    <el-dialog v-model="exportDialogVisible" title="数据导出" width="600px">
      <el-form :model="exportForm" label-width="100px">
        <el-form-item label="导出类型">
          <el-select v-model="exportForm.exportType" placeholder="请选择导出类型">
            <el-option label="员工效率报表" value="employee_efficiency" />
            <el-option label="部门绩效报表" value="department_performance" />
            <el-option label="考勤分析报表" value="attendance_analysis" />
            <el-option label="会议效率报表" value="meeting_efficiency" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="exportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="文件格式">
          <el-radio-group v-model="exportForm.fileFormat">
            <el-radio label="excel">Excel</el-radio>
            <el-radio label="pdf">PDF</el-radio>
            <el-radio label="csv">CSV</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startExport">开始导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  TrendCharts,
  DataAnalysis,
  Warning,
  User,
  VideoCamera,
  Document,
  ArrowUp,
  ArrowDown,
  Minus,
  ArrowRight,
  Refresh,
  Setting
} from '@element-plus/icons-vue'

// 响应式数据
const efficiencyChart = ref(null)
const performanceChart = ref(null)
const activityChart = ref(null)

const efficiencyPeriod = ref('30d')
const performanceMetric = ref('task_completion')

const exportDialogVisible = ref(false)
const exportForm = reactive({
  exportType: '',
  dateRange: [],
  fileFormat: 'excel'
})

// 概览卡片数据
const overviewCards = ref([
  {
    key: 'efficiency',
    title: '整体效率',
    value: '87.5%',
    trend: 'up',
    trendText: '+2.3%',
    trendIcon: ArrowUp,
    icon: TrendCharts,
    type: 'success'
  },
  {
    key: 'attendance',
    title: '出勤率',
    value: '94.2%',
    trend: 'up',
    trendText: '+1.1%',
    trendIcon: ArrowUp,
    icon: User,
    type: 'primary'
  },
  {
    key: 'satisfaction',
    title: '员工满意度',
    value: '4.2/5.0',
    trend: 'stable',
    trendText: '持平',
    trendIcon: Minus,
    icon: DataAnalysis,
    type: 'info'
  },
  {
    key: 'risk',
    title: '离职风险',
    value: '12.3%',
    trend: 'down',
    trendText: '-0.8%',
    trendIcon: ArrowDown,
    icon: Warning,
    type: 'warning'
  }
])

// 离职风险预警数据
const riskAlerts = ref([
  {
    id: 1,
    employeeName: '张三',
    level: 'high',
    levelText: '高风险',
    riskScore: 85,
    riskFactors: '工作满意度下降，加班频率增加'
  },
  {
    id: 2,
    employeeName: '李四',
    level: 'medium',
    levelText: '中风险',
    riskScore: 65,
    riskFactors: '团队协作参与度降低'
  },
  {
    id: 3,
    employeeName: '王五',
    level: 'low',
    levelText: '低风险',
    riskScore: 35,
    riskFactors: '工作负荷略高'
  }
])

// 实时活动统计
const activityStats = ref({
  onlineUsers: 156,
  activeMeetings: 8,
  activeDocuments: 23
})

// 智能洞察数据
const intelligentInsights = ref([
  {
    id: 1,
    title: '效率优化建议',
    description: '技术部门会议时间过长，建议优化会议流程',
    icon: TrendCharts
  },
  {
    id: 2,
    title: '协作模式分析',
    description: '跨部门协作项目增加，建议建立专项沟通机制',
    icon: DataAnalysis
  },
  {
    id: 3,
    title: '资源配置建议',
    description: '市场部工作负荷较重，建议适当调配人力资源',
    icon: User
  }
])

// KPI指标数据
const kpiMetrics = ref([
  {
    id: 1,
    name: '任务完成率',
    currentValue: 92.5,
    targetValue: 90,
    unit: '%',
    status: 'good',
    statusText: '良好',
    progressPercentage: 103,
    trendDirection: 'up',
    trendText: '较上月+2.1%'
  },
  {
    id: 2,
    name: '会议参与率',
    currentValue: 78.3,
    targetValue: 85,
    unit: '%',
    status: 'warning',
    statusText: '待改善',
    progressPercentage: 92,
    trendDirection: 'down',
    trendText: '较上月-1.5%'
  },
  {
    id: 3,
    name: '文档协作活跃度',
    currentValue: 65.8,
    targetValue: 60,
    unit: '%',
    status: 'good',
    statusText: '良好',
    progressPercentage: 110,
    trendDirection: 'up',
    trendText: '较上月+3.2%'
  },
  {
    id: 4,
    name: '员工满意度',
    currentValue: 4.2,
    targetValue: 4.0,
    unit: '分',
    status: 'excellent',
    statusText: '优秀',
    progressPercentage: 105,
    trendDirection: 'stable',
    trendText: '较上月持平'
  }
])

// 生命周期
onMounted(() => {
  initCharts()
  loadDashboardData()
})

// 方法
const initCharts = () => {
  nextTick(() => {
    initEfficiencyChart()
    initPerformanceChart()
    initActivityChart()
  })
}

const initEfficiencyChart = () => {
  const chart = echarts.init(efficiencyChart.value)
  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['任务完成率', '工作效率', '协作活跃度']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '任务完成率',
        type: 'line',
        data: [85, 87, 89, 91, 88, 92, 94],
        smooth: true
      },
      {
        name: '工作效率',
        type: 'line',
        data: [78, 82, 85, 87, 84, 89, 91],
        smooth: true
      },
      {
        name: '协作活跃度',
        type: 'line',
        data: [65, 68, 72, 75, 71, 78, 82],
        smooth: true
      }
    ]
  }
  chart.setOption(option)
}

const initPerformanceChart = () => {
  const chart = echarts.init(performanceChart.value)
  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['技术部', '市场部', '销售部', '人事部', '财务部']
    },
    series: [
      {
        name: '绩效得分',
        type: 'bar',
        data: [92, 88, 85, 90, 87],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
}

const initActivityChart = () => {
  const chart = echarts.init(activityChart.value)
  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
      axisLabel: {
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 10
      }
    },
    series: [
      {
        name: '活跃用户',
        type: 'area',
        data: [12, 8, 45, 78, 65, 23],
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(24, 144, 255, 0.6)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
}

const loadDashboardData = async () => {
  try {
    // 这里调用API加载仪表板数据
    console.log('Loading dashboard data...')
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const loadEfficiencyTrend = () => {
  console.log('Loading efficiency trend for period:', efficiencyPeriod.value)
  // 重新加载效率趋势数据
}

const loadDepartmentPerformance = () => {
  console.log('Loading department performance for metric:', performanceMetric.value)
  // 重新加载部门绩效数据
}

const refreshKpiData = () => {
  ElMessage.success('KPI数据已刷新')
  // 刷新KPI数据
}

const showKpiConfig = () => {
  ElMessage.info('KPI配置功能开发中...')
}

const viewRiskDetails = (alert) => {
  ElMessageBox.alert(`员工：${alert.employeeName}\n风险指数：${alert.riskScore}%\n风险因素：${alert.riskFactors}`, '离职风险详情', {
    confirmButtonText: '确定'
  })
}

const createRetentionPlan = (alert) => {
  ElMessage.success(`已为${alert.employeeName}创建挽留计划`)
}

const viewInsightDetails = (insight) => {
  ElMessageBox.alert(insight.description, insight.title, {
    confirmButtonText: '确定'
  })
}

const startExport = () => {
  if (!exportForm.exportType || !exportForm.dateRange.length) {
    ElMessage.warning('请完善导出配置')
    return
  }
  
  ElMessage.success('导出任务已创建，请稍后查看导出结果')
  exportDialogVisible.value = false
}

// 辅助方法
const getRiskTagType = (level) => {
  const types = {
    high: 'danger',
    medium: 'warning',
    low: 'success'
  }
  return types[level] || 'info'
}

const getKpiStatusType = (status) => {
  const types = {
    excellent: 'success',
    good: 'primary',
    warning: 'warning',
    critical: 'danger'
  }
  return types[status] || 'info'
}

const getKpiProgressColor = (status) => {
  const colors = {
    excellent: '#67c23a',
    good: '#409eff',
    warning: '#e6a23c',
    critical: '#f56c6c'
  }
  return colors[status] || '#909399'
}

const getTrendIcon = (direction) => {
  const icons = {
    up: ArrowUp,
    down: ArrowDown,
    stable: Minus
  }
  return icons[direction] || Minus
}
</script>

<style scoped>
.analytics-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  margin-right: 16px;
  padding: 12px;
  border-radius: 8px;
  background-color: rgba(24, 144, 255, 0.1);
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.card-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.card-trend.up {
  color: #67c23a;
}

.card-trend.down {
  color: #f56c6c;
}

.card-trend.stable {
  color: #909399;
}

.charts-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.mini-chart {
  height: 150px;
  width: 100%;
}

.risk-alerts {
  max-height: 400px;
  overflow-y: auto;
}

.risk-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  background-color: #fff;
}

.risk-item.high {
  border-left: 4px solid #f56c6c;
}

.risk-item.medium {
  border-left: 4px solid #e6a23c;
}

.risk-item.low {
  border-left: 4px solid #67c23a;
}

.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.employee-name {
  font-weight: bold;
  font-size: 16px;
}

.risk-details {
  margin-bottom: 12px;
}

.risk-score {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.risk-factors {
  font-size: 12px;
  color: #999;
}

.risk-actions {
  display: flex;
  gap: 8px;
}

.activity-monitor {
  padding: 16px 0;
}

.activity-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
}

.stat-value.online {
  color: #67c23a;
}

.stat-value.meeting {
  color: #409eff;
}

.stat-value.document {
  color: #e6a23c;
}

.insights-list {
  max-height: 400px;
  overflow-y: auto;
}

.insight-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.insight-item:last-child {
  border-bottom: none;
}

.insight-icon {
  margin-right: 12px;
  padding: 8px;
  border-radius: 50%;
  background-color: rgba(24, 144, 255, 0.1);
}

.insight-content {
  flex: 1;
}

.insight-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.insight-description {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.kpi-section {
  margin-bottom: 20px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.kpi-card {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.3s;
}

.kpi-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.kpi-card.excellent {
  border-left: 4px solid #67c23a;
}

.kpi-card.good {
  border-left: 4px solid #409eff;
}

.kpi-card.warning {
  border-left: 4px solid #e6a23c;
}

.kpi-card.critical {
  border-left: 4px solid #f56c6c;
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.kpi-name {
  font-weight: bold;
  font-size: 16px;
}

.kpi-value {
  margin-bottom: 8px;
}

.current-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.unit {
  font-size: 14px;
  color: #666;
  margin-left: 4px;
}

.kpi-target {
  font-size: 12px;
  color: #666;
  margin-bottom: 12px;
}

.kpi-progress {
  margin-bottom: 12px;
}

.kpi-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.kpi-trend .el-icon {
  margin-right: 4px;
}

.kpi-trend .el-icon.up {
  color: #67c23a;
}

.kpi-trend .el-icon.down {
  color: #f56c6c;
}

.kpi-trend .el-icon.stable {
  color: #909399;
}
</style>