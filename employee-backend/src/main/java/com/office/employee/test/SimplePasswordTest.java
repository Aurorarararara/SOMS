package com.office.employee.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 简化密码测试
 */
public class SimplePasswordTest {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 从错误日志中获取的数据库哈希值
        String databaseHash = "$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW";
        
        // 测试不同的密码
        String[] testPasswords = {"123456", "password", "emp001", "admin"};
        
        System.out.println("数据库哈希值: " + databaseHash);
        System.out.println("PasswordEncoder类型: " + passwordEncoder.getClass().getName());
        System.out.println();
        
        for (String password : testPasswords) {
            boolean matches = passwordEncoder.matches(password, databaseHash);
            System.out.println("密码 '" + password + "' 验证结果: " + matches);
        }
        
        // 生成新的密码哈希值
        System.out.println("\n生成新的'123456'密码哈希值:");
        String newHash = passwordEncoder.encode("123456");
        System.out.println("新哈希值: " + newHash);
        
        boolean newMatches = passwordEncoder.matches("123456", newHash);
        System.out.println("新哈希值验证结果: " + newMatches);
    }
}