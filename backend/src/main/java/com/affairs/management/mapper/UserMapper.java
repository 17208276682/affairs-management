package com.affairs.management.mapper;

import com.affairs.management.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") String id);

    User selectByUsername(@Param("username") String username);

    User selectByPhone(@Param("phone") String phone);

    List<User> selectByDeptId(@Param("deptId") String deptId);

    List<User> selectByDeptIds(@Param("deptIds") List<String> deptIds);

    List<User> selectMemberList(@Param("deptId") String deptId,
                                @Param("keyword") String keyword,
                                @Param("position") String position,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    long countMembers(@Param("deptId") String deptId,
                      @Param("keyword") String keyword,
                      @Param("position") String position);

    int insert(User user);

    int update(User user);

    int deleteById(@Param("id") String id);

    /** 查询同一部门是否已有 manager */
    User selectManagerByDeptId(@Param("deptId") String deptId, @Param("excludeId") String excludeId);

    /** 获取最大用户ID的数字部分 */
    String selectMaxId();

    /** 根据IDs批量查询 */
    List<User> selectByIds(@Param("ids") List<String> ids);

    /** 查是否有关联任务 */
    int countTasksByUserId(@Param("userId") String userId);
}
