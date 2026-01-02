package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.oldcaresystem.entity.ActivityRegistration;

/**
 * 活动报名服务接口
 */
public interface ActivityRegistrationService extends IService<ActivityRegistration> {
    
    /**
     * 统计活动的报名人数
     * @param activityId 活动ID
     * @return 报名人数
     */
    int countByActivityId(Long activityId);
    
    /**
     * 检查用户是否已报名
     * @param activityId 活动ID
     * @param residentId 居民ID
     * @return 是否已报名
     */
    boolean isRegistered(Long activityId, Long residentId);
}

