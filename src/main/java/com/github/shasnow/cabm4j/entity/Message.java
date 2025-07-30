package com.github.shasnow.cabm4j.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Message {
    private String role;
    private String content;

    /**
     * 初始化消息
     * @param role 消息角色（"system", "user", "assistant"）
     * @param content 消息内容
     */
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 转换为字典格式
     * @return 包含角色和内容的Map
     */
    public Map<String, String> toMap() {
        Map<String, String> dict = new HashMap<>();
        dict.put("role", this.role);
        dict.put("content", this.content);
        return dict;
    }

    /**
     * 从字典创建消息
     * @param data 包含角色和内容的Map
     * @return Message对象
     */
    public static Message fromMap(Map<String, String> data) {
        return new Message(data.get("role"), data.get("content"));
    }
}
