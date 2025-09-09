package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 公告Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 分页查询所有公告（带发布者信息）
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "ORDER BY a.is_top DESC, a.create_time DESC")
    IPage<Announcement> selectAnnouncementPageWithDetails(Page<Announcement> page);

    /**
     * 分页查询已发布的公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 ORDER BY a.is_top DESC, a.publish_time DESC")
    IPage<Announcement> selectPublishedAnnouncementsPage(Page<Announcement> page);

    /**
     * 分页查询草稿公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 0 ORDER BY a.create_time DESC")
    IPage<Announcement> selectDraftAnnouncementsPage(Page<Announcement> page);

    /**
     * 根据发布者查询公告
     */
    @Select("SELECT * FROM announcements WHERE publisher_id = #{publisherId} ORDER BY create_time DESC")
    List<Announcement> findByPublisherId(@Param("publisherId") Long publisherId);

    /**
     * 根据类型和状态查询公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.type = #{type} AND a.status = #{status} " +
            "ORDER BY a.is_top DESC, a.publish_time DESC")
    List<Announcement> findByTypeAndStatus(@Param("type") String type, @Param("status") Integer status);

    /**
     * 搜索公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE (a.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR a.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY a.is_top DESC, a.create_time DESC")
    List<Announcement> searchAnnouncements(@Param("keyword") String keyword);

    /**
     * 获取公告统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as published, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as draft, " +
            "SUM(CASE WHEN is_top = 1 THEN 1 ELSE 0 END) as top " +
            "FROM announcements")
    Map<String, Object> getAnnouncementStatistics();

    /**
     * 按类型统计公告
     */
    @Select("SELECT type, COUNT(*) as count FROM announcements WHERE status = 1 GROUP BY type")
    List<Map<String, Object>> getAnnouncementTypeStatistics();

    /**
     * 查询最新发布的公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 ORDER BY a.publish_time DESC LIMIT #{limit}")
    List<Announcement> findLatestPublished(@Param("limit") int limit);

    /**
     * 查询置顶公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 AND a.is_top = 1 ORDER BY a.publish_time DESC")
    List<Announcement> findTopAnnouncements();

    /**
     * 批量更新公告状态
     */
    @Select("UPDATE announcements SET status = #{status} WHERE id IN (${ids})")
    int batchUpdateStatus(@Param("ids") String ids, @Param("status") Integer status);

    /**
     * 取消所有置顶
     */
    @Select("UPDATE announcements SET is_top = 0 WHERE is_top = 1")
    int cancelAllTop();
}