package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {
    
    /**
     * 查询任务的所有评论（包括回复）
     */
    @Select("SELECT * FROM task_comments WHERE task_id = #{taskId} AND is_deleted = 0 " +
            "ORDER BY create_time ASC")
    List<TaskComment> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 查询顶级评论
     */
    @Select("SELECT * FROM task_comments WHERE task_id = #{taskId} AND parent_id IS NULL " +
            "AND is_deleted = 0 ORDER BY create_time DESC")
    List<TaskComment> selectTopLevelComments(@Param("taskId") Long taskId);
    
    /**
     * 查询评论的回复
     */
    @Select("SELECT * FROM task_comments WHERE parent_id = #{parentId} AND is_deleted = 0 " +
            "ORDER BY create_time ASC")
    List<TaskComment> selectRepliesByParentId(@Param("parentId") Long parentId);
}