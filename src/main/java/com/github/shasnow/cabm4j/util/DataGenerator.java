package com.github.shasnow.cabm4j.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataGenerator {
    public static void dataGen(){
        // 创建必要的目录
        createEssentialDirectories();
        // 创建必要的文件
        createEssentialFiles();

    }
    private static void createEssentialDirectories() {
        try {
            Files.createDirectories(Path.of(ConfigManager.getAppConfig().get("character_folder").toString()));
            Files.createDirectories(Path.of(ConfigManager.getAppConfig().get("image_cache_dir").toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createEssentialFiles(){
        try {
            Path userPropertiesPath = Path.of("user.properties");
            if (!Files.exists(userPropertiesPath)) {
                Files.createFile(userPropertiesPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user.properties file", e);
        }
    }
}
