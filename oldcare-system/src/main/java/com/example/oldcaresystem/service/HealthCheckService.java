package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.HealthCheck;
import com.example.oldcaresystem.mapper.HealthCheckMapper;
import org.springframework.stereotype.Service;

/**
 * 健康检查管理服务类
 */
@Service
public class HealthCheckService extends ServiceImpl<HealthCheckMapper, HealthCheck> {
}
