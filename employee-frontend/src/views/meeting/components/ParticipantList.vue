<template>
  <div class="participant-list">
    <!-- 头部 -->
    <div class="list-header">
      <div class="header-left">
        <el-icon><User /></el-icon>
        <span class="title">参与者</span>
        <el-badge :value="participants.length" class="count-badge" />
      </div>
      <div class="header-right">
        <el-button 
          text 
          :icon="UserPlus" 
          @click="showInviteDialog = true"
          v-if="canInvite"
        />
        <el-button 
          text 
          :icon="Setting" 
          @click="showSettings = !showSettings"
          v-if="isHost"
        />
        <el-button 
          text 
          :icon="Close" 
          @click="$emit('close')"
        />
      </div>
    </div>
    
    <!-- 设置面板 -->
    <div class="settings-panel" v-if="showSettings && isHost">
      <div class="setting-item">
        <span>允许参与者邀请他人</span>
        <el-switch v-model="allowInvite" @change="updateMeetingSettings" />
      </div>
      <div class="setting-item">
        <span>允许参与者共享屏幕</span>
        <el-switch v-model="allowScreenShare" @change="updateMeetingSettings" />
      </div>
      <div class="setting-item">
        <span>自动静音新加入者</span>
        <el-switch v-model="autoMuteNewJoiners" @change="updateMeetingSettings" />
      </div>
      <div class="setting-item">
        <span>锁定会议</span>
        <el-switch v-model="meetingLocked" @change="updateMeetingSettings" />
      </div>
    </div>
    
    <!-- 搜索框 -->
    <div class="search-container" v-if="participants.length > 10">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索参与者"
        :prefix-icon="Search"
        size="small"
        clearable
      />
    </div>
    
    <!-- 参与者列表 -->
    <div class="participants-container">
      <!-- 主持人 -->
      <div class="participant-section" v-if="hostParticipants.length > 0">
        <div class="section-title">主持人</div>
        <div 
          v-for="participant in hostParticipants"
          :key="participant.id"
          class="participant-item host"
        >
          <div class="participant-info">
            <div class="avatar-container">
              <el-avatar 
                :size="32" 
                :src="participant.avatar"
                :alt="participant.name"
              >
                {{ participant.name?.charAt(0) }}
              </el-avatar>
              <div class="status-indicators">
                <div class="connection-status" :class="participant.connectionStatus"></div>
                <el-icon v-if="participant.isHost" class="host-icon"><Crown /></el-icon>
              </div>
            </div>
            
            <div class="participant-details">
              <div class="name-container">
                <span class="name">{{ participant.name }}</span>
                <span class="you-label" v-if="participant.id === currentUserId">(您)</span>
              </div>
              <div class="status-text">{{ getParticipantStatus(participant) }}</div>
            </div>
          </div>
          
          <div class="participant-controls">
            <!-- 音频状态 -->
            <el-tooltip :content="participant.audioEnabled ? '静音' : '取消静音'" placement="top">
              <el-button
                  text
                  :icon="Microphone"
                  :class="{ 'audio-disabled': !participant.audioEnabled }"
                  @click="toggleParticipantAudio(participant)"
                :disabled="!canControlParticipant(participant)"
                size="small"
              />
            </el-tooltip>
            
            <!-- 视频状态 -->
            <el-tooltip :content="participant.videoEnabled ? '关闭视频' : '开启视频'" placement="top">
              <el-button
                  text
                  :icon="VideoCamera"
                  :class="{ 'video-disabled': !participant.videoEnabled }"
                  @click="toggleParticipantVideo(participant)"
                :disabled="!canControlParticipant(participant)"
                size="small"
              />
            </el-tooltip>
            
            <!-- 更多操作 -->
            <el-dropdown 
              @command="(command) => handleParticipantAction(command, participant)"
              v-if="canManageParticipant(participant)"
            >
              <el-button text :icon="More" size="small" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    command="pin" 
                    :icon="Pin"
                    v-if="!participant.isPinned"
                  >
                    置顶视频
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="unpin" 
                    :icon="Pin"
                    v-if="participant.isPinned"
                  >
                    取消置顶
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="spotlight" 
                    :icon="View"
                    v-if="!participant.isSpotlight"
                  >
                    聚焦
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="unspotlight" 
                    :icon="View"
                    v-if="participant.isSpotlight"
                  >
                    取消聚焦
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="makeHost" 
                    :icon="Crown"
                    v-if="isHost && !participant.isHost"
                  >
                    设为主持人
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="private-chat" 
                    :icon="ChatDotRound"
                  >
                    私聊
                  </el-dropdown-item>
                  <el-dropdown-item 
                    divided
                    command="remove" 
                    :icon="UserDelete"
                    v-if="isHost && participant.id !== currentUserId"
                  >
                    移除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <!-- 普通参与者 -->
      <div class="participant-section" v-if="regularParticipants.length > 0">
        <div class="section-title" v-if="hostParticipants.length > 0">参与者</div>
        <div 
          v-for="participant in regularParticipants"
          :key="participant.id"
          class="participant-item"
        >
          <div class="participant-info">
            <div class="avatar-container">
              <el-avatar 
                :size="32" 
                :src="participant.avatar"
                :alt="participant.name"
              >
                {{ participant.name?.charAt(0) }}
              </el-avatar>
              <div class="status-indicators">
                <div class="connection-status" :class="participant.connectionStatus"></div>
                <el-icon v-if="participant.isScreenSharing" class="screen-share-icon"><Monitor /></el-icon>
                <el-icon v-if="participant.isRecording" class="recording-icon"><VideoCamera /></el-icon>
              </div>
            </div>
            
            <div class="participant-details">
              <div class="name-container">
                <span class="name">{{ participant.name }}</span>
                <span class="you-label" v-if="participant.id === currentUserId">(您)</span>
              </div>
              <div class="status-text">{{ getParticipantStatus(participant) }}</div>
            </div>
          </div>
          
          <div class="participant-controls">
            <!-- 音频状态 -->
            <el-tooltip :content="participant.audioEnabled ? '静音' : '取消静音'" placement="top">
              <el-button
                text
                :icon="Microphone"
                :class="{ 'audio-disabled': !participant.audioEnabled }"
                @click="toggleParticipantAudio(participant)"
                :disabled="!canControlParticipant(participant)"
                size="small"
              />
            </el-tooltip>
            
            <!-- 视频状态 -->
            <el-tooltip :content="participant.videoEnabled ? '关闭视频' : '开启视频'" placement="top">
              <el-button
                text
                :icon="VideoCamera"
                :class="{ 'video-disabled': !participant.videoEnabled }"
                @click="toggleParticipantVideo(participant)"
                :disabled="!canControlParticipant(participant)"
                size="small"
              />
            </el-tooltip>
            
            <!-- 更多操作 -->
            <el-dropdown 
              @command="(command) => handleParticipantAction(command, participant)"
              v-if="canManageParticipant(participant)"
            >
              <el-button text :icon="More" size="small" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    command="pin" 
                    :icon="Pin"
                    v-if="!participant.isPinned"
                  >
                    置顶视频
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="unpin" 
                    :icon="Pin"
                    v-if="participant.isPinned"
                  >
                    取消置顶
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="spotlight" 
                    :icon="View"
                    v-if="!participant.isSpotlight"
                  >
                    聚焦
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="unspotlight" 
                    :icon="View"
                    v-if="participant.isSpotlight"
                  >
                    取消聚焦
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="makeHost" 
                    :icon="Crown"
                    v-if="isHost && !participant.isHost"
                  >
                    设为主持人
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="private-chat" 
                    :icon="ChatDotRound"
                  >
                    私聊
                  </el-dropdown-item>
                  <el-dropdown-item 
                    divided
                    command="remove" 
                    :icon="UserDelete"
                    v-if="isHost && participant.id !== currentUserId"
                  >
                    移除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <!-- 等待加入的参与者 -->
      <div class="participant-section" v-if="waitingParticipants.length > 0">
        <div class="section-title">等待加入</div>
        <div 
          v-for="participant in waitingParticipants"
          :key="participant.id"
          class="participant-item waiting"
        >
          <div class="participant-info">
            <div class="avatar-container">
              <el-avatar 
                :size="32" 
                :src="participant.avatar"
                :alt="participant.name"
              >
                {{ participant.name?.charAt(0) }}
              </el-avatar>
              <div class="status-indicators">
                <div class="waiting-indicator"></div>
              </div>
            </div>
            
            <div class="participant-details">
              <div class="name-container">
                <span class="name">{{ participant.name }}</span>
              </div>
              <div class="status-text">等待主持人批准</div>
            </div>
          </div>
          
          <div class="participant-controls" v-if="isHost">
            <el-button 
              type="primary" 
              size="small" 
              @click="admitParticipant(participant)"
            >
              允许
            </el-button>
            <el-button 
              size="small" 
              @click="denyParticipant(participant)"
            >
              拒绝
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 邀请对话框 -->
    <el-dialog
      v-model="showInviteDialog"
      title="邀请参与者"
      width="500px"
      :before-close="handleInviteDialogClose"
    >
      <div class="invite-content">
        <el-tabs v-model="inviteTab">
          <el-tab-pane label="邀请链接" name="link">
            <div class="invite-link-section">
              <div class="link-container">
                <el-input 
                  v-model="meetingLink" 
                  readonly 
                  placeholder="会议链接"
                >
                  <template #append>
                    <el-button @click="copyMeetingLink">
                      <el-icon><CopyDocument /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>
              
              <div class="meeting-info">
                <p><strong>会议号:</strong> {{ meetingId }}</p>
                <p><strong>会议密码:</strong> {{ meetingPassword || '无' }}</p>
              </div>
              
              <div class="share-options">
                <el-button @click="shareViaEmail">
                  <el-icon><Message /></el-icon>
                  邮件分享
                </el-button>
                <el-button @click="shareViaQR">
                  <el-icon><QrCode /></el-icon>
                  二维码
                </el-button>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="邀请用户" name="users">
            <div class="invite-users-section">
              <el-input
                v-model="userSearchKeyword"
                placeholder="搜索用户"
                :prefix-icon="Search"
                @input="searchUsers"
                clearable
              />
              
              <div class="user-list" v-if="searchResults.length > 0">
                <div 
                  v-for="user in searchResults"
                  :key="user.id"
                  class="user-item"
                  @click="toggleUserSelection(user)"
                >
                  <el-checkbox 
                    :model-value="selectedUsers.includes(user.id)"
                    @change="toggleUserSelection(user)"
                  />
                  <el-avatar :size="24" :src="user.avatar">
                    {{ user.name?.charAt(0) }}
                  </el-avatar>
                  <span class="user-name">{{ user.name }}</span>
                  <span class="user-department">{{ user.department }}</span>
                </div>
              </div>
              
              <div class="selected-users" v-if="selectedUsers.length > 0">
                <div class="selected-title">已选择 {{ selectedUsers.length }} 人</div>
                <div class="selected-list">
                  <el-tag 
                    v-for="userId in selectedUsers"
                    :key="userId"
                    closable
                    @close="removeSelectedUser(userId)"
                  >
                    {{ getUserName(userId) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showInviteDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="sendInvitations"
            :disabled="inviteTab === 'users' && selectedUsers.length === 0"
            :loading="inviteLoading"
          >
            {{ inviteTab === 'link' ? '复制链接' : '发送邀请' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, UserPlus, Setting, Close, Search, Crown, Microphone,
  VideoCamera, More, Pin, View, ChatDotRound, UserDelete,
  Monitor, CopyDocument, Message, QrCode
} from '@element-plus/icons-vue'
import meetingApi from '@/api/meeting'
import { useUserStore } from '@/stores/user'

export default {
  name: 'ParticipantList',
  components: {
    User, UserPlus, Setting, Close, Search, Crown, Microphone,
    VideoCamera, More, Pin, View, ChatDotRound, UserDelete,
    Monitor, CopyDocument, Message, QrCode
  },
  props: {
    meetingId: {
      type: String,
      required: true
    },
    participants: {
      type: Array,
      default: () => []
    },
    isHost: {
      type: Boolean,
      default: false
    },
    meetingSettings: {
      type: Object,
      default: () => ({})
    }
  },
  emits: [
    'close', 'toggle-audio', 'toggle-video', 'pin-participant',
    'spotlight-participant', 'make-host', 'remove-participant',
    'admit-participant', 'deny-participant', 'private-chat'
  ],
  setup(props, { emit }) {
    const userStore = useUserStore()
    const currentUserId = computed(() => userStore.userInfo?.id)
    
    // 搜索和过滤
    const searchKeyword = ref('')
    const showSettings = ref(false)
    
    // 会议设置
    const allowInvite = ref(props.meetingSettings.allowInvite || false)
    const allowScreenShare = ref(props.meetingSettings.allowScreenShare || true)
    const autoMuteNewJoiners = ref(props.meetingSettings.autoMuteNewJoiners || false)
    const meetingLocked = ref(props.meetingSettings.meetingLocked || false)
    
    // 邀请相关
    const showInviteDialog = ref(false)
    const inviteTab = ref('link')
    const inviteLoading = ref(false)
    const meetingLink = ref('')
    const meetingPassword = ref('')
    const userSearchKeyword = ref('')
    const searchResults = ref([])
    const selectedUsers = ref([])
    
    // 计算属性
    const canInvite = computed(() => {
      return props.isHost || allowInvite.value
    })
    
    const filteredParticipants = computed(() => {
      if (!searchKeyword.value) return props.participants
      
      const keyword = searchKeyword.value.toLowerCase()
      return props.participants.filter(participant => 
        participant.name.toLowerCase().includes(keyword)
      )
    })
    
    const hostParticipants = computed(() => {
      return filteredParticipants.value.filter(p => p.isHost && p.status === 'joined')
    })
    
    const regularParticipants = computed(() => {
      return filteredParticipants.value.filter(p => !p.isHost && p.status === 'joined')
    })
    
    const waitingParticipants = computed(() => {
      return filteredParticipants.value.filter(p => p.status === 'waiting')
    })
    
    // 获取参与者状态文本
    const getParticipantStatus = (participant) => {
      const statuses = []
      
      if (participant.isScreenSharing) {
        statuses.push('正在共享屏幕')
      }
      
      if (participant.isRecording) {
        statuses.push('正在录制')
      }
      
      if (participant.connectionStatus === 'poor') {
        statuses.push('网络不佳')
      } else if (participant.connectionStatus === 'reconnecting') {
        statuses.push('重新连接中')
      }
      
      if (!participant.audioEnabled && !participant.videoEnabled) {
        statuses.push('音视频已关闭')
      } else if (!participant.audioEnabled) {
        statuses.push('已静音')
      } else if (!participant.videoEnabled) {
        statuses.push('视频已关闭')
      }
      
      return statuses.length > 0 ? statuses.join(', ') : '在线'
    }
    
    // 权限检查
    const canControlParticipant = (participant) => {
      return participant.id === currentUserId.value || props.isHost
    }
    
    const canManageParticipant = (participant) => {
      return participant.id !== currentUserId.value
    }
    
    // 参与者控制
    const toggleParticipantAudio = (participant) => {
      emit('toggle-audio', participant)
    }
    
    const toggleParticipantVideo = (participant) => {
      emit('toggle-video', participant)
    }
    
    const handleParticipantAction = async (command, participant) => {
      switch (command) {
        case 'pin':
        case 'unpin':
          emit('pin-participant', participant, command === 'pin')
          break
        case 'spotlight':
        case 'unspotlight':
          emit('spotlight-participant', participant, command === 'spotlight')
          break
        case 'makeHost':
          try {
            await ElMessageBox.confirm(
              `确定要将 ${participant.name} 设为主持人吗？`,
              '设为主持人',
              {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
              }
            )
            emit('make-host', participant)
          } catch {
            // 用户取消
          }
          break
        case 'private-chat':
          emit('private-chat', participant)
          break
        case 'remove':
          try {
            await ElMessageBox.confirm(
              `确定要移除 ${participant.name} 吗？`,
              '移除参与者',
              {
                confirmButtonText: '移除',
                cancelButtonText: '取消',
                type: 'warning'
              }
            )
            emit('remove-participant', participant)
          } catch {
            // 用户取消
          }
          break
      }
    }
    
    // 等待列表管理
    const admitParticipant = (participant) => {
      emit('admit-participant', participant)
    }
    
    const denyParticipant = (participant) => {
      emit('deny-participant', participant)
    }
    
    // 会议设置更新
    const updateMeetingSettings = async () => {
      try {
        const settings = {
          allowInvite: allowInvite.value,
          allowScreenShare: allowScreenShare.value,
          autoMuteNewJoiners: autoMuteNewJoiners.value,
          meetingLocked: meetingLocked.value
        }
        
        await meetingApi.updateMeetingSettings(props.meetingId, settings)
        ElMessage.success('设置已更新')
      } catch (error) {
        console.error('更新设置失败:', error)
        ElMessage.error('更新设置失败')
      }
    }
    
    // 邀请功能
    const generateMeetingLink = () => {
      const baseUrl = window.location.origin
      meetingLink.value = `${baseUrl}/meeting/join?id=${props.meetingId}`
      if (meetingPassword.value) {
        meetingLink.value += `&password=${meetingPassword.value}`
      }
    }
    
    const copyMeetingLink = async () => {
      try {
        await navigator.clipboard.writeText(meetingLink.value)
        ElMessage.success('链接已复制到剪贴板')
      } catch (error) {
        console.error('复制失败:', error)
        ElMessage.error('复制失败')
      }
    }
    
    const shareViaEmail = () => {
      const subject = encodeURIComponent('会议邀请')
      const body = encodeURIComponent(`您被邀请参加会议\n\n会议链接: ${meetingLink.value}\n会议号: ${props.meetingId}\n会议密码: ${meetingPassword.value || '无'}`)
      window.open(`mailto:?subject=${subject}&body=${body}`)
    }
    
    const shareViaQR = () => {
      // 实现二维码分享
      ElMessage.info('二维码功能开发中')
    }
    
    const searchUsers = async () => {
      if (!userSearchKeyword.value.trim()) {
        searchResults.value = []
        return
      }
      
      try {
        const response = await meetingApi.searchUsers(userSearchKeyword.value)
        searchResults.value = response.data || []
      } catch (error) {
        console.error('搜索用户失败:', error)
      }
    }
    
    const toggleUserSelection = (user) => {
      const index = selectedUsers.value.indexOf(user.id)
      if (index > -1) {
        selectedUsers.value.splice(index, 1)
      } else {
        selectedUsers.value.push(user.id)
      }
    }
    
    const removeSelectedUser = (userId) => {
      const index = selectedUsers.value.indexOf(userId)
      if (index > -1) {
        selectedUsers.value.splice(index, 1)
      }
    }
    
    const getUserName = (userId) => {
      const user = searchResults.value.find(u => u.id === userId)
      return user ? user.name : userId
    }
    
    const sendInvitations = async () => {
      if (inviteTab.value === 'link') {
        copyMeetingLink()
        showInviteDialog.value = false
        return
      }
      
      if (selectedUsers.value.length === 0) {
        ElMessage.warning('请选择要邀请的用户')
        return
      }
      
      inviteLoading.value = true
      try {
        await meetingApi.inviteUsers(props.meetingId, selectedUsers.value)
        ElMessage.success(`已向 ${selectedUsers.value.length} 人发送邀请`)
        showInviteDialog.value = false
        selectedUsers.value = []
      } catch (error) {
        console.error('发送邀请失败:', error)
        ElMessage.error('发送邀请失败')
      } finally {
        inviteLoading.value = false
      }
    }
    
    const handleInviteDialogClose = () => {
      selectedUsers.value = []
      userSearchKeyword.value = ''
      searchResults.value = []
      showInviteDialog.value = false
    }
    
    // 监听会议设置变化
    watch(() => props.meetingSettings, (newSettings) => {
      allowInvite.value = newSettings.allowInvite || false
      allowScreenShare.value = newSettings.allowScreenShare || true
      autoMuteNewJoiners.value = newSettings.autoMuteNewJoiners || false
      meetingLocked.value = newSettings.meetingLocked || false
    }, { deep: true })
    
    // 组件挂载
    onMounted(() => {
      generateMeetingLink()
    })
    
    return {
      currentUserId,
      searchKeyword,
      showSettings,
      allowInvite,
      allowScreenShare,
      autoMuteNewJoiners,
      meetingLocked,
      showInviteDialog,
      inviteTab,
      inviteLoading,
      meetingLink,
      meetingPassword,
      userSearchKeyword,
      searchResults,
      selectedUsers,
      canInvite,
      hostParticipants,
      regularParticipants,
      waitingParticipants,
      getParticipantStatus,
      canControlParticipant,
      canManageParticipant,
      toggleParticipantAudio,
      toggleParticipantVideo,
      handleParticipantAction,
      admitParticipant,
      denyParticipant,
      updateMeetingSettings,
      copyMeetingLink,
      shareViaEmail,
      shareViaQR,
      searchUsers,
      toggleUserSelection,
      removeSelectedUser,
      getUserName,
      sendInvitations,
      handleInviteDialogClose
    }
  }
}
</script>

<style scoped>
.participant-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-weight: 600;
  color: #303133;
}

.count-badge {
  margin-left: 4px;
}

.header-right {
  display: flex;
  gap: 4px;
}

.settings-panel {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.search-container {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.participants-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.participant-section {
  margin-bottom: 16px;
}

.section-title {
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.participant-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  transition: background-color 0.2s;
}

.participant-item:hover {
  background: #f5f7fa;
}

.participant-item.host {
  background: #f0f9ff;
}

.participant-item.waiting {
  background: #fef3c7;
}

.participant-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.avatar-container {
  position: relative;
}

.status-indicators {
  position: absolute;
  bottom: -2px;
  right: -2px;
  display: flex;
  gap: 2px;
}

.connection-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 1px solid white;
}

.connection-status.excellent {
  background: #67c23a;
}

.connection-status.good {
  background: #e6a23c;
}

.connection-status.fair {
  background: #f56c6c;
}

.connection-status.poor {
  background: #f56c6c;
  animation: blink 1s infinite;
}

.connection-status.reconnecting {
  background: #909399;
  animation: pulse 1s infinite;
}

.host-icon {
  color: #f39c12;
  font-size: 12px;
}

.screen-share-icon {
  color: #409eff;
  font-size: 12px;
}

.recording-icon {
  color: #f56c6c;
  font-size: 12px;
}

.waiting-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e6a23c;
  animation: pulse 1s infinite;
}

.participant-details {
  flex: 1;
  min-width: 0;
}

.name-container {
  display: flex;
  align-items: center;
  gap: 4px;
}

.name {
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.you-label {
  font-size: 12px;
  color: #409eff;
  font-weight: normal;
}

.status-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.participant-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.participant-controls .el-button {
  padding: 4px;
}

.audio-disabled {
  color: #f56c6c !important;
}

.video-disabled {
  color: #f56c6c !important;
}

/* 邀请对话框样式 */
.invite-content {
  min-height: 300px;
}

.invite-link-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.link-container {
  margin-bottom: 16px;
}

.meeting-info {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.meeting-info p {
  margin: 4px 0;
}

.share-options {
  display: flex;
  gap: 8px;
}

.invite-users-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.user-item:hover {
  background: #f5f7fa;
}

.user-name {
  font-weight: 500;
  flex: 1;
}

.user-department {
  font-size: 12px;
  color: #909399;
}

.selected-users {
  border-top: 1px solid #e4e7ed;
  padding-top: 12px;
}

.selected-title {
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 动画效果 */
@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0.3;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 滚动条样式 */
.participants-container::-webkit-scrollbar,
.user-list::-webkit-scrollbar {
  width: 6px;
}

.participants-container::-webkit-scrollbar-track,
.user-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.participants-container::-webkit-scrollbar-thumb,
.user-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.participants-container::-webkit-scrollbar-thumb:hover,
.user-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .list-header {
    padding: 12px;
  }
  
  .participant-item {
    padding: 12px;
  }
  
  .participant-controls {
    flex-direction: column;
    gap: 4px;
  }
  
  .share-options {
    flex-direction: column;
  }
}

/* 深色主题适配 */
.dark .participant-list {
  background: #1a1a1a;
  color: #e5e5e5;
}

.dark .list-header {
  background: #2a2a2a;
  border-bottom-color: #3a3a3a;
}

.dark .settings-panel {
  background: #2a2a2a;
  border-bottom-color: #3a3a3a;
}

.dark .participant-item:hover {
  background: #2a2a2a;
}

.dark .participant-item.host {
  background: #1e3a8a;
}

.dark .participant-item.waiting {
  background: #92400e;
}

.dark .meeting-info {
  background: #2a2a2a;
}

.dark .user-item:hover {
  background: #2a2a2a;
}
</style>