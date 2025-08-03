package com.github.shasnow.cabm4j.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.shasnow.cabm4j.entity.Message;
import com.github.shasnow.cabm4j.mapper.MessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    public Integer saveMessage(Message message) {
        // 使用 MyBatis-Plus 的 save 方法保存消息
        if (message.getCreateTime()==null) message.setCreateTime(LocalDateTime.now());
        if (this.save(message)) {
            return message.getId();  // 返回保存后的消息 ID
        }
        logger.error("Failed to save message: {}", message);
        return null;  // 保存失败时返回 null
    }
}
