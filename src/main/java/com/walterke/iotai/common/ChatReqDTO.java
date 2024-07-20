package com.walterke.iotai.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ChatReqDTO implements Serializable {
    private String model;
    private String chatId;
    private boolean stream = true;
    private boolean detail;
    private Map<String, String> variables;
    private List<Message> messages;
    @Data
    @NoArgsConstructor
    public static class Message {
        private String content;
        private String role;
    }
}
