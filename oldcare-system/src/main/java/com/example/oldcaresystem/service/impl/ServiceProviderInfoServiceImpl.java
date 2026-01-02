package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.ServiceProviderInfo;
import com.example.oldcaresystem.mapper.ServiceProviderInfoMapper;
import com.example.oldcaresystem.service.ServiceProviderInfoService;
import org.springframework.stereotype.Service;

/**
 * 服务提供商信息服务实现
 */
@Service
public class ServiceProviderInfoServiceImpl extends ServiceImpl<ServiceProviderInfoMapper, ServiceProviderInfo>
        implements ServiceProviderInfoService {
}
