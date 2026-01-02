package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.Service;
import com.example.oldcaresystem.entity.Appointment;
import com.example.oldcaresystem.service.AppointmentService;
import com.example.oldcaresystem.service.ServiceManageService;
import com.example.oldcaresystem.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务项目控制器
 */
@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @Autowired
    private ServiceManageService serviceManageService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 新增服务项目
     */
    @PostMapping
    public ResponseUtil<Service> add(@RequestBody Service service) {
        service.setCreatedTime(LocalDateTime.now());
        service.setUpdatedTime(LocalDateTime.now());
        serviceManageService.save(service);
        return ResponseUtil.success("新增成功", service);
    }

    /**
     * 修改服务项目
     */
    @PutMapping
    public ResponseUtil<Service> update(@RequestBody Service service) {
        service.setUpdatedTime(LocalDateTime.now());
        serviceManageService.updateById(service);
        return ResponseUtil.success("修改成功", service);
    }

    /**
     * 删除服务项目
     */
    @DeleteMapping("/{id}")
    public ResponseUtil<String> delete(@PathVariable Long id) {
        // 如果存在关联预约，避免外键报错，改为禁用并提示
        long refCount = appointmentService.lambdaQuery()
                .eq(Appointment::getServiceId, id)
                .count();
        if (refCount > 0) {
            Service toDisable = new Service();
            toDisable.setId(id);
            toDisable.setStatus(0); // 禁用
            toDisable.setUpdatedTime(LocalDateTime.now());
            serviceManageService.updateById(toDisable);
            return ResponseUtil.error("该服务已有预约，已自动禁用，无法删除。");
        }

        serviceManageService.removeById(id);
        return ResponseUtil.success("删除成功");
    }

    /**
     * 查询单个服务项目
     */
    @GetMapping("/{id}")
    public ResponseUtil<Service> getById(@PathVariable Long id) {
        Service service = serviceManageService.getById(id);
        return ResponseUtil.success(service);
    }

    /**
     * 分页查询服务列表
     */
    @GetMapping
    public ResponseUtil<Page<Service>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String serviceName) {
        QueryWrapper<Service> queryWrapper = new QueryWrapper<>();
        if (serviceName != null && !serviceName.isEmpty()) {
            queryWrapper.like("service_name", serviceName);
        }
        queryWrapper.eq("status", 1);
        Page<Service> page = new Page<>(current, size);
        Page<Service> result = serviceManageService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询所有可用服务（不分页）
     */
    @GetMapping("/all")
    public ResponseUtil<List<Service>> getAll() {
        QueryWrapper<Service> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<Service> list = serviceManageService.list(queryWrapper);
        return ResponseUtil.success(list);
    }
}
