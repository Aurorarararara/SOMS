import request from '@/utils/request'

/**
 * 聊天管理API
 */
export default {
  // 获取聊天列表
  getChatList(params) {
    return request({
      url: '/chat',
      method: 'get',
      params
    })
  },

  // 创建聊天群组
  createChatGroup(data) {
    return request({
      url: '/chat/groups',
      method: 'post',
      data
    })
  },

  // 获取聊天群组详情
  getChatGroupDetail(id) {
    return request({
      url: `/chat/groups/${id}`,
      method: 'get'
    })
  },

  // 更新聊天群组
  updateChatGroup(id, data) {
    return request({
      url: `/chat/groups/${id}`,
      method: 'put',
      data
    })
  },

  // 删除聊天群组
  deleteChatGroup(id) {
    return request({
      url: `/chat/groups/${id}`,
      method: 'delete'
    })
  },

  // 发送消息
  sendMessage(data) {
    return request({
      url: '/chat/messages',
      method: 'post',
      data
    })
  },

  // 获取聊天消息
  getChatMessages(chatId, params) {
    return request({
      url: `/chat/${chatId}/messages`,
      method: 'get',
      params
    })
  },

  // 删除消息
  deleteMessage(messageId) {
    return request({
      url: `/chat/messages/${messageId}`,
      method: 'delete'
    })
  },

  // 撤回消息
  recallMessage(messageId) {
    return request({
      url: `/chat/messages/${messageId}/recall`,
      method: 'put'
    })
  },

  // 添加群组成员
  addGroupMember(groupId, userIds) {
    return request({
      url: `/chat/groups/${groupId}/members`,
      method: 'post',
      data: { userIds }
    })
  },

  // 移除群组成员
  removeGroupMember(groupId, userId) {
    return request({
      url: `/chat/groups/${groupId}/members/${userId}`,
      method: 'delete'
    })
  },

  // 获取群组成员
  getGroupMembers(groupId) {
    return request({
      url: `/chat/groups/${groupId}/members`,
      method: 'get'
    })
  },

  // 设置群组管理员
  setGroupAdmin(groupId, userId, isAdmin) {
    return request({
      url: `/chat/groups/${groupId}/admin`,
      method: 'put',
      data: { userId, isAdmin }
    })
  },

  // 禁言用户
  muteUser(groupId, userId, duration) {
    return request({
      url: `/chat/groups/${groupId}/mute`,
      method: 'put',
      data: { userId, duration }
    })
  },

  // 取消禁言
  unmuteUser(groupId, userId) {
    return request({
      url: `/chat/groups/${groupId}/unmute`,
      method: 'put',
      data: { userId }
    })
  },

  // 获取聊天统计
  getChatStatistics() {
    return request({
      url: '/chat/statistics',
      method: 'get'
    })
  },

  // 搜索聊天记录
  searchMessages(keyword, params) {
    return request({
      url: '/chat/messages/search',
      method: 'get',
      params: { keyword, ...params }
    })
  },

  // 上传聊天文件
  uploadChatFile(formData) {
    return request({
      url: '/chat/files/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 下载聊天文件
  downloadChatFile(fileId) {
    return request({
      url: `/chat/files/${fileId}/download`,
      method: 'get',
      responseType: 'blob'
    })
  }
}