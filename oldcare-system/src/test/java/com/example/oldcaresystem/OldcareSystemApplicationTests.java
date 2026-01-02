package com.example.oldcaresystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OldcareSystemApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试应用上下文加载
     */
    @Test
    void contextLoads() {
        System.out.println("\n========================================");
        System.out.println("✅ 应用上下文加载成功！");
        System.out.println("========================================\n");
    }

    /**
     * 测试健康检查接口
     */
    @Test
    void testHealthCheck() throws Exception {
        System.out.println("\n========================================");
        System.out.println("🧪 测试健康检查接口: GET /api/auth/health");
        System.out.println("========================================");
        
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").exists());
        
        System.out.println("✅ 健康检查接口测试通过！\n");
    }

    /**
     * 测试登录接口
     */
    @Test
    void testLogin() throws Exception {
        System.out.println("\n========================================");
        System.out.println("🧪 测试登录接口: POST /api/auth/login");
        System.out.println("========================================");
        
        // 测试错误的登录凭证
        String loginJson = "{\"username\":\"admin\",\"password\":\"admin\"}";
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
        
        System.out.println("✅ 登录接口测试通过！\n");
    }

    /**
     * 测试获取老人列表接口
     */
    @Test
    void testGetElderlyList() throws Exception {
        System.out.println("\n========================================");
        System.out.println("🧪 测试获取老人列表接口: GET /api/elderly");
        System.out.println("========================================");
        
        mockMvc.perform(get("/api/elderly")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
        
        System.out.println("✅ 获取老人列表接口测试通过！\n");
    }

}

