package com.example.oldcaresystem.controller;

import com.example.oldcaresystem.dto.LoginRequest;
import com.example.oldcaresystem.dto.LoginResponse;
import com.example.oldcaresystem.service.UserService;
import com.example.oldcaresystem.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param request 登录请求（包含用户名和密码）
     * @return 登录响应（包含JWT token和用户信息）
     */
    @PostMapping("/login")
    public ResponseUtil<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseUtil.success("登录成功", response);
    }

    /**
     * 用户注册
     *
     * @param request 注册请求（包含用户名和密码）
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseUtil<String> register(@RequestBody LoginRequest request) {
        userService.register(request, null);
        return ResponseUtil.success("注册成功");
    }

    /**
     * 管理员注册护工
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register-caregiver")
    public ResponseUtil<String> registerCaregiver(@RequestBody LoginRequest request) {
        userService.register(request, "caregiver");
        return ResponseUtil.success("护工注册成功");
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseUtil<String> health() {
        return ResponseUtil.success("系统正常运行");
    }
}
