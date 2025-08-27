package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("collaborative_sessions")
public class CollaborativeSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("document_id")
    private Long documentId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("user_name")
    private String userName;
    
    @TableField("session_id")
    private String sessionId;
    
    @TableField("cursor_position")
    private String cursorPosition; // JSON格式存储光标位置
    
    @TableField("selection_range")
    private String selectionRange; // JSON格式存储选择范围
    
    @TableField("user_color")
    private String userColor; // 用户在协同编辑中的颜色标识
    
    @TableField("is_online")
    private Boolean isOnline;
    
    @TableField("last_seen")
    private LocalDateTime lastSeen;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}