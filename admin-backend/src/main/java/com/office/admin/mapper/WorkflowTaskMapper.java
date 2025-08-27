package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.WorkflowTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批任务Mapper接口
 */
@Mapper
public interface WorkflowTaskMapper extends BaseMapper<WorkflowTask> {
    
    /**
     * 根据流程实例ID查询任务列表
     */
    @Select("SELECT * FROM workflow_tasks WHERE instance_id = #{instanceId} ORDER BY create_time ASC")
    List<WorkflowTask> findByInstanceId(@Param("instanceId") Long instanceId);
    
    /**
     * 根据审批人ID查询待处理任务
     */
    @Select("SELECT * FROM workflow_tasks WHERE assignee_id = #{assigneeId} AND task_status = 'pending' ORDER BY create_time DESC")
    List<WorkflowTask> findPendingTasksByAssignee(@Param("assigneeId") Long assigneeId);
    
    /**
     * 根据审批人ID查询所有任务（包括已处理）
     */
    @Select("SELECT * FROM workflow_tasks WHERE assignee_id = #{assigneeId} ORDER BY create_time DESC")
    List<WorkflowTask> findTasksByAssignee(@Param("assigneeId") Long assigneeId);
    
    /**
     * 根据节点ID查询当前活跃任务
     */
    @Select("SELECT * FROM workflow_tasks WHERE node_id = #{nodeId} AND task_status = 'pending'")
    List<WorkflowTask> findActiveTasksByNodeId(@Param("nodeId") Long nodeId);
    
    /**
     * 根据实例ID和节点ID查询任务
     */
    @Select("SELECT * FROM workflow_tasks WHERE instance_id = #{instanceId} AND node_id = #{nodeId}")
    List<WorkflowTask> findByInstanceIdAndNodeId(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);
    
    /**
     * 生成任务编号
     */
    @Select("SELECT CONCAT('TK', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(IFNULL(MAX(CAST(SUBSTRING(task_no, 11) AS UNSIGNED)), 0) + 1, 4, '0')) " +
            "FROM workflow_tasks WHERE DATE(create_time) = CURDATE()")
    String generateTaskNo();
}