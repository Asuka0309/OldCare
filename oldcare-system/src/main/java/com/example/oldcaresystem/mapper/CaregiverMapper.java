package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.Caregiver;
import org.apache.ibatis.annotations.Mapper;

/**
 * 护工信息 Mapper
 */
@Mapper
public interface CaregiverMapper extends BaseMapper<Caregiver> {
}
