package com.office.employee.controller;

import com.office.employee.dto.LoginResponse;
import com.office.employee.service.AuthService;
import com.office.employee.util.JwtUtil;
import com.office.employee.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<LoginResponse.UserInfo> getUserInfo(HttpServletRequest request) {
        // 从请求头中获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            return Result.error(401, "未登录");
        }

        // 验证token并获取用户ID
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "token无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "token解析失败");
        }

        // 获取用户信息
        LoginResponse.UserInfo userInfo = authService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新用户基本信息
     */
    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody LoginResponse.UserInfo userInfo, HttpServletRequest request) {
        // 从请求头中获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            return Result.error(401, "未登录");
        }

        // 验证token并获取用户ID
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "token无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "token解析失败");
        }

        // 更新用户信息
        authService.updateUserInfo(userId, userInfo);
        return Result.success("用户信息更新成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        // 从请求头中获取token
        String token = getTokenFromRequest(httpRequest);
        if (token == null) {
            return Result.error(401, "未登录");
        }

        // 验证token并获取用户ID
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "token无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "token解析失败");
        }

        // 修改密码
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功");
    }

    /**
     * 获取工作统计
     */
    @GetMapping("/work-stats")
    public Result<WorkStatsResponse> getWorkStats(HttpServletRequest request) {
        // 从请求头中获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            return Result.error(401, "未登录");
        }

        // 验证token并获取用户ID
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "token无效");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "token解析失败");
        }

        // 获取工作统计
        WorkStatsResponse workStats = authService.getWorkStats(userId);
        return Result.success(workStats);
    }

    /**
     * 从请求中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 修改密码请求DTO
     */
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    /**
     * 工作统计响应DTO
     */
    public static class WorkStatsResponse {
        private int totalAttendance;
        private int todayAttendance;
        private int pendingLeave;
        private int totalTasks;

        public WorkStatsResponse(int totalAttendance, int todayAttendance, int pendingLeave, int totalTasks) {
            this.totalAttendance = totalAttendance;
            this.todayAttendance = todayAttendance;
            this.pendingLeave = pendingLeave;
            this.totalTasks = totalTasks;
        }

        // Getter and Setter methods
        public int getTotalAttendance() {
            return totalAttendance;
        }

        public void setTotalAttendance(int totalAttendance) {
            this.totalAttendance = totalAttendance;
        }

        public int getTodayAttendance() {
            return todayAttendance;
        }

        public void setTodayAttendance(int todayAttendance) {
            this.todayAttendance = todayAttendance;
        }

        public int getPendingLeave() {
            return pendingLeave;
        }

        public void setPendingLeave(int pendingLeave) {
            this.pendingLeave = pendingLeave;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(int totalTasks) {
            this.totalTasks = totalTasks;
        }
    }
}