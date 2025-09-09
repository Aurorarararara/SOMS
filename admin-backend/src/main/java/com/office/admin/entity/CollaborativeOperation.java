package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("collaborative_operations")
public class CollaborativeOperation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("document_id")
    private Long documentId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("user_name")
    private String userName;
    
    @TableField("operation_type")
    private String operationType; // insert, delete, retain, format
    
    @TableField("operation_data")
    private String operationData; // JSON格式的操作数据
    
    @TableField("sequence_number")
    private Long sequenceNumber; // 操作序列号，用于排序
    
    @TableField("session_id")
    private String sessionId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}