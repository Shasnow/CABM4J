package com.github.shasnow.cabm4j.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.shasnow.cabm4j.entity.ChatRequestBody;
import com.github.shasnow.cabm4j.entity.Message;
import com.github.shasnow.cabm4j.util.APIUtil;
import com.github.shasnow.cabm4j.util.PropertiesManager;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ConfigService configService;
    private final WebClient webClient;
    private final CharacterService characterService;
    private final MessageService messageService;
    private final ConversationService conversationService;

    public ChatService(ConfigService configService, CharacterService characterService, MessageService messageService, ConversationService conversationService) {
        this.configService = configService;
        this.characterService = characterService;
        this.messageService = messageService;
        this.conversationService = conversationService;
        webClient = WebClient.create(configService.getChatApiUrl());
    }

    public Flux<String> chatCompletionsStream(String message) {
        // 1. 准备消息
        Message systemMessage = new Message("system", characterService.getCurrentCharacter().getPrompt());
        systemMessage.addContent("以下是与用户的近10次对话记录: "+conversationService.getLatestConversationsMessagesString(10,characterService.getCurrentCharacterId()));
        Message chatMessage = new Message("user", message);
        int messageId = messageService.saveMessage(chatMessage); // 保存用户消息

        // 2. 构造请求体
        ChatRequestBody requestBody = new ChatRequestBody(
                PropertiesManager.getProperty("CHAT_MODEL"),
                List.of(systemMessage, chatMessage),
                true,
                4096,
                1.0f,
                5
        );
        logger.info("Posting chat completion request: {}", requestBody.toMap());

        // 3. 使用 AtomicReference 替代 StringBuilder（线程安全）
        AtomicReference<String> responseBuffer = new AtomicReference<>("");

        // 4. 发起 WebClient 请求
        return webClient.post()
                .header("content-type", "application/json")
                .header("Authorization", "Bearer " + configService.getChatApiKey())
                .bodyValue(requestBody.toMap())
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> {
                    // 跳过 [DONE] 标记（如果是 OpenAI 兼容的流式响应）
                    if ("[DONE]".equals(chunk)) {
                        String fullResponse = responseBuffer.get().replace("\n", ""); // 去除换行符
                        int responseMessageId=messageService.saveMessage(new Message("assistant", fullResponse));
                        conversationService.saveConversation(messageId,responseMessageId, characterService.getCurrentCharacterId());
                        logger.info("Conversation saved.");
                        return;
                    }
                    // 解析 JSON 并提取 delta 内容
                    try {
                        JSONObject jsonChunk = JSON.parseObject(chunk);
                        JSONArray choices = jsonChunk.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                            if (delta.containsKey("content")) {
                                String content = delta.getString("content");
                                responseBuffer.updateAndGet(prev -> prev + content);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse chunk: {}", chunk, e);
                    }
                })
                .doOnComplete(() -> {
                    // 流结束时保存完整响应（异步非阻塞）
                    logger.info("Chat completion stream completed. Full response: {}", responseBuffer.get().replace("\n", ""));
                })
                .doOnError(err -> {
                    // 记录错误并更新消息状态
                    logger.error("Error in chat completion stream: ", err);
                })
                .doOnCancel(() -> {
                    // 客户端取消订阅时更新状态
                    logger.warn("Client cancelled the stream for messages ID: {}", messageId);
                });
    }

    public JSONObject chatCompletions(String message) {
        Message messageObj = new Message("user", message);
        Header[] headers = new Header[]{
                new BasicHeader("content-type", "application/json"),
                new BasicHeader("Authorization", "Bearer " + configService.getChatApiKey())
        };
        ChatRequestBody requestBody = new ChatRequestBody(
                PropertiesManager.getProperty("CHAT_MODEL"),
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
