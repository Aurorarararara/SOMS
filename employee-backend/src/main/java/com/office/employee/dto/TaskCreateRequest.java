package com.office.employee.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskCreateRequest {
    private String title;
    private String description;
    private String priority;
    private Long assigneeId;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private List<String> tags;
    private Boolean isUrgent;
    private Boolean allowReassign;
    private Boolean notifyOnUpdate;
    private Integer estimatedHours;
}

