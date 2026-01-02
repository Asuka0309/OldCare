package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.ActivityRegistration;
import com.example.oldcaresystem.mapper.ActivityRegistrationMapper;
import com.example.oldcaresystem.service.ActivityRegistrationService;
import org.springframework.stereotype.Service;

/**
 * 活动报名服务实现
 */
@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration> implements ActivityRegistrationService {

    @Override
    public int countByActivityId(Long activityId) {
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);
        wrapper.eq("registration_status", "registered");
        return (int) this.count(wrapper);
    }

    @Override
    public boolean isRegistered(Long activityId, Long residentId) {
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);
        wrapper.eq("resident_id", residentId);
        wrapper.eq("registration_status", "registered");
        return this.count(wrapper) > 0;
    }
}

