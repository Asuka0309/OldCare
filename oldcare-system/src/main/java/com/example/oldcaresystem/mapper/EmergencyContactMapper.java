package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.EmergencyContact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 紧急联系人Mapper
 */
@Mapper
public interface EmergencyContactMapper extends BaseMapper<EmergencyContact> {
}
