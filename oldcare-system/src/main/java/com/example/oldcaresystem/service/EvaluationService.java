package com.example.oldcaresystem.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.Evaluation;
import com.example.oldcaresystem.mapper.EvaluationMapper;
import org.springframework.stereotype.Service;

/**
 * 评价反馈服务类
 */
@Service
public class EvaluationService extends ServiceImpl<EvaluationMapper, Evaluation> {
}
