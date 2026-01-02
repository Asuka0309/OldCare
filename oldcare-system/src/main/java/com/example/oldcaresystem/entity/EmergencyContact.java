package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 紧急联系人表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("emergency_contacts")
public class EmergencyContact {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 联系人名称 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 关系：子女、朋友、邻居等 */
    private String relation;

    /** 优先级：1最高 */
    private Integer priority;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
