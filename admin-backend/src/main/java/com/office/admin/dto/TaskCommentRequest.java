package com.office.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskCommentRequest {
    private Long taskId;
    private String content;
    private String commentType; // comment, annotation
    private Long parentId;
    private List<String> attachments;
    private List<Long> mentions;
}
