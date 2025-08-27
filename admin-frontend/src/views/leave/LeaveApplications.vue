<template>
  <div class="leave-applications-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">请假申请</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportApplications">
          <el-icon><Download /></el-icon>
          导出申请
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="申请人:">
          <el-input v-model="searchForm.applicantName" placeholder="请输入申请人姓名" clearable />
        </el-form-item>
        <el-form-item label="请假类型:">
          <el-select v-model="searchForm.leaveType" placeholder="请选择请假类型" clearable>
            <el-option label="病假" value="sick" />
            <el-option label="事假" value="personal" />
            <el-option label="年假" value="annual" />
            <el-option label="婚假" value="marriage" />
            <el-option label="产假" value="maternity" />
            <el-option label="丧假" value="bereavement" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请状态:">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审批" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="已撤销" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请日期:">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadApplications">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请列表 -->
    <el-card class="table-card">
      <el-table :data="applications" v-loading="loading" stripe>
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="employeeCode" label="工号" width="100" />
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
        <el-table-column prop="days" label="请假天数" width="100" align="center" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column prop="reason" label="请假原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewApplication(row)">查看详情</el-button>
            <el-button 
              link 
              type="info" 
              v-if="row.workflowInstanceId"
              @click="viewWorkflowProgress(row)"
            >
              流程进度
            </el-button>
            <el-button 
              link 
              type="warning" 
              v-if="row.status === 'PENDING' && row.canWithdraw"
              @click="withdrawApplication(row)"
            >
              撤回申请
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
          @size-change="loadApplications"
          @current-change="loadApplications"
        />
      </div>
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="请假申请详情" width="800px">
      <div class="application-detail" v-if="selectedApplication">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- 申请信息选项卡 -->
          <el-tab-pane label="申请信息" name="info">
            <div class="detail-section">
              <h3>申请信息</h3>
              <div class="detail-item">
                <label>申请人：</label>
                <span>{{ selectedApplication.applicantName }} ({{ selectedApplication.employeeCode }})</span>
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
                <label>开始日期：</label>
                <span>{{ selectedApplication.startDate }}</span>
              </div>
              <div class="detail-item">
                <label>结束日期：</label>
                <span>{{ selectedApplication.endDate }}</span>
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
                <label>申请状态：</label>
                <el-tag :type="getStatusColor(selectedApplication.status)">
                  {{ getStatusText(selectedApplication.status) }}
                </el-tag>
              </div>
            </div>
            
            <div class="detail-section" v-if="selectedApplication.attachments?.length">
              <h3>附件</h3>
              <div class="attachments-list">
                <div 
                  v-for="file in selectedApplication.attachments" 
                  :key="file.id"
                  class="attachment-item"
                >
                  <el-icon><Document /></el-icon>
                  <span>{{ file.name }}</span>
                  <el-button link type="primary" @click="downloadFile(file)">下载</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 审批流程选项卡 -->
          <el-tab-pane label="审批流程" name="workflow" v-if="selectedApplication.workflowInstanceId">
            <div class="workflow-section">
              <div class="workflow-info">
                <p><strong>流程实例 ID:</strong> {{ selectedApplication.workflowInstanceId }}</p>
                <p><strong>当前状态:</strong> 
                  <el-tag :type="getStatusColor(selectedApplication.status)">
                    {{ getStatusText(selectedApplication.status) }}
                  </el-tag>
                </p>
              </div>
              
              <div class="workflow-progress" v-if="workflowHistory.length > 0">
                <h4>审批进度</h4>
                <el-timeline>
                  <el-timeline-item
                    v-for="(item, index) in workflowHistory"
                    :key="index"
                    :timestamp="formatTime(item.createTime)"
                    placement="top"
                    :color="getWorkflowStatusColor(item.action)"
                  >
                    <el-card>
                      <div class="timeline-content">
                        <div class="timeline-header">
                          <span class="timeline-action">{{ item.actionName }}</span>
                          <span class="timeline-operator">{{ item.operatorName || '系统' }}</span>
                        </div>
                        <div v-if="item.comment" class="timeline-comment">
                          {{ item.comment }}
                        </div>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
              
              <div v-else class="no-workflow">
                <el-empty description="正在加载流程信息..." />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Download, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInstanceHistory, withdrawInstance } from '@/api/workflow'
import { useUserStore } from '@/stores/user'

// 响应式数据
const loading = ref(false)
const applications = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const showDetailDialog = ref(false)
const selectedApplication = ref(null)
const activeTab = ref('info')
const workflowHistory = ref([])
const userStore = useUserStore()

// 表单数据
const searchForm = reactive({
  applicantName: '',
  leaveType: '',
  status: '',
  dateRange: []
})

