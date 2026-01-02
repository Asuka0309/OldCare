package com.example.oldcaresystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.oldcaresystem.entity.ElderlyInfo;
import com.example.oldcaresystem.service.ElderlyInfoService;
import com.example.oldcaresystem.service.UserService;
import com.example.oldcaresystem.entity.User;
import com.example.oldcaresystem.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 老人信息控制器
 */
@RestController
@RequestMapping("/api/elderly")
public class ElderlyInfoController {

    @Autowired
    private ElderlyInfoService elderlyInfoService;

    @Autowired
    private UserService userService;

    /**
     * 新增老人信息
     */
    @PostMapping
    @Transactional
    public ResponseUtil<ElderlyInfo> add(@RequestBody ElderlyInfo elderlyInfo) {
        elderlyInfo.setCreatedTime(LocalDateTime.now());
        elderlyInfo.setUpdatedTime(LocalDateTime.now());
        // 自动创建对应居民用户，并关联 user_id
        if (elderlyInfo.getUserId() == null) {
            String desiredUsername = elderlyInfo.getPhone();
            User user = userService.provisionUser(desiredUsername, elderlyInfo.getName(), elderlyInfo.getPhone(), "resident");
            elderlyInfo.setUserId(user.getId());
        }
        elderlyInfoService.save(elderlyInfo);
        return ResponseUtil.success("新增成功", elderlyInfo);
    }

    /**
     * 修改老人信息
     */
    @PutMapping
    @Transactional
    public ResponseUtil<ElderlyInfo> update(@RequestBody ElderlyInfo elderlyInfo) {
        ElderlyInfo existing = elderlyInfoService.getById(elderlyInfo.getId());
        elderlyInfo.setUpdatedTime(LocalDateTime.now());
        elderlyInfoService.updateById(elderlyInfo);

        // 同步更新对应用户的姓名与电话
        if (existing != null && existing.getUserId() != null) {
            User user = userService.getById(existing.getUserId());
            if (user != null) {
                user.setRealName(elderlyInfo.getName());
                user.setPhone(elderlyInfo.getPhone());
                user.setUpdatedTime(LocalDateTime.now());
                userService.updateById(user);
            }
        }
        return ResponseUtil.success("修改成功", elderlyInfo);
    }

    /**
     * 删除老人信息
     */
    @DeleteMapping("/{id}")
    public ResponseUtil<String> delete(@PathVariable Long id) {
        elderlyInfoService.removeById(id);
        return ResponseUtil.success("删除成功");
    }

    /**
     * 查询单个老人信息
     */
    @GetMapping("/{id}")
    public ResponseUtil<ElderlyInfo> getById(@PathVariable Long id) {
        ElderlyInfo elderlyInfo = elderlyInfoService.getById(id);
        return ResponseUtil.success(elderlyInfo);
    }

    /**
     * 分页查询老人列表
     */
    @GetMapping
    public ResponseUtil<Page<ElderlyInfo>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        QueryWrapper<ElderlyInfo> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        Page<ElderlyInfo> page = new Page<>(current, size);
        Page<ElderlyInfo> result = elderlyInfoService.page(page, queryWrapper);
        return ResponseUtil.success(result);
    }

    /**
     * 查询所有老人信息（不分页）
     */
    @GetMapping("/all")
    public ResponseUtil<List<ElderlyInfo>> getAll() {
        List<ElderlyInfo> list = elderlyInfoService.list();
        return ResponseUtil.success(list);
    }
}
