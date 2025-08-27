package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.admin.dto.WorkflowTemplateRequest;
import com.office.admin.entity.*;
import com.office.admin.mapper.*;
import com.office.admin.service.WorkflowTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批流程模板Service实现类
 */
@Service
@RequiredArgsConstructor
public class WorkflowTemplateServiceImpl extends ServiceImpl<WorkflowTemplateMapper, WorkflowTemplate> implements WorkflowTemplateService {
    
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final WorkflowConditionMapper workflowConditionMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTemplate createTemplate(WorkflowTemplateRequest request, Long creatorId, String creatorName) {
        // 创建模板
        WorkflowTemplate template = new WorkflowTemplate();
        BeanUtils.copyProperties(request, template);
        template.setCreatorId(creatorId);
        template.setCreatorName(creatorName);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        
        // 如果设置为默认模板，先将同业务类型的其他默认模板取消
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            workflowTemplateMapper.update(null, new UpdateWrapper<WorkflowTemplate>()
                    .eq("business_type", request.getBusinessType())
                    .set("is_default", false));
        }
        
        save(template);
        
        // 创建节点
        if (request.getNodes() != null && !request.getNodes().isEmpty()) {
            List<WorkflowNode> nodes = request.getNodes().stream().map(nodeRequest -> {
                WorkflowNode node = new WorkflowNode();
                BeanUtils.copyProperties(nodeRequest, node);
                node.setTemplateId(template.getId());
                return node;
            }).collect(Collectors.toList());
            
            workflowNodeMapper.insertBatch(nodes);
        }
        
        // 创建条件
        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            List<WorkflowCondition> conditions = request.getConditions().stream().map(conditionRequest -> {
                WorkflowCondition condition = new WorkflowCondition();
                BeanUtils.copyProperties(conditionRequest, condition);
                condition.setTemplateId(template.getId());
                return condition;
            }).collect(Collectors.toList());
            
