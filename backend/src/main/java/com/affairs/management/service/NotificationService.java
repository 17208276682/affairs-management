package com.affairs.management.service;

import com.affairs.management.entity.Notification;

import java.util.List;

public interface NotificationService {
    void createNotification(String userId, String title, String content,
                            String type, String relatedTaskId);
    List<Notification> getNotifications(String userId);
    void markAsRead(String id);
    void markAllAsRead(String userId);
    int getUnreadCount(String userId);
}
