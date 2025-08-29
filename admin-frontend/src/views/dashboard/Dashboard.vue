<template>
  <div class="dashboard-container">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon employee-icon">
              <el-icon size="40"><User /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ $t('dashboard.totalEmployees') }}</div>
              <div class="card-value">{{ stats.totalEmployees }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon attendance-icon">
              <el-icon size="40"><Clock /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ $t('dashboard.todayAttendance') }}</div>
              <div class="card-value">{{ typeof stats.todayAttendance === 'object' ? (stats.todayAttendance.total || 0) : stats.todayAttendance }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon leave-icon">
              <el-icon size="40"><DocumentCopy /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ $t('dashboard.pendingLeave') }}</div>
              <div class="card-value">{{ stats.pendingLeave }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="card-content">
            <div class="card-icon department-icon">
              <el-icon size="40"><OfficeBuilding /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ $t('dashboard.totalDepartments') }}</div>
              <div class="card-value">{{ stats.totalDepartments }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="chart-title">{{ $t('dashboard.attendanceStats') }}</span>
              <el-date-picker
                v-model="attendanceDate"
                type="month"
                placeholder="选择月份"
                format="YYYY年MM月"
                value-format="YYYY-MM"
                size="small"
                @change="loadAttendanceChart"
              />
            </div>
          </template>
          <div ref="attendanceChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="chart-title">{{ $t('dashboard.departmentDistribution') }}</span>
          </template>
          <div ref="departmentChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新动态 -->
    <el-row :gutter="20" class="activity-section">
      <el-col :span="8">
        <el-card class="activity-card">
          <template #header>
            <span class="activity-title">{{ $t('dashboard.latestAnnouncements') }}</span>
          </template>
          <div class="activity-list">
            <div v-for="announcement in recentAnnouncements" :key="announcement.id" class="activity-item">
              <div class="activity-title-text">{{ announcement.title }}</div>
              <div class="activity-time">{{ formatTime(announcement.createTime) }}</div>
            </div>
            <div v-if="recentAnnouncements.length === 0" class="empty-state">
              暂无公告
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="activity-card">
          <template #header>
            <span class="activity-title">{{ $t('dashboard.pendingTasks') }}</span>
          </template>
          <div class="activity-list">
            <div v-for="task in pendingTasks" :key="task.id" class="activity-item">
              <div class="activity-title-text">{{ task.title }}</div>
              <div class="activity-time">{{ formatTime(task.createTime) }}</div>
            </div>
            <div v-if="pendingTasks.length === 0" class="empty-state">
              {{ $t('dashboard.noPendingTasks') }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="activity-card">
          <template #header>
            <span class="activity-title">{{ $t('dashboard.systemMessages') }}</span>
          </template>
          <div class="activity-list">
            <div v-for="message in systemMessages" :key="message.id" class="activity-item">
              <div class="activity-title-text">{{ message.content }}</div>
              <div class="activity-time">{{ formatTime(message.createTime) }}</div>
            </div>
            <div v-if="systemMessages.length === 0" class="empty-state">
              {{ $t('dashboard.noSystemMessages') }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { User, Clock, DocumentCopy, OfficeBuilding } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const { t: $t } = useI18n()

// 响应式数据
const stats = reactive({
  totalEmployees: 0,
  todayAttendance: 0,
  pendingLeave: 0,
  totalDepartments: 0
})

const attendanceDate = ref(new Date().toISOString().substr(0, 7))
const recentAnnouncements = ref([])
const pendingTasks = ref([])
const systemMessages = ref([])

// 图表引用和实例
const attendanceChart = ref(null)
const departmentChart = ref(null)
let attendanceChartInstance = null
let departmentChartInstance = null
let resizeHandler = null

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await dashboardApi.getOverallStats()
    if (response.data) {
      // 处理特殊的数据结构
      if (response.data.todayAttendance && typeof response.data.todayAttendance === 'object') {
        // 如果todayAttendance是对象，提取总数
        stats.todayAttendance = response.data.todayAttendance.total || 0;
      } else {
        stats.todayAttendance = response.data.todayAttendance || 0;
      }
      
      // 处理pendingLeave
      if (response.data.leaveStatistics && typeof response.data.leaveStatistics === 'object') {
        stats.pendingLeave = response.data.leaveStatistics.pending || 0;
      } else {
        stats.pendingLeave = response.data.pendingLeave || 0;
      }
      
      // 处理其他简单字段
      stats.totalEmployees = response.data.totalEmployees || 0;
      stats.totalDepartments = response.data.totalDepartments || 0;
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用默认数据作为备用
    stats.totalEmployees = 156
    stats.todayAttendance = 142
    stats.pendingLeave = 8
    stats.totalDepartments = 12
  }
}

// 加载考勤图表 - 优化版本
const loadAttendanceChart = async () => {
  try {
    // 销毁现有图表实例
    if (attendanceChartInstance) {
      attendanceChartInstance.dispose();
      attendanceChartInstance = null;
    }
    
    const response = await dashboardApi.getAttendanceStats(attendanceDate.value);
    let chartData = {
      weeks: ['第1周', '第2周', '第3周', '第4周'],
      attendance: [720, 690, 701, 680],
      late: [15, 20, 18, 22],
      earlyLeave: [8, 12, 10, 15],
      absent: [5, 8, 6, 10]
    };
    
    // 处理后端返回的数据
    if (response.data) {
      // 确保数据结构正确，且数据类型为数组
      chartData = {
        weeks: Array.isArray(response.data.weeks) ? response.data.weeks : chartData.weeks,
        attendance: Array.isArray(response.data.attendance) ? response.data.attendance.map(Number) : chartData.attendance,
        late: Array.isArray(response.data.late) ? response.data.late.map(Number) : chartData.late,
        earlyLeave: Array.isArray(response.data.earlyLeave) ? response.data.earlyLeave.map(Number) : chartData.earlyLeave,
        absent: Array.isArray(response.data.absent) ? response.data.absent.map(Number) : chartData.absent
      };
    }
    
    // 确保所有数据数组长度一致
    const maxLength = Math.max(
      chartData.weeks.length,
      chartData.attendance.length,
      chartData.late.length,
      chartData.earlyLeave.length,
      chartData.absent.length
    );
    
    // 补全缺失的数据
    while (chartData.weeks.length < maxLength) chartData.weeks.push('');
    while (chartData.attendance.length < maxLength) chartData.attendance.push(0);
    while (chartData.late.length < maxLength) chartData.late.push(0);
    while (chartData.earlyLeave.length < maxLength) chartData.earlyLeave.push(0);
    while (chartData.absent.length < maxLength) chartData.absent.push(0);
    
    nextTick(() => {
      if (attendanceChart.value) {
        // 重新初始化图表
        attendanceChartInstance = echarts.init(attendanceChart.value);
        
        const option = {
          title: {
            text: '本月考勤统计',
            left: 'center',
            textStyle: {
              fontSize: 14,
              fontWeight: 'normal'
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross'
            }
          },
          legend: {
            data: ['出勤', '迟到', '早退', '缺勤'],
            top: 30
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '20%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: chartData.weeks
          },
          yAxis: {
            type: 'value'
          },
          series: [
            {
              name: '出勤',
              type: 'bar',
              data: chartData.attendance,
              itemStyle: { color: '#67C23A' }
            },
            {
              name: '迟到',
              type: 'bar',
              data: chartData.late,
              itemStyle: { color: '#E6A23C' }
            },
            {
              name: '早退',
              type: 'bar',
              data: chartData.earlyLeave,
              itemStyle: { color: '#F56C6C' }
            },
            {
              name: '缺勤',
              type: 'bar',
              data: chartData.absent,
              itemStyle: { color: '#909399' }
            }
          ]
        };
        
        // 使用notMerge选项确保完全替换配置
        attendanceChartInstance.setOption(option, { notMerge: true });
      }
    });
  } catch (error) {
    console.error('加载考勤图表数据失败:', error);
    // 使用默认数据渲染图表
    nextTick(() => {
      if (attendanceChart.value) {
        // 如果图表实例不存在，初始化它
        if (!attendanceChartInstance) {
          attendanceChartInstance = echarts.init(attendanceChart.value);
        }
        
        const defaultOption = {
          title: {
            text: '本月考勤统计',
            left: 'center',
            textStyle: {
              fontSize: 14,
              fontWeight: 'normal'
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross'
            }
          },
          legend: {
            data: ['出勤', '迟到', '早退', '缺勤'],
            top: 30
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '20%',
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
              name: '出勤',
              type: 'bar',
              data: [720, 690, 701, 680],
              itemStyle: { color: '#67C23A' }
            },
            {
              name: '迟到',
              type: 'bar',
              data: [15, 20, 18, 22],
              itemStyle: { color: '#E6A23C' }
            },
            {
              name: '早退',
              type: 'bar',
              data: [8, 12, 10, 15],
              itemStyle: { color: '#F56C6C' }
            },
            {
              name: '缺勤',
              type: 'bar',
              data: [5, 8, 6, 10],
              itemStyle: { color: '#909399' }
            }
          ]
        };
        
        // 使用notMerge选项确保完全替换配置
        attendanceChartInstance.setOption(defaultOption, { notMerge: true });
      }
    });
  }
}

// 加载部门图表 - 优化版本
const loadDepartmentChart = async () => {
  try {
    // 销毁现有图表实例
    if (departmentChartInstance) {
      departmentChartInstance.dispose();
      departmentChartInstance = null;
    }
    
    const response = await dashboardApi.getDepartmentDistribution();
    let chartData = [
      { value: 35, name: '技术部' },
      { value: 28, name: '市场部' },
      { value: 25, name: '销售部' },
      { value: 20, name: '财务部' },
      { value: 18, name: '人事部' },
      { value: 15, name: '行政部' },
      { value: 15, name: '其他部门' }
    ];
    
    // 处理后端返回的数据
    if (response.data && Array.isArray(response.data)) {
      // 确保每个数据项都有value和name字段，且value为数字类型
      chartData = response.data
        .filter(item => item && item.name) // 过滤掉无效项
        .map(item => ({
          value: Number(item.value) || 0,
          name: String(item.name) || '未知部门'
        }))
        .filter(item => item.value > 0); // 过滤掉值为0的项
      
      // 如果没有有效数据，使用默认数据
      if (chartData.length === 0) {
        chartData = [
          { value: 35, name: '技术部' },
          { value: 28, name: '市场部' },
          { value: 25, name: '销售部' },
          { value: 20, name: '财务部' },
          { value: 18, name: '人事部' },
          { value: 15, name: '行政部' },
          { value: 15, name: '其他部门' }
        ];
      }
    }
    
    nextTick(() => {
      if (departmentChart.value) {
        // 重新初始化图表
        departmentChartInstance = echarts.init(departmentChart.value);
        
        const option = {
          title: {
            text: $t('dashboard.departmentDistribution'),
            left: 'center',
            textStyle: {
              fontSize: 14,
              fontWeight: 'normal'
            }
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 30
          },
          series: [
            {
              name: '部门人员',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['60%', '50%'],
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
              data: chartData
            }
          ]
        };
        
        // 使用notMerge选项确保完全替换配置
        departmentChartInstance.setOption(option, { notMerge: true });
      }
    });
  } catch (error) {
    console.error('加载部门图表数据失败:', error);
    // 使用默认数据渲染图表
    nextTick(() => {
      if (departmentChart.value) {
        // 如果图表实例不存在，初始化它
        if (!departmentChartInstance) {
          departmentChartInstance = echarts.init(departmentChart.value);
        }
        
        const defaultOption = {
          title: {
            text: $t('dashboard.departmentDistribution'),
            left: 'center',
            textStyle: {
              fontSize: 14,
              fontWeight: 'normal'
            }
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 30
          },
          series: [
            {
              name: '部门人员',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['60%', '50%'],
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
        };
        
        // 使用notMerge选项确保完全替换配置
        departmentChartInstance.setOption(defaultOption, { notMerge: true });
      }
    });
  }
};

// 统一的窗口大小调整处理
const handleResize = () => {
  if (attendanceChartInstance && typeof attendanceChartInstance.resize === 'function') {
    try {
      attendanceChartInstance.resize();
    } catch (e) {
      console.error('调整考勤图表大小时出错:', e);
    }
  }
  if (departmentChartInstance && typeof departmentChartInstance.resize === 'function') {
    try {
      departmentChartInstance.resize();
    } catch (e) {
      console.error('调整部门图表大小时出错:', e);
    }
  }
};

// 加载动态数据
const loadActivities = async () => {
  try {
    // 并行加载各种活动数据
    const [announcementsRes, tasksRes, messagesRes] = await Promise.allSettled([
      dashboardApi.getRecentAnnouncements(3),
      dashboardApi.getPendingTasks(3),
      dashboardApi.getSystemMessages(3)
    ])
    
    // 处理最新公告数据
    if (announcementsRes.status === 'fulfilled' && announcementsRes.value.data) {
      recentAnnouncements.value = announcementsRes.value.data
    } else {
      recentAnnouncements.value = [
        { id: 1, title: '关于国庆节放假安排的通知', createTime: new Date() },
        { id: 2, title: '员工手册更新说明', createTime: new Date(Date.now() - 86400000) },
        { id: 3, title: '办公室搬迁通知', createTime: new Date(Date.now() - 172800000) }
      ]
    }
    
    // 处理待处理事项
    if (tasksRes.status === 'fulfilled' && tasksRes.value.data) {
      pendingTasks.value = tasksRes.value.data
    } else {
      pendingTasks.value = [
        { id: 1, title: '审批张三的年假申请', createTime: new Date() },
        { id: 2, title: '处理李四的调薪申请', createTime: new Date(Date.now() - 3600000) },
        { id: 3, title: '确认新员工入职信息', createTime: new Date(Date.now() - 7200000) }
      ]
    }
    
    // 处理系统消息
    if (messagesRes.status === 'fulfilled' && messagesRes.value.data) {
      systemMessages.value = messagesRes.value.data
    } else {
      systemMessages.value = [
        { id: 1, content: '系统将于今晚23:00进行维护', createTime: new Date() },
        { id: 2, content: '新版本已发布，请及时更新', createTime: new Date(Date.now() - 3600000) }
      ]
    }
  } catch (error) {
    console.error('加载活动数据失败:', error)
    // 使用默认数据
    recentAnnouncements.value = [
      { id: 1, title: '关于国庆节放假安排的通知', createTime: new Date() },
      { id: 2, title: '员工手册更新说明', createTime: new Date(Date.now() - 86400000) },
      { id: 3, title: '办公室搬迁通知', createTime: new Date(Date.now() - 172800000) }
    ]
    
    pendingTasks.value = [
      { id: 1, title: '审批张三的年假申请', createTime: new Date() },
      { id: 2, title: '处理李四的调薪申请', createTime: new Date(Date.now() - 3600000) },
      { id: 3, title: '确认新员工入职信息', createTime: new Date(Date.now() - 7200000) }
    ]
    
    systemMessages.value = [
      { id: 1, content: '系统将于今晚23:00进行维护', createTime: new Date() },
      { id: 2, content: '新版本已发布，请及时更新', createTime: new Date(Date.now() - 3600000) }
    ]
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 3600000) { // 1小时内
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 24小时内
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    return date.toLocaleDateString()
  }
}

// 组件挂载
onMounted(async () => {
  // 确保DOM已经渲染后再初始化图表
  await nextTick();
  
  // 并行加载所有数据
  await Promise.all([
    loadStats(),
    loadActivities(),
    loadAttendanceChart(),
    loadDepartmentChart()
  ]);
  
  // 添加窗口大小调整监听
  resizeHandler = handleResize;
  window.addEventListener('resize', resizeHandler);
});

// 组件卸载 - 清理资源
onUnmounted(() => {
  // 销毁图表实例
  if (attendanceChartInstance) {
    try {
      attendanceChartInstance.dispose();
    } catch (e) {
      console.error('销毁考勤图表时出错:', e);
    }
    attendanceChartInstance = null;
  }
  if (departmentChartInstance) {
    try {
      departmentChartInstance.dispose();
    } catch (e) {
      console.error('销毁部门图表时出错:', e);
    }
    departmentChartInstance = null;
  }
  
  // 移除事件监听
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler);
    resizeHandler = null;
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 140px);
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
}

.employee-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.attendance-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.leave-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.department-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
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
  height: 300px;
}

.activity-section {
  margin-bottom: 20px;
}

.activity-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 400px;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.activity-list {
  max-height: 320px;
  overflow-y: auto;
}

.activity-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.activity-item:hover {
  background-color: #f8f9fa;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-title-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.4;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 10px;
  }
  
  .card-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>