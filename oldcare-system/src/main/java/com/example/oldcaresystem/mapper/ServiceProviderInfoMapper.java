package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.ServiceProviderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务提供商信息Mapper
 */
@Mapper
public interface ServiceProviderInfoMapper extends BaseMapper<ServiceProviderInfo> {
}
