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
                // 写入默认内容
                String defaultContent = """
                # SiliconFlow API Configuration
                CHAT_API_URL=https://api.siliconflow.cn/v1/chat/completions
                CHAT_API_KEY=your_api_key_here
                # Replace with your actual API key
                CHAT_MODEL=deepseek-ai/DeepSeek-V3
                
                # Image Generation API Configuration
                IMAGE_API_URL=https://api.siliconflow.cn/v1/images/generations
                IMAGE_API_KEY=your_api_key_here
                # Replace with your actual API key
                IMAGE_MODEL=Kwai-Kolors/Kolors
                
                # Embedding API Configuration
                EMBEDDING_API_URL=https://api.siliconflow.cn/v1/embeddings
                EMBEDDING_API_KEY=your_api_key_here
                # Replace with your actual API key
                EMBEDDING_MODEL=BAAI/bge-m3
                """;
                Files.writeString(userPropertiesPath, defaultContent);
                System.out.println("user.properties file created with default content.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user.properties file", e);
        }
    }
}
