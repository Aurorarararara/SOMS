package com.office.admin.controller;

import com.office.admin.entity.Announcement;
import com.office.admin.mapper.AnnouncementMapper;
import com.office.admin.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端公告控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/admin/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 获取最近公告列表
     */
    @GetMapping("/recent")
    public Result<List<Announcement>> getRecentAnnouncements(
            @RequestParam(defaultValue = "5") int limit) {
        List<Announcement> announcements = announcementMapper.findLatestPublished(limit);
        return Result.success(announcements);
    }
}