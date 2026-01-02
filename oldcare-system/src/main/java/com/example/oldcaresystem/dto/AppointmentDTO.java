package com.example.oldcaresystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    /** 预约ID */
    private Long id;

    /** 老人ID */
    private Long elderlyId;

    /** 护工ID */
    private Long caregiverId;

    /** 服务ID */
    private Long serviceId;

    /** 预约日期时间 */
    private LocalDateTime appointmentDate;

    /** 预约状态 */
    private String status;

    /** 总费用 */
    private BigDecimal totalAmount;

    /** 备注 */
    private String remark;

    /** 老人姓名 */
    private String elderlyName;

    /** 护工姓名 */
    private String caregiverName;

    /** 服务名称 */
    private String serviceName;

    /** 创建时间 */
    private LocalDateTime createdTime;
}
