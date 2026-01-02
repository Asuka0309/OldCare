package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.HealthRecord;
import com.example.oldcaresystem.mapper.HealthRecordMapper;
import com.example.oldcaresystem.service.HealthRecordService;
import org.springframework.stereotype.Service;

/**
 * 健康档案服务实现
 */
@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {
}
