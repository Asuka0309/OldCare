package com.example.oldcaresystem.controller;

import com.example.oldcaresystem.entity.Notification;
import com.example.oldcaresystem.service.NotificationService;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.security.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * 获取我的所有通知（包括已读和未读）
     */
    @GetMapping
    public ApiResponse<List<Notification>> getAllNotifications() {
        Long userId = UserContext.getUserId();
        List<Notification> notifications = notificationService.getAllNotifications(userId);
        return ApiResponse.success(notifications);
    }
    
    /**
     * 获取我的未读通知
     */
    @GetMapping("/unread")
    public ApiResponse<List<Notification>> getUnreadNotifications() {
        Long userId = UserContext.getUserId();
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ApiResponse.success(notifications);
    }
    
    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success("已标记为已读");
    }
}
