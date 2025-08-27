<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑审批节点' : '添加审批节点'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="node-form"
    >
      <el-form-item label="节点名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入节点名称，如：部门经理审批"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="节点类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择节点类型" style="width: 100%">
          <el-option label="审批节点" value="APPROVAL" />
          <el-option label="抄送节点" value="CC" />
          <el-option label="条件节点" value="CONDITION" />
        </el-select>
      </el-form-item>

      <el-form-item label="审批人类型" prop="approverType">
        <el-radio-group v-model="form.approverType">
          <el-radio label="USER">指定用户</el-radio>
          <el-radio label="ROLE">指定角色</el-radio>
          <el-radio label="DEPARTMENT">指定部门</el-radio>
          <el-radio label="DYNAMIC">动态指定</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 指定用户 -->
      <el-form-item 
        v-if="form.approverType === 'USER'" 
        label="选择用户" 
        prop="approverIds"
      >
        <el-select
          v-model="form.approverIds"
          multiple
          placeholder="请选择审批用户"
          style="width: 100%"
          :loading="userLoading"
        >
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="user.realName"
            :value="user.id"
          >
            <span>{{ user.realName }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              {{ user.username }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 指定角色 -->
      <el-form-item 
        v-if="form.approverType === 'ROLE'" 
        label="选择角色" 
        prop="approverIds"
      >
        <el-select
          v-model="form.approverIds"
          multiple
          placeholder="请选择审批角色"
          style="width: 100%"
          :loading="roleLoading"
        >
          <el-option
            v-for="role in roles"
            :key="role.id"
            :label="role.name"
            :value="role.id"
          />
        </el-select>
      </el-form-item>

      <!-- 指定部门 -->
      <el-form-item 
        v-if="form.approverType === 'DEPARTMENT'" 
        label="选择部门" 
        prop="approverIds"
      >
        <el-select
          v-model="form.approverIds"
          multiple
          placeholder="请选择审批部门"
          style="width: 100%"
          :loading="departmentLoading"
        >
          <el-option
            v-for="dept in departments"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>

      <!-- 动态指定 -->
      <el-form-item 
        v-if="form.approverType === 'DYNAMIC'" 
        label="动态规则" 
        prop="dynamicRule"
      >
        <el-select v-model="form.dynamicRule" placeholder="请选择动态规则" style="width: 100%">
          <el-option label="申请人直属上级" value="DIRECT_MANAGER" />
          <el-option label="申请人部门负责人" value="DEPARTMENT_HEAD" />
          <el-option label="指定层级上级" value="LEVEL_MANAGER" />
        </el-select>
      </el-form-item>

      <el-form-item label="审批策略" prop="approvalStrategy">
        <el-radio-group v-model="form.approvalStrategy">
          <el-radio label="ANY">任意一人审批通过</el-radio>
          <el-radio label="ALL">所有人都需审批</el-radio>
          <el-radio label="SEQUENTIAL">按顺序依次审批</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="是否必须" prop="required">
        <el-switch
          v-model="form.required"
          active-text="必须审批"
          inactive-text="可选审批"
        />
      </el-form-item>

      <el-form-item label="超时时间" prop="timeoutHours">
        <el-input-number
          v-model="form.timeoutHours"
          :min="1"
          :max="720"
          placeholder="请输入超时小时数"
          style="width: 200px"
        />
        <span style="margin-left: 10px; color: #999;">小时（1-720小时）</span>
      </el-form-item>

      <el-form-item label="超时处理" prop="timeoutAction">
        <el-select v-model="form.timeoutAction" placeholder="请选择超时处理方式" style="width: 100%">
          <el-option label="自动通过" value="AUTO_APPROVE" />
          <el-option label="自动拒绝" value="AUTO_REJECT" />
          <el-option label="转交上级" value="TRANSFER_SUPERIOR" />
          <el-option label="仅提醒" value="REMINDER_ONLY" />
        </el-select>
      </el-form-item>

      <el-form-item label="节点说明" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入节点说明，用于帮助审批人理解审批要求"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ isEdit ? '更新' : '添加' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi, departmentApi } from '@/api/employee'
import { roleApi } from '@/api/role'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  nodeData: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'confirm'])

// 响应式数据
const dialogVisible = ref(false)
const loading = ref(false)
const userLoading = ref(false)
const roleLoading = ref(false)
const departmentLoading = ref(false)

const formRef = ref(null)
const form = reactive({
  name: '',
  type: 'APPROVAL',
  approverType: 'USER',
  approverIds: [],
  dynamicRule: '',
  approvalStrategy: 'ANY',
  required: true,
  timeoutHours: 24,
  timeoutAction: 'REMINDER_ONLY',
  description: '',
  sortOrder: 1
})

