package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报价表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("quotations")
public class Quotation {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 服务需求ID */
    private Long serviceNeedId;

    /** 服务提供商ID */
    private Long providerId;

    /** 报价金额 */
    private BigDecimal quotedPrice;

    /** 报价说明 */
    private String description;

    /** 有效期（天） */
    private Integer validityDays;

    /** 状态：pending(待选择)、accepted(已接受)、rejected(已拒绝)、expired(已过期) */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
