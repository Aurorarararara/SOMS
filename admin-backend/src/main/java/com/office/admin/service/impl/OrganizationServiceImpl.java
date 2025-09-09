package com.office.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.office.admin.entity.Department;
import com.office.admin.entity.Employee;
import com.office.admin.entity.User;
import com.office.admin.mapper.DepartmentMapper;
import com.office.admin.mapper.EmployeeMapper;
import com.office.admin.mapper.UserMapper;
import com.office.admin.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织架构服务实现类
 *
 * @author office-system
 * @since 2024-01-01
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取完整的组织架构树
     */
    @Override
    public List<Department> getOrganizationTree() {
        // 获取所有有效的部门
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByAsc("level", "sort_order");
        List<Department> allDepartments = departmentMapper.selectList(queryWrapper);

        // 构建部门树结构
        return buildDepartmentTree(allDepartments, 0L);
    }

    /**
     * 获取部门层级结构（包含员工信息）
     */
    @Override
    public Department getDepartmentHierarchy(Long deptId) {
        // 获取部门信息
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            return null;
        }

        // 获取部门员工
        QueryWrapper<Employee> empQuery = new QueryWrapper<>();
        empQuery.eq("department_id", deptId);
        empQuery.eq("status", 1);
        List<Employee> employees = employeeMapper.selectList(empQuery);

        // 关联用户信息
        if (!employees.isEmpty()) {
            List<Long> userIds = employees.stream()
                    .map(Employee::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.in("id", userIds);
                List<User> users = userMapper.selectList(userQuery);

                Map<Long, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

                employees.forEach(emp -> {
                    User user = userMap.get(emp.getUserId());
                    if (user != null) {
                        emp.setUser(user);
                    }
                });
            }
        }

        department.setEmployees(employees);

        // 获取子部门
        List<Department> children = departmentMapper.findByParentId(deptId);
        department.setChildren(children);

        return department;
    }

    /**
     * 获取组织架构统计信息
     */
    @Override
    public Map<String, Object> getOrganizationStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计部门数量
        QueryWrapper<Department> deptQuery = new QueryWrapper<>();
        deptQuery.eq("status", 1);
        Long totalDepartments = departmentMapper.selectCount(deptQuery);
        stats.put("totalDepartments", totalDepartments);

        // 统计员工数量
        QueryWrapper<Employee> empQuery = new QueryWrapper<>();
        empQuery.eq("status", 1);
        Long totalEmployees = employeeMapper.selectCount(empQuery);
        stats.put("totalEmployees", totalEmployees);

        // 按部门统计员工数量
        List<Map<String, Object>> deptStats = departmentMapper.getDepartmentEmployeeStats();
        stats.put("departmentStats", deptStats);

        // 统计层级信息
        QueryWrapper<Department> levelQuery = new QueryWrapper<>();
        levelQuery.select("level", "COUNT(*) as count");
        levelQuery.eq("status", 1);
        levelQuery.groupBy("level");
        levelQuery.orderByAsc("level");
        List<Map<String, Object>> levelStats = departmentMapper.selectMaps(levelQuery);
        stats.put("levelStats", levelStats);

        return stats;
    }

    /**
     * 搜索部门和员工
     */
    @Override
    public List<Object> searchOrganization(String keyword) {
        List<Object> results = new ArrayList<>();

        // 搜索部门
        QueryWrapper<Department> deptQuery = new QueryWrapper<>();
        deptQuery.like("name", keyword);
        deptQuery.eq("status", 1);
        List<Department> departments = departmentMapper.selectList(deptQuery);
        results.addAll(departments);

        // 搜索员工（通过用户姓名）
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.like("real_name", keyword);
        List<User> users = userMapper.selectList(userQuery);

        if (!users.isEmpty()) {
            List<Long> userIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            QueryWrapper<Employee> empQuery = new QueryWrapper<>();
            empQuery.in("user_id", userIds);
            empQuery.eq("status", 1);
            List<Employee> employees = employeeMapper.selectList(empQuery);

            // 关联用户信息
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));

            employees.forEach(emp -> {
                User user = userMap.get(emp.getUserId());
                if (user != null) {
                    emp.setUser(user);
                }
            });

            results.addAll(employees);
        }

        return results;
    }

    /**
     * 获取部门路径（面包屑导航用）
     */
    @Override
    public List<Department> getDepartmentPath(Long deptId) {
        List<Department> path = new ArrayList<>();
        Department current = departmentMapper.selectById(deptId);

        while (current != null) {
            path.add(0, current); // 添加到开头
            if (current.getParentId() != null && current.getParentId() > 0) {
                current = departmentMapper.selectById(current.getParentId());
            } else {
                break;
            }
        }

        return path;
    }

    /**
     * 调整部门结构（移动部门）
     */
    @Override
    @Transactional
    public void moveDepartment(Long deptId, Long newParentId, Integer sortOrder) {
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        // 更新部门信息
        department.setParentId(newParentId);
        
        // 计算新的层级
        if (newParentId == 0) {
            department.setLevel(1);
        } else {
            Department parent = departmentMapper.selectById(newParentId);
            if (parent != null) {
                department.setLevel(parent.getLevel() + 1);
            }
        }

        // 设置排序
        if (sortOrder != null) {
            department.setSortOrder(sortOrder);
        } else {
            // 获取新的最大排序值
            int maxSortOrder = departmentMapper.getMaxSortOrder(newParentId);
            department.setSortOrder(maxSortOrder + 1);
        }

        departmentMapper.updateById(department);

        // 递归更新子部门的层级
        updateChildDepartmentLevels(deptId, department.getLevel());
    }

    /**
     * 创建部门
     */
    @Override
    @Transactional
    public Department createDepartment(Department department) {
        // 设置默认值
        if (department.getStatus() == null) {
            department.setStatus(1); // 默认启用
        }
        
        // 计算层级
        if (department.getParentId() == null || department.getParentId() == 0) {
            department.setParentId(0L);
            department.setLevel(1);
        } else {
            Department parent = departmentMapper.selectById(department.getParentId());
            if (parent != null) {
                department.setLevel(parent.getLevel() + 1);
            } else {
                department.setLevel(1);
            }
        }
        
        // 设置排序
        if (department.getSortOrder() == null) {
            int maxSortOrder = departmentMapper.getMaxSortOrder(department.getParentId());
            department.setSortOrder(maxSortOrder + 1);
        }
        
        // 设置创建时间
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());
        
        // 插入部门
        departmentMapper.insert(department);
        
        return department;
    }

    /**
     * 更新部门
     */
    @Override
    @Transactional
    public Department updateDepartment(Long id, Department department) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("部门不存在");
        }
        
        // 更新字段
        existing.setName(department.getName());
        existing.setDescription(department.getDescription());
        existing.setManagerId(department.getManagerId());
        existing.setStatus(department.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        
        // 更新部门
        departmentMapper.updateById(existing);
        
        return existing;
    }

    /**
     * 删除部门
     */
    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }
        
        // 检查是否有子部门
        int childCount = departmentMapper.countChildDepartments(id);
        if (childCount > 0) {
            throw new RuntimeException("该部门下有子部门，无法删除");
        }
        
        // 检查是否有员工
        int employeeCount = departmentMapper.countDepartmentEmployees(id);
        if (employeeCount > 0) {
            throw new RuntimeException("该部门下有员工，无法删除");
        }
        
        // 删除部门
        departmentMapper.deleteById(id);
    }

    /**
     * 获取部门详情
     */
    @Override
    public Department getDepartmentById(Long id) {
        return departmentMapper.selectById(id);
    }

    /**
     * 启用/禁用部门
     */
    @Override
    @Transactional
    public void toggleDepartmentStatus(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }
        
        // 切换状态
        department.setStatus(department.getStatus() == 1 ? 0 : 1);
        department.setUpdateTime(LocalDateTime.now());
        
        departmentMapper.updateById(department);
    }

    /**
     * 获取部门员工列表
     */
    @Override
    public List<Employee> getDepartmentEmployees(Long deptId) {
        // 检查部门是否存在
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        // 获取部门员工
        QueryWrapper<Employee> empQuery = new QueryWrapper<>();
        empQuery.eq("department_id", deptId);
        empQuery.eq("status", 1);
        List<Employee> employees = employeeMapper.selectList(empQuery);

        // 关联用户信息
        if (!employees.isEmpty()) {
            List<Long> userIds = employees.stream()
                    .map(Employee::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.in("id", userIds);
                List<User> users = userMapper.selectList(userQuery);

                Map<Long, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

                employees.forEach(emp -> {
                    User user = userMap.get(emp.getUserId());
                    if (user != null) {
                        emp.setUser(user);
                    }
                });
            }
        }

        return employees;
    }

    /**
     * 递归更新子部门层级
     */
    private void updateChildDepartmentLevels(Long parentId, Integer parentLevel) {
        List<Department> children = departmentMapper.findByParentId(parentId);
        for (Department child : children) {
            child.setLevel(parentLevel + 1);
            departmentMapper.updateById(child);
            updateChildDepartmentLevels(child.getId(), child.getLevel());
        }
    }

    /**
     * 构建部门树结构
     */
    private List<Department> buildDepartmentTree(List<Department> allDepartments, Long parentId) {
        List<Department> children = new ArrayList<>();
        
        for (Department dept : allDepartments) {
            if (Objects.equals(dept.getParentId(), parentId)) {
                // 递归构建子部门树
                List<Department> childDepartments = buildDepartmentTree(allDepartments, dept.getId());
                dept.setChildren(childDepartments);
                
                // 获取员工数量
                if (dept.getId() != null) {
                    Long employeeCount = employeeMapper.selectCount(
                        new QueryWrapper<Employee>()
                            .eq("department_id", dept.getId())
                            .eq("status", 1)
                    );
                    dept.setEmployeeCount(employeeCount.intValue());
                }
                
                children.add(dept);
            }
        }
        
        // 按排序字段排序
        children.sort(Comparator.comparing(Department::getSortOrder));
        return children;
    }
}