package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.WorkflowNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批节点Mapper接口
 */
@Mapper
public interface WorkflowNodeMapper extends BaseMapper<WorkflowNode> {
    
    /**
     * 根据模板ID查询节点列表
     */
    @Select("SELECT * FROM workflow_nodes WHERE template_id = #{templateId} ORDER BY node_order ASC")
    List<WorkflowNode> findByTemplateId(@Param("templateId") Long templateId);
    
    /**
     * 根据模板ID和节点顺序查询节点
     */
    @Select("SELECT * FROM workflow_nodes WHERE template_id = #{templateId} AND node_order = #{nodeOrder} LIMIT 1")
    WorkflowNode findByTemplateIdAndOrder(@Param("templateId") Long templateId, @Param("nodeOrder") Integer nodeOrder);
    
    /**
     * 查询下一个节点
     */
    @Select("SELECT * FROM workflow_nodes WHERE template_id = #{templateId} AND node_order > #{currentOrder} ORDER BY node_order ASC LIMIT 1")
    WorkflowNode findNextNode(@Param("templateId") Long templateId, @Param("currentOrder") Integer currentOrder);
    
    /**
     * 删除模板相关的所有节点
     */
    @Select("DELETE FROM workflow_nodes WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);
    
    /**
     * 批量插入节点
     */
    default void insertBatch(List<WorkflowNode> nodes) {
        for (WorkflowNode node : nodes) {
            insert(node);
        }
    }
}