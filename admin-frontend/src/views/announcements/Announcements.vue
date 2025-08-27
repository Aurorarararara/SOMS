<template>
  <div class="announcements-container">
    <div class="page-header">
      <h2 class="page-title">公告管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        发布公告
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table :data="announcements" v-loading="loading" stripe>
        <el-table-column prop="id" label="公告ID" width="80" />
        <el-table-column prop="title" label="公告标题" min-width="200" />
        <el-table-column prop="type" label="公告类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="readCount" label="阅读量" width="100" align="center" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewAnnouncement(row)">查看</el-button>
            <el-button link type="primary" @click="editAnnouncement(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteAnnouncement(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      :title="isEdit ? '编辑公告' : '发布公告'" 
      width="800px"
      @close="resetForm"
    >
      <el-form :model="announcementForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="announcementForm.type" placeholder="请选择公告类型">
            <el-option label="通知公告" value="notice" />
            <el-option label="系统维护" value="maintenance" />
            <el-option label="紧急通知" value="urgent" />
            <el-option label="活动公告" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="announcementForm.content" 
            type="textarea" 
            :rows="8"
            placeholder="请输入公告内容" 
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="announcementForm.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAnnouncement" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

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
    notice: '通知公告',
    maintenance: '系统维护',
    urgent: '紧急通知',
    activity: '活动公告'
  }
  return textMap[type] || '未知'
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    announcements.value = [
      {
        id: 1,
        title: '系统维护通知',
        type: 'maintenance',
        content: '系统将于本周六进行维护...',
        publisher: '系统管理员',
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
  ElMessage.info(`查看公告: ${announcement.title}`)
}

const editAnnouncement = (announcement) => {
  isEdit.value = true
  Object.assign(announcementForm, announcement)
  showAddDialog.value = true
}

const deleteAnnouncement = async (announcement) => {
  try {
    await ElMessageBox.confirm(`确定要删除公告 ${announcement.title} 吗？`, '删除确认')
    ElMessage.success('删除成功')
    loadAnnouncements()
  } catch (error) {
    // 用户取消删除
  }
}

const saveAnnouncement = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saving.value = true
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(isEdit.value ? '更新成功' : '发布成功')
    showAddDialog.value = false
    loadAnnouncements()
  } catch (error) {
    console.error('表单验证失败:', error)
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