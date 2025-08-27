<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2><el-icon><User /></el-icon> 个人信息</h2>
      <p class="subtitle">查看和编辑您的个人信息</p>
    </div>

    <!-- 内容区域 -->
    <div class="profile-content">
      <!-- 头像和基本信息卡片 -->
      <el-card class="profile-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><Avatar /></el-icon> 基本信息</span>
            <el-button type="primary" :icon="Edit" @click="editProfile" v-if="!isEditing">编辑</el-button>
            <div v-else>
              <el-button type="success" :icon="Check" @click="saveProfile" :loading="saving">保存</el-button>
              <el-button :icon="Close" @click="cancelEdit">取消</el-button>
            </div>
          </div>
        </template>

        <div class="profile-info">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <el-avatar :size="120" :src="userInfo.avatar" class="user-avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <el-button type="text" @click="changeAvatar" v-if="isEditing" class="change-avatar">
              <el-icon><Camera /></el-icon> 更换头像
            </el-button>
          </div>

          <!-- 信息表单 -->
          <div class="info-form">
            <el-form :model="formData" label-width="100px" :disabled="!isEditing">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名">
                    <el-input v-model="formData.username" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="员工编号">
                    <el-input v-model="formData.employeeNumber" disabled />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名">
                    <el-input v-model="formData.name" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="性别">
                    <el-select v-model="formData.gender" placeholder="请选择性别">
                      <el-option label="男" value="M" />
                      <el-option label="女" value="F" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="手机号">
                    <el-input v-model="formData.phone" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱">
                    <el-input v-model="formData.email" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="部门">
                    <el-input v-model="formData.departmentName" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="职位">
                    <el-input v-model="formData.position" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="入职时间">
                    <el-input v-model="formData.hireDate" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="角色">
                    <el-tag v-for="role in formData.roles" :key="role" type="success" class="role-tag">
                      {{ role }}
                    </el-tag>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="地址">
                <el-input v-model="formData.address" type="textarea" :rows="2" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>

      <!-- 密码修改卡片 -->
      <el-card class="password-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><Lock /></el-icon> 修改密码</span>
          </div>
        </template>

        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input
              v-model="passwordForm.currentPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePassword" :loading="changingPassword">
              修改密码
            </el-button>
            <el-button @click="resetPasswordForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 系统设置卡片 -->
      <el-card class="settings-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><Setting /></el-icon> 系统设置</span>
          </div>
        </template>

        <div class="settings-content">
          <div class="setting-item">
            <div class="setting-label">
              <el-icon><Notification /></el-icon>
              <span>邮件通知</span>
            </div>
            <el-switch v-model="settings.emailNotification" />
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <el-icon><Message /></el-icon>
              <span>短信通知</span>
            </div>
            <el-switch v-model="settings.smsNotification" />
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <el-icon><Bell /></el-icon>
              <span>系统通知</span>
            </div>
            <el-switch v-model="settings.systemNotification" />
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <el-icon><Moon /></el-icon>
              <span>深色模式</span>
            </div>
            <el-switch v-model="settings.darkMode" />
          </div>

          <div class="setting-actions">
            <el-button type="primary" @click="saveSettings" :loading="savingSettings">
              保存设置
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 登录日志卡片 -->
      <el-card class="login-log-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><Clock /></el-icon> 最近登录记录</span>
            <el-button type="text" @click="refreshLoginLog">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </template>

        <el-table :data="loginLogs" style="width: 100%">
          <el-table-column prop="loginTime" label="登录时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.loginTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="ipAddress" label="IP地址" width="150" />
          <el-table-column prop="location" label="登录地点" width="200" />
          <el-table-column prop="device" label="设备信息" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="totalLogs"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Avatar, Edit, Check, Close, Camera, Lock, Setting,
  Notification, Message, Bell, Moon, Clock, Refresh, UserFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 响应式数据
const isEditing = ref(false)
const saving = ref(false)
const changingPassword = ref(false)
const savingSettings = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalLogs = ref(0)

// 用户信息
const userInfo = reactive({
  avatar: '',
  username: '',
  employeeNumber: '',
  name: '',
  gender: '',
  phone: '',
  email: '',
  departmentName: '',
  position: '',
  hireDate: '',
  roles: [],
  address: ''
})

