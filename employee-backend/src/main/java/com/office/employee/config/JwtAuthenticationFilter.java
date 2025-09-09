package com.office.employee.config;

import com.office.employee.util.JwtUtil;
import com.office.employee.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 *
 * @author office-system
 * @since 2024-01-01
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 检查是否是允许匿名访问的路径
        String requestPath = request.getRequestURI();
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (username != null && userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 检查用户状态
                if (authService.isUserActive(userId)) {
                    UserDetails userDetails = User.builder()
                            .username(username)
                            .password("")
                            .authorities(new ArrayList<>())
                            .build();

                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 将用户ID设置到请求属性中，方便后续使用
                    request.setAttribute("userId", userId);
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        
        filterChain.doFilter(request, response);
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
     * 检查是否是公共路径（允许匿名访问）
     */
    private boolean isPublicPath(String requestPath) {
        return requestPath.startsWith("/api/auth/") ||
               requestPath.equals("/api/health") ||
               requestPath.startsWith("/actuator/") ||
               requestPath.equals("/error") ||
               requestPath.startsWith("/ws-collaboration/") ||
               requestPath.startsWith("/ws-richtext/") ||
               requestPath.startsWith("/ws-markdown/") ||
               requestPath.startsWith("/ws-code/") ||
               requestPath.startsWith("/ws-table/");
    }
}