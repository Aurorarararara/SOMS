package com.office.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.admin.dto.WorkflowTemplateRequest;
import com.office.admin.entity.WorkflowTemplate;

import java.util.List;

/**
 * 审批流程模板Service接口
 */
public interface WorkflowTemplateService extends IService<WorkflowTemplate> {
    
    /**
     * 创建流程模板
     */
    WorkflowTemplate createTemplate(WorkflowTemplateRequest request, Long creatorId, String creatorName);
    
    /**
     * 更新流程模板
     */
    WorkflowTemplate updateTemplate(WorkflowTemplateRequest request, Long updaterId);
    
    /**
     * 删除流程模板
     */
    boolean deleteTemplate(Long templateId);
    
    /**
     * 获取模板详情（包含节点和条件）
     */
    WorkflowTemplate getTemplateDetail(Long templateId);
    
    /**
     * 根据业务类型查询可用模板
     */
    List<WorkflowTemplate> getAvailableTemplates(String businessType);
    
    /**
     * 根据业务类型查询默认模板
     */
    WorkflowTemplate getDefaultTemplate(String businessType);
    
    /**
     * 启用/禁用模板
     */
    boolean toggleTemplateStatus(Long templateId, Boolean isActive);
    
    /**
     * 设置默认模板
     */
    boolean setDefaultTemplate(Long templateId, String businessType);
    
    /**
     * 复制模板
     */
    WorkflowTemplate copyTemplate(Long templateId, String newName, Long creatorId, String creatorName);
}