// 加载申请列表
const loadApplications = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    applications.value = [
      {
        id: 1,
        applicantName: '张三',
        employeeCode: 'EMP001',
        departmentName: '技术部',
        leaveType: 'sick',
        startDate: '2024-01-20',
        endDate: '2024-01-22',
        days: 3,
        status: 'PENDING',
        reason: '感冒发烧，需要休息治疗',
        applyTime: '2024-01-18 14:30:00',
        workflowInstanceId: 'wf-instance-001',
        canWithdraw: true,
        attachments: [
          { id: 1, name: '医院诊断证明.pdf' }
        ]
      },
      {
        id: 2,
        applicantName: '李四',
        employeeCode: 'EMP002',
        departmentName: '市场部',
        leaveType: 'personal',
        startDate: '2024-01-25',
        endDate: '2024-01-25',
        days: 1,
        status: 'COMPLETED',
        reason: '家里有事需要处理',
        applyTime: '2024-01-20 09:15:00',
        workflowInstanceId: 'wf-instance-002',
        canWithdraw: false,
        attachments: []
      },
      {
        id: 3,
        applicantName: '王五',
        employeeCode: 'EMP003',
        departmentName: '财务部',
        leaveType: 'annual',
        startDate: '2024-02-01',
        endDate: '2024-02-03',
        days: 3,
        status: 'REJECTED',
        reason: '年假旅游',
        applyTime: '2024-01-22 16:45:00',
        workflowInstanceId: 'wf-instance-003',
        canWithdraw: false,
        attachments: []
      }
    ]
    total.value = 89
  } finally {
    loading.value = false
  }
}

// 获取请假类型颜色
const getLeaveTypeColor = (type) => {
  const colorMap = {
    sick: 'warning',
    personal: 'info',
    annual: 'success',
    marriage: 'danger',
    maternity: 'primary',
    bereavement: 'info'
  }
  return colorMap[type] || ''
}

// 获取请假类型文本
const getLeaveTypeText = (type) => {
  const textMap = {
    sick: '病假',
    personal: '事假',
    annual: '年假',
    marriage: '婚假',
    maternity: '产假',
    bereavement: '丧假'
  }
  return textMap[type] || '未知'
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    PENDING: 'warning',
    COMPLETED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'info'
  }
  return colorMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    PENDING: '审批中',
    COMPLETED: '已通过',
    REJECTED: '已拒绝',
    WITHDRAWN: '已撤回'
  }
  return textMap[status] || '未知'
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    applicantName: '',
    leaveType: '',
    status: '',
    dateRange: []
  })
  loadApplications()
}

// 查看申请详情
const viewApplication = async (application) => {
  selectedApplication.value = application
  activeTab.value = 'info'
  workflowHistory.value = []
  showDetailDialog.value = true
  
  // 如果有审批流程实例，加载流程历史
  if (application.workflowInstanceId) {
    try {
      const response = await getInstanceHistory(application.workflowInstanceId)
      if (response.success) {
        workflowHistory.value = response.data || []
      }
    } catch (error) {
      console.error('获取流程历史失败:', error)
      ElMessage.warning('获取流程历史失败')
    }
  }
}

// 查看流程进度
const viewWorkflowProgress = async (application) => {
  selectedApplication.value = application
  activeTab.value = 'workflow'
  workflowHistory.value = []
  showDetailDialog.value = true
  
  // 加载流程历史
  if (application.workflowInstanceId) {
    try {
      const response = await getInstanceHistory(application.workflowInstanceId)
      if (response.success) {
        workflowHistory.value = response.data || []
      }
    } catch (error) {
      console.error('获取流程历史失败:', error)
      ElMessage.error('获取流程历史失败')
    }
  }
}

// 撤回申请
const withdrawApplication = async (application) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入撤回原因：',
      '撤回请假申请',
      {
        confirmButtonText: '确定撤回',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入撤回原因',
        inputValidator: (value) => {
          if (!value || value.trim().length < 2) return '请输入至少2个字符的撤回原因'
          return true
        }
      }
    )
    
    if (application.workflowInstanceId) {
      const response = await withdrawInstance(application.workflowInstanceId, {
        userId: userStore.userInfo.id,
        reason: reason
      })
      
      if (response.success) {
        ElMessage.success('请假申请撤回成功')
        loadApplications() // 刷新列表
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回申请失败:', error)
      ElMessage.error('撤回申请失败')
    }
  }
}

// 获取审批流程状态颜色
const getWorkflowStatusColor = (action) => {
  const colorMap = {
    'START': '#409EFF',
    'APPROVED': '#67C23A',
    'REJECTED': '#F56C6C',
    'WITHDRAW': '#E6A23C',
    'COMPLETE': '#67C23A',
    'FORWARD': '#409EFF'
  }
  return colorMap[action] || '#909399'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 下载附件
const downloadFile = (file) => {
  ElMessage.info(`下载文件: ${file.name}`)
}

// 导出申请
const exportApplications = () => {
  ElMessage.success('导出功能开发中...')
}

// 组件挂载
onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.leave-applications-container {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.application-detail {
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

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.attachment-item span {
  flex: 1;
}

/* 审批流程相关样式 */
.detail-tabs {
  margin-top: -16px;
}

.workflow-section {
  padding: 16px 0;
}

.workflow-info {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.workflow-info p {
  margin: 8px 0;
  color: #333;
}

.workflow-progress h4 {
  margin: 16px 0;
  color: #333;
  font-weight: 600;
}

.timeline-content {
  padding: 0;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.timeline-action {
  font-weight: 600;
  color: #333;
}

.timeline-operator {
  color: #666;
  font-size: 14px;
}

.timeline-comment {
  color: #555;
  line-height: 1.6;
  padding: 8px 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
  margin-top: 8px;
}

.no-workflow {
  text-align: center;
  padding: 40px 0;
  color: #999;
}
</style>