package com.affairs.management.service.impl;

import com.affairs.management.entity.Notification;
import com.affairs.management.mapper.NotificationMapper;
import com.affairs.management.service.NotificationService;
import com.affairs.management.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final IdGenerator idGenerator;

    @Override
    public void createNotification(String userId, String title, String content,
                                    String type, String relatedTaskId) {
        Notification notification = new Notification();
        notification.setId(idGenerator.nextNotificationId());
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedTaskId(relatedTaskId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    public List<Notification> getNotifications(String userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    public void markAsRead(String id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead(String userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public int getUnreadCount(String userId) {
        return notificationMapper.countUnread(userId);
    }
}
