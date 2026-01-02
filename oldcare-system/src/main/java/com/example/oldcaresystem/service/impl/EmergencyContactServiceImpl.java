package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.EmergencyContact;
import com.example.oldcaresystem.mapper.EmergencyContactMapper;
import com.example.oldcaresystem.service.EmergencyContactService;
import org.springframework.stereotype.Service;

/**
 * 紧急联系人服务实现
 */
@Service
public class EmergencyContactServiceImpl extends ServiceImpl<EmergencyContactMapper, EmergencyContact> implements EmergencyContactService {
}
