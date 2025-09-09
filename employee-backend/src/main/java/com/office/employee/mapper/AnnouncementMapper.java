package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公告Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 分页查询已发布的公告（置顶在前）
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 ORDER BY a.is_top DESC, a.publish_time DESC")
    IPage<Announcement> selectPublishedAnnouncementsPage(Page<Announcement> page);

    /**
     * 查询最新的置顶公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 AND a.is_top = 1 ORDER BY a.publish_time DESC LIMIT 5")
    List<Announcement> findTopAnnouncements();

    /**
     * 查询最新的普通公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 ORDER BY a.publish_time DESC LIMIT #{limit}")
    List<Announcement> findLatestAnnouncements(@Param("limit") int limit);

    /**
     * 根据类型查询公告
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 AND a.type = #{type} ORDER BY a.is_top DESC, a.publish_time DESC")
    List<Announcement> findByType(@Param("type") String type);

    /**
     * 搜索公告（按标题和内容）
     */
    @Select("SELECT a.*, u.real_name as publisher_name FROM announcements a " +
            "LEFT JOIN users u ON a.publisher_id = u.id " +
            "WHERE a.status = 1 AND (a.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR a.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY a.is_top DESC, a.publish_time DESC")
    List<Announcement> searchAnnouncements(@Param("keyword") String keyword);

    /**
     * 统计已发布公告数量
     */
    @Select("SELECT COUNT(*) FROM announcements WHERE status = 1")
    int countPublishedAnnouncements();
}