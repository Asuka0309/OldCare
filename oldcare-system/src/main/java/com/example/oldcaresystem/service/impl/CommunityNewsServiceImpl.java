package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.oldcaresystem.entity.CommunityNews;
import com.example.oldcaresystem.mapper.CommunityNewsMapper;
import com.example.oldcaresystem.service.CommunityNewsService;
import org.springframework.stereotype.Service;

/**
 * 社区资讯服务实现
 */
@Service
public class CommunityNewsServiceImpl extends ServiceImpl<CommunityNewsMapper, CommunityNews> implements CommunityNewsService {
}
