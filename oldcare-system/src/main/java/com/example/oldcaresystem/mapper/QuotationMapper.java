package com.example.oldcaresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.oldcaresystem.entity.Quotation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报价Mapper
 */
@Mapper
public interface QuotationMapper extends BaseMapper<Quotation> {
}
