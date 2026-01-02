package com.example.oldcaresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import com.example.oldcaresystem.entity.Notification;
import com.example.oldcaresystem.mapper.NotificationMapper;
import com.example.oldcaresystem.service.NotificationService;
import com.example.oldcaresystem.entity.User;
import com.example.oldcaresystem.mapper.UserMapper;
import com.example.oldcaresystem.entity.ElderlyInfo;
import com.example.oldcaresystem.mapper.ElderlyInfoMapper;
import com.example.oldcaresystem.entity.EmergencyHelp;
import com.example.oldcaresystem.service.EmergencyHelpService;
import com.example.oldcaresystem.security.UserContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    private final UserMapper userMapper;
    private final ElderlyInfoMapper elderlyInfoMapper;
    private final EmergencyHelpService emergencyHelpService;
    
    public NotificationServiceImpl(UserMapper userMapper, ElderlyInfoMapper elderlyInfoMapper, EmergencyHelpService emergencyHelpService) {
        this.userMapper = userMapper;
        this.elderlyInfoMapper = elderlyInfoMapper;
        this.emergencyHelpService = emergencyHelpService;
    }

    @Override
    public void notifyAllCaregivers(Long residentId, String title, String content, String notificationType, Long relatedId) {
        // 获取居民姓名（优先 realName，其次 username），用于在通知中展示
        // 1) 优先取用户的真实姓名/用户名
        User resident = userMapper.selectById(residentId);
        String residentName = null;
        if (resident != null) {
            residentName = resident.getRealName() != null && !resident.getRealName().isEmpty()
                    ? resident.getRealName()
                    : resident.getUsername();
        }
        // 2) 若为纯数字用户名或为空，再尝试 elderly_info 的姓名（id 与用户ID对齐的设计）
        if ((residentName == null || residentName.matches("^\\d+$"))) {
            ElderlyInfo elderly = elderlyInfoMapper.selectById(residentId);
            if (elderly != null && elderly.getName() != null && !elderly.getName().isEmpty()) {
                residentName = elderly.getName();
            }
        }
        String finalContent = content;
        if (residentName != null && !residentName.isEmpty()) {
            // 如果内容中包含以ID占位的“居民【<id>】”，替换为姓名
            String idPlaceholder = "居民【" + residentId + "】";
            if (finalContent != null && finalContent.contains(idPlaceholder)) {
                finalContent = finalContent.replace(idPlaceholder, "居民【" + residentName + "】");
            } else {
                // 否则在前面补充姓名标识
                finalContent = "居民【" + residentName + "】" + (finalContent == null ? "" : ("：" + finalContent));
            }
        }

        // 若是紧急求助，附加位置信息，方便护工在通知列表直接看到求助地点
        if ("emergency_help".equals(notificationType) && relatedId != null) {
            EmergencyHelp help = emergencyHelpService.getById(relatedId);
            if (help != null) {
                String loc = help.getLocation();
                if (loc != null && !loc.isEmpty()) {
                    finalContent = (finalContent == null || finalContent.isEmpty())
                            ? ("位置：" + loc)
                            : (finalContent + "，位置：" + loc);
                }
            }
        }
        // 查询所有护工
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("role", "caregiver");
        List<User> caregivers = userMapper.selectList(userWrapper);
        
        // 为每个护工创建通知
        for (User caregiver : caregivers) {
            Notification notification = new Notification();
            notification.setReceiverId(caregiver.getId());
            notification.setSenderId(residentId);
            notification.setNotificationType(notificationType);
            notification.setTitle(title);
            notification.setContent(finalContent);
            notification.setRelatedId(relatedId);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            this.save(notification);
        }
    }

    @Override
    public List<Notification> getAllNotifications(Long userId) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId);
        wrapper.orderByDesc("created_at");
        List<Notification> list = this.list(wrapper);
        return filterInvalidEmergency(list);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId);
        wrapper.eq("is_read", false);
        wrapper.orderByDesc("created_at");
        List<Notification> list = this.list(wrapper);
        return filterInvalidEmergency(list);
    }

    /**
     * 过滤掉已被删除或不存在的紧急求助关联通知，避免前端看到已删记录
     */
    private List<Notification> filterInvalidEmergency(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) return notifications;

        // 找出所有紧急求助关联ID
        List<Long> relatedIds = notifications.stream()
                .filter(n -> "emergency_help".equals(n.getNotificationType()) && n.getRelatedId() != null)
                .map(Notification::getRelatedId)
                .distinct()
                .collect(Collectors.toList());

        if (relatedIds.isEmpty()) return notifications;

        // 批量查询存在的求助，并准备位置映射
        List<EmergencyHelp> exists = emergencyHelpService.listByIds(relatedIds);
        List<Long> existingIds = exists.stream().map(EmergencyHelp::getId).collect(Collectors.toList());

        // id -> location
        var locationMap = exists.stream()
            .filter(e -> e.getLocation() != null && !e.getLocation().isEmpty())
            .collect(Collectors.toMap(EmergencyHelp::getId, EmergencyHelp::getLocation, (a, b) -> a));

        return notifications.stream()
                .filter(n -> {
                    if (!"emergency_help".equals(n.getNotificationType())) return true;
                    Long rid = n.getRelatedId();
                    return rid == null || existingIds.contains(rid);
                })
                .peek(n -> {
                    if (!"emergency_help".equals(n.getNotificationType())) return;
                    Long rid = n.getRelatedId();
                    if (rid == null) return;
                    String loc = locationMap.get(rid);
                    if (loc == null || loc.isEmpty()) return;
                    String content = n.getContent();
                    if (content == null || content.isEmpty()) {
                        n.setContent("位置：" + loc);
                    } else if (!content.contains(loc) && !content.contains("位置：")) {
                        n.setContent(content + "，位置：" + loc);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification != null && !notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            this.updateById(notification);
            
            // 如果是紧急求助通知，且当前用户是护工或管理员，自动更新紧急求助状态为"已响应"
            if ("emergency_help".equals(notification.getNotificationType()) && notification.getRelatedId() != null) {
                String role = UserContext.getRole();
                // 只有护工或管理员可以自动响应
                if ("caregiver".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role)) {
                    EmergencyHelp emergencyHelp = emergencyHelpService.getById(notification.getRelatedId());
                    if (emergencyHelp != null && "pending".equalsIgnoreCase(emergencyHelp.getStatus())) {
                        Long responderId = UserContext.getUserId();
                        emergencyHelp.setStatus("responding");
                        emergencyHelp.setResponderId(responderId);
                        emergencyHelp.setResponseTime(LocalDateTime.now());
                        emergencyHelp.setUpdatedAt(LocalDateTime.now());
                        emergencyHelpService.updateById(emergencyHelp);
                    }
                }
            }
        }
    }

    @Override
    public void deleteByReceiver(Long userId) {
        if (userId == null) return;
        this.remove(new QueryWrapper<Notification>().eq("receiver_id", userId));
    }
}
