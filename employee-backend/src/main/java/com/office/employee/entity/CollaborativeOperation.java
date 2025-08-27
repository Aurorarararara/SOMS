package com.office.employee.entity;

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
    
    @TableField("operation_index")
    private Integer operationIndex; // 操作位置索引
    
    @TableField("operation_length")
    private Integer operationLength; // 操作长度
    
    @TableField("before_content")
    private String beforeContent; // 操作前内容片段
    
    @TableField("after_content")
    private String afterContent; // 操作后内容片段
    
    @TableField("version_before")
    private Long versionBefore; // 操作前版本号
    
    @TableField("version_after")
    private Long versionAfter; // 操作后版本号
    
    @TableField("session_id")
    private String sessionId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}