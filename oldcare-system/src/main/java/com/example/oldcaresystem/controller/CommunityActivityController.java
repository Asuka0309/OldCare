package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.entity.CommunityActivity;
import com.example.oldcaresystem.entity.ActivityRegistration;
import com.example.oldcaresystem.security.UserContext;
import com.example.oldcaresystem.service.CommunityActivityService;
import com.example.oldcaresystem.service.ActivityRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 社区活动控制器
 */
@RestController
@RequestMapping("/api/activities")
public class CommunityActivityController {

    @Autowired
    private CommunityActivityService activityService;
    
    @Autowired
    private ActivityRegistrationService registrationService;

    /**
     * 获取活动列表
     */
    @GetMapping
        public ApiResponse<Page<CommunityActivity>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        
        QueryWrapper<CommunityActivity> wrapper = new QueryWrapper<>();
        
        // 角色与状态筛选：非管理员默认只看已发布；若传入status则按传入筛选
        String userRole = UserContext.getRole();
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            if (status != null && !status.isEmpty()) {
                wrapper.eq("status", status);
            } else {
                wrapper.eq("status", "published");
            }
        } else {
            if (status != null && !status.isEmpty()) {
                wrapper.eq("status", status);
            }
        }

        // 类型筛选
        if (type != null && !type.isEmpty()) {
            wrapper.eq("activity_type", type);
        }

        // 标题关键词模糊查询
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("activity_title", keyword);
        }
        
        wrapper.orderByDesc("activity_date");
        
        Page<CommunityActivity> pageData = activityService.page(new Page<>(page, size), wrapper);
        
        // 计算每个活动的当前报名人数
        if (pageData.getRecords() != null) {
            for (CommunityActivity activity : pageData.getRecords()) {
                int count = registrationService.countByActivityId(activity.getId());
                activity.setCurrentParticipants(count);
            }
        }
        
        return ApiResponse.success(pageData);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CommunityActivity> getById(@PathVariable Long id) {
        CommunityActivity activity = activityService.getById(id);
        if (activity == null) {
            return ApiResponse.error("活动不存在");
        }
        // 计算当前报名人数
        int count = registrationService.countByActivityId(activity.getId());
        activity.setCurrentParticipants(count);
        return ApiResponse.success(activity);
    }

    /**
     * 发布活动（管理员）
     */
    @PostMapping
    public ApiResponse<CommunityActivity> create(@RequestBody CommunityActivity activity) {
        Long userId = UserContext.getUserId();
        
        activity.setOrganizerId(userId);
        activity.setStatus("published");
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        
        boolean success = activityService.save(activity);
        if (success) {
            return ApiResponse.success("发布成功", activity);
        }
        return ApiResponse.error("发布失败");
    }

    /**
     * 更新活动（管理员）
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody CommunityActivity activity) {
        CommunityActivity existing = activityService.getById(id);
        if (existing == null) {
            return ApiResponse.error("活动不存在");
        }
        
        activity.setId(id);
        activity.setUpdatedAt(LocalDateTime.now());
        boolean success = activityService.updateById(activity);
        
        if (success) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 取消活动（管理员）
     */
    @PutMapping("/{id}/cancel")
    public ApiResponse<String> cancel(@PathVariable Long id) {
        String role = UserContext.getRole();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("无权限进行取消操作");
        }
        CommunityActivity activity = activityService.getById(id);
        if (activity == null) {
            return ApiResponse.error("活动不存在");
        }
        // 已完成的活动不能取消
        if ("completed".equals(activity.getStatus())) {
            return ApiResponse.error("已完成的活动不能取消");
        }
        activity.setStatus("cancelled");
        activity.setUpdatedAt(LocalDateTime.now());
        boolean success = activityService.updateById(activity);
        if (success) {
            return ApiResponse.success("已取消");
        }
        return ApiResponse.error("取消失败");
    }

    /**
     * 删除活动（管理员，物理删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        String role = UserContext.getRole();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ApiResponse.error("无权限进行删除操作");
        }
        CommunityActivity activity = activityService.getById(id);
        if (activity == null) {
            return ApiResponse.error("活动不存在");
        }
        boolean success = activityService.removeById(id);
        if (success) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }

    /**
     * 报名参加活动（居民）
     */
    @PostMapping("/{id}/register")
    public ApiResponse<String> register(@PathVariable Long id) {
        CommunityActivity activity = activityService.getById(id);
        if (activity == null) {
            return ApiResponse.error("活动不存在");
        }
        
        // 检查活动状态
        if (!"published".equals(activity.getStatus())) {
            return ApiResponse.error("该活动不可报名");
        }
        
        Long userId = UserContext.getUserId();
        
        // 检查是否已报名
        if (registrationService.isRegistered(id, userId)) {
            return ApiResponse.error("您已经报名过此活动");
        }
        
        // 检查报名人数是否已满
        int currentCount = registrationService.countByActivityId(id);
        if (activity.getMaxParticipants() != null && currentCount >= activity.getMaxParticipants()) {
            return ApiResponse.error("报名人数已满");
        }
        
        // 创建报名记录
        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(id);
        registration.setResidentId(userId);
        registration.setRegistrationStatus("registered");
        registration.setCreatedTime(LocalDateTime.now());
        registration.setUpdatedTime(LocalDateTime.now());
        
        boolean success = registrationService.save(registration);
        if (success) {
            return ApiResponse.success("报名成功");
        }
        return ApiResponse.error("报名失败");
    }

    /**
     * 取消报名（居民）
     */
    @DeleteMapping("/{id}/register")
    public ApiResponse<String> unregister(@PathVariable Long id) {
        CommunityActivity activity = activityService.getById(id);
        if (activity == null) {
            return ApiResponse.error("活动不存在");
        }
        
        Long userId = UserContext.getUserId();
        
        // 查找报名记录
        QueryWrapper<ActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", id);
        wrapper.eq("resident_id", userId);
        wrapper.eq("registration_status", "registered");
        
        ActivityRegistration registration = registrationService.getOne(wrapper);
        if (registration == null) {
            return ApiResponse.error("您未报名此活动");
        }
        
        // 更新报名状态为已取消
        registration.setRegistrationStatus("cancelled");
        registration.setUpdatedTime(LocalDateTime.now());
        
        boolean success = registrationService.updateById(registration);
        if (success) {
            return ApiResponse.success("已取消报名");
        }
        return ApiResponse.error("取消失败");
    }

    /**
     * 标记活动为已完成（管理员）
     */
    @PutMapping("/{id}/complete")
    public ApiResponse<String> complete(@PathVariable Long id) {
        CommunityActivity activity = activityService.getById(id);
        if (activity == null) {
            return ApiResponse.error("活动不存在");
        }
        
        activity.setStatus("completed");
        activity.setUpdatedAt(LocalDateTime.now());
        boolean success = activityService.updateById(activity);
        
        if (success) {
            return ApiResponse.success("已标记为完成");
        }
        return ApiResponse.error("操作失败");
    }
}
