package com.office.employee.dto;

import java.time.LocalDateTime;
import java.util.List;

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
    private Integer actualHours;

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getIsUrgent() {
        return isUrgent;
    }

    public void setIsUrgent(Boolean isUrgent) {
        this.isUrgent = isUrgent;
    }

    public Boolean getAllowReassign() {
        return allowReassign;
    }

    public void setAllowReassign(Boolean allowReassign) {
        this.allowReassign = allowReassign;
    }

    public Boolean getNotifyOnUpdate() {
        return notifyOnUpdate;
    }

    public void setNotifyOnUpdate(Boolean notifyOnUpdate) {
        this.notifyOnUpdate = notifyOnUpdate;
    }

    public Integer getActualHours() {
        return actualHours;
    }

    public void setActualHours(Integer actualHours) {
        this.actualHours = actualHours;
    }
}
