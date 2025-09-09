package com.office.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体类
 *
 * @author office-system
 * @since 2024-01-01
 */
@TableName("departments")
public class Department {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("parent_id")
    private Long parentId;

    @TableField("level")
    private Integer level;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("description")
    private String description;

    @TableField("status")
    private Integer status;

    @TableField("manager_id")
    private Long managerId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 关联字段（不存储在数据库）
    @TableField(exist = false)
    private List<Department> children;

    @TableField(exist = false)
    private Integer employeeCount;
    
    @TableField(exist = false)
    private List<Employee> employees;

    // 构造函数
    public Department() {}

    public Department(String name, Long parentId, Integer level) {
        this.name = name;
        this.parentId = parentId;
        this.level = level;
        this.status = 1;
        this.sortOrder = 0;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<Department> getChildren() {
        return children;
    }

    public void setChildren(List<Department> children) {
        this.children = children;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }
    
    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
}