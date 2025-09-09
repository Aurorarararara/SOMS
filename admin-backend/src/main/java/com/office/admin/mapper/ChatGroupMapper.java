package com.office.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.admin.entity.ChatGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    /**
     * 获取用户群组列表（包含群组信息和用户在群中的角色）
     */
    @Select("SELECT g.*, e.name as creator_name, d.name as department_name, " +
            "gm.role as user_role, gm.is_muted as user_muted, " +
            "gm.unread_count as user_unread_count " +
            "FROM chat_groups g " +
            "LEFT JOIN employees e ON g.creator_id = e.id " +
            "LEFT JOIN departments d ON g.department_id = d.id " +
            "INNER JOIN chat_group_members gm ON g.id = gm.group_id " +
            "WHERE gm.user_id = #{userId} AND gm.status = 'active' " +
            "AND g.status = 'active' " +
            "ORDER BY gm.is_pinned DESC, g.updated_at DESC")
    List<Map<String, Object>> selectUserGroups(Long userId);

    /**
     * 获取群组详情（包含创建者和部门信息）
     */
    @Select("SELECT g.*, e.name as creator_name, d.name as department_name " +
            "FROM chat_groups g " +
            "LEFT JOIN employees e ON g.creator_id = e.id " +
            "LEFT JOIN departments d ON g.department_id = d.id " +
            "WHERE g.id = #{groupId}")
    Map<String, Object> selectGroupWithDetails(Long groupId);

    /**
     * 获取群组成员列表
     */
    @Select("SELECT gm.*, e.name as user_name, e.avatar as user_avatar, " +
            "d.name as department_name, e.position " +
            "FROM chat_group_members gm " +
            "LEFT JOIN employees e ON gm.user_id = e.id " +
            "LEFT JOIN departments d ON e.department_id = d.id " +
            "WHERE gm.group_id = #{groupId} AND gm.status = 'active' " +
            "ORDER BY gm.role DESC, gm.join_time ASC")
    List<Map<String, Object>> selectGroupMembers(Long groupId);

    /**
     * 获取群组统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_members, " +
            "COUNT(CASE WHEN gm.status = 'active' THEN 1 END) as active_members, " +
            "COUNT(CASE WHEN gm.role = 'admin' THEN 1 END) as admin_count " +
            "FROM chat_group_members gm " +
            "WHERE gm.group_id = #{groupId}")
    Map<String, Object> selectGroupStats(Long groupId);

    /**
     * 检查用户是否为群成员
     */
    @Select("SELECT COUNT(*) > 0 FROM chat_group_members " +
            "WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 'active'")
    Boolean isGroupMember(Long groupId, Long userId);

    /**
     * 获取用户在群中的角色
     */
    @Select("SELECT role FROM chat_group_members " +
            "WHERE group_id = #{groupId} AND user_id = #{userId} AND status = 'active'")
    String getUserRoleInGroup(Long groupId, Long userId);
}