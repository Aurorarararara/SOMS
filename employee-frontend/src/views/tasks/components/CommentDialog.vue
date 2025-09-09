<template>
  <el-dialog 
    v-model="dialogVisible"
    title="任务批注与评论"
    width="700px"
    @close="handleClose"
  >
    <div class="comment-container">
      <!-- 任务信息 -->
      <div class="task-info">
        <h3>{{ task?.title }}</h3>
        <el-tag :type="getPriorityType(task?.priority)">
          {{ getPriorityText(task?.priority) }}
        </el-tag>
      </div>

      <!-- 评论输入 -->
      <div class="comment-input">
        <el-form @submit.prevent="handleAddComment">
          <el-form-item>
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="添加评论或批注..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <div class="comment-actions">
              <div class="comment-tools">
                <el-tooltip content="添加批注" placement="top">
                  <el-button 
                    :type="isAnnotation ? 'primary' : 'default'"
                    size="small"
                    @click="toggleAnnotation"
                  >
                    <el-icon><EditPen /></el-icon>
                    批注模式
                  </el-button>
                </el-tooltip>
                <el-tooltip content="上传附件" placement="top">
                  <el-upload
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    :on-success="handleFileUpload"
                  >
                    <el-button size="small">
                      <el-icon><Paperclip /></el-icon>
                      附件
                    </el-button>
                  </el-upload>
                </el-tooltip>
                <el-tooltip content="@提醒同事" placement="top">
                  <el-button size="small" @click="showMentionDialog = true">
                    <el-icon><Bell /></el-icon>
                    提醒
                  </el-button>
                </el-tooltip>
              </div>
              <div class="submit-actions">
                <el-button 
                  type="primary" 
                  @click="handleAddComment"
                  :loading="submitting"
                >
                  发布评论
                </el-button>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <div class="comment-header">
          <span class="comment-count">共 {{ comments.length }} 条评论</span>
          <el-select 
            v-model="sortType" 
            size="small" 
            style="width: 120px"
            @change="sortComments"
          >
            <el-option label="最新优先" value="newest" />
            <el-option label="最早优先" value="oldest" />
          </el-select>
        </div>

        <div class="comments">
          <div 
            v-for="comment in sortedComments" 
            :key="comment.id"
            class="comment-item"
            :class="{ 'is-annotation': comment.type === 'annotation' }"
          >
            <div class="comment-avatar">
              <el-avatar :size="36" :src="comment.userAvatar">
                {{ comment.userName?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <div class="user-info">
                  <span class="user-name">{{ comment.userName }}</span>
                  <span class="user-role">{{ comment.userRole }}</span>
                  <el-tag v-if="comment.type === 'annotation'" type="warning" size="small">
                    批注
                  </el-tag>
                </div>
                <div class="comment-meta">
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  <el-dropdown trigger="click">
                    <el-button type="text" size="small">
                      <el-icon><More /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click="replyComment(comment)">回复</el-dropdown-item>
                        <el-dropdown-item @click="editComment(comment)">编辑</el-dropdown-item>
                        <el-dropdown-item 
                          @click="deleteComment(comment)"
                          class="danger-item"
                        >
                          删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
              
              <div class="comment-body">
                <div class="comment-text" v-html="formatCommentText(comment.content)"></div>
                <div v-if="comment.attachments?.length" class="comment-attachments">
                  <div 
                    v-for="file in comment.attachments" 
                    :key="file.id"
                    class="attachment-item"
                    @click="previewFile(file)"
                  >
                    <el-icon><Document /></el-icon>
                    <span>{{ file.name }}</span>
                  </div>
                </div>
              </div>

              <!-- 回复列表 -->
              <div v-if="comment.replies?.length" class="replies">
                <div 
                  v-for="reply in comment.replies" 
                  :key="reply.id"
                  class="reply-item"
                >
                  <div class="reply-avatar">
                    <el-avatar :size="24" :src="reply.userAvatar">
                      {{ reply.userName?.charAt(0) }}
                    </el-avatar>
                  </div>
                  <div class="reply-content">
                    <span class="reply-user">{{ reply.userName }}</span>
                    <span class="reply-text">{{ reply.content }}</span>
                    <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                  </div>
                </div>
              </div>

              <!-- 回复输入框 -->
              <div v-if="replyingTo === comment.id" class="reply-input">
                <el-input
                  v-model="replyContent"
                  size="small"
                  placeholder="输入回复内容..."
                  @keyup.enter="handleReply(comment)"
                >
                  <template #append>
                    <el-button @click="handleReply(comment)">回复</el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- @提醒对话框 -->
    <el-dialog
      v-model="showMentionDialog"
      title="提醒同事"
      width="400px"
    >
      <el-select
        v-model="selectedMentions"
        multiple
        filterable
        placeholder="选择要提醒的同事"
        style="width: 100%"
      >
        <el-option
          v-for="user in userOptions"
          :key="user.id"
          :label="user.name"
          :value="user.id"
        >
          <div class="user-option">
            <el-avatar :size="20" :src="user.avatar">
              {{ user.name.charAt(0) }}
            </el-avatar>
            <span>{{ user.name }}</span>
          </div>
        </el-option>
      </el-select>
      <template #footer>
        <el-button @click="showMentionDialog = false">取消</el-button>
        <el-button type="primary" @click="addMentions">确定</el-button>
      </template>
    </el-dialog>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  EditPen, Paperclip, Bell, More, Document 
} from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: Boolean,
  task: Object
})

const emit = defineEmits(['update:modelValue', 'save'])

