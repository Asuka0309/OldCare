package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.FeeRecord;
import com.example.oldcaresystem.mapper.FeeRecordMapper;
import org.springframework.stereotype.Service;

/**
 * 费用记录管理服务类
 */
@Service
public class FeeRecordService extends ServiceImpl<FeeRecordMapper, FeeRecord> {
}
