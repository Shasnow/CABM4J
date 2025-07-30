package com.github.shasnow.cabm4j.service;

import com.alibaba.fastjson2.JSONObject;
import com.github.shasnow.cabm4j.entity.ChatRequestBody;
import com.github.shasnow.cabm4j.entity.Message;
import com.github.shasnow.cabm4j.util.APIUtil;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


@Service
public class ChatService {
    private final ConfigService configService;
    private final WebClient webClient;

    public ChatService(ConfigService configService) {
        this.configService = configService;
        webClient= WebClient.create(configService.getChatApiUrl());
    }
    public Flux<String> chatCompletionsStream(String message){
        Message messageObj=new Message("user",message);
        ChatRequestBody requestBody=new ChatRequestBody(
                System.getProperty("CHAT_MODEL"),
                messageObj,
                true,
                4096,
                1.0f,
                5
        );
        return webClient.post()
                .header("content-type", "application/json")
                .header("Authorization", "Bearer " + configService.getChatApiKey())
                .bodyValue(requestBody.toMap())
                .retrieve()
                .bodyToFlux(String.class);
    }

    public JSONObject chatCompletions(String message){
        Message messageObj=new Message("user",message);
        Header[] headers=new Header[]{
                new BasicHeader("content-type", "application/json"),
                new BasicHeader("Authorization","Bearer "+ configService.getChatApiKey())
        };
        ChatRequestBody requestBody=new ChatRequestBody(
                System.getProperty("CHAT_MODEL"),
                messageObj,
                false,
                4096,
                1.0f,
                5
        );
        String url = configService.getChatApiUrl();
        try {
            return APIUtil.makeApiRequest(url, "POST", headers, null, requestBody.toMap(), true, 30, 3, 1).data();
        } catch (Exception e) {
            throw new RuntimeException("Error during chat completion: " + e.getMessage(), e);
        }
    }
}
