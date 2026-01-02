package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.HealthCheck;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康检查 Mapper
 */
@Mapper
public interface HealthCheckMapper extends BaseMapper<HealthCheck> {
}
