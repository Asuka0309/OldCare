package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.util.ApiResponse;
import com.example.oldcaresystem.entity.Quotation;
import com.example.oldcaresystem.security.UserContext;
import com.example.oldcaresystem.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报价管理控制器
 */
@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    // ServiceNeedService已删除（发布需求功能已移除）

    /**
     * 获取我的报价列表（服务提供商）
     */
    @GetMapping("/my")
    public ApiResponse<Page<Quotation>> getMyQuotations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        Long userId = UserContext.getUserId();
        QueryWrapper<Quotation> wrapper = new QueryWrapper<>();
        wrapper.eq("provider_id", userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_at");
        
        Page<Quotation> pageData = quotationService.page(new Page<>(page, size), wrapper);
        return ApiResponse.success(pageData);
    }

    /**
     * 获取某个服务需求的所有报价（居民查看）
     * 已禁用：发布需求功能已移除
     */
    @GetMapping("/need/{needId}")
    public ApiResponse<List<Quotation>> getQuotationsByNeed(@PathVariable Long needId) {
        return ApiResponse.error("该功能已禁用：发布需求功能已移除");
    }

    /**
     * 获取报价详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Quotation> getById(@PathVariable Long id) {
        Quotation quotation = quotationService.getById(id);
        if (quotation == null) {
            return ApiResponse.error("报价不存在");
        }
        return ApiResponse.success(quotation);
    }

    /**
     * 提交报价（服务提供商）
     * 已禁用：发布需求功能已移除
     */
    @PostMapping
    public ApiResponse<Quotation> create(@RequestBody Quotation quotation) {
        return ApiResponse.error("该功能已禁用：发布需求功能已移除");
    }

    /**
     * 修改报价（服务提供商）
     */
    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Quotation quotation) {
        Quotation existing = quotationService.getById(id);
        if (existing == null) {
            return ApiResponse.error("报价不存在");
        }
        
        // 只允许修改自己的报价
        Long userId = UserContext.getUserId();
        if (!existing.getProviderId().equals(userId)) {
            return ApiResponse.error("无权限操作");
        }
        
        // 已接受或已拒绝的报价不能修改
        if (!"pending".equals(existing.getStatus())) {
            return ApiResponse.error("该报价已处理，不能修改");
        }
        
        quotation.setId(id);
        quotation.setUpdatedAt(LocalDateTime.now());
        boolean success = quotationService.updateById(quotation);
        
        if (success) {
            return ApiResponse.success("修改成功");
        }
        return ApiResponse.error("修改失败");
    }

    /**
     * 接受报价（居民）
     * 已禁用：发布需求功能已移除
     */
    @PostMapping("/{id}/accept")
    public ApiResponse<String> accept(@PathVariable Long id) {
        return ApiResponse.error("该功能已禁用：发布需求功能已移除");
    }

    /**
     * 拒绝报价（居民）
     * 已禁用：发布需求功能已移除
     */
    @PostMapping("/{id}/reject")
    public ApiResponse<String> reject(@PathVariable Long id) {
        return ApiResponse.error("该功能已禁用：发布需求功能已移除");
    }
}
