package com.github.shasnow.cabm4j.util;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class APIUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_SECONDS = 1;
    private static final Logger logger = LoggerFactory.getLogger(APIUtil.class);

    public static ApiResponse makeApiRequest(
            String url,
            String method,
            Header[] headers,
            Map<String, Object> formData,
            Object jsonData,
            boolean stream,
            int timeoutSeconds,
            int maxRetries,
            int retryDelaySeconds
    ) throws ApiException, IOException {
        logger.info("Making API request: URL={}, Method={}, Headers={}, FormData={}, JSONData={}, Stream={}, Timeout={}, MaxRetries={}, RetryDelay={}",
                url, method, headers, formData, jsonData, stream, timeoutSeconds, maxRetries, retryDelaySeconds);
        method = method.toUpperCase();
        headers = headers != null ? headers : new Header[]{
                new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()),
        };
        maxRetries = maxRetries > 0 ? maxRetries : MAX_RETRIES;
        retryDelaySeconds = retryDelaySeconds > 0 ? retryDelaySeconds : RETRY_DELAY_SECONDS;

        // 配置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(timeoutSeconds))
                .setResponseTimeout(Timeout.ofSeconds(timeoutSeconds))
                .build();

        int retries = 0;
        Exception lastError = null;

        while (retries < maxRetries) {
            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build()) {
                logger.info("Try {} of {}: URL={}, Method={}", retries + 1, maxRetries, url, method);
                // 创建请求
                HttpUriRequestBase request = createRequest(method, url, headers, formData, jsonData);
                HttpClientResponseHandler<ApiResponse> responseHandler = response -> {
                    InputStream contentStream = response.getEntity().getContent();
                    String content=new Scanner(contentStream).useDelimiter("\\A").next();
                    logger.info("Response from API: Status={}, Headers={}, Content={}",
                            response.getCode(), response.getHeaders(), content);
                    // 检查 HTTP 状态码
                    int statusCode = response.getCode();
                    if (statusCode >= 400) {
                        String errorMsg = String.format("API请求失败: HTTP %d", statusCode);
                        if (content != null) {
                            errorMsg += " - " + content;
                        }
                        throw new HttpException(errorMsg, statusCode, response);
                    }
                    return new ApiResponse(statusCode, JSONObject.parseObject(content));
                };
                // 执行请求
                return httpClient.execute(request, responseHandler);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                lastError = e;
                retries++;

                // 等待重试
                if (retries < maxRetries) {
                    try {
                        TimeUnit.SECONDS.sleep(retryDelaySeconds);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("请求被中断", ie);
                    }
                }
            }
        }

        // 所有重试失败后抛出异常
        String errorMsg = lastError.getMessage();
        throw new ApiException(errorMsg, lastError);
    }

    // 辅助方法：创建请求对象
    private static HttpUriRequestBase createRequest(
            String method,
            String url,
            Header[] headers,
            Map<String, Object> formData,
            Object jsonData
    ) throws JsonProcessingException {

        HttpUriRequestBase request = switch (method) {
            case "GET" -> new HttpGet(url);
            case "POST" -> new HttpPost(url);
            case "PUT" -> new HttpPut(url);
            case "DELETE" -> new HttpDelete(url);
            default -> throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        };

        // 设置请求头
        for (Header header : headers) {
            request.addHeader(header);
        }

        // 设置请求体（JSON 或表单数据）
        if (jsonData != null) {
            StringEntity entity = new StringEntity(objectMapper.writeValueAsString(jsonData), ContentType.APPLICATION_JSON);
            request.setEntity(entity);
        } else if (formData != null) {
            List<BasicNameValuePair> params = formData.entrySet().stream()
                    .map(e -> new BasicNameValuePair(e.getKey(), e.getValue().toString()))
                    .toList();
            request.setEntity(new UrlEncodedFormEntity(params));
        }

        return request;
    }

    // 自定义异常类
    @Getter
    public static class ApiException extends Exception {
        private final int statusCode;
        private final ClassicHttpResponse response;

        public ApiException(String message, int statusCode, ClassicHttpResponse response) {
            super(message);
            this.statusCode = statusCode;
            this.response = response;
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = -1;
            this.response = null;
        }

    }

    // 响应包装类
    public record ApiResponse(int code, JSONObject data) {
    }
}
