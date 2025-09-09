<template>
  <div class="collaboration-demo">
    <div class="demo-header">
      <h2>协同编辑功能演示</h2>
      <p>展示用户在线状态、光标位置和实时协同编辑功能</p>
    </div>

    <div class="demo-controls">
      <div class="control-group">
        <label>模拟用户数量:</label>
        <input 
          v-model.number="simulatedUserCount" 
          type="number" 
          min="0" 
          max="10" 
          @change="updateSimulatedUsers"
        />
      </div>
      
      <div class="control-group">
        <label>编辑器类型:</label>
        <select v-model="selectedEditor" @change="switchEditor">
          <option value="richtext">富文本编辑器</option>
          <option value="markdown">Markdown编辑器</option>
          <option value="code">代码编辑器</option>
          <option value="table">表格编辑器</option>
        </select>
      </div>
      
      <div class="control-group">
        <button @click="toggleUserActivity" :class="{ active: userActivityEnabled }">
          {{ userActivityEnabled ? '停止用户活动' : '模拟用户活动' }}
        </button>
      </div>
    </div>

    <!-- 功能特性列表 -->
    <div class="features-showcase">
      <h3>已实现的功能特性</h3>
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon">👥</div>
          <h4>在线用户显示</h4>
          <p>实时显示当前在线的协同编辑用户，包括用户头像和状态</p>
          <div class="feature-status implemented">✅ 已实现</div>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">📍</div>
          <h4>光标位置同步</h4>
          <p>显示其他用户的光标位置，支持多种编辑器类型</p>
          <div class="feature-status implemented">✅ 已实现</div>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">✏️</div>
          <h4>实时输入状态</h4>
          <p>显示用户正在输入的状态，提供即时反馈</p>
          <div class="feature-status implemented">✅ 已实现</div>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">🎨</div>
          <h4>用户色彩区分</h4>
          <p>每个用户有独特的颜色标识，便于识别</p>
          <div class="feature-status implemented">✅ 已实现</div>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">🔄</div>
          <h4>实时内容同步</h4>
          <p>文档内容实时同步，支持操作转换</p>
          <div class="feature-status implemented">✅ 已实现</div>
        </div>
        
        <div class="feature-card">
          <div class="feature-icon">🌐</div>
          <h4>WebSocket连接</h4>
          <p>基于WebSocket的实时通信，低延迟高效率</p>
          <div class="feature-status implemented">✅ 已实现</div>
        </div>
      </div>
    </div>

    <!-- 技术架构说明 -->
    <div class="architecture-info">
      <h3>技术架构</h3>
      <div class="architecture-diagram">
        <div class="layer frontend">
          <h4>前端层</h4>
          <ul>
            <li>Vue 3 + Composition API</li>
            <li>UserPresence 组件</li>
            <li>CursorTrackingService 服务</li>
            <li>WebSocket 客户端</li>
          </ul>
        </div>
        
        <div class="layer backend">
          <h4>后端层</h4>
          <ul>
            <li>Spring Boot WebSocket</li>
            <li>STOMP 消息协议</li>
            <li>CollaborationWebSocketHandler</li>
            <li>Session 管理</li>
          </ul>
        </div>
        
        <div class="layer database">
          <h4>数据层</h4>
          <ul>
            <li>MySQL 数据存储</li>
            <li>Redis 会话缓存</li>
            <li>操作日志记录</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 模拟用户列表 -->
    <div class="simulated-users">
      <h3>模拟在线用户</h3>
      <div class="users-list">
        <div 
          v-for="user in simulatedUsers" 
          :key="user.sessionId"
          class="user-item"
          :class="{ 'is-typing': user.isTyping }"
        >
          <div 
            class="user-avatar"
            :style="{ backgroundColor: user.userColor }"
          >
            {{ user.userName.charAt(0) }}
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.userName }}</div>
            <div class="user-status">
              <span v-if="user.isTyping" class="typing">正在输入...</span>
              <span v-else class="online">在线</span>
            </div>
          </div>
          <div class="user-actions">
            <button @click="toggleUserTyping(user)" class="action-btn">
              {{ user.isTyping ? '停止输入' : '开始输入' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 使用说明 -->
    <div class="usage-guide">
      <h3>使用说明</h3>
      <div class="guide-steps">
        <div class="step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h4>创建文档</h4>
            <p>在协同编辑页面创建新文档或打开现有文档</p>
          </div>
        </div>
        
        <div class="step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h4>分享协作</h4>
            <p>通过分享链接邀请其他用户加入协同编辑</p>
          </div>
        </div>
        
        <div class="step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h4>实时编辑</h4>
            <p>多用户可同时编辑，实时查看他人的光标位置和编辑内容</p>
          </div>
        </div>
        
        <div class="step">
          <div class="step-number">4</div>
          <div class="step-content">
            <h4>状态感知</h4>
            <p>查看在线用户列表，了解谁在编辑什么位置</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 响应式数据
const simulatedUserCount = ref(3)
const selectedEditor = ref('richtext')
const userActivityEnabled = ref(false)
const simulatedUsers = ref([])
const activityInterval = ref(null)

// 模拟用户数据
const mockUsers = [
  { name: '张三', color: '#FF6B6B' },
  { name: '李四', color: '#4ECDC4' },
  { name: '王五', color: '#45B7D1' },
  { name: '赵六', color: '#96CEB4' },
  { name: '钱七', color: '#FFEAA7' },
  { name: '孙八', color: '#DDA0DD' },
  { name: '周九', color: '#98D8C8' },
  { name: '吴十', color: '#F7DC6F' }
]

// 生命周期
onMounted(() => {
  updateSimulatedUsers()
})

onUnmounted(() => {
  if (activityInterval.value) {
    clearInterval(activityInterval.value)
  }
})

// 方法
function updateSimulatedUsers() {
  const users = []
  for (let i = 0; i < simulatedUserCount.value; i++) {
    const mockUser = mockUsers[i % mockUsers.length]
    users.push({
      sessionId: `sim_${i}`,
      userId: i + 1,
      userName: mockUser.name,
      userColor: mockUser.color,
      isTyping: false,
      lastSeen: Date.now()
    })
  }
  simulatedUsers.value = users
}

function switchEditor() {
  console.log('切换到编辑器类型:', selectedEditor.value)
  // 这里可以添加实际的编辑器切换逻辑
}

function toggleUserActivity() {
  userActivityEnabled.value = !userActivityEnabled.value
  
  if (userActivityEnabled.value) {
    startUserActivity()
  } else {
    stopUserActivity()
  }
}

function startUserActivity() {
  activityInterval.value = setInterval(() => {
    // 随机选择一个用户开始或停止输入
    if (simulatedUsers.value.length > 0) {
      const randomUser = simulatedUsers.value[Math.floor(Math.random() * simulatedUsers.value.length)]
      randomUser.isTyping = Math.random() > 0.7
      randomUser.lastSeen = Date.now()
    }
  }, 2000)
}

function stopUserActivity() {
  if (activityInterval.value) {
    clearInterval(activityInterval.value)
    activityInterval.value = null
  }
  
  // 停止所有用户的输入状态
  simulatedUsers.value.forEach(user => {
    user.isTyping = false
  })
}

function toggleUserTyping(user) {
  user.isTyping = !user.isTyping
  user.lastSeen = Date.now()
}

function generateSessionId() {
  return 'demo_' + Math.random().toString(36).substr(2, 9)
}
</script>

<style scoped>
.collaboration-demo {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.demo-header {
  text-align: center;
  margin-bottom: 40px;
}

.demo-header h2 {
  font-size: 32px;
  color: #333;
  margin-bottom: 10px;
}

.demo-header p {
  font-size: 16px;
  color: #666;
}

.demo-controls {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.control-group label {
  font-weight: 500;
  color: #333;
}

.control-group input,
.control-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.control-group button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  background: #1890ff;
  color: white;
  transition: background 0.2s;
}

.control-group button:hover {
  background: #40a9ff;
}

.control-group button.active {
  background: #52c41a;
}

.features-showcase {
  margin-bottom: 40px;
}

.features-showcase h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.feature-card {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: box-shadow 0.2s;
}

.feature-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-card h4 {
  font-size: 18px;
  color: #333;
  margin-bottom: 12px;
}

.feature-card p {
  color: #666;
  line-height: 1.5;
  margin-bottom: 16px;
}

.feature-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.feature-status.implemented {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.architecture-info {
  margin-bottom: 40px;
}

.architecture-info h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.architecture-diagram {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.layer {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
}

.layer h4 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #333;
}

.layer ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.layer li {
  padding: 4px 0;
  color: #666;
  font-size: 14px;
}

.layer.frontend {
  border-left: 4px solid #1890ff;
}

.layer.backend {
  border-left: 4px solid #52c41a;
}

.layer.database {
  border-left: 4px solid #faad14;
}

.simulated-users {
  margin-bottom: 40px;
}

.simulated-users h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.users-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.2s;
}

.user-item.is-typing {
  background: #e6f7ff;
  border-color: #91d5ff;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  margin-right: 12px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.user-status {
  font-size: 12px;
}

.user-status .typing {
  color: #52c41a;
  font-weight: 500;
}

.user-status .online {
  color: #666;
}

.action-btn {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}

.usage-guide {
  margin-bottom: 40px;
}

.usage-guide h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.guide-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.step {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.step-number {
  width: 32px;
  height: 32px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.step-content p {
  color: #666;
  line-height: 1.5;
  font-size: 14px;
}
</style>