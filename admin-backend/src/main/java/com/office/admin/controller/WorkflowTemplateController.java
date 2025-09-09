package com.office.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.common.Result;
import com.office.admin.dto.WorkflowTemplateRequest;
import com.office.admin.entity.WorkflowTemplate;
import com.office.admin.service.WorkflowTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 审批流程模板管理Controller
 */
@RestController
@RequestMapping("/api/admin/workflow/templates")
@RequiredArgsConstructor
public class WorkflowTemplateController {
    
    private final WorkflowTemplateService workflowTemplateService;
    
    /**
     * 分页查询流程模板列表
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<Page<WorkflowTemplate>> getTemplateList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name) {
        
        Page<WorkflowTemplate> page = new Page<>(current, size);
        QueryWrapper<WorkflowTemplate> query = new QueryWrapper<>();
        
        if (businessType != null && !businessType.trim().isEmpty()) {
            query.eq("business_type", businessType);
        }
        if (category != null && !category.trim().isEmpty()) {
            query.eq("category", category);
        }
        if (status != null && !status.trim().isEmpty()) {
            query.eq("status", status);
        }
        if (name != null && !name.trim().isEmpty()) {
            query.like("name", name);
        }
        
        query.orderByDesc("create_time");
        
        Page<WorkflowTemplate> result = workflowTemplateService.page(page, query);
        return Result.success(result);
    }
    
    /**
     * 获取模板详情
     */
    @GetMapping("/{templateId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<WorkflowTemplate> getTemplateDetail(@PathVariable Long templateId) {
        WorkflowTemplate template = workflowTemplateService.getTemplateDetail(templateId);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success(template);
    }
    
    /**
     * 创建流程模板
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<WorkflowTemplate> createTemplate(@Valid @RequestBody WorkflowTemplateRequest request) {
        try {
            // TODO: 从当前用户上下文获取创建者信息
            Long creatorId = 1L; // 临时硬编码，实际应从SecurityContext获取
            String creatorName = "系统管理员";
            
            WorkflowTemplate template = workflowTemplateService.createTemplate(request, creatorId, creatorName);
            return Result.success(template);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新流程模板
     */
    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<WorkflowTemplate> updateTemplate(@PathVariable Long templateId, 
                                                  @Valid @RequestBody WorkflowTemplateRequest request) {
        try {
            request.setId(templateId);
            // TODO: 从当前用户上下文获取更新者信息
            Long updaterId = 1L;
            
            WorkflowTemplate template = workflowTemplateService.updateTemplate(request, updaterId);
            return Result.success(template);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除流程模板
     */
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Boolean> deleteTemplate(@PathVariable Long templateId) {
        try {
            boolean result = workflowTemplateService.deleteTemplate(templateId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 启用/禁用模板
     */
    @PutMapping("/{templateId}/toggle-status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Boolean> toggleTemplateStatus(@PathVariable Long templateId, 
                                              @RequestParam Boolean isActive) {
        try {
            boolean result = workflowTemplateService.toggleTemplateStatus(templateId, isActive);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置默认模板
     */
    @PutMapping("/{templateId}/set-default")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Boolean> setDefaultTemplate(@PathVariable Long templateId, 
                                            @RequestParam String businessType) {
        try {
            boolean result = workflowTemplateService.setDefaultTemplate(templateId, businessType);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制模板
     */
    @PostMapping("/{templateId}/copy")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<WorkflowTemplate> copyTemplate(@PathVariable Long templateId, 
                                               @RequestParam String newName) {
        try {
            // TODO: 从当前用户上下文获取创建者信息
            Long creatorId = 1L;
            String creatorName = "系统管理员";
            
            WorkflowTemplate template = workflowTemplateService.copyTemplate(templateId, newName, creatorId, creatorName);
            return Result.success(template);
        } catch (Exception e) {
            return Result.error("复制失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据业务类型获取可用模板
     */
    @GetMapping("/available")
    public Result<List<WorkflowTemplate>> getAvailableTemplates(@RequestParam String businessType) {
        List<WorkflowTemplate> templates = workflowTemplateService.getAvailableTemplates(businessType);
        return Result.success(templates);
    }
    
    /**
     * 根据业务类型获取默认模板
     */
    @GetMapping("/default")
    public Result<WorkflowTemplate> getDefaultTemplate(@RequestParam String businessType) {
        WorkflowTemplate template = workflowTemplateService.getDefaultTemplate(businessType);
        if (template == null) {
            return Result.error("未找到默认模板");
        }
        return Result.success(template);
    }
}