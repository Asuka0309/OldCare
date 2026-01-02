package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.oldcaresystem.entity.FeeRecord;
import com.example.oldcaresystem.entity.Appointment;
import com.example.oldcaresystem.entity.Caregiver;
import com.example.oldcaresystem.service.FeeRecordService;
import com.example.oldcaresystem.service.AppointmentService;
import com.example.oldcaresystem.service.CaregiverService;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收入统计控制器
 */
@RestController
@RequestMapping("/api/income")
public class IncomeController {

    @Autowired
    private FeeRecordService feeRecordService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private CaregiverService caregiverService;

    /**
     * 获取收入统计
     * 管理员：查看总收入
     * 护工：查看自己的收入
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @RequestParam(required = false) String period) { // period: today, week, month, year
        
        String role = UserContext.getRole();
        Long userId = UserContext.getUserId();
        
        if (!"admin".equalsIgnoreCase(role) && !"caregiver".equalsIgnoreCase(role)) {
            return ApiResponse.error(403, "无权限查看收入统计");
        }

        // 默认查询本月
        if (period == null || period.isEmpty()) {
            period = "month";
        }

        LocalDate startDate = getStartDate(period);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        QueryWrapper<FeeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "已支付");
        queryWrapper.between("payment_time", startDateTime, endDateTime);

        // 护工只能查看自己的收入
        if ("caregiver".equalsIgnoreCase(role)) {
            // 查找护工对应的caregiver记录
            QueryWrapper<Caregiver> caregiverWrapper = new QueryWrapper<>();
            caregiverWrapper.eq("user_id", userId);
            Caregiver caregiver = caregiverService.getOne(caregiverWrapper);
            
            if (caregiver == null || caregiver.getUserId() == null) {
                return ApiResponse.success(createEmptyStatistics(period));
            }
            
            // 查找该护工的所有预约；provider_id 新语义=users.id，兼容旧数据=caregivers.id
            QueryWrapper<Appointment> appointmentWrapper = new QueryWrapper<>();
            appointmentWrapper.eq("status", "已完成");
            appointmentWrapper.and(q -> q.eq("provider_id", caregiver.getUserId())
                                       .or()
                                       .eq("provider_id", caregiver.getId()));
            List<Appointment> appointments = appointmentService.list(appointmentWrapper);
            
            if (appointments.isEmpty()) {
                return ApiResponse.success(createEmptyStatistics(period));
            }
            
            // 获取这些预约对应的费用记录
            List<Long> appointmentIds = new ArrayList<>();
            for (Appointment apt : appointments) {
                appointmentIds.add(apt.getId());
            }
            queryWrapper.in("appointment_id", appointmentIds);
        }

        List<FeeRecord> records = feeRecordService.list(queryWrapper);

        // 计算统计数据
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = records.size();
        
        for (FeeRecord record : records) {
            if (record.getAmount() != null) {
                totalAmount = totalAmount.add(record.getAmount());
            }
        }

        // 按日期分组统计（用于图表）
        Map<String, BigDecimal> dailyStats = new HashMap<>();
        for (FeeRecord record : records) {
            if (record.getPaymentTime() != null) {
                String dateKey = record.getPaymentTime().toLocalDate().toString();
                BigDecimal amount = record.getAmount() != null ? record.getAmount() : BigDecimal.ZERO;
                dailyStats.put(dateKey, dailyStats.getOrDefault(dateKey, BigDecimal.ZERO).add(amount));
            }
        }

        // 构建图表数据
        List<Map<String, Object>> chartData = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(LocalDate.now())) {
            String dateKey = current.toString();
            BigDecimal amount = dailyStats.getOrDefault(dateKey, BigDecimal.ZERO);
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", dateKey);
            dataPoint.put("amount", amount);
            chartData.add(dataPoint);
            current = current.plusDays(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("totalCount", totalCount);
        result.put("period", period);
        result.put("startDate", startDate.toString());
        result.put("endDate", LocalDate.now().toString());
        result.put("chartData", chartData);
        result.put("role", role);

        return ApiResponse.success(result);
    }

    /**
     * 根据周期获取开始日期
     */
    private LocalDate getStartDate(String period) {
        LocalDate now = LocalDate.now();
        switch (period.toLowerCase()) {
            case "today":
                return now;
            case "week":
                return now.minusWeeks(1);
            case "month":
                return YearMonth.from(now).atDay(1);
            case "year":
                return now.withDayOfYear(1);
            default:
                return YearMonth.from(now).atDay(1);
        }
    }

    /**
     * 创建空统计数据
     */
    private Map<String, Object> createEmptyStatistics(String period) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", BigDecimal.ZERO);
        result.put("totalCount", 0);
        result.put("period", period);
        result.put("startDate", getStartDate(period).toString());
        result.put("endDate", LocalDate.now().toString());
        result.put("chartData", new ArrayList<>());
        return result;
    }
}

