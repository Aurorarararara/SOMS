<template>
  <div class="employees-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.employees') }}</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('employees.addEmployee') }}
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('employees.employeeName') + ':'">
          <el-input v-model="searchForm.realName" :placeholder="$t('employees.enterEmployeeName')" clearable />
        </el-form-item>
        <el-form-item :label="$t('employees.department') + ':'">
          <el-select v-model="searchForm.departmentId" :placeholder="$t('employees.selectDepartment')" clearable>
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('employees.employeeStatus') + ':'">
          <el-select v-model="searchForm.status" :placeholder="$t('employees.selectStatus')" clearable>
            <el-option :label="$t('employees.active')" :value="1" />
            <el-option :label="$t('employees.inactive')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadEmployees">{{ $t('common.search') }}</el-button>
          <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 员工列表 -->
    <el-card class="table-card">
      <el-table :data="employees" v-loading="loading" stripe>
        <el-table-column prop="id" :label="$t('employees.employeeId')" width="80" />
        <el-table-column prop="employeeCode" :label="$t('employees.employeeCode')" width="120" />
        <el-table-column prop="realName" :label="$t('employees.name')" width="100" />
        <el-table-column prop="email" :label="$t('employees.email')" width="180" />
        <el-table-column prop="phone" :label="$t('employees.phone')" width="130" />
        <el-table-column prop="departmentName" :label="$t('employees.department')" width="120" />
        <el-table-column prop="position" :label="$t('employees.position')" width="120" />
        <el-table-column :label="$t('employees.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? $t('employees.active') : $t('employees.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="entryDate" label="入职日期" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewEmployee(row)">查看</el-button>
            <el-button link type="primary" @click="editEmployee(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteEmployee(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadEmployees"
          @current-change="loadEmployees"
        />
      </div>
    </el-card>

    <!-- 新增/编辑员工对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="isEdit ? '编辑员工' : '新增员工'" 
      width="600px"
      @close="resetForm"
    >
      <el-form :model="employeeForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工工号" prop="employeeCode">
              <el-input v-model="employeeForm.employeeCode" placeholder="请输入员工工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="employeeForm.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="employeeForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="employeeForm.phone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门" prop="departmentId">
              <el-select v-model="employeeForm.departmentId" placeholder="请选择部门" style="width: 100%">
                <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="employeeForm.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入职日期" prop="entryDate">
              <el-date-picker
                v-model="employeeForm.entryDate"
                type="date"
                placeholder="请选择入职日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工状态" prop="status">
              <el-select v-model="employeeForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="在职" :value="1" />
                <el-option label="离职" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEmployee" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 员工详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="员工详情" width="500px">
      <div class="employee-detail" v-if="selectedEmployee">
        <div class="detail-item">
          <label>员工工号：</label>
          <span>{{ selectedEmployee.employeeCode }}</span>
        </div>
        <div class="detail-item">
          <label>姓名：</label>
          <span>{{ selectedEmployee.realName }}</span>
        </div>
        <div class="detail-item">
          <label>邮箱：</label>
          <span>{{ selectedEmployee.email }}</span>
        </div>
        <div class="detail-item">
          <label>电话：</label>
          <span>{{ selectedEmployee.phone }}</span>
        </div>
        <div class="detail-item">
          <label>部门：</label>
          <span>{{ selectedEmployee.departmentName }}</span>
        </div>
        <div class="detail-item">
          <label>职位：</label>
          <span>{{ selectedEmployee.position }}</span>
        </div>
        <div class="detail-item">
          <label>入职日期：</label>
          <span>{{ selectedEmployee.entryDate }}</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <el-tag :type="selectedEmployee.status === 1 ? 'success' : 'danger'">
            {{ selectedEmployee.status === 1 ? '在职' : '离职' }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi, departmentApi } from '@/api/employee'

const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const employees = ref([])
const departments = ref([])
const total = ref(0)

const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const selectedEmployee = ref(null)

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 20
})

// 表单数据
const searchForm = reactive({
  realName: '',
  departmentId: '',
  status: ''
})

const employeeForm = reactive({
  id: null,
  employeeCode: '',
  realName: '',
  email: '',
  phone: '',
  departmentId: '',
  position: '',
  entryDate: '',
  status: 1
})

// 表单验证规则
const formRules = {
  employeeCode: [
    { required: true, message: '请输入员工工号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  position: [
    { required: true, message: '请输入职位', trigger: 'blur' }
  ],
  entryDate: [
    { required: true, message: '请选择入职日期', trigger: 'change' }
  ]
}

const formRef = ref(null)

// 加载员工列表
const loadEmployees = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize,
      ...searchForm
    }
    
    const response = await employeeApi.getEmployeeList(params)
    if (response.data) {
      employees.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载员工列表失败:', error)
    ElMessage.error('加载员工列表失败')
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
    realName: '',
    departmentId: '',
    status: ''
  })
  loadEmployees()
}

// 查看员工详情
const viewEmployee = (employee) => {
  selectedEmployee.value = employee
  showDetailDialog.value = true
}

// 编辑员工
const editEmployee = (employee) => {
  isEdit.value = true
  Object.assign(employeeForm, { ...employee })
  showAddDialog.value = true
}

// 删除员工
const deleteEmployee = async (employee) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除员工 ${employee.realName} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用真实删除API
    await employeeApi.deleteEmployee(employee.id)
    ElMessage.success('删除成功')
    loadEmployees()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除员工失败:', error)
      ElMessage.error('删除员工失败')
    }
  }
}

// 保存员工
const saveEmployee = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    // 调用真实API
    if (isEdit.value) {
      // 更新员工
      await employeeApi.updateEmployee(employeeForm.id, employeeForm)
      ElMessage.success('更新成功')
    } else {
      // 创建员工
      await employeeApi.createEmployee(employeeForm)
      ElMessage.success('新增成功')
    }
    
    showAddDialog.value = false
    loadEmployees()
  } catch (error) {
    console.error('保存员工失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(employeeForm, {
    id: null,
    employeeCode: '',
    realName: '',
    email: '',
    phone: '',
    departmentId: '',
    position: '',
    entryDate: '',
    status: 1
  })
  isEdit.value = false
}

// 组件挂载
onMounted(() => {
  loadEmployees()
  loadDepartments()
})
</script>

<style scoped>
.employees-container {
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

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.employee-detail {
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