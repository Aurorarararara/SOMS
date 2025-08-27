package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.TaskComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskCommentMapper extends BaseMapper<TaskComment> {
    
    /**
     * 根据父评论ID查询回复
     */
    @Select("SELECT * FROM task_comments WHERE parent_id = #{parentId} " +
            "AND is_deleted = false ORDER BY create_time ASC")
    List<TaskComment> selectRepliesByParentId(@Param("parentId") Long parentId);
}