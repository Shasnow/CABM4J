package com.github.shasnow.cabm4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("conversation")
public class Conversation implements Serializable {
    @Serial
    private static final long serialVersionUID = 4L;
    @TableId(type = IdType.AUTO)  // 数据库自增主键
    private Integer id;

    private Integer userMessageId;
    private Integer assistantMessageId;
    private String assistantId;
}
