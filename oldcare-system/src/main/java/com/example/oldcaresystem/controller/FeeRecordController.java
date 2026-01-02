package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.FeeRecord;
import com.example.oldcaresystem.entity.Appointment;
import com.example.oldcaresystem.entity.ElderlyInfo;
import com.example.oldcaresystem.entity.User;
import com.example.oldcaresystem.service.FeeRecordService;
import com.example.oldcaresystem.service.AppointmentService;
import com.example.oldcaresystem.service.ElderlyInfoService;
import com.example.oldcaresystem.service.UserService;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 费用记录控制器
 */
@RestController
@RequestMapping("/api/fee-record")
public class FeeRecordController {

    @Autowired
    private FeeRecordService feeRecordService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private ElderlyInfoService elderlyInfoService;
    
    @Autowired
    private UserService userService;

    /**
     * 新增费用记录（仅支持通过预约自动生成）
     * 手动创建时需要包含 appointmentId
     * 仅管理员可以手动创建费用记录，护工和居民不能创建
     */
    @PostMapping
    public ApiResponse<?> add(@RequestBody FeeRecord feeRecord) {
        // 检查权限：只有管理员可以手动创建费用记录
        String role = UserContext.getRole();
        if (!"admin".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "无权限操作，仅管理员可以创建费用记录");
        }
        
        // 必须关联预约
        if (feeRecord.getAppointmentId() == null) {
            return ApiResponse.error(400, "费用必须关联预约ID");
        }
        
        // 验证居民ID
        if (feeRecord.getResidentId() == null) {
            return ApiResponse.error(400, "居民ID不能为空");
        }
        if (feeRecord.getAmount() == null || feeRecord.getAmount().signum() <= 0) {
            return ApiResponse.error(400, "金额必须大于0");
        }
        
