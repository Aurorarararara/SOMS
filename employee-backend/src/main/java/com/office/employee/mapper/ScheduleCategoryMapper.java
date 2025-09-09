package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.ScheduleCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 日程分类Mapper接口
 */
@Mapper
public interface ScheduleCategoryMapper extends BaseMapper<ScheduleCategory> {
    
    /**
     * 获取用户可用的分类列表(包括系统分类和用户自定义分类)
     */
    @Select("""
        SELECT * FROM schedule_categories
        WHERE (is_system = TRUE OR user_id = #{userId})
        AND deleted = FALSE
        ORDER BY is_system DESC, sort_order ASC, created_time ASC
    """)
    List<ScheduleCategory> getUserAvailableCategories(@Param("userId") Long userId);
    
    /**
     * 获取分类使用统计
     */
    @Select("""
        SELECT 
            sc.id,
            sc.name,
            sc.color,
            sc.icon,
            COUNT(s.id) as schedule_count,
            COUNT(CASE WHEN s.status = 'SCHEDULED' THEN 1 END) as scheduled_count,
            COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) as completed_count
        FROM schedule_categories sc
        LEFT JOIN schedules s ON sc.id = s.category_id AND s.user_id = #{userId} AND s.deleted = FALSE
        WHERE (sc.is_system = TRUE OR sc.user_id = #{userId})
        AND sc.deleted = FALSE
        GROUP BY sc.id, sc.name, sc.color, sc.icon
        ORDER BY sc.is_system DESC, sc.sort_order ASC
    """)
    List<Map<String, Object>> getCategoryStatistics(@Param("userId") Long userId);
}