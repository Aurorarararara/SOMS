package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Meeting;
import com.office.admin.entity.MeetingRoom;
import com.office.admin.entity.MeetingParticipant;
import com.office.admin.mapper.MeetingMapper;
import com.office.admin.mapper.MeetingRoomMapper;
import com.office.admin.mapper.MeetingParticipantMapper;
import com.office.admin.service.MeetingService;
import com.office.admin.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingMapper meetingMapper;
    private final MeetingRoomMapper meetingRoomMapper;
    private final MeetingParticipantMapper meetingParticipantMapper;

    @Override
    @Transactional
    public Meeting createMeeting(Meeting meeting) {
        // 生成会议代码
        meeting.setMeetingCode(generateMeetingCode());
        
        // 设置创建时间
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        
        // 如果是即时会议，设置开始时间
        if ("instant".equals(meeting.getMeetingType())) {
            meeting.setStartTime(LocalDateTime.now());
            meeting.setMeetingStatus("ongoing");
        } else {
            meeting.setMeetingStatus("scheduled");
        }
        
        meetingMapper.insert(meeting);
        
        // 添加主持人为参与者
        MeetingParticipant hostParticipant = new MeetingParticipant();
        hostParticipant.setMeetingId(meeting.getId());
        hostParticipant.setUserId(meeting.getHostId());
        hostParticipant.setRole("host");
        hostParticipant.setConnectionStatus("disconnected");
        meetingParticipantMapper.insert(hostParticipant);
        
        return meeting;
    }

    @Override
    public List<Meeting> getMeetings(Integer page, Integer size, String status, Long hostId) {
        Page<Meeting> pageObj = new Page<>(page, size);
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("meeting_status", status);
        }
        
        if (hostId != null) {
            queryWrapper.eq("host_id", hostId);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<Meeting> result = meetingMapper.selectPage(pageObj, queryWrapper);
        return result.getRecords();
    }

    @Override
    public Meeting getMeetingById(Long id) {
        Meeting meeting = meetingMapper.selectById(id);
        if (meeting != null) {
            // 获取参与者信息
            QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
            wrapper.eq("meeting_id", id);
            List<MeetingParticipant> participants = meetingParticipantMapper.selectList(wrapper);
            meeting.setParticipants(participants);
            meeting.setParticipantCount(participants.size());
        }
        return meeting;
    }

    @Override
    public Meeting getMeetingByCode(String meetingCode) {
        QueryWrapper<Meeting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("meeting_code", meetingCode);
        return meetingMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public Meeting updateMeeting(Meeting meeting) {
        meeting.setUpdatedAt(LocalDateTime.now());
        meetingMapper.updateById(meeting);
        return meeting;
    }

    @Override
    @Transactional
    public void deleteMeeting(Long id) {
        // 删除会议参与者
        QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("meeting_id", id);
        meetingParticipantMapper.delete(wrapper);
        
        // 删除会议
        meetingMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Map<String, Object> joinMeeting(Long meetingId, String password) {
        Meeting meeting = getMeetingById(meetingId);
        if (meeting == null) {
            throw new RuntimeException("会议不存在");
        }
        
        // 检查会议状态
        if ("ended".equals(meeting.getMeetingStatus()) || "cancelled".equals(meeting.getMeetingStatus())) {
            throw new RuntimeException("会议已结束或已取消");
        }
        
        // 检查密码
        if (meeting.getMeetingPassword() != null && !meeting.getMeetingPassword().equals(password)) {
            throw new RuntimeException("会议密码错误");
        }
        
        // 检查参与者数量
        if (meeting.getParticipantCount() >= meeting.getMaxParticipants()) {
            throw new RuntimeException("会议人数已满");
        }
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 检查是否已经是参与者
        QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("meeting_id", meetingId).eq("user_id", currentUserId);
        MeetingParticipant existingParticipant = meetingParticipantMapper.selectOne(wrapper);
        
        if (existingParticipant == null) {
            // 添加为参与者
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeetingId(meetingId);
            participant.setUserId(currentUserId);
            participant.setRole("participant");
            participant.setJoinTime(LocalDateTime.now());
            participant.setConnectionStatus("connected");
            meetingParticipantMapper.insert(participant);
        } else {
            // 更新连接状态
            existingParticipant.setJoinTime(LocalDateTime.now());
            existingParticipant.setConnectionStatus("connected");
            meetingParticipantMapper.updateById(existingParticipant);
        }
        
        // 如果是预定会议且还未开始，则开始会议
        if ("scheduled".equals(meeting.getMeetingStatus())) {
            meeting.setMeetingStatus("ongoing");
            meeting.setActualStartTime(LocalDateTime.now());
            updateMeeting(meeting);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("meeting", meeting);
        result.put("canJoin", true);
        return result;
    }

    @Override
    @Transactional
    public void leaveMeeting(Long meetingId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("meeting_id", meetingId).eq("user_id", currentUserId);
        MeetingParticipant participant = meetingParticipantMapper.selectOne(wrapper);
        
        if (participant != null) {
            participant.setLeaveTime(LocalDateTime.now());
            participant.setConnectionStatus("disconnected");
            meetingParticipantMapper.updateById(participant);
        }
    }

    @Override
    @Transactional
    public void startMeeting(Long meetingId) {
        Meeting meeting = getMeetingById(meetingId);
        if (meeting != null && "scheduled".equals(meeting.getMeetingStatus())) {
            meeting.setMeetingStatus("ongoing");
            meeting.setActualStartTime(LocalDateTime.now());
            updateMeeting(meeting);
        }
    }

    @Override
    @Transactional
    public void endMeeting(Long meetingId) {
        Meeting meeting = getMeetingById(meetingId);
        if (meeting != null && "ongoing".equals(meeting.getMeetingStatus())) {
            meeting.setMeetingStatus("ended");
            meeting.setActualEndTime(LocalDateTime.now());
            updateMeeting(meeting);
            
            // 更新所有参与者状态
            QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
            wrapper.eq("meeting_id", meetingId).eq("connection_status", "connected");
            List<MeetingParticipant> participants = meetingParticipantMapper.selectList(wrapper);
            
            for (MeetingParticipant participant : participants) {
                participant.setLeaveTime(LocalDateTime.now());
                participant.setConnectionStatus("disconnected");
                meetingParticipantMapper.updateById(participant);
            }
        }
    }

    @Override
    @Transactional
    public void startRecording(Long meetingId) {
        Meeting meeting = getMeetingById(meetingId);
        if (meeting != null) {
            meeting.setIsRecording(true);
            updateMeeting(meeting);
        }
    }

    @Override
    @Transactional
    public void stopRecording(Long meetingId) {
        Meeting meeting = getMeetingById(meetingId);
        if (meeting != null) {
            meeting.setIsRecording(false);
            updateMeeting(meeting);
        }
    }

    @Override
    @Transactional
    public void inviteParticipants(Long meetingId, List<Long> userIds) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        for (Long userId : userIds) {
            // 检查是否已经是参与者
            QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
            wrapper.eq("meeting_id", meetingId).eq("user_id", userId);
            MeetingParticipant existingParticipant = meetingParticipantMapper.selectOne(wrapper);
            
            if (existingParticipant == null) {
                MeetingParticipant participant = new MeetingParticipant();
                participant.setMeetingId(meetingId);
                participant.setUserId(userId);
                participant.setRole("participant");
                participant.setConnectionStatus("disconnected");
                meetingParticipantMapper.insert(participant);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getMeetingParticipants(Long meetingId) {
        return meetingParticipantMapper.selectMeetingParticipants(meetingId);
    }

    @Override
    @Transactional
    public void muteParticipant(Long meetingId, Long userId, Boolean muted) {
        QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("meeting_id", meetingId).eq("user_id", userId);
        MeetingParticipant participant = meetingParticipantMapper.selectOne(wrapper);
        
        if (participant != null) {
            participant.setIsMuted(muted);
            meetingParticipantMapper.updateById(participant);
        }
    }

    @Override
    public List<MeetingRoom> getMeetingRooms() {
        QueryWrapper<MeetingRoom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        queryWrapper.orderByDesc("created_at");
        return meetingRoomMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public MeetingRoom createMeetingRoom(MeetingRoom room) {
        room.setRoomCode(generateRoomCode());
        room.setCreatedBy(SecurityUtils.getCurrentUserId());
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        meetingRoomMapper.insert(room);
        return room;
    }

    @Override
    public String generateMeetingLink(Long meetingId) {
        Meeting meeting = getMeetingById(meetingId);
        if (meeting != null) {
            return String.format("/meeting/join/%s", meeting.getMeetingCode());
        }
        return null;
    }

    @Override
    public boolean canJoinMeeting(Long meetingId, Long userId) {
        Meeting meeting = getMeetingById(meetingId);
        if (meeting == null) {
            return false;
        }
        
        // 检查会议状态
        if ("ended".equals(meeting.getMeetingStatus()) || "cancelled".equals(meeting.getMeetingStatus())) {
            return false;
        }
        
        // 检查参与者数量
        if (meeting.getParticipantCount() >= meeting.getMaxParticipants()) {
            // 检查是否已经是参与者
            QueryWrapper<MeetingParticipant> wrapper = new QueryWrapper<>();
            wrapper.eq("meeting_id", meetingId).eq("user_id", userId);
            return meetingParticipantMapper.selectOne(wrapper) != null;
        }
        
        return true;
    }

    @Override
    public void saveChatMessage(Long meetingId, Long userId, Map<String, Object> messageData) {
        // 这里可以保存聊天消息到数据库
        // 暂时只记录日志
        log.info("Chat message saved: meetingId={}, userId={}, message={}", 
                meetingId, userId, messageData.get("message"));
    }

    @Override
    public void saveWhiteboardData(Long meetingId, Map<String, Object> whiteboardData) {
        // 这里可以保存白板数据到数据库
        // 暂时只记录日志
        log.info("Whiteboard data saved: meetingId={}, data={}", meetingId, whiteboardData);
    }

    /**
     * 生成会议代码
     */
    private String generateMeetingCode() {
        return String.valueOf(System.currentTimeMillis() % 1000000000L);
    }

    /**
     * 生成会议室代码
     */
    private String generateRoomCode() {
        return "ROOM" + System.currentTimeMillis() % 1000000L;
    }
}