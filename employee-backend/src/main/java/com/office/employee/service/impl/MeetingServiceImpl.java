package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.office.employee.common.BusinessException;
import com.office.employee.dto.MeetingCreateRequest;
import com.office.employee.entity.Meeting;
import com.office.employee.entity.MeetingMessage;
import com.office.employee.entity.MeetingParticipant;
import com.office.employee.mapper.MeetingMapper;
import com.office.employee.mapper.MeetingMessageMapper;
import com.office.employee.mapper.MeetingParticipantMapper;
import com.office.employee.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 会议服务实现类
 */
@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private MeetingMapper meetingMapper;

    @Autowired
    private MeetingParticipantMapper participantMapper;

    @Autowired
    private MeetingMessageMapper messageMapper;

    @Override
    @Transactional
    public Meeting createMeeting(MeetingCreateRequest request, Long hostId) {
        Meeting meeting = new Meeting();
        meeting.setMeetingTitle(request.getMeetingTitle());
        meeting.setMeetingCode(generateMeetingCode());
        meeting.setHostId(hostId);
        meeting.setMeetingType(request.getMeetingType());
        meeting.setStartTime(request.getStartTime());
        meeting.setEndTime(request.getEndTime());
        meeting.setMeetingPassword(request.getMeetingPassword());
        meeting.setMaxParticipants(request.getMaxParticipants());
        meeting.setIsRecording(request.getIsRecording());
        meeting.setAgenda(request.getAgenda());
        meeting.setMeetingStatus("scheduled");
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());

        meetingMapper.insert(meeting);

        // 添加主持人为参与者
        MeetingParticipant hostParticipant = new MeetingParticipant();
        hostParticipant.setMeetingId(meeting.getId());
        hostParticipant.setUserId(hostId);
        hostParticipant.setRole("host");
        hostParticipant.setIsMuted(false);
        hostParticipant.setIsVideoOn(true);
        hostParticipant.setConnectionStatus("disconnected");
        hostParticipant.setCreatedAt(LocalDateTime.now());
        hostParticipant.setUpdatedAt(LocalDateTime.now());
        participantMapper.insert(hostParticipant);

        // 添加邀请的参与者
        if (request.getInviteeIds() != null && request.getInviteeIds().length > 0) {
            for (Long inviteeId : request.getInviteeIds()) {
                MeetingParticipant participant = new MeetingParticipant();
                participant.setMeetingId(meeting.getId());
                participant.setUserId(inviteeId);
                participant.setRole("participant");
                participant.setIsMuted(false);
                participant.setIsVideoOn(true);
                participant.setConnectionStatus("disconnected");
                participant.setCreatedAt(LocalDateTime.now());
                participant.setUpdatedAt(LocalDateTime.now());
                participantMapper.insert(participant);
            }
        }

        return meeting;
    }

    @Override
    public MeetingParticipant joinMeeting(Long meetingId, Long userId) {
        // 检查会议是否存在
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 检查用户是否已经在会议中
        int count = participantMapper.checkUserInMeeting(meetingId, userId);
        if (count > 0) {
            // 更新连接状态
            MeetingParticipant participant = participantMapper.selectOne(
                new QueryWrapper<MeetingParticipant>()
                    .eq("meeting_id", meetingId)
                    .eq("user_id", userId)
            );
            participant.setJoinTime(LocalDateTime.now());
            participant.setConnectionStatus("connected");
            participant.setUpdatedAt(LocalDateTime.now());
            participantMapper.updateById(participant);
            return participant;
        }

        // 添加新参与者
        MeetingParticipant participant = new MeetingParticipant();
        participant.setMeetingId(meetingId);
        participant.setUserId(userId);
        participant.setRole("participant");
        participant.setJoinTime(LocalDateTime.now());
        participant.setIsMuted(false);
        participant.setIsVideoOn(true);
        participant.setConnectionStatus("connected");
        participant.setCreatedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());
        participantMapper.insert(participant);

        return participant;
    }

    @Override
    public MeetingParticipant joinMeetingByCode(String meetingCode, String password, Long userId) {
        Meeting meeting = meetingMapper.findByMeetingCode(meetingCode);
        if (meeting == null) {
            throw new BusinessException("会议代码无效");
        }

        if (StringUtils.hasText(meeting.getMeetingPassword()) && 
            !meeting.getMeetingPassword().equals(password)) {
            throw new BusinessException("会议密码错误");
        }

        return joinMeeting(meeting.getId(), userId);
    }

    @Override
    public void leaveMeeting(Long meetingId, Long userId) {
        participantMapper.setLeaveTime(meetingId, userId, LocalDateTime.now());
    }

    @Override
    public List<Meeting> getUserMeetings(Long userId) {
        return meetingMapper.getUserMeetings(userId);
    }

    @Override
    public List<Meeting> getUpcomingMeetings(Long userId) {
        return meetingMapper.getUpcomingMeetings(userId, LocalDateTime.now());
    }

    @Override
    public List<Meeting> getTodayMeetings(Long userId) {
        return meetingMapper.getTodayMeetings(userId, LocalDateTime.now());
    }

    @Override
    public Meeting getMeetingDetail(Long meetingId, Long userId) {
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 检查用户是否有权限查看
        int count = participantMapper.checkUserInMeeting(meetingId, userId);
        if (count == 0 && !meeting.getHostId().equals(userId)) {
            throw new BusinessException("无权限查看此会议");
        }

        return meeting;
    }

    @Override
    public List<MeetingParticipant> getMeetingParticipants(Long meetingId) {
        return participantMapper.getMeetingParticipants(meetingId);
    }

    @Override
    public MeetingMessage sendMeetingMessage(Long meetingId, Long senderId, String content, String messageType) {
        MeetingMessage message = new MeetingMessage();
        message.setMeetingId(meetingId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setIsPrivate(false);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        return message;
    }

    @Override
    public Map<String, Object> getMeetingMessages(Long meetingId, Long userId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        
        int offset = (page - 1) * size;
        List<MeetingMessage> messages = messageMapper.getMeetingMessages(meetingId, userId, offset, size);
        int total = messageMapper.getMeetingMessageCount(meetingId, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public void updateParticipantStatus(Long meetingId, Long userId, Boolean isMuted, Boolean isVideoOn, String connectionStatus) {
        participantMapper.updateParticipantStatus(meetingId, userId, isMuted, isVideoOn, connectionStatus, LocalDateTime.now());
    }

    @Override
    public Map<String, Object> checkMeetingStatus(Long meetingId) {
        Meeting meeting = meetingMapper.selectById(meetingId);
        Map<String, Object> result = new HashMap<>();
        if (meeting != null) {
            result.put("status", meeting.getMeetingStatus());
            result.put("participantCount", participantMapper.selectCount(
                new QueryWrapper<MeetingParticipant>()
                    .eq("meeting_id", meetingId)
                    .eq("connection_status", "connected")
            ));
        } else {
            result.put("status", "not_found");
        }
        return result;
    }

    @Override
    public void saveWhiteboardData(Long meetingId, String canvasData) {
        // 这里可以实现白板数据保存逻辑
        // 暂时返回空实现
    }

    @Override
    public String getWhiteboardData(Long meetingId) {
        // 这里可以实现白板数据获取逻辑
        // 暂时返回空数据
        return "{}";
    }

    @Override
    public String getMeetingRecording(Long meetingId) {
        Meeting meeting = meetingMapper.selectById(meetingId);
        return meeting != null ? meeting.getRecordingUrl() : null;
    }

    @Override
    public boolean checkUserInMeeting(Long meetingId, Long userId) {
        int count = participantMapper.checkUserInMeeting(meetingId, userId);
        return count > 0;
    }

    @Override
    public void endMeeting(Long meetingId, Long userId) {
        // 检查用户是否有权限结束会议（通常是会议创建者）
        Meeting meeting = meetingMapper.selectById(meetingId);
        if (meeting != null && meeting.getHostId().equals(userId)) {
            // 更新会议状态为已结束
            meeting.setMeetingStatus("ended");
            meeting.setUpdatedAt(LocalDateTime.now());
            meetingMapper.updateById(meeting);
            // 设置所有参与者的离开时间
            participantMapper.setAllParticipantsLeaveTime(meetingId, LocalDateTime.now());
        }
    }

    /**
     * 生成会议代码
     */
    private String generateMeetingCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        int randomNum = random.nextInt(9000) + 1000; // 生成4位随机数
        return timestamp + randomNum;
    }
}