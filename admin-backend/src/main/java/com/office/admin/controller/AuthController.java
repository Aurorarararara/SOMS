package com.office.admin.controller;

import com.office.admin.dto.LoginRequest;
import com.office.admin.dto.LoginResponse;
import com.office.admin.service.AuthService;
import com.office.admin.util.JwtUtil;
import com.office.admin.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 管理端认证控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/auth/admin")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return Result.success("登录成功", loginResponse);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        // JWT是无状态的，登出只需要前端删除token即可
        return Result.success("登出成功");
    }

    /**
     * 获取当前管理员用户信息
     */
    @GetMapping("/userinfo")
    public Result<LoginResponse.AdminUserInfo> getUserInfo(HttpServletRequest request) {
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

        // 验证管理员权限
        if (!authService.hasAdminPermission(userId)) {
            return Result.error(403, "权限不足");
        }

        // 获取用户信息
        LoginResponse.AdminUserInfo userInfo = authService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public Result<String> refreshToken(HttpServletRequest request) {
        // 从请求头中获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            return Result.error(401, "未登录");
        }

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            return Result.error(401, "token无效");
        }

        // 获取用户信息并生成新token
        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        if (username == null || userId == null) {
            return Result.error(401, "token解析失败");
        }

        // 检查用户状态和管理员权限
        if (!authService.isUserActive(userId)) {
            return Result.error(401, "用户已被禁用");
        }

        if (!authService.hasAdminPermission(userId)) {
            return Result.error(403, "权限不足");
        }

        // 生成新token
        String newToken = jwtUtil.generateToken(username, userId);
        return Result.success("token刷新成功", newToken);
    }

    /**
     * 检查权限
     */
    @GetMapping("/check-permission")
    public Result<Boolean> checkPermission(@RequestParam String permission, HttpServletRequest request) {
        // 从请求头中获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            return Result.success(false);
        }

        // 验证token并获取用户ID
        if (!jwtUtil.validateToken(token)) {
            return Result.success(false);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.success(false);
        }

        // 获取用户信息并检查权限
        try {
            LoginResponse.AdminUserInfo userInfo = authService.getUserInfo(userId);
            boolean hasPermission = userInfo.getPermissions() != null && 
                                  userInfo.getPermissions().contains(permission);
            return Result.success(hasPermission);
        } catch (Exception e) {
            return Result.success(false);
        }
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
}