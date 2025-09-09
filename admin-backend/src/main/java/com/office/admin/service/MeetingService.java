package com.office.admin.service;

import com.office.admin.entity.Meeting;
import com.office.admin.entity.MeetingRoom;
import com.office.admin.entity.MeetingParticipant;

import java.util.List;
import java.util.Map;

public interface MeetingService {
    
    /**
     * 创建会议
     */
    Meeting createMeeting(Meeting meeting);
    
    /**
     * 获取会议列表
     */
    List<Meeting> getMeetings(Integer page, Integer size, String status, Long hostId);
    
    /**
     * 根据ID获取会议
     */
    Meeting getMeetingById(Long id);
    
    /**
     * 根据会议代码获取会议
     */
    Meeting getMeetingByCode(String meetingCode);
    
    /**
     * 更新会议
     */
    Meeting updateMeeting(Meeting meeting);
    
    /**
     * 删除会议
     */
    void deleteMeeting(Long id);
    
    /**
     * 加入会议
     */
    Map<String, Object> joinMeeting(Long meetingId, String password);
    
    /**
     * 离开会议
     */
    void leaveMeeting(Long meetingId);
    
    /**
     * 开始会议
     */
    void startMeeting(Long meetingId);
    
    /**
     * 结束会议
     */
    void endMeeting(Long meetingId);
    
    /**
     * 开始录制
     */
    void startRecording(Long meetingId);
    
    /**
     * 停止录制
     */
    void stopRecording(Long meetingId);
    
    /**
     * 邀请参与者
     */
    void inviteParticipants(Long meetingId, List<Long> userIds);
    
    /**
     * 获取会议参与者
     */
    List<Map<String, Object>> getMeetingParticipants(Long meetingId);
    
    /**
     * 静音/取消静音参与者
     */
    void muteParticipant(Long meetingId, Long userId, Boolean muted);
    
    /**
     * 获取会议室列表
     */
    List<MeetingRoom> getMeetingRooms();
    
    /**
     * 创建会议室
     */
    MeetingRoom createMeetingRoom(MeetingRoom room);
    
    /**
     * 生成会议链接
     */
    String generateMeetingLink(Long meetingId);
    
    /**
     * 检查是否可以加入会议
     */
    boolean canJoinMeeting(Long meetingId, Long userId);
    
    /**
     * 保存聊天消息
     */
    void saveChatMessage(Long meetingId, Long userId, Map<String, Object> messageData);
    
    /**
     * 保存白板数据
     */
    void saveWhiteboardData(Long meetingId, Map<String, Object> whiteboardData);
}