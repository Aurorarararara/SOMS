<template>
  <div class="attendance-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">考勤管理</h1>
          <p class="page-subtitle">查看和管理您的考勤记录</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showAttendanceDialog = true">
            <el-icon><Plus /></el-icon>
            手动打卡
          </el-button>
        </div>
      </div>
    </div>

    <!-- 今日打卡区域 -->
    <div class="today-section">
      <div class="today-card">
        <div class="today-header">
          <div class="date-info">
            <h2>{{ currentDate }}</h2>
            <p>{{ currentTime }}</p>
          </div>
          <div class="weather-info">
            <el-icon size="24" color="#f39c12"><Sunny /></el-icon>
            <span>24°C 晴朗</span>
          </div>
        </div>
        
        <div class="attendance-actions">
          <div class="action-card" :class="{ 'checked-in': todayRecord?.checkInTime }">
            <div class="action-icon">
              <el-icon size="32"><Clock /></el-icon>
            </div>
            <div class="action-content">
              <h3>上班打卡</h3>
              <p>{{ todayRecord?.checkInTime || '未打卡' }}</p>
              <el-button 
                type="primary" 
                :disabled="!!todayRecord?.checkInTime"
                @click="handleCheckIn"
                class="action-btn"
              >
                {{ todayRecord?.checkInTime ? '已打卡' : '立即打卡' }}
              </el-button>
            </div>
          </div>

          <div class="action-card" :class="{ 'checked-out': todayRecord?.checkOutTime }">
            <div class="action-icon">
              <el-icon size="32"><Timer /></el-icon>
            </div>
            <div class="action-content">
              <h3>下班打卡</h3>
              <p>{{ todayRecord?.checkOutTime || '未打卡' }}</p>
              <el-button 
                type="success" 
                :disabled="!todayRecord?.checkInTime || !!todayRecord?.checkOutTime"
                @click="handleCheckOut"
                class="action-btn"
              >
                {{ todayRecord?.checkOutTime ? '已打卡' : '立即打卡' }}
              </el-button>
            </div>
          </div>

          <div class="work-hours-card">
            <div class="work-hours-content">
              <div class="hours-info">
                <h3>今日工时</h3>
                <div class="hours-display">
                  <span class="hours">{{ todayRecord?.workHours || '0' }}</span>
                  <span class="unit">小时</span>
                </div>
              </div>
              <div class="status-info">
                <el-tag 
                  :type="getStatusTagType(todayRecord?.status)" 
                  size="large"
                >
                  {{ getStatusText(todayRecord?.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计数据区域 -->
    <div class="statistics-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon primary">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">本月出勤</div>
            <div class="stat-value">{{ monthlyStats.attendanceDays }}</div>
            <div class="stat-subtitle">天</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">工作时长</div>
            <div class="stat-value">{{ monthlyStats.workHours }}</div>
            <div class="stat-subtitle">小时</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon warning">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">迟到次数</div>
            <div class="stat-value">{{ monthlyStats.lateDays }}</div>
            <div class="stat-subtitle">次</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon info">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">出勤率</div>
            <div class="stat-value">{{ monthlyStats.attendanceRate }}%</div>
            <div class="stat-subtitle">本月</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 考勤记录区域 -->
    <div class="records-section">
      <div class="records-card">
        <div class="card-header">
          <h3>考勤记录</h3>
          <div class="header-actions">
            <el-date-picker
              v-model="selectedMonth"
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              value-format="YYYY-MM"
              @change="loadAttendanceRecords"
            />
            <el-button @click="exportRecords">
              <el-icon><Download /></el-icon>
              导出记录
            </el-button>
          </div>
        </div>

        <div class="records-table">
          <el-table 
            :data="attendanceRecords" 
            v-loading="loading"
            height="400"
            :default-sort="{ prop: 'date', order: 'descending' }"
          >
            <el-table-column prop="date" label="日期" width="120" sortable>
              <template #default="{ row }">
                <div class="date-cell">
                  <div class="date-text">{{ formatDate(row.date) }}</div>
                  <div class="weekday-text">{{ getWeekday(row.date) }}</div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="checkInTime" label="上班时间" width="120">
              <template #default="{ row }">
                <span :class="{ 'late-time': isLate(row.checkInTime) }">
                  {{ row.checkInTime || '-' }}
                </span>
              </template>
            </el-table-column>
            
            <el-table-column prop="checkOutTime" label="下班时间" width="120">
              <template #default="{ row }">
                {{ row.checkOutTime || '-' }}
              </template>
            </el-table-column>
            
            <el-table-column prop="workHours" label="工作时长" width="100">
              <template #default="{ row }">
                {{ row.workHours ? row.workHours + 'h' : '-' }}
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="remark" label="备注" min-width="200">
              <template #default="{ row }">
                {{ row.remark || '-' }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button 
                  text 
                  type="primary" 
                  @click="viewDetail(row)"
                  size="small"
                >
                  详情
                </el-button>
                <el-button 
                  text 
                  type="warning" 
                  @click="editRecord(row)"
                  size="small"
                  v-if="canEdit(row)"
                >
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadAttendanceRecords"
            @current-change="loadAttendanceRecords"
          />
        </div>
      </div>
    </div>

    <!-- 手动打卡对话框 -->
    <el-dialog
      v-model="showAttendanceDialog"
      title="手动打卡"
      width="500px"
      @close="resetAttendanceForm"
    >
      <el-form
        ref="attendanceFormRef"
        :model="attendanceForm"
        :rules="attendanceRules"
        label-width="100px"
      >
        <el-form-item label="打卡类型" prop="type">
          <el-radio-group v-model="attendanceForm.type">
            <el-radio :label="1">上班打卡</el-radio>
            <el-radio :label="2">下班打卡</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="打卡时间" prop="time">
          <el-time-picker
            v-model="attendanceForm.time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="attendanceForm.remark"
            type="textarea"
            rows="3"
            placeholder="请输入备注（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAttendanceDialog = false">取消</el-button>
          <el-button type="primary" @click="submitAttendance" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 记录详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="考勤详情"
      width="600px"
    >
      <div class="detail-content" v-if="selectedRecord">
        <div class="detail-grid">
          <div class="detail-item">
            <label>日期</label>
            <span>{{ formatDate(selectedRecord.date) }} {{ getWeekday(selectedRecord.date) }}</span>
          </div>
          <div class="detail-item">
            <label>上班时间</label>
            <span>{{ selectedRecord.checkInTime || '未打卡' }}</span>
          </div>
          <div class="detail-item">
            <label>下班时间</label>
            <span>{{ selectedRecord.checkOutTime || '未打卡' }}</span>
          </div>
          <div class="detail-item">
            <label>工作时长</label>
            <span>{{ selectedRecord.workHours ? selectedRecord.workHours + '小时' : '-' }}</span>
          </div>
          <div class="detail-item">
            <label>状态</label>
            <el-tag :type="getStatusTagType(selectedRecord.status)">
              {{ getStatusText(selectedRecord.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>备注</label>
            <span>{{ selectedRecord.remark || '无' }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Clock, Timer, Sunny, Calendar, Warning, TrendCharts,
  Download
} from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/attendance'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showAttendanceDialog = ref(false)
const showDetailDialog = ref(false)
const selectedRecord = ref(null)
const todayRecord = ref(null)
const attendanceRecords = ref([])
const selectedMonth = ref(new Date().toISOString().slice(0, 7))

const attendanceFormRef = ref()
const attendanceForm = reactive({
  type: 1,
  time: '',
  remark: ''
})

const attendanceRules = {
  type: [{ required: true, message: '请选择打卡类型', trigger: 'change' }],
  time: [{ required: true, message: '请选择打卡时间', trigger: 'change' }]
}

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const monthlyStats = reactive({
  attendanceDays: 20,
  workHours: 160,
  lateDays: 2,
  attendanceRate: 95
})

// 计算属性
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const currentTime = computed(() => {
  return new Date().toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

// 方法
const handleCheckIn = async () => {
  try {
    await ElMessageBox.confirm('确认上班打卡吗？', '考勤打卡', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    await attendanceApi.checkIn()
    ElMessage.success('上班打卡成功！')
    loadTodayRecord()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('打卡失败，请重试')
    }
  }
}

const handleCheckOut = async () => {
  try {
    await ElMessageBox.confirm('确认下班打卡吗？', '考勤打卡', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    await attendanceApi.checkOut()
    ElMessage.success('下班打卡成功！')
    loadTodayRecord()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('打卡失败，请重试')
    }
  }
}

const submitAttendance = async () => {
  try {
    await attendanceFormRef.value?.validate()
    submitting.value = true

    const data = {
      type: attendanceForm.type,
      time: attendanceForm.time,
      remark: attendanceForm.remark
    }

    await attendanceApi.manualCheckIn(data)
    ElMessage.success('手动打卡成功！')
    showAttendanceDialog.value = false
    loadTodayRecord()
    loadAttendanceRecords()
  } catch (error) {
    ElMessage.error('手动打卡失败，请重试')
  } finally {
    submitting.value = false
  }
}

const resetAttendanceForm = () => {
  attendanceForm.type = 1
  attendanceForm.time = ''
  attendanceForm.remark = ''
  attendanceFormRef.value?.resetFields()
}

const viewDetail = (record) => {
  selectedRecord.value = record
  showDetailDialog.value = true
}

const editRecord = (record) => {
  // 编辑记录逻辑
  ElMessage.info('编辑功能开发中...')
}

const canEdit = (record) => {
  // 判断是否可以编辑（例如当天的记录）
  const today = new Date().toISOString().split('T')[0]
  return record.date === today
}

const exportRecords = async () => {
  try {
    await attendanceApi.exportRecords(selectedMonth.value)
    ElMessage.success('导出成功！')
  } catch (error) {
    ElMessage.error('导出失败，请重试')
  }
}

const getStatusTagType = (status) => {
  const types = {
    1: 'success',  // 正常
    2: 'warning',  // 迟到
    3: 'danger',   // 早退
    4: 'info'      // 异常
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    1: '正常',
    2: '迟到',
    3: '早退',
    4: '异常'
  }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

const getWeekday = (date) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[new Date(date).getDay()]
}

const isLate = (checkInTime) => {
  if (!checkInTime) return false
  const time = new Date(`2000-01-01 ${checkInTime}`)
  const nineAM = new Date('2000-01-01 09:00:00')
  return time > nineAM
}

// 数据加载
const loadTodayRecord = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const { data } = await attendanceApi.getTodayRecord(today)
    todayRecord.value = data
  } catch (error) {
    console.error('加载今日记录失败:', error)
  }
}

const loadAttendanceRecords = async () => {
  try {
    loading.value = true
    const params = {
      month: selectedMonth.value,
      current: pagination.current,
      size: pagination.size
    }
    
    const { data } = await attendanceApi.getRecords(params)
    attendanceRecords.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error('加载考勤记录失败:', error)
    attendanceRecords.value = []
  } finally {
    loading.value = false
  }
}

const loadMonthlyStats = async () => {
  try {
    const [year, month] = selectedMonth.value.split('-')
    const { data } = await attendanceApi.getMonthlyStats(parseInt(year), parseInt(month))
    Object.assign(monthlyStats, data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadTodayRecord()
  loadAttendanceRecords()
  loadMonthlyStats()
  
  // 定时更新当前时间
  setInterval(() => {
    // 触发响应式更新
  }, 1000)
})
</script>

<style scoped>
.attendance-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 今日打卡区域 */
.today-section {
  margin-bottom: 24px;
}

.today-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.today-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.date-info h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  margin-bottom: 8px;
}

.date-info p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.weather-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  opacity: 0.9;
}

.attendance-actions {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 24px;
}

.action-card {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.action-card:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.action-card.checked-in,
.action-card.checked-out {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
}

.action-icon {
  margin-bottom: 16px;
  opacity: 0.9;
}

.action-content h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  margin-bottom: 8px;
}

.action-content p {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
  margin-bottom: 16px;
}

.action-btn {
  width: 100%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.work-hours-card {
  display: flex;
  align-items: center;
  justify-content: center;
}

.work-hours-content {
  text-align: center;
}

.hours-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  margin-bottom: 16px;
}

.hours-display {
  margin-bottom: 16px;
}

.hours {
  font-size: 36px;
  font-weight: 700;
}

.unit {
  font-size: 16px;
  margin-left: 4px;
  opacity: 0.8;
}

/* 统计数据区域 */
.statistics-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.primary { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.success { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-icon.warning { background: linear-gradient(135deg, #fa709a, #fee140); }
.stat-icon.info { background: linear-gradient(135deg, #a8edea, #fed6e3); }

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
}

.stat-subtitle {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 考勤记录区域 */
.records-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.records-table {
  margin-bottom: 24px;
}

.date-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.date-text {
  font-weight: 500;
}

.weekday-text {
  font-size: 12px;
  color: #999;
}

.late-time {
  color: #f56565;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

/* 对话框 */
.dialog-footer {
  display: flex;
  gap: 12px;
}

/* 详情内容 */
.detail-content {
  padding: 16px 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item label {
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.detail-item span {
  font-size: 16px;
  color: #1a1a1a;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .attendance-actions {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .attendance-container {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .today-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>