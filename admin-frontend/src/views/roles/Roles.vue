<template>
  <div class="roles-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.roles') }}</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('roles.addRole') }}
      </el-button>
    </div>

    <!-- 角色列表 -->
    <el-card class="table-card">
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="id" :label="$t('roles.roleId')" width="80" />
        <el-table-column prop="name" :label="$t('roles.roleName')" width="150" />
        <el-table-column prop="code" :label="$t('roles.roleCode')" width="150" />
        <el-table-column prop="description" :label="$t('roles.description')" min-width="300" />
        <el-table-column :label="$t('roles.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? $t('roles.enabled') : $t('roles.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('roles.createTime')" width="180" />
        <el-table-column :label="$t('common.actions')" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewRole(row)">{{ $t('common.view') }}</el-button>
            <el-button link type="primary" @click="editRole(row)">{{ $t('common.edit') }}</el-button>
            <el-button link type="warning" @click="setPermissions(row)">{{ $t('roles.permissions') }}</el-button>
            <el-button link type="danger" @click="deleteRole(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="isEdit ? $t('roles.editRole') : $t('roles.addRole')"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="roleForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('roles.roleName')" prop="name">
          <el-input v-model="roleForm.name" :placeholder="$t('roles.enterRoleName')" />
        </el-form-item>
        <el-form-item :label="$t('roles.roleCode')" prop="code">
          <el-input v-model="roleForm.code" :placeholder="$t('roles.enterRoleCode')" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input 
            v-model="roleForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入角色描述" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRole" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 角色详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="角色详情" width="600px">
      <div class="role-detail" v-if="selectedRole">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-item">
            <label>角色名称：</label>
            <span>{{ selectedRole.name }}</span>
          </div>
          <div class="detail-item">
            <label>角色编码：</label>
            <span>{{ selectedRole.code }}</span>
          </div>
          <div class="detail-item">
            <label>角色描述：</label>
            <span>{{ selectedRole.description }}</span>
          </div>
          <div class="detail-item">
            <label>状态：</label>
            <el-tag :type="selectedRole.status === 1 ? 'success' : 'danger'">
              {{ selectedRole.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
        </div>
        
        <div class="detail-section">
          <h3>角色权限</h3>
          <div class="permissions-list">
            <el-tag 
              v-for="permission in rolePermissions" 
              :key="permission.id"
              class="permission-tag"
            >
              {{ permission.name }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog v-model="showPermissionDialog" title="设置权限" width="600px">
      <div class="permission-tree">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedPermissions"
          :props="treeProps"
        />
      </div>
      <template #footer>
        <el-button @click="showPermissionDialog = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '@/api/role'

const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const roles = ref([])
const total = ref(0)
const rolePermissions = ref([])
const permissionTree = ref([])
const checkedPermissions = ref([])

const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const showPermissionDialog = ref(false)
const isEdit = ref(false)
const selectedRole = ref(null)
const permissionTreeRef = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 表单数据
const roleForm = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  status: 1
})

// 树形组件属性配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'blur' }
  ]
}

const formRef = ref(null)

// 加载角色列表
const loadRoles = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize,
      ...searchForm
    }
    
    const response = await roleApi.getRoleList(params)
    if (response.data) {
      roles.value = response.data.records || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 加载权限树
const loadPermissionTree = async () => {
  // 模拟权限树数据
  permissionTree.value = [
    {
      id: 1,
      name: '系统管理',
      children: [
        { id: 11, name: '用户管理' },
        { id: 12, name: '角色管理' },
        { id: 13, name: '权限管理' }
      ]
    },
    {
      id: 2,
      name: '员工管理',
      children: [
        { id: 21, name: '员工信息' },
        { id: 22, name: '考勤管理' },
        { id: 23, name: '请假审批' }
      ]
    },
    {
      id: 3,
      name: '部门管理',
      children: [
        { id: 31, name: '部门信息' },
        { id: 32, name: '部门员工' }
      ]
    },
    {
      id: 4,
      name: '报表管理',
      children: [
        { id: 41, name: '考勤报表' },
        { id: 42, name: '绩效报表' }
      ]
    }
  ]
}

// 查看角色详情
const viewRole = async (role) => {
  selectedRole.value = role
  
  // 加载角色权限
  rolePermissions.value = [
    { id: 11, name: '用户管理' },
    { id: 21, name: '员工信息' },
    { id: 31, name: '部门信息' }
  ]
  
  showDetailDialog.value = true
}

// 编辑角色
const editRole = (role) => {
  isEdit.value = true
  Object.assign(roleForm, { ...role })
  showAddDialog.value = true
}

// 设置权限
const setPermissions = async (role) => {
  selectedRole.value = role
  
  // 加载角色当前权限
  checkedPermissions.value = [11, 21, 31] // 模拟已选择的权限ID
  
  await loadPermissionTree()
  showPermissionDialog.value = true
}

// 保存权限
const savePermissions = () => {
  if (!permissionTreeRef.value) return
  
  const checkedNodes = permissionTreeRef.value.getCheckedNodes()
  console.log('选中的权限:', checkedNodes)
  
  ElMessage.success('权限设置成功')
  showPermissionDialog.value = false
}

// 删除角色
const deleteRole = async (role) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 ${role.name} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟删除API调用
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    // 用户取消删除
  }
}

// 保存角色
const saveRole = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    // 模拟保存API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    showAddDialog.value = false
    loadRoles()
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
  Object.assign(roleForm, {
    id: null,
    name: '',
    code: '',
    description: '',
    status: 1
  })
  isEdit.value = false
}

// 组件挂载
onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.roles-container {
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

.role-detail {
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

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.permission-tag {
  margin: 0;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #eee;
  padding: 15px;
  border-radius: 4px;
}
</style>