<template>
  <div class="system-config-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.systemConfig') }}</h2>
      <el-button type="primary" @click="saveConfig">
        <el-icon><Check /></el-icon>
        {{ $t('systemConfig.saveConfig') }}
      </el-button>
    </div>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="$t('systemConfig.basicSettings')" name="basic">
          <el-form :model="configForm" label-width="150px">
            <el-form-item :label="$t('systemConfig.systemName') + ':'">
              <el-input v-model="configForm.systemName" />
            </el-form-item>
            <el-form-item :label="$t('systemConfig.systemVersion') + ':'">
              <el-input v-model="configForm.systemVersion" />
            </el-form-item>
            <el-form-item :label="$t('systemConfig.adminEmail') + ':'">
              <el-input v-model="configForm.adminEmail" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane :label="$t('systemConfig.attendanceSettings')" name="attendance">
          <el-form :model="configForm" label-width="150px">
            <el-form-item :label="$t('systemConfig.standardWorkHours') + ':'">
              <el-input-number v-model="configForm.workHours" :min="1" :max="12" />
              <span style="margin-left: 8px;">{{ $t('systemConfig.hoursPerDay') }}</span>
            </el-form-item>
            <el-form-item :label="$t('systemConfig.lateThreshold') + ':'">
              <el-input-number v-model="configForm.lateThreshold" :min="1" :max="60" />
              <span style="margin-left: 8px;">{{ $t('systemConfig.minutes') }}</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const { t: $t } = useI18n()

const activeTab = ref('basic')

const configForm = reactive({
  systemName: '办公管理系统',
  systemVersion: '1.0.0',
  adminEmail: 'admin@office.com',
  workHours: 8,
  lateThreshold: 15
})

const saveConfig = () => {
  ElMessage.success('配置保存成功')
}
</script>

<style scoped>
.system-config-container {
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
</style>