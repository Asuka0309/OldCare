package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 健康提醒表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("health_reminders")
public class HealthReminder {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 提醒类型：medication(用药)、checkup(体检)、custom(自定义) */
    private String reminderType;

    /** 提醒内容 */
    private String reminderContent;

    /** 提醒日期 */
    private LocalDate reminderDate;

    /** 提醒时间 */
    private LocalTime reminderTime;

    /** 是否完成：0-未完成、1-已完成 */
    private Integer isCompleted;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
