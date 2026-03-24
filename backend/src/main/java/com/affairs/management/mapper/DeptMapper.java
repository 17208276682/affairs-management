package com.affairs.management.mapper;

import com.affairs.management.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeptMapper {

    Department selectById(@Param("id") String id);

    List<Department> selectAll();

    List<Department> selectByParentId(@Param("parentId") String parentId);

    List<Department> selectChildren(@Param("parentId") String parentId);

    int insert(Department dept);

    int update(Department dept);

    int deleteById(@Param("id") String id);

    /** 检查同级下是否存在重名部门 */
    Department selectByNameAndParentId(@Param("name") String name,
                                       @Param("parentId") String parentId,
                                       @Param("excludeId") String excludeId);

    /** 获取最大部门ID的数字部分 */
    String selectMaxId();

    /** 查询同级最大的排序值 */
    Integer selectMaxSortByParentId(@Param("parentId") String parentId);

    /** 更新成员数量 */
    int updateMemberCount(@Param("id") String id, @Param("count") int count);

    /** 递归获取所有子部门ID */
    List<String> selectAllChildIds(@Param("parentId") String parentId);
}
