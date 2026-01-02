package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.dto.LoginRequest;
import com.example.oldcaresystem.dto.LoginResponse;
import com.example.oldcaresystem.entity.User;
import com.example.oldcaresystem.entity.Caregiver;
import com.example.oldcaresystem.exception.BusinessException;
import com.example.oldcaresystem.mapper.UserMapper;
import com.example.oldcaresystem.service.CaregiverService;
import com.example.oldcaresystem.util.JwtUtil;
import com.example.oldcaresystem.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户服务类
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CaregiverService caregiverService;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 验证参数
        if (request == null || !isValidUsername(request.getUsername()) || !isValidPassword(request.getPassword())) {
            throw new BusinessException(400, "用户名或密码为空");
        }

        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = getOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(400, "用户已被禁用");
        }

        // 验证密码
        String encryptedPassword = MD5Util.encrypt(request.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(400, "密码错误");
        }

        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 返回登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setRealName(user.getRealName());

        return response;
    }

    /**
     * 用户注册
     */
    public void register(LoginRequest request, String role) {
        // 验证参数
        if (!isValidUsername(request.getUsername()) || !isValidPassword(request.getPassword())) {
            throw new BusinessException(400, "用户名或密码不符合要求");
        }

        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User existUser = getOne(queryWrapper);

        if (existUser != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(MD5Util.encrypt(request.getPassword()));
        user.setRole(role == null ? "elderly_family" : role);
        user.setStatus(1);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        save(user);

        // 如果是护工角色，自动创建护工档案，主键与用户ID一致，便于关联预约/provider_id
        if ("caregiver".equals(user.getRole())) {
            Caregiver caregiver = new Caregiver();
            caregiver.setId(user.getId()); // 与用户ID对齐，方便provider_id引用
            caregiver.setName(user.getUsername());
            caregiver.setStatus("active");
            caregiver.setCreatedTime(LocalDateTime.now());
            caregiver.setUpdatedTime(LocalDateTime.now());
            caregiverService.save(caregiver);
        }
    }

    /**
     * 验证用户名有效性
     */
    private boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && username.length() <= 50;
    }

    /**
     * 验证密码有效性
     */
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * 根据用户ID查询用户
     */
    public User getUserById(Long userId) {
        return getById(userId);
    }

    /**
     * 根据用户名查询用户
     */
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    /**
     * 为指定角色预配一个用户（默认密码：123456，MD5加密）。
     * 若期望用户名已存在，则生成一个唯一用户名（role+时间戳片段）。
     */
    public User provisionUser(String desiredUsername, String realName, String phone, String role) {
        String finalRole = Objects.requireNonNullElse(role, "resident");

        String candidate = (desiredUsername != null && !desiredUsername.isEmpty())
                ? desiredUsername
                : finalRole + (System.currentTimeMillis() % 100000);

        // 确保用户名唯一
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", candidate);
        User existing = getOne(query);
        if (existing != null) {
            candidate = finalRole + (System.currentTimeMillis() % 100000);
        }

        User user = new User();
        user.setUsername(candidate);
        user.setPassword(MD5Util.encrypt("123456"));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(finalRole);
        user.setStatus(1);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        save(user);
        return user;
    }
}
