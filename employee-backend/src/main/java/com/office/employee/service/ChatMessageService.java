package com.office.employee.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.office.employee.entity.ChatMessage;

public interface ChatMessageService extends IService<ChatMessage> {
    
    /**
     * 保存聊天消息
     * @param chatMessage 聊天消息实体
     * @return 是否保存成功
     */
    boolean saveMessage(ChatMessage chatMessage);
    
    /**
     * 根据房间ID获取聊天记录
     * @param roomId 房间ID
     * @param page 页码
     * @param size 每页大小
     * @return 聊天记录分页列表
     */
    IPage<ChatMessage> getMessagesByRoomId(String roomId, int page, int size);
    
    /**
     * 根据房间ID获取最新的聊天记录
     * @param roomId 房间ID
     * @param limit 限制数量
     * @return 聊天记录列表
     */
    java.util.List<ChatMessage> getLatestMessagesByRoomId(String roomId, int limit);
}