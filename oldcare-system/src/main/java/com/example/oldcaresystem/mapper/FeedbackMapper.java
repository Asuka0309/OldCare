package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈Mapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
