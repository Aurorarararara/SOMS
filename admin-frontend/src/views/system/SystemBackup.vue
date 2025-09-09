<template>
  <div class="system-backup-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.systemBackup') }}</h2>
      <el-button type="primary" @click="createBackup">
        <el-icon><FolderAdd /></el-icon>
        {{ $t('systemBackup.createBackup') }}
      </el-button>
    </div>

    <el-card>
      <el-table :data="backups" v-loading="loading" stripe>
        <el-table-column prop="id" :label="$t('systemBackup.backupId')" width="80" />
        <el-table-column prop="name" :label="$t('systemBackup.backupName')" width="200" />
        <el-table-column prop="size" :label="$t('systemBackup.fileSize')" width="120" />
        <el-table-column prop="createTime" :label="$t('systemBackup.createTime')" width="180" />
        <el-table-column prop="description" :label="$t('systemBackup.backupDescription')" min-width="200" />
        <el-table-column :label="$t('common.actions')" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="downloadBackup(row)">{{ $t('systemBackup.download') }}</el-button>
            <el-button link type="success" @click="restoreBackup(row)">{{ $t('systemBackup.restore') }}</el-button>
            <el-button link type="danger" @click="deleteBackup(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { FolderAdd } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t: $t } = useI18n()

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