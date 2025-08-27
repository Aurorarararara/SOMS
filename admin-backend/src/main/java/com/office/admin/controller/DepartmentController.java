package com.office.admin.controller;

import com.office.admin.common.Result;
import com.office.admin.entity.Department;
import com.office.admin.entity.Employee;
import com.office.admin.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 部门管理控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/admin/departments")
public class DepartmentController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * 获取所有部门列表
     */
    @GetMapping
    public Result<List<Department>> getDepartmentList() {
        try {
            List<Department> departments = organizationService.getOrganizationTree();
            // 展平树结构以获取所有部门
            List<Department> flatDepartments = flattenDepartmentTree(departments);
            return Result.success(flatDepartments);
        } catch (Exception e) {
            return Result.error("获取部门列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    public Result<List<Department>> getDepartmentTree() {
        try {
            List<Department> tree = organizationService.getOrganizationTree();
            return Result.success(tree);
        } catch (Exception e) {
            return Result.error("获取部门树失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    public Result<Department> getDepartmentDetail(@PathVariable Long id) {
        try {
            Department department = organizationService.getDepartmentById(id);
            if (department != null) {
                return Result.success(department);
            } else {
                return Result.error("部门不存在");
            }
        } catch (Exception e) {
            return Result.error("获取部门详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建部门
     */
    @PostMapping
    public Result<Department> createDepartment(@RequestBody Department department) {
        try {
            Department created = organizationService.createDepartment(department);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建部门失败: " + e.getMessage());
        }
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public Result<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        try {
            Department updated = organizationService.updateDepartment(id, department);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新部门失败: " + e.getMessage());
        }
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDepartment(@PathVariable Long id) {
        try {
            organizationService.deleteDepartment(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除部门失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用部门
     */
    @PutMapping("/{id}/toggle-status")
    public Result<String> toggleDepartmentStatus(@PathVariable Long id) {
        try {
            organizationService.toggleDepartmentStatus(id);
            return Result.success("状态切换成功");
        } catch (Exception e) {
            return Result.error("切换部门状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门员工列表
     */
    @GetMapping("/{id}/employees")
    public Result<List<Employee>> getDepartmentEmployees(@PathVariable Long id) {
        try {
            List<Employee> employees = organizationService.getDepartmentEmployees(id);
            return Result.success(employees);
        } catch (Exception e) {
            return Result.error("获取部门员工列表失败: " + e.getMessage());
        }
    }

    /**
     * 展平部门树结构
     */
    private List<Department> flattenDepartmentTree(List<Department> departments) {
        List<Department> result = new java.util.ArrayList<>();
        for (Department dept : departments) {
            result.add(dept);
            if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
                result.addAll(flattenDepartmentTree(dept.getChildren()));
            }
        }
        return result;
    }
}