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
 * 紧急求助表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("emergency_helps")
public class EmergencyHelp {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（发起求助的居民） */
    @TableField("resident_id")
    private Long userId;

    /** 求助类型：accident(意外)、medical(医疗)、other(其他) */
    @TableField("help_type")
    private String helpType;

    /** 求助描述 */
    private String description;

    /** 当前位置（GPS坐标或地址） */
    private String location;

    /** 状态：pending(待处理)、in_progress(处理中)、resolved(已解决)、cancelled(已取消) */
    private String status;

    /** 响应者ID（管理员或服务提供商） */
    @TableField("assigned_admin_id")
    private Long responderId;

    /** 响应时间 */
    @TableField("response_time")
    private LocalDateTime responseTime;

    /** 响应备注 */
    private String responseNote;

    /** 解决时间 */
    @TableField("resolved_time")
    private LocalDateTime resolvedTime;

    /** 已通知的联系人(JSON格式) */
    @TableField("notified_contacts")
    private String notifiedContacts;

    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updatedAt;

    // 非持久化字段：响应者姓名
    @TableField(exist = false)
    private String responderName;
}

