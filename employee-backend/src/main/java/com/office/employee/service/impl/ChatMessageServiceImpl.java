package com.office.employee.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.employee.entity.ChatMessage;
import com.office.employee.mapper.ChatMessageMapper;
import com.office.employee.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    
    private final ChatMessageMapper chatMessageMapper;
    
    @Override
    public boolean saveMessage(ChatMessage chatMessage) {
        return this.save(chatMessage);
    }
    
    @Override
    public IPage<ChatMessage> getMessagesByRoomId(String roomId, int page, int size) {
        Page<ChatMessage> pageObj = new Page<>(page, size);
        return chatMessageMapper.selectByRoomId(pageObj, roomId);
    }
    
    @Override
    public List<ChatMessage> getLatestMessagesByRoomId(String roomId, int limit) {
        return chatMessageMapper.selectLatestByRoomId(roomId, limit);
    }
}