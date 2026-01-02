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
 * 服务项目实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("services")
public class Service {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 服务项目名称 */
    private String serviceName;

    /** 服务类型：生活护理、医疗护理、心理关怀、健康检查等 */
    private String serviceType;

    /** 服务描述 */
    private String description;

    /** 服务价格 */
    private BigDecimal price;

    /** 服务时长（分钟） */
    private Integer durationMinutes;

    /** 状态：1-可用、0-禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
