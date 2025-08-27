package com.office.admin.dto;

import com.office.admin.entity.User;
import com.office.admin.entity.Employee;
import com.office.admin.entity.Role;

import java.util.List;

/**
 * 管理员登录响应DTO
 *
 * @author office-system
 * @since 2024-01-01
 */
public class LoginResponse {

    private String token;
    private AdminUserInfo userInfo;

    // 构造函数
    public LoginResponse() {}

    public LoginResponse(String token, AdminUserInfo userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }

    // Getter and Setter methods
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AdminUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(AdminUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * 管理员用户信息内部类
     */
    public static class AdminUserInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String avatar;
        private String employeeNo;
        private String departmentName;
        private String position;
        private List<String> roles;
        private List<String> permissions;
        private Boolean isSuperAdmin;

        // 构造函数
        public AdminUserInfo() {}

        public AdminUserInfo(User user, Employee employee) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.realName = user.getRealName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
            this.avatar = user.getAvatar();
            if (employee != null) {
                this.employeeNo = employee.getEmployeeNo();
                this.position = employee.getPosition();
            }
            this.isSuperAdmin = false;
        }

        // Getter and Setter methods
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getEmployeeNo() {
            return employeeNo;
        }

        public void setEmployeeNo(String employeeNo) {
            this.employeeNo = employeeNo;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public Boolean getIsSuperAdmin() {
            return isSuperAdmin;
        }

        public void setIsSuperAdmin(Boolean isSuperAdmin) {
            this.isSuperAdmin = isSuperAdmin;
        }
    }
}