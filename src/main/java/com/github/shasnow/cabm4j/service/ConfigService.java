package com.github.shasnow.cabm4j.service;

import com.github.shasnow.cabm4j.character.Characters;
import com.github.shasnow.cabm4j.util.ConfigManager;
import com.github.shasnow.cabm4j.util.PropertiesManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class ConfigService {
    private final boolean initialized = false;
    private final boolean configLoaded = false;
    private String currentCharacterID = null;

    public ConfigService() {
        PropertiesManager.loadProperties();
        createEssentialDirectories();
    }

    private void createEssentialDirectories() {
        try {
            Files.createDirectories(Path.of(ConfigManager.getAppConfig().get("image_cache_dir").toString()));
            Files.createDirectories(Path.of(ConfigManager.getAppConfig().get("static_folder").toString()));
            Files.createDirectories(Path.of(ConfigManager.getAppConfig().get("template_folder").toString()));
            Files.createDirectories(Path.of(ConfigManager.getAppConfig().get("history_dir").toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,Object> getCharacterConfig(String characterID) {
        Map<String,Object> characterConfig = Characters.getCharacterConfig(characterID);
        currentCharacterID= characterConfig.get("id").toString();
        return characterConfig;
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
        return System.getProperty("IMAGE_API_URL");
    }

    public String getImageAPIKey(){
        return System.getProperty("IMAGE_API_KEY");
    }
}
