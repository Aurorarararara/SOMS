import request from '@/utils/request'
import { ElMessage, ElNotification } from 'element-plus'

/**
 * 服务健康检查工具
 * 用于检测后端服务是否正常运行
 */
class HealthChecker {
  constructor() {
    this.isChecking = false;
    this.backendAvailable = null;
    this.lastCheckTime = 0;
    this.checkInterval = 30000; // 30秒
  }

  /**
   * 检查后端服务健康状态
   * @returns {Promise<boolean>} 服务是否可用
   */
  async checkBackendHealth() {
    // 避免频繁检查
    const now = Date.now();
    if (this.isChecking || (now - this.lastCheckTime < this.checkInterval && this.backendAvailable !== null)) {
      return this.backendAvailable;
    }

    this.isChecking = true;
    try {
      console.log('正在检查后端服务健康状态...');
      
      // 尝试请求健康检查接口，超时时间设置较短
      await request({
        url: '/api/health', // 健康检查接口路径
        method: 'get',
        timeout: 5000
      });
      
      // 请求成功，后端可用
      if (this.backendAvailable === false) {
        ElNotification({
          title: '连接恢复',
          message: '后端服务连接已恢复',
          type: 'success',
          duration: 5000
        });
      }
      
      this.backendAvailable = true;
      console.log('后端服务健康检查通过');
      return true;
    } catch (error) {
      // 请求失败，后端不可用
      if (this.backendAvailable !== false) {
        ElNotification({
          title: '连接错误',
          message: '无法连接到后端服务，请确认服务已启动',
          type: 'error',
          duration: 10000
        });
      }
      
      this.backendAvailable = false;
      console.error('后端服务健康检查失败:', error);
      return false;
    } finally {
      this.isChecking = false;
      this.lastCheckTime = Date.now();
    }
  }

  /**
   * 开始定期健康检查
   */
  startPeriodicCheck() {
    // 初始检查
    this.checkBackendHealth();
    
    // 设置定期检查
    setInterval(() => {
      this.checkBackendHealth();
    }, this.checkInterval);
  }
}

// 导出单例
export const healthChecker = new HealthChecker();

export default healthChecker;