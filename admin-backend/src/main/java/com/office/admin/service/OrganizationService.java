package com.office.admin.service;

import com.office.admin.entity.Department;
import com.office.admin.entity.Employee;

import java.util.List;
import java.util.Map;

/**
 * 组织架构服务接口
 *
 * @author office-system
 * @since 2024-01-01
 */
public interface OrganizationService {

    /**
     * 获取完整的组织架构树
     */
    List<Department> getOrganizationTree();

    /**
     * 获取部门层级结构（包含员工信息）
     */
    Department getDepartmentHierarchy(Long deptId);

    /**
     * 获取组织架构统计信息
     */
    Map<String, Object> getOrganizationStats();

    /**
     * 搜索部门和员工
     */
    List<Object> searchOrganization(String keyword);

    /**
     * 获取部门路径（面包屑导航用）
     */
    List<Department> getDepartmentPath(Long deptId);

    /**
     * 调整部门结构（移动部门）
     */
    void moveDepartment(Long deptId, Long newParentId, Integer sortOrder);
    
    /**
     * 创建部门
     */
    Department createDepartment(Department department);
    
    /**
     * 更新部门
     */
    Department updateDepartment(Long id, Department department);
    
    /**
     * 删除部门
     */
    void deleteDepartment(Long id);
    
    /**
     * 获取部门详情
     */
    Department getDepartmentById(Long id);
    
    /**
     * 启用/禁用部门
     */
    void toggleDepartmentStatus(Long id);
    
    /**
     * 获取部门员工列表
     */
    List<Employee> getDepartmentEmployees(Long deptId);
}