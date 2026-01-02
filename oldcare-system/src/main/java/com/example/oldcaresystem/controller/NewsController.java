package com.example.oldcaresystem.controller;

import com.example.oldcaresystem.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 新闻代理控制器
 * 用于代理新闻API请求，避免CORS问题
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    // 新闻API地址（从配置文件读取）
    @Value("${news.api.url:http://v.juhe.cn/toutiao/index}")
    private String newsApiUrl;
    
    // 新闻API Key（从配置文件读取）
    @Value("${news.api.key:}")
    private String newsApiKey;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取新闻列表
     * 调用聚合数据头条新闻API
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getNews(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        
        try {
            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("key", newsApiKey);
            if (type != null && !type.isEmpty()) {
                params.put("type", type);
            }
            params.put("page", String.valueOf(page));
            params.put("page_size", String.valueOf(pageSize));
            params.put("is_filter", "1"); // 过滤重复新闻
            
            // 构建URL参数
            StringBuilder urlParams = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (urlParams.length() > 0) {
                    urlParams.append("&");
                }
                urlParams.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
            
            String fullUrl = newsApiUrl + "?" + urlParams.toString();
            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            
            BufferedReader reader = null;
            try {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                } else {
                    // 读取错误响应
                    if (connection.getErrorStream() != null) {
                        reader = new BufferedReader(
                                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                    } else {
                        return ApiResponse.error("新闻API请求失败，状态码: " + responseCode);
                    }
                }
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                // 解析JSON响应
                String responseBody = response.toString();
                System.out.println("新闻API响应状态码: " + responseCode);
                System.out.println("新闻API响应内容: " + responseBody);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> newsData = (Map<String, Object>) objectMapper.readValue(responseBody, Map.class);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return ApiResponse.success(newsData);
                } else {
                    // 如果API返回错误，返回错误信息
                    String errorMsg = newsData.containsKey("message") ? 
                        (String) newsData.get("message") : "新闻API请求失败";
                    return ApiResponse.error("新闻API错误: " + errorMsg);
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("新闻API请求超时: " + newsApiUrl);
            return ApiResponse.error("新闻API请求超时，请稍后重试");
        } catch (java.net.ConnectException e) {
            System.err.println("无法连接到新闻API服务: " + newsApiUrl);
            System.err.println("请检查网络连接或 application.properties 中的 news.api.url 配置");
            return ApiResponse.error("无法连接到新闻API服务: " + newsApiUrl);
        } catch (java.io.IOException e) {
            System.err.println("新闻API网络错误: " + e.getMessage());
            return ApiResponse.error("新闻API网络错误: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取新闻失败: " + e.getMessage());
        }
    }
}

