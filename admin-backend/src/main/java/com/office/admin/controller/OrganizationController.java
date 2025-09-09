package com.office.admin.controller;

import com.office.admin.common.Result;
import com.office.admin.entity.Department;
import com.office.admin.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组织架构控制器
 *
 * @author office-system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/admin/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * 获取完整的组织架构树
     */
    @GetMapping("/tree")
    public Result<List<Department>> getOrganizationTree() {
        try {
            List<Department> tree = organizationService.getOrganizationTree();
            return Result.success(tree);
        } catch (Exception e) {
            return Result.error("获取组织架构失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门层级结构（包含员工信息）
     */
    @GetMapping("/departments/{deptId}/hierarchy")
    public Result<Department> getDepartmentHierarchy(@PathVariable Long deptId) {
        try {
            Department department = organizationService.getDepartmentHierarchy(deptId);
            if (department != null) {
                return Result.success(department);
            } else {
                return Result.error("部门不存在");
            }
        } catch (Exception e) {
            return Result.error("获取部门信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取组织架构统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOrganizationStats() {
        try {
            Map<String, Object> stats = organizationService.getOrganizationStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 搜索部门和员工
     */
    @GetMapping("/search")
    public Result<List<Object>> searchOrganization(@RequestParam String keyword) {
        try {
            List<Object> results = organizationService.searchOrganization(keyword);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门路径（面包屑导航用）
     */
    @GetMapping("/departments/{deptId}/path")
    public Result<List<Department>> getDepartmentPath(@PathVariable Long deptId) {
        try {
            List<Department> path = organizationService.getDepartmentPath(deptId);
            return Result.success(path);
        } catch (Exception e) {
            return Result.error("获取部门路径失败: " + e.getMessage());
        }
    }

    /**
     * 调整部门结构（移动部门）
     */
    @PutMapping("/departments/{deptId}/move")
    public Result<String> moveDepartment(@PathVariable Long deptId,
                                       @RequestBody Map<String, Object> params) {
        try {
            Long newParentId = ((Number) params.get("newParentId")).longValue();
            Integer sortOrder = (Integer) params.get("sortOrder");
            organizationService.moveDepartment(deptId, newParentId, sortOrder);
            return Result.success("部门移动成功");
        } catch (Exception e) {
            return Result.error("移动部门失败: " + e.getMessage());
        }
    }
}