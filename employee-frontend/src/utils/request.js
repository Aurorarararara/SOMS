import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',  // 设置基础URL为/api
  timeout: 15000  // 增加超时时间到15秒
})

// 重试配置
const MAX_RETRIES = 2;
const RETRY_DELAY = 1000; // 1秒

// 全局异常管理
let hasConnectionError = false;
let connectionErrorTimer = null;

// 显示服务连接错误提示
const showConnectionError = (message) => {
  if (!hasConnectionError) {
    hasConnectionError = true;
    ElNotification({
      title: '连接错误',
      message: message || '无法连接到服务器，请确认后端服务已启动',
      type: 'error',
      duration: 10000,
    });
    
    // 30秒内不重复显示错误提示
    clearTimeout(connectionErrorTimer);
    connectionErrorTimer = setTimeout(() => {
      hasConnectionError = false;
    }, 30000);
  }
};

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) {
      return { data, message }
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  error => {
    // 检查是否是连接错误
    if (error.code === 'ECONNABORTED' || error.message?.includes('Network Error') || error.message?.includes('socket hang up')) {
      showConnectionError('网络连接失败，请确认后端服务已启动')
      
      // 添加重试机制
      const config = error.config;
      // 设置重试计数器
      config.__retryCount = config.__retryCount || 0;
      
      if (config.__retryCount < MAX_RETRIES) {
        // 增加重试计数
        config.__retryCount += 1;
        
        // 创建新的Promise来处理重试
        return new Promise((resolve) => {
          setTimeout(() => {
            console.log(`重试请求 ${config.url}`, config.__retryCount);
            resolve(request(config));
          }, RETRY_DELAY * config.__retryCount);
        });
      }
    }
    
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logoutAction()
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response?.status === 403) {
      // 处理403错误，可能是权限问题
      ElMessage.error('访问被拒绝，权限不足')
    } else if (error.response?.status === 500) {
      ElMessage.error('服务器内部错误，请稍后再试')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request