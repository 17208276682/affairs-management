package com.affairs.management.mapper;

import com.affairs.management.entity.TaskAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskAttachmentMapper {

    int insert(TaskAttachment ta);

    int batchInsert(@Param("taskId") String taskId, @Param("attachmentIds") List<String> attachmentIds);

    List<String> selectAttachmentIdsByTaskId(@Param("taskId") String taskId);
}
