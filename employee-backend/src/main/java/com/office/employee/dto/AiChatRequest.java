package com.office.employee.dto;

import jakarta.validation.constraints.NotBlank;

public class AiChatRequest {

    @NotBlank(message = "消息内容不能为空")
    private String message;

    // 可选：允许客户端覆盖模型或温度
    private String model;
    private Float temperature;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }
}