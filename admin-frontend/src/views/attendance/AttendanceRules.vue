<template>
  <div class="attendance-rules-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">考勤规则</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增规则
      </el-button>
    </div>

    <!-- 考勤规则列表 -->
    <el-card class="table-card">
      <el-table :data="rules" v-loading="loading" stripe>
        <el-table-column prop="id" label="规则ID" width="80" />
        <el-table-column prop="name" label="规则名称" width="150" />
        <el-table-column prop="workStartTime" label="上班时间" width="120" />
        <el-table-column prop="workEndTime" label="下班时间" width="120" />
        <el-table-column prop="lateThreshold" label="迟到阈值" width="100" />
        <el-table-column prop="earlyThreshold" label="早退阈值" width="100" />
        <el-table-column prop="applicableDepartments" label="适用部门" min-width="200" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewRule(row)">查看</el-button>
            <el-button link type="primary" @click="editRule(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑规则对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="isEdit ? '编辑规则' : '新增规则'" 
      width="600px"
      @close="resetForm"
    >
      <el-form :model="ruleForm" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上班时间" prop="workStartTime">
              <el-time-picker
                v-model="ruleForm.workStartTime"
                placeholder="请选择上班时间"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下班时间" prop="workEndTime">
              <el-time-picker
                v-model="ruleForm.workEndTime"
                placeholder="请选择下班时间"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="迟到阈值" prop="lateThreshold">
              <el-input-number v-model="ruleForm.lateThreshold" :min="1" :max="60" />
              <span style="margin-left: 8px; color: #999;">分钟</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="早退阈值" prop="earlyThreshold">
              <el-input-number v-model="ruleForm.earlyThreshold" :min="1" :max="60" />
              <span style="margin-left: 8px; color: #999;">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适用部门" prop="applicableDepartmentIds">
          <el-select 
            v-model="ruleForm.applicableDepartmentIds" 
            multiple 
            placeholder="请选择适用部门" 
            style="width: 100%"
          >
            <el-option 
              v-for="dept in departments" 
              :key="dept.id" 
              :label="dept.name" 
              :value="dept.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="ruleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="规则描述">
          <el-input 
            v-model="ruleForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入规则描述" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRule" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const rules = ref([])
const departments = ref([])

const showAddDialog = ref(false)
const isEdit = ref(false)

// 表单数据
const ruleForm = reactive({
  id: null,
  name: '',
  workStartTime: '09:00',
  workEndTime: '18:00',
  lateThreshold: 15,
  earlyThreshold: 15,
  applicableDepartmentIds: [],
  status: 1,
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入规则名称', trigger: 'blur' }
  ],
  workStartTime: [
    { required: true, message: '请选择上班时间', trigger: 'blur' }
  ],
  workEndTime: [
    { required: true, message: '请选择下班时间', trigger: 'blur' }
  ],
  applicableDepartmentIds: [
    { required: true, message: '请选择适用部门', trigger: 'change' }
  ]
}

const formRef = ref(null)

// 加载考勤规则
const loadRules = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    rules.value = [
      {
        id: 1,
        name: '标准工作制',
        workStartTime: '09:00',
        workEndTime: '18:00',
        lateThreshold: 15,
        earlyThreshold: 15,
        applicableDepartments: '技术部, 市场部, 销售部',
        status: 1,
        description: '标准朝九晚六工作制度'
      },
      {
        id: 2,
        name: '弹性工作制',
        workStartTime: '10:00',
        workEndTime: '19:00',
        lateThreshold: 30,
        earlyThreshold: 30,
        applicableDepartments: '设计部, 产品部',
        status: 1,
        description: '弹性上下班时间'
      }
    ]
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
    { id: 4, name: '财务部' },
    { id: 5, name: '设计部' },
    { id: 6, name: '产品部' }
  ]
}

// 查看规则详情
const viewRule = (rule) => {
  ElMessage.info('查看功能开发中...')
}

// 编辑规则
const editRule = (rule) => {
  isEdit.value = true
  Object.assign(ruleForm, { 
    ...rule,
    applicableDepartmentIds: [1, 2, 3] // 模拟部门ID数组
  })
  showAddDialog.value = true
}

// 删除规则
const deleteRule = async (rule) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除规则 ${rule.name} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    loadRules()
  } catch (error) {
    // 用户取消删除
  }
}

// 保存规则
const saveRule = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    // 模拟保存API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    showAddDialog.value = false
    loadRules()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(ruleForm, {
    id: null,
    name: '',
    workStartTime: '09:00',
    workEndTime: '18:00',
    lateThreshold: 15,
    earlyThreshold: 15,
    applicableDepartmentIds: [],
    status: 1,
    description: ''
  })
  isEdit.value = false
}

// 组件挂载
onMounted(() => {
  loadRules()
  loadDepartments()
})
</script>

<style scoped>
.attendance-rules-container {
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
</style>