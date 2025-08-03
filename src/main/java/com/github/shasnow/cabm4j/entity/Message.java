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

    public void addContent(String additionalContent) {
        if (this.content == null) {
            this.content = additionalContent;
        } else {
            this.content += additionalContent;
        }
    }

    /**
     * 将消息转换为 JSON 字符串格式
     * @return JSON 字符串, 格式为 {"role":"role_value","content":"content_value"}
     */
    public String toJSONString() {
        return String.format("{\"role\":\"%s\",\"content\":\"%s\"}", role, content);
    }

    /**
     * 将消息转换为简单字符串格式
     * @return 简单字符串, 格式为 "role: content"
     */
    public String toSimpleString() {
        return String.format("%s: %s", role, content);
    }
}