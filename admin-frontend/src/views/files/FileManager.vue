<template>
  <div class="file-manager">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item 
            v-for="item in breadcrumb" 
            :key="item.id"
            :to="{ path: '/files', query: { folderId: item.id } }"
          >
            {{ item.name }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件..."
          prefix-icon="Search"
          size="small"
          style="width: 200px; margin-right: 10px;"
          @keyup.enter="searchFiles"
        />
        <el-button-group size="small">
          <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
            <el-icon><List /></el-icon>
          </el-button>
        </el-button-group>
        <el-dropdown @command="handleToolbarCommand" style="margin-left: 10px;">
          <el-button size="small">
            操作 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="upload">
                <el-icon><Upload /></el-icon> 上传文件
              </el-dropdown-item>
              <el-dropdown-item command="createFolder">
                <el-icon><FolderAdd /></el-icon> 新建文件夹
              </el-dropdown-item>
              <el-dropdown-item command="refresh">
                <el-icon><Refresh /></el-icon> 刷新
              </el-dropdown-item>
              <el-dropdown-item divided command="selectAll">
                <el-icon><Select /></el-icon> 全选
              </el-dropdown-item>
              <el-dropdown-item command="clearSelection">
                <el-icon><Close /></el-icon> 取消选择
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-section">
          <div class="section-title">快速访问</div>
          <div class="quick-access">
            <div 
              v-for="item in quickAccess" 
              :key="item.key"
              class="quick-item"
              :class="{ active: currentView === item.key }"
              @click="switchView(item.key)"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <span class="count" v-if="item.count">{{ item.count }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-section">
          <div class="section-title">文件夹</div>
          <div class="folder-tree">
            <el-tree
              ref="folderTree"
              :data="folderTreeData"
              :props="treeProps"
              node-key="id"
              :expand-on-click-node="false"
              :highlight-current="true"
              @node-click="handleFolderClick"
              @node-contextmenu="handleFolderContextMenu"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon><component :is="getFolderIcon(data.folderType)" /></el-icon>
                  <span class="node-label">{{ node.label }}</span>
                  <span class="node-count" v-if="data.fileCount">({{ data.fileCount }})</span>
                </div>
              </template>
            </el-tree>
          </div>
        </div>

        <div class="sidebar-section">
          <div class="section-title">标签</div>
          <div class="tags-list">
            <el-tag
              v-for="tag in tags"
              :key="tag.id"
              :color="tag.tagColor"
              size="small"
              class="tag-item"
              @click="filterByTag(tag.id)"
            >
              {{ tag.tagName }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 文件列表区域 -->
      <div class="content-area">
        <!-- 选择工具栏 -->
        <div v-if="selectedItems.length > 0" class="selection-toolbar">
          <div class="selection-info">
            已选择 {{ selectedItems.length }} 项
          </div>
          <div class="selection-actions">
            <el-button size="small" @click="downloadSelected">下载</el-button>
            <el-button size="small" @click="moveSelected">移动</el-button>
            <el-button size="small" @click="copySelected">复制</el-button>
            <el-button size="small" @click="shareSelected">分享</el-button>
            <el-button size="small" type="danger" @click="deleteSelected">删除</el-button>
          </div>
        </div>

        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="grid-view">
          <div class="file-grid">
            <div
              v-for="item in fileList"
              :key="item.id"
              class="file-item"
              :class="{ 
                selected: selectedItems.includes(item.id),
                folder: item.type === 'folder'
              }"
              @click="handleItemClick(item)"
              @dblclick="handleItemDoubleClick(item)"
              @contextmenu.prevent="handleItemContextMenu(item, $event)"
            >
              <div class="file-icon">
                <el-icon v-if="item.type === 'folder'" size="48">
                  <component :is="getFolderIcon(item.folderType)" />
                </el-icon>
                <div v-else class="file-thumbnail">
                  <img v-if="item.thumbnailPath" :src="item.thumbnailPath" alt="thumbnail" />
                  <el-icon v-else size="48">
                    <component :is="getFileIcon(item.fileType)" />
                  </el-icon>
                </div>
              </div>
              <div class="file-name" :title="item.name">{{ item.name }}</div>
              <div class="file-info">
                <span v-if="item.type === 'file'" class="file-size">{{ item.fileSizeFormatted }}</span>
                <span class="file-date">{{ formatDate(item.updatedAt) }}</span>
              </div>
              <div class="file-actions">
                <el-dropdown @command="(command) => handleItemAction(command, item)">
                  <el-button size="small" text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="item.type === 'file'" command="preview">预览</el-dropdown-item>
                      <el-dropdown-item command="download">下载</el-dropdown-item>
                      <el-dropdown-item command="rename">重命名</el-dropdown-item>
                      <el-dropdown-item command="move">移动</el-dropdown-item>
                      <el-dropdown-item command="copy">复制</el-dropdown-item>
                      <el-dropdown-item command="share">分享</el-dropdown-item>
                      <el-dropdown-item command="favorite">
                        {{ item.isFavorite ? '取消收藏' : '收藏' }}
                      </el-dropdown-item>
                      <el-dropdown-item divided command="properties">属性</el-dropdown-item>
                      <el-dropdown-item command="delete" style="color: #f56c6c;">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else class="list-view">
          <el-table
            :data="fileList"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
            @row-dblclick="handleRowDoubleClick"
            @row-contextmenu="handleRowContextMenu"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column label="名称" min-width="300">
              <template #default="{ row }">
                <div class="file-name-cell">
                  <el-icon class="file-icon">
                    <component :is="row.type === 'folder' ? getFolderIcon(row.folderType) : getFileIcon(row.fileType)" />
                  </el-icon>
                  <span class="name">{{ row.name }}</span>
                  <div class="tags" v-if="row.tags && row.tags.length">
                    <el-tag v-for="tag in row.tags" :key="tag" size="small">{{ tag }}</el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="大小" width="120">
              <template #default="{ row }">
                {{ row.type === 'file' ? row.fileSizeFormatted : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                {{ row.type === 'folder' ? '文件夹' : row.fileType }}
              </template>
            </el-table-column>
            <el-table-column label="修改时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="创建者" width="120">
              <template #default="{ row }">
                {{ row.creatorName || row.uploaderName }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-dropdown @command="(command) => handleItemAction(command, row)">
                  <el-button size="small" text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="row.type === 'file'" command="preview">预览</el-dropdown-item>
                      <el-dropdown-item command="download">下载</el-dropdown-item>
                      <el-dropdown-item command="share">分享</el-dropdown-item>
                      <el-dropdown-item command="delete" style="color: #f56c6c;">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 空状态 -->
        <div v-if="fileList.length === 0" class="empty-state">
          <el-empty description="暂无文件">
            <el-button type="primary" @click="handleUpload">上传文件</el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 文件上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="文件上传" width="600px">
      <div class="upload-area">
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadData"
          :multiple="true"
          :drag="true"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持多文件上传，单个文件不超过100MB
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startUpload">开始上传</el-button>
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog v-model="previewDialogVisible" :title="previewFile?.name" width="80%" top="5vh">
      <div class="file-preview">
        <div v-if="previewFile?.canPreview" class="preview-content">
          <!-- 图片预览 -->
          <img 
            v-if="isImageFile(previewFile.fileType)" 
            :src="previewFile.previewUrl" 
            alt="preview"
            style="max-width: 100%; max-height: 500px;"
          />
          <!-- 视频预览 -->
          <video 
            v-else-if="isVideoFile(previewFile.fileType)"
            :src="previewFile.previewUrl"
            controls
            style="max-width: 100%; max-height: 500px;"
          />
          <!-- 文档预览 -->
          <iframe
            v-else-if="isDocumentFile(previewFile.fileType)"
            :src="previewFile.previewUrl"
            style="width: 100%; height: 500px; border: none;"
          />
          <!-- 其他文件类型 -->
          <div v-else class="unsupported-preview">
            <el-icon size="64"><Document /></el-icon>
            <p>此文件类型不支持预览</p>
            <el-button type="primary" @click="downloadFile(previewFile.id)">下载文件</el-button>
          </div>
        </div>
        <div v-else class="no-preview">
          <el-icon size="64"><Document /></el-icon>
          <p>无法预览此文件</p>
          <el-button type="primary" @click="downloadFile(previewFile.id)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 文件分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="文件分享" width="500px">
      <el-form :model="shareForm" label-width="80px">
        <el-form-item label="分享类型">
          <el-radio-group v-model="shareForm.shareType">
            <el-radio label="public">公开链接</el-radio>
            <el-radio label="password">密码保护</el-radio>
            <el-radio label="internal">内部分享</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="shareForm.shareType === 'password'" label="访问密码">
          <el-input v-model="shareForm.password" placeholder="请输入访问密码" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-select v-model="shareForm.expireDays" placeholder="请选择有效期">
            <el-option label="1天" :value="1" />
            <el-option label="7天" :value="7" />
            <el-option label="30天" :value="30" />
            <el-option label="永久" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限设置">
          <el-checkbox-group v-model="shareForm.permissions">
            <el-checkbox label="view">查看</el-checkbox>
            <el-checkbox label="download">下载</el-checkbox>
            <el-checkbox label="upload">上传</el-checkbox>
            <el-checkbox label="comment">评论</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shareDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createShare">创建分享</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Grid,
  List,
  ArrowDown,
  Upload,
  FolderAdd,
  Refresh,
  Select,
  Close,
  Folder,
  FolderOpened,
  Document,
  Picture,
  VideoCamera,
  Headphone,
  Files,
  MoreFilled,
  UploadFilled,
  Search
} from '@element-plus/icons-vue'

// 响应式数据
const viewMode = ref('grid')
const searchKeyword = ref('')
const currentView = ref('all')
const currentFolderId = ref(null)
const selectedItems = ref([])
const fileList = ref([])
const folderTreeData = ref([])
const breadcrumb = ref([])
const tags = ref([])

const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const shareDialogVisible = ref(false)

const previewFile = ref(null)
const uploadRef = ref(null)

// 表单数据
const shareForm = reactive({
  shareType: 'public',
  password: '',
  expireDays: 7,
  permissions: ['view', 'download']
})

// 快速访问菜单
const quickAccess = ref([
  { key: 'all', label: '所有文件', icon: Files, count: 0 },
  { key: 'recent', label: '最近访问', icon: Document, count: 0 },
  { key: 'favorites', label: '我的收藏', icon: 'Star', count: 0 },
  { key: 'shared', label: '共享文件', icon: 'Share', count: 0 },
  { key: 'recycle', label: '回收站', icon: 'Delete', count: 0 }
])

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'folderName'
}

// 上传配置
const uploadUrl = computed(() => '/api/files/upload')
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
}))
const uploadData = computed(() => ({
  folderId: currentFolderId.value
}))

