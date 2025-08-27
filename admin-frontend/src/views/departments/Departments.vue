<template>
  <div class="departments-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">部门管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增部门
      </el-button>
    </div>

    <!-- 部门列表 -->
    <el-card class="table-card">
      <el-table :data="departments" v-loading="loading" stripe>
        <el-table-column prop="id" label="部门ID" width="80" />
        <el-table-column prop="name" label="部门名称" width="200" />
        <el-table-column prop="description" label="部门描述" min-width="300" />
        <el-table-column prop="managerName" label="部门经理" width="120" />
        <el-table-column prop="employeeCount" label="员工数量" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDepartment(row)">查看</el-button>
            <el-button link type="primary" @click="editDepartment(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteDepartment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑部门对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="isEdit ? '编辑部门' : '新增部门'" 
      width="500px"
      @close="resetForm"
    >
      <el-form :model="departmentForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门描述" prop="description">
          <el-input 
            v-model="departmentForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入部门描述" 
          />
        </el-form-item>
        <el-form-item label="部门经理" prop="managerId">
          <el-select v-model="departmentForm.managerId" placeholder="请选择部门经理" style="width: 100%">
            <el-option 
              v-for="employee in employees" 
              :key="employee.id" 
              :label="employee.realName" 
              :value="employee.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="departmentForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDepartment" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 部门详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="部门详情" width="600px">
      <div class="department-detail" v-if="selectedDepartment">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-item">
            <label>部门名称：</label>
            <span>{{ selectedDepartment.name }}</span>
          </div>
          <div class="detail-item">
            <label>部门描述：</label>
            <span>{{ selectedDepartment.description }}</span>
          </div>
          <div class="detail-item">
            <label>部门经理：</label>
            <span>{{ selectedDepartment.managerName }}</span>
          </div>
          <div class="detail-item">
            <label>员工数量：</label>
            <span>{{ selectedDepartment.employeeCount }}人</span>
          </div>
          <div class="detail-item">
            <label>状态：</label>
            <el-tag :type="selectedDepartment.status === 1 ? 'success' : 'danger'">
              {{ selectedDepartment.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>部门员工</h3>
          <el-table :data="departmentEmployees" size="small">
            <el-table-column prop="employeeCode" label="工号" width="100" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="position" label="职位" width="120" />
            <el-table-column prop="entryDate" label="入职日期" width="120" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { departmentApi, employeeApi } from '@/api/employee'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const departments = ref([])
const employees = ref([])
const departmentEmployees = ref([])

const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const selectedDepartment = ref(null)

// 表单数据
const departmentForm = reactive({
  id: null,
  name: '',
  description: '',
  managerId: '',
  status: 1
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入部门描述', trigger: 'blur' }
  ]
}

const formRef = ref(null)

// 加载部门列表
const loadDepartments = async () => {
  loading.value = true
  try {
    const response = await departmentApi.getDepartmentList()
    if (response.data) {
      departments.value = response.data
    }
  } catch (error) {
    console.error('加载部门列表失败:', error)
    ElMessage.error('加载部门列表失败')
    // 使用默认部门数据作为备用
    departments.value = [
      {
        id: 1,
        name: '技术部',
        description: '负责公司产品的技术开发和维护',
        managerId: 1,
        managerName: '王技术',
        employeeCount: 35,
        status: 1,
        createTime: '2023-01-01 10:00:00'
      },
      {
        id: 2,
        name: '市场部',
        description: '负责公司产品的市场推广和品牌建设',
        managerId: 2,
        managerName: '李市场',
        employeeCount: 28,
        status: 1,
        createTime: '2023-01-01 10:00:00'
      },
      {
        id: 3,
        name: '销售部',
        description: '负责公司产品的销售和客户关系维护',
        managerId: 3,
        managerName: '张销售',
        employeeCount: 25,
        status: 1,
        createTime: '2023-01-01 10:00:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 加载员工列表（用于选择部门经理）
const loadEmployees = async () => {
  try {
    const response = await employeeApi.getEmployeeList({ status: 1 })
    if (response.data && response.data.records) {
      employees.value = response.data.records
    }
  } catch (error) {
    console.error('加载员工列表失败:', error)
    // 使用默认员工数据作为备用
    employees.value = [
      { id: 1, realName: '王技术' },
      { id: 2, realName: '李市场' },
      { id: 3, realName: '张销售' },
      { id: 4, realName: '赵财务' },
      { id: 5, realName: '钱人事' },
      { id: 6, realName: '孙行政' }
    ]
  }
}

// 查看部门详情
const viewDepartment = async (department) => {
  selectedDepartment.value = department
  
  // 加载部门员工
  try {
    const response = await departmentApi.getDepartmentEmployees(department.id)
    if (response.data) {
      departmentEmployees.value = response.data
    }
  } catch (error) {
    console.error('加载部门员工失败:', error)
    // 使用默认数据作为备用
    departmentEmployees.value = [
      {
        employeeCode: 'EMP001',
        realName: '员工A',
        position: '高级工程师',
        entryDate: '2023-01-15'
      },
      {
        employeeCode: 'EMP002',
        realName: '员工B',
        position: '工程师',
        entryDate: '2023-03-20'
      }
    ]
  }
  
  showDetailDialog.value = true
}

// 编辑部门
const editDepartment = (department) => {
  isEdit.value = true
  Object.assign(departmentForm, { ...department })
  showAddDialog.value = true
}

// 删除部门
const deleteDepartment = async (department) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门 ${department.name} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await departmentApi.deleteDepartment(department.id)
    ElMessage.success('删除成功')
    loadDepartments()
  } catch (error) {
    if (error.message) {
      console.error('删除部门失败:', error)
      ElMessage.error('删除失败: ' + error.message)
    }
    // 用户取消删除时不显示错误
  }
}

// 保存部门
const saveDepartment = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    if (isEdit.value) {
      await departmentApi.updateDepartment(departmentForm.id, departmentForm)
      ElMessage.success('更新成功')
    } else {
      await departmentApi.createDepartment(departmentForm)
      ElMessage.success('新增成功')
    }
    
    showAddDialog.value = false
    loadDepartments()
  } catch (error) {
    console.error('保存部门失败:', error)
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(departmentForm, {
    id: null,
    name: '',
    description: '',
    managerId: '',
    status: 1
  })
  isEdit.value = false
}

// 组件挂载
onMounted(() => {
  loadDepartments()
  loadEmployees()
})
</script>

<style scoped>
.departments-container {
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

.table-card {
  margin-bottom: 20px;
}

.department-detail {
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