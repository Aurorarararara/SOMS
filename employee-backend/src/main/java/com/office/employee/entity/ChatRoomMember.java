package com.office.employee.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 聊天室成员实体类
 *
 * @author office-system
 * @since 2025-08-30
 */
@TableName("chat_room_member")
public class ChatRoomMember {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String roomId;
    
    private Long userId;
    
    private String nickname;
    
    private String role;
    
    private LocalDateTime joinTime;
    
    private String status;
    
    private LocalDateTime lastReadTime;
    
    @TableLogic
    private Integer isDeleted;

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(LocalDateTime lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}