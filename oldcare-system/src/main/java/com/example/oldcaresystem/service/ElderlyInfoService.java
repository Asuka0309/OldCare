package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.ElderlyInfo;
import com.example.oldcaresystem.mapper.ElderlyInfoMapper;
import org.springframework.stereotype.Service;

/**
 * 老人信息服务类
 */
@Service
public class ElderlyInfoService extends ServiceImpl<ElderlyInfoMapper, ElderlyInfo> {
}
