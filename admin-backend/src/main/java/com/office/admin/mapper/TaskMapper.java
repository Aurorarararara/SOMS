package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
    /**
     * 复杂查询任务
     */
    IPage<Task> selectTasksWithConditions(Page<Task> page, 
                                        @Param("title") String title,
                                        @Param("status") String status,
                                        @Param("priority") String priority,
                                        @Param("assigneeId") Long assigneeId,
                                        @Param("creatorId") Long creatorId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询任务统计数据
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending, " +
            "COUNT(CASE WHEN status = 'processing' THEN 1 END) as processing, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed, " +
            "COUNT(CASE WHEN status = 'overdue' THEN 1 END) as overdue " +
            "FROM tasks WHERE assignee_id = #{userId}")
    Map<String, Integer> selectTaskStatsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户分配的任务
     */
    @Select("SELECT * FROM tasks WHERE assignee_id = #{userId} " +
            "ORDER BY is_urgent DESC, due_date ASC")
    List<Task> selectByAssigneeId(@Param("userId") Long userId);
    
    /**
     * 查询用户创建的任务
     */
    @Select("SELECT * FROM tasks WHERE creator_id = #{userId} " +
            "ORDER BY create_time DESC")
    List<Task> selectByCreatorId(@Param("userId") Long userId);
    
    /**
     * 查询即将到期的任务
     */
    @Select("SELECT * FROM tasks WHERE status IN ('pending', 'processing') " +
            "AND due_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY due_date ASC")
    List<Task> selectUpcomingTasks(@Param("days") Integer days);
    
    /**
     * 查询逾期任务
     */
    @Select("SELECT * FROM tasks WHERE status IN ('pending', 'processing') " +
            "AND due_date < NOW() ORDER BY due_date ASC")
    List<Task> selectOverdueTasks();
}