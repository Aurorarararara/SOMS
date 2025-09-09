package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知数据访问层
 * 提供通知相关的数据库操作
 *
 * @author office-system
 * @since 2025-01-01
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询用户通知
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param type 通知类型（可选）
     * @param isRead 是否已读（可选）
     * @return 通知分页列表
     */
    @Select("<script>" +
            "SELECT * FROM notifications " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "<if test='type != null and type != \"\"'>" +
            "AND type = #{type} " +
            "</if>" +
            "<if test='isRead != null'>" +
            "AND is_read = #{isRead} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<Notification> selectUserNotifications(Page<Notification> page, 
                                               @Param("userId") Long userId,
                                               @Param("type") String type,
                                               @Param("isRead") Boolean isRead);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    int selectUnreadCount(@Param("userId") Long userId);

    /**
     * 获取用户各类型通知统计
     *
     * @param userId 用户ID
     * @return 统计结果
     */
    @Select("SELECT type, COUNT(*) as count, " +
            "SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) as unread_count " +
            "FROM notifications " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "GROUP BY type")
    List<Map<String, Object>> selectNotificationStatsByType(@Param("userId") Long userId);

    /**
     * 获取用户最近通知
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近通知列表
     */
    @Select("SELECT * FROM notifications " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<Notification> selectRecentNotifications(@Param("userId") Long userId, 
                                                @Param("limit") int limit);

    /**
     * 批量标记通知为已读
     *
     * @param notificationIds 通知ID列表
     * @param userId 用户ID
     * @param readTime 阅读时间
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE notifications SET is_read = 1, read_time = #{readTime} " +
            "WHERE user_id = #{userId} AND deleted = 0 AND is_read = 0 " +
            "AND id IN " +
            "<foreach collection='notificationIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchMarkAsRead(@Param("notificationIds") List<Long> notificationIds,
                       @Param("userId") Long userId,
                       @Param("readTime") LocalDateTime readTime);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @param readTime 阅读时间
     * @return 更新数量
     */
    @Update("UPDATE notifications SET is_read = 1, read_time = #{readTime} " +
            "WHERE user_id = #{userId} AND deleted = 0 AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId, 
                     @Param("readTime") LocalDateTime readTime);

    /**
     * 删除过期通知
     *
     * @param expireTime 过期时间
     * @return 删除数量
     */
    @Update("UPDATE notifications SET deleted = 1 " +
            "WHERE (expire_time IS NOT NULL AND expire_time < #{expireTime}) " +
            "OR (expire_time IS NULL AND create_time < #{expireTime}) " +
            "AND deleted = 0")
    int deleteExpiredNotifications(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 获取系统通知统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as read_count, " +
            "SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) as unread_count, " +
            "COUNT(DISTINCT user_id) as user_count " +
            "FROM notifications " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0")
    Map<String, Object> selectSystemNotificationStats(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取热门通知类型
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 热门通知类型列表
     */
    @Select("SELECT type, COUNT(*) as count " +
            "FROM notifications " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 " +
            "GROUP BY type " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectPopularNotificationTypes(@Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            @Param("limit") int limit);

    /**
     * 获取用户通知活跃度
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 活跃度数据
     */
    @Select("SELECT " +
            "DATE(create_time) as date, " +
            "COUNT(*) as notification_count, " +
            "SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as read_count " +
            "FROM notifications " +
            "WHERE user_id = #{userId} " +
            "AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> selectUserNotificationActivity(@Param("userId") Long userId,
                                                            @Param("days") int days);

    /**
     * 清理用户已读的旧通知
     *
     * @param userId 用户ID
     * @param beforeTime 时间界限
     * @param keepCount 保留数量
     * @return 清理数量
     */
    @Update("UPDATE notifications SET deleted = 1 " +
            "WHERE user_id = #{userId} " +
            "AND is_read = 1 " +
            "AND create_time < #{beforeTime} " +
            "AND deleted = 0 " +
            "AND id NOT IN (" +
            "  SELECT id FROM (" +
            "    SELECT id FROM notifications " +
            "    WHERE user_id = #{userId} AND is_read = 1 AND deleted = 0 " +
            "    ORDER BY create_time DESC " +
            "    LIMIT #{keepCount}" +
            "  ) t" +
            ")")
    int cleanOldReadNotifications(@Param("userId") Long userId,
                                 @Param("beforeTime") LocalDateTime beforeTime,
                                 @Param("keepCount") int keepCount);
}