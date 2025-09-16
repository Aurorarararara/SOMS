<template>
  <div class="expense-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Money /></el-icon>
          报销管理
        </h1>
        <p class="page-subtitle">管理您的报销申请</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建申请
        </el-button>
        <el-button @click="exportApplications">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon draft">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.draft }}</div>
            <div class="stat-label">草稿</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.pending }}</div>
            <div class="stat-label">待审批</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon approved">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.approved }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon rejected">
            <el-icon><Close /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ statistics.rejected }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索 -->
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="searchQuery"
            placeholder="搜索申请标题或编号"
            style="width: 300px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filterStatus" placeholder="申请状态" style="width: 150px" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </div>
        <div class="filter-right">
          <el-button @click="resetFilters">重置筛选</el-button>
        </div>
      </div>

      <!-- 申请列表 -->
      <div class="application-list">
        <el-table :data="filteredApplications" v-loading="loading" style="width: 100%">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="applicationNumber" label="申请编号" width="150" />
          <el-table-column prop="title" label="申请标题" min-width="200" />
          <el-table-column prop="totalAmount" label="申请金额" width="120">
            <template #default="scope">
              <span class="amount">¥{{ scope.row.totalAmount.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
              <el-button 
                v-if="scope.row.status === 'DRAFT'"
                size="small" 
                type="primary" 
                @click="editApplication(scope.row)"
              >
                编辑
              </el-button>
              <el-button 
                v-if="scope.row.status === 'DRAFT'"
                size="small" 
                type="success" 
                @click="submitApplication(scope.row)"
              >
                提交
              </el-button>
              <el-button 
                v-if="scope.row.status === 'PENDING'"
                size="small" 
                type="warning" 
                @click="withdrawApplication(scope.row)"
              >
                撤回
              </el-button>
              <el-button 
                v-if="scope.row.status === 'DRAFT'"
                size="small" 
                type="danger" 
                @click="deleteApplication(scope.row)"
              >
                删除
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
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 创建/编辑申请对话框 -->
    <CreateExpenseDialog
      v-model="showCreateDialog"
      :application-data="editingApplication"
      @success="handleCreateSuccess"
    />

    <!-- 申请详情对话框 -->
    <ExpenseDetailDialog
      v-model="showDetailDialog"
      :application-id="selectedApplicationId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Money, Plus, Download, Edit, Clock, Check, Close, Search } from '@element-plus/icons-vue'
import expenseApi from '@/api/expense'
import CreateExpenseDialog from './components/CreateExpenseDialog.vue'
import ExpenseDetailDialog from './components/ExpenseDetailDialog.vue'

// 响应式数据
const loading = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const editingApplication = ref(null)
const selectedApplicationId = ref(null)

// 统计数据
const statistics = reactive({
  draft: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

// 申请列表
const applications = ref([])

// 计算属性
const filteredApplications = computed(() => {
  let filtered = applications.value
  
  if (searchQuery.value) {
    filtered = filtered.filter(app => 
      app.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      app.applicationNumber.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }
  
  if (filterStatus.value) {
    filtered = filtered.filter(app => app.status === filterStatus.value)
  }
  
  if (dateRange.value && dateRange.value.length === 2) {
    const [startDate, endDate] = dateRange.value
    filtered = filtered.filter(app => {
      const createTime = new Date(app.createTime)
      return createTime >= startDate && createTime <= endDate
    })
  }
  
  return filtered
})

// 方法
const loadApplications = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    const response = await expenseApi.getEmployeeApplications(params)
    applications.value = response.data.records
    total.value = response.data.total
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await expenseApi.getExpenseStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const viewDetail = (application) => {
  selectedApplicationId.value = application.id
  showDetailDialog.value = true
}

const editApplication = (application) => {
  editingApplication.value = application
  showCreateDialog.value = true
}

const submitApplication = async (application) => {
  try {
    await ElMessageBox.confirm('确定要提交此申请吗？提交后将无法修改。', '确认提交', {
      type: 'warning'
    })
    
    await expenseApi.submitApplication(application.id)
    ElMessage.success('申请提交成功')
    loadApplications()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交申请失败')
    }
  }
}

const withdrawApplication = async (application) => {
  try {
    await ElMessageBox.confirm('确定要撤回此申请吗？', '确认撤回', {
      type: 'warning'
    })
    
    await expenseApi.withdrawApplication(application.id)
    ElMessage.success('申请撤回成功')
    loadApplications()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回申请失败')
    }
  }
}

const deleteApplication = async (application) => {
  try {
    await ElMessageBox.confirm('确定要删除此申请吗？此操作不可恢复。', '确认删除', {
      type: 'warning'
    })
    
    await expenseApi.deleteApplication(application.id)
    ElMessage.success('申请删除成功')
    loadApplications()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除申请失败')
    }
  }
}

const exportApplications = async () => {
  try {
    const response = await expenseApi.exportApplications()
    // 处理文件下载
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `报销申请_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  dateRange.value = []
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadApplications()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadApplications()
}

const handleCreateSuccess = () => {
  showCreateDialog.value = false
  editingApplication.value = null
  loadApplications()
  loadStatistics()
}

const getStatusType = (status) => {
  const statusMap = {
    'DRAFT': '',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  return new Date(date).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadApplications()
  loadStatistics()
})
</script>

<style scoped>
.expense-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.title-icon {
  margin-right: 12px;
  color: #409eff;
}

.page-subtitle {
  color: #909399;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.content-container {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.stat-icon.draft {
  background: #909399;
}

.stat-icon.pending {
  background: #e6a23c;
}

.stat-icon.approved {
  background: #67c23a;
}

.stat-icon.rejected {
  background: #f56c6c;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.application-list {
  margin-top: 20px;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>