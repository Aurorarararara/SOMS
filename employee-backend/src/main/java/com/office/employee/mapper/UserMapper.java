package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

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
    @Select("SELECT COUNT(*) FROM user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);
    
    /**
     * 根据团队ID查询用户列表
     */
    @Select("SELECT u.* FROM user u INNER JOIN employee e ON u.id = e.user_id WHERE e.team_id = #{teamId}")
    List<User> selectByTeamId(@Param("teamId") Long teamId);

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
}