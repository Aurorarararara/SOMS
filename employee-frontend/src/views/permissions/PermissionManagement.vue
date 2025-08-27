<template>
  <div class="permission-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Lock /></el-icon>
          权限分级管理
        </h1>
        <p class="page-subtitle">角色权限配置与权限回收机制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateRoleDialog = true">
          <el-icon><Plus /></el-icon>
          新建角色
        </el-button>
        <el-button @click="showPermissionMatrix = true">
          <el-icon><Grid /></el-icon>
          权限矩阵
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <!-- 权限回收提醒 -->
      <div v-if="expiredPermissions.length > 0" class="expired-alert">
        <el-alert
          title="权限到期提醒"
          type="warning"
          :description="`有 ${expiredPermissions.length} 个权限即将到期或已到期，请及时处理`"
          show-icon
          :closable="false"
        >
          <template #default>
            <div class="alert-actions">
              <el-button type="warning" size="small" @click="showExpiredDialog = true">
                查看详情
              </el-button>
              <el-button size="small" @click="batchRevokeExpired">
                批量回收
              </el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 角色列表 -->
      <div class="roles-section">
        <div class="section-header">
          <h2>角色列表</h2>
          <div class="section-actions">
            <el-input
              v-model="roleSearchQuery"
              placeholder="搜索角色"
              style="width: 200px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>

        <div class="roles-grid">
          <div 
            v-for="role in filteredRoles" 
            :key="role.id"
            class="role-card"
            :class="{ 'is-disabled': !role.enabled }"
          >
            <div class="role-header">
              <div class="role-info">
                <div class="role-icon" :style="{ backgroundColor: role.color }">
                  <el-icon><component :is="role.icon" /></el-icon>
                </div>
                <div class="role-details">
                  <h3 class="role-name">{{ role.name }}</h3>
                  <p class="role-description">{{ role.description }}</p>
                </div>
              </div>
              <div class="role-actions">
                <el-dropdown trigger="click">
                  <el-button type="text">
                    <el-icon><More /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="editRole(role)">编辑</el-dropdown-item>
                      <el-dropdown-item @click="cloneRole(role)">克隆</el-dropdown-item>
                      <el-dropdown-item @click="viewPermissions(role)">查看权限</el-dropdown-item>
                      <el-dropdown-item divided>
                        <span :class="role.enabled ? 'danger-text' : 'success-text'">
                          {{ role.enabled ? '禁用' : '启用' }}
                        </span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <div class="role-stats">
              <div class="stat-item">
                <span class="stat-label">用户数量</span>
                <span class="stat-value">{{ role.userCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">权限数量</span>
                <span class="stat-value">{{ role.permissionCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">等级</span>
                <el-tag :type="getLevelType(role.level)" size="small">
                  {{ getLevelText(role.level) }}
                </el-tag>
              </div>
            </div>

            <div class="role-permissions-preview">
              <div class="permissions-title">主要权限：</div>
              <div class="permissions-tags">
                <el-tag 
                  v-for="permission in role.mainPermissions" 
                  :key="permission"
                  size="small"
                  class="permission-tag"
                >
                  {{ permission }}
                </el-tag>
                <span v-if="role.permissionCount > role.mainPermissions.length" class="more-permissions">
                  +{{ role.permissionCount - role.mainPermissions.length }} 更多
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 权限回收记录 -->
      <div class="revoke-section">
        <div class="section-header">
          <h2>权限回收记录</h2>
          <div class="section-actions">
            <el-button @click="exportRevokeLog">
              <el-icon><Download /></el-icon>
              导出记录
            </el-button>
          </div>
        </div>

        <el-table :data="revokeRecords" v-loading="loading">
          <el-table-column prop="userName" label="用户" width="120" />
          <el-table-column prop="roleName" label="角色" width="120" />
          <el-table-column prop="permissions" label="回收权限" min-width="200">
            <template #default="scope">
              <el-tag 
                v-for="permission in scope.row.permissions.slice(0, 3)" 
                :key="permission"
                size="small"
                class="permission-tag"
              >
                {{ permission }}
              </el-tag>
              <span v-if="scope.row.permissions.length > 3" class="more-text">
                +{{ scope.row.permissions.length - 3 }} 更多
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="回收原因" width="150" />
          <el-table-column prop="revokeTime" label="回收时间" width="160" />
          <el-table-column prop="operator" label="操作人" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="primary" size="small" @click="restorePermission(scope.row)">
                恢复
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 创建/编辑角色对话框 -->
    <RoleFormDialog 
      v-model="showCreateRoleDialog"
      :role="currentRole"
      @save="handleRoleSave"
    />

    <!-- 权限矩阵对话框 -->
    <PermissionMatrixDialog v-model="showPermissionMatrix" />

    <!-- 到期权限对话框 -->
    <ExpiredPermissionsDialog 
      v-model="showExpiredDialog"
      :permissions="expiredPermissions"
      @revoke="handleBatchRevoke"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Lock, Plus, Grid, Search, More, Download,
  User, UserFilled, Crown, Setting
} from '@element-plus/icons-vue'
import RoleFormDialog from './components/RoleFormDialog.vue'
import PermissionMatrixDialog from './components/PermissionMatrixDialog.vue'
import ExpiredPermissionsDialog from './components/ExpiredPermissionsDialog.vue'

// 响应式数据
const loading = ref(false)
const roleSearchQuery = ref('')
const showCreateRoleDialog = ref(false)
const showPermissionMatrix = ref(false)
const showExpiredDialog = ref(false)
const currentRole = ref(null)

// 角色数据
const roles = ref([
  {
    id: 1,
    name: '超级管理员',
    description: '拥有系统所有权限',
    color: '#ff4d4f',
    icon: 'Crown',
    level: 4,
    enabled: true,
    userCount: 2,
    permissionCount: 45,
    mainPermissions: ['系统管理', '用户管理', '权限管理']
  },
  {
    id: 2,
    name: '部门经理',
    description: '部门管理和审批权限',
    color: '#faad14',
    icon: 'UserFilled',
    level: 3,
    enabled: true,
    userCount: 8,
    permissionCount: 28,
    mainPermissions: ['部门管理', '审批管理', '数据查看']
  },
  {
    id: 3,
    name: '普通员工',
    description: '基础办公权限',
    color: '#52c41a',
    icon: 'User',
    level: 1,
    enabled: true,
    userCount: 156,
    permissionCount: 12,
    mainPermissions: ['考勤打卡', '请假申请', '个人信息']
  },
  {
    id: 4,
    name: '系统管理员',
    description: '系统维护和配置权限',
    color: '#1890ff',
    icon: 'Setting',
    level: 3,
    enabled: true,
    userCount: 3,
    permissionCount: 32,
    mainPermissions: ['系统配置', '数据备份', '日志管理']
  }
])

// 到期权限数据
const expiredPermissions = ref([
  {
    id: 1,
    userName: '张三',
    roleName: '临时管理员',
    permissions: ['用户管理', '数据导出'],
    expireDate: '2025-08-25',
    status: 'expired'
  },
  {
    id: 2,
    userName: '李四',
    roleName: '项目经理',
    permissions: ['项目管理'],
    expireDate: '2025-08-28',
    status: 'expiring'
  }
])

// 权限回收记录
const revokeRecords = ref([
  {
    id: 1,
    userName: '王五',
    roleName: '临时审核员',
    permissions: ['审核管理', '数据修改'],
    reason: '项目结束',
    revokeTime: '2025-08-20 14:30:00',
    operator: '管理员'
  },
  {
    id: 2,
    userName: '赵六',
    roleName: '财务助理',
    permissions: ['财务查看'],
    reason: '岗位调整',
    revokeTime: '2025-08-18 09:15:00',
    operator: '人事经理'
  }
])

// 计算属性
const filteredRoles = computed(() => {
  if (!roleSearchQuery.value) return roles.value
  return roles.value.filter(role => 
    role.name.includes(roleSearchQuery.value) || 
    role.description.includes(roleSearchQuery.value)
  )
})

// 方法
const getLevelType = (level) => {
  const types = { 1: 'info', 2: 'success', 3: 'warning', 4: 'danger' }
  return types[level] || 'info'
}

const getLevelText = (level) => {
  const texts = { 1: '初级', 2: '中级', 3: '高级', 4: '超级' }
  return texts[level] || '初级'
}

const editRole = (role) => {
  currentRole.value = { ...role }
  showCreateRoleDialog.value = true
}

const cloneRole = (role) => {
  const clonedRole = { 
    ...role, 
    id: null, 
    name: `${role.name} (副本)`,
    userCount: 0 
  }
  currentRole.value = clonedRole
  showCreateRoleDialog.value = true
}

const viewPermissions = (role) => {
  ElMessage.info(`查看 ${role.name} 的详细权限`)
}

const handleRoleSave = (roleData) => {
  if (roleData.id) {
    // 更新角色
    const index = roles.value.findIndex(r => r.id === roleData.id)
    if (index > -1) {
      roles.value[index] = roleData
    }
    ElMessage.success('角色更新成功！')
  } else {
    // 新建角色
    roleData.id = Date.now()
    roleData.userCount = 0
    roles.value.push(roleData)
    ElMessage.success('角色创建成功！')
  }
}

const batchRevokeExpired = () => {
  ElMessageBox.confirm(
    `确定要批量回收 ${expiredPermissions.value.length} 个到期权限吗？`,
    '批量权限回收',
    {
      confirmButtonText: '确定回收',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    handleBatchRevoke(expiredPermissions.value)
  })
}

const handleBatchRevoke = (permissions) => {
  // 处理批量权限回收
  permissions.forEach(permission => {
    revokeRecords.value.unshift({
      id: Date.now() + Math.random(),
      userName: permission.userName,
      roleName: permission.roleName,
      permissions: permission.permissions,
      reason: '权限到期自动回收',
      revokeTime: new Date().toLocaleString(),
      operator: '系统自动'
    })
  })
  
  expiredPermissions.value = []
  ElMessage.success('权限回收成功！')
}

const restorePermission = (record) => {
  ElMessageBox.confirm(
    `确定要恢复 ${record.userName} 的权限吗？`,
    '权限恢复',
    {
      confirmButtonText: '确定恢复',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(() => {
    ElMessage.success('权限恢复成功！')
  })
}

const exportRevokeLog = () => {
  ElMessage.success('权限回收记录导出成功！')
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.permission-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content .page-title {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.title-icon {
  margin-right: 12px;
  color: #1890ff;
}

.page-subtitle {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.expired-alert {
  .alert-actions {
    margin-top: 8px;
    display: flex;
    gap: 8px;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  color: #1a1a1a;
  font-size: 18px;
}

.roles-section {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.role-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 20px;
  background: white;
  transition: all 0.3s ease;
}

.role-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.role-card.is-disabled {
  opacity: 0.6;
  background: #f5f5f5;
}

.role-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.role-info {
  display: flex;
  gap: 12px;
}

.role-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.role-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.role-description {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.role-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.role-permissions-preview .permissions-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.permissions-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.permission-tag {
  font-size: 11px;
}

.more-permissions, .more-text {
  font-size: 12px;
  color: #999;
}

.revoke-section {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.danger-text {
  color: #ff4d4f;
}

.success-text {
  color: #52c41a;
}
</style>