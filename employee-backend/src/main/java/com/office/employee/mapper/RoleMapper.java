package com.office.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.employee.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT COUNT(*) FROM roles WHERE code = #{code}")
    int countByCode(@Param("code") String code);

    /**
     * 检查角色名称是否存在
     */
    @Select("SELECT COUNT(*) FROM roles WHERE name = #{name}")
    int countByName(@Param("name") String name);
}