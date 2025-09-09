<template>
  <el-dialog
    v-model="dialogVisible"
    title="搜索日程"
    width="700px"
    :close-on-click-modal="false"
  >
    <!-- 搜索表单 -->
    <el-form
      ref="searchFormRef"
      :model="searchForm"
      label-width="80px"
      @submit.prevent="handleSearch"
    >
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题、描述、地点"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类">
            <el-select
              v-model="searchForm.categoryId"
              placeholder="选择分类"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              >
                <div class="category-option">
                  <div
                    class="category-color"
                    :style="{ backgroundColor: category.color }"
                  ></div>
                  <span>{{ category.name }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="选择状态"
              clearable
              style="width: 100%"
            >
              <el-option label="待开始" value="SCHEDULED" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级">
            <el-select
              v-model="searchForm.priority"
              placeholder="选择优先级"
              clearable
              style="width: 100%"
            >
              <el-option label="低" value="LOW" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="高" value="HIGH" />
              <el-option label="紧急" value="URGENT" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="24">
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item>
        <el-button type="primary" @click="handleSearch" :loading="searching">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0 || hasSearched" class="search-results">
      <div class="results-header">
        <h3>搜索结果 ({{ pagination.total }})</h3>
        <div class="results-actions">
          <el-button
            size="small"
            @click="exportResults"
            :disabled="searchResults.length === 0"
          >
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </div>

      <div v-if="searchResults.length === 0" class="empty-results">
        <el-empty description="未找到匹配的日程" />
      </div>

      <div v-else class="results-list">
        <div
          v-for="schedule in searchResults"
          :key="schedule.id"
          class="result-item"
          @click="viewScheduleDetail(schedule.id)"
        >
          <div class="schedule-color-bar" :style="{ backgroundColor: schedule.color || '#409eff' }"></div>
          <div class="schedule-content">
            <div class="schedule-header">
              <h4 class="schedule-title">{{ schedule.title }}</h4>
              <div class="schedule-badges">
                <el-tag
                  :type="getStatusType(schedule.status)"
                  size="small"
                >
                  {{ getStatusText(schedule.status) }}
                </el-tag>
                <el-tag
                  :type="getPriorityType(schedule.priority)"
                  size="small"
                >
                  {{ getPriorityText(schedule.priority) }}
                </el-tag>
              </div>
            </div>
            
            <div class="schedule-meta">
              <div class="schedule-time">
                <el-icon><Clock /></el-icon>
                <span v-if="schedule.all_day">
                  全天 - {{ formatDate(schedule.start_time) }}
                </span>
                <span v-else>
                  {{ formatDateTime(schedule.start_time) }} - {{ formatDateTime(schedule.end_time) }}
                </span>
              </div>
              
              <div v-if="schedule.location" class="schedule-location">
                <el-icon><Location /></el-icon>
                {{ schedule.location }}
              </div>
            </div>
            
            <div v-if="schedule.description" class="schedule-description">
              {{ truncateText(schedule.description, 100) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Download, 
  Clock, 
  Location 
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { useScheduleStore } from '@/stores/schedule'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'search'])

// Store
const scheduleStore = useScheduleStore()

// 响应式数据
const searchFormRef = ref(null)
const searching = ref(false)
const hasSearched = ref(false)
const searchResults = ref([])

const searchForm = ref({
  keyword: '',
  categoryId: null,
  status: '',
  priority: '',
  dateRange: []
})

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

// 分类选项
const categories = ref([
  { id: 1, name: '工作', color: '#409eff' },
  { id: 2, name: '会议', color: '#67c23a' },
  { id: 3, name: '学习', color: '#e6a23c' },
  { id: 4, name: '生活', color: '#f56c6c' },
  { id: 5, name: '其他', color: '#909399' }
])

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
const handleSearch = async () => {
  try {
    searching.value = true
    hasSearched.value = true
    
    const searchParams = {
      keyword: searchForm.value.keyword || undefined,
      categoryId: searchForm.value.categoryId || undefined,
      status: searchForm.value.status || undefined,
      priority: searchForm.value.priority || undefined,
      startDate: searchForm.value.dateRange?.[0] || undefined,
      endDate: searchForm.value.dateRange?.[1] || undefined,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize
    }
    
    const result = await scheduleStore.searchSchedules(searchParams)
    
    searchResults.value = result.records || []
    pagination.value.total = result.total || 0
    
    emit('search', searchParams)
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

const handleReset = () => {
  searchForm.value = {
    keyword: '',
    categoryId: null,
    status: '',
    priority: '',
    dateRange: []
  }
  searchResults.value = []
  hasSearched.value = false
  pagination.value = {
    current: 1,
    pageSize: 10,
    total: 0
  }
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.current = 1
  handleSearch()
}

const handleCurrentChange = (page) => {
  pagination.value.current = page
  handleSearch()
}

const viewScheduleDetail = (scheduleId) => {
  // 触发父组件显示详情
  emit('schedule-detail', scheduleId)
}

const exportResults = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能开发中...')
}

const formatDate = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD')
}

const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('MM-DD HH:mm')
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const getStatusText = (status) => {
  const statusMap = {
    'SCHEDULED': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getPriorityText = (priority) => {
  const priorityMap = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'URGENT': '紧急'
  }
  return priorityMap[priority] || priority
}

const getPriorityType = (priority) => {
  const typeMap = {
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'URGENT': 'danger'
  }
  return typeMap[priority] || 'info'
}

const handleClose = () => {
  dialogVisible.value = false
}

// 监听器
watch(() => props.modelValue, (visible) => {
  if (!visible) {
    // 对话框关闭时重置搜索状态
    setTimeout(() => {
      handleReset()
    }, 300)
  }
})
</script>

<style scoped>
.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.search-results {
  margin-top: 24px;
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.results-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.empty-results {
  text-align: center;
  padding: 40px 0;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.result-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.result-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.schedule-color-bar {
  width: 4px;
  height: 60px;
  border-radius: 2px;
  flex-shrink: 0;
}

.schedule-content {
  flex: 1;
  min-width: 0;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.schedule-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 12px;
}

.schedule-badges {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.schedule-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 8px;
}

.schedule-time,
.schedule-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.schedule-description {
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-pagination) {
  justify-content: center;
}
</style>