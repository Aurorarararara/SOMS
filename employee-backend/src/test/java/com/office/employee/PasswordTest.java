package com.office.employee;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码测试类
 * 用于验证BCrypt密码加密和验证逻辑
 */
public class PasswordTest {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 数据库中的密码哈希值
        String databaseHash = "$2a$10$7JB720yubVSOfvVhdXe/2.6hHprl/ruNSiN6ym2qJR7Cj/3OwENxW";
        
        // 用户输入的密码
        String inputPassword = "123456";
        
        // 验证密码
        boolean matches = passwordEncoder.matches(inputPassword, databaseHash);
        System.out.println("密码验证结果: " + matches);
        
        // 生成多个新的密码哈希值进行对比
        System.out.println("\n生成新的密码哈希值:");
        for (int i = 1; i <= 5; i++) {
            String newHash = passwordEncoder.encode(inputPassword);
            System.out.println("第" + i + "次生成: " + newHash);
            
            // 验证新生成的哈希值
            boolean newMatches = passwordEncoder.matches(inputPassword, newHash);
            System.out.println("验证结果: " + newMatches);
            System.out.println();
        }
        
        // 生成一个标准的哈希值用于数据库更新
        System.out.println("推荐用于数据库的哈希值:");
        String recommendedHash = passwordEncoder.encode(inputPassword);
        System.out.println(recommendedHash);
        
        // 验证推荐的哈希值
        boolean recommendedMatches = passwordEncoder.matches(inputPassword, recommendedHash);
        System.out.println("推荐哈希值验证结果: " + recommendedMatches);
    }
}