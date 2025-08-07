package com.github.shasnow.cabm4j.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequestBody implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    private String model;
    private List<Message> messages;
    private boolean stream;
    private int maxTokens;
    private boolean enableThinking=false;
//    private int thinkingBudget;
//    private float minP;
//    private String stop;
    private float temperature;
//    private String topP;
    private int topK;
//    private String frequencyPenalty;
//    private String n;


    public ChatRequestBody(String model, Message message, boolean stream, int maxTokens, float temperature, int topK) {
        this.model = model;
        this.messages = Collections.singletonList(message);
        this.stream = stream;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.topK = topK;
    }

    public ChatRequestBody(String model, List<Message> messages, boolean stream, int maxTokens, float temperature, int topK) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.topK = topK;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "model", model,
                "messages", messages,
                "stream", stream,
                "max_tokens", maxTokens,
                "temperature", temperature,
                "top_k", topK,
                "enable_thinking", enableThinking
        );
    }
}
