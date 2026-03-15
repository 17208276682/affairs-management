package com.affairs.management.mapper;

import com.affairs.management.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachmentMapper {

    int insert(Attachment attachment);

    Attachment selectById(@Param("id") String id);

    List<Attachment> selectByIds(@Param("ids") List<String> ids);

    /** 查询任务的附件 */
    List<Attachment> selectByTaskId(@Param("taskId") String taskId);

    /** 查询流程记录的附件 */
    List<Attachment> selectByRecordId(@Param("recordId") String recordId);

    String selectMaxId();
}
