package com.affairs.management.mapper;

import com.affairs.management.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    List<Notification> selectByUserId(@Param("userId") String userId);

    int markAsRead(@Param("id") String id);

    int markAllAsRead(@Param("userId") String userId);

    int countUnread(@Param("userId") String userId);

    String selectMaxId();
}
