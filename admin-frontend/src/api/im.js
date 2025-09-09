import request from '@/utils/request'

/**
 * IM相关API
 */
const imApi = {
  /**
   * 获取用户IM Token
   * @param {number} userId 用户ID
   * @returns {Promise} IM Token
   */
  getUserToken(userId) {
    return request({
      url: '/im/token',
      method: 'post',
      params: { userId }
    })
  },

  /**
   * 获取在线用户列表
   * @returns {Promise} 在线用户列表
   */
  getOnlineUsers() {
    return request({
      url: '/im/users/online',
      method: 'get'
    })
  },

  /**
   * 更新用户状态
   * @param {string} userId 用户ID
   * @param {string} status 用户状态
   * @returns {Promise} 更新结果
   */
  updateUserStatus(userId, status) {
    return request({
      url: `/im/users/${userId}/status`,
      method: 'post',
      params: { status }
    })
  },

  /**
   * 获取用户状态
   * @param {string} userId 用户ID
   * @returns {Promise} 用户状态
   */
  getUserStatus(userId) {
    return request({
      url: `/im/users/${userId}/status`,
      method: 'get'
    })
  },

  /**
   * 创建聊天室
   * @param {Object} data 聊天室信息
   * @returns {Promise} 聊天室信息
   */
  createChatRoom(data) {
    return request({
      url: '/im/chatroom',
      method: 'post',
      data
    })
  },

  /**
   * 获取聊天记录
   * @param {string} roomId 聊天室ID
   * @param {number} limit 获取数量限制
   * @returns {Promise} 聊天记录列表
   */
  getChatMessages(roomId, limit = 50) {
    return request({
      url: '/im/messages',
      method: 'get',
      params: { roomId, limit }
    })
  },

  /**
   * 发送消息
   * @param {Object} data 消息内容
   * @returns {Promise} 发送结果
   */
  sendMessage(data) {
    return request({
      url: '/im/messages',
      method: 'post',
      data
    })
  }
}

export default imApi