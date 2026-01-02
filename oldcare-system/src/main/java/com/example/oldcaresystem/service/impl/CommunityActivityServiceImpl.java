package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.CommunityActivity;
import com.example.oldcaresystem.mapper.CommunityActivityMapper;
import com.example.oldcaresystem.service.CommunityActivityService;
import org.springframework.stereotype.Service;

/**
 * 社区活动服务实现
 */
@Service
public class CommunityActivityServiceImpl extends ServiceImpl<CommunityActivityMapper, CommunityActivity> implements CommunityActivityService {
}
