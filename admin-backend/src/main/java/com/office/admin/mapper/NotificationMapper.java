package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知Mapper接口
 *
 * @author office-system
 * @since 2024-08-30
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    /**
     * 分页查询用户通知
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} AND is_deleted = 0 " +
            "ORDER BY is_read ASC, priority DESC, create_time DESC")
    IPage<Notification> selectPageByReceiverId(Page<Notification> page, @Param("receiverId") Long receiverId);
    
    /**
     * 分页查询所有通知（管理员用）
     */
    @Select("SELECT n.*, u1.real_name as sender_real_name, u2.real_name as receiver_real_name " +
            "FROM notifications n " +
            "LEFT JOIN users u1 ON n.sender_id = u1.id " +
            "LEFT JOIN users u2 ON n.receiver_id = u2.id " +
            "WHERE n.is_deleted = 0 ORDER BY n.create_time DESC")
    IPage<Notification> selectAllNotificationsPage(Page<Notification> page);
    
    /**
     * 查询用户未读通知
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} AND is_read = 0 " +
            "AND is_deleted = 0 ORDER BY priority DESC, create_time DESC")
    List<Notification> selectUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notifications WHERE receiver_id = #{receiverId} " +
            "AND is_read = 0 AND is_deleted = 0")
    Integer countUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 查询用户通知统计
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "COUNT(CASE WHEN is_read = 0 THEN 1 END) as unread, " +
            "COUNT(CASE WHEN is_read = 1 THEN 1 END) as read, " +
            "COUNT(CASE WHEN priority = 'urgent' AND is_read = 0 THEN 1 END) as urgent_unread " +
            "FROM notifications WHERE receiver_id = #{receiverId} AND is_deleted = 0")
    Map<String, Integer> selectNotificationStatsByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 查询系统通知统计（管理员用）
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "COUNT(CASE WHEN is_read = 0 THEN 1 END) as unread, " +
            "COUNT(CASE WHEN notification_type = 'system_notice' THEN 1 END) as system_notices, " +
            "COUNT(CASE WHEN notification_type LIKE 'task_%' THEN 1 END) as task_notifications, " +
            "COUNT(CASE WHEN priority = 'urgent' THEN 1 END) as urgent_notifications " +
            "FROM notifications WHERE is_deleted = 0")
    Map<String, Integer> selectSystemNotificationStats();
    
    /**
     * 按通知类型查询通知
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} " +
            "AND notification_type = #{notificationType} AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    List<Notification> selectByReceiverIdAndType(@Param("receiverId") Long receiverId, 
                                                @Param("notificationType") String notificationType);
    
    /**
     * 查询任务相关通知
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} " +
            "AND related_type = 'task' AND related_id = #{taskId} AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    List<Notification> selectTaskNotifications(@Param("receiverId") Long receiverId, 
                                              @Param("taskId") Long taskId);
    
    /**
     * 查询最近的通知
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} AND is_deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<Notification> selectRecentNotifications(@Param("receiverId") Long receiverId, 
                                                @Param("limit") Integer limit);
    
    /**
     * 批量标记为已读
     */
    @Update("UPDATE notifications SET is_read = 1, read_time = NOW() " +
            "WHERE receiver_id = #{receiverId} AND id IN " +
            "<foreach collection='notificationIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    Integer batchMarkAsRead(@Param("receiverId") Long receiverId, 
                           @Param("notificationIds") List<Long> notificationIds);
    
    /**
     * 标记所有未读通知为已读
     */
    @Update("UPDATE notifications SET is_read = 1, read_time = NOW() " +
            "WHERE receiver_id = #{receiverId} AND is_read = 0 AND is_deleted = 0")
    Integer markAllAsRead(@Param("receiverId") Long receiverId);
    
    /**
     * 批量删除通知（软删除）
     */
    @Update("UPDATE notifications SET is_deleted = 1 " +
            "WHERE receiver_id = #{receiverId} AND id IN " +
            "<foreach collection='notificationIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    Integer batchDelete(@Param("receiverId") Long receiverId, 
                       @Param("notificationIds") List<Long> notificationIds);
    
    /**
     * 管理员批量删除通知
     */
    @Update("UPDATE notifications SET is_deleted = 1 WHERE id IN " +
            "<foreach collection='notificationIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    Integer adminBatchDelete(@Param("notificationIds") List<Long> notificationIds);
    
    /**
     * 清理过期通知（物理删除）
     */
    @Delete("DELETE FROM notifications WHERE is_deleted = 1 " +
            "AND create_time < #{expireTime}")
    Integer deleteExpiredNotifications(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 查询系统通知
     */
    @Select("SELECT * FROM notifications WHERE notification_type = 'system_notice' " +
            "AND receiver_id = #{receiverId} AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    List<Notification> selectSystemNotices(@Param("receiverId") Long receiverId);
    
    /**
     * 查询紧急通知
     */
    @Select("SELECT * FROM notifications WHERE receiver_id = #{receiverId} " +
            "AND priority = 'urgent' AND is_read = 0 AND is_deleted = 0 " +
            "ORDER BY create_time DESC")
    List<Notification> selectUrgentNotifications(@Param("receiverId") Long receiverId);
    
    /**
     * 按部门查询通知统计
     */
    @Select("SELECT d.name as department_name, " +
            "COUNT(n.id) as total_notifications, " +
            "COUNT(CASE WHEN n.is_read = 0 THEN 1 END) as unread_count " +
            "FROM notifications n " +
            "JOIN users u ON n.receiver_id = u.id " +
            "JOIN departments d ON u.department_id = d.id " +
            "WHERE n.is_deleted = 0 " +
            "GROUP BY d.id, d.name " +
            "ORDER BY total_notifications DESC")
    List<Map<String, Object>> selectNotificationStatsByDepartment();
    
    /**
     * 复杂条件查询通知
     */
    IPage<Notification> selectNotificationsWithConditions(Page<Notification> page,
                                                         @Param("receiverId") Long receiverId,
                                                         @Param("notificationType") String notificationType,
                                                         @Param("priority") String priority,
                                                         @Param("isRead") Boolean isRead,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}
