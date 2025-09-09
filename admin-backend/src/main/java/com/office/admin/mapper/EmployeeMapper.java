package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 员工信息Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 根据用户ID查询员工信息
     */
    @Select("SELECT * FROM employees WHERE user_id = #{userId} AND status = 1")
    Employee findByUserId(@Param("userId") Long userId);

    /**
     * 根据员工编号查询员工信息
     */
    @Select("SELECT * FROM employees WHERE employee_no = #{employeeNo} AND status = 1")
    Employee findByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 检查员工编号是否存在
     */
    @Select("SELECT COUNT(*) FROM employees WHERE employee_no = #{employeeNo}")
    int countByEmployeeNo(@Param("employeeNo") String employeeNo);

    /**
     * 根据部门ID查询员工数量
     */
    @Select("SELECT COUNT(*) FROM employees WHERE department_id = #{departmentId} AND status = 1")
    int countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 分页查询员工列表（带用户信息和部门信息）
     */
    @Select("SELECT e.*, u.username, u.real_name, u.email, u.phone, u.avatar, u.status as user_status, " +
            "d.name as department_name FROM employees e " +
            "LEFT JOIN users u ON e.user_id = u.id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "WHERE e.status = 1 " +
            "ORDER BY e.create_time DESC")
    IPage<Employee> selectEmployeePageWithDetails(Page<Employee> page);

    /**
     * 根据部门ID查询员工列表
     */
    @Select("SELECT e.*, u.username, u.real_name, u.email, u.phone " +
            "FROM employees e " +
            "LEFT JOIN users u ON e.user_id = u.id " +
            "WHERE e.department_id = #{departmentId} AND e.status = 1")
    List<Employee> findByDepartmentIdWithUserInfo(@Param("departmentId") Long departmentId);

    /**
     * 查询所有在职员工统计
     */
    @Select("SELECT COUNT(*) FROM employees WHERE status = 1")
    int countActiveEmployees();

    /**
     * 根据关键词搜索员工
     */
    @Select("SELECT e.*, u.username, u.real_name, u.email, u.phone, d.name as department_name " +
            "FROM employees e " +
            "LEFT JOIN users u ON e.user_id = u.id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "WHERE e.status = 1 AND (" +
            "u.real_name LIKE CONCAT('%', #{keyword}, '%') OR " +
            "u.username LIKE CONCAT('%', #{keyword}, '%') OR " +
            "e.employee_no LIKE CONCAT('%', #{keyword}, '%') OR " +
            "e.position LIKE CONCAT('%', #{keyword}, '%'))")
    List<Employee> searchEmployees(@Param("keyword") String keyword);
    
    /**
     * 获取部门人员分布数据
     */
    @Select("SELECT d.name as department_name, COUNT(e.id) as employee_count " +
            "FROM departments d " +
            "LEFT JOIN employees e ON d.id = e.department_id AND e.status = 1 " +
            "GROUP BY d.id, d.name " +
            "ORDER BY employee_count DESC")
    List<Map<String, Object>> getDepartmentDistribution();
}