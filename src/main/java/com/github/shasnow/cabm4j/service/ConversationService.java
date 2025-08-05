package com.github.shasnow.cabm4j.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.shasnow.cabm4j.entity.Conversation;
import com.github.shasnow.cabm4j.entity.Message;
import com.github.shasnow.cabm4j.mapper.ConversationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {
    @Resource
    MessageService messageService;

    public void saveConversation(Conversation conversation) {
        // 使用 MyBatis-Plus 的 save 方法保存对话
        this.save(conversation);
    }
    public void saveConversation(Integer userMessageId, Integer assistantMessageId, String assistantId) {
        Conversation conversation = new Conversation();
        conversation.setUserMessageId(userMessageId);
        conversation.setAssistantMessageId(assistantMessageId);
        conversation.setAssistantId(assistantId);
        this.save(conversation);
    }
    public List<Message> getLatestConversationsMessages(int limit, String assistantId) {
        // 使用 MyBatis-Plus 的 lambda 查询获取最新的对话
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assistant_id", assistantId)
                    .orderByDesc("id")
                    .last("LIMIT " + limit);
        List<Conversation> conversations = this.list(queryWrapper);
        List<Message> messages = new ArrayList<>();
        for (int i = conversations.size() - 1; i >= 0; i--) {
            Conversation conversation = conversations.get(i);
            Message userMessage = messageService.getById(conversation.getUserMessageId());
            Message assistantMessage = messageService.getById(conversation.getAssistantMessageId());
            if (userMessage != null) {
                messages.add(userMessage);
            }
            if (assistantMessage != null) {
                messages.add(assistantMessage);
            }
        }
        return messages;
    }
    public String getLatestConversationsMessagesString(int limit, String assistantId) {
        // 获取最新的对话消息并转换为字符串
        List<Message> messages = getLatestConversationsMessages(limit, assistantId);
        StringBuilder sb = new StringBuilder();
        for (Message message : messages) {
            sb.append(message.toSimpleString()).append("\n");
        }
        return sb.toString();
    }
}
