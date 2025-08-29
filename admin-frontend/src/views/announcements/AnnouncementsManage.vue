<template>
  <div class="announcements-manage-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.announcements') }}</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('announcementsManage.publishAnnouncement') }}
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item :label="$t('announcementsManage.announcementTitle') + ':'">
          <el-input v-model="searchForm.title" :placeholder="$t('announcementsManage.enterTitle')" clearable />
        </el-form-item>
        <el-form-item :label="$t('announcementsManage.announcementType') + ':'">
          <el-select v-model="searchForm.type" :placeholder="$t('announcementsManage.selectType')" clearable>
            <el-option :label="$t('announcementsManage.notice')" value="notice" />
            <el-option :label="$t('announcementsManage.news')" value="news" />
            <el-option :label="$t('announcementsManage.policy')" value="policy" />
            <el-option :label="$t('announcementsManage.other')" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('announcementsManage.publishStatus') + ':'">
          <el-select v-model="searchForm.status" :placeholder="$t('announcementsManage.selectStatus')" clearable>
            <el-option :label="$t('announcementsManage.published')" :value="1" />
            <el-option :label="$t('announcementsManage.unpublished')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAnnouncements">{{ $t('common.search') }}</el-button>
          <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公告列表 -->
    <el-card class="table-card">
      <el-table :data="announcements" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" :label="$t('announcementsManage.announcementTitle')" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('announcementsManage.announcementType')" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" :label="$t('announcementsManage.contentSummary')" min-width="250" show-overflow-tooltip />
        <el-table-column prop="publisherName" :label="$t('announcementsManage.publisher')" width="100" />
        <el-table-column :label="$t('announcementsManage.publishStatus')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? $t('announcementsManage.published') : $t('announcementsManage.unpublished') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" :label="$t('announcementsManage.publishTime')" width="160" />
        <el-table-column prop="readCount" :label="$t('announcementsManage.readCount')" width="80" />
        <el-table-column :label="$t('common.actions')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewAnnouncement(row)">{{ $t('common.view') }}</el-button>
            <el-button link type="primary" @click="editAnnouncement(row)">{{ $t('common.edit') }}</el-button>
            <el-button 
              link 
              :type="row.status === 1 ? 'warning' : 'success'" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下线' : '发布' }}
            </el-button>
            <el-button link type="danger" @click="deleteAnnouncement(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAnnouncements"
          @current-change="loadAnnouncements"
        />
      </div>
    </el-card>

    <!-- 新增/编辑公告对话框 -->
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
          <el-select v-model="announcementForm.type" placeholder="请选择类型" style="width: 200px">
            <el-option label="通知公告" value="notice" />
            <el-option label="新闻动态" value="news" />
            <el-option label="制度规定" value="policy" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容摘要" prop="summary">
          <el-input 
            v-model="announcementForm.summary" 
            type="textarea" 
            :rows="2"
            placeholder="请输入内容摘要（不超过200字）" 
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="announcementForm.content" 
            type="textarea" 
            :rows="8"
            placeholder="请输入公告详细内容" 
          />
        </el-form-item>
        <el-form-item label="是否置顶" prop="isTop">
          <el-switch v-model="announcementForm.isTop" />
        </el-form-item>
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="announcementForm.status">
            <el-radio :label="1">立即发布</el-radio>
            <el-radio :label="0">保存草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAnnouncement" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 公告详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="公告详情" width="700px">
      <div class="announcement-detail" v-if="selectedAnnouncement">
        <div class="detail-header">
          <h2 class="detail-title">{{ selectedAnnouncement.title }}</h2>
          <div class="detail-meta">
            <span class="meta-item">
              <el-tag :type="getTypeColor(selectedAnnouncement.type)">
                {{ getTypeText(selectedAnnouncement.type) }}
              </el-tag>
            </span>
            <span class="meta-item">发布人：{{ selectedAnnouncement.publisherName }}</span>
            <span class="meta-item">发布时间：{{ selectedAnnouncement.publishTime }}</span>
            <span class="meta-item">阅读量：{{ selectedAnnouncement.readCount }}</span>
          </div>
        </div>
        <div class="detail-content">
          <div class="content-summary">
            <strong>内容摘要：</strong>
            <p>{{ selectedAnnouncement.summary }}</p>
          </div>
          <div class="content-body">
            <strong>详细内容：</strong>
            <div class="content-text" v-html="selectedAnnouncement.content"></div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const announcements = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const selectedAnnouncement = ref(null)