// 生命周期
onMounted(() => {
  loadFileList()
  loadFolderTree()
  loadTags()
})

// 方法
const loadFileList = async (folderId = null) => {
  try {
    // 调用API加载文件列表
    console.log('Loading file list for folder:', folderId)
    // 模拟数据
    fileList.value = [
      {
        id: 1,
        name: '项目文档.docx',
        type: 'file',
        fileType: 'document',
        fileSizeFormatted: '2.5MB',
        updatedAt: new Date(),
        uploaderName: '张三',
        canPreview: true,
        isFavorite: false,
        tags: ['重要', '项目']
      },
      {
        id: 2,
        name: '设计图',
        type: 'folder',
        folderType: 'normal',
        updatedAt: new Date(),
        creatorName: '李四',
        fileCount: 15
      }
    ]
  } catch (error) {
    ElMessage.error('加载文件列表失败')
  }
}

const loadFolderTree = async () => {
  try {
    // 调用API加载文件夹树
    console.log('Loading folder tree')
    folderTreeData.value = [
      {
        id: 1,
        folderName: '根目录',
        folderType: 'normal',
        children: [
          {
            id: 2,
            folderName: '项目文件',
            folderType: 'project',
            fileCount: 25
          },
          {
            id: 3,
            folderName: '部门文件',
            folderType: 'department',
            fileCount: 12
          }
        ]
      }
    ]
  } catch (error) {
    ElMessage.error('加载文件夹树失败')
  }
}

