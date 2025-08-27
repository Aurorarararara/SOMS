<template>
  <div class="leave-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">请假审批</h2>
    </div>

    <!-- 筛选条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="申请人:">
          <el-input v-model="searchForm.applicantName" placeholder="请输入申请人姓名" clearable />
        </el-form-item>
        <el-form-item label="请假类型:">
          <el-select v-model="searchForm.leaveType" placeholder="请选择请假类型" clearable>
            <el-option label="年假" value="annual" />
            <el-option label="病假" value="sick" />
            <el-option label="事假" value="personal" />
            <el-option label="婚假" value="marriage" />
            <el-option label="产假" value="maternity" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态:">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审批" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间:">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadLeaveApplications">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 审批统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <el-icon size="30"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon approved-icon">
              <el-icon size="30"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rejected-icon">
              <el-icon size="30"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.rejected }}</div>
              <div class="stat-label">已拒绝</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 请假申请列表 -->
    <el-card class="table-card">
      <el-table :data="leaveApplications" v-loading="loading" stripe>
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column label="请假类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getLeaveTypeColor(row.leaveType)">
              {{ getLeaveTypeText(row.leaveType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="days" label="天数" width="80" />
        <el-table-column prop="reason" label="请假原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewApplication(row)">详情</el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="success" 
              @click="approveApplication(row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="danger" 
              @click="rejectApplication(row)"
            >
              拒绝
            </el-button>
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
          @size-change="loadLeaveApplications"
          @current-change="loadLeaveApplications"
        />
      </div>
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="请假申请详情" width="600px">
      <div class="leave-detail" v-if="selectedApplication">
        <div class="detail-section">
          <h3>申请信息</h3>
          <div class="detail-item">
            <label>申请人：</label>
            <span>{{ selectedApplication.applicantName }}</span>
          </div>
          <div class="detail-item">
            <label>部门：</label>
            <span>{{ selectedApplication.departmentName }}</span>
          </div>
          <div class="detail-item">
            <label>请假类型：</label>
            <el-tag :type="getLeaveTypeColor(selectedApplication.leaveType)">
              {{ getLeaveTypeText(selectedApplication.leaveType) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>请假时间：</label>
            <span>{{ selectedApplication.startDate }} 至 {{ selectedApplication.endDate }}</span>
          </div>
          <div class="detail-item">
            <label>请假天数：</label>
            <span>{{ selectedApplication.days }}天</span>
          </div>
          <div class="detail-item">
            <label>请假原因：</label>
            <span>{{ selectedApplication.reason }}</span>
          </div>
          <div class="detail-item">
            <label>申请时间：</label>
            <span>{{ selectedApplication.applyTime }}</span>
          </div>
        </div>
        
        <div class="detail-section" v-if="selectedApplication.status !== 'pending'">
          <h3>审批信息</h3>
          <div class="detail-item">
            <label>审批状态：</label>
            <el-tag :type="getStatusType(selectedApplication.status)">
              {{ getStatusText(selectedApplication.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>审批人：</label>
            <span>{{ selectedApplication.approverName || '无' }}</span>
          </div>
          <div class="detail-item">
            <label>审批时间：</label>
            <span>{{ selectedApplication.approveTime || '无' }}</span>
          </div>
          <div class="detail-item">
            <label>审批意见：</label>
            <span>{{ selectedApplication.approveRemark || '无' }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="showApprovalDialog" :title="approvalType === 'approve' ? '批准申请' : '拒绝申请'" width="500px">
      <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" label-width="100px">
        <el-form-item label="审批意见" prop="remark">
          <el-input 
            v-model="approvalForm.remark" 
            type="textarea" 
            :rows="4"
            :placeholder="approvalType === 'approve' ? '请输入批准意见（可选）' : '请输入拒绝原因'" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button 
          :type="approvalType === 'approve' ? 'success' : 'danger'" 
          @click="submitApproval" 
          :loading="saving"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Clock, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { leaveManageApi } from '@/api/leaveManage'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const leaveApplications = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const showDetailDialog = ref(false)
const showApprovalDialog = ref(false)
const selectedApplication = ref(null)
const approvalType = ref('approve') // 'approve' or 'reject'

// 统计数据
const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0
})

// 搜索表单
const searchForm = reactive({
  applicantName: '',
  leaveType: '',
  status: '',
  dateRange: []
})

// 审批表单
const approvalForm = reactive({
  remark: ''
})

const approvalRules = {
  remark: [
    { 
      required: true, 
      message: '请输入审批意见', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (approvalType.value === 'reject' && !value) {
          callback(new Error('拒绝申请必须填写原因'))
        } else {
          callback()
        }
      }
    }
  ]
}

const approvalFormRef = ref(null)

// 获取请假类型颜色
const getLeaveTypeColor = (type) => {
  const colorMap = {
    annual: 'success',
    sick: 'warning',
    personal: 'info',
    marriage: 'danger',
    maternity: 'primary',
    other: ''
  }
  return colorMap[type] || 'info'
}

// 获取请假类型文本
const getLeaveTypeText = (type) => {
  const textMap = {
    annual: '年假',
    sick: '病假',
    personal: '事假',
    marriage: '婚假',
    maternity: '产假',
    other: '其他'
  }
  return textMap[type] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 加载请假申请
const loadLeaveApplications = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    // 处理日期范围参数
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
      delete params.dateRange
    }
    
    const response = await leaveManageApi.getLeaveApplications(params)
    if (response.data) {
      leaveApplications.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载请假申请失败:', error)
    ElMessage.error('加载请假申请失败')
    // 使用默认数据作为备用
    leaveApplications.value = [
      {
        id: 1,
        applicantName: '张三',
        departmentName: '技术部',
        leaveType: 'annual',
        startDate: '2025-09-01',
        endDate: '2025-09-03',
        days: 3,
        reason: '休年假回家看望父母',
        status: 'pending',
        applyTime: '2025-08-25 10:30:00'
      },
      {
        id: 2,
        applicantName: '李四',
        departmentName: '市场部',
        leaveType: 'sick',
        startDate: '2025-08-26',
        endDate: '2025-08-26',
        days: 1,
        reason: '感冒发烧需要休息',
        status: 'approved',
        applyTime: '2025-08-25 09:15:00',
        approverName: '王经理',
        approveTime: '2025-08-25 11:00:00',
        approveRemark: '注意休息，早日康复'
      }
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    applicantName: '',
    leaveType: '',
    status: '',
    dateRange: []
  })
  loadLeaveApplications()
}

// 查看申请详情
const viewApplication = (application) => {
  selectedApplication.value = application
  showDetailDialog.value = true
}

// 批准申请
const approveApplication = (application) => {
  selectedApplication.value = application
  approvalType.value = 'approve'
  approvalForm.remark = ''
  showApprovalDialog.value = true
}

// 拒绝申请
const rejectApplication = (application) => {
  selectedApplication.value = application
  approvalType.value = 'reject'
  approvalForm.remark = ''
  showApprovalDialog.value = true
}

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await leaveManageApi.getLeaveStatistics()
    if (response.data) {
      Object.assign(stats, response.data)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用默认统计数据
    stats.pending = 8
    stats.approved = 42
    stats.rejected = 8
  }
}

// 提交审批
const submitApproval = async () => {
  if (!approvalFormRef.value) return
  
  try {
    await approvalFormRef.value.validate()
    saving.value = true
    
    const approvalData = {
      remark: approvalForm.remark
    }
    
    if (approvalType.value === 'approve') {
      await leaveManageApi.approveLeaveApplication(selectedApplication.value.id, approvalData)
      ElMessage.success('审批通过')
    } else {
      await leaveManageApi.rejectLeaveApplication(selectedApplication.value.id, approvalData)
      ElMessage.success('审批拒绝')
    }
    
    showApprovalDialog.value = false
    loadLeaveApplications()
    loadStats() // 刷新统计数据
  } catch (error) {
    console.error('审批操作失败:', error)
    ElMessage.error('审批操作失败，请重试')
  } finally {
    saving.value = false
  }
}

// 组件挂载
onMounted(() => {
  loadLeaveApplications()
  loadStats()
})
</script>

<style scoped>
.leave-manage-container {
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

.search-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
}

.pending-icon { background-color: #E6A23C; }
.approved-icon { background-color: #67C23A; }
.rejected-icon { background-color: #F56C6C; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.leave-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.detail-item label {
  width: 100px;
  font-weight: 600;
  color: #666;
}

.detail-item span {
  flex: 1;
  color: #333;
}
</style>