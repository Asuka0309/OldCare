package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康档案表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("health_records")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 记录类型：checkup(体检)、diagnosis(诊断)、medication(用药)、other(其他) */
    @TableField("record_type")
    private String recordType;

    /** 记录日期 */
    @TableField("record_date")
    private LocalDate recordDate;

    /** 记录标题 */
    @TableField("title")
    private String title;

    /** 记录详情 */
    @TableField("details")
    private String details;

    /** 医院/诊所 */
    @TableField("hospital")
    private String hospital;

    /** 医生姓名 */
    @TableField("doctor_name")
    private String doctorName;

    /** 附件URL */
    @TableField("attachment_url")
    private String attachmentUrl;

    /** 其他备注 */
    @TableField("notes")
    private String notes;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
