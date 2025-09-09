import request from '@/utils/request'

/**
 * 会议管理API - 员工端
 */
export default {
  // 获取我的会议列表
  getMyMeetings(params) {
    return request({
      url: '/meetings/my',
      method: 'get',
      params
    })
  },

  // 获取会议详情
  getMeetingDetail(id) {
    return request({
      url: `/meetings/${id}`,
      method: 'get'
    })
  },

  // 创建会议
  createMeeting(data) {
    return request({
      url: '/meetings',
      method: 'post',
      data
    })
  },

  // 加入会议
  joinMeeting(meetingId) {
    return request({
      url: `/meetings/${meetingId}/join`,
      method: 'post'
    })
  },

  // 离开会议
  leaveMeeting(meetingId) {
    return request({
      url: `/meetings/${meetingId}/leave`,
      method: 'post'
    })
  },

  // 通过会议代码加入会议
  joinMeetingByCode(meetingCode, password = '') {
    return request({
      url: '/meetings/join-by-code',
      method: 'post',
      data: {
        meetingCode,
        password
      }
    })
  },

  // 获取会议参与者
  getMeetingParticipants(meetingId) {
    return request({
      url: `/meetings/${meetingId}/participants`,
      method: 'get'
    })
  },

  // 发送会议聊天消息
  sendMeetingMessage(meetingId, message) {
    return request({
      url: `/meetings/${meetingId}/messages`,
      method: 'post',
      data: { message }
    })
  },

  // 获取会议聊天消息
  getMeetingMessages(meetingId, params) {
    return request({
      url: `/meetings/${meetingId}/messages`,
      method: 'get',
      params
    })
  },

  // 更新参与者状态（静音、视频等）
  updateParticipantStatus(meetingId, status) {
    return request({
      url: `/meetings/${meetingId}/participant-status`,
      method: 'put',
      data: status
    })
  },

  // 获取会议录制
  getMeetingRecording(meetingId) {
    return request({
      url: `/meetings/${meetingId}/recording`,
      method: 'get'
    })
  },

  // 保存白板数据
  saveWhiteboardData(meetingId, data) {
    return request({
      url: `/meetings/${meetingId}/whiteboard`,
      method: 'post',
      data
    })
  },

  // 获取白板数据
  getWhiteboardData(meetingId) {
    return request({
      url: `/meetings/${meetingId}/whiteboard`,
      method: 'get'
    })
  },

  // 检查会议状态
  checkMeetingStatus(meetingId) {
    return request({
      url: `/meetings/${meetingId}/status`,
      method: 'get'
    })
  },

  // 获取即将开始的会议
  getUpcomingMeetings() {
    return request({
      url: '/meetings/upcoming',
      method: 'get'
    })
  },

  // 获取今日会议
  getTodayMeetings() {
    return request({
      url: '/meetings/today',
      method: 'get'
    })
  }
}

// 会议WebSocket相关
export const meetingWebSocket = {
  // WebSocket连接地址
  getWebSocketUrl() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    return `${protocol}//${host}/ws/meeting`
  },

  // 创建WebSocket连接
  createConnection(meetingId, userId) {
    const url = this.getWebSocketUrl()
    const ws = new WebSocket(`${url}?meetingId=${meetingId}&userId=${userId}`)
    return ws
  }
}