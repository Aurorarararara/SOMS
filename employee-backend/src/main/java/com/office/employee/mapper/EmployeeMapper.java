package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     * 根据部门ID查询部门经理
     */
    @Select("SELECT * FROM employees WHERE department_id = #{departmentId} AND position LIKE '%经理%' AND status = 1 LIMIT 1")
    Employee selectDepartmentManager(@Param("departmentId") Long departmentId);
}