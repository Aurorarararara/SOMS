import request from '@/utils/request'

export const organizationApi = {
  // 获取完整的组织架构树
  getOrganizationTree() {
    return request({
      url: '/api/admin/organization/tree',
      method: 'get'
    })
  },

  // 获取部门及其下级部门和员工信息
  getDepartmentHierarchy(departmentId) {
    return request({
      url: `/api/admin/organization/departments/${departmentId}/hierarchy`,
      method: 'get'
    })
  },

  // 获取组织架构统计信息
  getOrganizationStats() {
    return request({
      url: '/api/admin/organization/statistics',
      method: 'get'
    })
  },

  // 搜索部门和员工
  searchOrganization(keyword) {
    return request({
      url: '/api/admin/organization/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 获取部门路径（面包屑导航用）
  getDepartmentPath(departmentId) {
    return request({
      url: `/api/admin/organization/departments/${departmentId}/path`,
      method: 'get'
    })
  },

  // 调整部门结构（移动部门）
  moveDepartment(departmentId, newParentId, sortOrder) {
    return request({
      url: `/api/admin/organization/departments/${departmentId}/move`,
      method: 'put',
      data: { newParentId, sortOrder }
    })
  }
}