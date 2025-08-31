package com.office.employee.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskUpdateRequest {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private Long assigneeId;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private Integer progress;
    private List<String> tags;
    private Boolean isUrgent;
    private Boolean allowReassign;
    private Boolean notifyOnUpdate;
    private Integer estimatedHours;
    private Integer actualHours;
}
