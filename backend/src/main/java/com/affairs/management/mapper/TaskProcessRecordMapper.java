package com.affairs.management.mapper;

import com.affairs.management.entity.TaskProcessRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskProcessRecordMapper {

    int insert(TaskProcessRecord record);

    List<TaskProcessRecord> selectByTaskId(@Param("taskId") String taskId);

    /** 查最近N条流程记录（用于最近动态） */
    List<TaskProcessRecord> selectRecent(@Param("limit") int limit,
                                          @Param("userIds") List<String> userIds);

    String selectMaxId();
}
