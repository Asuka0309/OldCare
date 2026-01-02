package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.Feedback;
import com.example.oldcaresystem.entity.User;
import com.example.oldcaresystem.service.FeedbackService;
import com.example.oldcaresystem.service.UserService;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 反馈与建议控制器
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    /**
     * 提交反馈（居民和员工都可以）
     */
    @PostMapping
    public ApiResponse<Feedback> submit(@RequestBody Feedback feedback) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }

        // 居民和护工可以提交
        if (!"resident".equalsIgnoreCase(role) && !"caregiver".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "仅居民和员工可以提交反馈");
        }

        if (feedback.getTitle() == null || feedback.getTitle().trim().isEmpty()) {
            return ApiResponse.error(400, "标题不能为空");
        }
        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            return ApiResponse.error(400, "内容不能为空");
        }

        feedback.setUserId(userId);
        feedback.setRole(role);
        feedback.setStatus("pending");
        feedback.setCreatedTime(LocalDateTime.now());
        feedback.setUpdatedTime(LocalDateTime.now());

        feedbackService.save(feedback);
        return ApiResponse.success("反馈提交成功", feedback);
    }

    /**
     * 查看我的反馈（居民和员工查看自己提交的）
     */
    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> getMyFeedback(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }

        QueryWrapper<Feedback> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_time");

        Page<Feedback> pageData = feedbackService.page(new Page<>(page, size), wrapper);
        
        // 填充用户名和处理人名
        enrichFeedbackNames(pageData.getRecords());

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData.getRecords());
        result.put("total", pageData.getTotal());
        result.put("current", pageData.getCurrent());
        result.put("size", pageData.getSize());
        return ApiResponse.success(result);
    }

    /**
     * 管理员查看所有反馈
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String role) {
        String currentRole = UserContext.getRole();
        if (!"admin".equalsIgnoreCase(currentRole)) {
            return ApiResponse.error(403, "仅管理员可查看所有反馈");
        }

        QueryWrapper<Feedback> wrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq("role", role);
        }
        wrapper.orderByDesc("created_time");

        Page<Feedback> pageData = feedbackService.page(new Page<>(page, size), wrapper);
        
        // 填充用户名和处理人名
        enrichFeedbackNames(pageData.getRecords());

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData.getRecords());
        result.put("total", pageData.getTotal());
        result.put("current", pageData.getCurrent());
        result.put("size", pageData.getSize());
        return ApiResponse.success(result);
    }

    /**
     * 管理员查看单个反馈详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Feedback> getById(@PathVariable Long id) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return ApiResponse.error(404, "反馈不存在");
        }

        // 管理员可查看所有，居民和员工只能查看自己的
        if (!"admin".equalsIgnoreCase(role)) {
            if (!userId.equals(feedback.getUserId())) {
                return ApiResponse.error(403, "只能查看自己的反馈");
            }
        }

        enrichFeedbackNames(java.util.Collections.singletonList(feedback));
        return ApiResponse.success(feedback);
    }

    /**
     * 管理员处理反馈
     */
    @PutMapping("/{id}/resolve")
    public ApiResponse<String> resolve(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        if (!"admin".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "仅管理员可处理反馈");
        }

        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return ApiResponse.error(404, "反馈不存在");
        }

        String remark = request.get("remark");
        String status = request.get("status");

        feedback.setHandlerId(userId);
        feedback.setRemark(remark);
        feedback.setStatus(status != null ? status : "resolved");
        feedback.setUpdatedTime(LocalDateTime.now());

        feedbackService.updateById(feedback);
        return ApiResponse.success("处理成功");
    }

    /**
     * 填充反馈的用户名和处理人名
     */
    private void enrichFeedbackNames(java.util.List<Feedback> feedbacks) {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return;
        }

        for (Feedback feedback : feedbacks) {
            if (feedback.getUserId() != null) {
                User user = userService.getById(feedback.getUserId());
                if (user != null) {
                    feedback.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
            if (feedback.getHandlerId() != null) {
                User handler = userService.getById(feedback.getHandlerId());
                if (handler != null) {
                    feedback.setHandlerName(handler.getRealName() != null ? handler.getRealName() : handler.getUsername());
                }
            }
        }
    }
}
