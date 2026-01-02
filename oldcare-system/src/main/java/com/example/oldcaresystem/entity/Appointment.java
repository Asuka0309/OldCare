package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务预约实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("appointments")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 居民用户ID（users表ID，不再是elderly_info表ID） */
    @TableField("resident_id")
    private Long residentId;

    /** 护工ID */
    @TableField("provider_id")
    private Long caregiverId;

    /** 服务ID */
    @TableField("service_id")
    private Long serviceId;

    /** 预约日期时间 */
    @TableField("appointment_date")
    private LocalDateTime appointmentDate;

    /** 预约状态：待确认、已确认、进行中、已完成、已取消 */
    private String status;

    /** 总费用 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
