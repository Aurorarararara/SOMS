<template>
  <div class="system-backup-container">
    <div class="page-header">
      <h2 class="page-title">数据备份</h2>
      <el-button type="primary" @click="createBackup">
        <el-icon><FolderAdd /></el-icon>
        创建备份
      </el-button>
    </div>

    <el-card>
      <el-table :data="backups" v-loading="loading" stripe>
        <el-table-column prop="id" label="备份ID" width="80" />
        <el-table-column prop="name" label="备份名称" width="200" />
        <el-table-column prop="size" label="文件大小" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="description" label="备份描述" min-width="200" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="downloadBackup(row)">下载</el-button>
            <el-button link type="success" @click="restoreBackup(row)">还原</el-button>
            <el-button link type="danger" @click="deleteBackup(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { FolderAdd } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const backups = ref([])

const loadBackups = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    backups.value = [
      {
        id: 1,
        name: 'backup_20240115.sql',
        size: '15.2 MB',
        createTime: '2024-01-15 02:00:00',
        description: '系统自动备份'
      }
    ]
  } finally {
    loading.value = false
  }
}

const createBackup = () => {
  ElMessage.success('备份创建功能开发中...')
}

const downloadBackup = (backup) => {
  ElMessage.info(`下载备份文件: ${backup.name}`)
}

const restoreBackup = async (backup) => {
  try {
    await ElMessageBox.confirm(`确定要还原备份 ${backup.name} 吗？`, '还原确认')
    ElMessage.success('备份还原成功')
  } catch (error) {
    // 用户取消操作
  }
}

const deleteBackup = async (backup) => {
  try {
    await ElMessageBox.confirm(`确定要删除备份 ${backup.name} 吗？`, '删除确认')
    ElMessage.success('备份删除成功')
    loadBackups()
  } catch (error) {
    // 用户取消操作
  }
}

onMounted(() => {
  loadBackups()
})
</script>

<style scoped>
.system-backup-container {
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