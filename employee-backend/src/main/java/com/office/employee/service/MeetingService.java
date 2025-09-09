package com.office.employee.service;

import com.office.employee.dto.MeetingCreateRequest;
import com.office.employee.entity.Meeting;
import com.office.employee.entity.MeetingMessage;
import com.office.employee.entity.MeetingParticipant;

import java.util.List;
import java.util.Map;

/**
 * 会议服务接口
 */
public interface MeetingService {

    /**
     * 创建会议
     */
    Meeting createMeeting(MeetingCreateRequest request, Long hostId);

    /**
     * 加入会议
     */
    MeetingParticipant joinMeeting(Long meetingId, Long userId);

    /**
     * 通过会议代码加入会议
     */
    MeetingParticipant joinMeetingByCode(String meetingCode, String password, Long userId);

    /**
     * 离开会议
     */
    void leaveMeeting(Long meetingId, Long userId);

    /**
     * 获取用户的会议列表
     */
    List<Meeting> getUserMeetings(Long userId);

    /**
     * 获取即将开始的会议
     */
    List<Meeting> getUpcomingMeetings(Long userId);

    /**
     * 获取今天的会议
     */
    List<Meeting> getTodayMeetings(Long userId);

    /**
     * 获取会议详情
     */
    Meeting getMeetingDetail(Long meetingId, Long userId);

    /**
     * 获取会议参与者
     */
    List<MeetingParticipant> getMeetingParticipants(Long meetingId);

    /**
     * 发送会议消息
     */
    MeetingMessage sendMeetingMessage(Long meetingId, Long senderId, String content, String messageType);

    /**
     * 获取会议消息
     */
    Map<String, Object> getMeetingMessages(Long meetingId, Long userId, Integer page, Integer size);

    /**
     * 检查用户是否在会议中
     */
    boolean checkUserInMeeting(Long meetingId, Long userId);

    /**
     * 结束会议
     */
    void endMeeting(Long meetingId, Long userId);

    /**
     * 更新参与者状态
     */
    void updateParticipantStatus(Long meetingId, Long userId, Boolean isMuted, Boolean isVideoOn, String connectionStatus);

    /**
     * 检查会议状态
     */
    Map<String, Object> checkMeetingStatus(Long meetingId);

    /**
     * 保存白板数据
     */
    void saveWhiteboardData(Long meetingId, String canvasData);

    /**
     * 获取白板数据
     */
    String getWhiteboardData(Long meetingId);

    /**
     * 获取会议录制
     */
    String getMeetingRecording(Long meetingId);
}