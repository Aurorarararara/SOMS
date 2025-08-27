package com.office.employee.controller;

import com.office.employee.dto.LoginRequest;
import com.office.employee.dto.LoginResponse;
import com.office.employee.service.AuthService;
import com.office.employee.util.JwtUtil;
import com.office.employee.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return Result.success("登录成功", loginResponse);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        // JWT是无状态的，登出只需要前端删除token即可
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
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

        // 检查用户状态
        if (!authService.isUserActive(userId)) {
            return Result.error(401, "用户已被禁用");
        }

        // 生成新token
        String newToken = jwtUtil.generateToken(username, userId);
        return Result.success("token刷新成功", newToken);
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