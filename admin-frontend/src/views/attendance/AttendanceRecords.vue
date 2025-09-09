<template>
  <div class="attendance-records-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">考勤记录</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportRecords">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
        <el-button type="success" @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          导入记录
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="员工姓名:">
          <el-input v-model="searchForm.employeeName" placeholder="请输入员工姓名" clearable />
        </el-form-item>
        <el-form-item label="部门:">
          <el-select v-model="searchForm.departmentId" placeholder="请选择部门" clearable>
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤状态:">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="迟到" value="late" />
            <el-option label="早退" value="early" />
            <el-option label="旷工" value="absent" />
            <el-option label="请假" value="leave" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围:">
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
          <el-button type="primary" @click="loadRecords">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 考勤记录列表 -->
    <el-card class="table-card">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="employeeCode" label="工号" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="checkInTime" label="签到时间" width="180" />
        <el-table-column prop="checkOutTime" label="签退时间" width="180" />
        <el-table-column prop="workHours" label="工作时长" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="location" label="打卡地点" min-width="150">
          <template #default="{ row }">
            <div v-if="row.location" class="location-item">
              <el-icon class="location-icon"><Location /></el-icon>
              {{ row.location }}
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewRecord(row)">查看</el-button>
            <el-button link type="warning" @click="editRecord(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteRecord(row)">删除</el-button>
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
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-card>

    <!-- 记录详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="考勤记录详情" width="600px">
      <div class="record-detail" v-if="selectedRecord">
        <div class="detail-item">
          <label>员工信息：</label>
          <span>{{ selectedRecord.employeeName }} ({{ selectedRecord.employeeCode }})</span>
        </div>
        <div class="detail-item">
          <label>部门：</label>
          <span>{{ selectedRecord.departmentName }}</span>
        </div>
        <div class="detail-item">
          <label>日期：</label>
          <span>{{ selectedRecord.date }}</span>
        </div>
        <div class="detail-item">
          <label>签到时间：</label>
          <span>{{ selectedRecord.checkInTime || '未签到' }}</span>
        </div>
        <div class="detail-item">
          <label>签退时间：</label>
          <span>{{ selectedRecord.checkOutTime || '未签退' }}</span>
        </div>
        <div class="detail-item">
          <label>工作时长：</label>
          <span>{{ selectedRecord.workHours || '0小时' }}</span>
        </div>
        <div class="detail-item">
          <label>打卡地点：</label>
          <span>{{ selectedRecord.location || '无' }}</span>
        </div>
        <div class="detail-item">
          <label>考勤状态：</label>
          <el-tag :type="getStatusType(selectedRecord.status)">
            {{ getStatusText(selectedRecord.status) }}
          </el-tag>
        </div>
        <div class="detail-item">
          <label>备注：</label>
          <span>{{ selectedRecord.remark || '无' }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Download, Upload, Location } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const records = ref([])
const departments = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const showDetailDialog = ref(false)
const showImportDialog = ref(false)
const selectedRecord = ref(null)

// 表单数据
const searchForm = reactive({
  employeeName: '',
  departmentId: '',
  status: '',
  dateRange: []
})

// 加载考勤记录
const loadRecords = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    records.value = [
      {
        id: 1,
        employeeName: '张三',
        employeeCode: 'EMP001',
        departmentName: '技术部',
        checkInTime: '2024-01-15 09:00:00',
        checkOutTime: '2024-01-15 18:30:00',
        workHours: '8.5小时',
        status: 'normal',
        date: '2024-01-15',
        location: '公司大厦A座',
        remark: ''
      },
      {
        id: 2,
        employeeName: '李四',
        employeeCode: 'EMP002',
        departmentName: '市场部',
        checkInTime: '2024-01-15 09:15:00',
        checkOutTime: '2024-01-15 18:00:00',
        workHours: '7.75小时',
        status: 'late',
        date: '2024-01-15',
        location: '公司大厦B座',
        remark: '迟到15分钟'
      }
    ]
    total.value = 156
  } finally {
    loading.value = false
  }
}

// 加载部门列表
const loadDepartments = async () => {
  departments.value = [
    { id: 1, name: '技术部' },
    { id: 2, name: '市场部' },
    { id: 3, name: '销售部' },
    { id: 4, name: '财务部' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    normal: 'success',
    late: 'warning',
    early: 'warning', 
    absent: 'danger',
    leave: 'info'
  }
  return statusMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    normal: '正常',
    late: '迟到',
    early: '早退',
    absent: '旷工',
    leave: '请假'
  }
  return statusMap[status] || '未知'
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    employeeName: '',
    departmentId: '',
    status: '',
    dateRange: []
  })
  loadRecords()
}

// 查看记录详情
const viewRecord = (record) => {
  selectedRecord.value = record
  showDetailDialog.value = true
}

// 编辑记录
const editRecord = (record) => {
  ElMessage.info('编辑功能开发中...')
}

// 删除记录
const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${record.employeeName} 在 ${record.date} 的考勤记录吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    loadRecords()
  } catch (error) {
    // 用户取消删除
  }
}

// 导出记录
const exportRecords = () => {
  ElMessage.success('导出功能开发中...')
}

// 组件挂载
onMounted(() => {
  loadRecords()
  loadDepartments()
})
</script>

<style scoped>
.attendance-records-container {
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

.record-detail {
  padding: 10px 0;
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

.location-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.location-icon {
  font-size: 12px;
  color: #999;
}
</style>