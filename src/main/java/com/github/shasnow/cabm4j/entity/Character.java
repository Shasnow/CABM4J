package com.github.shasnow.cabm4j.entity;

import lombok.Data;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

@Data
public class Character implements Serializable {
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
    private Path characterDir;
    private Path imageDir;
    private Path memoryDir;
    private Path historyDir;

    private Character(String id, String name, String nameEn, String image, String nameColor, String description, String prompt, String welcomeMessage, String messageExample) {
        this.id = id;
        this.name = name;
        this.nameEn = nameEn;
        this.image = image;
        this.nameColor = nameColor;
        this.description = description;
        this.prompt = prompt;
        this.welcomeMessage = welcomeMessage;
        this.messageExample = messageExample;
        createEssentialDirectories();
    }

    private void createEssentialDirectories() {
        // 创建必要的目录
        // 这里可以添加代码来创建角色配置文件夹等
        characterDir = Path.of("resources/characters/" + id);
        imageDir = Path.of("resources/characters/" + id + "/images");
        memoryDir = Path.of("resources/characters/" + id + "/memory");
        historyDir = Path.of("resources/characters/" + id + "/history");
        try {
            Files.createDirectories(imageDir);
            Files.createDirectories(memoryDir);
            Files.createDirectories(historyDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Character create(Path configPath) {
        try {
            TomlParseResult toml = Toml.parse(configPath);
            toml.errors().forEach(error -> System.err.println(error.toString()));
            String id = configPath.getFileName().toString().split("\\.")[0];
            String name = toml.getString("name");
            String nameEn = toml.getString("name_en");
            String image = toml.getString("image");
            String nameColor = toml.getString("name_color");
            String description = toml.getString("description");
            String prompt = toml.getString("prompt");
            String welcomeMessage = toml.getString("welcome_message");
            String messageExample = toml.getString("message_example");
            return new Character(id, name, nameEn, image, nameColor, description, prompt, welcomeMessage, messageExample);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load character config from " + configPath, e);
        }

    }
}
