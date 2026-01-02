package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.Appointment;
import com.example.oldcaresystem.mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

/**
 * 服务预约管理服务类
 */
@Service
public class AppointmentService extends ServiceImpl<AppointmentMapper, Appointment> {
}
