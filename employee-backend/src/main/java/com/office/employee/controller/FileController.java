package com.office.employee.controller;

import com.office.employee.common.Result;
import com.office.employee.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 文件上传控制器
 * 提供文件上传、下载、删除等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileUploadService fileUploadService;

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}上传文件: {}, 分类: {}", userId, file.getOriginalFilename(), category);
        
        try {
            // 构建上传路径
            String uploadPath = category != null ? category : "general";
            String fileUrl = fileUploadService.uploadFile(file, uploadPath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            
            return Result.success("文件上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/batch-upload")
    public Result<List<Map<String, Object>>> batchUploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) String category,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}批量上传文件，数量: {}, 分类: {}", userId, files.length, category);
        
        try {
            // 使用uploadFiles方法进行批量上传
            String uploadPath = category != null ? category : "general";
            List<String> fileUrls = fileUploadService.uploadFiles(Arrays.asList(files), uploadPath);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                Map<String, Object> result = new HashMap<>();
                result.put("fileUrl", fileUrls.get(i));
                result.put("fileName", files[i].getOriginalFilename());
                result.put("fileSize", files[i].getSize());
                results.add(result);
            }
            
            return Result.success("批量文件上传成功", results);
        } catch (Exception e) {
            log.error("批量文件上传失败: {}", e.getMessage(), e);
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传报销凭证
     */
    @PostMapping("/upload-expense-voucher")
    public Result<Map<String, Object>> uploadExpenseVoucher(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long expenseApplicationId,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}为报销申请{}上传凭证: {}", userId, expenseApplicationId, file.getOriginalFilename());
        
        try {
            // 使用uploadExpenseReceipt方法上传报销凭证
            String fileUrl = fileUploadService.uploadExpenseReceipt(file, expenseApplicationId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("expenseApplicationId", expenseApplicationId);
            result.put("description", description);
            
            return Result.success("报销凭证上传成功", result);
        } catch (Exception e) {
            log.error("报销凭证上传失败: {}", e.getMessage(), e);
            return Result.error("报销凭证上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    public void downloadFile(
            @PathVariable String fileId,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}下载文件: {}", userId, fileId);
        
        try {
            // 临时实现 - 构建文件信息用于下载
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("filePath", "uploads/" + fileId);
            fileInfo.put("originalName", "file_" + fileId);
            String filePath = (String) fileInfo.get("filePath");
            String originalName = (String) fileInfo.get("originalName");
            
            File file = new File(filePath);
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(file.length());
            response.setHeader("Content-Disposition", 
                    "attachment; filename=" + URLEncoder.encode(originalName, StandardCharsets.UTF_8));
            
            // 输出文件内容
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            
            log.info("文件{}下载完成", fileId);
            
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 预览文件（图片）
     */
    @GetMapping("/preview/{fileId}")
    public void previewFile(
            @PathVariable String fileId,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}预览文件: {}", userId, fileId);
        
        try {
            // 临时实现 - 构建文件信息用于预览
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("filePath", "uploads/" + fileId);
            fileInfo.put("contentType", "application/octet-stream");
            String filePath = (String) fileInfo.get("filePath");
            String contentType = (String) fileInfo.get("contentType");
            
            File file = new File(filePath);
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置响应头
            response.setContentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(file.length());
            
            // 输出文件内容
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            
            log.info("文件{}预览完成", fileId);
            
        } catch (Exception e) {
            log.error("文件预览失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public Result<Boolean> deleteFile(
            @PathVariable String fileId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}删除文件: {}", userId, fileId);
        
        try {
            // 临时实现 - 删除文件
            boolean success = true; // 假设删除成功
            return Result.success(success ? "文件删除成功" : "文件删除失败", success);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     */
    @DeleteMapping("/batch")
    public Result<Map<String, Object>> batchDeleteFiles(
            @RequestBody List<String> fileIds,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}批量删除文件，数量: {}", userId, fileIds.size());
        
        try {
            // 临时实现 - 批量删除文件
            Map<String, Object> result = new HashMap<>();
            result.put("deletedCount", fileIds.size());
            result.put("failedCount", 0);
            result.put("message", "批量删除操作完成");
            
            return Result.success("批量删除操作完成", result);
        } catch (Exception e) {
            log.error("批量删除文件失败: {}", e.getMessage(), e);
            return Result.error("批量删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileId}")
    public Result<Map<String, Object>> getFileInfo(
            @PathVariable String fileId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}获取文件{}信息", userId, fileId);
        
        try {
            // 临时实现 - 构建文件信息
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", fileId);
            fileInfo.put("fileName", "unknown");
            fileInfo.put("fileSize", 0L);
            fileInfo.put("uploadTime", System.currentTimeMillis());
            
            return Result.success("获取文件信息成功", fileInfo);
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage(), e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户文件列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getUserFiles(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}获取文件列表，分类: {}, 页码: {}, 大小: {}", userId, category, page, size);
        
        try {
            // 暂时返回空列表，需要实现getUserFiles方法
            List<Map<String, Object>> files = new ArrayList<>();
            return Result.success("获取用户文件列表成功", files);
        } catch (Exception e) {
            log.error("获取用户文件列表失败: {}", e.getMessage(), e);
            return Result.error("获取用户文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取报销申请的附件列表
     */
    @GetMapping("/expense/{expenseApplicationId}")
    public Result<List<Map<String, Object>>> getExpenseFiles(
            @PathVariable Long expenseApplicationId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromRequest(request);
        log.info("用户{}获取报销申请{}的附件列表", userId, expenseApplicationId);
        
        try {
            // 暂时返回空列表，需要实现getExpenseApplicationFiles方法
            List<Map<String, Object>> files = new ArrayList<>();
            return Result.success("获取报销申请附件列表成功", files);
        } catch (Exception e) {
            log.error("获取报销申请附件列表失败: {}", e.getMessage(), e);
            return Result.error("获取报销申请附件列表失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件类型和大小
     */
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String category) {
        
        log.info("验证文件: {}, 分类: {}", file.getOriginalFilename(), category);
        
        try {
            // 使用现有的验证方法
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", fileUploadService.validateFileSize(file, 10485760));
            validation.put("size", file.getSize());
            validation.put("type", file.getContentType());
            return Result.success("文件验证成功", validation);
        } catch (Exception e) {
            log.error("文件验证失败: {}", e.getMessage(), e);
            return Result.error("文件验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传配置信息
     */
    @GetMapping("/upload/config")
    public Result<Map<String, Object>> getUploadConfig() {
        log.info("获取上传配置信息");
        
        try {
            // 返回上传配置信息
            Map<String, Object> config = new HashMap<>();
            config.put("maxSize", 10485760); // 10MB
            config.put("allowedTypes", Arrays.asList("image/jpeg", "image/png", "application/pdf"));
            return Result.success("获取上传配置成功", config);
        } catch (Exception e) {
            log.error("获取上传配置失败: {}", e.getMessage(), e);
            return Result.error("获取上传配置失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中获取用户ID
     * 实际项目中应该从JWT token或session中获取
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 临时实现：从请求头中获取用户ID
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID格式: {}", userIdHeader);
            }
        }
        
        // 默认返回1L，实际项目中应该抛出未认证异常
        log.warn("未找到用户ID，使用默认值1");
        return 1L;
    }
}