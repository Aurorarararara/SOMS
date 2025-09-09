package com.office.employee.controller;

import com.office.employee.common.Result;
import com.office.employee.dto.MeetingCreateRequest;
import com.office.employee.entity.Meeting;
import com.office.employee.entity.MeetingMessage;
import com.office.employee.entity.MeetingParticipant;
import com.office.employee.service.MeetingService;
import com.office.employee.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 会议控制器
 */
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建会议
     */
    @PostMapping
    public Result<Meeting> createMeeting(@Valid @RequestBody MeetingCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromToken(httpRequest);
        Meeting meeting = meetingService.createMeeting(request, userId);
        return Result.success(meeting);
    }

    /**
     * 加入会议
     */
    @PostMapping("/{meetingId}/join")
    public Result<MeetingParticipant> joinMeeting(@PathVariable Long meetingId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        MeetingParticipant participant = meetingService.joinMeeting(meetingId, userId);
        return Result.success(participant);
    }

    /**
     * 通过会议代码加入会议
     */
    @PostMapping("/join-by-code")
    public Result<MeetingParticipant> joinMeetingByCode(
            @RequestParam String meetingCode,
            @RequestParam(required = false) String password,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        MeetingParticipant participant = meetingService.joinMeetingByCode(meetingCode, password, userId);
        return Result.success(participant);
    }

    /**
     * 离开会议
     */
    @PostMapping("/{meetingId}/leave")
    public Result<Void> leaveMeeting(@PathVariable Long meetingId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        meetingService.leaveMeeting(meetingId, userId);
        return Result.success();
    }

    /**
     * 获取用户的会议列表
     */
    @GetMapping
    public Result<List<Meeting>> getUserMeetings(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        List<Meeting> meetings = meetingService.getUserMeetings(userId);
        return Result.success(meetings);
    }

    /**
     * 获取即将开始的会议
     */
    @GetMapping("/upcoming")
    public Result<List<Meeting>> getUpcomingMeetings(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        List<Meeting> meetings = meetingService.getUpcomingMeetings(userId);
        return Result.success(meetings);
    }

    /**
     * 获取今天的会议
     */
    @GetMapping("/today")
    public Result<List<Meeting>> getTodayMeetings(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        List<Meeting> meetings = meetingService.getTodayMeetings(userId);
        return Result.success(meetings);
    }

    /**
     * 获取会议详情
     */
    @GetMapping("/{meetingId}")
    public Result<Meeting> getMeetingDetail(@PathVariable Long meetingId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        Meeting meeting = meetingService.getMeetingDetail(meetingId, userId);
        return Result.success(meeting);
    }

    /**
     * 获取会议参与者
     */
    @GetMapping("/{meetingId}/participants")
    public Result<List<MeetingParticipant>> getMeetingParticipants(@PathVariable Long meetingId) {
        List<MeetingParticipant> participants = meetingService.getMeetingParticipants(meetingId);
        return Result.success(participants);
    }

    /**
     * 检查用户是否在会议中
     */
    @GetMapping("/{meetingId}/check-user")
    public Result<Boolean> checkUserInMeeting(@PathVariable Long meetingId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        boolean inMeeting = meetingService.checkUserInMeeting(meetingId, userId);
        return Result.success(inMeeting);
    }

    /**
     * 发送会议消息
     */
    @PostMapping("/{meetingId}/messages")
    public Result<MeetingMessage> sendMeetingMessage(
            @PathVariable Long meetingId,
            @RequestParam String content,
            @RequestParam(defaultValue = "text") String messageType,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        MeetingMessage message = meetingService.sendMeetingMessage(meetingId, userId, content, messageType);
        return Result.success(message);
    }

    /**
     * 获取会议消息
     */
    @GetMapping("/{meetingId}/messages")
    public Result<Map<String, Object>> getMeetingMessages(
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        Map<String, Object> result = meetingService.getMeetingMessages(meetingId, userId, page, size);
        return Result.success(result);
    }

    /**
     * 更新参与者状态
     */
    @PutMapping("/{meetingId}/participants/status")
    public Result<Void> updateParticipantStatus(
            @PathVariable Long meetingId,
            @RequestParam(required = false) Boolean isMuted,
            @RequestParam(required = false) Boolean isVideoOn,
            @RequestParam(required = false) String connectionStatus,
            HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        meetingService.updateParticipantStatus(meetingId, userId, isMuted, isVideoOn, connectionStatus);
        return Result.success();
    }

    /**
     * 检查会议状态
     */
    @GetMapping("/{meetingId}/status")
    public Result<Map<String, Object>> checkMeetingStatus(@PathVariable Long meetingId) {
        Map<String, Object> status = meetingService.checkMeetingStatus(meetingId);
        return Result.success(status);
    }

    /**
     * 保存白板数据
     */
    @PostMapping("/{meetingId}/whiteboard")
    public Result<Void> saveWhiteboardData(
            @PathVariable Long meetingId,
            @RequestParam String canvasData) {
        meetingService.saveWhiteboardData(meetingId, canvasData);
        return Result.success();
    }

    /**
     * 获取白板数据
     */
    @GetMapping("/{meetingId}/whiteboard")
    public Result<String> getWhiteboardData(@PathVariable Long meetingId) {
        String data = meetingService.getWhiteboardData(meetingId);
        return Result.success(data);
    }

    /**
     * 获取会议录制
     */
    @GetMapping("/{meetingId}/recording")
    public Result<String> getMeetingRecording(@PathVariable Long meetingId) {
        String recordingUrl = meetingService.getMeetingRecording(meetingId);
        return Result.success(recordingUrl);
    }

    /**
     * 结束会议
     */
    @PostMapping("/{meetingId}/end")
    public Result<Void> endMeeting(@PathVariable Long meetingId, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        meetingService.endMeeting(meetingId, userId);
        return Result.success();
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未找到有效的认证令牌");
    }
}