package com.office.employee.service.impl;

import com.office.employee.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    @Value("${file.upload.path:/uploads}")
    private String uploadBasePath;
    
    @Value("${file.upload.domain:http://localhost:8080}")
    private String uploadDomain;
    
    @Value("${file.upload.max-size:10485760}") // 10MB
    private long maxFileSize;
    
    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    );
    
    // 允许的文档类型
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    
    // 报销凭证允许的文件类型
    private static final List<String> EXPENSE_RECEIPT_TYPES = new ArrayList<>();
    static {
        EXPENSE_RECEIPT_TYPES.addAll(ALLOWED_IMAGE_TYPES);
        EXPENSE_RECEIPT_TYPES.addAll(ALLOWED_DOCUMENT_TYPES);
    }

    @Override
    public String uploadFile(MultipartFile file, String uploadPath) {
        log.info("开始上传文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        
        // 验证文件大小
        if (!validateFileSize(file, maxFileSize)) {
            throw new RuntimeException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        try {
            // 创建上传目录
            String fullUploadPath = uploadBasePath + File.separator + uploadPath;
            Path uploadDir = Paths.get(fullUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 生成唯一文件名
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
            Path filePath = uploadDir.resolve(uniqueFilename);
            
            // 保存文件
            file.transferTo(filePath.toFile());
            
            // 生成访问URL
            String fileUrl = uploadDomain + "/uploads/" + uploadPath + "/" + uniqueFilename;
            
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String uploadPath) {
        log.info("开始批量上传文件，数量: {}", files.size());
        
        List<String> fileUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String fileUrl = uploadFile(file, uploadPath);
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            log.warn("部分文件上传失败: {}", String.join(", ", errors));
        }
        
        log.info("批量上传完成，成功: {}, 失败: {}", fileUrls.size(), errors.size());
        return fileUrls;
    }

    @Override
    public String uploadExpenseReceipt(MultipartFile file, Long employeeId) {
        log.info("上传报销凭证，员工ID: {}, 文件: {}", employeeId, file.getOriginalFilename());
        
        // 验证文件类型
        if (!validateFileType(file, EXPENSE_RECEIPT_TYPES)) {
            throw new RuntimeException("不支持的文件类型，请上传图片或PDF文档");
        }
        
        // 构建上传路径：expense/receipts/employeeId/yyyy/MM
        String uploadPath = buildExpenseReceiptPath(employeeId);
        
        return uploadFile(file, uploadPath);
    }

    @Override
    public List<String> uploadExpenseReceipts(List<MultipartFile> files, Long employeeId) {
        log.info("批量上传报销凭证，员工ID: {}, 文件数量: {}", employeeId, files.size());
        
        String uploadPath = buildExpenseReceiptPath(employeeId);
        
        List<String> fileUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                if (!validateFileType(file, EXPENSE_RECEIPT_TYPES)) {
                    errors.add(file.getOriginalFilename() + ": 不支持的文件类型");
                    continue;
                }
                
                String fileUrl = uploadFile(file, uploadPath);
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        if (!errors.isEmpty()) {
            log.warn("部分报销凭证上传失败: {}", String.join(", ", errors));
        }
        
        return fileUrls;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        log.info("删除文件: {}", fileUrl);
        
        try {
            // 从URL中提取文件路径
            String filePath = extractFilePathFromUrl(fileUrl);
            if (filePath == null) {
                log.warn("无效的文件URL: {}", fileUrl);
                return false;
            }
            
            Path file = Paths.get(uploadBasePath + File.separator + filePath);
            if (Files.exists(file)) {
                Files.delete(file);
                log.info("文件删除成功: {}", fileUrl);
                return true;
            } else {
                log.warn("文件不存在: {}", fileUrl);
                return false;
            }
            
        } catch (IOException e) {
            log.error("文件删除失败: {}, 错误: {}", fileUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> deleteFiles(List<String> fileUrls) {
        log.info("批量删除文件，数量: {}", fileUrls.size());
        
        Map<String, Object> result = new HashMap<>();
        List<String> successUrls = new ArrayList<>();
        List<String> failedUrls = new ArrayList<>();
        
        for (String fileUrl : fileUrls) {
            if (deleteFile(fileUrl)) {
                successUrls.add(fileUrl);
            } else {
                failedUrls.add(fileUrl);
            }
        }
        
        result.put("successCount", successUrls.size());
        result.put("successUrls", successUrls);
        result.put("failedCount", failedUrls.size());
        result.put("failedUrls", failedUrls);
        
        log.info("批量删除完成，成功: {}, 失败: {}", successUrls.size(), failedUrls.size());
        return result;
    }

    @Override
    public Map<String, Object> getFileInfo(String fileUrl) {
        Map<String, Object> fileInfo = new HashMap<>();
        
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            if (filePath == null) {
                return fileInfo;
            }
            
            Path file = Paths.get(uploadBasePath + File.separator + filePath);
            if (Files.exists(file)) {
                fileInfo.put("exists", true);
                fileInfo.put("size", Files.size(file));
                fileInfo.put("lastModified", Files.getLastModifiedTime(file).toString());
                fileInfo.put("contentType", Files.probeContentType(file));
                fileInfo.put("filename", file.getFileName().toString());
            } else {
                fileInfo.put("exists", false);
            }
            
        } catch (IOException e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            fileInfo.put("error", e.getMessage());
        }
        
        return fileInfo;
    }

    @Override
    public boolean validateFileType(MultipartFile file, List<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        
        return allowedTypes.contains(contentType.toLowerCase());
    }

    @Override
    public boolean validateFileSize(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        return file.getSize() <= maxSize;
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            originalFilename = "file";
        }
        
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        
        return timestamp + "_" + randomStr + extension;
    }

    @Override
    public String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        
        return "";
    }

    @Override
    public int cleanupExpiredFiles(int days) {
        log.info("开始清理{}天前的过期文件", days);
        
        int cleanedCount = 0;
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        
        try {
            Path uploadDir = Paths.get(uploadBasePath);
            if (Files.exists(uploadDir)) {
                cleanedCount = cleanupDirectory(uploadDir, expireTime);
            }
        } catch (Exception e) {
            log.error("清理过期文件失败: {}", e.getMessage(), e);
        }
        
        log.info("过期文件清理完成，清理数量: {}", cleanedCount);
        return cleanedCount;
    }
    
    /**
     * 构建报销凭证上传路径
     */
    private String buildExpenseReceiptPath(Long employeeId) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("expense/receipts/%d/%d/%02d", 
                employeeId, now.getYear(), now.getMonthValue());
    }
    
    /**
     * 从URL中提取文件路径
     */
    private String extractFilePathFromUrl(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }
        
        // 移除域名部分，提取相对路径
        String uploadsPrefix = "/uploads/";
        int uploadsIndex = fileUrl.indexOf(uploadsPrefix);
        if (uploadsIndex >= 0) {
            return fileUrl.substring(uploadsIndex + uploadsPrefix.length());
        }
        
        return null;
    }
    
    /**
     * 递归清理目录中的过期文件
     */
    private int cleanupDirectory(Path directory, LocalDateTime expireTime) throws IOException {
        int cleanedCount = 0;
        
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return cleanedCount;
        }
        
        try (var stream = Files.list(directory)) {
            for (Path path : stream.collect(Collectors.toList())) {
                if (Files.isDirectory(path)) {
                    cleanedCount += cleanupDirectory(path, expireTime);
                    // 如果目录为空，删除目录
                    try (var dirStream = Files.list(path)) {
                        if (dirStream.findAny().isEmpty()) {
                            Files.delete(path);
                            log.debug("删除空目录: {}", path);
                        }
                    }
                } else if (Files.isRegularFile(path)) {
                    LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(),
                            java.time.ZoneId.systemDefault());
                    
                    if (fileTime.isBefore(expireTime)) {
                        Files.delete(path);
                        cleanedCount++;
                        log.debug("删除过期文件: {}", path);
                    }
                }
            }
        }
        
        return cleanedCount;
    }
}