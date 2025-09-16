<template>
  <div class="admin-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <el-card shadow="never" class="welcome-card">
        <div class="welcome-content">
          <div class="welcome-info">
            <h2 class="welcome-title">欢迎回来，{{ userInfo.name }}！</h2>
            <p class="welcome-subtitle">今天是 {{ currentDate }}，祝您工作愉快</p>
            <div class="quick-stats">
              <div class="stat-item">
                <span class="stat-label">待审批申请</span>
                <span class="stat-value pending">{{ dashboardData.pendingCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">今日新增</span>
                <span class="stat-value new">{{ dashboardData.todayCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">本月总额</span>
                <span class="stat-value amount">¥{{ formatAmount(dashboardData.monthlyAmount) }}</span>
              </div>
            </div>
          </div>
          <div class="welcome-actions">
            <el-button type="primary" size="large" @click="goToApproval">
              <el-icon><DocumentChecked /></el-icon>
              开始审批
            </el-button>
            <el-button size="large" @click="goToStatistics">
              <el-icon><DataAnalysis /></el-icon>
              查看统计
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 数据概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="hover" class="overview-card">
            <div class="card-content">
              <div class="card-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ dashboardData.totalApplications }}</div>
                <div class="card-label">总申请数</div>
                <div class="card-trend" :class="getTrendClass(dashboardData.applicationsTrend)">
                  <el-icon><TrendCharts /></el-icon>
                  {{ formatTrend(dashboardData.applicationsTrend) }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="hover" class="overview-card">
            <div class="card-content">
              <div class="card-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ dashboardData.pendingCount }}</div>
                <div class="card-label">待审批</div>
                <div class="card-subtitle">需要您的处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="hover" class="overview-card">
            <div class="card-content">
              <div class="card-icon approved">
                <el-icon><Check /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ dashboardData.approvedCount }}</div>
                <div class="card-label">已通过</div>
                <div class="card-rate">通过率 {{ getApprovalRate() }}%</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :lg="6">
          <el-card shadow="hover" class="overview-card">
            <div class="card-content">
              <div class="card-icon amount">
                <el-icon><Money /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">¥{{ formatAmount(dashboardData.totalAmount) }}</div>
                <div class="card-label">总金额</div>
                <div class="card-trend" :class="getTrendClass(dashboardData.amountTrend)">
                  <el-icon><TrendCharts /></el-icon>
                  {{ formatTrend(dashboardData.amountTrend) }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 主要内容区 -->
    <el-row :gutter="20" class="main-content">
      <!-- 待处理事项 -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">
                <el-icon><Bell /></el-icon>
                待处理事项
              </h3>
              <el-button text type="primary" @click="goToApproval">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div class="pending-list">
            <div v-if="pendingItems.length === 0" class="empty-state">
              <el-icon><CircleCheck /></el-icon>
              <p>暂无待处理事项</p>
            </div>
            <div
              v-for="item in pendingItems"
              :key="item.id"
              class="pending-item"
              @click="handleItemClick(item)"
            >
              <div class="item-info">
                <div class="item-title">{{ item.title }}</div>
                <div class="item-meta">
                  <span class="applicant">{{ item.applicantName }}</span>
                  <span class="amount">¥{{ formatAmount(item.amount) }}</span>
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                </div>
              </div>
              <div class="item-actions">
                <el-tag :type="getUrgencyType(item.urgency)">{{ item.urgency }}</el-tag>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最近活动 -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="content-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">
                <el-icon><Clock /></el-icon>
                最近活动
              </h3>
              <el-button text type="primary" @click="goToAuditLog">
                查看更多
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div class="activity-list">
            <div v-if="recentActivities.length === 0" class="empty-state">
              <el-icon><Document /></el-icon>
              <p>暂无最近活动</p>
            </div>
            <div
              v-for="activity in recentActivities"
              :key="activity.id"
              class="activity-item"
            >
              <div class="activity-icon" :class="activity.type">
                <el-icon>
                  <Check v-if="activity.type === 'approved'" />
                  <Close v-else-if="activity.type === 'rejected'" />
                  <Plus v-else-if="activity.type === 'created'" />
                  <Edit v-else />
                </el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-description">{{ activity.description }}</div>
                <div class="activity-time">{{ formatTime(activity.time) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <!-- 审批趋势 -->
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">
                <el-icon><TrendCharts /></el-icon>
                审批趋势
              </h3>
              <el-radio-group v-model="trendPeriod" size="small" @change="loadTrendData">
                <el-radio-button value="week">近7天</el-radio-button>
                <el-radio-button value="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 状态分布 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <h3 class="card-title">
              <el-icon><PieChart /></el-icon>
              状态分布
            </h3>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DocumentChecked,
  DataAnalysis,
  Document,
  Clock,
  Check,
  Close,
  Money,
  TrendCharts,
  Bell,
  ArrowRight,
  CircleCheck,
  Plus,
  Edit,
  PieChart
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import expenseApi from '@/api/expense'

// 状态管理
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const trendPeriod = ref('week')
const trendChartRef = ref()
const statusChartRef = ref()

// 图表实例
let trendChart = null
let statusChart = null

// 用户信息
const userInfo = computed(() => userStore.userInfo || { name: '管理员' })

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 仪表盘数据
const dashboardData = reactive({
  totalApplications: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  totalAmount: 0,
  todayCount: 0,
  monthlyAmount: 0,
  applicationsTrend: 0,
  amountTrend: 0
})

// 待处理事项
const pendingItems = ref([])

// 最近活动
const recentActivities = ref([])

// 生命周期
onMounted(() => {
  loadDashboardData()
  loadPendingItems()
  loadRecentActivities()
  initCharts()
})

// 方法
const loadDashboardData = async () => {
  try {
    const response = await expenseApi.getDashboardData()
    Object.assign(dashboardData, response.data)
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

const loadPendingItems = async () => {
  try {
    const response = await expenseApi.getPendingItems({ limit: 5 })
    pendingItems.value = response.data.records || []
  } catch (error) {
    console.error('加载待处理事项失败:', error)
  }
}

const loadRecentActivities = async () => {
  try {
    const response = await expenseApi.getRecentActivities({ limit: 8 })
    recentActivities.value = response.data || []
  } catch (error) {
    console.error('加载最近活动失败:', error)
  }
}

const loadTrendData = async () => {
  try {
    const response = await expenseApi.getTrendData({ period: trendPeriod.value })
    updateTrendChart(response.data)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 导航方法
const goToApproval = () => {
  emit('navigate', 'expense-approval')
}

const goToStatistics = () => {
  emit('navigate', 'expense-statistics')
}

const goToAuditLog = () => {
  emit('navigate', 'audit-log')
}

const handleItemClick = (item) => {
  // 跳转到审批页面并定位到具体申请
  emit('navigate', 'expense-approval', { applicationId: item.id })
}

// 图表相关方法
const initCharts = async () => {
  await nextTick()
  
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    loadTrendData()
  }
  
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    updateStatusChart()
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  trendChart?.resize()
  statusChart?.resize()
}

const updateTrendChart = (data) => {
  if (!trendChart || !data) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['申请数量', '审批数量']
    },
    xAxis: {
      type: 'category',
      data: data.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '申请数量',
        type: 'line',
        smooth: true,
        data: data.applications,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '审批数量',
        type: 'line',
        smooth: true,
        data: data.approvals,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const updateStatusChart = () => {
  if (!statusChart) return
  
  const data = [
    { name: '待审批', value: dashboardData.pendingCount, color: '#E6A23C' },
    { name: '已通过', value: dashboardData.approvedCount, color: '#67C23A' },
    { name: '已拒绝', value: dashboardData.rejectedCount, color: '#F56C6C' }
  ]
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
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
          fontSize: '16',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: data.map(item => ({
        name: item.name,
        value: item.value,
        itemStyle: { color: item.color }
      }))
    }]
  }
  
  statusChart.setOption(option)
}

// 工具方法
const formatAmount = (amount) => {
  return Number(amount || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const formatTrend = (trend) => {
  const abs = Math.abs(trend)
  return trend > 0 ? `+${abs}%` : trend < 0 ? `-${abs}%` : '0%'
}

const getTrendClass = (trend) => {
  return trend > 0 ? 'trend-up' : trend < 0 ? 'trend-down' : 'trend-stable'
}

const getApprovalRate = () => {
  if (dashboardData.totalApplications === 0) return 0
  return Math.round((dashboardData.approvedCount / dashboardData.totalApplications) * 100)
}

const getUrgencyType = (urgency) => {
  const types = {
    '紧急': 'danger',
    '重要': 'warning',
    '普通': 'info'
  }
  return types[urgency] || 'info'
}

// 事件发射
const emit = defineEmits(['navigate'])
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  background: #f0f2f5;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-card {
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-card :deep(.el-card__body) {
  padding: 32px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-info {
  flex: 1;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: white;
}

.welcome-subtitle {
  font-size: 16px;
  margin: 0 0 24px 0;
  opacity: 0.9;
}

.quick-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
}

.stat-value.pending {
  color: #ffd700;
}

.stat-value.new {
  color: #90ee90;
}

.stat-value.amount {
  color: #ffa500;
}

.welcome-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.overview-section {
  margin-bottom: 24px;
}

.overview-card {
  border: none;
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.card-icon.total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.card-icon.pending {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.card-icon.approved {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
}

.card-icon.amount {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.card-trend,
.card-rate,
.card-subtitle {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-trend.trend-up {
  color: #67c23a;
}

.card-trend.trend-down {
  color: #f56c6c;
}

.card-trend.trend-stable {
  color: #909399;
}

.card-rate {
  color: #409eff;
}

.card-subtitle {
  color: #909399;
}

.main-content {
  margin-bottom: 24px;
}

.content-card {
  border: none;
  height: 400px;
}

.content-card :deep(.el-card__body) {
  height: calc(100% - 60px);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pending-list,
.activity-list {
  height: 100%;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.pending-item:hover {
  background-color: #f8f9fa;
}

.pending-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.item-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.item-meta .amount {
  color: #f56c6c;
  font-weight: 500;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.arrow-icon {
  color: #c0c4cc;
  transition: color 0.3s ease;
}

.pending-item:hover .arrow-icon {
  color: #409eff;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
  flex-shrink: 0;
}

.activity-icon.approved {
  background: #67c23a;
}

.activity-icon.rejected {
  background: #f56c6c;
}

.activity-icon.created {
  background: #409eff;
}

.activity-icon.updated {
  background: #e6a23c;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.activity-description {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  border: none;
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 16px;
  }
  
  .welcome-content {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }
  
  .quick-stats {
    justify-content: center;
    gap: 24px;
  }
  
  .welcome-actions {
    flex-direction: row;
    justify-content: center;
  }
  
  .card-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .card-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
  
  .card-value {
    font-size: 24px;
  }
  
  .content-card {
    height: auto;
    min-height: 300px;
  }
  
  .item-meta {
    flex-direction: column;
    gap: 4px;
  }
}

/* 滚动条样式 */
.pending-list::-webkit-scrollbar,
.activity-list::-webkit-scrollbar {
  width: 4px;
}

.pending-list::-webkit-scrollbar-track,
.activity-list::-webkit-scrollbar-track {
  background: transparent;
}

.pending-list::-webkit-scrollbar-thumb,
.activity-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

.pending-list::-webkit-scrollbar-thumb:hover,
.activity-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>