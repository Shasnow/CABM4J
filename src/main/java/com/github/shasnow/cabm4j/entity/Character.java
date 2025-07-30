package com.github.shasnow.cabm4j.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public abstract class Character implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String nameEn;
    private String image;
    private String nameColor;
    private String description;
    private String prompt;
    private String welcomeMessage;
    private String messageExample;

    public Character(String id, String name, String nameEn, String image, String nameColor, String description, String prompt, String welcomeMessage, String messageExample) {
        this.id = id;
        this.name = name;
        this.nameEn = nameEn;
        this.image = image;
        this.nameColor = nameColor;
        this.description = description;
        this.prompt = prompt;
        this.welcomeMessage = welcomeMessage;
        this.messageExample = messageExample;
    }

}
