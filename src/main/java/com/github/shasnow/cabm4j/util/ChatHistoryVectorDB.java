package com.github.shasnow.cabm4j.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.Getter;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class ChatHistoryVectorDB {
    private static final Logger logger = LoggerFactory.getLogger(ChatHistoryVectorDB.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String apiKey;
    private final String url;
    @Getter
    private String model;
    private String characterName;
    @Getter
    private List<List<Double>> vectors = new ArrayList<>();
    private List<Map<String, Object>> metadata = new ArrayList<>();
    private Map<String, List<Integer>> textToIndex = new HashMap<>();
    private Set<String> loadedTexts = new HashSet<>();
    @Getter
    private String dbFilePath;

    public ChatHistoryVectorDB(String characterName) {
        this.apiKey = System.getProperty("EMBEDDING_API_KEY", null);
        this.model = System.getProperty("EMBEDDING_MODEL", "BAAI/bge-m3");
        this.url = System.getProperty("EMBEDDING_API_URL", "https://api.siliconflow.cn/v1/embeddings");
        this.characterName = characterName == null ? "default" : characterName;
        String memoryDir = "data/memory";
        this.dbFilePath = memoryDir + File.separator + this.characterName + "_memory.json";

        // Ensure data directory exists
        try {
            Files.createDirectories(Paths.get(memoryDir));
        } catch (IOException e) {
            logger.error("Failed to create memory directory", e);
        }
    }

    private List<Double> getEmbedding(String text) {
        // Check cache first
        if (textToIndex.containsKey(text) && !textToIndex.get(text).isEmpty()) {
            int index = textToIndex.get(text).get(0);
            return vectors.get(index);
        }

        // Prepare API request
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("input", text);

        Header[] headers = new BasicHeader[]{
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("Authorization", "Bearer " + apiKey)
        };

        try {
            // In a real implementation, you would use an HTTP client like OkHttp or Apache HttpClient
            // This is a simplified version for demonstration
            APIUtil.ApiResponse response = APIUtil.makeApiRequest(url, "POST", headers, null, payload, false, 30, 3, 1);
            JSONObject jsonResponse = response.data();

            if (jsonResponse.containsKey("data")) {
                JSONArray embeddings = jsonResponse.getJSONArray("data");
                if (!embeddings.isEmpty()) {
                    return embeddings.getJSONObject(0).getJSONArray("embedding")
                            .toJavaList(Double.class);
                }
            }

            logger.error("Failed to get embedding, response: {}", response);
            return null;
        } catch (Exception e) {
            logger.error("Error getting embedding", e);
            return null;
        }
    }

    public void addText(String text, Map<String, Object> metadata) {
        if (text.isBlank()) {
            return;
        }

        List<Double> vector = getEmbedding(text);
        if (vector == null) {
            return;
        }

        vectors.add(vector);
        Map<String, Object> meta = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        meta.put("text", text);
        this.metadata.add(meta);

        textToIndex.computeIfAbsent(text, k -> new ArrayList<>()).add(vectors.size() - 1);
    }

    public List<Map<String, Object>> search(String query, int topK, int timeoutSeconds) {
        if (vectors.isEmpty()) {
            return Collections.emptyList();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Map<String, Object>>> future = executor.submit(() -> {
            List<Double> queryVector = getEmbedding(query);
            if (queryVector == null) {
                return Collections.emptyList();
            }

            // Convert to 2D array for calculation
            double[][] vectorsArray = vectors.stream()
                    .map(v -> v.stream().mapToDouble(Double::doubleValue).toArray())
                    .toArray(double[][]::new);
            double[] queryArray = queryVector.stream().mapToDouble(Double::doubleValue).toArray();

            // Calculate cosine similarity
            double[] similarities = new double[vectors.size()];
            for (int i = 0; i < vectorsArray.length; i++) {
                similarities[i] = cosineSimilarity(vectorsArray[i], queryArray);
            }

            // Get top K indices
            Integer[] indices = IntStream.range(0, similarities.length)
                    .boxed()
                    .sorted((a, b) -> Double.compare(similarities[b], similarities[a]))
                    .limit(topK)
                    .toArray(Integer[]::new);

            // Build results
            List<Map<String, Object>> results = new ArrayList<>();
            for (int idx : indices) {
                Map<String, Object> result = new HashMap<>();
                result.put("text", metadata.get(idx).get("text"));
                result.put("similarity", similarities[idx]);

                Map<String, Object> meta = new HashMap<>(metadata.get(idx));
                meta.remove("text");
                result.put("metadata", meta);

                results.add(result);
            }

            // Log results
            if (!results.isEmpty()) {
                logger.info("Memory search for '{}' -> found {} relevant records",
                        query, results.size());
                for (int i = 0; i < results.size(); i++) {
                    Map<String, Object> result = results.get(i);
                    String textPreview = ((String) result.get("text")).length() > 100
                            ? ((String) result.get("text")).substring(0, 100) + "..."
                            : (String) result.get("text");
                    logger.info("  Record {}: similarity={}, content='{}'",
                            i + 1, result.get("similarity"), textPreview);
                }
            } else {
                logger.info("Memory search for '{}' -> no relevant records found", query);
            }

            return results;
        });

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.warn("Memory search timed out after {} seconds", timeoutSeconds);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error during memory search", e);
            return Collections.emptyList();
        } finally {
            executor.shutdownNow();
        }
    }

    private double cosineSimilarity(double[] vecA, double[] vecB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += Math.pow(vecA[i], 2);
            normB += Math.pow(vecB[i], 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public void saveToFile(String filePath) {
        Map<String, Object> data = new HashMap<>();
        data.put("character_name", characterName);
        data.put("model", model);
        data.put("vectors", vectors);
        data.put("metadata", metadata);
        data.put("last_updated", LocalDateTime.now().format(DATE_TIME_FORMATTER));

        try (OutputStream writer = new FileOutputStream(filePath)) {
            JSON.writeTo(writer, data);
            logger.info("Vector database saved to {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to save vector database", e);
        }
    }

    public void saveToFile() {
        saveToFile(dbFilePath);
    }

    public void loadFromFile(String filePath) {
        String path = filePath == null ? dbFilePath : filePath;

        if (!Files.exists(Paths.get(path))) {
            logger.info("Database file not found, will create new database: {}", path);
            return;
        }

        try (InputStream reader = new FileInputStream(path)) {
            Map<String, Object> data = JSON.parseObject(new Scanner(reader).useDelimiter("\\A").next(),
                    new TypeReference<>() {
                    });

            this.characterName = (String) data.getOrDefault("character_name", characterName);
            this.model = (String) data.getOrDefault("model", model);
            this.vectors = JSON.parseObject(JSON.toJSONString(data.get("vectors")),
                    new TypeReference<>() {
                    });
            this.metadata = JSON.parseObject(JSON.toJSONString(data.get("metadata")),
                    new TypeReference<>() {
                    });

            // Rebuild text to index mapping
            this.textToIndex = new HashMap<>();
            this.loadedTexts = new HashSet<>();
            for (int i = 0; i < metadata.size(); i++) {
                Map<String, Object> meta = metadata.get(i);
                String text = (String) meta.get("text");
                if (!text.isBlank()) {
                    textToIndex.computeIfAbsent(text, k -> new ArrayList<>()).add(i);
                    loadedTexts.add(text);
                }
            }

            logger.info("Successfully loaded vector database with {} records", vectors.size());
        } catch (Exception e) {
            logger.error("Failed to load database", e);
            // Initialize empty database
            this.vectors = new ArrayList<>();
            this.metadata = new ArrayList<>();
            this.textToIndex = new HashMap<>();
            this.loadedTexts = new HashSet<>();
        }
    }

    public void loadFromLog(String filePath, boolean incremental) {
        if (!Files.exists(Paths.get(filePath))) {
            throw new RuntimeException("File " + filePath + " not found");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JSONObject entry = JSON.parseObject(line);
                    String content = entry.getString("content");
                    if (content.isBlank()) {
                        continue;
                    }

                    // Skip already loaded texts in incremental mode
                    if (incremental && loadedTexts.contains(content)) {
                        continue;
                    }

                    // Get embedding
                    List<Double> vector = getEmbedding(content);
                    if (vector == null) {
                        continue;
                    }

                    // Store data and metadata
                    vectors.add(vector);
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("text", content);
                    meta.put("timestamp", entry.getString("timestamp"));
                    meta.put("role", entry.getString("role"));
                    metadata.add(meta);

                    textToIndex.computeIfAbsent(content, k -> new ArrayList<>()).add(vectors.size() - 1);
                    loadedTexts.add(content);
                } catch (Exception e) {
                    logger.warn("Skipping malformed line: {}", line);
                }
            }
        } catch (IOException e) {
            logger.error("Error loading from log file", e);
        }
    }

    public void addChatTurn(String userMessage, String assistantMessage, String timestamp) {
        if (timestamp == null) {
            timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        }

        String conversationText = "用户: " + userMessage + "\n助手: " + assistantMessage;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("user_message", userMessage);
        metadata.put("assistant_message", assistantMessage);
        metadata.put("timestamp", timestamp);
        metadata.put("type", "conversation");

        addText(conversationText, metadata);
        logger.info("Added conversation to vector database: {}", userMessage);
    }

    public void initializeDatabase() {
        loadFromFile(null);
        logger.info("Memory database initialized for character: {}", characterName);
    }

    public String getRelevantMemory(String query, int topK, int timeout, double minSimilarity) {
        try {
            List<Map<String, Object>> results = search(query, topK, timeout);
            if (results.isEmpty()) {
                return "";
            }

            // Filter results by minimum similarity
            List<Map<String, Object>> filteredResults = results.stream()
                    .filter(r -> ((Number) r.get("similarity")).doubleValue() >= minSimilarity)
                    .toList();

            if (filteredResults.isEmpty()) {
                logger.info("No results after filtering: minSimilarity={}, originalCount={}",
                        minSimilarity, results.size());
                return "";
            }

            logger.info("Memory filtering results: {}/{} records passed similarity threshold {}",
                    filteredResults.size(), results.size(), minSimilarity);

            // Format as prompt
            StringBuilder memoryPrompt = new StringBuilder("这是相关的记忆，可以作为参考：\n\n");

            for (int i = 0; i < filteredResults.size(); i++) {
                Map<String, Object> result = filteredResults.get(i);
                Map<String, Object> meta = (Map<String, Object>) result.get("metadata");

                if ("conversation".equals(meta.get("type"))) {
                    String userMsg = (String) meta.get("user_message");
                    String assistantMsg = (String) meta.get("assistant_message");
                    String timestamp = (String) meta.get("timestamp");

                    memoryPrompt.append(String.format("记录 %d:\n", i + 1))
                            .append("用户: ").append(userMsg).append("\n")
                            .append("你: ").append(assistantMsg).append("\n")
                            .append("时间: ").append(timestamp).append("\n\n");

                    logger.info("  -> Record {}: user='{}...', assistant='{}...', similarity={}",
                            i + 1, userMsg, assistantMsg, result.get("similarity"));
                } else {
                    memoryPrompt.append(String.format("记录 %d: %s\n\n", i + 1, result.get("text")));
                    logger.info("  -> Record {}: '{}...', similarity={}",
                            i + 1, result.get("text"), result.get("similarity"));
                }
            }

            memoryPrompt.append("请参考以上历史记录，保持对话的连贯性和一致性。\n\n以下是本次用户输入：");

            logger.info("Generated memory prompt: {} characters", memoryPrompt.length());
            return memoryPrompt.toString();
        } catch (Exception e) {
            logger.error("Failed to get relevant memory", e);
            return "";
        }
    }

    // Helper class for HTTP requests (would need actual implementation)
    private static class HttpClientUtil {
        public static String post(String url, String jsonBody, Map<String, String> headers) {
            // Implement actual HTTP POST request here
            // This is a placeholder implementation
            throw new UnsupportedOperationException("HTTP client not implemented");
        }
    }
}
