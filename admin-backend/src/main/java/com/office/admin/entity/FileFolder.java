package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件夹实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("file_folders")
public class FileFolder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件夹唯一标识
     */
    @TableField("folder_id")
    private String folderId;

    /**
     * 文件夹名称
     */
    @TableField("folder_name")
    private String folderName;

    /**
     * 父文件夹ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 文件夹路径
     */
    @TableField("folder_path")
    private String folderPath;

    /**
     * 文件夹层级
     */
    @TableField("folder_level")
    private Integer folderLevel;

    /**
     * 文件夹类型：normal,department,project,shared,personal
     */
    @TableField("folder_type")
    private String folderType;

    /**
     * 文件夹描述
     */
    @TableField("description")
    private String description;

    /**
     * 文件夹颜色
     */
    @TableField("folder_color")
    private String folderColor;

    /**
     * 文件夹图标
     */
    @TableField("folder_icon")
    private String folderIcon;

    /**
     * 是否系统文件夹
     */
    @TableField("is_system")
    private Boolean isSystem;

    /**
     * 是否公开文件夹
     */
    @TableField("is_public")
    private Boolean isPublic;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 关联部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 关联项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // 非数据库字段
    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private String parentFolderName;

    @TableField(exist = false)
    private List<FileFolder> children;

    @TableField(exist = false)
    private Integer fileCount;

    @TableField(exist = false)
    private Integer folderCount;

    @TableField(exist = false)
    private Long totalSize;

    @TableField(exist = false)
    private String totalSizeFormatted;

    @TableField(exist = false)
    private List<String> tags;

    @TableField(exist = false)
    private Boolean isFavorite;

    @TableField(exist = false)
    private String permissions;

    @TableField(exist = false)
    private String breadcrumb;
}