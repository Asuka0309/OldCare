package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价反馈实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("evaluations")
public class Evaluation {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约ID (必填) */
    private Long appointmentId;

    /** 居民ID */
    private Long residentId;

    /** 服务提供商ID */
    private Long providerId;

    /** 服务质量评分（1-5分） */
    private Double serviceQualityRating;

    /** 服务态度评分（1-5分） */
    private Double attitudeRating;

    /** 服务效果评分（1-5分） */
    private Double effectRating;

    /** 总体评分（1-5分） */
    private Double overallRating;

    /** 评价内容 */
    private String comment;

    /** 是否投诉：0-否、1-是 */
    private Boolean isComplaint;

    /** 投诉原因 */
    private String complaintReason;

    /** 状态：已评价、已回复、已解决 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
