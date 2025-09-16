import request from '@/utils/request'

/**
 * 部门管理API
 */
export default {
  // 获取所有部门
  getAllDepartments() {
    return request({
      url: '/api/admin/departments',
      method: 'get'
    })
  },

  // 获取部门详情
  getDepartmentDetail(id) {
    return request({
      url: `/api/admin/departments/${id}`,
      method: 'get'
    })
  },

  // 创建部门
  createDepartment(data) {
    return request({
      url: '/api/admin/departments',
      method: 'post',
      data
    })
  },

  // 更新部门
  updateDepartment(id, data) {
    return request({
      url: `/api/admin/departments/${id}`,
      method: 'put',
      data
    })
  },

  // 删除部门
  deleteDepartment(id) {
    return request({
      url: `/api/admin/departments/${id}`,
      method: 'delete'
    })
  },

  // 获取部门树结构
  getDepartmentTree() {
    return request({
      url: '/api/admin/departments/tree',
      method: 'get'
    })
  }
}