// 响应式数据
const newComment = ref('')
const isAnnotation = ref(false)
const submitting = ref(false)
const sortType = ref('newest')
const replyingTo = ref(null)
const replyContent = ref('')
const showMentionDialog = ref(false)
const selectedMentions = ref([])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 模拟评论数据
const comments = ref([
  {
    id: 1,
    content: '这个任务的优先级需要提高，建议尽快完成。',
    type: 'comment',
    userName: '张经理',
    userRole: '项目经理',
    userAvatar: '',
    createTime: '2025-08-26 10:30:00',
    attachments: [],
    replies: [
      {
        id: 11,
        content: '好的，我会加快进度。',
        userName: '李开发',
        userAvatar: '',
        createTime: '2025-08-26 10:35:00'
      }
    ]
  },
  {
    id: 2,
    content: '在第三个模块中发现了一些问题，需要重新设计。',
    type: 'annotation',
    userName: '王测试',
    userRole: '测试工程师',
    userAvatar: '',
    createTime: '2025-08-26 14:20:00',
    attachments: [
      { id: 1, name: '问题截图.png', url: '/files/screenshot.png' }
    ],
    replies: []
  }
])

const userOptions = ref([
  { id: 1, name: '张三', avatar: '' },
  { id: 2, name: '李四', avatar: '' },
  { id: 3, name: '王五', avatar: '' }
])

// 上传配置
const uploadUrl = ref('/api/upload')
const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('employee_token')}`
})

// 计算属性
const sortedComments = computed(() => {
  const sorted = [...comments.value]
  if (sortType.value === 'newest') {
    return sorted.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } else {
    return sorted.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
  }
})

// 方法
const getPriorityType = (priority) => {
  const types = { urgent: 'danger', high: 'warning', medium: 'info', low: 'success' }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = { urgent: '紧急', high: '高', medium: '中', low: '低' }
  return texts[priority] || '中'
}

const toggleAnnotation = () => {
  isAnnotation.value = !isAnnotation.value
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const formatCommentText = (text) => {
  // 处理@提醒和其他格式
  return text.replace(/@(\w+)/g, '<span class="mention">@$1</span>')
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const comment = {
      id: Date.now(),
      content: newComment.value,
      type: isAnnotation.value ? 'annotation' : 'comment',
      userName: '当前用户',
      userRole: '开发工程师',
      userAvatar: '',
      createTime: new Date().toISOString(),
      attachments: [],
      replies: []
    }
    
    comments.value.push(comment)
    newComment.value = ''
    isAnnotation.value = false
    
    ElMessage.success('评论发布成功')
    emit('save', comment)
  } finally {
    submitting.value = false
  }
}

const beforeUpload = (file) => {
  const isValidSize = file.size / 1024 / 1024 < 5
  if (!isValidSize) {
    ElMessage.error('文件大小不能超过 5MB!')
    return false
  }
  return true
}

const handleFileUpload = (response, file) => {
  // 处理文件上传成功
  ElMessage.success('文件上传成功')
}

const replyComment = (comment) => {
  replyingTo.value = comment.id
}

const handleReply = async (comment) => {
  if (!replyContent.value.trim()) return
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 300))
    
    const reply = {
      id: Date.now(),
      content: replyContent.value,
      userName: '当前用户',
      userAvatar: '',
      createTime: new Date().toISOString()
    }
    
    if (!comment.replies) comment.replies = []
    comment.replies.push(reply)
    
    replyContent.value = ''
    replyingTo.value = null
    
    ElMessage.success('回复成功')
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const editComment = (comment) => {
  // 编辑评论逻辑
  ElMessage.info('编辑功能开发中...')
}

const deleteComment = (comment) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const index = comments.value.findIndex(c => c.id === comment.id)
    if (index > -1) {
      comments.value.splice(index, 1)
      ElMessage.success('评论删除成功')
    }
  })
}

const previewFile = (file) => {
  // 文件预览逻辑
  window.open(file.url, '_blank')
}

const addMentions = () => {
  if (selectedMentions.value.length > 0) {
    const mentions = selectedMentions.value.map(id => {
      const user = userOptions.value.find(u => u.id === id)
      return `@${user.name}`
    }).join(' ')
    
    newComment.value += ` ${mentions}`
    selectedMentions.value = []
    showMentionDialog.value = false
  }
}

const sortComments = () => {
  // 排序逻辑已在计算属性中处理
}

const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.comment-container {
  max-height: 600px;
  overflow-y: auto;
}

.task-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.task-info h3 {
  margin: 0;
  color: #1a1a1a;
  font-size: 18px;
}

.comment-input {
  margin-bottom: 24px;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-tools {
  display: flex;
  gap: 8px;
}

.comment-list {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.comment-count {
  font-weight: 500;
  color: #666;
}

.comment-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.comment-item.is-annotation {
  background: #fff7e6;
  border-left: 4px solid #faad14;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: #1a1a1a;
}

.user-role {
  font-size: 12px;
  color: #999;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  color: #333;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-attachments {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #1890ff;
}

.attachment-item:hover {
  background: #f0f8ff;
}

.replies {
  margin-top: 12px;
  padding-left: 16px;
  border-left: 2px solid #e8e8e8;
}

.reply-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.reply-user {
  font-weight: 500;
  font-size: 12px;
  color: #1890ff;
}

.reply-text {
  color: #333;
  font-size: 13px;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.reply-input {
  margin-top: 8px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mention {
  color: #1890ff;
  background: #f0f8ff;
  padding: 2px 4px;
  border-radius: 3px;
}

.danger-item {
  color: #ff4d4f;
}
</style>