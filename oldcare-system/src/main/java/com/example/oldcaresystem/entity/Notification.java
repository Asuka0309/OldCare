package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息通知表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者ID（护工或管理员） */
    @TableField("receiver_id")
    private Long receiverId;

    /** 发送者ID（居民或系统） */
    @TableField("sender_id")
    private Long senderId;

    /** 通知类型：emergency_help(紧急求助)、appointment(预约)、evaluation(评价) */
    @TableField("notification_type")
    private String notificationType;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 相关的业务ID（如紧急求助ID） */
    @TableField("related_id")
    private Long relatedId;

    /** 是否已读 */
    private Boolean isRead;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 读取时间 */
    @TableField("read_at")
    private LocalDateTime readAt;
}
