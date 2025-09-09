<template>
  <el-dialog
    v-model="visible"
    title="收藏夹管理"
    width="600px"
    :before-close="handleClose"
    class="favorite-manager-dialog"
  >
    <div class="favorite-manager">
      <!-- 添加收藏表单 -->
      <div class="add-favorite-section">
        <h4>添加新收藏</h4>
        <el-form :model="newFavorite" :rules="rules" ref="formRef" label-width="80px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="页面路径" prop="path">
                <el-select
                  v-model="newFavorite.path"
                  placeholder="选择页面路径"
                  filterable
                  @change="onPathChange"
                  style="width: 100%"
                >
                  <el-option
                    v-for="(info, path) in availableRoutes"
                    :key="path"
                    :label="`${info.title} (${path})`"
                    :value="path"
                  >
                    <div class="route-option">
                      <el-icon class="route-icon">
                        <Clock v-if="info.icon === 'Clock'" />
                        <Calendar v-else-if="info.icon === 'Calendar'" />
                        <Bell v-else-if="info.icon === 'Bell'" />
                        <User v-else-if="info.icon === 'User'" />
                        <Edit v-else-if="info.icon === 'Edit'" />
                        <List v-else-if="info.icon === 'List'" />
                        <Odometer v-else-if="info.icon === 'Odometer'" />
                        <Document v-else />
                      </el-icon>
                      <span class="route-title">{{ info.title }}</span>
                      <span class="route-path">{{ path }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="自定义名称" prop="title">
                <el-input
                  v-model="newFavorite.title"
                  placeholder="可选，留空使用默认名称"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" @click="addFavorite" :loading="adding">
              <el-icon><Plus /></el-icon>
              添加收藏
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-divider />

      <!-- 收藏列表 -->
      <div class="favorites-list-section">
        <div class="section-header">
          <h4>我的收藏 ({{ favoriteRoutes.length }})</h4>
          <div class="header-actions">
            <el-button text @click="exportFavorites">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button text @click="importFavorites">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button text type="danger" @click="clearAllFavorites">
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
          </div>
        </div>

        <div v-if="Object.keys(favoritesByCategory).length > 0" class="favorites-content">
          <div
            v-for="(routes, category) in favoritesByCategory"
            :key="category"
            class="category-section"
          >
            <div class="category-header">
              <el-icon><Folder /></el-icon>
              <span class="category-name">{{ category }}</span>
              <el-badge :value="routes.length" class="category-count" />
            </div>

            <div class="category-items">
              <div
                v-for="(route, index) in routes"
                :key="route.path"
                class="favorite-item"
              >
                <div class="item-content">
                  <el-icon class="item-icon">
                    <Clock v-if="route.icon === 'Clock'" />
                    <Calendar v-else-if="route.icon === 'Calendar'" />
                    <Bell v-else-if="route.icon === 'Bell'" />
                    <User v-else-if="route.icon === 'User'" />
                    <Edit v-else-if="route.icon === 'Edit'" />
                    <List v-else-if="route.icon === 'List'" />
                    <Odometer v-else-if="route.icon === 'Odometer'" />
                    <Document v-else />
                  </el-icon>
                  <div class="item-info">
                    <div class="item-title">{{ route.title }}</div>
                    <div class="item-path">{{ route.path }}</div>
                    <div class="item-meta">
                      添加时间: {{ formatTime(route.timestamp) }}
                    </div>
                  </div>
                </div>

                <div class="item-actions">
                  <el-button text size="small" @click="navigateToRoute(route.path)">
                    <el-icon><Right /></el-icon>
                    访问
                  </el-button>
                  <el-button text size="small" @click="editFavorite(route)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button text size="small" type="danger" @click="removeFavorite(route.path)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-favorites">
          <el-icon class="empty-icon"><Star /></el-icon>
          <p>还没有收藏任何页面</p>
          <p class="empty-tip">在页面右上角点击星标按钮即可收藏</p>
        </div>
      </div>
    </div>

    <!-- 编辑收藏对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑收藏"
      width="400px"
      append-to-body
    >
      <el-form :model="editingFavorite" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editingFavorite.title" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="editingFavorite.path" readonly />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入文件输入 -->
    <input
      ref="fileInput"
      type="file"
      accept=".json"
      style="display: none"
      @change="handleFileImport"
    />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useBreadcrumbStore } from '@/stores/breadcrumb'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Download,
  Upload,
  Delete,
  Folder,
  Right,
  Edit,
  Star,
  Clock,
  Calendar,
  Bell,
  User,
  List,
  Odometer,
  Document
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()

// 响应式数据
const visible = ref(false)
const adding = ref(false)
const editDialogVisible = ref(false)
const fileInput = ref(null)
const formRef = ref(null)

