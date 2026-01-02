package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务预约 Mapper
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}
