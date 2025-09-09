package com.office.employee.dto;

import com.office.employee.entity.User;
import com.office.employee.entity.Employee;
import com.office.employee.entity.Role;

import java.util.List;

/**
 * 登录响应DTO
 *
 * @author office-system
 * @since 2024-01-01
 */
public class LoginResponse {

    private String token;
    private UserInfo userInfo;

    // 构造函数
    public LoginResponse() {}

    public LoginResponse(String token, UserInfo userInfo) {
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

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * 用户信息内部类
     */
    public static class UserInfo {
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

        // 构造函数
        public UserInfo() {}

        public UserInfo(User user, Employee employee) {
            if (user != null) {
                this.id = user.getId();
                this.username = user.getUsername();
                this.realName = user.getRealName();
                this.email = user.getEmail();
                this.phone = user.getPhone();
                this.avatar = user.getAvatar();
            } else {
                // 提供默认值以避免空指针
                this.id = 0L;
                this.username = "guest";
                this.realName = "游客用户";
                this.email = "";
                this.phone = "";
                this.avatar = "";
            }
            
            if (employee != null) {
                this.employeeNo = employee.getEmployeeNo();
                this.position = employee.getPosition();
            } else {
                // 提供默认值以避免空指针
                this.employeeNo = "";
                this.position = "";
            }
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
    }
}