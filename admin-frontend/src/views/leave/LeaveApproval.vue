<template>
  <div class="leave-approval-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.leaveApproval') }}</h2>
      <div class="header-actions">
        <el-badge :value="pendingCount" class="pending-badge">
          <el-button type="primary" @click="showPendingOnly = !showPendingOnly">
            <el-icon><Bell /></el-icon>
            {{ showPendingOnly ? $t('leaveApproval.showAll') : $t('leaveApproval.pending') }}
          </el-button>
        </el-badge>
      </div>
    </div>

    <!-- 快速统计 -->
    <div class="quick-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card pending">
            <div class="stat-content">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">{{ $t('leaveApproval.pending') }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card approved">
            <div class="stat-content">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">{{ $t('leaveApproval.approved') }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card rejected">
            <div class="stat-content">
              <div class="stat-value">{{ stats.rejected }}</div>
              <div class="stat-label">{{ $t('leaveApproval.rejected') }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card total">
            <div class="stat-content">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label">{{ $t('leaveApproval.totalApplications') }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 审批列表 -->
    <el-card class="table-card">
      <el-table :data="approvals" v-loading="loading" stripe>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" :label="$t('leaveApproval.applicationId')" width="80" />
        <el-table-column prop="applicantName" :label="$t('leaveApproval.applicant')" width="100" />
        <el-table-column prop="departmentName" :label="$t('leaveApproval.department')" width="120" />
        <el-table-column :label="$t('leaveApproval.leaveType')" width="100">
          <template #default="{ row }">
            <el-tag :type="getLeaveTypeColor(row.leaveType)">
              {{ getLeaveTypeText(row.leaveType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" :label="$t('leaveApproval.leaveDuration')" width="200" />
        <el-table-column prop="days" :label="$t('leaveApproval.days')" width="80" align="center" />
        <el-table-column :label="$t('leaveApproval.priority')" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityColor(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" :label="$t('leaveApproval.applicationTime')" width="160" />
        <el-table-column :label="$t('leaveApproval.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.actions')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetails(row)">{{ $t('leaveApproval.details') }}</el-button>
            <el-button
              link
              type="success"
              v-if="row.status === 'pending'"
              @click="quickApprove(row)"
            >
              {{ $t('leaveApproval.approve') }}
            </el-button>
            <el-button
              link
              type="warning"
              v-if="row.status === 'pending'"
              @click="quickReject(row)"
            >
              {{ $t('leaveApproval.reject') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions">
        <el-button 
          type="success" 
          :disabled="selectedIds.length === 0"
          @click="batchApprove"
        >
          批量通过
        </el-button>
        <el-button 
          type="warning" 
          :disabled="selectedIds.length === 0"
          @click="batchReject"
        >
          批量拒绝
        </el-button>
        <span class="selected-info" v-if="selectedIds.length > 0">
          已选择 {{ selectedIds.length }} 项
        </span>
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="审批详情" width="700px">
      <div class="approval-detail" v-if="currentApproval">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="申请信息" name="info">
            <div class="approval-info">
              <div class="info-item">
                <label>申请人：</label>
                <span>{{ currentApproval.applicantName }}</span>
              </div>
              <div class="info-item">
                <label>部门：</label>
                <span>{{ currentApproval.departmentName }}</span>
              </div>
              <div class="info-item">
                <label>请假类型：</label>
                <el-tag :type="getLeaveTypeColor(currentApproval.leaveType)">
                  {{ getLeaveTypeText(currentApproval.leaveType) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>请假时间：</label>
                <span>{{ currentApproval.duration }}</span>
              </div>
              <div class="info-item">
                <label>请假天数：</label>
                <span>{{ currentApproval.days }}天</span>
              </div>
              <div class="info-item">
                <label>请假原因：</label>
                <span>{{ currentApproval.reason }}</span>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="审批流程" name="process">
            <el-timeline class="approval-timeline">
              <el-timeline-item
                v-for="step in approvalProcess"
                :key="step.id"
                :timestamp="step.time"
                :type="step.type"
              >
                <div class="timeline-content">
                  <div class="step-title">{{ step.title }}</div>
                  <div class="step-desc">{{ step.description }}</div>
                  <div class="step-operator" v-if="step.operator">
                    操作人：{{ step.operator }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
        
        <div class="approval-actions" v-if="currentApproval.status === 'pending'">
          <el-form :model="approvalForm" label-width="80px">
            <el-form-item label="审批意见：">
              <el-input
                v-model="approvalForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入审批意见（可选）"
              />
            </el-form-item>
          </el-form>
          <div class="action-buttons">
            <el-button type="success" @click="handleApprove">通过申请</el-button>
            <el-button type="warning" @click="handleReject">拒绝申请</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const approvals = ref([])
const showPendingOnly = ref(false)
const selectedIds = ref([])
const showDetailDialog = ref(false)
const currentApproval = ref(null)
const activeTab = ref('info')

// 统计数据
const stats = reactive({
  pending: 12,
  approved: 45,
  rejected: 8,
  total: 65
})

// 审批表单
const approvalForm = reactive({
  comment: ''
})

// 审批流程
const approvalProcess = ref([])

// 计算属性
const pendingCount = computed(() => stats.pending)

// 加载审批列表
const loadApprovals = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    approvals.value = [
      {
        id: 1,
        applicantName: '张三',
        departmentName: '技术部',
        leaveType: 'sick',
        duration: '2024-01-20 至 2024-01-22',
        days: 3,
        priority: 'normal',
        applyTime: '01-18 14:30',
        status: 'pending',
        reason: '感冒发烧，需要休息治疗'
      },
      {
        id: 2,
        applicantName: '李四',
        departmentName: '市场部',
        leaveType: 'personal',
        duration: '2024-01-25',
        days: 1,
        priority: 'low',
        applyTime: '01-20 09:15',
        status: 'pending',
        reason: '家里有事需要处理'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 获取请假类型相关方法
const getLeaveTypeColor = (type) => {
  const colorMap = {
    sick: 'warning',
    personal: 'info',
    annual: 'success',
    marriage: 'danger'
  }
  return colorMap[type] || ''
}

const getLeaveTypeText = (type) => {
  const textMap = {
    sick: '病假',
    personal: '事假',
    annual: '年假',
    marriage: '婚假'
  }
  return textMap[type] || '未知'
}

// 获取优先级相关方法
const getPriorityColor = (priority) => {
  const colorMap = {
    high: 'danger',
    normal: 'warning',
    low: 'info'
  }
  return colorMap[priority] || ''
}

const getPriorityText = (priority) => {
  const textMap = {
    high: '高',
    normal: '中',
    low: '低'
  }
  return textMap[priority] || '未知'
}

// 获取状态相关方法
const getStatusColor = (status) => {
  const colorMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return colorMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 查看详情
const viewDetails = async (approval) => {
  currentApproval.value = approval
  
  // 模拟加载审批流程
  approvalProcess.value = [
    {
      id: 1,
      title: '提交申请',
      description: '员工提交请假申请',
      time: '2024-01-18 14:30:00',
      operator: approval.applicantName,
      type: 'primary'
    },
    {
      id: 2,
      title: '等待审批',
      description: '申请已提交，等待审批人处理',
      time: '2024-01-18 14:31:00',
      type: 'warning'
    }
  ]
  
  showDetailDialog.value = true
}

// 快速通过
const quickApprove = async (approval) => {
  try {
    await ElMessageBox.confirm(
      `确定通过 ${approval.applicantName} 的请假申请吗？`,
      '审批确认',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    ElMessage.success('申请已通过')
    loadApprovals()
  } catch (error) {
    // 用户取消操作
  }
}

// 快速拒绝
const quickReject = async (approval) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `拒绝 ${approval.applicantName} 的请假申请，请输入拒绝理由：`,
      '审批确认',
      {
        confirmButtonText: '确定拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝理由'
      }
    )
    
    ElMessage.success('申请已拒绝')
    loadApprovals()
  } catch (error) {
    // 用户取消操作
  }
}

// 批量通过
const batchApprove = () => {
  ElMessage.info('批量通过功能开发中...')
}

// 批量拒绝
const batchReject = () => {
  ElMessage.info('批量拒绝功能开发中...')
}

// 处理审批
const handleApprove = () => {
  ElMessage.success('申请已通过')
  showDetailDialog.value = false
  loadApprovals()
}

const handleReject = () => {
  if (!approvalForm.comment.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  ElMessage.success('申请已拒绝')
  showDetailDialog.value = false
  loadApprovals()
}

// 组件挂载
onMounted(() => {
  loadApprovals()
})
</script>

<style scoped>
.leave-approval-container {
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

.pending-badge :deep(.el-badge__content) {
  border: none;
}

.quick-stats {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.pending {
  border-left: 4px solid #e6a23c;
}

.stat-card.approved {
  border-left: 4px solid #67c23a;
}

.stat-card.rejected {
  border-left: 4px solid #f56c6c;
}

.stat-card.total {
  border-left: 4px solid #409eff;
}

.stat-content {
  padding: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.table-card {
  margin-bottom: 20px;
}

.batch-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-info {
  color: #666;
  font-size: 14px;
}

.approval-detail {
  padding: 10px 0;
}

.approval-info {
  padding: 0 10px;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.info-item label {
  width: 100px;
  font-weight: 600;
  color: #666;
}

.info-item span {
  flex: 1;
  color: #333;
}

.approval-timeline {
  margin: 10px 0;
}

.timeline-content {
  padding-left: 10px;
}

.step-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.step-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.step-operator {
  color: #999;
  font-size: 12px;
}

.approval-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.action-buttons {
  text-align: center;
  margin-top: 15px;
}

.action-buttons .el-button {
  margin: 0 8px;
}
</style>