const newFavorite = ref({
  path: '',
  title: ''
})

const editingFavorite = ref({
  path: '',
  title: ''
})

// 表单验证规则
const rules = {
  path: [
    { required: true, message: '请选择页面路径', trigger: 'change' }
  ]
}

// 计算属性
const favoriteRoutes = computed(() => breadcrumbStore.favoriteRoutes)
const favoritesByCategory = computed(() => breadcrumbStore.favoritesByCategory)
const availableRoutes = computed(() => breadcrumbStore.routeMap)

// 监听props变化
watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 方法
const handleClose = () => {
  visible.value = false
}

const onPathChange = (path) => {
  const routeInfo = breadcrumbStore.getRouteInfo(path)
  if (routeInfo && !newFavorite.value.title) {
    newFavorite.value.title = routeInfo.title
  }
}

const addFavorite = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    adding.value = true
    
    const route = {
      path: newFavorite.value.path,
      params: {},
      query: {}
    }
    
    const success = breadcrumbStore.addToFavorites(route, newFavorite.value.title || null)
    
    if (success) {
      ElMessage.success('添加收藏成功')
      newFavorite.value = { path: '', title: '' }
      formRef.value.resetFields()
    } else {
      ElMessage.warning('该页面已在收藏夹中')
    }
  } catch (error) {
    console.error('添加收藏失败:', error)
  } finally {
    adding.value = false
  }
}

const navigateToRoute = (path) => {
  router.push(path)
  visible.value = false
}

const editFavorite = (route) => {
  editingFavorite.value = { ...route }
  editDialogVisible.value = true
}

const saveEdit = () => {
  // 这里需要在store中添加编辑功能
  // 暂时通过删除再添加的方式实现
  breadcrumbStore.removeFromFavorites(editingFavorite.value.path)
  
  const route = {
    path: editingFavorite.value.path,
    params: {},
    query: {}
  }
  
  breadcrumbStore.addToFavorites(route, editingFavorite.value.title)
  editDialogVisible.value = false
  ElMessage.success('编辑成功')
}

const removeFavorite = async (path) => {
  try {
    await ElMessageBox.confirm('确定要删除这个收藏吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const success = breadcrumbStore.removeFromFavorites(path)
    if (success) {
      ElMessage.success('删除成功')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const clearAllFavorites = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有收藏吗？此操作不可恢复。', '确认清空', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    breadcrumbStore.clearFavorites()
    ElMessage.success('已清空所有收藏')
  } catch (error) {
    // 用户取消清空
  }
}

const exportFavorites = () => {
  const data = {
    favorites: favoriteRoutes.value,
    exportTime: new Date().toISOString(),
    version: '1.0'
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `favorites-${new Date().toISOString().split('T')[0]}.json`
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('导出成功')
}

const importFavorites = () => {
  fileInput.value?.click()
}

const handleFileImport = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = JSON.parse(e.target.result)
      if (data.favorites && Array.isArray(data.favorites)) {
        // 导入收藏
        data.favorites.forEach(favorite => {
          const route = {
            path: favorite.path,
            params: favorite.params || {},
            query: favorite.query || {}
          }
          breadcrumbStore.addToFavorites(route, favorite.title)
        })
        ElMessage.success(`成功导入 ${data.favorites.length} 个收藏`)
      } else {
        ElMessage.error('文件格式不正确')
      }
    } catch (error) {
      ElMessage.error('文件解析失败')
    }
  }
  reader.readAsText(file)
  
  // 清空文件输入
  event.target.value = ''
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString()
}
</script>

<style scoped>
.favorite-manager {
  max-height: 600px;
  overflow-y: auto;
}

.add-favorite-section {
  margin-bottom: 24px;
}

.add-favorite-section h4 {
  margin: 0 0 16px 0;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.route-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.route-icon {
  color: var(--el-color-primary);
}

.route-title {
  font-weight: 500;
}

.route-path {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-left: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h4 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.category-section {
  margin-bottom: 24px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 6px;
  margin-bottom: 12px;
}

.category-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.category-count {
  margin-left: auto;
}

.favorite-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  margin-bottom: 8px;
  transition: all 0.2s;
}

.favorite-item:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.item-icon {
  font-size: 18px;
  color: var(--el-color-primary);
}

.item-info {
  flex: 1;
}

.item-title {
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.item-path {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 2px;
}

.item-meta {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.item-actions {
  display: flex;
  gap: 4px;
}

.empty-favorites {
  text-align: center;
  padding: 48px 24px;
  color: var(--el-text-color-secondary);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-tip {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-top: 8px;
}
</style>
