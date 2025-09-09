<template>
  <div class="meeting-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>在线会议</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建会议
        </el-button>
        <el-button @click="showJoinDialog = true">
          <el-icon><Connection /></el-icon>
          加入会议
        </el-button>
      </div>
    </div>

    <!-- 快速操作卡片 -->
    <div class="quick-actions">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-card class="action-card" @click="quickStart">
            <div class="card-content">
              <el-icon class="card-icon"><VideoCamera /></el-icon>
              <h3>快速开始</h3>
              <p>立即开始一个会议</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="action-card" @click="showJoinDialog = true">
            <div class="card-content">
              <el-icon class="card-icon"><Connection /></el-icon>
              <h3>加入会议</h3>
              <p>通过会议号加入</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="action-card" @click="showCreateDialog = true">
            <div class="card-content">
              <el-icon class="card-icon"><Calendar /></el-icon>
              <h3>预约会议</h3>
              <p>安排未来的会议</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 会议列表 -->
    <div class="meeting-content">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="即将开始" name="upcoming">
          <meeting-list-content 
            :meetings="upcomingMeetings" 
            :loading="loading"
            type="upcoming"
            @join="joinMeeting"
            @edit="editMeeting"
            @delete="deleteMeeting"
          />
        </el-tab-pane>
        <el-tab-pane label="我的会议" name="my">
          <meeting-list-content 
            :meetings="myMeetings" 
            :loading="loading"
            type="my"
            @join="joinMeeting"
            @edit="editMeeting"
            @delete="deleteMeeting"
          />
        </el-tab-pane>
        <el-tab-pane label="历史会议" name="history">
          <meeting-list-content 
            :meetings="historyMeetings" 
            :loading="loading"
            type="history"
            @view="viewMeeting"
          />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 创建会议对话框 -->
    <create-meeting-dialog 
      v-model="showCreateDialog"
      @created="handleMeetingCreated"
    />

    <!-- 加入会议对话框 -->
    <join-meeting-dialog 
      v-model="showJoinDialog"
      @joined="handleMeetingJoined"
    />
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Connection, VideoCamera, Calendar } from '@element-plus/icons-vue'
import meetingApi from '@/api/meeting'
import MeetingListContent from './components/MeetingListContent.vue'
import CreateMeetingDialog from './components/CreateMeetingDialog.vue'
import JoinMeetingDialog from './components/JoinMeetingDialog.vue'

export default {
  name: 'MeetingList',
  components: {
    MeetingListContent,
    CreateMeetingDialog,
    JoinMeetingDialog,
    Plus,
    Connection,
    VideoCamera,
    Calendar
  },
  setup() {
    const router = useRouter()
    const activeTab = ref('upcoming')
    const loading = ref(false)
    const showCreateDialog = ref(false)
    const showJoinDialog = ref(false)
    
    const meetings = reactive({
      upcoming: [],
      my: [],
      history: []
    })

    // 计算属性
    const upcomingMeetings = computed(() => meetings.upcoming)
    const myMeetings = computed(() => meetings.my)
    const historyMeetings = computed(() => meetings.history)

    // 加载会议数据
    const loadMeetings = async (type = 'upcoming') => {
      loading.value = true
      try {
        let response
        switch (type) {
          case 'upcoming':
            response = await meetingApi.getUpcomingMeetings()
            meetings.upcoming = response.data || []
            break
          case 'my':
            response = await meetingApi.getMyMeetings()
            meetings.my = response.data || []
            break
          case 'history':
            response = await meetingApi.getMyMeetings({ status: 'ended' })
            meetings.history = response.data || []
            break
        }
      } catch (error) {
        console.error('加载会议列表失败:', error)
        ElMessage.error('加载会议列表失败')
      } finally {
        loading.value = false
      }
    }

    // 标签页切换
    const handleTabChange = (tabName) => {
      loadMeetings(tabName)
    }

    // 快速开始会议
    const quickStart = async () => {
      try {
        const response = await meetingApi.createMeeting({
          meetingTitle: `快速会议 - ${new Date().toLocaleString()}`,
          startTime: new Date().toISOString(),
          meetingType: 'instant'
        })
        
        if (response.data) {
          ElMessage.success('会议创建成功')
          router.push(`/meeting/room/${response.data.id}`)
        }
      } catch (error) {
        console.error('创建会议失败:', error)
        ElMessage.error('创建会议失败')
      }
    }

    // 加入会议
    const joinMeeting = (meeting) => {
      router.push(`/meeting/room/${meeting.id}`)
    }

    // 编辑会议
    const editMeeting = (meeting) => {
      // TODO: 实现编辑会议功能
      ElMessage.info('编辑功能开发中')
    }

    // 删除会议
    const deleteMeeting = async (meeting) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除会议 "${meeting.title}" 吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // TODO: 调用删除API
        ElMessage.success('会议删除成功')
        loadMeetings(activeTab.value)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除会议失败:', error)
          ElMessage.error('删除会议失败')
        }
      }
    }

    // 查看会议详情
    const viewMeeting = (meeting) => {
      // TODO: 实现查看会议详情功能
      ElMessage.info('查看详情功能开发中')
    }

    // 会议创建成功回调
    const handleMeetingCreated = (meeting) => {
      ElMessage.success('会议创建成功')
      loadMeetings(activeTab.value)
      if (meeting.startNow) {
        router.push(`/meeting/room/${meeting.id}`)
      }
    }

    // 会议加入成功回调
    const handleMeetingJoined = (meetingId) => {
      router.push(`/meeting/room/${meetingId}`)
    }

    // 组件挂载时加载数据
    onMounted(() => {
      loadMeetings('upcoming')
    })

    return {
      activeTab,
      loading,
      showCreateDialog,
      showJoinDialog,
      upcomingMeetings,
      myMeetings,
      historyMeetings,
      handleTabChange,
      quickStart,
      joinMeeting,
      editMeeting,
      deleteMeeting,
      viewMeeting,
      handleMeetingCreated,
      handleMeetingJoined
    }
  }
}
</script>

<style scoped>
.meeting-list {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.quick-actions {
  margin-bottom: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 120px;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-content {
  text-align: center;
  padding: 10px;
}

.card-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 8px;
}

.card-content h3 {
  margin: 8px 0 4px 0;
  font-size: 16px;
  color: #333;
}

.card-content p {
  margin: 0;
  font-size: 12px;
  color: #666;
}

.meeting-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
}
</style>