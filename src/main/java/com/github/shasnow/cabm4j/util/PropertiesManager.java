package com.github.shasnow.cabm4j.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class PropertiesManager {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesManager.class);
    private static Properties properties = new Properties();
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
                "EMBEDDING_MODEL",
                "OPTION_API_URL",
                "OPTION_API_KEY",
                "OPTION_MODEL"
        );
        for (String essentialKey : essentialKeys) {
            if (!prop.containsKey(essentialKey)) {
                logger.error("Missing essential property: {}", essentialKey);
                return;
            }
        }
        setProperties(prop);
        logger.info("Properties loaded successfully");
        logger.info(
                """
                Java version: {}
                Vendor: {}
                Working directory: {}
                OS name: {}
                OS architecture: {}
                """,
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                System.getProperty("user.dir"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"));
        logger.info("User properties:{}", properties.toString());
    }
    public static void loadProperties(){
        loadProperties("user.properties");
    }
    public static void setProperties(Properties properties) {
        PropertiesManager.properties = properties;
    }
    public static Properties getProperties() {
        return PropertiesManager.properties;
    }
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

}
