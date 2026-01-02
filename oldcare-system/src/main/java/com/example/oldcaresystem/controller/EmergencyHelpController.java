package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.entity.EmergencyHelp;
import com.example.oldcaresystem.security.UserContext;
import com.example.oldcaresystem.service.EmergencyHelpService;
import com.example.oldcaresystem.service.NotificationService;
import com.example.oldcaresystem.service.UserService;
import com.example.oldcaresystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 紧急求助控制器
 */
@RestController
@RequestMapping("/api/emergency-help")
public class EmergencyHelpController {

    @Autowired
    private EmergencyHelpService emergencyHelpService;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * 获取所有紧急求助（管理员）
     */
    @GetMapping
    public ApiResponse<Page<EmergencyHelp>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        QueryWrapper<EmergencyHelp> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        // 列名与实体映射的实际字段一致
        wrapper.orderByDesc("created_time");
        
        Page<EmergencyHelp> pageData = emergencyHelpService.page(new Page<>(page, size), wrapper);
        pageData.getRecords().forEach(this::fillResponderName);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取我的紧急求助记录
     */
    @GetMapping("/my")
    public ApiResponse<Page<EmergencyHelp>> getMyHelps(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = UserContext.getUserId();
        QueryWrapper<EmergencyHelp> wrapper = new QueryWrapper<>();
        wrapper.eq("resident_id", userId);
        wrapper.orderByDesc("created_time");
        
        Page<EmergencyHelp> pageData = emergencyHelpService.page(new Page<>(page, size), wrapper);
        pageData.getRecords().forEach(this::fillResponderName);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取紧急求助详情
     */
    @GetMapping("/{id}")
    public ApiResponse<EmergencyHelp> getById(@PathVariable Long id) {
        EmergencyHelp emergencyHelp = emergencyHelpService.getById(id);
        if (emergencyHelp == null) {
            return ApiResponse.error("求助记录不存在");
        }
        fillResponderName(emergencyHelp);
        return ApiResponse.success(emergencyHelp);
    }

    /**
     * 发起紧急求助
     */
    @PostMapping
    public ApiResponse<EmergencyHelp> create(@RequestBody EmergencyHelp emergencyHelp) {
        Long userId = UserContext.getUserId();
        
        emergencyHelp.setUserId(userId);
        emergencyHelp.setStatus("pending");
        emergencyHelp.setCreatedAt(LocalDateTime.now());
        emergencyHelp.setUpdatedAt(LocalDateTime.now());
        
        boolean success = emergencyHelpService.save(emergencyHelp);
        if (success) {
            // 向所有护工发送通知
            notificationService.notifyAllCaregivers(
                userId,
                "紧急求助通知",
                "居民【" + userId + "】发起紧急求助：" + emergencyHelp.getDescription(),
                "emergency_help",
                emergencyHelp.getId()
            );
            return ApiResponse.success("求助已发送", emergencyHelp);
        }
        return ApiResponse.error("发送失败");
    }

    /**
     * 响应紧急求助（管理员或护工）
     */
    @PutMapping("/{id}/respond")
    public ApiResponse<String> respond(
            @PathVariable Long id,
            @RequestBody EmergencyHelp request) {
        
        // 检查权限：只有管理员或护工可以响应
        String role = UserContext.getRole();
        if (!"admin".equalsIgnoreCase(role) && !"caregiver".equalsIgnoreCase(role)) {
            return ApiResponse.error("无权限操作，仅管理员或护工可以响应");
        }
        
        EmergencyHelp emergencyHelp = emergencyHelpService.getById(id);
        if (emergencyHelp == null) {
            return ApiResponse.error("求助记录不存在");
        }
        
        Long responderId = UserContext.getUserId();
        
        emergencyHelp.setStatus("responding");
        emergencyHelp.setResponderId(responderId);
        emergencyHelp.setResponseTime(LocalDateTime.now());
        if (request.getResponseNote() != null) {
            emergencyHelp.setResponseNote(request.getResponseNote());
        }
        emergencyHelp.setUpdatedAt(LocalDateTime.now());
        
        boolean success = emergencyHelpService.updateById(emergencyHelp);
        if (success) {
            return ApiResponse.success("已响应");
        }
        return ApiResponse.error("响应失败");
    }

    /**
     * 标记为已解决
     */
    @PutMapping("/{id}/resolve")
    public ApiResponse<String> resolve(
            @PathVariable Long id,
            @RequestBody EmergencyHelp request) {
        
        // 检查权限：只有管理员或护工可以标记已解决
        String role = UserContext.getRole();
        if (!"admin".equalsIgnoreCase(role) && !"caregiver".equalsIgnoreCase(role)) {
            return ApiResponse.error("无权限操作，仅管理员或护工可以标记已解决");
        }
        
        EmergencyHelp emergencyHelp = emergencyHelpService.getById(id);
        if (emergencyHelp == null) {
            return ApiResponse.error("求助记录不存在");
        }
        
        emergencyHelp.setStatus("resolved");
        emergencyHelp.setResolvedTime(LocalDateTime.now());
        if (request.getResponseNote() != null) {
            emergencyHelp.setResponseNote(request.getResponseNote());
        }
        emergencyHelp.setUpdatedAt(LocalDateTime.now());
        
        boolean success = emergencyHelpService.updateById(emergencyHelp);
        if (success) {
            return ApiResponse.success("已解决");
        }
        return ApiResponse.error("操作失败");
    }

    /**
     * 取消紧急求助（仅发起人）
     */
    @PutMapping("/{id}/cancel")
    public ApiResponse<String> cancel(@PathVariable Long id) {
        EmergencyHelp emergencyHelp = emergencyHelpService.getById(id);
        if (emergencyHelp == null) {
            return ApiResponse.error("求助记录不存在");
        }
        Long userId = UserContext.getUserId();
        if (!emergencyHelp.getUserId().equals(userId)) {
            return ApiResponse.error("无权限操作");
        }
        if ("resolved".equalsIgnoreCase(emergencyHelp.getStatus())) {
            return ApiResponse.error("已解决的求助不能取消");
        }
        emergencyHelp.setStatus("cancelled");
        emergencyHelp.setUpdatedAt(LocalDateTime.now());
        boolean success = emergencyHelpService.updateById(emergencyHelp);
        if (success) {
            return ApiResponse.success("已取消");
        }
        return ApiResponse.error("取消失败");
    }

    /**
     * 删除紧急求助（物理删除，仅发起人）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        EmergencyHelp emergencyHelp = emergencyHelpService.getById(id);
        if (emergencyHelp == null) {
            return ApiResponse.error("求助记录不存在");
        }
        Long userId = UserContext.getUserId();
        if (!emergencyHelp.getUserId().equals(userId)) {
            return ApiResponse.error("无权限操作");
        }
        boolean success = emergencyHelpService.removeById(id);
        if (success) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }

    /**
     * 补充响应者姓名，便于前端展示
     */
    private void fillResponderName(EmergencyHelp emergencyHelp) {
        if (emergencyHelp == null || emergencyHelp.getResponderId() == null) {
            return;
        }
        User responder = userService.getById(emergencyHelp.getResponderId());
        if (responder != null) {
            String name = responder.getRealName();
            if (name == null || name.isEmpty()) {
                name = responder.getUsername();
            }
            emergencyHelp.setResponderName(name);
        }
        // 若状态为已响应/已解决但响应时间为空，使用更新时间作为兜底，避免前端展示"-"
        if (("responding".equalsIgnoreCase(emergencyHelp.getStatus()) || "resolved".equalsIgnoreCase(emergencyHelp.getStatus()))
                && emergencyHelp.getResponseTime() == null
                && emergencyHelp.getUpdatedAt() != null) {
            emergencyHelp.setResponseTime(emergencyHelp.getUpdatedAt());
        }
    }
}
