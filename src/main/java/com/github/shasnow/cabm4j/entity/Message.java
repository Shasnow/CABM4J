package com.github.shasnow.cabm4j.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    @TableId(type = IdType.AUTO)  // 数据库自增主键
    private Integer id;

    private String role;
    private String content;

    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createTime;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}