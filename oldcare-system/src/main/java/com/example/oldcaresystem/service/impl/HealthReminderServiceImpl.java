package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.HealthReminder;
import com.example.oldcaresystem.mapper.HealthReminderMapper;
import com.example.oldcaresystem.service.HealthReminderService;
import org.springframework.stereotype.Service;

/**
 * 健康提醒服务实现
 */
@Service
public class HealthReminderServiceImpl extends ServiceImpl<HealthReminderMapper, HealthReminder> implements HealthReminderService {
}
