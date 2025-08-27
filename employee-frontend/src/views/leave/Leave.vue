<template>
  <div class="leave-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">请假管理</h1>
          <p class="page-subtitle">申请请假并查看申请记录</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showLeaveDialog = true">
            <el-icon><Plus /></el-icon>
            申请请假
          </el-button>
        </div>
      </div>
    </div>

    <!-- 快捷统计区域 -->
    <div class="statistics-section">
      <div class="stats-grid">
        <div class="stat-card pending">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">待审批</div>
            <div class="stat-value">{{ leaveStats.pending }}</div>
            <div class="stat-subtitle">申请</div>
          </div>
        </div>

        <div class="stat-card approved">
          <div class="stat-icon">
            <el-icon><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">已批准</div>
            <div class="stat-value">{{ leaveStats.approved }}</div>
            <div class="stat-subtitle">申请</div>
          </div>
        </div>

        <div class="stat-card rejected">
          <div class="stat-icon">
            <el-icon><CloseBold /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">已拒绝</div>
            <div class="stat-value">{{ leaveStats.rejected }}</div>
            <div class="stat-subtitle">申请</div>
          </div>
        </div>

        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">本年累计</div>
            <div class="stat-value">{{ leaveStats.totalDays }}</div>
            <div class="stat-subtitle">天</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 请假记录 -->
    <div class="records-section">
      <div class="records-card">
        <div class="card-header">
          <h3>请假记录</h3>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="loadLeaveRecords">
              <el-option label="待审批" :value="0" />
              <el-option label="已批准" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </div>
        </div>

        <div class="records-table">
          <el-table :data="leaveRecords" v-loading="loading" height="400">
            <el-table-column prop="leaveType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getLeaveTypeTagType(row.leaveType)">
                  {{ row.leaveType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column prop="days" label="天数" width="80">
              <template #default="{ row }">{{ row.days }} 天</template>
            </el-table-column>
            <el-table-column prop="reason" label="请假事由" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" @click="viewDetail(row)" size="small">详情</el-button>
                <el-button text type="warning" @click="editLeave(row)" size="small" v-if="row.status === 0">修改</el-button>
                <el-button text type="danger" @click="cancelLeave(row)" size="small" v-if="row.status === 0">撤销</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 请假申请对话框 -->
    <el-dialog v-model="showLeaveDialog" title="申请请假" width="600px" @close="resetLeaveForm">
      <el-form ref="leaveFormRef" :model="leaveForm" :rules="leaveRules" label-width="100px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="leaveForm.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="年假" value="年假" />
            <el-option label="病假" value="病假" />
            <el-option label="事假" value="事假" />
            <el-option label="婚假" value="婚假" />
            <el-option label="产假" value="产假" />
          </el-select>
        </el-form-item>
        <el-form-item label="请假时间" prop="dateRange">
          <el-date-picker
            v-model="leaveForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input
            v-model="leaveForm.reason"
            type="textarea"
            rows="4"
            placeholder="请详细说明请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showLeaveDialog = false">取消</el-button>
          <el-button type="primary" @click="submitLeave" :loading="submitting">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Clock, SuccessFilled, CloseBold, DataAnalysis } from '@element-plus/icons-vue'
import { leaveApi } from '@/api/leave'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showLeaveDialog = ref(false)
const leaveRecords = ref([])
const filterStatus = ref('')

const leaveFormRef = ref()
const leaveForm = reactive({
  leaveType: '',
  dateRange: [],
  reason: ''
})

const leaveRules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择请假时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }]
}

const leaveStats = reactive({
  pending: 2,
  approved: 8,
  rejected: 1,
  totalDays: 15
})

// 方法
const submitLeave = async () => {
  try {
    await leaveFormRef.value?.validate()
    submitting.value = true

    const data = {
      leaveType: leaveForm.leaveType,
      startDate: leaveForm.dateRange[0],
      endDate: leaveForm.dateRange[1],
      reason: leaveForm.reason
    }

    await leaveApi.submitLeave(data)
    ElMessage.success('提交请假申请成功！')
    showLeaveDialog.value = false
    loadLeaveRecords()
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  } finally {
    submitting.value = false
  }
}

const resetLeaveForm = () => {
  leaveForm.leaveType = ''
  leaveForm.dateRange = []
  leaveForm.reason = ''
  leaveFormRef.value?.resetFields()
}

const viewDetail = (leave) => {
  ElMessage.info('查看详情功能开发中...')
}

const editLeave = (leave) => {
  ElMessage.info('修改功能开发中...')
}

const cancelLeave = async (leave) => {
  try {
    await ElMessageBox.confirm('确认撤销此请假申请吗？', '撤销申请', {
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await leaveApi.cancelLeave(leave.id)
    ElMessage.success('撤销申请成功！')
    loadLeaveRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销失败，请重试')
    }
  }
}

const getLeaveTypeTagType = (type) => {
  const types = { '年假': 'success', '病假': 'warning', '事假': 'info', '婚假': 'danger', '产假': 'primary' }
  return types[type] || 'info'
}

const getStatusTagType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审批', 1: '已批准', 2: '已拒绝' }
  return texts[status] || '未知'
}

const loadLeaveRecords = async () => {
  try {
    loading.value = true
    const { data } = await leaveApi.getLeaveRecords({ status: filterStatus.value })
    leaveRecords.value = data.records || []
  } catch (error) {
    console.error('加载请假记录失败:', error)
    leaveRecords.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLeaveRecords()
})
</script>

<style scoped>
.leave-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

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
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-card.pending::before { background: linear-gradient(90deg, #faad14, #ffc53d); }
.stat-card.approved::before { background: linear-gradient(90deg, #52c41a, #73d13d); }
.stat-card.rejected::before { background: linear-gradient(90deg, #f5222d, #ff4d4f); }
.stat-card.total::before { background: linear-gradient(90deg, #1890ff, #40a9ff); }

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

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
  margin-bottom: 4px;
}

.stat-subtitle {
  font-size: 12px;
  color: #999;
}

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

.dialog-footer {
  display: flex;
  gap: 12px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .leave-container {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>