const loadTags = async () => {
  try {
    // 调用API加载标签
    tags.value = [
      { id: 1, tagName: '重要', tagColor: '#f56c6c' },
      { id: 2, tagName: '项目', tagColor: '#409eff' },
      { id: 3, tagName: '草稿', tagColor: '#909399' }
    ]
  } catch (error) {
    ElMessage.error('加载标签失败')
  }
}

const handleToolbarCommand = (command) => {
  switch (command) {
    case 'upload':
      handleUpload()
      break
    case 'createFolder':
      createFolder()
      break
    case 'refresh':
      loadFileList(currentFolderId.value)
      break
    case 'selectAll':
      selectAll()
      break
    case 'clearSelection':
      selectedItems.value = []
      break
  }
}

const handleUpload = () => {
  uploadDialogVisible.value = true
}

const createFolder = async () => {
  try {
    const { value: folderName } = await ElMessageBox.prompt('请输入文件夹名称', '新建文件夹', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    if (folderName) {
      // 调用API创建文件夹
      console.log('Creating folder:', folderName)
      ElMessage.success('文件夹创建成功')
      loadFileList(currentFolderId.value)
    }
  } catch (error) {
    console.log('取消创建文件夹')
  }
}

const switchView = (viewKey) => {
  currentView.value = viewKey
  // 根据视图类型加载不同数据
  switch (viewKey) {
    case 'recent':
      loadRecentFiles()
      break
    case 'favorites':
      loadFavorites()
      break
    case 'shared':
      loadSharedFiles()
      break
    case 'recycle':
      loadRecycleBin()
      break
    default:
      loadFileList()
  }
}

const handleFolderClick = (data) => {
  currentFolderId.value = data.id
  loadFileList(data.id)
  updateBreadcrumb(data)
}

const handleItemClick = (item) => {
  if (selectedItems.value.includes(item.id)) {
    selectedItems.value = selectedItems.value.filter(id => id !== item.id)
  } else {
    selectedItems.value.push(item.id)
  }
}

const handleItemDoubleClick = (item) => {
  if (item.type === 'folder') {
    handleFolderClick(item)
  } else {
    previewFile.value = item
    previewDialogVisible.value = true
  }
}

const handleItemAction = async (command, item) => {
  switch (command) {
    case 'preview':
      previewFile.value = item
      previewDialogVisible.value = true
      break
    case 'download':
      downloadFile(item.id)
      break
    case 'share':
      shareDialogVisible.value = true
      break
    case 'delete':
      await deleteItem(item)
      break
    case 'rename':
      await renameItem(item)
      break
    case 'favorite':
      await toggleFavorite(item)
      break
  }
}

const downloadFile = (fileId) => {
  // 调用下载API
  console.log('Downloading file:', fileId)
  ElMessage.success('开始下载文件')
}

const deleteItem = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${item.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用删除API
    console.log('Deleting item:', item.id)
    ElMessage.success('删除成功')
    loadFileList(currentFolderId.value)
  } catch (error) {
    console.log('取消删除')
  }
}

