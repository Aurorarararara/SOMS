<template>
  <div class="expense-approval">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>报销审批</h2>
      <p>处理待审批的报销申请</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.pendingCount }}</div>
                <div class="stats-label">待审批</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon approved">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.approvedCount }}</div>
                <div class="stats-label">今日已审批</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon amount">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">¥{{ stats.totalAmount?.toFixed(2) || '0.00' }}</div>
                <div class="stats-label">待审批金额</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon urgent">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ stats.urgentCount }}</div>
                <div class="stats-label">紧急申请</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速筛选 -->
    <div class="filter-section">
      <el-card>
        <el-form :model="filterForm" inline>
          <el-form-item label="优先级">
            <el-select
              v-model="filterForm.priority"
              placeholder="请选择优先级"
              clearable
              style="width: 150px"
            >
              <el-option label="紧急" value="HIGH" />
              <el-option label="普通" value="NORMAL" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-form-item>
          <el-form-item label="金额范围">
            <el-select
              v-model="filterForm.amountRange"
              placeholder="请选择金额范围"
              clearable
              style="width: 180px"
            >
              <el-option label="1000元以下" value="0-1000" />
              <el-option label="1000-5000元" value="1000-5000" />
              <el-option label="5000-10000元" value="5000-10000" />
              <el-option label="10000元以上" value="10000-999999" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请时间">
            <el-select
              v-model="filterForm.timeRange"
              placeholder="请选择时间范围"
              clearable
              style="width: 150px"
            >
              <el-option label="今天" value="today" />
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">
              <el-icon><Search /></el-icon>
              筛选
            </el-button>
            <el-button @click="handleResetFilter">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 待审批列表 -->
    <div class="approval-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>待审批申请</span>
            <div class="header-actions">
              <el-button 
                type="success" 
                :disabled="selectedItems.length === 0"
                @click="handleBatchApprove"
              >
                <el-icon><Check /></el-icon>
                批量通过
              </el-button>
              <el-button 
                type="danger" 
                :disabled="selectedItems.length === 0"
                @click="handleBatchReject"
              >
                <el-icon><Close /></el-icon>
                批量拒绝
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="申请编号" width="120" />
          <el-table-column prop="applicantName" label="申请人" width="100" />
          <el-table-column prop="departmentName" label="部门" width="120" />
          <el-table-column prop="title" label="报销标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="totalAmount" label="报销金额" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getPriorityType(row.priority)">{{ getPriorityText(row.priority) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="waitingDays" label="等待天数" width="100" align="center">
            <template #default="{ row }">
              <span :class="{ 'urgent-days': row.waitingDays > 3 }">
                {{ row.waitingDays }}天
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button type="success" size="small" @click="handleApprove(row)">
                通过
              </el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <ExpenseDetailDialog
      v-model="detailDialogVisible"
      :expense-id="selectedExpenseId"
      :is-admin="true"
      :show-approval="true"
      @refresh="loadData"
    />

    <!-- 审批对话框 -->
    <ApprovalDialog
      v-model="approvalDialogVisible"
      :expense-ids="approvalExpenseIds"
      :approval-type="approvalType"
      @confirm="handleApprovalConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Check, Close, Clock, Money, Warning 
} from '@element-plus/icons-vue'
import ExpenseDetailDialog from '@/components/expense/ExpenseDetailDialog.vue'
import ApprovalDialog from '@/components/expense/ApprovalDialog.vue'
import expenseApi from '@/api/expense'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedItems = ref([])
const detailDialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const selectedExpenseId = ref(null)
const approvalExpenseIds = ref([])
const approvalType = ref('approve') // 'approve' | 'reject'

// 统计数据
const stats = reactive({
  pendingCount: 0,
  approvedCount: 0,
  totalAmount: 0,
  urgentCount: 0
})

// 筛选表单
const filterForm = reactive({
  priority: '',
  amountRange: '',
  timeRange: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 优先级映射
const priorityMap = {
  HIGH: { text: '紧急', type: 'danger' },
  NORMAL: { text: '普通', type: '' },
  LOW: { text: '低', type: 'info' }
}

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      priority: filterForm.priority || undefined,
      minAmount: getAmountRange().min,
      maxAmount: getAmountRange().max,
      timeRange: filterForm.timeRange || undefined
    }
    
    const response = await expenseApi.getPendingApplications(params)
    tableData.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
    // 计算等待天数
    tableData.value.forEach(item => {
      const createdDate = new Date(item.createdAt)
      const today = new Date()
      item.waitingDays = Math.floor((today - createdDate) / (1000 * 60 * 60 * 24))
    })
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await expenseApi.getApprovalWorkload()
    Object.assign(stats, response.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const getAmountRange = () => {
  if (!filterForm.amountRange) return { min: undefined, max: undefined }
  
  const [min, max] = filterForm.amountRange.split('-').map(Number)
  return { min, max }
}

const handleFilter = () => {
  pagination.page = 1
  loadData()
}

const handleResetFilter = () => {
  Object.assign(filterForm, {
    priority: '',
    amountRange: '',
    timeRange: ''
  })
  pagination.page = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const handleView = (row) => {
  selectedExpenseId.value = row.id
  detailDialogVisible.value = true
}

const handleApprove = (row) => {
  approvalExpenseIds.value = [row.id]
  approvalType.value = 'approve'
  approvalDialogVisible.value = true
}

const handleReject = (row) => {
  approvalExpenseIds.value = [row.id]
  approvalType.value = 'reject'
  approvalDialogVisible.value = true
}

const handleBatchApprove = () => {
  approvalExpenseIds.value = selectedItems.value.map(item => item.id)
  approvalType.value = 'approve'
  approvalDialogVisible.value = true
}

const handleBatchReject = () => {
  approvalExpenseIds.value = selectedItems.value.map(item => item.id)
  approvalType.value = 'reject'
  approvalDialogVisible.value = true
}

const handleApprovalConfirm = () => {
  loadData()
  loadStats()
  selectedItems.value = []
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

const getPriorityType = (priority) => {
  return priorityMap[priority]?.type || ''
}

const getPriorityText = (priority) => {
  return priorityMap[priority]?.text || priority
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.expense-approval {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stats-section {
  margin-bottom: 20px;
}

.stats-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stats-icon.pending {
  background: linear-gradient(135deg, #E6A23C, #F7BA2A);
}

.stats-icon.approved {
  background: linear-gradient(135deg, #67C23A, #85CE61);
}

.stats-icon.amount {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
}

.stats-icon.urgent {
  background: linear-gradient(135deg, #F56C6C, #F78989);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #909399;
}

.filter-section {
  margin-bottom: 20px;
}

.approval-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.amount {
  font-weight: 600;
  color: #E6A23C;
}

.urgent-days {
  color: #F56C6C;
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>