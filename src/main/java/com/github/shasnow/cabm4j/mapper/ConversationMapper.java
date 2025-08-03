package com.github.shasnow.cabm4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.shasnow.cabm4j.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
