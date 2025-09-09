package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据父部门ID查询子部门列表
     */
    @Select("SELECT * FROM departments WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order")
    List<Department> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有顶级部门
     */
    @Select("SELECT * FROM departments WHERE parent_id = 0 AND status = 1 ORDER BY sort_order")
    List<Department> findTopLevelDepartments();

    /**
     * 根据部门名称查询部门
     */
    @Select("SELECT * FROM departments WHERE name = #{name} AND status = 1")
    Department findByName(@Param("name") String name);

    /**
     * 检查部门名称是否存在（同级部门）
     */
    @Select("SELECT COUNT(*) FROM departments WHERE name = #{name} AND parent_id = #{parentId}")
    int countByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);
}