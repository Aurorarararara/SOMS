<template>
  <div class="attendance-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">考勤管理</h2>
      <el-button type="primary" @click="exportAttendance">
        <el-icon><Download /></el-icon>
        导出考勤
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="员工姓名:">
          <el-input v-model="searchForm.employeeName" placeholder="请输入员工姓名" clearable />
        </el-form-item>
        <el-form-item label="所属部门:">
          <el-select v-model="searchForm.departmentId" placeholder="请选择部门" clearable>
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤日期:">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="考勤状态:">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="迟到" value="late" />
            <el-option label="早退" value="early" />
            <el-option label="缺勤" value="absent" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAttendanceRecords">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 考勤统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon normal-icon">
              <el-icon size="30"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.normal }}</div>
              <div class="stat-label">正常出勤</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon late-icon">
              <el-icon size="30"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.late }}</div>
              <div class="stat-label">迟到</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon early-icon">
              <el-icon size="30"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.early }}</div>
              <div class="stat-label">早退</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon absent-icon">
              <el-icon size="30"><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.absent }}</div>
              <div class="stat-label">缺勤</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 考勤记录 -->
    <el-card class="table-card">
      <el-table :data="attendanceRecords" v-loading="loading" stripe>
        <el-table-column prop="employeeCode" label="员工工号" width="120" />
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="attendanceDate" label="考勤日期" width="120" />
        <el-table-column prop="checkInTime" label="签到时间" width="120" />
        <el-table-column prop="checkOutTime" label="签退时间" width="120" />
        <el-table-column prop="workHours" label="工作时长" width="100" />
        <el-table-column label="考勤状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewRecord(row)">详情</el-button>
            <el-button link type="primary" @click="editRecord(row)">编辑</el-button>
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
          @size-change="loadAttendanceRecords"
          @current-change="loadAttendanceRecords"
        />
      </div>
    </el-card>

    <!-- 考勤详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="考勤详情" width="500px">
      <div class="attendance-detail" v-if="selectedRecord">
        <div class="detail-item">
          <label>员工：</label>
          <span>{{ selectedRecord.employeeName }}（{{ selectedRecord.employeeCode }}）</span>
        </div>
        <div class="detail-item">
          <label>部门：</label>
          <span>{{ selectedRecord.departmentName }}</span>
        </div>
        <div class="detail-item">
          <label>考勤日期：</label>
          <span>{{ selectedRecord.attendanceDate }}</span>
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

    <!-- 编辑考勤对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑考勤" width="500px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="签到时间" prop="checkInTime">
          <el-time-picker
            v-model="editForm.checkInTime"
            placeholder="选择签到时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="签退时间" prop="checkOutTime">
          <el-time-picker
            v-model="editForm.checkOutTime"
            placeholder="选择签退时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="考勤状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" value="normal" />
            <el-option label="迟到" value="late" />
            <el-option label="早退" value="early" />
            <el-option label="缺勤" value="absent" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="editForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRecord" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Download, CircleCheck, Warning, Clock, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { attendanceApi } from '@/api/attendance'
import { departmentApi } from '@/api/employee'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const attendanceRecords = ref([])
const departments = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const selectedRecord = ref(null)

// 统计数据
const stats = reactive({
  normal: 0,
  late: 0,
  early: 0,
  absent: 0
})

// 搜索表单
const searchForm = reactive({
  employeeName: '',
  departmentId: '',
  dateRange: [],
  status: ''
})

// 编辑表单
const editForm = reactive({
  id: null,
  checkInTime: '',
  checkOutTime: '',
  status: '',
  remark: ''
})

const editRules = {
  status: [
    { required: true, message: '请选择考勤状态', trigger: 'change' }
  ]
}

const editFormRef = ref(null)

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    normal: 'success',
    late: 'warning',
    early: 'info',
    absent: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    normal: '正常',
    late: '迟到',
    early: '早退',
    absent: '缺勤'
  }
  return textMap[status] || '未知'
}

// 加载考勤记录
const loadAttendanceRecords = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    const response = await attendanceApi.getAttendanceRecords(params)
    if (response.data) {
      attendanceRecords.value = response.data.records || []
      total.value = response.data.total || 0
      
      // 获取统计数据
      const statsResponse = await attendanceApi.getAttendanceStatistics({
        startDate: searchForm.dateRange?.[0],
        endDate: searchForm.dateRange?.[1],
        departmentId: searchForm.departmentId
      })
      
      if (statsResponse.data) {
        stats.normal = statsResponse.data.normalCount || 0
        stats.late = statsResponse.data.lateCount || 0
        stats.early = statsResponse.data.earlyCount || 0
        stats.absent = statsResponse.data.absentCount || 0
      }
    }
  } catch (error) {
    console.error('加载考勤记录失败:', error)
    ElMessage.error('加载考勤记录失败')
    // 使用默认数据作为备选
    attendanceRecords.value = [
      {
        id: 1,
        employeeCode: 'EMP001',
        employeeName: '张三',
        departmentName: '技术部',
        attendanceDate: '2025-08-25',
        checkInTime: '09:00:00',
        checkOutTime: '18:00:00',
        workHours: '8小时',
        status: 'normal',
        remark: ''
      }
    ]
    total.value = 1
    stats.normal = 1
    stats.late = 0
    stats.early = 0
    stats.absent = 0
  } finally {
    loading.value = false
  }
}

// 加载部门列表
const loadDepartments = async () => {
  try {
    const response = await departmentApi.getDepartmentList()
    if (response.data) {
      departments.value = response.data
    }
  } catch (error) {
    console.error('加载部门列表失败:', error)
    // 使用默认部门数据
    departments.value = [
      { id: 1, name: '技术部' },
      { id: 2, name: '市场部' },
      { id: 3, name: '销售部' },
      { id: 4, name: '财务部' },
      { id: 5, name: '人事部' },
      { id: 6, name: '行政部' }
    ]
  }
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    employeeName: '',
    departmentId: '',
    dateRange: [],
    status: ''
  })
  loadAttendanceRecords()
}

// 查看考勤详情
const viewRecord = (record) => {
  selectedRecord.value = record
  showDetailDialog.value = true
}

// 编辑考勤记录
const editRecord = (record) => {
  Object.assign(editForm, {
    id: record.id,
    checkInTime: record.checkInTime,
    checkOutTime: record.checkOutTime,
    status: record.status,
    remark: record.remark
  })
  showEditDialog.value = true
}

// 保存考勤记录
const saveRecord = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    saving.value = true
    
    // 调用真实API
    await attendanceApi.updateAttendanceRecord(editForm.id, editForm)
    
    ElMessage.success('保存成功')
    showEditDialog.value = false
    loadAttendanceRecords()
  } catch (error) {
    console.error('保存考勤记录失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 导出考勤数据
const exportAttendance = async () => {
  try {
    const response = await attendanceApi.exportAttendanceRecords(searchForm)
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.style.display = 'none'
    link.href = url
    link.setAttribute('download', `考勤记录_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出考勤数据失败:', error)
    ElMessage.error('导出失败')
  }
}

// 组件挂载
onMounted(() => {
  loadAttendanceRecords()
  loadDepartments()
})
</script>

<style scoped>
.attendance-manage-container {
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

.normal-icon { background-color: #67C23A; }
.late-icon { background-color: #E6A23C; }
.early-icon { background-color: #409EFF; }
.absent-icon { background-color: #F56C6C; }

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

.attendance-detail {
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
</style>