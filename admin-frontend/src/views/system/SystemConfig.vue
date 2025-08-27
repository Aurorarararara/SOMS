<template>
  <div class="system-config-container">
    <div class="page-header">
      <h2 class="page-title">系统配置</h2>
      <el-button type="primary" @click="saveConfig">
        <el-icon><Check /></el-icon>
        保存配置
      </el-button>
    </div>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="configForm" label-width="150px">
            <el-form-item label="系统名称:">
              <el-input v-model="configForm.systemName" />
            </el-form-item>
            <el-form-item label="系统版本:">
              <el-input v-model="configForm.systemVersion" />
            </el-form-item>
            <el-form-item label="管理员邮箱:">
              <el-input v-model="configForm.adminEmail" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="考勤设置" name="attendance">
          <el-form :model="configForm" label-width="150px">
            <el-form-item label="标准工作时间:">
              <el-input-number v-model="configForm.workHours" :min="1" :max="12" />
              <span style="margin-left: 8px;">小时/天</span>
            </el-form-item>
            <el-form-item label="迟到阈值:">
              <el-input-number v-model="configForm.lateThreshold" :min="1" :max="60" />
              <span style="margin-left: 8px;">分钟</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

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