<template>
  <div class="notification-settings">
    <div class="page-header">
      <h1>通知设置</h1>
      <p>管理您的通知偏好设置</p>
    </div>

    <div class="settings-content" v-loading="loading">
      <el-form :model="settings" label-width="150px">
        <el-card class="setting-card">
          <template #header>
            <span>任务通知</span>
          </template>
          
          <el-form-item label="任务分配通知">
            <el-switch v-model="settings.taskAssigned" />
            <div class="setting-description">当有新任务分配给您时接收通知</div>
          </el-form-item>
          
          <el-form-item label="任务更新通知">
            <el-switch v-model="settings.taskUpdated" />
            <div class="setting-description">当您的任务状态发生变化时接收通知</div>
          </el-form-item>
          
          <el-form-item label="任务完成通知">
            <el-switch v-model="settings.taskCompleted" />
            <div class="setting-description">当您完成任务时接收确认通知</div>
          </el-form-item>
          
          <el-form-item label="任务逾期提醒">
            <el-switch v-model="settings.taskOverdue" />
            <div class="setting-description">当任务即将逾期或已逾期时接收提醒</div>
          </el-form-item>
          
          <el-form-item label="任务评论通知">
            <el-switch v-model="settings.taskCommented" />
            <div class="setting-description">当有人评论您的任务时接收通知</div>
          </el-form-item>
        </el-card>

        <el-card class="setting-card">
          <template #header>
            <span>系统通知</span>
          </template>
          
          <el-form-item label="系统公告">
            <el-switch v-model="settings.systemNotice" />
            <div class="setting-description">接收系统重要公告和更新通知</div>
          </el-form-item>
          
          <el-form-item label="用户提及">
            <el-switch v-model="settings.userMention" />
            <div class="setting-description">当有人在评论中@您时接收通知</div>
          </el-form-item>
        </el-card>

        <el-card class="setting-card">
          <template #header>
            <span>通知方式</span>
          </template>
          
          <el-form-item label="浏览器通知">
            <el-switch v-model="settings.browserNotification" @change="handleBrowserNotificationChange" />
            <div class="setting-description">在浏览器中显示桌面通知</div>
          </el-form-item>
          
          <el-form-item label="邮件通知">
            <el-switch v-model="settings.emailNotification" />
            <div class="setting-description">通过邮件接收重要通知</div>
          </el-form-item>
          
          <el-form-item label="通知频率">
            <el-select v-model="settings.notificationFrequency" style="width: 200px;">
              <el-option label="实时通知" value="realtime" />
              <el-option label="每小时汇总" value="hourly" />
              <el-option label="每日汇总" value="daily" />
            </el-select>
            <div class="setting-description">选择接收通知的频率</div>
          </el-form-item>
        </el-card>

        <el-card class="setting-card">
          <template #header>
            <span>免打扰时间</span>
          </template>
          
          <el-form-item label="启用免打扰">
            <el-switch v-model="settings.doNotDisturb" />
            <div class="setting-description">在指定时间段内不接收通知</div>
          </el-form-item>
          
          <el-form-item label="免打扰时间" v-if="settings.doNotDisturb">
            <el-time-picker
              v-model="settings.doNotDisturbStart"
              placeholder="开始时间"
              style="width: 120px; margin-right: 10px;"
            />
            <span>至</span>
            <el-time-picker
              v-model="settings.doNotDisturbEnd"
              placeholder="结束时间"
              style="width: 120px; margin-left: 10px;"
            />
            <div class="setting-description">在此时间段内不会收到通知</div>
          </el-form-item>
        </el-card>

        <div class="form-actions">
          <el-button type="primary" @click="saveSettings" :loading="saving">
            保存设置
          </el-button>
          <el-button @click="resetSettings">重置</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as notificationApi from '@/api/notificationApi'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const settings = ref({
  // 任务通知
  taskAssigned: true,
  taskUpdated: true,
  taskCompleted: true,
  taskOverdue: true,
  taskCommented: true,
  
  // 系统通知
  systemNotice: true,
  userMention: true,
  
  // 通知方式
  browserNotification: false,
  emailNotification: true,
  notificationFrequency: 'realtime',
  
  // 免打扰
  doNotDisturb: false,
  doNotDisturbStart: null,
  doNotDisturbEnd: null
})

// 加载通知设置
const loadSettings = async () => {
  loading.value = true
  try {
    const response = await notificationApi.getNotificationSettings()
    if (response.code === 200) {
      Object.assign(settings.value, response.data)
    }
  } catch (error) {
    console.error('加载通知设置失败:', error)
    ElMessage.error('加载通知设置失败')
  } finally {
    loading.value = false
  }
}

// 保存设置
const saveSettings = async () => {
  saving.value = true
  try {
    const response = await notificationApi.updateNotificationSettings(settings.value)
    if (response.code === 200) {
      ElMessage.success('设置保存成功')
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    saving.value = false
  }
}

// 重置设置
const resetSettings = () => {
  loadSettings()
}

// 处理浏览器通知权限
const handleBrowserNotificationChange = (enabled) => {
  if (enabled) {
    if ('Notification' in window) {
      if (Notification.permission === 'default') {
        Notification.requestPermission().then(permission => {
          if (permission !== 'granted') {
            settings.value.browserNotification = false
            ElMessage.warning('需要授权浏览器通知权限')
          }
        })
      } else if (Notification.permission === 'denied') {
        settings.value.browserNotification = false
        ElMessage.warning('浏览器通知权限已被拒绝，请在浏览器设置中开启')
      }
    } else {
      settings.value.browserNotification = false
      ElMessage.warning('当前浏览器不支持桌面通知')
    }
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.notification-settings {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.setting-card {
  margin-bottom: 20px;
}

.setting-description {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
