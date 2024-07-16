package com.walterke.iotai.service;


import com.walterke.iotai.common.ChatReqDTO;
import com.walterke.iotai.properties.FastGPTProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ChatService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FastGPTProperties properties;

    //本地fastgpt接口
    private String url;

    //本地fastgpt key
    private String apikey;

    public String sendChatRequest(ChatReqDTO chatRequest) {
        url = properties.getUrl();
        apikey = properties.getApikey();
        log.info("请求接口{}", url);
        log.info("apikey:{}", apikey);

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", apikey);
        headers.set("Content-Type", "application/json");

        HttpEntity<ChatReqDTO> requestEntity = new HttpEntity<>(chatRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        log.info("响应：{}", response);
        return response.getBody();
    }
}
