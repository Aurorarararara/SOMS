<template>
  <div class="system-logs-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.systemLogs') }}</h2>
      <el-button type="primary">
        <el-icon><Download /></el-icon>
        {{ $t('systemLogs.exportLogs') }}
      </el-button>
    </div>

    <el-card>
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" :label="$t('systemLogs.logId')" width="80" />
        <el-table-column prop="username" :label="$t('systemLogs.operationUser')" width="120" />
        <el-table-column prop="action" :label="$t('systemLogs.operationType')" width="120" />
        <el-table-column prop="description" :label="$t('systemLogs.operationDescription')" min-width="200" />
        <el-table-column prop="ip" :label="$t('systemLogs.ipAddress')" width="140" />
        <el-table-column prop="time" :label="$t('systemLogs.operationTime')" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Download } from '@element-plus/icons-vue'

const { t: $t } = useI18n()

const loading = ref(false)
const logs = ref([])

const loadLogs = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    logs.value = [
      {
        id: 1,
        username: '管理员',
        action: '登录',
        description: '用户登录系统',
        ip: '192.168.1.100',
        time: '2024-01-15 09:00:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.system-logs-container {
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