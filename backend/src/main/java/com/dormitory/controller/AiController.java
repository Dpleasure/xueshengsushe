package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.service.DeepSeekService;
import com.dormitory.service.DeepSeekService.DeepSeekException;
import lombok.Data;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final DeepSeekService deepSeekService;

    public AiController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping("/chat")
    public RestBean<?> chat(@RequestBody ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            return RestBean.failure(400, "请输入要咨询的问题。");
        }

        String message = request.getMessage().trim();
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return RestBean.failure(400, "问题不能超过 2000 个字符。");
        }

        try {
            return RestBean.success(deepSeekService.chat(message));
        } catch (DeepSeekException e) {
            return RestBean.failure(500, e.getMessage());
        }
    }

    @Data
    public static class ChatRequest {
        private String message;
    }
}