// 表单数据
const searchForm = reactive({
  title: '',
  type: '',
  status: ''
})

const announcementForm = reactive({
  id: null,
  title: '',
  type: '',
  summary: '',
  content: '',
  isTop: false,
  status: 1
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择公告类型', trigger: 'change' }
  ],
  summary: [
    { required: true, message: '请输入内容摘要', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

const formRef = ref(null)

// 获取类型颜色
const getTypeColor = (type) => {
  const colorMap = {
    notice: 'primary',
    news: 'success',
    policy: 'warning',
    other: 'info'
  }
  return colorMap[type] || 'info'
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = {
    notice: '通知公告',
    news: '新闻动态',
    policy: '制度规定',
    other: '其他'
  }
  return textMap[type] || '未知'
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    announcements.value = [
      {
        id: 1,
        title: '关于国庆节放假安排的通知',
        type: 'notice',
        summary: '根据国务院办公厅通知精神，现将2025年国庆节放假安排通知如下...',
        content: '各部门：\n\n根据国务院办公厅通知精神，现将2025年国庆节放假安排通知如下：\n\n一、放假时间：10月1日至10月7日，共7天。\n二、上班时间：10月8日（星期二）正常上班。\n三、注意事项：\n1. 各部门做好放假前的工作安排；\n2. 值班人员按照值班表正常值班；\n3. 注意节日期间的安全防范。\n\n特此通知。\n\n人事部\n2025年8月25日',
        publisherName: '张管理员',
        publishTime: '2025-08-25 14:30:00',
        readCount: 256,
        status: 1,
        isTop: true
      },
      {
        id: 2,
        title: '员工手册更新说明',
        type: 'policy',
        summary: '为了更好地规范员工行为，提高工作效率，现对员工手册进行更新...',
        content: '各位员工：\n\n为了更好地规范员工行为，提高工作效率，现对员工手册进行更新，主要更新内容如下：\n\n1. 考勤管理制度优化\n2. 请假流程简化\n3. 绩效考核标准调整\n4. 培训发展政策更新\n\n请各位员工仔细阅读新版员工手册，并严格遵守相关规定。\n\n人事部\n2025年8月24日',
        publisherName: '李人事',
        publishTime: '2025-08-24 10:15:00',
        readCount: 189,
        status: 1,
        isTop: false
      }
    ]
    
    total.value = 25
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    title: '',
    type: '',
    status: ''
  })
  loadAnnouncements()
}

// 查看公告详情
const viewAnnouncement = (announcement) => {
  selectedAnnouncement.value = announcement
  showDetailDialog.value = true
}

// 编辑公告
const editAnnouncement = (announcement) => {
  isEdit.value = true
  Object.assign(announcementForm, { ...announcement })
  showAddDialog.value = true
}

// 切换发布状态
const toggleStatus = async (announcement) => {
  const action = announcement.status === 1 ? '下线' : '发布'
  try {
    await ElMessageBox.confirm(
      `确定要${action}公告"${announcement.title}"吗？`,
      '状态切换确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    ElMessage.success(`${action}成功`)
    loadAnnouncements()
  } catch (error) {
    // 用户取消操作
  }
}

// 删除公告
const deleteAnnouncement = async (announcement) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告"${announcement.title}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟删除API调用
    ElMessage.success('删除成功')
    loadAnnouncements()
  } catch (error) {
    // 用户取消删除
  }
}

// 保存公告
const saveAnnouncement = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    // 模拟保存API调用
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

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(announcementForm, {
    id: null,
    title: '',
    type: '',
    summary: '',
    content: '',
    isTop: false,
    status: 1
  })
  isEdit.value = false
}

// 组件挂载
onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-manage-container {
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.announcement-detail {
  padding: 10px 0;
}

.detail-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.detail-title {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  font-size: 14px;
  color: #666;
}

.meta-item {
  display: flex;
  align-items: center;
}

.detail-content {
  line-height: 1.6;
}

.content-summary {
  margin-bottom: 20px;
}

.content-summary p {
  margin: 8px 0;
  color: #666;
  font-style: italic;
}

.content-body strong {
  color: #333;
  font-size: 16px;
}

.content-text {
  margin-top: 10px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  white-space: pre-line;
  color: #333;
  line-height: 1.8;
}
</style>