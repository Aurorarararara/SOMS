package com.office.employee.service;

import com.office.employee.dto.LoginRequest;
import com.office.employee.dto.LoginResponse;
import com.office.employee.entity.User;
import com.office.employee.entity.Employee;
import com.office.employee.entity.Department;
import com.office.employee.entity.Role;
import com.office.employee.mapper.UserMapper;
import com.office.employee.mapper.EmployeeMapper;
import com.office.employee.mapper.DepartmentMapper;
import com.office.employee.mapper.RoleMapper;
import com.office.employee.util.JwtUtil;
import com.office.employee.common.BusinessException;
import com.office.employee.controller.UserController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务类
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
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
        
        System.out.println("用户名: " + loginRequest.getUsername());
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

        // 查询员工信息
        Employee employee = employeeMapper.findByUserId(user.getId());
        if (employee == null) {
            throw new BusinessException("员工信息不存在");
        }

        // 查询部门信息
        Department department = null;
        if (employee.getDepartmentId() != null) {
            department = departmentMapper.selectById(employee.getDepartmentId());
        }

        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(user, employee);
        if (department != null) {
            userInfo.setDepartmentName(department.getName());
        }
        userInfo.setRoles(roleNames);

        return new LoginResponse(token, userInfo);
    }

    /**
     * 获取用户信息
     */
    public LoginResponse.UserInfo getUserInfo(Long userId) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 查询员工信息
        Employee employee = employeeMapper.findByUserId(userId);
        if (employee == null) {
            throw new BusinessException("员工信息不存在");
        }

        // 查询部门信息
        Department department = null;
        if (employee.getDepartmentId() != null) {
            department = departmentMapper.selectById(employee.getDepartmentId());
        }

        // 查询用户角色
        List<Role> roles = roleMapper.findByUserId(userId);
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(user, employee);
        if (department != null) {
            userInfo.setDepartmentName(department.getName());
        }
        userInfo.setRoles(roleNames);

        return userInfo;
    }

    /**
     * 验证用户权限
     */
    public boolean hasRole(Long userId, String roleCode) {
        List<Role> roles = roleMapper.findByUserId(userId);
        return roles.stream()
                .anyMatch(role -> role.getCode().equals(roleCode));
    }

    /**
     * 检查用户状态
     */
    public boolean isUserActive(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getStatus() == 1;
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(Long userId, LoginResponse.UserInfo userInfo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户基本信息
        user.setRealName(userInfo.getRealName());
        user.setEmail(userInfo.getEmail());
        user.setPhone(userInfo.getPhone());
        userMapper.updateById(user);

        // 更新员工信息
        Employee employee = employeeMapper.findByUserId(userId);
        if (employee != null) {
            employee.setPosition(userInfo.getPosition());
            employeeMapper.updateById(employee);
        }
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!oldPassword.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(newPassword);
        userMapper.updateById(user);
    }

    /**
     * 获取工作统计
     */
    public UserController.WorkStatsResponse getWorkStats(Long userId) {
        // 这里可以根据实际需求查询各种统计数据
        // 暂时返回模拟数据
        return new UserController.WorkStatsResponse(25, 1, 2, 8);
    }
}