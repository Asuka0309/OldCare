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
 * 活动报名表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_registrations")
public class ActivityRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动ID */
    @TableField("activity_id")
    private Long activityId;

    /** 居民ID */
    @TableField("resident_id")
    private Long residentId;

    /** 报名状态：registered(已报名)、attended(已参加)、cancelled(已取消) */
    @TableField("registration_status")
    private String registrationStatus;

    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}