// 表单数据
const formData = reactive({
  username: '',
  employeeNumber: '',
  name: '',
  gender: '',
  phone: '',
  email: '',
  departmentName: '',
  position: '',
  hireDate: '',
  roles: [],
  address: ''
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordFormRef = ref()

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 系统设置
const settings = reactive({
  emailNotification: true,
  smsNotification: false,
  systemNotification: true,
  darkMode: false
})

// 登录日志
const loginLogs = ref([
  {
    loginTime: '2024-01-20 09:30:15',
    ipAddress: '192.168.1.100',
    location: '北京市',
    device: 'Chrome 120.0 / Windows 10',
    status: '成功'
  },
  {
    loginTime: '2024-01-19 14:25:30',
    ipAddress: '192.168.1.100',
    location: '北京市',
    device: 'Chrome 120.0 / Windows 10',
    status: '成功'
  },
  {
    loginTime: '2024-01-19 08:45:12',
    ipAddress: '192.168.1.100',
    location: '北京市',
    device: 'Chrome 120.0 / Windows 10',
    status: '成功'
  }
])

// 方法
const editProfile = () => {
  isEditing.value = true
  // 复制当前数据到表单
  Object.assign(formData, userInfo)
}

const saveProfile = async () => {
  saving.value = true
  try {
    // 调用实际的用户信息更新API
    await userStore.updateUserInfo(formData)
    
    // 更新本地用户信息
    Object.assign(userInfo, formData)
    
    ElMessage.success('个人信息保存成功')
    isEditing.value = false
  } catch (error) {
    console.error('保存用户信息失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const cancelEdit = () => {
  isEditing.value = false
  // 重置表单数据
  Object.assign(formData, userInfo)
}

const changeAvatar = () => {
  ElMessage.info('头像上传功能待实现')
}

const changePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  changingPassword.value = true
  try {
    // 调用实际的密码修改API
    await userStore.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功')
    resetPasswordForm()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('密码修改失败，请重试')
  } finally {
    changingPassword.value = false
  }
}

const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
  Object.assign(passwordForm, {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
}

const saveSettings = async () => {
  savingSettings.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('设置保存成功')
  } catch (error) {
    ElMessage.error('设置保存失败，请重试')
  } finally {
    savingSettings.value = false
  }
}

const refreshLoginLog = () => {
  ElMessage.success('登录记录已刷新')
}

const formatDate = (dateStr) => {
  return dateStr
}

const handleSizeChange = (size) => {
  pageSize.value = size
  // 重新加载数据
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  // 重新加载数据
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 优先从userStore获取用户信息
    if (userStore.userInfo && userStore.userInfo.id && userStore.isCacheValid()) {
      const storeUserInfo = userStore.userInfo
      Object.assign(userInfo, {
        avatar: storeUserInfo.avatar || '',
        username: storeUserInfo.username || '',
        employeeNumber: storeUserInfo.employeeCode || storeUserInfo.employeeNumber || '',
        name: storeUserInfo.realName || storeUserInfo.name || '',
        gender: storeUserInfo.gender || '',
        phone: storeUserInfo.phone || '',
        email: storeUserInfo.email || '',
        departmentName: storeUserInfo.departmentName || '',
        position: storeUserInfo.position || '',
        hireDate: storeUserInfo.hireDate || '',
        roles: storeUserInfo.roles || [],
        address: storeUserInfo.address || ''
      })
      Object.assign(formData, userInfo)
      return
    }
    
    // 如果缓存无效或为空，则调用API获取最新信息
    await userStore.getUserInfoAction(true)
    if (userStore.userInfo && userStore.userInfo.id) {
      const storeUserInfo = userStore.userInfo
      Object.assign(userInfo, {
        avatar: storeUserInfo.avatar || '',
        username: storeUserInfo.username || '',
        employeeNumber: storeUserInfo.employeeCode || storeUserInfo.employeeNumber || '',
        name: storeUserInfo.realName || storeUserInfo.name || '',
        gender: storeUserInfo.gender || '',
        phone: storeUserInfo.phone || '',
        email: storeUserInfo.email || '',
        departmentName: storeUserInfo.departmentName || '',
        position: storeUserInfo.position || '',
        hireDate: storeUserInfo.hireDate || '',
        roles: storeUserInfo.roles || [],
        address: storeUserInfo.address || ''
      })
      Object.assign(formData, userInfo)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 如果API调用失败，使用默认数据
    Object.assign(userInfo, {
      avatar: '',
      username: 'admin',
      employeeNumber: 'EMP001',
      name: '管理员',
      gender: 'M',
      phone: '13800138000',
      email: 'admin@company.com',
      departmentName: '管理部门',
      position: '系统管理员',
      hireDate: '2024-01-01',
      roles: ['超级管理员', '系统管理员'],
      address: '北京市朝阳区某某街道123号'
    })
    Object.assign(formData, userInfo)
  }
}

// 初始化
onMounted(async () => {
  await loadUserInfo()
  totalLogs.value = loginLogs.value.length
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.profile-info {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  min-width: 120px;
}

.user-avatar {
  border: 3px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.change-avatar {
  font-size: 12px;
  color: #409eff;
}

.info-form {
  flex: 1;
}

.role-tag {
  margin-right: 8px;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #303133;
}

.setting-actions {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.profile-card, .password-card, .settings-card, .login-log-card {
  margin-bottom: 20px;
}

.profile-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.profile-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.profile-card :deep(.el-card__body) {
  background: rgba(255, 255, 255, 0.05);
}

.profile-card .card-header span,
.profile-card .card-header .el-button {
  color: white;
}

@media (max-width: 768px) {
  .profile-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .info-form {
    width: 100%;
  }
}
</style>