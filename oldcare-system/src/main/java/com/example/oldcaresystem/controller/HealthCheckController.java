package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.HealthCheck;
import com.example.oldcaresystem.service.HealthCheckService;
import com.example.oldcaresystem.util.ResponseUtil;
import com.example.oldcaresystem.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/health-check")
public class HealthCheckController {

    @Autowired
    private HealthCheckService healthCheckService;

    /**
     * 新增健康检查记录（管理员和护工可以新增）
     */
    @PostMapping
    public ResponseUtil<HealthCheck> add(@RequestBody HealthCheck healthCheck) {
        String role = UserContext.getRole();
        if (role == null) {
            return ResponseUtil.unauthorized("未登录或凭证失效");
        }
        // 居民只能查看，不能新增
        if ("resident".equalsIgnoreCase(role) || "RESIDENT".equalsIgnoreCase(role)) {
            return ResponseUtil.forbidden("居民无权新增健康检查记录");
        }
        healthCheck.setCheckDate(LocalDateTime.now());
        healthCheck.setCreatedTime(LocalDateTime.now());
        healthCheck.setUpdatedTime(LocalDateTime.now());
        healthCheckService.save(healthCheck);
        return ResponseUtil.success("健康检查记录保存成功", healthCheck);
    }

    /**
     * 修改健康检查记录（管理员和护工可以编辑）
     */
    @PutMapping
    public ResponseUtil<HealthCheck> update(@RequestBody HealthCheck healthCheck) {
        String role = UserContext.getRole();
        if (role == null) {
            return ResponseUtil.unauthorized("未登录或凭证失效");
        }
        // 居民只能查看，不能编辑
        if ("resident".equalsIgnoreCase(role) || "RESIDENT".equalsIgnoreCase(role)) {
            return ResponseUtil.forbidden("居民无权编辑健康检查记录");
        }
        healthCheck.setUpdatedTime(LocalDateTime.now());
        healthCheckService.updateById(healthCheck);
        return ResponseUtil.success("修改成功", healthCheck);
    }

    /**
     * 删除健康检查记录（仅管理员可以删除）
     */
    @DeleteMapping("/{id}")
    public ResponseUtil<String> delete(@PathVariable Long id) {
        String role = UserContext.getRole();
        if (role == null) {
            return ResponseUtil.unauthorized("未登录或凭证失效");
        }
        // 只有管理员可以删除
        if (!"admin".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseUtil.forbidden("仅管理员可删除健康检查记录");
        }
        healthCheckService.removeById(id);
        return ResponseUtil.success("删除成功");
    }

    /**
     * 查询单个健康检查记录（所有角色都可以查看）
     */
    @GetMapping("/{id}")
    public ResponseUtil<HealthCheck> getById(@PathVariable Long id) {
        HealthCheck healthCheck = healthCheckService.getById(id);
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        if (role == null) {
            return ResponseUtil.unauthorized("未登录或凭证失效");
        }
        if (healthCheck == null) {
            return ResponseUtil.success((HealthCheck) null);
        }
        // 居民只能查看自己的记录，管理员和护工可以查看所有
        if ("resident".equalsIgnoreCase(role) || "RESIDENT".equalsIgnoreCase(role)) {
            if (userId == null || !userId.equals(healthCheck.getResidentId())) {
                return ResponseUtil.forbidden("无权查看他人的健康检查记录");
            }
        }
        return ResponseUtil.success(healthCheck);
    }

    /**
     * 分页查询健康检查记录
     */
    @GetMapping
    public ResponseUtil<?> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        if (role == null) {
            return ResponseUtil.unauthorized("未登录或凭证失效");
        }
        QueryWrapper<HealthCheck> queryWrapper = new QueryWrapper<>();
        // 角色过滤：居民只能查看自己的，管理员和护工可以查看所有
        if ("resident".equalsIgnoreCase(role) || "RESIDENT".equalsIgnoreCase(role)) {
            if (userId == null) {
                return ResponseUtil.unauthorized("未登录或凭证失效");
            }
            queryWrapper.eq("resident_id", userId);
        } else {
            // 管理员和护工可以查看所有，也可以按residentId筛选
            if (residentId != null) {
                queryWrapper.eq("resident_id", residentId);
            }
        }
        // 日期范围过滤（按照检查日期）
        try {
            if (startDate != null && !startDate.isEmpty()) {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                queryWrapper.ge("check_date", start.atStartOfDay());
            }
            if (endDate != null && !endDate.isEmpty()) {
                java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                queryWrapper.le("check_date", end.atTime(23, 59, 59));
            }
        } catch (Exception e) {
            return ResponseUtil.paramError("日期格式错误，应为YYYY-MM-DD");
        }
        queryWrapper.orderByDesc("check_date");
        Page<HealthCheck> page = new Page<>(current, size);
        Page<HealthCheck> result = healthCheckService.page(page, queryWrapper);
        
        // 返回前端期望的结构
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        return ResponseUtil.success(response);
    }

    /**
     * 查询老人的所有健康检查记录
     */
    @GetMapping("/resident/{residentId}")
    public ResponseUtil<Page<HealthCheck>> getHealthChecksByResidentId(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        QueryWrapper<HealthCheck> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resident_id", residentId);
        queryWrapper.orderByDesc("check_date");
        Page<HealthCheck> page = new Page<>(current, size);
        Page<HealthCheck> result = healthCheckService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询老人的最新健康检查记录
     */
    @GetMapping("/resident/{residentId}/latest")
    public ResponseUtil<HealthCheck> getLatestHealthCheck(@PathVariable Long residentId) {
        QueryWrapper<HealthCheck> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resident_id", residentId);
        queryWrapper.orderByDesc("check_date");
        queryWrapper.last("LIMIT 1");
        HealthCheck healthCheck = healthCheckService.getOne(queryWrapper);
        return ResponseUtil.success(healthCheck);
    }
}
