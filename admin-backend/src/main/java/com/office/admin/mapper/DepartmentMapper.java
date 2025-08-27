package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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
    @Select("SELECT * FROM departments WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order, create_time")
    List<Department> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有顶级部门
     */
    @Select("SELECT * FROM departments WHERE parent_id = 0 AND status = 1 ORDER BY sort_order, create_time")
    List<Department> findTopLevelDepartments();

    /**
     * 根据部门名称查询部门
     */
    @Select("SELECT * FROM departments WHERE name = #{name} AND status = 1")
    Department findByName(@Param("name") String name);

    /**
     * 检查部门名称是否存在（同级部门）
     */
    @Select("SELECT COUNT(*) FROM departments WHERE name = #{name} AND parent_id = #{parentId} AND id != #{excludeId}")
    int countByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId, @Param("excludeId") Long excludeId);

    /**
     * 查询部门树结构（带员工数量）
     */
    @Select("SELECT d.*, " +
            "(SELECT COUNT(*) FROM employees e WHERE e.department_id = d.id AND e.status = 1) as employee_count " +
            "FROM departments d WHERE d.status = 1 ORDER BY d.level, d.sort_order")
    List<Department> findDepartmentTreeWithEmployeeCount();

    /**
     * 查询所有子部门ID（递归）
     */
    @Select("WITH RECURSIVE dept_tree AS (" +
            "SELECT id FROM departments WHERE id = #{deptId} " +
            "UNION ALL " +
            "SELECT d.id FROM departments d " +
            "INNER JOIN dept_tree dt ON d.parent_id = dt.id " +
            "WHERE d.status = 1) " +
            "SELECT id FROM dept_tree")
    List<Long> findAllChildDepartmentIds(@Param("deptId") Long deptId);

    /**
     * 检查部门是否有子部门
     */
    @Select("SELECT COUNT(*) FROM departments WHERE parent_id = #{deptId} AND status = 1")
    int countChildDepartments(@Param("deptId") Long deptId);

    /**
     * 检查部门是否有员工
     */
    @Select("SELECT COUNT(*) FROM employees WHERE department_id = #{deptId} AND status = 1")
    int countDepartmentEmployees(@Param("deptId") Long deptId);

    /**
     * 获取部门的最大排序值
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM departments WHERE parent_id = #{parentId}")
    int getMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 获取各部门员工统计
     */
    @Select("SELECT d.name as department_name, COUNT(e.id) as employee_count " +
            "FROM departments d " +
            "LEFT JOIN employees e ON d.id = e.department_id AND e.status = 1 " +
            "WHERE d.status = 1 " +
            "GROUP BY d.id, d.name " +
            "ORDER BY d.level, d.sort_order")
    List<Map<String, Object>> getDepartmentEmployeeStats();
}