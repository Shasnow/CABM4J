package com.github.shasnow.cabm4j.util;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class PropertiesManager {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PropertiesManager.class);
    public static void loadProperties(String filePath) {
        Properties prop = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            prop.load(fileInputStream);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
            return;
        }
        Set<String> essentialKeys = Set.of(
                "CHAT_API_URL",
                "CHAT_API_KEY",
                "CHAT_MODEL",
                "IMAGE_API_URL",
                "IMAGE_API_KEY",
                "IMAGE_MODEL",
                "EMBEDDING_API_URL",
                "EMBEDDING_API_KEY",
                "EMBEDDING_MODEL"
        );
        for (String essentialKey : essentialKeys) {
            if (!prop.containsKey(essentialKey)) {
                System.err.println("Missing essential property: " + essentialKey);
                return;
            }
        }
        Properties originalProp = System.getProperties();
        prop.putAll(originalProp);
        System.setProperties(prop);
        logger.info("Properties loaded successfully");
        logger.info(System.getProperties().toString());
    }
    public static void loadProperties(){
        loadProperties("user.properties");
    }


}
