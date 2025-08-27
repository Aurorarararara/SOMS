package com.office.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.admin.entity.Employee;
import com.office.admin.entity.User;
import com.office.admin.mapper.EmployeeMapper;
import com.office.admin.mapper.UserMapper;
import com.office.admin.mapper.RoleMapper;
import com.office.admin.common.BusinessException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 员工管理服务类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Service
public class EmployeeManagementService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private RoleMapper roleMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 分页查询员工列表
     */
    public IPage<Employee> getEmployeePage(int current, int size) {
        Page<Employee> page = new Page<>(current, size);
        return employeeMapper.selectEmployeePageWithDetails(page);
    }

    /**
     * 搜索员工
     */
    public List<Employee> searchEmployees(String keyword) {
        return employeeMapper.searchEmployees(keyword);
    }

    /**
     * 根据部门查询员工
     */
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeMapper.findByDepartmentIdWithUserInfo(departmentId);
    }

    /**
     * 获取部门人员分布数据
     */
    public List<Map<String, Object>> getDepartmentDistribution() {
        return employeeMapper.getDepartmentDistribution();
    }

    /**
     * 创建员工
     */
    @Transactional
    public Employee createEmployee(EmployeeCreateRequest request) {
        // 检查用户名是否存在
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查员工编号是否存在
        if (employeeMapper.countByEmployeeNo(request.getEmployeeNo()) > 0) {
            throw new BusinessException("员工编号已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);

        userMapper.insert(user);

        // 创建员工
        Employee employee = new Employee();
        employee.setUserId(user.getId());
        employee.setEmployeeNo(request.getEmployeeNo());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(1);

        employeeMapper.insert(employee);

        // 分配默认角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                roleMapper.assignRoleToUser(user.getId(), roleId);
            }
        } else {
            // 默认分配普通员工角色（假设角色ID为4）
            roleMapper.assignRoleToUser(user.getId(), 4L);
        }

        return employee;
    }

    /**
     * 更新员工信息
     */
    @Transactional
    public Employee updateEmployee(Long employeeId, EmployeeUpdateRequest request) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        User user = userMapper.selectById(employee.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查员工编号是否重复（排除当前员工）
        Employee existingEmployee = employeeMapper.findByEmployeeNo(request.getEmployeeNo());
        if (existingEmployee != null && !existingEmployee.getId().equals(employeeId)) {
            throw new BusinessException("员工编号已存在");
        }

        // 更新用户信息
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateById(user);

        // 更新员工信息
        employee.setEmployeeNo(request.getEmployeeNo());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        employeeMapper.updateById(employee);

        // 更新角色
        if (request.getRoleIds() != null) {
            roleMapper.removeAllRolesFromUser(user.getId());
            for (Long roleId : request.getRoleIds()) {
                roleMapper.assignRoleToUser(user.getId(), roleId);
            }
        }

        return employee;
    }

    /**
     * 禁用/启用员工
     */
    @Transactional
    public void toggleEmployeeStatus(Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        User user = userMapper.selectById(employee.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 切换状态
        int newStatus = user.getStatus() == 1 ? 0 : 1;
        user.setStatus(newStatus);
        employee.setStatus(newStatus);

        userMapper.updateById(user);
        employeeMapper.updateById(employee);
    }

    /**
     * 删除员工（逻辑删除）
     */
    @Transactional
    public void deleteEmployee(Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 禁用用户和员工
        User user = userMapper.selectById(employee.getUserId());
        user.setStatus(0);
        employee.setStatus(0);

        userMapper.updateById(user);
        employeeMapper.updateById(employee);

        // 移除所有角色
        roleMapper.removeAllRolesFromUser(user.getId());
    }

    /**
     * 获取员工详情
     */
    public Employee getEmployeeDetail(Long employeeId) {
        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return employee;
    }

    /**
     * 统计在职员工数量
     */
    public int getActiveEmployeeCount() {
        return employeeMapper.countActiveEmployees();
    }

    /**
     * 员工创建请求DTO
     */
    public static class EmployeeCreateRequest {
        private String username;
        private String password;
        private String realName;
        private String email;
        private String phone;
        private String employeeNo;
        private Long departmentId;
        private String position;
        private LocalDate hireDate;
        private List<Long> roleIds;

        // Getter and Setter methods
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmployeeNo() {
            return employeeNo;
        }

        public void setEmployeeNo(String employeeNo) {
            this.employeeNo = employeeNo;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public LocalDate getHireDate() {
            return hireDate;
        }

        public void setHireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
        }

        public List<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(List<Long> roleIds) {
            this.roleIds = roleIds;
        }
    }

    /**
     * 员工更新请求DTO
     */
    public static class EmployeeUpdateRequest {
        private String password;
        private String realName;
        private String email;
        private String phone;
        private String employeeNo;
        private Long departmentId;
        private String position;
        private LocalDate hireDate;
        private List<Long> roleIds;

        // Getter and Setter methods
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmployeeNo() {
            return employeeNo;
        }

        public void setEmployeeNo(String employeeNo) {
            this.employeeNo = employeeNo;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public LocalDate getHireDate() {
            return hireDate;
        }

        public void setHireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
        }

        public List<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(List<Long> roleIds) {
            this.roleIds = roleIds;
        }
    }
}