package com.office.employee.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.office.employee.entity.Announcement;
import com.office.employee.service.AnnouncementService;
import com.office.employee.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 分页查询已发布的公告
     */
    @GetMapping
    public Result<IPage<Announcement>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Announcement> announcements = announcementService.getPublishedAnnouncements(current, size);
        return Result.success(announcements);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementDetail(id);
        return Result.success(announcement);
    }

    /**
     * 获取置顶公告列表
     */
    @GetMapping("/top")
    public Result<List<Announcement>> getTopAnnouncements() {
        List<Announcement> announcements = announcementService.getTopAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 获取最新公告列表
     */
    @GetMapping("/latest")
    public Result<List<Announcement>> getLatestAnnouncements(
            @RequestParam(defaultValue = "10") int limit) {
        List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
        return Result.success(announcements);
    }

    /**
     * 获取最近公告列表（与latest相同，为前端兼容性提供）
     */
    @GetMapping("/recent")
    public Result<List<Announcement>> getRecentAnnouncements(
            @RequestParam(defaultValue = "5") int limit) {
        List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
        return Result.success(announcements);
    }

    /**
     * 根据类型查询公告
     */
    @GetMapping("/type/{type}")
    public Result<List<Announcement>> getAnnouncementsByType(@PathVariable String type) {
        List<Announcement> announcements = announcementService.getAnnouncementsByType(type);
        return Result.success(announcements);
    }

    /**
     * 搜索公告
     */
    @GetMapping("/search")
    public Result<List<Announcement>> searchAnnouncements(@RequestParam String keyword) {
        List<Announcement> announcements = announcementService.searchAnnouncements(keyword);
        return Result.success(announcements);
    }

    /**
     * 获取紧急公告
     */
    @GetMapping("/urgent")
    public Result<List<Announcement>> getUrgentAnnouncements() {
        List<Announcement> announcements = announcementService.getUrgentAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 获取首页公告摘要
     */
    @GetMapping("/summary")
    public Result<AnnouncementService.AnnouncementSummary> getHomePageSummary() {
        AnnouncementService.AnnouncementSummary summary = announcementService.getHomePageSummary();
        return Result.success(summary);
    }
}