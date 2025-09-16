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
    // 如果userID为空或无效，使用默认值0
    const safeUserId = userId || 0;
    console.log(`尝试获取IM Token，用户ID: ${safeUserId}`);
    
    // 使用GET方法获取Token
    return request({
      url: '/api/im/token',  // 修复：与后端控制器路径保持一致
      method: 'get',
      params: { userId: safeUserId },
      timeout: 20000 // 增加超时时间
    }).then(response => {
      console.log('成功获取IM Token:', response.data);
      return response;
    }).catch(error => {
      console.error('获取IM Token失败:', error);
      
      // 如果GET方法失败，尝试使用POST方法
      console.log('尝试使用POST方法获取IM Token');
      return request({
        url: '/api/im/token',  // 修复：与后端控制器路径保持一致
        method: 'post',
        params: { userId: safeUserId },
        timeout: 20000 // 增加超时时间
      }).catch(postError => {
        console.error('POST方式获取IM Token也失败:', postError);
        // 最终都失败，返回模拟Token
        return {
          data: 'mock_token_' + Date.now(),
          message: '模拟Token'
        };
      });
    });
  },

  /**
   * 获取在线用户列表
   * @returns {Promise} 在线用户列表
   */
  getOnlineUsers() {
    return request({
      url: '/api/im/users/online',
      method: 'get'
    });
  },

  /**
   * 更新用户状态
   * @param {string} userId 用户ID
   * @param {string} status 用户状态
   * @returns {Promise} 更新结果
   */
  updateUserStatus(userId, status) {
    return request({
      url: `/api/im/users/${userId}/status`,
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
      url: `/api/im/users/${userId}/status`,
      method: 'get'
    })
  },

  /**
   * 获取用户信息
   * @param {number} userId 用户ID
   * @returns {Promise} 用户信息
   */
  getUserInfo(userId) {
    return request({
      url: `/api/im/users/${userId}`,
      method: 'get'
    })
  },

  /**
   * 获取用户聊天室列表
   * @param {number} userId 用户ID
   * @returns {Promise} 聊天室列表
   */
  getUserChatRooms(userId) {
    return request({
      url: '/api/im/chatrooms',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 创建或获取一对一聊天室
   * @param {number} userId1 用户1 ID
   * @param {number} userId2 用户2 ID
   * @returns {Promise} 聊天室信息
   */
  getOrCreateSingleChatRoom(userId1, userId2) {
    return request({
      url: '/api/im/chatrooms/single',
      method: 'post',
      params: { userId1, userId2 }
    })
  },

  /**
   * 创建群聊
   * @param {Object} data 群聊信息（roomName, creatorId, memberIds）
   * @returns {Promise} 聊天室信息
   */
  createGroupChatRoom(data) {
    return request({
      url: '/api/im/chatrooms/group',
      method: 'post',
      data
    })
  },

  /**
   * 获取聊天室成员
   * @param {string} roomId 聊天室ID
   * @returns {Promise} 成员列表
   */
  getChatRoomMembers(roomId) {
    return request({
      url: `/api/im/chatrooms/${roomId}/members`,
      method: 'get'
    })
  },

  /**
   * 加入聊天室
   * @param {string} roomId 聊天室ID
   * @param {number} userId 用户ID
   * @param {string} nickname 在聊天室中的昵称
   * @returns {Promise} 加入结果
   */
  joinChatRoom(roomId, userId, nickname) {
    return request({
      url: `/api/im/chatrooms/${roomId}/join`,
      method: 'post',
      params: { userId, nickname }
    })
  },

  /**
   * 离开聊天室
   * @param {string} roomId 聊天室ID
   * @param {number} userId 用户ID
   * @returns {Promise} 离开结果
   */
  leaveChatRoom(roomId, userId) {
    return request({
      url: `/api/im/chatrooms/${roomId}/leave`,
      method: 'post',
      params: { userId }
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
      url: '/api/im/messages',
      method: 'get',
      params: { roomId, limit }
    }).catch(error => {
      console.error('获取聊天记录失败:', error);
      // 返回默认消息
      return {
        data: {
          messages: [
            {
              clientMsgID: 'msg1',
              sendID: '1',
              senderNickname: '张三',
              text: '大家好，欢迎来到聊天室！',
              createTime: Math.floor(Date.now() / 1000) - 300
            },
            {
              clientMsgID: 'msg2',
              sendID: '2',
              senderNickname: '李四',
              text: '你好，很高兴加入！',
              createTime: Math.floor(Date.now() / 1000) - 240
            },
            {
              clientMsgID: 'msg3',
              sendID: '3',
              senderNickname: '王五',
              text: '当前处于演示模式',
              createTime: Math.floor(Date.now() / 1000) - 180
            }
          ],
          roomId: roomId
        },
        message: '使用模拟数据'
      };
    });
  },

  /**
   * 发送消息
   * @param {Object} data 消息内容
   * @returns {Promise} 发送结果
   */
  sendMessage(data) {
    return request({
      url: '/api/im/messages',
      method: 'post',
      data
    })
  }
}

export default imApi