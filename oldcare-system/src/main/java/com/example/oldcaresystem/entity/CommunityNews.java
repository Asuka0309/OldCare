package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 社区资讯表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("community_news")
public class CommunityNews {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布管理员ID */
    private Long adminId;

    /** 资讯标题 */
    private String title;

    /** 资讯内容 */
    private String content;

    /** 类别：policy(政策)、health(健康知识)、news(社区新闻) */
    private String category;

    /** 封面图片 */
    private String coverImage;

    /** 状态：draft(草稿)、published(已发布)、archived(已归档) */
    private String status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
