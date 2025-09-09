package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.employee.entity.Announcement;
import com.office.employee.mapper.AnnouncementMapper;
import com.office.employee.common.BusinessException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告服务类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 分页查询已发布的公告
     */
    public IPage<Announcement> getPublishedAnnouncements(int current, int size) {
        Page<Announcement> page = new Page<>(current, size);
        return announcementMapper.selectPublishedAnnouncementsPage(page);
    }

    /**
     * 获取公告详情
     */
    public Announcement getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        if (announcement.getStatus() != 1) {
            throw new BusinessException("公告未发布");
        }

        return announcement;
    }

    /**
     * 获取置顶公告列表
     */
    public List<Announcement> getTopAnnouncements() {
        return announcementMapper.findTopAnnouncements();
    }

    /**
     * 获取最新公告列表
     */
    public List<Announcement> getLatestAnnouncements(int limit) {
        return announcementMapper.findLatestAnnouncements(limit);
    }

    /**
     * 根据类型查询公告
     */
    public List<Announcement> getAnnouncementsByType(String type) {
        return announcementMapper.findByType(type);
    }

    /**
     * 搜索公告
     */
    public List<Announcement> searchAnnouncements(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException("搜索关键词不能为空");
        }
        return announcementMapper.searchAnnouncements(keyword.trim());
    }

    /**
     * 获取紧急公告
     */
    public List<Announcement> getUrgentAnnouncements() {
        return getAnnouncementsByType("urgent");
    }

    /**
     * 获取普通公告
     */
    public List<Announcement> getNormalAnnouncements() {
        return getAnnouncementsByType("normal");
    }

    /**
     * 统计已发布公告数量
     */
    public int getPublishedAnnouncementCount() {
        return announcementMapper.countPublishedAnnouncements();
    }

    /**
     * 获取首页公告摘要（置顶+最新）
     */
    public AnnouncementSummary getHomePageSummary() {
        List<Announcement> topAnnouncements = getTopAnnouncements();
        List<Announcement> latestAnnouncements = getLatestAnnouncements(5);
        
        AnnouncementSummary summary = new AnnouncementSummary();
        summary.setTopAnnouncements(topAnnouncements);
        summary.setLatestAnnouncements(latestAnnouncements);
        summary.setTotalCount(getPublishedAnnouncementCount());
        
        return summary;
    }

    /**
     * 公告摘要内部类
     */
    public static class AnnouncementSummary {
        private List<Announcement> topAnnouncements;
        private List<Announcement> latestAnnouncements;
        private int totalCount;

        // Getter and Setter methods
        public List<Announcement> getTopAnnouncements() {
            return topAnnouncements;
        }

        public void setTopAnnouncements(List<Announcement> topAnnouncements) {
            this.topAnnouncements = topAnnouncements;
        }

        public List<Announcement> getLatestAnnouncements() {
            return latestAnnouncements;
        }

        public void setLatestAnnouncements(List<Announcement> latestAnnouncements) {
            this.latestAnnouncements = latestAnnouncements;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
    }
}