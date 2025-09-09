package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件存储实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("file_storage")
public class FileStorage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件唯一标识
     */
    @TableField("file_id")
    private String fileId;

    /**
     * 原始文件名
     */
    @TableField("original_name")
    private String originalName;

    /**
     * 存储文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件存储路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * MIME类型
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 文件扩展名
     */
    @TableField("file_extension")
    private String fileExtension;

    /**
     * MD5哈希值
     */
    @TableField("md5_hash")
    private String md5Hash;

    /**
     * SHA256哈希值
     */
    @TableField("sha256_hash")
    private String sha256Hash;

    /**
     * 存储类型：local,oss,s3,minio
     */
    @TableField("storage_type")
    private String storageType;

    /**
     * 存储配置
     */
    @TableField("storage_config")
    private String storageConfig;

    /**
     * 是否加密
     */
    @TableField("is_encrypted")
    private Boolean isEncrypted;

    /**
     * 加密密钥
     */
    @TableField("encryption_key")
    private String encryptionKey;

    /**
     * 缩略图路径
     */
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /**
     * 预览文件路径
     */
    @TableField("preview_path")
    private String previewPath;

    /**
     * 上传状态：uploading,completed,failed
     */
    @TableField("upload_status")
    private String uploadStatus;

    /**
     * 上传者ID
     */
    @TableField("uploaded_by")
    private Long uploadedBy;

    /**
     * 上传时间
     */
    @TableField("uploaded_at")
    private LocalDateTime uploadedAt;

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
    private String uploaderName;

    @TableField(exist = false)
    private String fileSizeFormatted;

    @TableField(exist = false)
    private Boolean canPreview;

    @TableField(exist = false)
    private Boolean canEdit;

    @TableField(exist = false)
    private List<String> tags;

    @TableField(exist = false)
    private Boolean isFavorite;

    @TableField(exist = false)
    private String downloadUrl;

    @TableField(exist = false)
    private String previewUrl;

    @TableField(exist = false)
    private Integer versionCount;

    @TableField(exist = false)
    private String currentVersion;

    @TableField(exist = false)
    private String permissions;
}