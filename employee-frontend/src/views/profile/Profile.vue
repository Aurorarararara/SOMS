<template>
  <div class="profile-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">{{ $t('profile.title') }}</h1>
          <p class="page-subtitle">{{ $t('profile.subtitle') }}</p>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <!-- 左侧个人信息卡片 -->
      <div class="profile-sidebar">
        <div class="profile-card">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="120" :src="userInfo.avatar" class="user-avatar">
                {{ userInfo.name?.charAt(0) }}
              </el-avatar>
              <div class="avatar-overlay" @click="showAvatarDialog = true">
                <el-icon><Camera /></el-icon>
                <span>{{ $t('profile.changeAvatar') }}</span>
              </div>
            </div>
          </div>
          
          <div class="profile-info">
            <h3 class="user-name">{{ userInfo.name }}</h3>
            <p class="user-position">{{ userInfo.position }}</p>
            <p class="user-department">{{ userInfo.department?.name }}</p>
          </div>
          
          <div class="profile-stats">
            <div class="stat-item">
              <div class="stat-value">{{ workStats.workDays }}</div>
              <div class="stat-label">{{ $t('profile.workDays') }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ workStats.projects }}</div>
              <div class="stat-label">{{ $t('profile.projects') }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ workStats.score }}</div>
              <div class="stat-label">{{ $t('profile.overallScore') }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息编辑区域 -->
      <div class="profile-main">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <div class="tab-content">
              <el-form 
                ref="basicFormRef" 
                :model="basicForm" 
                :rules="basicRules" 
                label-width="120px"
                class="profile-form"
              >
                <div class="form-section">
                  <h4>{{ $t('profile.basicInfo') }}</h4>
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.name')" prop="name">
                        <el-input v-model="basicForm.name" :placeholder="$t('profile.enterName')" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.gender')" prop="gender">
                        <el-radio-group v-model="basicForm.gender">
                          <el-radio label="男">{{ $t('profile.male') }}</el-radio>
                          <el-radio label="女">{{ $t('profile.female') }}</el-radio>
                        </el-radio-group>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.birthday')" prop="birthday">
                        <el-date-picker
                          v-model="basicForm.birthday"
                          type="date"
                          :placeholder="$t('profile.selectBirthday')"
                          format="YYYY-MM-DD"
                          value-format="YYYY-MM-DD"
                          style="width: 100%"
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.idCard')" prop="idCard">
                        <el-input v-model="basicForm.idCard" :placeholder="$t('profile.enterIdCard')" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>

                <div class="form-section">
                  <h4>{{ $t('profile.contactInfo') }}</h4>
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.phone')" prop="phone">
                        <el-input v-model="basicForm.phone" :placeholder="$t('profile.enterPhone')" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.email')" prop="email">
                        <el-input v-model="basicForm.email" :placeholder="$t('profile.enterEmail')" />
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-form-item :label="$t('profile.address')" prop="address">
                    <el-input v-model="basicForm.address" :placeholder="$t('profile.enterAddress')" />
                  </el-form-item>
                </div>

                <div class="form-section">
                  <h4>{{ $t('profile.workInfo') }}</h4>
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.employeeId')">
                        <el-input :value="userInfo.code" readonly />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.hireDate')">
                        <el-input :value="userInfo.hireDate" readonly />
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-row :gutter="24">
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.department')">
                        <el-input :value="userInfo.department?.name" readonly />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item :label="$t('profile.position')">
                        <el-input :value="userInfo.position" readonly />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>

                <div class="form-actions">
                  <el-button type="primary" @click="updateBasicInfo" :loading="saving">
                    保存修改
                  </el-button>
                  <el-button @click="resetBasicForm">重置</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 安全设置 -->
          <el-tab-pane label="安全设置" name="security">
            <div class="tab-content">
              <div class="security-section">
                <h4>密码修改</h4>
                <el-form 
                  ref="passwordFormRef" 
                  :model="passwordForm" 
                  :rules="passwordRules" 
                  label-width="120px"
                  class="password-form"
                >
                  <el-form-item :label="$t('profile.currentPassword')" prop="oldPassword">
                    <el-input
                      v-model="passwordForm.oldPassword"
                      type="password"
                      :placeholder="$t('profile.enterCurrentPassword')"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item :label="$t('profile.newPassword')" prop="newPassword">
                    <el-input
                      v-model="passwordForm.newPassword"
                      type="password"
                      :placeholder="$t('profile.enterNewPassword')"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item :label="$t('profile.confirmPassword')" prop="confirmPassword">
                    <el-input
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      :placeholder="$t('profile.enterConfirmPassword')"
                      show-password
                    />
                  </el-form-item>
                  <div class="form-actions">
                    <el-button type="primary" @click="changePassword" :loading="changingPassword">
                      修改密码
                    </el-button>
                  </div>
                </el-form>
              </div>

              <div class="security-section">
                <h4>账户安全</h4>
                <div class="security-items">
                  <div class="security-item">
                    <div class="security-info">
                      <div class="security-title">登录密码</div>
                      <div class="security-desc">定期更新密码，确保账户安全</div>
                    </div>
                    <div class="security-status">
                      <el-tag type="success">已设置</el-tag>
                    </div>
                  </div>
                  
                  <div class="security-item">
                    <div class="security-info">
                      <div class="security-title">手机绑定</div>
                      <div class="security-desc">绑定手机号用于身份验证</div>
                    </div>
                    <div class="security-status">
                      <el-tag :type="userInfo.phone ? 'success' : 'warning'">
                        {{ userInfo.phone ? '已绑定' : '未绑定' }}
                      </el-tag>
                    </div>
                  </div>
                  
                  <div class="security-item">
                    <div class="security-info">
                      <div class="security-title">邮箱绑定</div>
                      <div class="security-desc">绑定邮箱用于接收重要通知</div>
                    </div>
                    <div class="security-status">
                      <el-tag :type="userInfo.email ? 'success' : 'warning'">
                        {{ userInfo.email ? '已绑定' : '未绑定' }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 偏好设置 -->
          <el-tab-pane label="偏好设置" name="preferences">
            <div class="tab-content">
              <div class="preferences-section">
                <h4>界面设置</h4>
                <div class="preference-items">
                  <div class="preference-item">
                    <div class="preference-info">
                      <div class="preference-title">主题模式</div>
                      <div class="preference-desc">选择您喜欢的界面主题</div>
                    </div>
                    <div class="preference-control">
                      <el-radio-group v-model="preferences.theme">
                        <el-radio label="light">浅色模式</el-radio>
                        <el-radio label="dark">深色模式</el-radio>
                        <el-radio label="auto">跟随系统</el-radio>
                      </el-radio-group>
                    </div>
                  </div>
                  
                  <div class="preference-item">
                    <div class="preference-info">
                      <div class="preference-title">语言设置</div>
                      <div class="preference-desc">选择界面显示语言</div>
                    </div>
                    <div class="preference-control">
                      <el-select v-model="preferences.language" style="width: 200px">
                        <el-option label="简体中文" value="zh-CN" />
                        <el-option label="繁體中文" value="zh-TW" />
                        <el-option label="English" value="en-US" />
                      </el-select>
                    </div>
                  </div>
                </div>
              </div>

              <div class="preferences-section">
                <h4>通知设置</h4>
                <div class="preference-items">
                  <div class="preference-item">
                    <div class="preference-info">
                      <div class="preference-title">邮件通知</div>
                      <div class="preference-desc">接收重要通知的邮件提醒</div>
                    </div>
                    <div class="preference-control">
                      <el-switch v-model="preferences.emailNotification" />
                    </div>
                  </div>
                  
                  <div class="preference-item">
                    <div class="preference-info">
                      <div class="preference-title">短信通知</div>
                      <div class="preference-desc">接收紧急通知的短信提醒</div>
                    </div>
                    <div class="preference-control">
                      <el-switch v-model="preferences.smsNotification" />
                    </div>
                  </div>
                  
                  <div class="preference-item">
                    <div class="preference-info">
                      <div class="preference-title">浏览器通知</div>
                      <div class="preference-desc">在浏览器中显示桌面通知</div>
                    </div>
                    <div class="preference-control">
                      <el-switch v-model="preferences.browserNotification" />
                    </div>
                  </div>
                </div>
              </div>

              <div class="form-actions">
                <el-button type="primary" @click="savePreferences" :loading="savingPreferences">
                  保存设置
                </el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="400px">
      <div class="avatar-upload">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleAvatarChange"
          accept="image/*"
        >
          <div class="upload-area">
            <el-icon><Plus /></el-icon>
            <p>点击选择图片</p>
          </div>
        </el-upload>
        
        <div v-if="avatarPreview" class="avatar-preview">
          <img :src="avatarPreview" alt="头像预览" />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarDialog = false">取消</el-button>
          <el-button type="primary" @click="uploadAvatar" :loading="uploadingAvatar">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Camera, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const { t: $t } = useI18n()
const userStore = useUserStore()

// 响应式数据
const activeTab = ref('basic')
const saving = ref(false)
const changingPassword = ref(false)
const savingPreferences = ref(false)
const showAvatarDialog = ref(false)
const uploadingAvatar = ref(false)
const avatarPreview = ref('')

const userInfo = ref({
  id: null,
  name: '',
  code: '',
  position: '',
  phone: '',
  email: '',
  avatar: '',
  hireDate: '',
  department: {
    name: ''
  }
})

const workStats = reactive({
  workDays: 365,
  projects: 8,
  score: 95
})

const basicFormRef = ref()
const basicForm = reactive({
  name: '',
  gender: '',
  birthday: '',
  idCard: '',
  phone: '',
  email: '',
  address: ''
})

const basicRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
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

const preferences = reactive({
  theme: 'light',
  language: 'zh-CN',
  emailNotification: true,
  smsNotification: false,
  browserNotification: true
})

// 方法
const loadUserInfo = async () => {
  try {
    // 优先从userStore获取用户信息
    if (userStore.userInfo && userStore.userInfo.id && userStore.isCacheValid()) {
      Object.assign(userInfo.value, userStore.userInfo)
      Object.assign(basicForm, {
        name: userStore.userInfo.realName || userStore.userInfo.name,
        gender: userStore.userInfo.gender,
        birthday: userStore.userInfo.birthday,
        idCard: userStore.userInfo.idCard,
        phone: userStore.userInfo.phone,
        email: userStore.userInfo.email,
        address: userStore.userInfo.address
      })
      return
    }
    
    // 如果缓存无效或为空，则调用API获取最新信息
    const { data } = await userApi.getUserInfo()
    Object.assign(userInfo.value, data)
    Object.assign(basicForm, {
      name: data.realName || data.name,
      gender: data.gender,
      birthday: data.birthday,
      idCard: data.idCard,
      phone: data.phone,
      email: data.email,
      address: data.address
    })
    
    // 更新userStore中的用户信息
    userStore.userInfo = { ...data }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 如果API调用失败，尝试使用userStore中的数据
    if (userStore.userInfo && userStore.userInfo.id) {
      Object.assign(userInfo.value, userStore.userInfo)
      Object.assign(basicForm, {
        name: userStore.userInfo.realName || userStore.userInfo.name,
        gender: userStore.userInfo.gender,
        birthday: userStore.userInfo.birthday,
        idCard: userStore.userInfo.idCard,
        phone: userStore.userInfo.phone,
        email: userStore.userInfo.email,
        address: userStore.userInfo.address
      })
    }
  }
}

const updateBasicInfo = async () => {
  try {
    await basicFormRef.value?.validate()
    saving.value = true
    
    await userApi.updateUserInfo(basicForm)
    Object.assign(userInfo.value, basicForm)
    
    // 更新userStore中的用户信息
    if (userStore.userInfo) {
      Object.assign(userStore.userInfo, {
        realName: basicForm.name,
        name: basicForm.name,
        gender: basicForm.gender,
        birthday: basicForm.birthday,
        idCard: basicForm.idCard,
        phone: basicForm.phone,
        email: basicForm.email,
        address: basicForm.address
      })
    }
    
    ElMessage.success('个人信息更新成功！')
  } catch (error) {
    ElMessage.error('更新失败，请重试')
  } finally {
    saving.value = false
  }
}

const resetBasicForm = () => {
  Object.assign(basicForm, {
    name: userInfo.value.name,
    gender: userInfo.value.gender,
    birthday: userInfo.value.birthday,
    idCard: userInfo.value.idCard,
    phone: userInfo.value.phone,
    email: userInfo.value.email,
    address: userInfo.value.address
  })
}

const changePassword = async () => {
  try {
    await passwordFormRef.value?.validate()
    changingPassword.value = true
    
    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功！')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    ElMessage.error('密码修改失败，请重试')
  } finally {
    changingPassword.value = false
  }
}

const savePreferences = async () => {
  try {
    savingPreferences.value = true
    await userApi.updatePreferences(preferences)
    ElMessage.success('偏好设置保存成功！')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    savingPreferences.value = false
  }
}

const handleAvatarChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const uploadAvatar = async () => {
  try {
    uploadingAvatar.value = true
    // 这里应该调用实际的上传API
    // await userApi.uploadAvatar(avatarFile)
    userInfo.value.avatar = avatarPreview.value
    ElMessage.success('头像更新成功！')
    showAvatarDialog.value = false
    avatarPreview.value = ''
  } catch (error) {
    ElMessage.error('头像上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.profile-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  text-align: center;
}

.avatar-section {
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.user-avatar {
  border: 4px solid #f0f0f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  font-size: 12px;
  margin-top: 4px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.user-position {
  font-size: 14px;
  color: #666;
  margin: 0 0 4px 0;
}

.user-department {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.profile-main {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.profile-tabs {
  padding: 0;
}

.tab-content {
  padding: 32px;
}

.form-section {
  margin-bottom: 32px;
}

.form-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

.security-section {
  margin-bottom: 32px;
}

.security-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.password-form {
  max-width: 400px;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.security-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.security-desc {
  font-size: 12px;
  color: #666;
}

.preferences-section {
  margin-bottom: 32px;
}

.preferences-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.preference-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preference-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preference-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.preference-desc {
  font-size: 12px;
  color: #666;
}

.avatar-upload {
  text-align: center;
}

.upload-area {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-area:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.upload-area .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.avatar-preview {
  margin-top: 20px;
}

.avatar-preview img {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
}

.dialog-footer {
  display: flex;
  gap: 12px;
}

@media (max-width: 1200px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
  
  .profile-sidebar {
    order: 1;
  }
  
  .profile-main {
    order: 0;
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }
  
  .tab-content {
    padding: 20px;
  }
  
  .preference-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .preference-control {
    width: 100%;
  }
}
</style>