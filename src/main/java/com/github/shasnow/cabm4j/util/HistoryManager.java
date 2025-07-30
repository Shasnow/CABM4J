package com.github.shasnow.cabm4j.util;

import com.alibaba.fastjson2.JSON;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class HistoryManager {
    private final String historyDir;
    private final Map<String, Deque<Map<String, String>>> historyCache = new HashMap<>();
    private static final Pattern BRACKET_PATTERN = Pattern.compile("【[^】]*】");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 历史记录管理器
     *
     * @param historyDir 历史记录存储目录
     */
    public HistoryManager(String historyDir) {
        this.historyDir = historyDir;
        ensureHistoryDir();
    }

    /**
     * 确保历史记录目录存在
     */
    private void ensureHistoryDir() {
        try {
            Files.createDirectories(Paths.get(historyDir));
        } catch (IOException e) {
            System.err.println("创建历史记录目录失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色历史记录文件路径
     *
     * @param characterId 角色ID
     * @return 历史记录文件路径
     */
    private String getCharacterHistoryFile(String characterId) {
        return Paths.get(historyDir, characterId + "_history.log").toString();
    }

    /**
     * 初始化角色历史记录缓存
     *
     * @param characterId 角色ID
     * @param maxSize     缓存的最大消息数量
     */
    private void initializeCache(String characterId, int maxSize) {
        if (historyCache.containsKey(characterId)) {
            return;
        }

        String historyFile = getCharacterHistoryFile(characterId);
        Deque<Map<String, String>> messages = new ArrayDeque<>();

        // 如果文件不存在，创建空缓存
        Path path = Paths.get(historyFile);
        if (!Files.exists(path)) {
            historyCache.put(characterId, new ArrayDeque<>(maxSize));
            return;
        }

        // 读取历史记录
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            List<Map<String, String>> tempMessages = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        Map<String, String> message = JSON.parseObject(line, Map.class);
                        tempMessages.add(message);
                    } catch (Exception e) {
                        // 忽略无效的JSON行
                        continue;
                    }
                }
            }

            // 只保留最近的maxSize条消息
            int start = Math.max(0, tempMessages.size() - maxSize);
            messages.addAll(tempMessages.subList(start, tempMessages.size()));

        } catch (IOException e) {
            System.err.println("加载历史记录失败: " + e.getMessage());
        }

        // 创建缓存
        Deque<Map<String, String>> cachedDeque = new ArrayDeque<>(maxSize);
        cachedDeque.addAll(messages);
        historyCache.put(characterId, cachedDeque);
        System.out.printf("已加载 %d 条 %s 的历史记录到内存%n", cachedDeque.size(), characterId);
    }

    /**
     * 清理assistant消息内容，去除【】及其内部的内容
     *
     * @param content 原始消息内容
     * @return 清理后的消息内容
     */
    private String cleanAssistantContent(String content) {
        return BRACKET_PATTERN.matcher(content).replaceAll("");
    }

    /**
     * 保存消息到历史记录
     *
     * @param characterId 角色ID
     * @param role        消息角色
     * @param content     消息内容
     */
    public void saveMessage(String characterId, String role, String content) {
        // 如果是assistant消息且启用了清理功能，清理内容去除【】及其内部的内容
        if ("assistant".equals(role) && AppConfig.get().getBoolean("clean_assistant_history", true)) {
            content = cleanAssistantContent(content);
        }

        // 获取当前时间戳
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // 创建消息记录
        Map<String, String> messageRecord = new HashMap<>();
        messageRecord.put("timestamp", timestamp);
        messageRecord.put("role", role);
        messageRecord.put("content", content);

        // 获取历史记录文件路径
        String historyFile = getCharacterHistoryFile(characterId);

        // 写入历史记录文件
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(historyFile), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(JSON.toJSONString(messageRecord));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("保存历史记录失败: " + e.getMessage());
        }

        // 更新内存缓存
        if (historyCache.containsKey(characterId)) {
            historyCache.get(characterId).add(messageRecord);
        }
    }

    /**
     * 加载历史记录
     *
     * @param characterId    角色ID
     * @param count          加载的消息数量
     * @param maxCacheSize   缓存的最大消息数量
     * @return 历史记录列表，按时间从旧到新排序
     */
    public List<Map<String, String>> loadHistory(String characterId, int count, int maxCacheSize) {
        // 确保缓存已初始化
        if (!historyCache.containsKey(characterId)) {
            initializeCache(characterId, maxCacheSize);
        }

        // 从缓存中获取历史记录
        Deque<Map<String, String>> messages = historyCache.get(characterId);
        List<Map<String, String>> result = new ArrayList<>();

        // 获取最后count条消息或全部消息
        List<Map<String, String>> messageList = new ArrayList<>(messages);
        int start = Math.max(0, messageList.size() - count);
        result.addAll(messageList.subList(start, messageList.size()));

        return result;
    }

    /**
     * 清空历史记录
     *
     * @param characterId 角色ID
     * @return 是否成功清空
     */
    public boolean clearHistory(String characterId) {
        // 获取历史记录文件路径
        String historyFile = getCharacterHistoryFile(characterId);

        // 如果文件不存在，返回true
        Path path = Paths.get(historyFile);
        if (!Files.exists(path)) {
            return true;
        }

        // 清空文件
        try {
            Files.write(path, new byte[0]);

            // 清空缓存
            if (historyCache.containsKey(characterId)) {
                historyCache.get(characterId).clear();
            }

            return true;
        } catch (IOException e) {
            System.err.println("清空历史记录失败: " + e.getMessage());
            return false;
        }
    }
}

// 假设的AppConfig类，类似于Python中的get_app_config()
class AppConfig {
    private static final AppConfig instance = new AppConfig();
    private final Map<String, Object> config = new HashMap<>();

    private AppConfig() {
        // 默认配置
        config.put("clean_assistant_history", true);
    }

    public static AppConfig get() {
        return instance;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
}