            workflowConditionMapper.insertBatch(conditions);
        }
        
        return getTemplateDetail(template.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTemplate updateTemplate(WorkflowTemplateRequest request, Long updaterId) {
        WorkflowTemplate template = getById(request.getId());
        if (template == null) {
            throw new RuntimeException("流程模板不存在");
        }
        
        // 更新模板基本信息
        BeanUtils.copyProperties(request, template);
        template.setUpdateBy(updaterId);
        template.setUpdateTime(LocalDateTime.now());
        template.setVersion(template.getVersion() + 1);
        
        // 如果设置为默认模板，先将同业务类型的其他默认模板取消
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            workflowTemplateMapper.update(null, new UpdateWrapper<WorkflowTemplate>()
                    .eq("business_type", request.getBusinessType())
                    .ne("id", request.getId())
                    .set("is_default", false));
        }
        
        updateById(template);
        
        // 删除原有节点和条件
        workflowNodeMapper.delete(new QueryWrapper<WorkflowNode>().eq("template_id", template.getId()));
        workflowConditionMapper.delete(new QueryWrapper<WorkflowCondition>().eq("template_id", template.getId()));
        
        // 重新创建节点和条件
        if (request.getNodes() != null && !request.getNodes().isEmpty()) {
            List<WorkflowNode> nodes = request.getNodes().stream().map(nodeRequest -> {
                WorkflowNode node = new WorkflowNode();
                BeanUtils.copyProperties(nodeRequest, node);
                node.setTemplateId(template.getId());
                node.setCreateTime(LocalDateTime.now());
                return node;
            }).collect(Collectors.toList());
            
            workflowNodeMapper.insertBatch(nodes);
        }
        
        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            List<WorkflowCondition> conditions = request.getConditions().stream().map(conditionRequest -> {
                WorkflowCondition condition = new WorkflowCondition();
                BeanUtils.copyProperties(conditionRequest, condition);
                condition.setTemplateId(template.getId());
                condition.setCreateTime(LocalDateTime.now());
                return condition;
            }).collect(Collectors.toList());
            
            workflowConditionMapper.insertBatch(conditions);
        }
        
        return getTemplateDetail(template.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long templateId) {
        // 检查是否有运行中的流程实例
        QueryWrapper<WorkflowInstance> instanceQuery = new QueryWrapper<>();
        instanceQuery.eq("template_id", templateId).eq("instance_status", "running");
        long runningCount = workflowInstanceMapper.selectCount(instanceQuery);
        
        if (runningCount > 0) {
            throw new RuntimeException("该模板有正在运行的流程实例，无法删除");
        }
        
        // 删除相关数据
        workflowNodeMapper.delete(new QueryWrapper<WorkflowNode>().eq("template_id", templateId));
        workflowConditionMapper.delete(new QueryWrapper<WorkflowCondition>().eq("template_id", templateId));
        
        return removeById(templateId);
    }
    
    @Override
    public WorkflowTemplate getTemplateDetail(Long templateId) {
        WorkflowTemplate template = getById(templateId);
        if (template != null) {
            // 加载节点
            List<WorkflowNode> nodes = workflowNodeMapper.selectList(
                    new QueryWrapper<WorkflowNode>().eq("template_id", templateId).orderByAsc("node_order"));
            template.setNodes(nodes);
            
            // 加载条件
            List<WorkflowCondition> conditions = workflowConditionMapper.selectList(
                    new QueryWrapper<WorkflowCondition>().eq("template_id", templateId).orderByAsc("sort_order"));
            template.setConditions(conditions);
        }
        return template;
    }
    
    @Override
    public List<WorkflowTemplate> getAvailableTemplates(String businessType) {
        return workflowTemplateMapper.findAvailableTemplates(businessType);
    }
    
    @Override
    public WorkflowTemplate getDefaultTemplate(String businessType) {
        return workflowTemplateMapper.findDefaultTemplate(businessType);
    }
    
    @Override
    public boolean toggleTemplateStatus(Long templateId, Boolean isActive) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setId(templateId);
        template.setIsActive(isActive);
        template.setStatus(isActive ? "active" : "archived");
        template.setUpdateTime(LocalDateTime.now());
        
        return updateById(template);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultTemplate(Long templateId, String businessType) {
        // 先将同业务类型的其他默认模板取消
        workflowTemplateMapper.update(null, new UpdateWrapper<WorkflowTemplate>()
                .eq("business_type", businessType)
                .set("is_default", false));
        
        // 设置新的默认模板
        WorkflowTemplate template = new WorkflowTemplate();
        template.setId(templateId);
        template.setIsDefault(true);
        template.setUpdateTime(LocalDateTime.now());
        
        return updateById(template);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTemplate copyTemplate(Long templateId, String newName, Long creatorId, String creatorName) {
        WorkflowTemplate originalTemplate = getTemplateDetail(templateId);
        if (originalTemplate == null) {
            throw new RuntimeException("原模板不存在");
        }
        
        // 复制模板基本信息
        WorkflowTemplate newTemplate = new WorkflowTemplate();
        BeanUtils.copyProperties(originalTemplate, newTemplate);
        newTemplate.setId(null);
        newTemplate.setName(newName);
        newTemplate.setIsDefault(false);
        newTemplate.setCreatorId(creatorId);
        newTemplate.setCreatorName(creatorName);
        newTemplate.setStatus("draft");
        newTemplate.setVersion(1);
        newTemplate.setCreateTime(LocalDateTime.now());
        newTemplate.setUpdateTime(LocalDateTime.now());
        
        save(newTemplate);
        
        // 复制节点
        if (originalTemplate.getNodes() != null && !originalTemplate.getNodes().isEmpty()) {
            List<WorkflowNode> newNodes = originalTemplate.getNodes().stream().map(node -> {
                WorkflowNode newNode = new WorkflowNode();
                BeanUtils.copyProperties(node, newNode);
                newNode.setId(null);
                newNode.setTemplateId(newTemplate.getId());
                newNode.setCreateTime(LocalDateTime.now());
                newNode.setUpdateTime(LocalDateTime.now());
                return newNode;
            }).collect(Collectors.toList());
            
            workflowNodeMapper.insertBatch(newNodes);
        }
        
        // 复制条件
        if (originalTemplate.getConditions() != null && !originalTemplate.getConditions().isEmpty()) {
            List<WorkflowCondition> newConditions = originalTemplate.getConditions().stream().map(condition -> {
                WorkflowCondition newCondition = new WorkflowCondition();
                BeanUtils.copyProperties(condition, newCondition);
                newCondition.setId(null);
                newCondition.setTemplateId(newTemplate.getId());
                newCondition.setCreateTime(LocalDateTime.now());
                newCondition.setUpdateTime(LocalDateTime.now());
                return newCondition;
            }).collect(Collectors.toList());
            
            workflowConditionMapper.insertBatch(newConditions);
        }
        
        return getTemplateDetail(newTemplate.getId());
    }
}