package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.Quotation;
import com.example.oldcaresystem.mapper.QuotationMapper;
import com.example.oldcaresystem.service.QuotationService;
import org.springframework.stereotype.Service;

/**
 * 报价服务实现
 */
@Service
public class QuotationServiceImpl extends ServiceImpl<QuotationMapper, Quotation> implements QuotationService {
}
