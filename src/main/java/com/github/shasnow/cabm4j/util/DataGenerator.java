package com.github.shasnow.cabm4j.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
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
                logger.info("user.properties file created with default content.");
            }
            Path dataBasePath = Path.of("identifier.sqlite");
            // 创建数据库文件
            if (!Files.exists(dataBasePath)) {
                Files.createFile(dataBasePath);
                logger.info("Database file created.");
            }
            initializeDatabase(dataBasePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create user.properties file", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void initializeDatabase(Path dataBasePath) throws SQLException {
        // SQLite JDBC URL 格式：jdbc:sqlite:文件路径
        String jdbcUrl = "jdbc:sqlite:" + dataBasePath.toAbsolutePath();

        // SQL 建表语句
        String sql = """
            CREATE TABLE IF NOT EXISTS message (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;
        String sql2 = """
            CREATE TABLE IF NOT EXISTS conversation (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_message_id INTEGER,
                assistant_message_id INTEGER,
                assistant_id TEXT
            );
            """;

        // 3. 使用 try-with-resources 确保资源释放
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {

            // 4. 执行 SQL
            stmt.execute(sql);
            stmt.execute(sql2);
            logger.info("Table initialized successfully.");

        } catch (SQLException e) {
            logger.error("SQL error: {}", e.getMessage());
            throw e;
        }
    }
}
