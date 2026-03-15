package com.affairs.management.mapper;

import com.affairs.management.entity.UserManagedDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserManagedDeptMapper {

    List<String> selectDeptIdsByUserId(@Param("userId") String userId);

    List<String> selectUserIdsByDeptId(@Param("deptId") String deptId);

    int insert(UserManagedDepartment umd);

    int deleteByUserId(@Param("userId") String userId);

    int deleteByDeptId(@Param("deptId") String deptId);

    int batchInsert(@Param("userId") String userId, @Param("deptIds") List<String> deptIds);
}
