package com.github.shasnow.cabm4j.service;

import com.alibaba.fastjson2.JSONObject;
import com.github.shasnow.cabm4j.entity.ChatRequestBody;
import com.github.shasnow.cabm4j.entity.Message;
import com.github.shasnow.cabm4j.util.PropertiesManager;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class OptionService {
    private static final Logger logger = LoggerFactory.getLogger(OptionService.class);
    private final WebClient webClient;
    @Resource
    ConversationService conversationService;
    @Resource
    CharacterService characterService;

    public OptionService() {
        webClient = WebClient.create(PropertiesManager.getProperty("OPTION_API_URL"));
    }

    public String[] generateOptions(){
        logger.info("Generating options based on recent conversations.");
        Message systemMessage = new Message("system", """
                你是一个选项生成器，你需要根据对话内容，为`user`提供1到3个简单的选项用于回复`assistant`最新的消息。选项之间用换行隔开，不用添加序号。""");
        Message userMessage = new Message("system", "请根据以下对话内容，提供选项。\n");
        userMessage.addContent("对话内容: \n"+conversationService.getLatestConversationsMessagesString(3, characterService.getCurrentCharacterId()));
        var requestBody = new ChatRequestBody(
                PropertiesManager.getProperty("OPTION_MODEL"),
                List.of(systemMessage,userMessage),
                false,
                1024,
                1.0f,
                5
        );
        requestBody.setEnableThinking(true);
        logger.info("Option generation request body: {}", requestBody.toMap());
        var response = webClient.post()
                .header("Authorization", "Bearer " + PropertiesManager.getProperty("OPTION_API_KEY"))
                .bodyValue(requestBody.toMap())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        logger.info("Option generation response: {}", response);
        JSONObject responseJson = JSONObject.parseObject(response);
        if(response==null || response.isEmpty()){
            return new String[]{"我不知道"};
        }else
            return responseJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim().split("\n");
    }
}
