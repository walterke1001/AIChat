package com.walterke.iotai.controller;

import com.walterke.iotai.common.ChatReqDTO;
import com.walterke.iotai.service.impl.ChatServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private ChatServiceImpl chatService;
    ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);

    @PostMapping("/chat")
    public String chat(@RequestBody ChatReqDTO chatReqDTO) {
        log.info("请求：{}", chatReqDTO);
        return chatService.sendChatRequest(chatReqDTO);
    }

    @PostMapping("/chat/stream")
    public ResponseBodyEmitter chatStream(@RequestBody ChatReqDTO chatReqDTO, HttpServletResponse response) {
        log.info("请求：{}", chatReqDTO);
        
        // 基础配置；流式输出、编码、禁用缓存
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        
        //异步响应对象
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        
        return chatService.sendStreamChatRequest(chatReqDTO,emitter);
    }
}
