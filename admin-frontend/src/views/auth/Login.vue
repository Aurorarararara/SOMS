<template>
  <div class="admin-login-container">
    <div class="login-background">
      <div class="geometric-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
      <div class="gradient-overlay"></div>
    </div>
    
    <div class="login-content">
      <div class="login-card">
        <div class="login-header">
          <div class="brand-logo">
            <div class="logo-icon">
              <el-icon size="40"><Setting /></el-icon>
            </div>
            <div class="brand-text">
              <h1>{{ $t('login.adminPlatform') }}</h1>
              <p>{{ $t('login.systemTitle') }}</p>
            </div>
          </div>
        </div>
        
        <div class="login-form-wrapper">
          <div class="form-title">
            <h2>{{ $t('login.adminLogin') }}</h2>
            <p>{{ $t('login.adminLoginPrompt') }}</p>
          </div>
          
          <el-form 
            ref="loginFormRef" 
            :model="loginForm" 
            :rules="loginRules" 
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input
                  v-model="loginForm.username"
                  :placeholder="$t('login.adminAccount')"
                  size="large"
                  class="login-input"
                  clearable
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  :placeholder="$t('user.password')"
                  size="large"
                  class="login-input"
                  show-password
                  clearable
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="captcha">
              <div class="captcha-wrapper">
                <el-input
                  v-model="loginForm.captcha"
                  :placeholder="$t('login.enterCaptcha')"
                  size="large"
                  class="captcha-input"
                  clearable
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Key /></el-icon>
                  </template>
                </el-input>
                <div class="captcha-image" @click="refreshCaptcha">
                  <canvas ref="captchaCanvas" width="120" height="40"></canvas>
                  <div class="refresh-hint">{{ $t('login.clickRefresh') }}</div>
                </div>
              </div>
            </el-form-item>
            
            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">{{ $t('user.rememberMe') }}</el-checkbox>
              <el-button text type="primary">{{ $t('user.forgotPassword') }}</el-button>
            </div>
            
            <el-button 
              type="primary" 
              size="large" 
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? $t('login.loggingIn') : $t('user.login') }}
            </el-button>
          </el-form>
        </div>
        
        <div class="login-footer">
          <div class="system-info">
            <p>办公管理系统 v2.0</p>
            <p>© 2024 Company. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const { t: $t } = useI18n()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const captchaCanvas = ref(null)
const captchaCode = ref('')

const loginFormRef = ref()
const loginForm = reactive({
  username: 'admin',
  password: '123456',
  captcha: '',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位字符', trigger: 'blur' }
  ]
}

// 方法
const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()
    
    // 验证验证码
    if (loginForm.captcha.toLowerCase() !== captchaCode.value.toLowerCase()) {
      ElMessage.error('验证码错误')
      refreshCaptcha()
      return
    }
    
    loading.value = true
    
    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      rememberMe: loginForm.rememberMe
    }
    
    await userStore.loginAction(loginData)
    ElMessage.success('登录成功！')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查账号密码')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

const refreshCaptcha = () => {
  generateCaptcha()
  loginForm.captcha = ''
}

// 生成验证码
const generateCaptcha = () => {
  const canvas = captchaCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let code = ''
  
  // 清空画布
  ctx.clearRect(0, 0, 120, 40)
  
  // 设置背景
  ctx.fillStyle = '#f8f9fa'
  ctx.fillRect(0, 0, 120, 40)
  
  // 生成随机验证码
  for (let i = 0; i < 4; i++) {
    const char = chars.charAt(Math.floor(Math.random() * chars.length))
    code += char
    
    // 设置字体样式
    ctx.font = '18px Arial'
    ctx.fillStyle = `hsl(${Math.random() * 360}, 70%, 50%)`
    
    // 随机位置和角度
    const x = 20 + i * 20
    const y = 25
    const angle = (Math.random() - 0.5) * 0.3
    
    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(angle)
    ctx.fillText(char, 0, 0)
    ctx.restore()
  }
  
  // 添加干扰线
  for (let i = 0; i < 3; i++) {
    ctx.beginPath()
    ctx.moveTo(Math.random() * 120, Math.random() * 40)
    ctx.lineTo(Math.random() * 120, Math.random() * 40)
    ctx.strokeStyle = `hsl(${Math.random() * 360}, 50%, 70%)`
    ctx.lineWidth = 1
    ctx.stroke()
  }
  
  // 添加干扰点
  for (let i = 0; i < 20; i++) {
    ctx.fillStyle = `hsl(${Math.random() * 360}, 50%, 70%)`
    ctx.fillRect(Math.random() * 120, Math.random() * 40, 2, 2)
  }
  
  captchaCode.value = code
}

// 生命周期
onMounted(() => {
  // 如果已经登录，直接跳转到管理后台
  if (userStore.isLoggedIn) {
    router.replace('/dashboard')
  }
  
  // 生成验证码
  setTimeout(() => {
    generateCaptcha()
  }, 100)
})
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 背景效果 */
.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.geometric-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 120px;
  height: 120px;
  top: 10%;
  left: 10%;
  animation-delay: -2s;
}

.shape-2 {
  width: 80px;
  height: 80px;
  top: 20%;
  right: 15%;
  animation-delay: -4s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  bottom: 15%;
  left: 15%;
  animation-delay: -1s;
}

.shape-4 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  right: 10%;
  animation-delay: -3s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%);
}

/* 登录内容 */
.login-content {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 品牌头部 */
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

.brand-text h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-text p {
  font-size: 14px;
  color: #666;
  margin: 4px 0 0 0;
}

/* 表单标题 */
.form-title {
  text-align: center;
  margin-bottom: 32px;
}

.form-title h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.form-title p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  transition: all 0.3s ease;
  padding: 12px 16px;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__inner) {
  font-size: 16px;
  padding-left: 32px;
}

.input-icon {
  color: #999;
  margin-right: 8px;
}

/* 验证码 */
.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 2px solid #f0f0f0;
  transition: all 0.3s ease;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-image:hover {
  border-color: #667eea;
}

.captcha-image canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.refresh-hint {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(102, 126, 234, 0.9);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  opacity: 0;
  transition: all 0.3s ease;
  border-radius: 6px;
}

.captcha-image:hover .refresh-hint {
  opacity: 1;
}

/* 登录选项 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

/* 页脚 */
.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.system-info p {
  font-size: 12px;
  color: #999;
  margin: 4px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-content {
    padding: 16px;
  }
  
  .login-card {
    padding: 32px 24px;
    border-radius: 16px;
  }
  
  .brand-logo {
    flex-direction: column;
    gap: 12px;
  }
  
  .brand-text h1 {
    font-size: 24px;
  }
  
  .captcha-wrapper {
    flex-direction: column;
    gap: 12px;
  }
  
  .captcha-image {
    width: 100%;
    height: 48px;
  }
  
  .login-options {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

/* 动画效果 */
.login-card {
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>