package com.office.admin.controller;

import com.office.admin.entity.Meeting;
import com.office.admin.entity.MeetingRoom;
import com.office.admin.service.MeetingService;
import com.office.admin.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "会议管理", description = "会议相关接口")
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @Operation(summary = "创建会议")
    @PostMapping
    public Result<Meeting> createMeeting(@Valid @RequestBody Meeting meeting) {
        Meeting createdMeeting = meetingService.createMeeting(meeting);
        return Result.success(createdMeeting);
    }

    @Operation(summary = "获取会议列表")
    @GetMapping
    public Result<List<Meeting>> getMeetings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long hostId) {
        List<Meeting> meetings = meetingService.getMeetings(page, size, status, hostId);
        return Result.success(meetings);
    }

    @Operation(summary = "获取会议详情")
    @GetMapping("/{id}")
    public Result<Meeting> getMeetingById(@PathVariable Long id) {
        Meeting meeting = meetingService.getMeetingById(id);
        return Result.success(meeting);
    }

    @Operation(summary = "通过会议代码获取会议")
    @GetMapping("/code/{meetingCode}")
    public Result<Meeting> getMeetingByCode(@PathVariable String meetingCode) {
        Meeting meeting = meetingService.getMeetingByCode(meetingCode);
        return Result.success(meeting);
    }

    @Operation(summary = "更新会议")
    @PutMapping("/{id}")
    public Result<Meeting> updateMeeting(@PathVariable Long id, @Valid @RequestBody Meeting meeting) {
        meeting.setId(id);
        Meeting updatedMeeting = meetingService.updateMeeting(meeting);
        return Result.success(updatedMeeting);
    }

    @Operation(summary = "删除会议")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return Result.success();
    }

    @Operation(summary = "加入会议")
    @PostMapping("/{id}/join")
    public Result<Map<String, Object>> joinMeeting(
            @PathVariable Long id,
            @RequestParam(required = false) String password) {
        Map<String, Object> joinInfo = meetingService.joinMeeting(id, password);
        return Result.success(joinInfo);
    }

    @Operation(summary = "离开会议")
    @PostMapping("/{id}/leave")
    public Result<Void> leaveMeeting(@PathVariable Long id) {
        meetingService.leaveMeeting(id);
        return Result.success();
    }

    @Operation(summary = "开始会议")
    @PostMapping("/{id}/start")
    public Result<Void> startMeeting(@PathVariable Long id) {
        meetingService.startMeeting(id);
        return Result.success();
    }

    @Operation(summary = "结束会议")
    @PostMapping("/{id}/end")
    public Result<Void> endMeeting(@PathVariable Long id) {
        meetingService.endMeeting(id);
        return Result.success();
    }

    @Operation(summary = "开始录制")
    @PostMapping("/{id}/recording/start")
    public Result<Void> startRecording(@PathVariable Long id) {
        meetingService.startRecording(id);
        return Result.success();
    }

    @Operation(summary = "停止录制")
    @PostMapping("/{id}/recording/stop")
    public Result<Void> stopRecording(@PathVariable Long id) {
        meetingService.stopRecording(id);
        return Result.success();
    }

    @Operation(summary = "邀请参与者")
    @PostMapping("/{id}/invite")
    public Result<Void> inviteParticipants(
            @PathVariable Long id,
            @RequestBody List<Long> userIds) {
        meetingService.inviteParticipants(id, userIds);
        return Result.success();
    }

    @Operation(summary = "获取会议参与者")
    @GetMapping("/{id}/participants")
    public Result<List<Map<String, Object>>> getMeetingParticipants(@PathVariable Long id) {
        List<Map<String, Object>> participants = meetingService.getMeetingParticipants(id);
        return Result.success(participants);
    }

    @Operation(summary = "静音/取消静音参与者")
    @PostMapping("/{id}/participants/{userId}/mute")
    public Result<Void> muteParticipant(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam Boolean muted) {
        meetingService.muteParticipant(id, userId, muted);
        return Result.success();
    }

    @Operation(summary = "获取会议室列表")
    @GetMapping("/rooms")
    public Result<List<MeetingRoom>> getMeetingRooms() {
        List<MeetingRoom> rooms = meetingService.getMeetingRooms();
        return Result.success(rooms);
    }

    @Operation(summary = "创建会议室")
    @PostMapping("/rooms")
    public Result<MeetingRoom> createMeetingRoom(@Valid @RequestBody MeetingRoom room) {
        MeetingRoom createdRoom = meetingService.createMeetingRoom(room);
        return Result.success(createdRoom);
    }

    @Operation(summary = "生成会议链接")
    @GetMapping("/{id}/link")
    public Result<String> generateMeetingLink(@PathVariable Long id) {
        String link = meetingService.generateMeetingLink(id);
        return Result.success(link);
    }
}