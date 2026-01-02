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
 * 反馈与建议实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("feedbacks")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提交人ID */
    @TableField("user_id")
    private Long userId;

    /** 提交人角色 */
    @TableField("role")
    private String role;

    /** 反馈标题 */
    @TableField("title")
    private String title;

    /** 反馈类别 */
    @TableField("category")
    private String category;

    /** 反馈内容 */
    @TableField("content")
    private String content;

    /** 状态：pending/processing/resolved */
    @TableField("status")
    private String status;

    /** 处理人ID */
    @TableField("handler_id")
    private Long handlerId;

    /** 处理备注 */
    @TableField("remark")
    private String remark;

    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    // 附加字段，不映射数据库
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String handlerName;
}
