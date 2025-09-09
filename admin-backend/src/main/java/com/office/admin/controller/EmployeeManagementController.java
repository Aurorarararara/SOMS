package com.office.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.office.admin.entity.Employee;
import com.office.admin.service.EmployeeManagementService;
import com.office.admin.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 员工管理控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/admin/employees")
public class EmployeeManagementController {

    @Autowired
    private EmployeeManagementService employeeManagementService;

    /**
     * 分页查询员工列表
     */
    @GetMapping
    public Result<IPage<Employee>> getEmployeePage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Employee> page = employeeManagementService.getEmployeePage(current, size);
        return Result.success(page);
    }

    /**
     * 搜索员工
     */
    @GetMapping("/search")
    public Result<List<Employee>> searchEmployees(@RequestParam String keyword) {
        List<Employee> employees = employeeManagementService.searchEmployees(keyword);
        return Result.success(employees);
    }

    /**
     * 根据部门查询员工
     */
    @GetMapping("/department/{departmentId}")
    public Result<List<Employee>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<Employee> employees = employeeManagementService.getEmployeesByDepartment(departmentId);
        return Result.success(employees);
    }

    /**
     * 创建员工
     */
    @PostMapping
    public Result<Employee> createEmployee(@RequestBody @Valid EmployeeManagementService.EmployeeCreateRequest request) {
        Employee employee = employeeManagementService.createEmployee(request);
        return Result.success("员工创建成功", employee);
    }

    /**
     * 更新员工信息
     */
    @PutMapping("/{id}")
    public Result<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeManagementService.EmployeeUpdateRequest request) {
        Employee employee = employeeManagementService.updateEmployee(id, request);
        return Result.success("员工信息更新成功", employee);
    }

    /**
     * 禁用/启用员工
     */
    @PutMapping("/{id}/toggle-status")
    public Result<String> toggleEmployeeStatus(@PathVariable Long id) {
        employeeManagementService.toggleEmployeeStatus(id);
        return Result.success("员工状态更新成功");
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteEmployee(@PathVariable Long id) {
        employeeManagementService.deleteEmployee(id);
        return Result.success("员工删除成功");
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    public Result<Employee> getEmployeeDetail(@PathVariable Long id) {
        Employee employee = employeeManagementService.getEmployeeDetail(id);
        return Result.success(employee);
    }

    /**
     * 统计在职员工数量
     */
    @GetMapping("/count/active")
    public Result<Integer> getActiveEmployeeCount() {
        int count = employeeManagementService.getActiveEmployeeCount();
        return Result.success(count);
    }
}