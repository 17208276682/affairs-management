package com.affairs.management.mapper;

import com.affairs.management.entity.TaskProcessRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskProcessRecordMapper {

    int insert(TaskProcessRecord record);

    List<TaskProcessRecord> selectByTaskId(@Param("taskId") String taskId);

    List<TaskProcessRecord> selectByTaskIds(@Param("taskIds") List<String> taskIds);

    /** 查最近N条流程记录（用于最近动态） */
    List<TaskProcessRecord> selectRecent(@Param("limit") int limit,
                                          @Param("userIds") List<String> userIds);

    /** 按任务关系查询最近动态（根据角色过滤） */
    List<TaskProcessRecord> selectRecentByRelation(@Param("limit") int limit,
                                                    @Param("userId") String userId,
                                                    @Param("checkAssigner") boolean checkAssigner,
                                                    @Param("checkExecutor") boolean checkExecutor);

    String selectMaxId();
}
