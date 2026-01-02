package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.Caregiver;
import com.example.oldcaresystem.mapper.CaregiverMapper;
import org.springframework.stereotype.Service;

/**
 * 护工信息服务类
 */
@Service
public class CaregiverService extends ServiceImpl<CaregiverMapper, Caregiver> {
}
