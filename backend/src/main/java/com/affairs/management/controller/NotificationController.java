package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.response.NotificationVO;
import com.affairs.management.entity.Notification;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/list")
    public ApiResponse<List<NotificationVO>> getNotifications() {
        List<Notification> list = notificationService.getNotifications(SecurityUtils.getCurrentUserId());
        return ApiResponse.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead(SecurityUtils.getCurrentUserId());
        return ApiResponse.success();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount() {
        return ApiResponse.success(
                notificationService.getUnreadCount(SecurityUtils.getCurrentUserId()));
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setType(n.getType());
        vo.setRelatedTaskId(n.getRelatedTaskId());
        vo.setRead(n.getIsRead() != null && n.getIsRead() == 1);
        if (n.getCreatedAt() != null) {
            vo.setCreatedAt(n.getCreatedAt().format(DTF));
        }
        return vo;
    }
}
