package com.walterke.iotai.controller;

import com.walterke.iotai.common.ChatReqDTO;
import com.walterke.iotai.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatReqDTO chatReqDTO) {
        log.info("请求：{}",chatReqDTO);
        return chatService.sendChatRequest(chatReqDTO);
    }
}
