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
 * 健康检查记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("health_checks")
public class HealthCheck {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 居民ID (users表) */
    @TableField("resident_id")
    private Long residentId;

    /** 检查日期 */
    @TableField("check_date")
    private LocalDateTime checkDate;

    /** 检查类型 */
    @TableField("check_type")
    private String checkType;

    /** 检查结果 */
    @TableField("check_result")
    private String checkResult;

    /** 是否正常 */
    @TableField("normal_status")
    private String normalStatus;

    /** 备注 */
    @TableField("remark")
    private String notes;

    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    // 以下为前端显示用的虚拟字段，不映射到数据库
    @TableField(exist = false)
    private String bloodPressure;

    @TableField(exist = false)
    private Double bloodSugar;

    @TableField(exist = false)
    private Double weight;
}
