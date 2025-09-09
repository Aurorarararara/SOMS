package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.WorkflowHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批历史Mapper接口
 */
@Mapper
public interface WorkflowHistoryMapper extends BaseMapper<WorkflowHistory> {
    
    /**
     * 根据流程实例ID查询历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE instance_id = #{instanceId} ORDER BY action_time ASC")
    List<WorkflowHistory> findByInstanceId(@Param("instanceId") Long instanceId);
    
    /**
     * 根据任务ID查询历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE task_id = #{taskId} ORDER BY action_time ASC")
    List<WorkflowHistory> findByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 根据节点ID查询历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE node_id = #{nodeId} ORDER BY action_time DESC")
    List<WorkflowHistory> findByNodeId(@Param("nodeId") Long nodeId);
    
    /**
     * 根据操作人查询历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE operator_id = #{operatorId} ORDER BY action_time DESC")
    List<WorkflowHistory> findByOperatorId(@Param("operatorId") Long operatorId);
    
    /**
     * 根据操作类型查询历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE action_type = #{actionType} ORDER BY action_time DESC")
    List<WorkflowHistory> findByActionType(@Param("actionType") String actionType);
}