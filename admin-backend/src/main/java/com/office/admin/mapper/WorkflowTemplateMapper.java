package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.WorkflowTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批流程模板Mapper接口
 */
@Mapper
public interface WorkflowTemplateMapper extends BaseMapper<WorkflowTemplate> {
    
    /**
     * 根据业务类型查询可用的流程模板
     */
    @Select("SELECT * FROM workflow_templates WHERE business_type = #{businessType} AND is_active = true AND status = 'active' ORDER BY is_default DESC, sort_order ASC")
    List<WorkflowTemplate> findAvailableTemplates(@Param("businessType") String businessType);
    
    /**
     * 根据业务类型查询默认模板
     */
    @Select("SELECT * FROM workflow_templates WHERE business_type = #{businessType} AND is_active = true AND is_default = true AND status = 'active' LIMIT 1")
    WorkflowTemplate findDefaultTemplate(@Param("businessType") String businessType);
    
    /**
     * 更新模板状态
     */
    @Select("UPDATE workflow_templates SET status = #{status} WHERE id = #{templateId}")
    int updateTemplateStatus(@Param("templateId") Long templateId, @Param("status") String status);
}