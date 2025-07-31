package com.github.shasnow.cabm4j.service;

import com.github.shasnow.cabm4j.entity.ImageConfig;
import com.github.shasnow.cabm4j.util.APIUtil;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImageService {
    private final ConfigService configService;
    private Path cacheDir;
    private String currentBackground = null;

    public ImageService(ConfigService configService) {
        this.configService = configService;
        cacheDir= Paths.get(configService.getAppConfig().get("image_cache_dir").toString());
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,Object> generateImage(ImageConfig imageConfig){
        String url= configService.getImageAPIURL();
        String apiKey = configService.getImageAPIKey();
        Header[] headers = new Header[]{
                new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer "+apiKey),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json")};
        if (imageConfig==null){
            imageConfig = new ImageConfig(configService.getImageConfig(),configService.getRandomImagePrompt(),configService.getNegativePrompts());
        }
        Map<String,Object> requestData= imageConfig.toMap();
        try {
            APIUtil.ApiResponse response = APIUtil.makeApiRequest(url,"POST", headers,null, requestData,false, 30, 3, 1);
            if (response.data().containsKey("images")) {
                String imageUrl;
                Object images = response.data().get("images");
                List<?> imagesList = (List<?>) images;
                Map<String,String> urlMap = (Map<String, String>) imagesList.get(0);
                imageUrl = urlMap.get("url");
                String imagePath = null;
                if (imageUrl != null) {
                    imagePath = String.valueOf(downloadImage(imageUrl));
                }
                currentBackground = imagePath;
                return Map.of("image_path", imagePath, "info", response.data());
            } else {
                throw new RuntimeException("Image generation failed: " + response.data());
            }
        } catch (APIUtil.ApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentBackground() {
        if (currentBackground != null && Files.exists(Path.of(currentBackground))) return currentBackground;
        Path fallbackImage = getFallbackImage().orElse(null);
        if (fallbackImage != null && Files.exists(fallbackImage)) {
            currentBackground = fallbackImage.toString();
            return currentBackground;
        }
        return null;
    }

    public Optional<Path> getFallbackImage() {
        try {
            return Files.list(cacheDir)
                    .filter(path -> path.toString().endsWith(".jpg") || path.toString().endsWith(".png"))
                    .max(Comparator.comparingLong(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    }));
        } catch (IOException e) {
            System.err.println("Error reading cache directory: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 生成背景图像
     * @param prompt 描述生成背景图像的提示词
     * @return 包含图像路径和信息的字典
     */
    public Map<String,Object> generateBackground(String prompt){
        if (prompt==null || prompt.isEmpty()) {
            prompt=configService.getRandomImagePrompt();
        }
        ImageConfig imageConfig = new ImageConfig(configService.getImageConfig(),prompt, configService.getNegativePrompts());
        Map<String,Object> result = generateImage(imageConfig);
        cleanupOldImages(24,20);
        return result;
    }

    public Path downloadImage(String imageUrl) throws IOException {
        long timestamp = System.currentTimeMillis();
        String fileName = "image_" + timestamp + ".jpg";
        Path savePath = cacheDir.resolve(fileName);
        HttpGet request = new HttpGet(imageUrl);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(request)) {

            // 检查响应码
            if (response.getCode() != 200) {
                throw new IOException("HTTP request failed with code: " + response.getCode());
            }

            // 流式下载到文件
            try (InputStream inputStream = response.getEntity().getContent();
                 FileOutputStream outputStream = new FileOutputStream(savePath.toFile())) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 确保实体内容被完全消费（推荐）
            EntityUtils.consume(response.getEntity());
            System.out.println("Image downloaded successfully: " + savePath);
            return savePath;
        }
    }

    /**
     * 清理旧图像
     *
     * @param maxAgeHours 最大保留时间（小时）
     * @param maxFiles    最大保留文件数
     */
    public void cleanupOldImages(int maxAgeHours, int maxFiles) {
        try {
            // 获取所有 .jpg 和 .png 文件
            List<Path> imageFiles = Files.list(cacheDir)
                    .filter(file -> file.toString().endsWith(".jpg") || file.toString().endsWith(".png"))
                    .toList();

            // 如果文件数量超过最大值，按修改时间排序并删除最旧的文件
            if (imageFiles.size() > maxFiles) {
                List<Path> sortedFiles = imageFiles.stream()
                        .sorted(Comparator.comparingLong(this::getLastModifiedTime))
                        .toList();

                List<Path> filesToDelete = sortedFiles.subList(0, sortedFiles.size() - maxFiles);
                filesToDelete.forEach(this::deleteSilently);
            }

            // 删除超过最大保留时间的文件
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(maxAgeHours);
            Instant cutoffInstant = cutoffTime.atZone(ZoneId.systemDefault()).toInstant();

            for (Path filePath : imageFiles) {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                    LocalDateTime fileModifiedTime = attrs.lastModifiedTime()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    if (fileModifiedTime.isBefore(cutoffTime)) {
                        deleteSilently(filePath);
                    }
                } catch (IOException e) {
                    // 忽略单个文件删除错误
                }
            }
        } catch (IOException e) {
            System.err.println("清理旧图像时出错: " + e.getMessage());
        }
    }

    /**
     * 获取文件的最后修改时间（毫秒）
     */
    private long getLastModifiedTime(Path file) {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            return 0L; // 如果出错，返回 0（最旧的时间）
        }
    }

    /**
     * 静默删除文件（忽略错误）
     */
    private void deleteSilently(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 忽略删除错误
        }
    }
}
