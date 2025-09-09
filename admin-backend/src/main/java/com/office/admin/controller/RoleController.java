package com.office.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.common.Result;
import com.office.admin.entity.Role;
import com.office.admin.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleMapper roleMapper;
    
    /**
     * 获取所有有效角色列表
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<List<Role>> getAllRoles() {
        try {
            List<Role> roles = roleMapper.findAllActiveRoles();
            return Result.success(roles);
        } catch (Exception e) {
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<Page<Role>> getRoleList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer status) {
        
        Page<Role> page = new Page<>(current, size);
        QueryWrapper<Role> query = new QueryWrapper<>();
        
        if (name != null && !name.trim().isEmpty()) {
            query.like("name", name);
        }
        if (code != null && !code.trim().isEmpty()) {
            query.like("code", code);
        }
        if (status != null) {
            query.eq("status", status);
        }
        
        query.orderByDesc("create_time");
        
        Page<Role> result = roleMapper.selectPage(page, query);
        return Result.success(result);
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<Role> getRoleDetail(@PathVariable Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }
    
    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Role> createRole(@Valid @RequestBody RoleRequest request) {
        try {
            // 检查角色编码是否存在
            if (roleMapper.countByCode(request.getCode(), 0L) > 0) {
                return Result.error("角色编码已存在");
            }
            
            // 检查角色名称是否存在
            if (roleMapper.countByName(request.getName(), 0L) > 0) {
                return Result.error("角色名称已存在");
            }
            
            Role role = new Role();
            role.setName(request.getName());
            role.setCode(request.getCode());
            role.setDescription(request.getDescription());
            role.setStatus(1);
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            
            roleMapper.insert(role);
            return Result.success("角色创建成功", role);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Role> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleRequest request) {
        try {
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            // 检查角色编码是否存在（排除自己）
            if (roleMapper.countByCode(request.getCode(), roleId) > 0) {
                return Result.error("角色编码已存在");
            }
            
            // 检查角色名称是否存在（排除自己）
            if (roleMapper.countByName(request.getName(), roleId) > 0) {
                return Result.error("角色名称已存在");
            }
            
            role.setName(request.getName());
            role.setCode(request.getCode());
            role.setDescription(request.getDescription());
            role.setUpdateTime(LocalDateTime.now());
            
            roleMapper.updateById(role);
            return Result.success("角色更新成功", role);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<String> deleteRole(@PathVariable Long roleId) {
        try {
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            // 检查是否有用户使用该角色
            int userCount = roleMapper.countUsersByRoleId(roleId);
            if (userCount > 0) {
                return Result.error("该角色下还有用户，无法删除");
            }
            
            roleMapper.deleteById(roleId);
            return Result.success("角色删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 启用/禁用角色
     */
    @PutMapping("/{roleId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<String> toggleRoleStatus(@PathVariable Long roleId, @RequestParam Integer status) {
        try {
            Role role = roleMapper.selectById(roleId);
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            role.setStatus(status);
            role.setUpdateTime(LocalDateTime.now());
            roleMapper.updateById(role);
            
            String message = status == 1 ? "角色启用成功" : "角色禁用成功";
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取角色的用户数量
     */
    @GetMapping("/{roleId}/users/count")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<Integer> getRoleUserCount(@PathVariable Long roleId) {
        int userCount = roleMapper.countUsersByRoleId(roleId);
        return Result.success(userCount);
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<String> assignRoleToUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            @SuppressWarnings("unchecked")
            List<Long> roleIds = (List<Long>) request.get("roleIds");
            
            // 先删除用户现有的所有角色
            roleMapper.removeAllRolesFromUser(userId);
            
            // 分配新角色
            for (Long roleId : roleIds) {
                roleMapper.assignRoleToUser(userId, roleId);
            }
            
            return Result.success("角色分配成功");
        } catch (Exception e) {
            return Result.error("分配失败: " + e.getMessage());
        }
    }
    
    /**
     * 移除用户角色
     */
    @PostMapping("/remove")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<String> removeRoleFromUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long roleId = Long.valueOf(request.get("roleId").toString());
            
            roleMapper.removeRoleFromUser(userId, roleId);
            return Result.success("角色移除成功");
        } catch (Exception e) {
            return Result.error("移除失败: " + e.getMessage());
        }
    }
    
    /**
     * 角色请求DTO
     */
    public static class RoleRequest {
        private String name;
        private String code;
        private String description;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}