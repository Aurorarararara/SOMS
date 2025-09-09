package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.WorkflowCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批流程条件Mapper接口
 */
@Mapper
public interface WorkflowConditionMapper extends BaseMapper<WorkflowCondition> {
    
    /**
     * 根据模板ID查询条件列表
     */
    @Select("SELECT * FROM workflow_conditions WHERE template_id = #{templateId} AND is_active = true ORDER BY sort_order ASC")
    List<WorkflowCondition> findByTemplateId(@Param("templateId") Long templateId);
    
    /**
     * 根据节点ID查询条件列表
     */
    @Select("SELECT * FROM workflow_conditions WHERE node_id = #{nodeId} AND is_active = true ORDER BY sort_order ASC")
    List<WorkflowCondition> findByNodeId(@Param("nodeId") Long nodeId);
    
    /**
     * 根据模板ID和条件类型查询条件
     */
    @Select("SELECT * FROM workflow_conditions WHERE template_id = #{templateId} AND condition_type = #{conditionType} AND is_active = true ORDER BY sort_order ASC")
    List<WorkflowCondition> findByTemplateIdAndType(@Param("templateId") Long templateId, @Param("conditionType") String conditionType);
    
    /**
     * 批量插入条件
     */
    default void insertBatch(List<WorkflowCondition> conditions) {
        for (WorkflowCondition condition : conditions) {
            insert(condition);
        }
    }
    
    /**
     * 删除模板相关的所有条件
     */
    @Select("DELETE FROM workflow_conditions WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);
}