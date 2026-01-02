package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.EmergencyHelp;
import com.example.oldcaresystem.mapper.EmergencyHelpMapper;
import com.example.oldcaresystem.service.EmergencyHelpService;
import org.springframework.stereotype.Service;

/**
 * 紧急求助服务实现
 */
@Service
public class EmergencyHelpServiceImpl extends ServiceImpl<EmergencyHelpMapper, EmergencyHelp> implements EmergencyHelpService {
}
