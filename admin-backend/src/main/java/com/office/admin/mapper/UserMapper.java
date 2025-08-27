package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（管理员权限检查）
     */
    @Select("SELECT u.* FROM users u " +
            "INNER JOIN user_roles ur ON u.id = ur.user_id " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE u.username = #{username} AND u.status = 1 " +
            "AND r.code IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    User findAdminByUsername(@Param("username") String username);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND status = 1")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户名和密码查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND password = #{password} AND status = 1")
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    int countByEmail(@Param("email") String email);

    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone}")
    int countByPhone(@Param("phone") String phone);

    /**
     * 验证用户是否有管理员权限
     */
    @Select("SELECT COUNT(*) FROM user_roles ur " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.code IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    int checkAdminPermission(@Param("userId") Long userId);
}