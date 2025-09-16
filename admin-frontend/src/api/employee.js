import request from '@/utils/request'

export const employeeApi = {
  // 分页查询员工列表
  getEmployeeList(params) {
    return request({
      url: '/api/admin/employees',
      method: 'get',
      params
    })
  },

  // 搜索员工
  searchEmployees(keyword) {
    return request({
      url: '/api/admin/employees/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 根据部门查询员工
  getEmployeesByDepartment(departmentId) {
    return request({
      url: `/api/admin/employees/department/${departmentId}`,
      method: 'get'
    })
  },

  // 获取员工详情
  getEmployeeDetail(employeeId) {
    return request({
      url: `/api/admin/employees/${employeeId}`,
      method: 'get'
    })
  },

  // 创建员工
  createEmployee(data) {
    return request({
      url: '/api/admin/employees',
      method: 'post',
      data
    })
  },

  // 更新员工信息
  updateEmployee(employeeId, data) {
    return request({
      url: `/api/admin/employees/${employeeId}`,
      method: 'put',
      data
    })
  },

  // 启用/禁用员工
  toggleEmployeeStatus(employeeId) {
    return request({
      url: `/api/admin/employees/${employeeId}/toggle-status`,
      method: 'put'
    })
  },

  // 删除员工
  deleteEmployee(employeeId) {
    return request({
      url: `/api/admin/employees/${employeeId}`,
      method: 'delete'
    })
  },

  // 统计在职员工数量
  getActiveEmployeeCount() {
    return request({
      url: '/api/admin/employees/count/active',
      method: 'get'
    })
  }
}

// 部门管理API
export const departmentApi = {
  // 获取所有部门列表
  getDepartmentList() {
    return request({
      url: '/api/admin/departments',
      method: 'get'
    })
  },

  // 获取部门树
  getDepartmentTree() {
    return request({
      url: '/api/admin/departments/tree',
      method: 'get'
    })
  },

  // 获取部门详情
  getDepartmentDetail(departmentId) {
    return request({
      url: `/api/admin/departments/${departmentId}`,
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

  // 更新部门信息
  updateDepartment(departmentId, data) {
    return request({
      url: `/api/admin/departments/${departmentId}`,
      method: 'put',
      data
    })
  },

  // 删除部门
  deleteDepartment(departmentId) {
    return request({
      url: `/api/admin/departments/${departmentId}`,
      method: 'delete'
    })
  },

  // 启用/禁用部门
  toggleDepartmentStatus(departmentId) {
    return request({
      url: `/api/admin/departments/${departmentId}/toggle-status`,
      method: 'put'
    })
  },

  // 获取部门员工列表
  getDepartmentEmployees(departmentId) {
    return request({
      url: `/api/admin/departments/${departmentId}/employees`,
      method: 'get'
    })
  }
}