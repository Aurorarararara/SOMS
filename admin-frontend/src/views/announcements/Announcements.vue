<template>
  <div class="announcements-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('announcement.management') }}</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('announcement.add') }}
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table :data="announcements" v-loading="loading" stripe>
        <el-table-column prop="id" :label="$t('announcement.announcementId')" width="80" />
        <el-table-column prop="title" :label="$t('announcement.title')" min-width="200" />
        <el-table-column prop="type" :label="$t('announcement.announcementType')" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" :label="$t('announcement.publisher')" width="100" />
        <el-table-column prop="publishTime" :label="$t('announcement.publishTime')" width="180" />
        <el-table-column prop="readCount" :label="$t('announcement.readCount')" width="100" align="center" />
        <el-table-column :label="$t('announcement.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? $t('announcement.published') : $t('announcement.draft') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('announcement.actions')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewAnnouncement(row)">{{ $t('announcement.view') }}</el-button>
            <el-button link type="primary" @click="editAnnouncement(row)">{{ $t('announcement.edit') }}</el-button>
            <el-button link type="danger" @click="deleteAnnouncement(row)">{{ $t('announcement.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEdit ? $t('announcement.edit') : $t('announcement.add')"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="announcementForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('announcement.title')" prop="title">
          <el-input v-model="announcementForm.title" :placeholder="$t('announcement.enterTitle')" />
        </el-form-item>
        <el-form-item :label="$t('announcement.announcementType')" prop="type">
          <el-select v-model="announcementForm.type" :placeholder="$t('announcement.selectType')">
            <el-option :label="$t('announcement.notice')" value="notice" />
            <el-option :label="$t('announcement.maintenance')" value="maintenance" />
            <el-option :label="$t('announcement.urgent')" value="urgent" />
            <el-option :label="$t('announcement.activity')" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('announcement.content')" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="8"
            :placeholder="$t('announcement.enterContent')"
          />
        </el-form-item>
        <el-form-item :label="$t('announcement.status')">
          <el-radio-group v-model="announcementForm.status">
            <el-radio :label="0">{{ $t('announcement.draft') }}</el-radio>
            <el-radio :label="1">{{ $t('announcement.publishImmediately') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">{{ $t('announcement.cancel') }}</el-button>
        <el-button type="primary" @click="saveAnnouncement" :loading="saving">{{ $t('announcement.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t: $t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const announcements = ref([])
const showAddDialog = ref(false)
const isEdit = ref(false)

const announcementForm = reactive({
  id: null,
  title: '',
  type: '',
  content: '',
  status: 1
})

const formRules = computed(() => ({
  title: [{ required: true, message: $t('announcement.titleRequired'), trigger: 'blur' }],
  type: [{ required: true, message: $t('announcement.typeRequired'), trigger: 'change' }],
  content: [{ required: true, message: $t('announcement.contentRequired'), trigger: 'blur' }]
}))

const formRef = ref(null)

const getTypeColor = (type) => {
  const colorMap = {
    notice: 'primary',
    maintenance: 'warning',
    urgent: 'danger',
    activity: 'success'
  }
  return colorMap[type] || ''
}

const getTypeText = (type) => {
  const textMap = {
    notice: $t('announcement.notice'),
    maintenance: $t('announcement.maintenance'),
    urgent: $t('announcement.urgent'),
    activity: $t('announcement.activity')
  }
  return textMap[type] || $t('announcement.unknown')
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    announcements.value = [
      {
        id: 1,
        title: 'System Maintenance Notice',
        type: 'maintenance',
        content: 'System maintenance will be performed this Saturday...',
        publisher: 'System Administrator',
        publishTime: '2024-01-15 10:00:00',
        readCount: 125,
        status: 1
      }
    ]
  } finally {
    loading.value = false
  }
}

const viewAnnouncement = (announcement) => {
  ElMessage.info(`${$t('announcement.viewAnnouncement')}: ${announcement.title}`)
}

const editAnnouncement = (announcement) => {
  isEdit.value = true
  Object.assign(announcementForm, announcement)
  showAddDialog.value = true
}

const deleteAnnouncement = async (announcement) => {
  try {
    await ElMessageBox.confirm(
      $t('announcement.deleteConfirmMessage', { title: announcement.title }),
      $t('announcement.deleteConfirm')
    )
    ElMessage.success($t('announcement.deleteSuccess'))
    loadAnnouncements()
  } catch (error) {
    // User cancelled deletion
  }
}

const saveAnnouncement = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saving.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(isEdit.value ? $t('announcement.updateSuccess') : $t('announcement.publishSuccess'))
    showAddDialog.value = false
    loadAnnouncements()
  } catch (error) {
    console.error('Form validation failed:', error)
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(announcementForm, {
    id: null,
    title: '',
    type: '',
    content: '',
    status: 1
  })
  isEdit.value = false
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-container {
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

.table-card {
  margin-bottom: 20px;
}
</style>