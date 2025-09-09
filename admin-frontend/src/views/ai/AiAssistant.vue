<template>
  <div class="ai-assistant-container">
    <!-- 助手选择侧边栏 -->
    <div class="assistant-sidebar">
      <div class="sidebar-header">
        <h3>AI助手</h3>
        <el-button type="primary" size="small" @click="showCreateAssistant = true">
          <el-icon><Plus /></el-icon>
          创建助手
        </el-button>
      </div>
      
      <div class="assistant-list">
        <div 
          v-for="assistant in assistants" 
          :key="assistant.id"
          class="assistant-item"
          :class="{ active: currentAssistant?.id === assistant.id }"
          @click="selectAssistant(assistant)"
        >
          <div class="assistant-avatar">
            <img v-if="assistant.assistantAvatar" :src="assistant.assistantAvatar" :alt="assistant.assistantName">
            <el-icon v-else><Robot /></el-icon>
          </div>
          <div class="assistant-info">
            <div class="assistant-name">{{ assistant.assistantName }}</div>
            <div class="assistant-type">{{ getAssistantTypeLabel(assistant.assistantType) }}</div>
          </div>
          <div class="assistant-status">
            <el-tag v-if="assistant.isActive" type="success" size="small">活跃</el-tag>
            <el-tag v-else type="info" size="small">停用</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 对话区域 -->
    <div class="conversation-area">
      <div v-if="!currentAssistant" class="welcome-screen">
        <div class="welcome-content">
          <el-icon class="welcome-icon"><Robot /></el-icon>
          <h2>欢迎使用AI助手</h2>
          <p>选择一个AI助手开始对话，或创建您自己的专属助手</p>
        </div>
      </div>

      <div v-else class="chat-container">
        <!-- 对话头部 -->
        <div class="chat-header">
          <div class="assistant-info">
            <div class="assistant-avatar">
              <img v-if="currentAssistant.assistantAvatar" :src="currentAssistant.assistantAvatar" :alt="currentAssistant.assistantName">
              <el-icon v-else><Robot /></el-icon>
            </div>
            <div class="assistant-details">
              <h3>{{ currentAssistant.assistantName }}</h3>
              <p>{{ currentAssistant.assistantDescription }}</p>
            </div>
          </div>
          
          <div class="chat-actions">
            <el-dropdown @command="handleChatAction">
              <el-button type="text">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="new-conversation">新建对话</el-dropdown-item>
                  <el-dropdown-item command="export-conversation">导出对话</el-dropdown-item>
                  <el-dropdown-item command="clear-conversation">清空对话</el-dropdown-item>
                  <el-dropdown-item command="assistant-settings">助手设置</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-container" ref="messagesContainer">
          <div v-for="message in messages" :key="message.id" class="message-item" :class="message.messageRole">
            <div class="message-avatar">
              <img v-if="message.messageRole === 'assistant' && currentAssistant.assistantAvatar" 
                   :src="currentAssistant.assistantAvatar" :alt="currentAssistant.assistantName">
              <el-icon v-else-if="message.messageRole === 'assistant'"><Robot /></el-icon>
              <el-avatar v-else :src="currentUser.avatar" :size="32">{{ currentUser.name?.charAt(0) }}</el-avatar>
            </div>
            
            <div class="message-content">
              <div class="message-header">
                <span class="sender-name">
                  {{ message.messageRole === 'assistant' ? currentAssistant.assistantName : currentUser.name }}
                </span>
                <span class="message-time">{{ formatTime(message.createdAt) }}</span>
              </div>
              
              <div class="message-body" v-html="renderMessage(message.messageContent, message.messageFormat)"></div>
              
              <!-- 消息附件 -->
              <div v-if="message.attachments && message.attachments.length > 0" class="message-attachments">
                <div v-for="attachment in message.attachments" :key="attachment.id" class="attachment-item">
                  <el-icon><Paperclip /></el-icon>
                  <span>{{ attachment.name }}</span>
                </div>
              </div>
              
              <!-- 消息操作 -->
              <div class="message-actions">
                <el-button type="text" size="small" @click="copyMessage(message)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
                <el-button v-if="message.messageRole === 'assistant'" type="text" size="small" @click="regenerateMessage(message)">
                  <el-icon><Refresh /></el-icon>
                </el-button>
                <el-button type="text" size="small" @click="feedbackMessage(message)">
                  <el-icon><Star /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 加载中指示器 -->
          <div v-if="isLoading" class="message-item assistant">
            <div class="message-avatar">
              <el-icon><Robot /></el-icon>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-tools">
            <el-upload
              :show-file-list="false"
              :before-upload="handleFileUpload"
              accept=".txt,.pdf,.docx,.xlsx,.pptx,.jpg,.png,.gif"
            >
              <el-button type="text" size="small">
                <el-icon><Paperclip /></el-icon>
              </el-button>
            </el-upload>
            
            <el-button type="text" size="small" @click="showToolsPanel = !showToolsPanel">
              <el-icon><Tools /></el-icon>
            </el-button>
            
            <el-button type="text" size="small" @click="showTemplatesPanel = !showTemplatesPanel">
              <el-icon><Document /></el-icon>
            </el-button>
          </div>
          
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入您的问题或需求..."
              @keydown.enter.ctrl="sendMessage"
              @input="handleInputChange"
            />
            
            <!-- 智能提示 -->
            <div v-if="suggestions.length > 0" class="suggestions-panel">
              <div v-for="suggestion in suggestions" :key="suggestion" class="suggestion-item" @click="applySuggestion(suggestion)">
                {{ suggestion }}
              </div>
            </div>
          </div>
          
          <div class="input-actions">
            <el-button type="primary" @click="sendMessage" :loading="isLoading" :disabled="!inputMessage.trim()">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 工具面板 -->
    <div v-if="showToolsPanel" class="tools-panel">
      <div class="panel-header">
        <h4>AI工具</h4>
        <el-button type="text" @click="showToolsPanel = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="tools-grid">
        <div v-for="tool in availableTools" :key="tool.toolId" class="tool-item" @click="useTool(tool)">
          <div class="tool-icon">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="tool-info">
            <div class="tool-name">{{ tool.toolName }}</div>
            <div class="tool-description">{{ tool.toolDescription }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 模板面板 -->
    <div v-if="showTemplatesPanel" class="templates-panel">
      <div class="panel-header">
        <h4>对话模板</h4>
        <el-button type="text" @click="showTemplatesPanel = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="templates-list">
        <div v-for="template in templates" :key="template.id" class="template-item" @click="useTemplate(template)">
          <div class="template-title">{{ template.title }}</div>
          <div class="template-content">{{ template.content }}</div>
        </div>
      </div>
    </div>

    <!-- 创建助手对话框 -->
    <el-dialog v-model="showCreateAssistant" title="创建AI助手" width="600px">
      <el-form :model="newAssistant" :rules="assistantRules" ref="assistantForm" label-width="100px">
        <el-form-item label="助手名称" prop="assistantName">
          <el-input v-model="newAssistant.assistantName" placeholder="请输入助手名称" />
        </el-form-item>
        
        <el-form-item label="助手类型" prop="assistantType">
          <el-select v-model="newAssistant.assistantType" placeholder="请选择助手类型">
            <el-option label="通用助手" value="general" />
            <el-option label="文档助手" value="document" />
            <el-option label="数据分析助手" value="data" />
            <el-option label="代码助手" value="code" />
            <el-option label="会议助手" value="meeting" />
            <el-option label="HR助手" value="hr" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="助手描述" prop="assistantDescription">
          <el-input v-model="newAssistant.assistantDescription" type="textarea" :rows="3" placeholder="请输入助手描述" />
        </el-form-item>
        
        <el-form-item label="能力配置">
          <el-checkbox-group v-model="newAssistant.capabilities">
            <el-checkbox label="chat">对话聊天</el-checkbox>
            <el-checkbox label="analysis">数据分析</el-checkbox>
            <el-checkbox label="generation">内容生成</el-checkbox>
            <el-checkbox label="translation">翻译</el-checkbox>
            <el-checkbox label="summary">总结</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateAssistant = false">取消</el-button>
        <el-button type="primary" @click="createAssistant">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Robot, MoreFilled, Paperclip, Tools, Document, Close, CopyDocument, Refresh, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import MarkdownIt from 'markdown-it'

const userStore = useUserStore()
const currentUser = computed(() => userStore.userInfo)

// 响应式数据
const assistants = ref([])
const currentAssistant = ref(null)
const currentConversation = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const suggestions = ref([])
const availableTools = ref([])
const templates = ref([])

// 面板显示状态
const showToolsPanel = ref(false)
const showTemplatesPanel = ref(false)
const showCreateAssistant = ref(false)

// 创建助手表单
const newAssistant = reactive({
  assistantName: '',
  assistantType: '',
  assistantDescription: '',
  capabilities: []
})

const assistantRules = {
  assistantName: [
    { required: true, message: '请输入助手名称', trigger: 'blur' }
  ],
  assistantType: [
    { required: true, message: '请选择助手类型', trigger: 'change' }
  ],
  assistantDescription: [
    { required: true, message: '请输入助手描述', trigger: 'blur' }
  ]
}

// Markdown渲染器
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
})

