package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.HealthReminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康提醒Mapper
 */
@Mapper
public interface HealthReminderMapper extends BaseMapper<HealthReminder> {
}