const startUpload = () => {
  uploadRef.value.submit()
}

const handleFileChange = (file, fileList) => {
  console.log('File changed:', file, fileList)
}

const handleUploadSuccess = (response, file) => {
  ElMessage.success(`${file.name} 上传成功`)
  uploadDialogVisible.value = false
  loadFileList(currentFolderId.value)
}

const handleUploadError = (error, file) => {
  ElMessage.error(`${file.name} 上传失败`)
}

const beforeUpload = (file) => {
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB!')
  }
  return isLt100M
}

const createShare = () => {
  // 调用分享API
  console.log('Creating share:', shareForm)
  ElMessage.success('分享链接创建成功')
  shareDialogVisible.value = false
}

// 辅助方法
const getFolderIcon = (folderType) => {
  const icons = {
    normal: Folder,
    project: FolderOpened,
    department: Folder,
    shared: Folder
  }
  return icons[folderType] || Folder
}

const getFileIcon = (fileType) => {
  const icons = {
    document: Document,
    image: Picture,
    video: VideoCamera,
    audio: Headphone
  }
  return icons[fileType] || Document
}

const isImageFile = (fileType) => {
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(fileType?.toLowerCase())
}

const isVideoFile = (fileType) => {
  return ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm'].includes(fileType?.toLowerCase())
}

