import { permission } from './permission'

/**
 * 注册全局指令
 * @param {Object} app Vue应用实例
 */
export function setupDirectives(app) {
  // 注册权限指令
  app.directive('permission', permission)
}

export { permission }
export * from './permission'