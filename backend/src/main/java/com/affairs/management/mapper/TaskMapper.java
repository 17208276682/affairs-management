package com.affairs.management.mapper;

import com.affairs.management.entity.Task;
import com.affairs.management.dto.response.DeptStats;
import com.affairs.management.dto.response.LevelStats;
import com.affairs.management.dto.response.PersonStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper {

    Task selectById(@Param("id") String id);

    int insert(Task task);

    int update(Task task);

    /** 动态事务列表查询 */
    List<Task> selectTaskList(@Param("type") String type,
                              @Param("currentUserId") String currentUserId,
                              @Param("role") String role,
                              @Param("managedUserIds") List<String> managedUserIds,
                              @Param("status") String status,
                              @Param("level") String level,
                              @Param("keyword") String keyword,
                              @Param("offset") int offset,
                              @Param("limit") int limit);

    long countTaskList(@Param("type") String type,
                       @Param("currentUserId") String currentUserId,
                       @Param("role") String role,
                       @Param("managedUserIds") List<String> managedUserIds,
                       @Param("status") String status,
                       @Param("level") String level,
                       @Param("keyword") String keyword);

    /** 根据父任务ID查子任务ID */
    List<String> selectChildTaskIds(@Param("parentTaskId") String parentTaskId);

    /** 获取当天最大事务ID */
    String selectMaxIdByDate(@Param("datePrefix") String datePrefix);

    /** 获取最大ID */
    String selectMaxId();

    /** 批量更新状态（级联审核/驳回） */
    int batchUpdateStatus(@Param("ids") List<String> ids,
                          @Param("status") String status);

    /** 统计概览 */
    Map<String, Object> selectStatsOverview(@Param("userIds") List<String> userIds);

    /** 按部门统计 */
    List<DeptStats> selectStatsByDept(@Param("userIds") List<String> userIds);

    /** 按级别统计 */
    List<LevelStats> selectStatsByLevel(@Param("userIds") List<String> userIds);

    /** 按个人统计 */
    List<PersonStats> selectStatsByPerson(@Param("userIds") List<String> userIds);

    /** 趋势统计 */
    List<Map<String, Object>> selectTrend(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("userIds") List<String> userIds);

    /** 递归查询所有子任务ID */
    List<String> selectAllDescendantIds(@Param("parentTaskId") String parentTaskId);
}
