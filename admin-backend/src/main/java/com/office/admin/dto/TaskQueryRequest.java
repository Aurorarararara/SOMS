package com.office.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskQueryRequest {
    private String title;
    private String status;
    private String priority;
    private Long assigneeId;
    private Long creatorId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> tags;
    private Integer page = 1;
    private Integer size = 20;
}