const isDocumentFile = (fileType) => {
  return ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(fileType?.toLowerCase())
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const updateBreadcrumb = (folder) => {
  // 更新面包屑导航
  breadcrumb.value = [
    { id: null, name: '根目录' },
    { id: folder.id, name: folder.folderName }
  ]
}

// 其他方法的实现...
const selectAll = () => {
  selectedItems.value = fileList.value.map(item => item.id)
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection.map(item => item.id)
}

const loadRecentFiles = () => {
  console.log('Loading recent files')
}

const loadFavorites = () => {
  console.log('Loading favorites')
}

const loadSharedFiles = () => {
  console.log('Loading shared files')
}

const loadRecycleBin = () => {
  console.log('Loading recycle bin')
}

const searchFiles = () => {
  console.log('Searching files:', searchKeyword.value)
}

const filterByTag = (tagId) => {
  console.log('Filtering by tag:', tagId)
}

const downloadSelected = () => {
  console.log('Downloading selected items:', selectedItems.value)
}

const moveSelected = () => {
  console.log('Moving selected items:', selectedItems.value)
}

const copySelected = () => {
  console.log('Copying selected items:', selectedItems.value)
}

const shareSelected = () => {
  console.log('Sharing selected items:', selectedItems.value)
}

const deleteSelected = () => {
  console.log('Deleting selected items:', selectedItems.value)
}

const renameItem = async (item) => {
  try {
    const { value: newName } = await ElMessageBox.prompt('请输入新名称', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: item.name
    })
    
    if (newName) {
      console.log('Renaming item:', item.id, 'to:', newName)
      ElMessage.success('重命名成功')
      loadFileList(currentFolderId.value)
    }
  } catch (error) {
    console.log('取消重命名')
  }
}

const toggleFavorite = (item) => {
  console.log('Toggling favorite:', item.id)
  item.isFavorite = !item.isFavorite
  ElMessage.success(item.isFavorite ? '已添加到收藏' : '已取消收藏')
}

const handleRowClick = (row) => {
  handleItemClick(row)
}

const handleRowDoubleClick = (row) => {
  handleItemDoubleClick(row)
}

const handleRowContextMenu = (row, column, event) => {
  handleItemContextMenu(row, event)
}

const handleItemContextMenu = (item, event) => {
  // 右键菜单处理
  console.log('Context menu for item:', item)
}

const handleFolderContextMenu = (event, data) => {
  // 文件夹右键菜单
  console.log('Folder context menu:', data)
}
</script>

<style scoped>
.file-manager {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.toolbar-left {
  flex: 1;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 250px;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.sidebar-section {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.quick-access {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.quick-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-item:hover {
  background-color: #f5f7fa;
}

.quick-item.active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.quick-item .el-icon {
  margin-right: 8px;
}

.count {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}

.folder-tree {
  max-height: 300px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  flex: 1;
}

.tree-node .el-icon {
  margin-right: 6px;
}

.node-label {
  flex: 1;
}

.node-count {
  font-size: 12px;
  color: #999;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  cursor: pointer;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.selection-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background-color: #e6f7ff;
  border-bottom: 1px solid #91d5ff;
}

.selection-info {
  font-size: 14px;
  color: #1890ff;
}

.selection-actions {
  display: flex;
  gap: 8px;
}

.grid-view {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.file-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.file-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.file-item.selected {
  border-color: #409eff;
  background-color: #e6f7ff;
}

.file-icon {
  margin-bottom: 8px;
}

.file-thumbnail {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 4px;
}

.file-thumbnail img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.file-name {
  font-size: 14px;
  text-align: center;
  margin-bottom: 4px;
  word-break: break-all;
  line-height: 1.4;
  max-height: 2.8em;
  overflow: hidden;
}

.file-info {
  font-size: 12px;
  color: #666;
  text-align: center;
}

.file-size {
  margin-right: 8px;
}

.file-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.file-item:hover .file-actions {
  opacity: 1;
}

.list-view {
  flex: 1;
  padding: 20px;
  background-color: #fff;
}

.file-name-cell {
  display: flex;
  align-items: center;
}

.file-name-cell .file-icon {
  margin-right: 8px;
}

.file-name-cell .name {
  flex: 1;
  margin-right: 8px;
}

.file-name-cell .tags {
  display: flex;
  gap: 4px;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}

.upload-area {
  padding: 20px;
}

.file-preview {
  text-align: center;
}

.preview-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.unsupported-preview,
.no-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #666;
}
</style>