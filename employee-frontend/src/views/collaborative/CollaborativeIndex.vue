<template>
  <div class="collaborative-page">
    <div class="page-header">
      <div class="header-left">
        <h2>协同编辑</h2>
        <p>创建和管理您的协同文档</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="createNewDocument">
          <i class="fa fa-plus"></i> 新建文档
        </el-button>
      </div>
    </div>

    <!-- 快速创建 -->
    <div class="quick-create">
      <h3>快速创建</h3>
      <div class="create-options">
        <div class="create-option" @click="createDocument('richtext')">
          <div class="option-icon richtext">
            <i class="fa fa-file-text"></i>
          </div>
          <div class="option-info">
            <h4>富文本文档</h4>
            <p>支持格式化文本，类似Google Docs</p>
          </div>
        </div>
        
        <div class="create-option" @click="createDocument('markdown')">
          <div class="option-icon markdown">
            <i class="fa fa-markdown"></i>
          </div>
          <div class="option-info">
            <h4>Markdown文档</h4>
            <p>支持Markdown语法的轻量级文档</p>
          </div>
        </div>
        
        <div class="create-option" @click="createDocument('code')">
          <div class="option-icon code">
            <i class="fa fa-code"></i>
          </div>
          <div class="option-info">
            <h4>代码文档</h4>
            <p>支持语法高亮的代码编辑器</p>
          </div>
        </div>
        
        <div class="create-option" @click="createDocument('table')">
          <div class="option-icon table">
            <i class="fa fa-table"></i>
          </div>
          <div class="option-info">
            <h4>表格文档</h4>
            <p>在线协同编辑电子表格</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 文档列表 -->
    <div class="document-list">
      <div class="list-header">
        <h3>我的文档</h3>
        <div class="list-filters">
          <el-select v-model="filterType" placeholder="文档类型" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="富文本" value="richtext"></el-option>
            <el-option label="Markdown" value="markdown"></el-option>
            <el-option label="代码" value="code"></el-option>
            <el-option label="表格" value="table"></el-option>
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索文档"
            style="width: 200px"
            clearable
          >
            <template #prefix>
              <i class="fa fa-search"></i>
            </template>
          </el-input>
        </div>
      </div>

      <div class="documents-grid" v-if="documents.length > 0">
        <div
          v-for="doc in filteredDocuments"
          :key="doc.id"
          class="document-card"
          @click="openDocument(doc)"
        >
          <div class="card-header">
            <div class="doc-type-icon" :class="doc.docType">
              <i :class="getDocTypeIcon(doc.docType)"></i>
            </div>
            <div class="card-actions" @click.stop>
              <el-dropdown trigger="click">
                <i class="fa fa-ellipsis-v"></i>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openDocument(doc)">
                      <i class="fa fa-eye"></i> 查看
                    </el-dropdown-item>
                    <el-dropdown-item @click="shareDocument(doc)">
                      <i class="fa fa-share"></i> 分享
                    </el-dropdown-item>
                    <el-dropdown-item @click="copyDocument(doc)">
                      <i class="fa fa-copy"></i> 复制
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteDocument(doc)" divided>
                      <i class="fa fa-trash"></i> 删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          
          <div class="card-content">
            <h4>{{ doc.title }}</h4>
            <p class="doc-meta">
              <span class="doc-type">{{ getDocTypeName(doc.docType) }}</span>
              <span class="update-time">{{ formatTime(doc.updateTime) }}</span>
            </p>
            <p class="doc-preview" v-if="doc.content">
              {{ getDocumentPreview(doc) }}
            </p>
          </div>
          
          <div class="card-footer" v-if="getActiveUsers(doc.id).length > 0">
            <div class="active-users">
              <div 
                v-for="user in getActiveUsers(doc.id).slice(0, 3)"
                :key="user.sessionId"
                class="user-avatar"
                :style="{ backgroundColor: user.userColor }"
                :title="user.userName"
              >
                {{ user.userName?.charAt(0) || '?' }}
              </div>
              <span v-if="getActiveUsers(doc.id).length > 3" class="more-users">
                +{{ getActiveUsers(doc.id).length - 3 }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <i class="fa fa-file-text-o"></i>
        <h3>暂无文档</h3>
        <p>点击上方按钮创建您的第一个协同文档</p>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新建文档对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="新建文档"
      width="500px"
      :before-close="handleCreateDialogClose"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="80px">
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入文档标题" />
        </el-form-item>
        <el-form-item label="文档类型" prop="docType">
          <el-select v-model="createForm.docType" placeholder="选择文档类型" style="width: 100%">
            <el-option label="富文本文档" value="richtext">
              <i class="fa fa-file-text"></i> 富文本文档
            </el-option>
            <el-option label="Markdown文档" value="markdown">
              <i class="fa fa-markdown"></i> Markdown文档
            </el-option>
            <el-option label="代码文档" value="code">
              <i class="fa fa-code"></i> 代码文档
            </el-option>
            <el-option label="表格文档" value="table">
              <i class="fa fa-table"></i> 表格文档
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="编程语言" prop="language" v-if="createForm.docType === 'code'">
          <el-select v-model="createForm.language" placeholder="选择编程语言" style="width: 100%">
            <el-option label="JavaScript" value="javascript"></el-option>
            <el-option label="TypeScript" value="typescript"></el-option>
            <el-option label="Python" value="python"></el-option>
            <el-option label="Java" value="java"></el-option>
            <el-option label="C++" value="cpp"></el-option>
            <el-option label="HTML" value="html"></el-option>
            <el-option label="CSS" value="css"></el-option>
            <el-option label="SQL" value="sql"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="权限设置" prop="isPublic">
          <el-radio-group v-model="createForm.isPublic">
            <el-radio :label="false">私有</el-radio>
            <el-radio :label="true">公开</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateDocument" :loading="creating">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import collaborativeApi from '@/api/collaborative'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

// 状态管理
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const documents = ref([])
const activeSessions = ref({}) // documentId -> sessions
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)

