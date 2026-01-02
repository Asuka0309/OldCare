package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oldcaresystem.entity.Notification;

import java.util.List;

/**
 * 消息通知服务
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 创建并发送通知给所有护工
     */
    void notifyAllCaregivers(Long residentId, String title, String content, String notificationType, Long relatedId);
    
    /**
     * 获取用户的所有通知（包括已读和未读）
     */
    List<Notification> getAllNotifications(Long userId);
    
    /**
     * 获取用户未读通知
     */
    List<Notification> getUnreadNotifications(Long userId);
    
    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 删除某个接收者的所有通知（用于删除用户前清理外键）
     */
    void deleteByReceiver(Long userId);
}
