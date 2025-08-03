package com.github.shasnow.cabm4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.shasnow.cabm4j.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