// 组件引用
const messagesContainer = ref(null)
const assistantForm = ref(null)

// 生命周期
onMounted(() => {
  loadAssistants()
  loadAvailableTools()
  loadTemplates()
})

// 方法
const loadAssistants = async () => {
  try {
    // 模拟API调用
    assistants.value = [
      {
        id: 1,
        assistantId: 'general-assistant',
        assistantName: '通用助手',
        assistantType: 'general',
        assistantDescription: '智能通用助手，可以回答各种问题，协助日常工作',
        assistantAvatar: null,
        isActive: true,
        capabilities: ['chat', 'analysis', 'generation', 'translation', 'summary']
      },
      {
        id: 2,
        assistantId: 'document-assistant',
        assistantName: '文档助手',
        assistantType: 'document',
        assistantDescription: '专业文档处理助手，擅长文档分析、总结、翻译和格式转换',
        assistantAvatar: null,
        isActive: true,
        capabilities: ['analysis', 'summary', 'translation', 'generation']
      },
      {
        id: 3,
        assistantId: 'data-assistant',
        assistantName: '数据分析助手',
        assistantType: 'data',
        assistantDescription: '数据分析专家，提供数据洞察、图表生成和报告分析',
        assistantAvatar: null,
        isActive: true,
        capabilities: ['analysis', 'generation', 'visualization']
      }
    ]
  } catch (error) {
    ElMessage.error('加载助手列表失败')
  }
}

