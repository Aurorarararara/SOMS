package com.office.admin.service;

import com.office.admin.dto.LoginRequest;
import com.office.admin.dto.LoginResponse;
import com.office.admin.entity.User;
import com.office.admin.entity.Employee;
import com.office.admin.entity.Department;
import com.office.admin.entity.Role;
import com.office.admin.mapper.UserMapper;
import com.office.admin.mapper.EmployeeMapper;
import com.office.admin.mapper.DepartmentMapper;
import com.office.admin.mapper.RoleMapper;
import com.office.admin.util.JwtUtil;
import com.office.admin.common.BusinessException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 管理端认证服务类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录
     */
    public LoginResponse login(LoginRequest loginRequest) {
        // 查询用户
        User user = userMapper.findByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        // 验证密码（使用明文对比）
        String inputPassword = loginRequest.getPassword();
        String storedPassword = user.getPassword();
        
        System.out.println("管理员用户名: " + loginRequest.getUsername());
        System.out.println("输入密码: " + inputPassword);
        System.out.println("数据库密码: " + storedPassword);
        
        // 直接比较明文密码
        boolean passwordMatches = inputPassword.equals(storedPassword);
        System.out.println("密码验证结果: " + passwordMatches);
        
        if (!passwordMatches) {
            throw new BusinessException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 验证管理员权限
        if (userMapper.checkAdminPermission(user.getId()) == 0) {
            throw new BusinessException("您没有管理员权限");
        }

        // 查询员工信息
        Employee employee = employeeMapper.findByUserId(user.getId());

        // 查询部门信息
        Department department = null;
        if (employee != null && employee.getDepartmentId() != null) {
            department = departmentMapper.selectById(employee.getDepartmentId());
        }

        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        // 构建管理员用户信息
        LoginResponse.AdminUserInfo userInfo = new LoginResponse.AdminUserInfo(user, employee);
        if (department != null) {
            userInfo.setDepartmentName(department.getName());
        }
        userInfo.setRoles(roleNames);
        
        // 设置权限列表（基于角色）
        userInfo.setPermissions(generatePermissions(roleCodes));
        
        // 检查是否为超级管理员
        userInfo.setIsSuperAdmin(roleCodes.contains("SUPER_ADMIN"));

        return new LoginResponse(token, userInfo);
    }

    /**
     * 获取管理员用户信息
     */
    public LoginResponse.AdminUserInfo getUserInfo(Long userId) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证管理员权限
        if (userMapper.checkAdminPermission(userId) == 0) {
            throw new BusinessException("您没有管理员权限");
        }

        // 查询员工信息
        Employee employee = employeeMapper.findByUserId(userId);

        // 查询部门信息
        Department department = null;
        if (employee != null && employee.getDepartmentId() != null) {
            department = departmentMapper.selectById(employee.getDepartmentId());
        }

        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(userId);
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        // 构建管理员用户信息
        LoginResponse.AdminUserInfo userInfo = new LoginResponse.AdminUserInfo(user, employee);
        if (department != null) {
            userInfo.setDepartmentName(department.getName());
        }
        userInfo.setRoles(roleNames);
        userInfo.setPermissions(generatePermissions(roleCodes));
        userInfo.setIsSuperAdmin(roleCodes.contains("SUPER_ADMIN"));

        return userInfo;
    }

    /**
     * 验证管理员权限
     */
    public boolean hasAdminPermission(Long userId) {
        return userMapper.checkAdminPermission(userId) > 0;
    }

    /**
     * 验证用户角色
     */
    public boolean hasRole(Long userId, String roleCode) {
        return roleMapper.checkUserHasRole(userId, roleCode) > 0;
    }

    /**
     * 检查用户状态
     */
    public boolean isUserActive(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getStatus() == 1;
    }

    /**
     * 根据角色生成权限列表
     */
    private List<String> generatePermissions(List<String> roleCodes) {
        if (roleCodes.contains("SUPER_ADMIN")) {
            return Arrays.asList(
                "user:manage", "role:manage", "department:manage", 
                "employee:manage", "attendance:manage", "leave:manage",
                "announcement:manage", "report:view", "system:config"
            );
        } else if (roleCodes.contains("ADMIN")) {
            return Arrays.asList(
                "employee:manage", "attendance:manage", "leave:manage",
                "announcement:manage", "report:view"
            );
        } else if (roleCodes.contains("MANAGER")) {
            return Arrays.asList(
                "attendance:view", "leave:approve", "report:view"
            );
        }
        return Arrays.asList("dashboard:view");
    }
}