        feeRecord.setStatus("未支付");
        feeRecord.setCreatedTime(LocalDateTime.now());
        feeRecord.setUpdatedTime(LocalDateTime.now());
        feeRecordService.save(feeRecord);
        return ApiResponse.success("费用记录创建成功", feeRecord);
    }

    /**
     * 支付费用（审批确认，仅管理员）
     * 只有未支付的费用记录才能支付
     * 如果费用记录关联了预约，验证预约是否已完成
     */
    @PutMapping("/{id}/pay")
    public ApiResponse<?> pay(@PathVariable Long id) {
        // 检查权限：只有管理员可以审批确认支付
        String role = UserContext.getRole();
        if (!"admin".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "无权限操作，仅管理员可以审批确认支付");
        }
        
        FeeRecord feeRecord = feeRecordService.getById(id);
        if (feeRecord == null) {
            return ApiResponse.error(404, "费用记录不存在");
        }
        
        // 检查费用记录状态
        if ("已支付".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "该费用已支付");
        }
        if ("已退款".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "已退款的费用不能再次支付");
        }
        if ("退款申请中".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "退款申请中的费用不能支付，请先处理退款申请");
        }
        
        // 如果费用记录关联了预约，验证预约是否已完成
        if (feeRecord.getAppointmentId() != null) {
            Appointment appointment = appointmentService.getById(feeRecord.getAppointmentId());
            if (appointment == null) {
                return ApiResponse.error(400, "关联的预约不存在");
            }
            // 只有已完成的预约才能支付费用
            if (!"已完成".equals(appointment.getStatus())) {
                return ApiResponse.error(400, "只有已完成的预约才能支付费用");
            }
        }
        
        feeRecord.setStatus("已支付");
        feeRecord.setPaymentTime(LocalDateTime.now());
        feeRecord.setUpdatedTime(LocalDateTime.now());
        feeRecordService.updateById(feeRecord);
        return ApiResponse.success("支付成功");
    }

    /**
     * 居民自助支付（仅居民，且只能支付自己的费用）
     * 校验规则：状态必须为"未支付"；如有关联预约，需为已完成或已取消；
     * 所有权校验支持 resident_id 双口径（users.id 或 elderly_info.id）以及兼容字段 elderlyId。
     */
    @PutMapping("/{id}/pay-self")
    public ApiResponse<?> paySelf(@PathVariable Long id) {
        String role = UserContext.getRole();
        if (!"resident".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "仅居民可进行自助支付");
        }

        Long userId = UserContext.getUserId();
        FeeRecord feeRecord = feeRecordService.getById(id);
        if (feeRecord == null) {
            return ApiResponse.error(404, "费用记录不存在");
        }

        // 所有权校验：resident_id 现在统一为 users.id
        if (!userId.equals(feeRecord.getResidentId())) {
            return ApiResponse.error(403, "只能支付本人的费用记录");
        }

        // 状态校验
        if (!"未支付".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "仅未支付的费用可自助支付");
        }
        if ("已退款".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "已退款的费用不可支付");
        }
        if ("退款申请中".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "退款申请中不可支付");
        }

        // 预约校验：允许已完成或已取消的预约进行支付（取消的情况可以视规则允许/禁止，这里允许）
        if (feeRecord.getAppointmentId() != null) {
            Appointment appointment = appointmentService.getById(feeRecord.getAppointmentId());
            if (appointment == null) {
                return ApiResponse.error(400, "关联的预约不存在");
            }
            if (!"已完成".equals(appointment.getStatus()) && !"已取消".equals(appointment.getStatus())) {
                return ApiResponse.error(400, "仅已完成或已取消的预约可支付费用");
            }
        }

        // 标记支付
        feeRecord.setStatus("已支付");
        feeRecord.setPaymentTime(LocalDateTime.now());
        feeRecord.setUpdatedTime(LocalDateTime.now());
        feeRecordService.updateById(feeRecord);
        return ApiResponse.success("支付成功");
    }

    /**
     * 申请退款（仅居民，只能申请退款自己的费用）
     * 护工不能进行退款操作
     * 只有已支付的费用记录才能申请退款
     * 申请后状态变为"退款申请中"，需要管理员审批
     */
    @PutMapping("/{id}/refund")
    public ApiResponse<?> refund(@PathVariable Long id) {
        FeeRecord feeRecord = feeRecordService.getById(id);
        if (feeRecord == null) {
            return ApiResponse.error(404, "费用记录不存在");
        }
        
        // 检查权限：只有居民可以申请退款自己的费用记录
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        
        // 管理员和护工不能申请退款，只有居民可以申请
        if ("admin".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "管理员不能申请退款，退款申请仅限居民");
        }
        if ("caregiver".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "护工不能申请退款，退款申请仅限居民");
        }
        
        // 所有权校验：resident_id 现在统一为 users.id
        if (!userId.equals(feeRecord.getResidentId())) {
            return ApiResponse.error(403, "只能申请退款自己的费用记录");
        }
        
        // 检查费用记录状态
        if (!"已支付".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "只能申请退款已支付的费用");
        }
        if ("已退款".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "该费用已退款");
        }
        if ("退款申请中".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "该费用已申请退款，等待管理员审批");
        }
        
        // 如果费用记录关联了预约，验证预约状态
        if (feeRecord.getAppointmentId() != null) {
            Appointment appointment = appointmentService.getById(feeRecord.getAppointmentId());
            if (appointment != null && "已取消".equals(appointment.getStatus())) {
                // 如果预约已取消，允许申请退款
            } else if (appointment != null && !"已完成".equals(appointment.getStatus())) {
                return ApiResponse.error(400, "只有已完成或已取消的预约才能申请退款");
            }
        }
        
        // 申请退款，状态改为"退款申请中"
        feeRecord.setStatus("退款申请中");
        feeRecord.setUpdatedTime(LocalDateTime.now());
        feeRecordService.updateById(feeRecord);
        return ApiResponse.success("退款申请已提交，等待管理员审批");
    }
    
    /**
     * 审批退款（仅管理员）
     * 同意退款：状态从"退款申请中" -> "已退款"
     * 拒绝退款：状态从"退款申请中" -> "已支付"
     */
    @PutMapping("/{id}/approve-refund")
    public ApiResponse<?> approveRefund(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        // 检查权限：只有管理员可以审批退款
        String role = UserContext.getRole();
        if (!"admin".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "无权限操作，仅管理员可以审批退款");
        }
        
        FeeRecord feeRecord = feeRecordService.getById(id);
        if (feeRecord == null) {
            return ApiResponse.error(404, "费用记录不存在");
        }
        
        // 检查费用记录状态
        if (!"退款申请中".equals(feeRecord.getStatus())) {
            return ApiResponse.error(400, "该费用记录不是退款申请中状态，无法审批");
        }
        
        // 获取审批结果
        Boolean approved = null;
        if (request != null && request.containsKey("approved")) {
            Object approvedObj = request.get("approved");
            if (approvedObj instanceof Boolean) {
                approved = (Boolean) approvedObj;
            } else if (approvedObj instanceof String) {
                approved = Boolean.parseBoolean((String) approvedObj);
            }
        }
        
        if (approved == null) {
            return ApiResponse.error(400, "审批结果参数不能为空");
        }
        
        if (approved) {
            // 同意退款
            feeRecord.setStatus("已退款");
            feeRecord.setRefundTime(LocalDateTime.now());
            feeRecord.setUpdatedTime(LocalDateTime.now());
            feeRecordService.updateById(feeRecord);
            return ApiResponse.success("退款审批通过，退款成功");
        } else {
            // 拒绝退款
            feeRecord.setStatus("已支付");
            feeRecord.setUpdatedTime(LocalDateTime.now());
            feeRecordService.updateById(feeRecord);
            return ApiResponse.success("退款申请已拒绝，费用记录恢复为已支付状态");
        }
    }

    /**
     * 查询居民的所有费用记录（需要放在 /{id} 前面避免路由冲突）
     * 权限控制：管理员可查所有，居民仅能查自己的，护工只读
     */
    @GetMapping("/by-resident/{residentId}")
    public ApiResponse<Map<String, Object>> getFeeRecordsByResidentId(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // 权限校验
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        
        // 居民不能查他人的费用记录（管理员和护工可以）
        if ("resident".equalsIgnoreCase(role)) {
            // 检查是否是本人的费用记录
            boolean isOwnRecord = false;
            
            // 情况1：residentId直接等于userId
            if (residentId.equals(userId)) {
                isOwnRecord = true;
            }
            
            // 情况2：residentId是elderly_info的ID，需要检查user_id
            if (!isOwnRecord) {
                ElderlyInfo elderlyInfo = elderlyInfoService.getById(residentId);
                if (elderlyInfo != null && elderlyInfo.getUserId() != null && elderlyInfo.getUserId().equals(userId)) {
                    isOwnRecord = true;
                }
            }
            
            if (!isOwnRecord) {
                return ApiResponse.error(403, "无权限查看他人的费用记录");
            }
        }
        
        QueryWrapper<FeeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resident_id", residentId);
        queryWrapper.orderByDesc("created_time");
        Page<FeeRecord> page = new Page<>(current, size);
        IPage<FeeRecord> result = feeRecordService.page(page, queryWrapper);
        
        // 为每条费用记录设置elderlyId字段，方便前端显示
        setElderlyIdForRecords(result.getRecords());
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return ApiResponse.success(data);
    }

    /**
     * 分页查询费用记录
     * 管理员：查看所有费用记录
     * 护工：查看所有费用记录（只读）
     * 居民：查看自己的费用记录
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) String status) {
        QueryWrapper<FeeRecord> queryWrapper = new QueryWrapper<>();
        
        // 根据角色过滤数据
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        
        if ("resident".equalsIgnoreCase(role)) {
            // 居民只能查看自己的费用记录
            // 费用记录的resident_id可能存储的是：
            // 1. users表的ID（新数据，通过elderly_info.user_id关联）
            // 2. elderly_info表的ID（旧数据，直接存储elderly_info.id）
            // 需要同时匹配两种情况
            
            // 先查找所有关联到该用户的elderly_info记录
            QueryWrapper<ElderlyInfo> elderlyWrapper = new QueryWrapper<>();
            elderlyWrapper.eq("user_id", userId);
            java.util.List<ElderlyInfo> elderlyInfoList = elderlyInfoService.list(elderlyWrapper);
            
            // 构建所有可能的resident_id列表
            java.util.List<Long> allPossibleIds = new java.util.ArrayList<>();
            allPossibleIds.add(userId); // 添加userId本身（新数据格式）
            
            // 添加所有关联的elderly_info的ID（旧数据格式）
            if (elderlyInfoList != null && !elderlyInfoList.isEmpty()) {
                for (ElderlyInfo info : elderlyInfoList) {
                    if (info.getId() != null) {
                        allPossibleIds.add(info.getId());
                    }
                }
            }
            
            // 如果allPossibleIds只有一个元素（只有userId），直接使用eq
            // 如果有多个，使用in查询
            if (allPossibleIds.size() == 1) {
                queryWrapper.eq("resident_id", allPossibleIds.get(0));
            } else if (allPossibleIds.size() > 1) {
                queryWrapper.in("resident_id", allPossibleIds);
            } else {
                // 如果没有任何匹配的ID，返回空结果
                queryWrapper.eq("resident_id", -1L); // 不存在的ID，确保返回空结果
            }
        }
        // 管理员和护工可以查看所有费用记录，不需要额外过滤
        
        if (residentId != null) {
            // 如果传入 residentId，支持双口径查询：
            // 1. 直接匹配（该ID可能是users.id或elderly_info.id）
            // 2. 如果是elderly_info.id，同时查询其关联的user_id
            ElderlyInfo elderlyInfo = elderlyInfoService.getById(residentId);
            if (elderlyInfo != null && elderlyInfo.getUserId() != null) {
                // residentId是elderly_info的ID，需要同时匹配该elderly对应的user_id
                queryWrapper.and(q -> 
                    q.eq("resident_id", residentId)
                     .or()
                     .eq("resident_id", elderlyInfo.getUserId())
                );
            } else {
                // residentId可能直接是users.id，或是不存在的elderly_id，直接匹配
                queryWrapper.eq("resident_id", residentId);
            }
        }
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_time");
        
        Page<FeeRecord> page = new Page<>(current, size);
        IPage<FeeRecord> result = feeRecordService.page(page, queryWrapper);
        
        // 为每条费用记录设置elderlyId字段，方便前端显示
        setElderlyIdForRecords(result.getRecords());
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return ApiResponse.success(data);
    }

    /**
     * 为费用记录设置elderlyId和residentName字段，方便前端显示
     * 如果residentId是users表的ID，通过elderly_info查找对应的elderlyId和姓名
     * 如果residentId是elderly_info表的ID，直接使用并获取姓名
     */
    private void setElderlyIdForRecords(java.util.List<FeeRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (FeeRecord record : records) {
            if (record.getResidentId() != null) {
                String residentName = null;
                Long elderlyId = null;
                
                // 先尝试作为users表的ID查找
                User user = userService.getById(record.getResidentId());
                if (user != null) {
                    // residentId是users表的ID
                    residentName = user.getRealName();
                    // 通过elderly_info查找对应的elderlyId
                    QueryWrapper<ElderlyInfo> elderlyWrapper = new QueryWrapper<>();
                    elderlyWrapper.eq("user_id", record.getResidentId());
                    ElderlyInfo elderlyInfo = elderlyInfoService.getOne(elderlyWrapper);
                    if (elderlyInfo != null && elderlyInfo.getId() != null) {
                        elderlyId = elderlyInfo.getId();
                        // 如果user的realName为空，使用elderlyInfo的name
                        if (residentName == null || residentName.isEmpty()) {
                            residentName = elderlyInfo.getName();
                        }
                    }
                } else {
                    // 如果找不到user，说明residentId可能是elderly_info表的ID（旧数据格式）
                    ElderlyInfo elderlyInfo = elderlyInfoService.getById(record.getResidentId());
                    if (elderlyInfo != null) {
                        elderlyId = elderlyInfo.getId();
                        residentName = elderlyInfo.getName();
                        // 如果elderly_info有user_id，也尝试获取user的realName
                        if (elderlyInfo.getUserId() != null) {
                            User userByElderly = userService.getById(elderlyInfo.getUserId());
                            if (userByElderly != null && userByElderly.getRealName() != null && !userByElderly.getRealName().isEmpty()) {
                                residentName = userByElderly.getRealName();
                            }
                        }
                    }
                }
                
                // 设置字段
                record.setElderlyId(elderlyId != null ? elderlyId : record.getResidentId());
                record.setResidentName(residentName != null ? residentName : "-");
            }
        }
    }

    /**
     * 查询单个费用记录（需要放在最后避免与其他 GET 路由冲突）
     */
    @GetMapping("/{id}")
    public ApiResponse<FeeRecord> getById(@PathVariable Long id) {
        FeeRecord feeRecord = feeRecordService.getById(id);
        if (feeRecord == null) {
            return ApiResponse.error(404, "费用记录不存在");
        }
        // 设置elderlyId字段
        java.util.List<FeeRecord> singleRecord = java.util.Collections.singletonList(feeRecord);
        setElderlyIdForRecords(singleRecord);
        return ApiResponse.success(feeRecord);
    }
}

