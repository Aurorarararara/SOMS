package com.office.employee.controller;

import com.office.employee.common.Result;
import com.office.employee.dto.AiChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient chatClient;

    public AiController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/chat")
    public Result<String> chat(@Validated @RequestBody AiChatRequest request) {
        // 简化为使用默认配置的模型与温度
        String reply = this.chatClient
                .prompt()
                .user(request.getMessage())
                .call()
                .content();
        return Result.success(reply);
    }
}