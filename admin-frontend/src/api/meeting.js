import request from '@/utils/request'

/**
 * 会议管理API
 */
export default {
  // 获取会议列表
  getMeetingList(params) {
    return request({
      url: '/api/meetings',
      method: 'get',
      params
    })
  },

  // 创建会议
  createMeeting(data) {
    return request({
      url: '/api/meetings',
      method: 'post',
      data
    })
  },

  // 获取会议详情
  getMeetingDetail(id) {
    return request({
      url: `/api/meetings/${id}`,
      method: 'get'
    })
  },

  // 更新会议
  updateMeeting(id, data) {
    return request({
      url: `/api/meetings/${id}`,
      method: 'put',
      data
    })
  },

  // 删除会议
  deleteMeeting(id) {
    return request({
      url: `/api/meetings/${id}`,
      method: 'delete'
    })
  },

  // 加入会议
  joinMeeting(id) {
    return request({
      url: `/api/meetings/${id}/join`,
      method: 'post'
    })
  },

  // 离开会议
  leaveMeeting(id) {
    return request({
      url: `/api/meetings/${id}/leave`,
      method: 'post'
    })
  },

  // 开始会议
  startMeeting(id) {
    return request({
      url: `/api/meetings/${id}/start`,
      method: 'post'
    })
  },

  // 结束会议
  endMeeting(id) {
    return request({
      url: `/api/meetings/${id}/end`,
      method: 'post'
    })
  },

  // 获取会议参与者
  getMeetingParticipants(id) {
    return request({
      url: `/api/meetings/${id}/participants`,
      method: 'get'
    })
  },

  // 添加参与者
  addParticipant(id, userId) {
    return request({
      url: `/api/meetings/${id}/participants`,
      method: 'post',
      data: { userId }
    })
  },

  // 移除参与者
  removeParticipant(id, userId) {
    return request({
      url: `/api/meetings/${id}/participants/${userId}`,
      method: 'delete'
    })
  },

  // 获取会议室列表
  getMeetingRooms() {
    return request({
      url: '/api/meetings/rooms',
      method: 'get'
    })
  },

  // 检查会议室可用性
  checkRoomAvailability(roomId, startTime, endTime) {
    return request({
      url: `/api/meetings/rooms/${roomId}/availability`,
      method: 'get',
      params: { startTime, endTime }
    })
  },

  // 预订会议室
  bookMeetingRoom(roomId, data) {
    return request({
      url: `/api/meetings/rooms/${roomId}/book`,
      method: 'post',
      data
    })
  },

  // 获取会议录制
  getMeetingRecording(id) {
    return request({
      url: `/api/meetings/${id}/recording`,
      method: 'get'
    })
  },

  // 开始录制
  startRecording(id) {
    return request({
      url: `/api/meetings/${id}/recording/start`,
      method: 'post'
    })
  },

  // 停止录制
  stopRecording(id) {
    return request({
      url: `/api/meetings/${id}/recording/stop`,
      method: 'post'
    })
  }
}