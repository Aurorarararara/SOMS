import request from '@/utils/request'

/**
 * 系统健康检查API
 */
export default {
  // 获取系统健康状态
  getHealthStatus() {
    return request({
      url: '/health',
      method: 'get'
    })
  },

  // 获取系统信息
  getSystemInfo() {
    return request({
      url: '/health/info',
      method: 'get'
    })
  },

  // 获取数据库连接状态
  getDatabaseStatus() {
    return request({
      url: '/health/database',
      method: 'get'
    })
  },

  // 获取Redis连接状态
  getRedisStatus() {
    return request({
      url: '/health/redis',
      method: 'get'
    })
  },

  // 获取服务器性能指标
  getServerMetrics() {
    return request({
      url: '/health/metrics',
      method: 'get'
    })
  },

  // 检查网络连接
  checkNetworkConnection() {
    return request({
      url: '/health/network',
      method: 'get'
    })
  }
}