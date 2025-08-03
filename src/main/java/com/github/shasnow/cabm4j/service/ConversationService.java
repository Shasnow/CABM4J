package com.github.shasnow.cabm4j.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.shasnow.cabm4j.entity.Conversation;
import com.github.shasnow.cabm4j.mapper.ConversationMapper;
import org.springframework.stereotype.Service;

@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {
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
}
