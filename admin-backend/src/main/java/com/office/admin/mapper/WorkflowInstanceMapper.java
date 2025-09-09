package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.WorkflowInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审批流程实例Mapper接口
 */
@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {
    
    /**
     * 根据业务键查询流程实例
     */
    @Select("SELECT * FROM workflow_instances WHERE business_key = #{businessKey} AND business_type = #{businessType} LIMIT 1")
    WorkflowInstance findByBusinessKey(@Param("businessKey") String businessKey, @Param("businessType") String businessType);
    
    /**
     * 根据申请人查询流程实例列表
     */
    @Select("SELECT * FROM workflow_instances WHERE applicant_id = #{applicantId} ORDER BY create_time DESC")
    List<WorkflowInstance> findByApplicantId(@Param("applicantId") Long applicantId);
    
    /**
     * 查询运行中的流程实例
     */
    @Select("SELECT * FROM workflow_instances WHERE instance_status = 'running' ORDER BY create_time DESC")
    List<WorkflowInstance> findRunningInstances();
    
    /**
     * 根据模板ID查询流程实例
     */
    @Select("SELECT * FROM workflow_instances WHERE template_id = #{templateId} ORDER BY create_time DESC")
    List<WorkflowInstance> findByTemplateId(@Param("templateId") Long templateId);
    
    /**
     * 生成流程实例编号
     */
    @Select("SELECT CONCAT('WF', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(IFNULL(MAX(CAST(SUBSTRING(instance_no, 11) AS UNSIGNED)), 0) + 1, 4, '0')) " +
            "FROM workflow_instances WHERE DATE(create_time) = CURDATE()")
    String generateInstanceNo();
}