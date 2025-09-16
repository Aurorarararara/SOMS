import { useUserStore } from '@/stores/user'

/**
 * 权限指令
 * 用法：
 * v-permission="'admin'" - 检查单个角色
 * v-permission="['admin', 'manager']" - 检查多个角色（满足其一即可）
 * v-permission.all="['admin', 'manager']" - 检查多个角色（必须全部满足）
 */
export const permission = {
  mounted(el, binding) {
    checkPermission(el, binding)
  },
  updated(el, binding) {
    checkPermission(el, binding)
  }
}

function checkPermission(el, binding) {
  const { value, modifiers } = binding
  const userStore = useUserStore()
  
  if (!value) {
    console.warn('权限指令需要指定角色参数')
    return
  }
  
  let hasPermission = false
  
  if (Array.isArray(value)) {
    if (modifiers.all) {
      // 必须拥有所有指定角色
      hasPermission = value.every(role => userStore.hasRole(role))
    } else {
      // 拥有任意一个角色即可
      hasPermission = userStore.hasAnyRole(value)
    }
  } else {
    // 单个角色检查
    hasPermission = userStore.hasRole(value)
  }
  
  if (!hasPermission) {
    // 没有权限则隐藏元素
    el.style.display = 'none'
    el.setAttribute('data-permission-hidden', 'true')
  } else {
    // 有权限则显示元素
    if (el.getAttribute('data-permission-hidden')) {
      el.style.display = ''
      el.removeAttribute('data-permission-hidden')
    }
  }
}

/**
 * 权限检查函数
 * 用于在JavaScript代码中检查权限
 */
export function checkUserPermission(roles) {
  const userStore = useUserStore()
  
  if (!roles) return true
  
  if (Array.isArray(roles)) {
    return userStore.hasAnyRole(roles)
  } else {
    return userStore.hasRole(roles)
  }
}

/**
 * 管理员权限检查
 */
export function isAdmin() {
  const userStore = useUserStore()
  return userStore.isAdmin
}

/**
 * 管理者权限检查（管理员或经理）
 */
export function isManager() {
  const userStore = useUserStore()
  return userStore.isManager
}

/**
 * 权限常量
 */
export const ROLES = {
  ADMIN: 'admin',
  MANAGER: 'manager',
  USER: 'user',
  HR: 'hr',
  FINANCE: 'finance'
}

/**
 * 权限组合
 */
export const ROLE_GROUPS = {
  MANAGEMENT: [ROLES.ADMIN, ROLES.MANAGER],
  APPROVAL: [ROLES.ADMIN, ROLES.MANAGER, ROLES.HR, ROLES.FINANCE],
  ALL_USERS: [ROLES.ADMIN, ROLES.MANAGER, ROLES.USER, ROLES.HR, ROLES.FINANCE]
}