// 选项数据
const users = ref([])
const roles = ref([])
const departments = ref([])

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入节点名称', trigger: 'blur' },
    { min: 2, max: 50, message: '节点名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择节点类型', trigger: 'change' }
  ],
  approverType: [
    { required: true, message: '请选择审批人类型', trigger: 'change' }
  ],
  approverIds: [
    { 
      validator: (rule, value, callback) => {
        if (form.approverType !== 'DYNAMIC' && (!value || value.length === 0)) {
          callback(new Error('请选择审批人'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  dynamicRule: [
    { 
      validator: (rule, value, callback) => {
        if (form.approverType === 'DYNAMIC' && !value) {
          callback(new Error('请选择动态规则'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  approvalStrategy: [
    { required: true, message: '请选择审批策略', trigger: 'change' }
  ],
  timeoutHours: [
    { required: true, message: '请输入超时时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 720, message: '超时时间必须在1-720小时之间', trigger: 'blur' }
  ],
  timeoutAction: [
    { required: true, message: '请选择超时处理方式', trigger: 'change' }
  ]
}

// 监听对话框显示状态
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    resetForm()
    if (props.isEdit && props.nodeData) {
      Object.assign(form, props.nodeData)
    }
    loadSelectOptions()
  }
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

// 监听审批人类型变化，重置审批人选择
watch(() => form.approverType, () => {
  form.approverIds = []
  form.dynamicRule = ''
})

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    name: '',
    type: 'APPROVAL',
    approverType: 'USER',
    approverIds: [],
    dynamicRule: '',
    approvalStrategy: 'ANY',
    required: true,
    timeoutHours: 24,
    timeoutAction: 'REMINDER_ONLY',
    description: '',
    sortOrder: 1
  })
}

// 加载选择器选项数据
const loadSelectOptions = async () => {
  await Promise.all([
    loadUsers(),
    loadRoles(),
    loadDepartments()
  ])
}

// 加载用户列表
const loadUsers = async () => {
  userLoading.value = true
  try {
    const response = await employeeApi.getEmployeeList({ size: 1000 })
    if (response.data) {
      users.value = (response.data.records || response.data).map(emp => ({
        id: emp.id,
        realName: emp.realName || emp.name,
        username: emp.employeeCode || emp.username
      }))
    }
  } catch (error) {
    console.error('加载用户失败:', error)
    ElMessage.error('加载用户列表失败')
    // 使用默认数据作为备选
    users.value = [
      { id: 1, realName: '张经理', username: 'manager1' },
      { id: 2, realName: '李主管', username: 'supervisor1' },
      { id: 3, realName: '王总监', username: 'director1' },
      { id: 4, realName: '陈副总', username: 'vp1' }
    ]
  } finally {
    userLoading.value = false
  }
}

// 加载角色列表
const loadRoles = async () => {
  roleLoading.value = true
  try {
    const response = await roleApi.getAllRoles()
    if (response.data) {
      roles.value = Array.isArray(response.data) ? response.data : []
    }
  } catch (error) {
    console.error('加载角色失败:', error)
    ElMessage.error('加载角色列表失败')
    // 使用默认数据作为备选
    roles.value = [
      { id: 1, name: '部门经理' },
      { id: 2, name: '项目主管' },
      { id: 3, name: '财务总监' },
      { id: 4, name: '人事经理' }
    ]
  } finally {
    roleLoading.value = false
  }
}

// 加载部门列表
const loadDepartments = async () => {
  departmentLoading.value = true
  try {
    const response = await departmentApi.getDepartmentList()
    if (response.data) {
      departments.value = Array.isArray(response.data) ? response.data : []
    }
  } catch (error) {
    console.error('加载部门失败:', error)
    ElMessage.error('加载部门列表失败')
    // 使用默认数据作为备选
    departments.value = [
      { id: 1, name: '技术部' },
      { id: 2, name: '人事部' },
      { id: 3, name: '财务部' },
      { id: 4, name: '市场部' },
      { id: 5, name: '行政部' }
    ]
  } finally {
    departmentLoading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 构造提交数据
    const submitData = { ...form }
    
    // 如果是动态规则，清空审批人ID
    if (submitData.approverType === 'DYNAMIC') {
      submitData.approverIds = []
    } else {
      submitData.dynamicRule = ''
    }
    
    emit('confirm', submitData)
    ElMessage.success(props.isEdit ? '节点更新成功' : '节点添加成功')
    handleClose()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 组件挂载时加载数据
onMounted(() => {
  // 预加载选项数据以提升用户体验
  loadSelectOptions()
})
</script>

<style scoped>
.node-form {
  padding: 0 20px;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

:deep(.el-radio) {
  margin-right: 0;
  margin-bottom: 8px;
}

:deep(.el-switch) {
  --el-switch-on-color: #409eff;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>