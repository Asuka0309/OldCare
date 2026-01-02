package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.Caregiver;
import com.example.oldcaresystem.service.CaregiverService;
import com.example.oldcaresystem.service.UserService;
import com.example.oldcaresystem.entity.User;
import com.example.oldcaresystem.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工信息控制器（原护工）
 */
@RestController
@RequestMapping("/api/caregiver")
public class CaregiverController {

    @Autowired
    private CaregiverService caregiverService;

    @Autowired
    private UserService userService;

    /**
     * 新增员工信息
     */
    @PostMapping
    @Transactional
    public ResponseUtil<Caregiver> add(@RequestBody Caregiver caregiver) {
        caregiver.setCreatedTime(LocalDateTime.now());
        caregiver.setUpdatedTime(LocalDateTime.now());
        // 自动创建对应员工用户（角色：caregiver）并关联 user_id
        if (caregiver.getUserId() == null) {
            String desiredUsername = caregiver.getPhone();
            User user = userService.provisionUser(desiredUsername, caregiver.getName(), caregiver.getPhone(), "caregiver");
            caregiver.setUserId(user.getId());
        }
        caregiverService.save(caregiver);
        return ResponseUtil.success("新增成功", caregiver);
    }

    /**
     * 修改员工信息
     */
    @PutMapping
    @Transactional
    public ResponseUtil<Caregiver> update(@RequestBody Caregiver caregiver) {
        Caregiver existing = caregiverService.getById(caregiver.getId());
        caregiver.setUpdatedTime(LocalDateTime.now());
        caregiverService.updateById(caregiver);

        // 同步更新员工对应用户的姓名与电话
        if (existing != null && existing.getUserId() != null) {
            User user = userService.getById(existing.getUserId());
            if (user != null) {
                String oldPhone = existing.getPhone();
                String newPhone = caregiver.getPhone();

                // 更新姓名与电话
                user.setRealName(caregiver.getName());
                user.setPhone(newPhone);

                // 若用户名原为旧手机号且新手机号可用，则同步用户名
                if (oldPhone != null && oldPhone.equals(user.getUsername()) && newPhone != null && !newPhone.isEmpty()) {
                    QueryWrapper<User> conflictQ = new QueryWrapper<>();
                    conflictQ.eq("username", newPhone);
                    User conflict = userService.getOne(conflictQ);
                    if (conflict == null || conflict.getId().equals(user.getId())) {
                        user.setUsername(newPhone);
                    }
                }

                user.setUpdatedTime(LocalDateTime.now());
                userService.updateById(user);
            }
        }
        return ResponseUtil.success("修改成功", caregiver);
    }

    /**
     * 删除员工信息
     */
    @DeleteMapping("/{id}")
    public ResponseUtil<String> delete(@PathVariable Long id) {
        caregiverService.removeById(id);
        return ResponseUtil.success("删除成功");
    }

    /**
     * 查询单个员工信息
     */
    @GetMapping("/{id}")
    public ResponseUtil<Caregiver> getById(@PathVariable Long id) {
        Caregiver caregiver = caregiverService.getById(id);
        return ResponseUtil.success(caregiver);
    }

    /**
     * 分页查询员工列表
     */
    @GetMapping
    public ResponseUtil<Page<Caregiver>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        QueryWrapper<Caregiver> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        // 临时移除 status 过滤以调试
        Page<Caregiver> page = new Page<>(current, size);
        Page<Caregiver> result = caregiverService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询所有在职员工（不分页）
     */
    @GetMapping("/all")
    public ResponseUtil<List<Caregiver>> getAll() {
        // 临时移除 status 过滤以调试
        List<Caregiver> list = caregiverService.list();
        return ResponseUtil.success(list);
    }

    /**
     * 更新员工评分
     */
    @PutMapping("/{id}/rating")
    public ResponseUtil<String> updateRating(@PathVariable Long id, @RequestParam Double rating) {
        Caregiver caregiver = caregiverService.getById(id);
        caregiver.setRating(rating);
        caregiver.setUpdatedTime(LocalDateTime.now());
        caregiverService.updateById(caregiver);
        return ResponseUtil.success("评分更新成功");
    }
}
