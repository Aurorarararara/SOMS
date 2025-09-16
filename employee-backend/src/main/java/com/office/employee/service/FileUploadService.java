package com.office.employee.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件上传服务接口
 * 提供文件上传、下载、删除等功能
 */
public interface FileUploadService {
    
    /**
     * 上传单个文件
     * @param file 上传的文件
     * @param uploadPath 上传路径（相对路径）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String uploadPath);
    
    /**
     * 批量上传文件
     * @param files 上传的文件列表
     * @param uploadPath 上传路径（相对路径）
     * @return 文件访问URL列表
     */
    List<String> uploadFiles(List<MultipartFile> files, String uploadPath);
    
    /**
     * 上传报销凭证
     * @param file 凭证文件
     * @param employeeId 员工ID
     * @return 文件访问URL
     */
    String uploadExpenseReceipt(MultipartFile file, Long employeeId);
    
    /**
     * 批量上传报销凭证
     * @param files 凭证文件列表
     * @param employeeId 员工ID
     * @return 文件访问URL列表
     */
    List<String> uploadExpenseReceipts(List<MultipartFile> files, Long employeeId);
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     * @return 删除结果统计
     */
    Map<String, Object> deleteFiles(List<String> fileUrls);
    
    /**
     * 获取文件信息
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    Map<String, Object> getFileInfo(String fileUrl);
    
    /**
     * 验证文件类型
     * @param file 上传的文件
     * @param allowedTypes 允许的文件类型
     * @return 是否为允许的类型
     */
    boolean validateFileType(MultipartFile file, List<String> allowedTypes);
    
    /**
     * 验证文件大小
     * @param file 上传的文件
     * @param maxSize 最大文件大小（字节）
     * @return 是否符合大小限制
     */
    boolean validateFileSize(MultipartFile file, long maxSize);
    
    /**
     * 生成唯一文件名
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    String generateUniqueFilename(String originalFilename);
    
    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名
     */
    String getFileExtension(String filename);
    
    /**
     * 清理过期的临时文件
     * @param days 保留天数
     * @return 清理的文件数量
     */
    int cleanupExpiredFiles(int days);
}