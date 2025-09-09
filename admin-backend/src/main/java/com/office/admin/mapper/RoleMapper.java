package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author office-system
 * @since 2024-01-01
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
     */
    @Select("SELECT * FROM roles WHERE code = #{code} AND status = 1")
    Role findByCode(@Param("code") String code);

    /**
     * 根据用户ID查询用户角色列表
     */
    @Select("SELECT r.* FROM roles r " +
            "INNER JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 检查角色编码是否存在
     */
    @Select("SELECT COUNT(*) FROM roles WHERE code = #{code} AND id != #{excludeId}")
    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * 检查角色名称是否存在
     */
    @Select("SELECT COUNT(*) FROM roles WHERE name = #{name} AND id != #{excludeId}")
    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 查询所有有效角色
     */
    @Select("SELECT * FROM roles WHERE status = 1 ORDER BY create_time")
    List<Role> findAllActiveRoles();

    /**
     * 为用户分配角色
     */
    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int assignRoleToUser(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 删除用户的角色
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int removeRoleFromUser(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 删除用户的所有角色
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int removeAllRolesFromUser(@Param("userId") Long userId);

    /**
     * 查询角色的用户数量
     */
    @Select("SELECT COUNT(*) FROM user_roles WHERE role_id = #{roleId}")
    int countUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 检查用户是否有指定角色
     */
    @Select("SELECT COUNT(*) FROM user_roles ur " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.code = #{roleCode}")
    int checkUserHasRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}