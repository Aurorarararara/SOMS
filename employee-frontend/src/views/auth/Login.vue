<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon class="logo-icon" :size="60">
            <OfficeBuilding />
          </el-icon>
        </div>
        <h1 class="brand-title">智慧办公系统</h1>
        <p class="brand-subtitle">提升企业办公效率，实现智能化管理</p>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon><UserFilled /></el-icon>
            <span>专业的人事管理</span>
          </div>
          <div class="feature-item">
            <el-icon><Timer /></el-icon>
            <span>高效的考勤统计</span>
          </div>
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>实时的数据分析</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="login-section">
      <div class="login-form-wrapper">
        <div class="login-header">
          <h2 class="login-title">登录员工端</h2>
          <p class="login-subtitle">请输入您的用户名和密码</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              clearable
              class="form-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
              class="form-input"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">立即登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <div class="help-links">
            <a href="#" class="help-link">忘记密码？</a>
            <a href="#" class="help-link">联系管理员</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, OfficeBuilding, UserFilled, Timer, DataAnalysis } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    await userStore.loginAction(loginForm)
    
    ElMessage.success('登录成功！')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 左侧品牌展示区 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="%23ffffff" opacity="0.03"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  animation: grain 20s linear infinite;
}

@keyframes grain {
  0%, 100% { transform: translate(0, 0); }
  10% { transform: translate(-5%, -10%); }
  20% { transform: translate(-15%, 5%); }
  30% { transform: translate(7%, -25%); }
  40% { transform: translate(-5%, 25%); }
  50% { transform: translate(-15%, 10%); }
  60% { transform: translate(15%, 0%); }
  70% { transform: translate(0%, 15%); }
  80% { transform: translate(3%, 35%); }
  90% { transform: translate(-10%, 10%); }
}

.brand-content {
  color: white;
  text-align: center;
  z-index: 1;
  position: relative;
}

.brand-logo {
  margin-bottom: 30px;
}

.logo-icon {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
  line-height: 1.6;
}

.feature-list {
  text-align: left;
  max-width: 300px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.feature-item:last-child {
  border-bottom: none;
}

.feature-item .el-icon {
  margin-right: 12px;
  font-size: 20px;
  opacity: 0.8;
}

.feature-item span {
  font-size: 16px;
  opacity: 0.9;
}

/* 右侧登录区域 */
.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 30px;
}

.login-form .el-form-item {
  margin-bottom: 24px;
}

.form-input {
  height: 48px;
}

.form-input .el-input__wrapper {
  border-radius: 8px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  box-shadow: none;
}

.form-input.is-focus .el-input__wrapper {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-icon {
  color: #999;
  margin-right: 8px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.login-footer {
  text-align: center;
}

.help-links {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.help-link {
  color: #666;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s ease;
}

.help-link:hover {
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .brand-section {
    min-height: 40vh;
    padding: 20px;
  }
  
  .brand-title {
    font-size: 28px;
  }
  
  .brand-subtitle {
    font-size: 16px;
  }
  
  .feature-list {
    display: none;
  }
  
  .login-section {
    padding: 20px;
  }
  
  .login-title {
    font-size: 24px;
  }
}
</style>