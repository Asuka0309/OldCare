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
 * 社区活动表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("community_activities")
public class CommunityActivity {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 组织者ID（管理员） */
    @TableField("admin_id")
    private Long organizerId;

    /** 活动标题 */
    @TableField("activity_title")
    private String activityTitle;

    /** 活动描述 */
    @TableField("activity_description")
    private String activityDescription;

    /** 活动类型：education(老年大学)、interest(兴趣小组)、festival(节日活动)、other(其他) */
    @TableField("activity_type")
    private String activityType;

    /** 活动日期时间 */
    @TableField("activity_date")
    private LocalDateTime activityDate;

    /** 活动地点 */
    private String location;

    /** 最多参与人数 */
    @TableField("max_participants")
    private Integer maxParticipants;

    /** 当前参与人数 */
    @TableField(exist = false)
    private Integer currentParticipants;

    /** 状态：draft(草稿)、published(已发布)、completed(已完成)、cancelled(已取消) */
    private String status;

    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updatedAt;
}
