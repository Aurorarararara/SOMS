package com.office.admin.controller;

import com.office.admin.common.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统消息控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/admin/messages")
public class MessageController {

    /**
     * 获取系统消息
     */
    @GetMapping("/system")
    public Result<List<SystemMessage>> getSystemMessages(@RequestParam(defaultValue = "5") int limit) {
        System.out.println("收到获取系统消息的请求，limit: " + limit);
        // 创建一些示例系统消息
        List<SystemMessage> messages = new ArrayList<>();
        
        messages.add(new SystemMessage(1L, "系统将于今晚23:00进行维护", LocalDateTime.now().minusHours(2)));
        messages.add(new SystemMessage(2L, "新版本已发布，请及时更新", LocalDateTime.now().minusDays(1)));
        messages.add(new SystemMessage(3L, "安全补丁已安装", LocalDateTime.now().minusDays(2)));
        
        // 如果需要更多消息，可以添加
        if (limit > 3) {
            messages.add(new SystemMessage(4L, "服务器升级完成", LocalDateTime.now().minusDays(3)));
            messages.add(new SystemMessage(5L, "系统性能优化", LocalDateTime.now().minusDays(5)));
        }
        
        // 限制返回数量
        if (messages.size() > limit) {
            messages = messages.subList(0, limit);
        }
        
        System.out.println("返回系统消息数量: " + messages.size());
        return Result.success(messages);
    }
    
    /**
     * 系统消息内部类
     */
    public static class SystemMessage {
        private Long id;
        private String content;
        private LocalDateTime createTime;
        
        public SystemMessage() {}
        
        public SystemMessage(Long id, String content, LocalDateTime createTime) {
            this.id = id;
            this.content = content;
            this.createTime = createTime;
        }
        
        // Getter and Setter methods
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public LocalDateTime getCreateTime() {
            return createTime;
        }
        
        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
}