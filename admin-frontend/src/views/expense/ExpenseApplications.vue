<template>
  <div class="expense-applications">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>报销申请管理</h2>
      <p>查看和管理所有员工的报销申请</p>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-card>
        <el-form :model="searchForm" inline>
          <el-form-item label="申请人">
            <el-input
              v-model="searchForm.applicantName"
              placeholder="请输入申请人姓名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="部门">
            <el-select
              v-model="searchForm.departmentId"
              placeholder="请选择部门"
              clearable
              style="width: 200px"
            >
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 150px"
            >
              <el-option label="草稿" value="DRAFT" />
              <el-option label="待审批" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已撤回" value="WITHDRAWN" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>报销申请列表</span>
            <div class="header-actions">
              <el-button type="success" @click="handleExport">
                <el-icon><Download /></el-icon>
                导出数据
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
        >
          <el-table-column prop="id" label="申请编号" width="120" />
          <el-table-column prop="applicantName" label="申请人" width="100" />
          <el-table-column prop="departmentName" label="部门" width="120" />
          <el-table-column prop="title" label="报销标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="totalAmount" label="报销金额" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button 
                v-if="row.status === 'PENDING'"
                type="success" 
                size="small" 
                @click="handleApprove(row)"
              >
                审批
              </el-button>
              <el-button 
                v-if="canDelete(row)"
                type="danger" 
                size="small" 
                @click="handleDelete(row)"
              >
                删除
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
            :page-sizes="[10, 20, 50, 100]"
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
      @refresh="loadData"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import ExpenseDetailDialog from '@/components/expense/ExpenseDetailDialog.vue'
import expenseApi from '@/api/expense'
import departmentApi from '@/api/department'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const departments = ref([])
const detailDialogVisible = ref(false)
const selectedExpenseId = ref(null)

// 搜索表单
const searchForm = reactive({
  applicantName: '',
  departmentId: '',
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 状态映射
const statusMap = {
  DRAFT: { text: '草稿', type: 'info' },
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
  WITHDRAWN: { text: '已撤回', type: 'info' }
}

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      applicantName: searchForm.applicantName || undefined,
      departmentId: searchForm.departmentId || undefined,
      status: searchForm.status || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined
    }
    
    const response = await expenseApi.getAllApplications(params)
    tableData.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    const response = await departmentApi.getAllDepartments()
    departments.value = response.data || []
  } catch (error) {
    console.error('加载部门数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    applicantName: '',
    departmentId: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
  loadData()
}

const handleView = (row) => {
  selectedExpenseId.value = row.id
  detailDialogVisible.value = true
}

const handleApprove = (row) => {
  selectedExpenseId.value = row.id
  detailDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除申请编号为 ${row.id} 的报销申请吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await expenseApi.deleteApplication(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleExport = async () => {
  try {
    const params = {
      applicantName: searchForm.applicantName || undefined,
      departmentId: searchForm.departmentId || undefined,
      status: searchForm.status || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined
    }
    
    await expenseApi.exportAllApplications(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
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

const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || status
}

const canDelete = (row) => {
  return row.status === 'DRAFT' || row.status === 'REJECTED'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadData()
  loadDepartments()
})
</script>

<style scoped>
.expense-applications {
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

.search-section {
  margin-bottom: 20px;
}

.table-section {
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>