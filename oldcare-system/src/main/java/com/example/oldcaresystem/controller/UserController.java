package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.*;
import com.example.oldcaresystem.service.*;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.service.NotificationService;
import com.example.oldcaresystem.util.MD5Util;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ServiceProviderInfoService serviceProviderInfoService;

    @Autowired
    private ElderlyInfoService elderlyInfoService;

    @Autowired
    private CaregiverService caregiverService;

    @PostMapping("/register")
    @Transactional
    public ApiResponse<?> register(@RequestBody Map<String, Object> requestBody) {
        String username = (String) requestBody.get("username");
        String password = (String) requestBody.get("password");
        String realName = (String) requestBody.get("realName");
        String phone = (String) requestBody.get("phone");
        String role = (String) requestBody.get("role");
        String companyName = (String) requestBody.get("companyName");

        // 验证必填字段
        if (username == null || username.isEmpty()) {
            return ApiResponse.error(400, "用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            return ApiResponse.error(400, "密码至少6位");
        }
        if (role == null || role.isEmpty()) {
            role = "resident"; // 默认为居民
        }

        // 验证角色
        if (!role.equals("admin") && !role.equals("resident") && !role.equals("service_provider") && !role.equals("caregiver")) {
            return ApiResponse.error(400, "无效的角色类型");
        }

        // 检查用户名是否已存在
        User existing = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .one();
        if (existing != null) {
            return ApiResponse.error(400, "用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(MD5Util.encrypt(password));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(1);
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        boolean success = userService.save(user);
        if (!success) {
            return ApiResponse.error(500, "注册失败");
        }

        // 根据角色创建对应的额外记录
        try {
            if ("resident".equals(role)) {
                // 为居民创建 elderly_info 记录
                ElderlyInfo elderlyInfo = new ElderlyInfo();
                elderlyInfo.setUserId(user.getId());
                elderlyInfo.setName(realName);
                elderlyInfo.setPhone(phone);
                elderlyInfo.setCreatedTime(LocalDateTime.now());
                elderlyInfo.setUpdatedTime(LocalDateTime.now());
                elderlyInfoService.save(elderlyInfo);
            } else if ("caregiver".equals(role)) {
                // 为护工创建 caregiver 记录
                Caregiver caregiver = new Caregiver();
                caregiver.setUserId(user.getId());
                caregiver.setName(realName);
                caregiver.setPhone(phone);
                caregiver.setCompanyName(companyName != null ? companyName : "");
                caregiver.setStatus("active");
                caregiver.setCreatedTime(LocalDateTime.now());
                caregiver.setUpdatedTime(LocalDateTime.now());
                caregiverService.save(caregiver);
            } else if ("service_provider".equals(role)) {
                // 为服务提供商创建信息记录
                ServiceProviderInfo providerInfo = new ServiceProviderInfo();
                providerInfo.setUserId(user.getId());
                providerInfo.setCompanyName(companyName != null ? companyName : realName);
                providerInfo.setRating(new BigDecimal("5.0"));
                providerInfo.setCompletedOrders(0);
                providerInfo.setApprovalStatus("pending"); // 待审核
                providerInfo.setCreatedTime(LocalDateTime.now());
                providerInfo.setUpdatedTime(LocalDateTime.now());
                serviceProviderInfoService.save(providerInfo);
            }
        } catch (Exception e) {
            // 如果创建额外记录失败，删除已创建的用户
            userService.removeById(user.getId());
            return ApiResponse.error(500, "注册失败：" + e.getMessage());
        }

        return ApiResponse.success("注册成功");
    }

    @GetMapping
    public ApiResponse<IPage<User>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        Page<User> page = new Page<>(current, size);
        IPage<User> result = userService.lambdaQuery()
                .like(username != null, User::getUsername, username)
                .eq(role != null, User::getRole, role)
                .orderByDesc(User::getId)
                .page(page);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<?> add(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return ApiResponse.error(400, "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return ApiResponse.error(400, "密码至少6位");
        }
        
        // Check if username already exists
        User existing = userService.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        if (existing != null) {
            return ApiResponse.error(400, "用户名已存在");
        }

        // Hash password with MD5
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        
        boolean success = userService.save(user);
        if (success) {
            return ApiResponse.success("已创建");
        } else {
            return ApiResponse.error(500, "创建失败");
        }
    }

    @PutMapping
    public ApiResponse<?> update(@RequestBody User user) {
        if (user.getId() == null) {
            return ApiResponse.error(400, "ID不能为空");
        }

        User existing = userService.getById(user.getId());
        if (existing == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        // Only allow updating these fields
        existing.setRealName(user.getRealName());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole());
        existing.setStatus(user.getStatus());

        boolean success = userService.updateById(existing);
        if (success) {
            return ApiResponse.success("已更新");
        } else {
            return ApiResponse.error(500, "更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        // 先删除以该用户为接收者的通知，避免外键约束
        notificationService.deleteByReceiver(id);

        boolean success = userService.removeById(id);
        if (success) {
            return ApiResponse.success("已删除");
        } else {
            return ApiResponse.error(500, "删除失败");
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        user.setStatus(status);
        boolean success = userService.updateById(user);
        if (success) {
            return ApiResponse.success("已更新");
        } else {
            return ApiResponse.error(500, "更新失败");
        }
    }
}
