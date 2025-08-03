package com.github.shasnow.cabm4j.service;

import com.github.shasnow.cabm4j.util.ConfigManager;
import com.github.shasnow.cabm4j.util.DataGenerator;
import com.github.shasnow.cabm4j.util.PropertiesManager;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ConfigService {
    private final boolean initialized = false;
    private final boolean configLoaded = false;
    @Getter
    private String currentCharacterId = null;

    public ConfigService() {
        DataGenerator.dataGen();
        PropertiesManager.loadProperties();
    }

    public Map<String,Object> getAppConfig() {
        return ConfigManager.getAppConfig();
    }

    public Map<String,Object> getImageConfig() {
        return ConfigManager.getImageConfig();
    }

    public String getRandomImagePrompt() {
        return ConfigManager.getRandomImagePrompt();
    }

    public String getNegativePrompts() {
        return ConfigManager.getNegativePrompts();
    }

    public String getImageAPIURL(){
        return PropertiesManager.getProperty("IMAGE_API_URL");
    }

    public String getImageAPIKey(){
        return PropertiesManager.getProperty("IMAGE_API_KEY");
    }

    public String getChatApiUrl() {
        return PropertiesManager.getProperty("CHAT_API_URL");
    }

    public String getChatApiKey() {
        return PropertiesManager.getProperty("CHAT_API_KEY");
    }

    public Map<String, Object> getChatConfig() {
        return ConfigManager.getChatConfig();
    }

    public Map<String, Object> getStreamConfig() {
        return ConfigManager.getStreamConfig();
    }
}
