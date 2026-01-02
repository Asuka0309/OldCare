package com.example.oldcaresystem.controller;

import com.example.oldcaresystem.util.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 天气代理控制器
 * 用于代理天行数据天气API请求，避免CORS问题
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    // 天行数据API Key
    private static final String TIANAPI_KEY = "0bdd9006e7e21b1724a76645ced0edf1";
    private static final String TIANAPI_URL = "https://apis.tianapi.com/tianqi/index";
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取天气信息
     * @param city 城市名称或城市代码，默认北京
     * @return 天气数据
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getWeather(
            @RequestParam(defaultValue = "北京") String city) {
        
        try {
            URL url = new URL(TIANAPI_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            // 构建POST参数
            String content = "key=" + URLEncoder.encode(TIANAPI_KEY, StandardCharsets.UTF_8) +
                           "&city=" + URLEncoder.encode(city, StandardCharsets.UTF_8) +
                           "&type=1";
            
            // 发送POST请求
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            
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
                        return ApiResponse.error("天气API请求失败，状态码: " + responseCode);
                    }
                }
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                // 解析JSON响应
                String responseBody = response.toString();
                System.out.println("天气API响应状态码: " + responseCode);
                System.out.println("天气API响应内容: " + responseBody);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> weatherData = (Map<String, Object>) objectMapper.readValue(responseBody, Map.class);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return ApiResponse.success(weatherData);
                } else {
                    // 如果API返回错误，返回错误信息
                    String errorMsg = weatherData.containsKey("msg") ? 
                        (String) weatherData.get("msg") : "天气API请求失败";
                    return ApiResponse.error("天气API错误: " + errorMsg);
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            return ApiResponse.error("天气API请求超时，请稍后重试");
        } catch (java.io.IOException e) {
            return ApiResponse.error("天气API网络错误: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取天气失败: " + e.getMessage());
        }
    }
}

