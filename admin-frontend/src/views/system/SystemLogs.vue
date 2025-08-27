<template>
  <div class="system-logs-container">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
      <el-button type="primary">
        <el-icon><Download /></el-icon>
        导出日志
      </el-button>
    </div>

    <el-card>
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="action" label="操作类型" width="120" />
        <el-table-column prop="description" label="操作描述" min-width="200" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="time" label="操作时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'

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