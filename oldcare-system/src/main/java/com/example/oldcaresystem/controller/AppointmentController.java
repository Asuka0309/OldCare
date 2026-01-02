package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.Appointment;
import com.example.oldcaresystem.entity.ElderlyInfo;
import com.example.oldcaresystem.entity.FeeRecord;
import com.example.oldcaresystem.entity.Service;
import com.example.oldcaresystem.entity.Caregiver;
import com.example.oldcaresystem.service.AppointmentService;
import com.example.oldcaresystem.service.ElderlyInfoService;
import com.example.oldcaresystem.service.FeeRecordService;
import com.example.oldcaresystem.service.ServiceManageService;
import com.example.oldcaresystem.service.CaregiverService;
import com.example.oldcaresystem.util.ResponseUtil;
import com.example.oldcaresystem.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 服务预约控制器
 */
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ElderlyInfoService elderlyInfoService;

    @Autowired
    private ServiceManageService serviceManageService;
    
    @Autowired
    private FeeRecordService feeRecordService;

    @Autowired
    private com.example.oldcaresystem.service.EvaluationService evaluationService;
    
    @Autowired
    private CaregiverService caregiverService;
    
    @Autowired
    private com.example.oldcaresystem.service.UserService userService;

    /**
     * 新增预约
     */
    @PostMapping
    public ResponseUtil<Appointment> add(@RequestBody Appointment appointment) {
        try {
            // 验证居民用户、服务是否存在
            if (appointment.getResidentId() == null) {
                return ResponseUtil.error(400, "居民ID不能为空");
            }
            
            com.example.oldcaresystem.entity.User resident = userService.getById(appointment.getResidentId());
            if (resident == null || !"resident".equals(resident.getRole())) {
                return ResponseUtil.error(400, "居民用户不存在");
            }

            Service service = serviceManageService.getById(appointment.getServiceId());
            if (service == null) {
                return ResponseUtil.error(400, "服务项目不存在");
            }

            // 验证护工是否存在（必须指定护工）
            if (appointment.getCaregiverId() == null) {
                return ResponseUtil.error(400, "必须指定护工");
            }
            
            // 兼容两种输入：
            // 1) 传入caregivers.id（通过caregiverService按主键查询得到userId）
            // 2) 直接传入users.id（若按caregivers主键查询为空，则按用户ID校验角色）
            Long inputCaregiverId = appointment.getCaregiverId();
            Long providerUserId = null;

            Caregiver caregiverByPk = caregiverService.getById(inputCaregiverId);
            if (caregiverByPk != null && caregiverByPk.getUserId() != null) {
                providerUserId = caregiverByPk.getUserId();
            } else {
                com.example.oldcaresystem.entity.User caregiverUser = userService.getById(inputCaregiverId);
                if (caregiverUser == null || caregiverUser.getRole() == null || !"caregiver".equalsIgnoreCase(caregiverUser.getRole())) {
                    return ResponseUtil.error(400, "护工信息不存在或角色不匹配，ID: " + inputCaregiverId);
                }
                providerUserId = caregiverUser.getId();
            }

            // 保存provider_id为护工的用户ID（users.id），以满足外键约束
            appointment.setCaregiverId(providerUserId);

            // 设置预约信息
            appointment.setTotalAmount(service.getPrice());
            appointment.setStatus("待确认");
            appointment.setCreatedTime(LocalDateTime.now());
            appointment.setUpdatedTime(LocalDateTime.now());

            appointmentService.save(appointment);
            
            return ResponseUtil.success("预约成功", appointment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 修改预约信息
     */
    @PutMapping
    public ResponseUtil<Appointment> update(@RequestBody Appointment appointment) {
        appointment.setUpdatedTime(LocalDateTime.now());
        appointmentService.updateById(appointment);
        return ResponseUtil.success("修改成功", appointment);
    }

    /**
     * 取消预约
     */
    @PutMapping("/{id}/cancel")
    public ResponseUtil<String> cancel(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseUtil.error(400, "预约不存在");
        }
        appointment.setStatus("已取消");
        appointment.setUpdatedTime(LocalDateTime.now());
        appointmentService.updateById(appointment);
        return ResponseUtil.success("预约已取消");
    }

    /**
     * 确认预约
     */
    @PutMapping("/{id}/confirm")
    public ResponseUtil<String> confirm(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseUtil.error(400, "预约不存在");
        }
        appointment.setStatus("已确认");
        appointment.setUpdatedTime(LocalDateTime.now());
        appointmentService.updateById(appointment);
        return ResponseUtil.success("预约已确认");
    }

    /**
     * 完成预约（自动生成费用记录）
     * 只有已确认的预约才能完成
     * 如果已存在费用记录，则不重复生成
     */
    @PutMapping("/{id}/complete")
    public ResponseUtil<String> complete(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseUtil.error(400, "预约不存在");
        }
        
        // 只有已确认的预约才能完成
        if (!"已确认".equals(appointment.getStatus())) {
            return ResponseUtil.error(400, "只有已确认的预约才能完成");
        }
        
        // 检查是否已经完成
        if ("已完成".equals(appointment.getStatus())) {
            return ResponseUtil.error(400, "该预约已完成");
        }
        
        // 检查是否已存在该预约的费用记录
        QueryWrapper<FeeRecord> feeWrapper = new QueryWrapper<>();
        feeWrapper.eq("appointment_id", id);
        FeeRecord existingFeeRecord = feeRecordService.getOne(feeWrapper);
        
        // 更新预约状态为已完成
        appointment.setStatus("已完成");
        appointment.setUpdatedTime(LocalDateTime.now());
        appointmentService.updateById(appointment);
        
        // 如果不存在费用记录，则自动生成
        if (existingFeeRecord == null) {
            Service service = serviceManageService.getById(appointment.getServiceId());
            if (service != null && appointment.getTotalAmount() != null) {
                FeeRecord feeRecord = new FeeRecord();
                feeRecord.setAppointmentId(id);
                
                // resident_id现在直接指向users.id，无需转换
                feeRecord.setResidentId(appointment.getResidentId());
                feeRecord.setServiceName(service.getServiceName());
                feeRecord.setAmount(appointment.getTotalAmount());
                feeRecord.setStatus("未支付");
                feeRecord.setCreatedTime(LocalDateTime.now());
                feeRecord.setUpdatedTime(LocalDateTime.now());
                feeRecordService.save(feeRecord);
                return ResponseUtil.success("预约已完成，费用记录已生成");
            } else {
                return ResponseUtil.success("预约已完成，但服务信息或费用信息不完整，未生成费用记录");
            }
        } else {
            return ResponseUtil.success("预约已完成，费用记录已存在");
        }
    }

    /**
     * 查询单个预约
     */
    @GetMapping("/{id}")
    public ResponseUtil<Appointment> getById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        return ResponseUtil.success(appointment);
    }

    /**
     * 分页查询预约列表
     * 管理员：查看所有预约
     * 护工：只看分配给自己的预约
     * 居民：只看自己的预约
     */
    @GetMapping
    public ResponseUtil<Page<Appointment>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) String status) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        
        if ("caregiver".equalsIgnoreCase(role)) {
            // 护工只能看到分配给自己的预约
            if (userId == null) {
                return ResponseUtil.error(401, "用户未登录");
            }
            // 通过user_id查找对应的护工记录
            QueryWrapper<Caregiver> caregiverWrapper = new QueryWrapper<>();
            caregiverWrapper.eq("user_id", userId);
            Caregiver caregiver = caregiverService.getOne(caregiverWrapper);
            if (caregiver != null && caregiver.getUserId() != null) {
                // provider_id存储的是users.id，这里使用护工的user_id进行匹配
                queryWrapper.eq("provider_id", caregiver.getUserId());
            } else {
                // 如果找不到护工记录，说明该用户账号没有关联的护工信息
                // 这可能是因为护工账号创建时没有正确设置user_id
                // 返回空列表
                queryWrapper.eq("provider_id", -1L); // 不存在的ID，确保返回空结果
            }
        } else if ("resident".equalsIgnoreCase(role)) {
            // 居民只能查看自己的预约
            // resident_id现在直接存储users.id，直接匹配即可
            if (userId == null) {
                return ResponseUtil.error(401, "用户未登录");
            }
            queryWrapper.eq("resident_id", userId);
        }
        // 管理员不做过滤，可查看所有预约
        
        if (residentId != null) {
            queryWrapper.eq("resident_id", residentId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_time");
        Page<Appointment> page = new Page<>(current, size);
        Page<Appointment> result = appointmentService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询居民的所有预约（通过用户ID）
     */
    @GetMapping("/resident/{residentId}")
    public ResponseUtil<Page<Appointment>> getAppointmentsByResidentId(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resident_id", residentId);
        queryWrapper.orderByDesc("created_time");
        Page<Appointment> page = new Page<>(current, size);
        Page<Appointment> result = appointmentService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询护工的所有预约
     */
    @GetMapping("/caregiver/{caregiverId}")
    public ResponseUtil<Page<Appointment>> getAppointmentsByCaregiverId(
            @PathVariable Long caregiverId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        // 路径参数传入的是caregivers.id，需要转换为对应的users.id进行查询
        Caregiver caregiver = caregiverService.getById(caregiverId);
        if (caregiver != null && caregiver.getUserId() != null) {
            queryWrapper.eq("provider_id", caregiver.getUserId());
        } else {
            // 不存在的ID以确保空结果
            queryWrapper.eq("provider_id", -1L);
        }
        queryWrapper.orderByDesc("created_time");
        Page<Appointment> page = new Page<>(current, size);
        Page<Appointment> result = appointmentService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 删除预约（管理员权限）
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseUtil<String> delete(@PathVariable Long id) {
        String role = com.example.oldcaresystem.security.UserContext.getRole();
        if (role == null || !"admin".equals(role)) {
            return ResponseUtil.error(403, "无权限删除预约");
        }

        // 先删除可能存在的费用记录，避免外键约束阻止删除
        QueryWrapper<FeeRecord> feeWrapper = new QueryWrapper<>();
        feeWrapper.eq("appointment_id", id);
        feeRecordService.remove(feeWrapper);

        // 删除关联的评价记录
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.oldcaresystem.entity.Evaluation> evalWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        evalWrapper.eq("appointment_id", id);
        evaluationService.remove(evalWrapper);

        boolean ok = appointmentService.removeById(id);
        if (ok) {
            return ResponseUtil.success("删除成功");
        }
        return ResponseUtil.error(404, "预约不存在或删除失败");
    }
}
