package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.entity.HealthRecord;
import com.example.oldcaresystem.entity.ElderlyInfo;
import com.example.oldcaresystem.security.UserContext;
import com.example.oldcaresystem.service.UserService;
import com.example.oldcaresystem.entity.User;
import com.example.oldcaresystem.service.HealthRecordService;
import com.example.oldcaresystem.service.ElderlyInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康档案控制器
 */
@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private ElderlyInfoService elderlyInfoService;

    @Autowired
    private UserService userService;

    /**
     * 获取我的健康档案列表
     */
    @GetMapping("/my")
    public ApiResponse<Page<HealthRecord>> getMyRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = UserContext.getUserId();

        QueryWrapper<HealthRecord> wrapper = new QueryWrapper<>();

        // 兼容旧数据：user_id 可能存放 elderly_info.id
        java.util.List<Long> residentIds = buildResidentIds(userId);
        if (residentIds.size() == 1) {
            wrapper.eq("user_id", residentIds.get(0));
        } else {
            wrapper.in("user_id", residentIds);
        }
        if (recordType != null && !recordType.isEmpty()) {
            wrapper.eq("record_type", recordType);
        }
        if (startDate != null && endDate != null) {
            wrapper.between("record_date", LocalDate.parse(startDate), LocalDate.parse(endDate));
        }
        wrapper.orderByDesc("record_date").orderByDesc("created_at");

        Page<HealthRecord> pageData = healthRecordService.page(new Page<>(page, size), wrapper);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取健康档案分页列表
     * 管理员和护工可以查看所有，居民只能查看自己的
     */
    @GetMapping
    public ApiResponse<Page<HealthRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        String role = UserContext.getRole();
        Long currentUserId = UserContext.getUserId();
        
        QueryWrapper<HealthRecord> wrapper = new QueryWrapper<>();
        
        // 居民只能查看自己的记录
        if ("resident".equalsIgnoreCase(role)) {
            if (currentUserId == null) {
                return ApiResponse.error("未登录或凭证失效");
            }
            java.util.List<Long> residentIds = buildResidentIds(currentUserId);
            if (residentIds.size() == 1) {
                wrapper.eq("user_id", residentIds.get(0));
            } else {
                wrapper.in("user_id", residentIds);
            }
        } else {
            // 管理员和护工可以查看所有，也可以按userId筛选
            if (userId != null) {
                java.util.List<Long> residentIds = buildResidentIds(userId);
                if (residentIds.size() == 1) {
                    wrapper.eq("user_id", residentIds.get(0));
                } else {
                    wrapper.in("user_id", residentIds);
                }
            }
        }
        
        if (recordType != null && !recordType.isEmpty()) {
            wrapper.eq("record_type", recordType);
        }
        if (startDate != null && endDate != null) {
            wrapper.between("record_date", LocalDate.parse(startDate), LocalDate.parse(endDate));
        }
        wrapper.orderByDesc("record_date").orderByDesc("created_at");
        
        Page<HealthRecord> pageData = healthRecordService.page(new Page<>(page, size), wrapper);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取健康档案详情（所有角色都可以查看）
     */
    @GetMapping("/{id}")
    public ApiResponse<HealthRecord> getById(@PathVariable Long id) {
        HealthRecord record = healthRecordService.getById(id);
        if (record == null) {
            return ApiResponse.error("健康档案不存在");
        }
        
        String role = UserContext.getRole();
        Long currentUserId = UserContext.getUserId();
        
        // 居民只能查看自己的记录
        if ("resident".equalsIgnoreCase(role)) {
            if (currentUserId == null || !currentUserId.equals(record.getUserId())) {
                return ApiResponse.error("无权查看他人的健康档案");
            }
        }
        
        return ApiResponse.success(record);
    }

    /**
     * 添加健康档案（管理员或护工可新增）
     */
    @PostMapping
    public ApiResponse<HealthRecord> create(@RequestBody HealthRecord record) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        
        // 管理员或护工可新增
        if (!"admin".equalsIgnoreCase(role) && !"caregiver".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "仅管理员或护工可新增健康档案");
        }
        
        // 录入目标用户ID必填（健康记录归属的居民 userId）
        if (record.getUserId() == null) {
            return ApiResponse.error(400, "userId 不能为空");
        }
        
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        
        boolean success = healthRecordService.save(record);
        if (success) {
            return ApiResponse.success("添加成功", record);
        }
        return ApiResponse.error("添加失败");
    }

    /**
     * 更新健康档案（管理员或护工可以编辑）
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody HealthRecord record) {
        HealthRecord existing = healthRecordService.getById(id);
        if (existing == null) {
            return ApiResponse.error("健康档案不存在");
        }
        
        String role = UserContext.getRole();
        // 管理员或护工可编辑
        if (!"admin".equalsIgnoreCase(role) && !"caregiver".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "仅管理员或护工可编辑健康档案");
        }
        
        record.setId(id);
        record.setUserId(existing.getUserId());
        record.setCreatedAt(existing.getCreatedAt());
        record.setUpdatedAt(LocalDateTime.now());
        boolean success = healthRecordService.updateById(record);
        
        if (success) {
            return ApiResponse.success("更新成功");
        }
        return ApiResponse.error("更新失败");
    }

    /**
     * 删除健康档案（仅管理员可以删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        HealthRecord existing = healthRecordService.getById(id);
        if (existing == null) {
            return ApiResponse.error("健康档案不存在");
        }
        
        String role = UserContext.getRole();
        
        // 只有管理员可以删除
        if (!"admin".equalsIgnoreCase(role)) {
            return ApiResponse.error("仅管理员可删除健康档案");
        }
        
        boolean success = healthRecordService.removeById(id);
        if (success) {
            return ApiResponse.success("删除成功");
        }
        return ApiResponse.error("删除失败");
    }

    /**
     * 构造居民可见的ID列表：自身userId + 绑定的elderly_info.id + 根据姓名匹配的elderly_info.id（用于旧数据无user_id的兜底）
     */
    private java.util.List<Long> buildResidentIds(Long userId) {
        java.util.List<Long> residentIds = new java.util.ArrayList<>();
        if (userId == null) {
            return residentIds;
        }
        residentIds.add(userId);

        User user = userService.getById(userId);
        String realName = user != null ? user.getRealName() : null;

        // 绑定 user_id 的老人档案
        QueryWrapper<ElderlyInfo> byUser = new QueryWrapper<ElderlyInfo>().eq("user_id", userId);
        java.util.List<ElderlyInfo> elderlyList = elderlyInfoService.list(byUser);
        if (elderlyList != null) {
            for (ElderlyInfo info : elderlyList) {
                if (info.getId() != null) {
                    residentIds.add(info.getId());
                }
            }
        }

        // 兜底：按姓名匹配无 user_id 的老人档案（旧数据）
        if (realName != null && !realName.isEmpty()) {
            QueryWrapper<ElderlyInfo> byName = new QueryWrapper<ElderlyInfo>()
                    .isNull("user_id")
                    .eq("name", realName);
            java.util.List<ElderlyInfo> elderlyByName = elderlyInfoService.list(byName);
            if (elderlyByName != null) {
                for (ElderlyInfo info : elderlyByName) {
                    if (info.getId() != null) {
                        residentIds.add(info.getId());
                    }
                }
            }
        }

        return residentIds;
    }
}
