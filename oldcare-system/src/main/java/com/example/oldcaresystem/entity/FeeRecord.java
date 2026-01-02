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
import java.time.LocalDate;

/**
 * 费用记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fee_records")
public class FeeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约ID */
    @TableField("appointment_id")
    private Long appointmentId;

    /** 居民ID */
    @TableField("resident_id")
    private Long residentId;

    /** 老人ID (兼容字段，映射到resident_id) */
    @TableField(exist = false)
    private Long elderlyId;
    
    /** 居民姓名 (用于前端显示，不映射到数据库) */
    @TableField(exist = false)
    private String residentName;

    /** 服务名称 */
    @TableField("service_name")
    private String serviceName;

    /** 费用金额 */
    private BigDecimal amount;

    /** 费用类型：service(服务费)、materials(材料费)、other(其他) */
    @TableField("fee_type")
    private String feeType;

    /** 支付状态：未支付、已支付、已退款 */
    private String status;

    /** 应付日期 */
    @TableField("due_date")
    private LocalDate dueDate;

    /** 支付时间 */
    @TableField("payment_time")
    private LocalDateTime paymentTime;

    /** 退款时间 */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
