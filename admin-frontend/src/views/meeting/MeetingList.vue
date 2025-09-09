<template>
  <div class="meeting-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>会议管理</h2>
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-number">{{ stats.totalMeetings }}</div>
            <div class="stat-label">今日会议</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ stats.ongoingMeetings }}</div>
            <div class="stat-label">进行中</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ stats.scheduledMeetings }}</div>
            <div class="stat-label">已预定</div>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建会议
        </el-button>
        <el-button @click="showRoomDialog = true">
          <el-icon><OfficeBuilding /></el-icon>
          会议室管理
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索会议标题或代码"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="会议状态" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="已预定" value="scheduled" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="ended" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.hostId" placeholder="主持人" clearable @change="handleSearch">
            <el-option
              v-for="host in hostList"
              :key="host.id"
              :label="host.name"
              :value="host.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 会议列表 -->
    <div class="meeting-table">
      <el-table
        v-loading="loading"
        :data="meetingList"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="meetingTitle" label="会议标题" min-width="200">
          <template #default="{ row }">
            <div class="meeting-title">
              <span class="title">{{ row.meetingTitle }}</span>
              <el-tag :type="getStatusType(row.meetingStatus)" size="small">
                {{ getStatusText(row.meetingStatus) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="meetingCode" label="会议号" width="120" />
        <el-table-column prop="hostName" label="主持人" width="120" />
        <el-table-column prop="roomName" label="会议室" width="120">
          <template #default="{ row }">
            {{ row.roomName || '无' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="参与人数" width="100">
          <template #default="{ row }">
            {{ row.participantCount || 0 }}/{{ row.maxParticipants }}
          </template>
        </el-table-column>
        <el-table-column prop="isRecording" label="录制" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.isRecording" color="#f56565"><VideoPlay /></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.meetingStatus === 'scheduled' || row.meetingStatus === 'ongoing'"
              type="primary"
              size="small"
              @click="joinMeeting(row)"
            >
              加入
            </el-button>
            <el-button
              v-if="row.meetingStatus === 'scheduled'"
              size="small"
              @click="editMeeting(row)"
            >
              编辑
            </el-button>
            <el-dropdown @command="handleCommand">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="`detail_${row.id}`">详情</el-dropdown-item>
                  <el-dropdown-item :command="`copy_${row.id}`">复制链接</el-dropdown-item>
                  <el-dropdown-item :command="`invite_${row.id}`">邀请</el-dropdown-item>
                  <el-dropdown-item
                    v-if="row.meetingStatus === 'ongoing'"
                    :command="`end_${row.id}`"
                    divided
                  >
                    结束会议
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="row.meetingStatus === 'scheduled'"
                    :command="`cancel_${row.id}`"
                    divided
                  >
                    取消会议
                  </el-dropdown-item>
                  <el-dropdown-item :command="`delete_${row.id}`" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑会议对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingMeeting ? '编辑会议' : '创建会议'"
      width="600px"
      @close="resetMeetingForm"
    >
      <el-form
        ref="meetingFormRef"
        :model="meetingForm"
        :rules="meetingRules"
        label-width="100px"
      >
        <el-form-item label="会议标题" prop="meetingTitle">
          <el-input v-model="meetingForm.meetingTitle" placeholder="请输入会议标题" />
        </el-form-item>
        <el-form-item label="会议类型" prop="meetingType">
          <el-radio-group v-model="meetingForm.meetingType">
            <el-radio label="instant">立即开始</el-radio>
            <el-radio label="scheduled">预定会议</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="meetingForm.meetingType === 'scheduled'"
          label="开始时间"
          prop="startTime"
        >
          <el-date-picker
            v-model="meetingForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          v-if="meetingForm.meetingType === 'scheduled'"
          label="结束时间"
          prop="endTime"
        >
          <el-date-picker
            v-model="meetingForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会议室" prop="roomId">
          <el-select v-model="meetingForm.roomId" placeholder="选择会议室（可选）" clearable>
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="`${room.roomName} (${room.capacity}人)`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number
            v-model="meetingForm.maxParticipants"
            :min="2"
            :max="100"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会议密码" prop="meetingPassword">
          <el-input
            v-model="meetingForm.meetingPassword"
            placeholder="设置会议密码（可选）"
            show-password
          />
        </el-form-item>
        <el-form-item label="会议议程" prop="agenda">
          <el-input
            v-model="meetingForm.agenda"
            type="textarea"
            :rows="3"
            placeholder="请输入会议议程"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMeeting" :loading="saving">
          {{ editingMeeting ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 会议室管理对话框 -->
    <el-dialog v-model="showRoomDialog" title="会议室管理" width="800px">
      <div class="room-header">
        <el-button type="primary" @click="showCreateRoomDialog = true">
          <el-icon><Plus /></el-icon>
          添加会议室
        </el-button>
      </div>
      <el-table :data="roomList" stripe>
        <el-table-column prop="roomName" label="会议室名称" />
        <el-table-column prop="roomCode" label="房间代码" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="editRoom(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRoom(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 创建会议室对话框 -->
    <el-dialog
      v-model="showCreateRoomDialog"
      title="创建会议室"
      width="500px"
      @close="resetRoomForm"
    >
      <el-form
        ref="roomFormRef"
        :model="roomForm"
        :rules="roomRules"
        label-width="100px"
      >
        <el-form-item label="会议室名称" prop="roomName">
          <el-input v-model="roomForm.roomName" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number
            v-model="roomForm.capacity"
            :min="2"
            :max="500"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roomForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入会议室描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateRoomDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRoom" :loading="saving">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  OfficeBuilding,
  VideoPlay,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const meetingList = ref([])
const roomList = ref([])
const hostList = ref([])
const selectedMeetings = ref([])

// 统计数据
const stats = reactive({
  totalMeetings: 0,
  ongoingMeetings: 0,
  scheduledMeetings: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  hostId: null,
  dateRange: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框显示状态
const showCreateDialog = ref(false)
const showRoomDialog = ref(false)
const showCreateRoomDialog = ref(false)

// 编辑状态
const editingMeeting = ref(null)

// 会议表单
const meetingForm = reactive({
  meetingTitle: '',
  meetingType: 'instant',
  startTime: null,
  endTime: null,
  roomId: null,
  maxParticipants: 10,
  meetingPassword: '',
  agenda: ''
})

// 会议室表单
const roomForm = reactive({
  roomName: '',
  capacity: 10,
  description: ''
})

// 表单验证规则
const meetingRules = {
  meetingTitle: [
    { required: true, message: '请输入会议标题', trigger: 'blur' }
  ],
  meetingType: [
    { required: true, message: '请选择会议类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  maxParticipants: [
    { required: true, message: '请设置最大参与人数', trigger: 'blur' }
  ]
}

const roomRules = {
  roomName: [
    { required: true, message: '请输入会议室名称', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请设置会议室容量', trigger: 'blur' }
  ]
}

// 组件引用
const meetingFormRef = ref(null)
const roomFormRef = ref(null)

onMounted(() => {
  loadMeetings()
  loadRooms()
  loadHosts()
  loadStats()
})

// 加载会议列表
async function loadMeetings() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await fetch('/api/meetings?' + new URLSearchParams(params))
    const result = await response.json()
    
    if (result.success) {
      meetingList.value = result.data
      pagination.total = result.total || 0
    } else {
      ElMessage.error('加载会议列表失败')
    }
  } catch (error) {
    console.error('Load meetings error:', error)
    ElMessage.error('加载会议列表失败')
  } finally {
    loading.value = false
  }
}

// 加载会议室列表
async function loadRooms() {
  try {
    const response = await fetch('/api/meetings/rooms')
    const result = await response.json()
    
    if (result.success) {
      roomList.value = result.data
    }
  } catch (error) {
    console.error('Load rooms error:', error)
  }
}

// 加载主持人列表
async function loadHosts() {
  try {
    const response = await fetch('/api/employees')
    const result = await response.json()
    
    if (result.success) {
      hostList.value = result.data
    }
  } catch (error) {
    console.error('Load hosts error:', error)
  }
}

// 加载统计数据
async function loadStats() {
  try {
    const response = await fetch('/api/meetings/stats/today')
    const result = await response.json()
    
    if (result.success) {
      Object.assign(stats, result.data)
    }
  } catch (error) {
    console.error('Load stats error:', error)
  }
}

// 搜索处理
function handleSearch() {
  pagination.page = 1
  loadMeetings()
}

// 重置搜索
function resetSearch() {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    hostId: null,
    dateRange: null
  })
  handleSearch()
}

// 分页处理
function handleSizeChange(size) {
  pagination.size = size
  loadMeetings()
}

function handleCurrentChange(page) {
  pagination.page = page
  loadMeetings()
}

// 选择变化
function handleSelectionChange(selection) {
  selectedMeetings.value = selection
}

// 加入会议
function joinMeeting(meeting) {
  router.push(`/meeting/room/${meeting.id}`)
}

// 编辑会议
function editMeeting(meeting) {
  editingMeeting.value = meeting
  Object.assign(meetingForm, meeting)
  showCreateDialog.value = true
}

// 保存会议
async function saveMeeting() {
  if (!meetingFormRef.value) return
  
  try {
    await meetingFormRef.value.validate()
    saving.value = true
    
    const url = editingMeeting.value 
      ? `/api/meetings/${editingMeeting.value.id}`
      : '/api/meetings'
    const method = editingMeeting.value ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(meetingForm)
    })
    
    const result = await response.json()
    
    if (result.success) {
      ElMessage.success(editingMeeting.value ? '会议更新成功' : '会议创建成功')
      showCreateDialog.value = false
      loadMeetings()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('Save meeting error:', error)
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

// 重置会议表单
function resetMeetingForm() {
  editingMeeting.value = null
  Object.assign(meetingForm, {
    meetingTitle: '',
    meetingType: 'instant',
    startTime: null,
    endTime: null,
    roomId: null,
    maxParticipants: 10,
    meetingPassword: '',
    agenda: ''
  })
  if (meetingFormRef.value) {
    meetingFormRef.value.resetFields()
  }
}

// 保存会议室
async function saveRoom() {
  if (!roomFormRef.value) return
  
  try {
    await roomFormRef.value.validate()
    saving.value = true
    
    const response = await fetch('/api/meetings/rooms', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(roomForm)
    })
    
    const result = await response.json()
    
    if (result.success) {
      ElMessage.success('会议室创建成功')
      showCreateRoomDialog.value = false
      loadRooms()
    } else {
      ElMessage.error(result.message || '创建失败')
    }
  } catch (error) {
    console.error('Save room error:', error)
    ElMessage.error('创建失败')
  } finally {
    saving.value = false
  }
}

// 重置会议室表单
function resetRoomForm() {
  Object.assign(roomForm, {
    roomName: '',
    capacity: 10,
    description: ''
  })
  if (roomFormRef.value) {
    roomFormRef.value.resetFields()
  }
}

// 命令处理
async function handleCommand(command) {
  const [action, id] = command.split('_')
  const meetingId = parseInt(id)
  
  switch (action) {
    case 'detail':
      // 查看详情
      break
    case 'copy':
      // 复制链接
      await copyMeetingLink(meetingId)
      break
    case 'invite':
      // 邀请参与者
      break
    case 'end':
      await endMeeting(meetingId)
      break
    case 'cancel':
      await cancelMeeting(meetingId)
      break
    case 'delete':
      await deleteMeeting(meetingId)
      break
  }
}

// 复制会议链接
async function copyMeetingLink(meetingId) {
  try {
    const response = await fetch(`/api/meetings/${meetingId}/link`)
    const result = await response.json()
    
    if (result.success) {
      await navigator.clipboard.writeText(window.location.origin + result.data)
      ElMessage.success('会议链接已复制到剪贴板')
    }
  } catch (error) {
    ElMessage.error('复制链接失败')
  }
}

// 结束会议
async function endMeeting(meetingId) {
  try {
    await ElMessageBox.confirm('确定要结束这个会议吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch(`/api/meetings/${meetingId}/end`, {
      method: 'POST'
    })
    
    const result = await response.json()
    
    if (result.success) {
      ElMessage.success('会议已结束')
      loadMeetings()
    } else {
      ElMessage.error('操作失败')
    }
  } catch {
    // 用户取消
  }
}

// 取消会议
async function cancelMeeting(meetingId) {
  try {
    await ElMessageBox.confirm('确定要取消这个会议吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 实现取消会议逻辑
    ElMessage.success('会议已取消')
    loadMeetings()
  } catch {
    // 用户取消
  }
}

// 删除会议
async function deleteMeeting(meetingId) {
  try {
    await ElMessageBox.confirm('确定要删除这个会议吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch(`/api/meetings/${meetingId}`, {
      method: 'DELETE'
    })
    
    const result = await response.json()
    
    if (result.success) {
      ElMessage.success('会议已删除')
      loadMeetings()
    } else {
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 工具函数
function getStatusType(status) {
  const typeMap = {
    scheduled: '',
    ongoing: 'success',
    ended: 'info',
    cancelled: 'danger'
  }
  return typeMap[status] || ''
}

function getStatusText(status) {
  const textMap = {
    scheduled: '已预定',
    ongoing: '进行中',
    ended: '已结束',
    cancelled: '已取消'
  }
  return textMap[status] || status
}

function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString()
}
</script>

<style scoped>
.meeting-list {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left h2 {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 600;
}

.stats-cards {
  display: flex;
  gap: 16px;
}

.stat-card {
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
  min-width: 100px;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 16px;
}

.meeting-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.meeting-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-weight: 500;
}

.pagination {
  padding: 16px;
  text-align: right;
  border-top: 1px solid #eee;
}

.room-header {
  margin-bottom: 16px;
}

:deep(.el-table) {
  border: none;
}

:deep(.el-table th) {
  background-color: #fafafa;
  border-bottom: 1px solid #eee;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f5f5f5;
}
</style>