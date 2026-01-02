package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.Feedback;
import com.example.oldcaresystem.mapper.FeedbackMapper;
import com.example.oldcaresystem.service.FeedbackService;
import org.springframework.stereotype.Service;

/**
 * 反馈服务实现
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
}
