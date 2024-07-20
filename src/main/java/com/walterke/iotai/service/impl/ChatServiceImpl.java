package com.walterke.iotai.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.walterke.iotai.common.ChatReqDTO;
import com.walterke.iotai.properties.FastGPTProperties;
import com.walterke.iotai.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FastGPTProperties properties;
    private ObjectMapper objectMapper;
    private OkHttpClient client;

    public ChatServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    //本地fastgpt接口
    private String url;

    //本地fastgpt key
    private String apikey;

    @Override
    public String sendChatRequest(ChatReqDTO chatRequest) {
        url = properties.getUrl();
        apikey = properties.getApikey();
        log.info("请求接口{}", url);
        //log.info("apikey:{}", apikey);

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", apikey);
        headers.set("Content-Type", "application/json");

        HttpEntity<ChatReqDTO> requestEntity = new HttpEntity<>(chatRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        log.info("响应：{}", response);
        return response.getBody();
    }

    @Override
    public ResponseBodyEmitter sendStreamChatRequest(ChatReqDTO chatRequest, ResponseBodyEmitter emitter) {
        url = properties.getUrl();
        apikey = properties.getApikey();
        log.info("请求接口{}", url);

        try {
            RequestBody body = RequestBody.create(
                    okhttp3.MediaType.get("application/json; charset=utf-8"),
                    objectMapper.writeValueAsString(chatRequest)
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", apikey)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    emitter.completeWithError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = responseBody.source().read(buffer)) != -1) {
                                String data = new String(buffer, 0, bytesRead);
                                log.info("data:{}",data);
                                emitter.send(data);
                            }
                        }
                        emitter.complete();
                    } catch (IOException ex) {
                        log.error("流式问答请求处理异常", ex);
                        emitter.completeWithError(ex);
                    }
                }
            });

            emitter.onCompletion(() -> {
                log.info("流式问答请求完成，使用模型：{}", chatRequest.getModel());
            });

            emitter.onError(throwable -> log.error("流式问答请求异常，使用模型：{}", chatRequest.getModel(), throwable));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }
}

