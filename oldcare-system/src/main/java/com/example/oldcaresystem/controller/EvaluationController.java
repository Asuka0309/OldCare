package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.Evaluation;
import com.example.oldcaresystem.entity.Appointment;
import com.example.oldcaresystem.entity.ElderlyInfo;
import com.example.oldcaresystem.entity.Caregiver;
import com.example.oldcaresystem.service.EvaluationService;
import com.example.oldcaresystem.service.AppointmentService;
import com.example.oldcaresystem.service.ElderlyInfoService;
import com.example.oldcaresystem.service.CaregiverService;
import com.example.oldcaresystem.util.ResponseUtil;
import com.example.oldcaresystem.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 评价反馈控制器
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ElderlyInfoService elderlyInfoService;

    @Autowired
    private CaregiverService caregiverService;

    /**
     * 新增评价（必须关联预约）
     * 只有居民可以创建评价，且只能为自己的预约创建评价
     */
    @PostMapping
    public ResponseUtil<Evaluation> add(@RequestBody Evaluation evaluation) {
        // 权限检查：只有居民可以创建评价
        String role = UserContext.getRole();
        if (!"resident".equalsIgnoreCase(role)) {
            return ResponseUtil.error(403, "无权限操作，仅居民可以创建评价");
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseUtil.error(401, "用户未登录");
        }

        // 验证必填字段
        if (evaluation.getAppointmentId() == null) {
            return ResponseUtil.error(400, "评价必须关联预约ID");
        }
        if (evaluation.getOverallRating() == null) {
            return ResponseUtil.error(400, "总体评分不能为空");
        }
        if (evaluation.getComment() == null || evaluation.getComment().isEmpty()) {
            return ResponseUtil.error(400, "评价内容不能为空");
        }

        // 验证预约是否存在且属于当前用户
        Appointment appointment = appointmentService.getById(evaluation.getAppointmentId());
        if (appointment == null) {
            return ResponseUtil.error(404, "预约不存在");
        }

        // 验证预约是否已完成
        if (!"已完成".equals(appointment.getStatus())) {
            return ResponseUtil.error(400, "只能对已完成的预约进行评价");
        }

        // 验证预约是否属于当前用户（residentId 现在直接指向 users.id）
        if (!userId.equals(appointment.getResidentId())) {
            return ResponseUtil.error(403, "只能为自己的预约创建评价");
        }

        // 统一由后端绑定居民ID和服务商ID
        // residentId 直接使用预约中的 residentId（users.id）
        evaluation.setResidentId(appointment.getResidentId());

        // providerId 需要符合外键：evaluations.provider_id -> caregivers.id
        // 预约的 caregiverId 已经存储为用户ID（users.id），这里反查到 caregivers.id
        Caregiver caregiver = caregiverService.getOne(new QueryWrapper<Caregiver>().eq("user_id", appointment.getCaregiverId()));
        if (caregiver == null) {
            // 兼容旧数据：若caregiverId本身就是caregivers.id，则直接按主键再试一次
            caregiver = caregiverService.getById(appointment.getCaregiverId());
        }
        if (caregiver == null || caregiver.getId() == null) {
            return ResponseUtil.error(400, "未找到对应的护工信息，无法提交评价");
        }
        evaluation.setProviderId(caregiver.getId());
        
        evaluation.setStatus("已评价");
        evaluation.setCreatedTime(LocalDateTime.now());
        evaluation.setUpdatedTime(LocalDateTime.now());
        evaluationService.save(evaluation);
        return ResponseUtil.success("评价提交成功", evaluation);
    }

    /**
     * 修改评价
     * 只有居民可以编辑评价，且只能编辑自己的评价
     */
    @PutMapping("/{id}")
    public ResponseUtil<Evaluation> update(@PathVariable Long id, @RequestBody Evaluation evaluation) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        Evaluation existing = evaluationService.getById(id);
        if (existing == null) {
            return ResponseUtil.error(404, "评价不存在");
        }

        // 只有居民可以编辑评价
        if (!"resident".equalsIgnoreCase(role)) {
            return ResponseUtil.error(403, "无权限操作，仅居民可以编辑评价");
        }

        // 居民只能修改自己的评价
        if (!userId.equals(existing.getResidentId())) {
            // 如果residentId是elderly_info.id，需要验证
            ElderlyInfo elderlyInfo = elderlyInfoService.getById(existing.getResidentId());
            if (elderlyInfo == null || !userId.equals(elderlyInfo.getUserId())) {
                return ResponseUtil.error(403, "只能修改自己的评价");
            }
        }

        evaluation.setId(id);
        evaluation.setUpdatedTime(LocalDateTime.now());
        evaluationService.updateById(evaluation);
        return ResponseUtil.success("修改成功", evaluation);
    }

    /**
     * 删除评价
     * 居民只能删除自己的评价，管理员可以删除所有评价，护工不能删除评价
     */
    @DeleteMapping("/{id}")
    public ResponseUtil<String> delete(@PathVariable Long id) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        Evaluation existing = evaluationService.getById(id);
        if (existing == null) {
            return ResponseUtil.error(404, "评价不存在");
        }

        // 护工不能删除评价
        if ("caregiver".equalsIgnoreCase(role)) {
            return ResponseUtil.error(403, "无权限操作，护工不能删除评价");
        }

        // 如果不是管理员，验证是否是自己的评价（居民只能删除自己的评价）
        if (!"admin".equalsIgnoreCase(role)) {
            if (!"resident".equalsIgnoreCase(role)) {
                return ResponseUtil.error(403, "无权限操作，仅居民和管理员可以删除评价");
            }
            // 居民只能删除自己的评价
            if (!userId.equals(existing.getResidentId())) {
                // 如果residentId是elderly_info.id，需要验证
                ElderlyInfo elderlyInfo = elderlyInfoService.getById(existing.getResidentId());
                if (elderlyInfo == null || !userId.equals(elderlyInfo.getUserId())) {
                    return ResponseUtil.error(403, "只能删除自己的评价");
                }
            }
        }

        evaluationService.removeById(id);
        return ResponseUtil.success("删除成功");
    }

    /**
     * 查询单个评价
     */
    @GetMapping("/{id}")
    public ResponseUtil<Evaluation> getById(@PathVariable Long id) {
        Evaluation evaluation = evaluationService.getById(id);
        return ResponseUtil.success(evaluation);
    }

    /**
     * 分页查询评价列表
     * 管理员和居民可以看到所有评价，护工只能看到自己的评价
     */
    @GetMapping
    public ResponseUtil<Page<Evaluation>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long appointmentId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Boolean isComplaint) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        
        // 护工只能看到自己的评价（作为provider_id对应的护工）
        if ("caregiver".equalsIgnoreCase(role) && userId != null) {
            // 获取当前用户对应的护工ID
            Caregiver caregiver = caregiverService.getOne(new QueryWrapper<Caregiver>().eq("user_id", userId));
            if (caregiver != null && caregiver.getId() != null) {
                queryWrapper.eq("provider_id", caregiver.getId());
            } else {
                queryWrapper.eq("provider_id", -1L); // 如果不存在护工记录，返回空结果
            }
        }
        // 管理员和居民可以看到所有评价
        if ("resident".equalsIgnoreCase(role) && userId != null) {
            // residentId可能是users.id或elderly_info.id
            QueryWrapper<ElderlyInfo> elderlyWrapper = new QueryWrapper<>();
            elderlyWrapper.eq("user_id", userId);
            java.util.List<ElderlyInfo> elderlyInfoList = elderlyInfoService.list(elderlyWrapper);

            java.util.List<Long> allPossibleIds = new java.util.ArrayList<>();
            allPossibleIds.add(userId); // 添加userId本身

            if (elderlyInfoList != null && !elderlyInfoList.isEmpty()) {
                for (ElderlyInfo info : elderlyInfoList) {
                    if (info.getId() != null) {
                        allPossibleIds.add(info.getId());
                    }
                }
            }
            if (allPossibleIds.size() == 1) {
                queryWrapper.eq("resident_id", allPossibleIds.get(0));
            } else if (allPossibleIds.size() > 1) {
                queryWrapper.in("resident_id", allPossibleIds);
            } else {
                queryWrapper.eq("resident_id", -1L); // 没有匹配的ID，返回空结果
            }
        }

        if (appointmentId != null) {
            queryWrapper.eq("appointment_id", appointmentId);
        }
        if (providerId != null) {
            queryWrapper.eq("provider_id", providerId);
        }
        if (isComplaint != null) {
            queryWrapper.eq("is_complaint", isComplaint);
        }
        queryWrapper.orderByDesc("created_time");
        Page<Evaluation> page = new Page<>(current, size);
        Page<Evaluation> result = evaluationService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询服务提供商的所有评价
     * 管理员、居民和对应的护工可以查看，其他护工不能查看
     */
    @GetMapping("/provider/{providerId}")
    public ResponseUtil<Page<Evaluation>> getEvaluationsByProvider(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        // 护工只能查看自己的评价
        if ("caregiver".equalsIgnoreCase(role) && userId != null) {
            Caregiver caregiver = caregiverService.getOne(new QueryWrapper<Caregiver>().eq("user_id", userId));
            if (caregiver == null || !providerId.equals(caregiver.getId())) {
                return ResponseUtil.error(403, "无权限查看其他护工的评价");
            }
        }

        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("provider_id", providerId);
        queryWrapper.orderByDesc("created_time");
        Page<Evaluation> page = new Page<>(current, size);
        Page<Evaluation> result = evaluationService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询投诉列表
     * 仅管理员和居民可以查看所有投诉，护工只能查看涉及自己的投诉
     */
    @GetMapping("/complaints")
    public ResponseUtil<Page<Evaluation>> getComplaints(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();

        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_complaint", true);

        // 护工只能看到涉及自己的投诉
        if ("caregiver".equalsIgnoreCase(role) && userId != null) {
            Caregiver caregiver = caregiverService.getOne(new QueryWrapper<Caregiver>().eq("user_id", userId));
            if (caregiver != null && caregiver.getId() != null) {
                queryWrapper.eq("provider_id", caregiver.getId());
            } else {
                queryWrapper.eq("provider_id", -1L);
            }
        }
        // 管理员和居民可以看到所有投诉

        queryWrapper.orderByDesc("created_time");
        Page<Evaluation> page = new Page<>(current, size);
        Page<Evaluation> result = evaluationService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }
}