const selectAssistant = async (assistant) => {
  currentAssistant.value = assistant
  await loadConversation(assistant.id)
}

const loadConversation = async (assistantId) => {
  try {
    // 模拟加载对话
    messages.value = [
      {
        id: 1,
        messageRole: 'assistant',
        messageContent: `您好！我是${currentAssistant.value.assistantName}，很高兴为您服务。我可以帮助您：\n\n${getCapabilityDescription(currentAssistant.value.capabilities)}\n\n请告诉我您需要什么帮助？`,
        messageFormat: 'markdown',
        createdAt: new Date(),
        attachments: []
      }
    ]
    
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    ElMessage.error('加载对话失败')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const userMessage = {
    id: Date.now(),
    messageRole: 'user',
    messageContent: inputMessage.value,
    messageFormat: 'text',
    createdAt: new Date(),
    attachments: []
  }
  
  messages.value.push(userMessage)
  const userInput = inputMessage.value
  inputMessage.value = ''
  isLoading.value = true
  
  nextTick(() => {
    scrollToBottom()
  })
  
  try {
    // 模拟AI响应
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const assistantMessage = {
      id: Date.now() + 1,
      messageRole: 'assistant',
      messageContent: generateMockResponse(userInput),
      messageFormat: 'markdown',
      createdAt: new Date(),
      attachments: []
    }
    
    messages.value.push(assistantMessage)
    
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    ElMessage.error('发送消息失败')
  } finally {
    isLoading.value = false
  }
}

const generateMockResponse = (input) => {
  const responses = [
    `我理解您的问题："${input}"。让我为您分析一下：\n\n1. **问题分析**：这是一个很好的问题\n2. **解决方案**：我建议采用以下方法\n3. **具体步骤**：\n   - 第一步：分析现状\n   - 第二步：制定计划\n   - 第三步：执行方案\n\n希望这个回答对您有帮助！`,
    
    `根据您的需求，我为您提供以下建议：\n\n## 📊 数据分析\n\n基于您提到的"${input}"，我认为可以从以下几个维度来考虑：\n\n- **效率提升**：通过优化流程提高工作效率\n- **成本控制**：合理配置资源降低成本\n- **质量保证**：建立完善的质量管控体系\n\n您还有其他问题吗？`,
    
    `感谢您的提问！关于"${input}"，我的建议如下：\n\n### 🎯 核心要点\n\n1. **明确目标**：首先要明确您想要达到的目标\n2. **制定策略**：根据目标制定相应的执行策略\n3. **监控执行**：在执行过程中持续监控和调整\n\n### 💡 实用建议\n\n- 建议您可以先从小范围试点开始\n- 收集反馈并持续优化\n- 逐步扩大应用范围\n\n如果您需要更详细的分析，请告诉我具体的场景和需求。`
  ]
  
  return responses[Math.floor(Math.random() * responses.length)]
}

const getCapabilityDescription = (capabilities) => {
  const descriptions = {
    chat: '💬 智能对话交流',
    analysis: '📊 数据分析处理',
    generation: '✍️ 内容创作生成',
    translation: '🌐 多语言翻译',
    summary: '📝 文档总结提取',
    visualization: '📈 数据可视化',
    review: '🔍 代码审查',
    debug: '🐛 问题调试'
  }
  
  return capabilities.map(cap => descriptions[cap] || cap).join('\n')
}

const getAssistantTypeLabel = (type) => {
  const labels = {
    general: '通用助手',
    document: '文档助手',
    data: '数据分析',
    code: '代码助手',
    meeting: '会议助手',
    hr: 'HR助手'
  }
  return labels[type] || type
}

const formatTime = (time) => {
  return formatDistanceToNow(new Date(time), { 
    addSuffix: true, 
    locale: zhCN 
  })
}

const renderMessage = (content, format) => {
  if (format === 'markdown') {
    return md.render(content)
  }
  return content.replace(/\n/g, '<br>')
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const handleInputChange = (value) => {
  // 模拟智能提示
  if (value.length > 2) {
    suggestions.value = [
      '帮我分析这个数据',
      '生成一份报告',
      '翻译这段文字',
      '总结这个文档'
    ].filter(s => s.includes(value))
  } else {
    suggestions.value = []
  }
}

const applySuggestion = (suggestion) => {
  inputMessage.value = suggestion
  suggestions.value = []
}

const copyMessage = (message) => {
  navigator.clipboard.writeText(message.messageContent)
  ElMessage.success('消息已复制到剪贴板')
}

const regenerateMessage = async (message) => {
  // 实现重新生成消息的逻辑
  ElMessage.info('正在重新生成消息...')
}

const feedbackMessage = (message) => {
  // 实现消息反馈的逻辑
  ElMessage.info('感谢您的反馈！')
}

const handleFileUpload = (file) => {
  // 实现文件上传逻辑
  ElMessage.info(`正在上传文件：${file.name}`)
  return false // 阻止默认上传
}

const loadAvailableTools = async () => {
  availableTools.value = [
    {
      toolId: 'web-search',
      toolName: '网络搜索',
      toolDescription: '搜索互联网获取最新信息'
    },
    {
      toolId: 'file-analyzer',
      toolName: '文件分析器',
      toolDescription: '分析文件内容并提取关键信息'
    },
    {
      toolId: 'chart-generator',
      toolName: '图表生成器',
      toolDescription: '根据数据生成各种类型的图表'
    }
  ]
}

const loadTemplates = async () => {
  templates.value = [
    {
      id: 1,
      title: '数据分析请求',
      content: '请帮我分析以下数据，并提供洞察和建议：'
    },
    {
      id: 2,
      title: '文档总结',
      content: '请帮我总结这个文档的主要内容和关键点：'
    },
    {
      id: 3,
      title: '代码审查',
      content: '请帮我审查这段代码，指出潜在问题和改进建议：'
    }
  ]
}

const useTool = (tool) => {
  inputMessage.value = `使用${tool.toolName}：`
  showToolsPanel.value = false
}

const useTemplate = (template) => {
  inputMessage.value = template.content
  showTemplatesPanel.value = false
}

const createAssistant = async () => {
  try {
    await assistantForm.value.validate()
    
    // 模拟创建助手
    const assistant = {
      id: Date.now(),
      assistantId: `custom-${Date.now()}`,
      ...newAssistant,
      isActive: true,
      assistantAvatar: null
    }
    
    assistants.value.push(assistant)
    showCreateAssistant.value = false
    
    // 重置表单
    Object.keys(newAssistant).forEach(key => {
      if (Array.isArray(newAssistant[key])) {
        newAssistant[key] = []
      } else {
        newAssistant[key] = ''
      }
    })
    
    ElMessage.success('助手创建成功')
  } catch (error) {
    ElMessage.error('创建助手失败')
  }
}

const handleChatAction = (command) => {
  switch (command) {
    case 'new-conversation':
      messages.value = []
      loadConversation(currentAssistant.value.id)
      break
    case 'export-conversation':
      ElMessage.info('导出功能开发中...')
      break
    case 'clear-conversation':
      ElMessageBox.confirm('确定要清空当前对话吗？', '确认', {
        type: 'warning'
      }).then(() => {
        messages.value = []
        ElMessage.success('对话已清空')
      })
      break
    case 'assistant-settings':
      ElMessage.info('助手设置功能开发中...')
      break
  }
}
</script>

<style scoped>
.ai-assistant-container {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

.assistant-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  color: #333;
}

.assistant-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.assistant-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.assistant-item:hover {
  background: #f0f9ff;
}

.assistant-item.active {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
}

.assistant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.assistant-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.assistant-info {
  flex: 1;
}

.assistant-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.assistant-type {
  font-size: 12px;
  color: #666;
}

.conversation-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content {
  text-align: center;
  color: #666;
}

.welcome-icon {
  font-size: 64px;
  color: #ccc;
  margin-bottom: 20px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header .assistant-info {
  display: flex;
  align-items: center;
}

.chat-header .assistant-avatar {
  width: 48px;
  height: 48px;
  margin-right: 16px;
}

.assistant-details h3 {
  margin: 0 0 4px 0;
  color: #333;
}

.assistant-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 24px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-item .message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 12px;
}

.message-item.user .message-avatar {
  margin: 0 0 0 12px;
}

.message-content {
  max-width: 70%;
  min-width: 200px;
}

.message-item.user .message-content {
  text-align: right;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.message-item.user .message-header {
  justify-content: flex-end;
}

.sender-name {
  font-weight: 500;
  margin-right: 8px;
}

.message-body {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
}

.message-item.user .message-body {
  background: #2196f3;
  color: white;
}

.message-attachments {
  margin-top: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 8px;
  background: #f0f0f0;
  border-radius: 6px;
  margin-bottom: 4px;
  font-size: 14px;
}

.attachment-item .el-icon {
  margin-right: 8px;
}

.message-actions {
  margin-top: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.message-item:hover .message-actions {
  opacity: 1;
}

.message-item.user .message-actions {
  text-align: right;
}

.typing-indicator {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
  margin-right: 4px;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

.input-area {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  background: white;
}

.input-tools {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.input-tools .el-button {
  margin-right: 8px;
}

.input-container {
  position: relative;
  margin-bottom: 12px;
}

.suggestions-panel {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 10;
}

.suggestion-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.suggestion-item:hover {
  background: #f8f9fa;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}

.tools-panel,
.templates-panel {
  position: absolute;
  right: 20px;
  bottom: 120px;
  width: 300px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 100;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h4 {
  margin: 0;
  color: #333;
}

.tools-grid {
  padding: 16px;
}

.tool-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background 0.3s;
}

.tool-item:hover {
  background: #f8f9fa;
}

.tool-icon {
  width: 32px;
  height: 32px;
  background: #f0f0f0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.tool-description {
  font-size: 12px;
  color: #666;
}

.templates-list {
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.template-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.template-item:hover {
  border-color: #2196f3;
  background: #f8f9fa;
}

.template-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.template-content {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}
</style>