// 筛选和搜索
const filterType = ref('')
const searchKeyword = ref('')

// 新建文档
const showCreateDialog = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  title: '',
  docType: 'richtext',
  language: 'javascript',
  isPublic: false
})

const createRules = {
  title: [
    { required: true, message: '请输入文档标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  docType: [
    { required: true, message: '请选择文档类型', trigger: 'change' }
  ]
}

// 计算属性
const filteredDocuments = computed(() => {
  let result = documents.value

  // 按类型筛选
  if (filterType.value) {
    result = result.filter(doc => doc.docType === filterType.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(doc => 
      doc.title.toLowerCase().includes(keyword) ||
      (doc.content && doc.content.toLowerCase().includes(keyword))
    )
  }

  return result
})

// 生命周期
onMounted(() => {
  loadDocuments()
})

// 监听筛选条件变化
watch([filterType, searchKeyword], () => {
  currentPage.value = 1
  loadDocuments()
})

// 方法
async function loadDocuments() {
  try {
    loading.value = true
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    }

    const response = await collaborativeApi.pageDocuments(params)
    documents.value = response.data.records || []
    total.value = response.data.total || 0

    // 加载活跃会话信息
    await loadActiveSessions()

  } catch (error) {
    console.error('加载文档列表失败:', error)
    ElMessage.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

async function loadActiveSessions() {
  for (const doc of documents.value) {
    try {
      const response = await collaborativeApi.getActiveSessions(doc.id)
      activeSessions.value[doc.id] = response.data || []
    } catch (error) {
      console.error(`加载文档 ${doc.id} 的活跃会话失败:`, error)
    }
  }
}

function createNewDocument() {
  showCreateDialog.value = true
  resetCreateForm()
}

function createDocument(type) {
  createForm.docType = type
  showCreateDialog.value = true
}

async function handleCreateDocument() {
  try {
    await createFormRef.value.validate()
    creating.value = true

    const data = {
      title: createForm.title,
      documentType: createForm.docType,
      content: getInitialContent(createForm.docType),
      language: createForm.docType === 'code' ? createForm.language : undefined,
      isPublic: createForm.isPublic,
      userId: userStore.user?.id
    }

    const response = await collaborativeApi.createDocument(data)
    const newDoc = response.data

    ElMessage.success('文档创建成功')
    showCreateDialog.value = false
    
    // 跳转到编辑器
    openDocument(newDoc)

  } catch (error) {
    console.error('创建文档失败:', error)
    ElMessage.error('创建文档失败')
  } finally {
    creating.value = false
  }
}

function openDocument(doc) {
  const routeMap = {
    richtext: '/collaborative/richtext',
    markdown: '/collaborative/markdown',
    code: '/collaborative/code',
    table: '/collaborative/table'
  }

  const route = routeMap[doc.docType] || '/collaborative/richtext'
  router.push(`${route}/${doc.id}`)
}

async function shareDocument(doc) {
  try {
    const shareUrl = `${window.location.origin}/collaborative/${doc.docType}/${doc.id}`
    await navigator.clipboard.writeText(shareUrl)
    ElMessage.success('分享链接已复制到剪贴板')
  } catch (error) {
    console.error('复制分享链接失败:', error)
    ElMessage.error('复制分享链接失败')
  }
}

async function copyDocument(doc) {
  try {
    const data = {
      newTitle: `${doc.title} - 副本`,
      userId: userStore.user?.id
    }

    await collaborativeApi.copyDocument(doc.id, data)
    ElMessage.success('文档复制成功')
    loadDocuments()

  } catch (error) {
    console.error('复制文档失败:', error)
    ElMessage.error('复制文档失败')
  }
}

async function deleteDocument(doc) {
  try {
    await ElMessageBox.confirm(
      `确认删除文档"${doc.title}"吗？删除后不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await collaborativeApi.deleteDocument(doc.id, userStore.user?.id)
    ElMessage.success('文档删除成功')
    loadDocuments()

  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文档失败:', error)
      ElMessage.error('删除文档失败')
    }
  }
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadDocuments()
}

function handleCurrentChange(page) {
  currentPage.value = page
  loadDocuments()
}

function handleCreateDialogClose() {
  showCreateDialog.value = false
  resetCreateForm()
}

function resetCreateForm() {
  createForm.title = ''
  createForm.docType = 'richtext'
  createForm.language = 'javascript'
  createForm.isPublic = false
  createFormRef.value?.clearValidate()
}

// 工具函数
function getDocTypeIcon(type) {
  const iconMap = {
    richtext: 'fa fa-file-text',
    markdown: 'fa fa-markdown',
    code: 'fa fa-code',
    table: 'fa fa-table'
  }
  return iconMap[type] || 'fa fa-file'
}

function getDocTypeName(type) {
  const nameMap = {
    richtext: '富文本',
    markdown: 'Markdown',
    code: '代码',
    table: '表格'
  }
  return nameMap[type] || '未知'
}

function getActiveUsers(documentId) {
  return activeSessions.value[documentId] || []
}

function getDocumentPreview(doc) {
  if (!doc.content) return '空白文档'
  
  try {
    if (doc.docType === 'richtext') {
      const content = JSON.parse(doc.content)
      // 提取纯文本内容
      return extractTextFromQuillDelta(content).substring(0, 100) + '...'
    } else {
      return doc.content.substring(0, 100) + '...'
    }
  } catch (error) {
    return '无法预览'
  }
}

function extractTextFromQuillDelta(delta) {
  if (!delta.ops) return ''
  return delta.ops
    .filter(op => typeof op.insert === 'string')
    .map(op => op.insert)
    .join('')
}

function getInitialContent(docType) {
  const contentMap = {
    richtext: JSON.stringify({ ops: [{ insert: '\n' }] }),
    markdown: '# 新建文档\n\n开始编写您的Markdown文档...',
    code: '// 开始编写您的代码\nfunction hello() {\n    console.log("Hello, World!");\n}',
    table: JSON.stringify({ 
      sheets: [{ 
        name: 'Sheet1', 
        data: [['A1', 'B1'], ['A2', 'B2']] 
      }] 
    })
  }
  return contentMap[docType] || ''
}

function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.collaborative-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e8e8e8;
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.header-left p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.quick-create {
  margin-bottom: 40px;
}

.quick-create h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.create-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.create-option {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.create-option:hover {
  border-color: #1890ff;
  background: #f0f9ff;
  transform: translateY(-2px);
}

.option-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.option-icon.richtext {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.option-icon.markdown {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.option-icon.code {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.option-icon.table {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.option-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.option-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.document-list {
  margin-bottom: 32px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.list-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.list-filters {
  display: flex;
  gap: 16px;
}

.documents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.document-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.document-card:hover {
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 12px;
}

.doc-type-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
}

.doc-type-icon.richtext {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.doc-type-icon.markdown {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.doc-type-icon.code {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.doc-type-icon.table {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-actions {
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  color: #999;
}

.card-actions:hover {
  background: #f5f5f5;
  color: #333;
}

.card-content {
  padding: 0 20px 16px;
}

.card-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-meta {
  display: flex;
  justify-content: space-between;
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #999;
}

.doc-preview {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  padding: 12px 20px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.active-users {
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 10px;
  font-weight: 600;
}

.more-users {
  font-size: 12px;
  color: #666;
  margin-left: 4px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 20px;
  color: #ddd;
}

.empty-state h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #666;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>