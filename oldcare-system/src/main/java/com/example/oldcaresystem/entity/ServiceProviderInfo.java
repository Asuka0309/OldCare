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
 * 服务提供商信息表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("service_provider_info")
public class ServiceProviderInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 公司/门店名称 */
    private String companyName;

    /** 营业执照 */
    private String businessLicense;

    /** 资质认证 */
    private String certification;

    /** 服务提供商描述 */
    private String description;

    /** 评分（5分制） */
    private BigDecimal rating;

    /** 完成订单数 */
    private Integer completedOrders;

    /** 审核状态：pending(待审核)、approved(已批准)、rejected(已拒绝) */
    private String approvalStatus;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
