import request from '@/utils/request'

export const roleApi = {
  // 获取所有有效角色列表
  getAllRoles() {
    return request({
      url: '/api/admin/roles',
      method: 'get'
    })
  },

  // 分页查询角色列表
  getRoleList(params) {
    return request({
      url: '/api/admin/roles/page',
      method: 'get',
      params
    })
  },

  // 获取角色详情
  getRoleDetail(roleId) {
    return request({
      url: `/api/admin/roles/${roleId}`,
      method: 'get'
    })
  },

  // 创建角色
  createRole(data) {
    return request({
      url: '/api/admin/roles',
      method: 'post',
      data
    })
  },

  // 更新角色
  updateRole(roleId, data) {
    return request({
      url: `/api/admin/roles/${roleId}`,
      method: 'put',
      data
    })
  },

  // 删除角色
  deleteRole(roleId) {
    return request({
      url: `/api/admin/roles/${roleId}`,
      method: 'delete'
    })
  },

  // 启用/禁用角色
  toggleRoleStatus(roleId, status) {
    return request({
      url: `/api/admin/roles/${roleId}/status`,
      method: 'put',
      params: { status }
    })
  },

  // 获取角色的用户数量
  getRoleUserCount(roleId) {
    return request({
      url: `/api/admin/roles/${roleId}/users/count`,
      method: 'get'
    })
  },

  // 为用户分配角色
  assignRoleToUser(userId, roleIds) {
    return request({
      url: '/api/admin/roles/assign',
      method: 'post',
      data: { userId, roleIds }
    })
  },

  // 移除用户角色
  removeRoleFromUser(userId, roleId) {
    return request({
      url: '/api/admin/roles/remove',
      method: 'post',
      data: { userId, roleId }
    })
  }
}