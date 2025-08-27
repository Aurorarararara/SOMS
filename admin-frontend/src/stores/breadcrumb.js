import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useBreadcrumbStore = defineStore('breadcrumb', () => {
  // 状态
  const visitHistory = ref([]) // 访问历史记录
  const favoriteRoutes = ref([]) // 收藏的路径
  const maxHistorySize = ref(20) // 最大历史记录数量
  
  // 路径映射配置 - 管理端
  const routeMap = ref({
    '/admin': { title: '管理后台', icon: 'House', category: '首页' },
    '/admin/dashboard': { title: '综合仪表盘', icon: 'DataBoard', category: '仪表盘' },
    '/admin/employees': { title: '员工管理', icon: 'User', category: '用户管理' },
    '/admin/departments': { title: '部门管理', icon: 'OfficeBuilding', category: '用户管理' },
    '/admin/roles': { title: '角色管理', icon: 'UserFilled', category: '用户管理' },
    '/admin/organization/chart': { title: '组织架构图', icon: 'OfficeBuilding', category: '用户管理' },
    
    // 考勤管理
    '/admin/attendance/records': { title: '考勤记录', icon: 'Clock', category: '考勤管理' },
    '/admin/attendance/rules': { title: '考勤规则', icon: 'Setting', category: '考勤管理' },
    '/admin/attendance/statistics': { title: '考勤统计', icon: 'DataAnalysis', category: '考勤管理' },
    
    // 请假管理
    '/admin/leave/applications': { title: '请假申请', icon: 'Calendar', category: '请假管理' },
    '/admin/leave/approval': { title: '审批管理', icon: 'Select', category: '请假管理' },
    '/admin/leave/statistics': { title: '请假统计', icon: 'DataAnalysis', category: '请假管理' },
    
    // 公告管理
    '/admin/announcements': { title: '公告管理', icon: 'Bell', category: '内容管理' },
    
    // 审批流程
    '/admin/workflow': { title: '审批流程管理', icon: 'Connection', category: '流程管理' },
    '/admin/workflow/my-tasks': { title: '我的待办任务', icon: 'List', category: '流程管理', parent: '/admin/workflow' },
    '/admin/workflow/my-initiated': { title: '我发起的流程', icon: 'Upload', category: '流程管理', parent: '/admin/workflow' },
    
    // 数据报表
    '/admin/reports/attendance': { title: '考勤报表', icon: 'DataAnalysis', category: '数据报表' },
    '/admin/reports/leave': { title: '请假报表', icon: 'DataAnalysis', category: '数据报表' },
    '/admin/reports/performance': { title: '绩效报表', icon: 'TrendCharts', category: '数据报表' },
    
    // 系统设置
    '/admin/system/config': { title: '系统配置', icon: 'Setting', category: '系统设置' },
    '/admin/system/logs': { title: '操作日志', icon: 'Document', category: '系统设置' },
    '/admin/system/backup': { title: '数据备份', icon: 'FolderOpened', category: '系统设置' },
    
    // 个人信息
    '/admin/profile': { title: '个人信息', icon: 'User', category: '个人' }
  })

  // 计算属性
  const recentRoutes = computed(() => {
    return visitHistory.value.slice(-10).reverse() // 最近10个访问记录，倒序显示
  })

  const favoritesByCategory = computed(() => {
    const grouped = {}
    favoriteRoutes.value.forEach(route => {
      const category = route.category || '其他'
      if (!grouped[category]) {
        grouped[category] = []
      }
      grouped[category].push(route)
    })
    return grouped
  })

  // 方法
  
  /**
   * 添加访问历史记录
   * @param {Object} route - 路由信息
   */
  const addToHistory = (route) => {
    const routeInfo = getRouteInfo(route.path)
    if (!routeInfo) return

    const historyItem = {
      path: route.path,
      title: routeInfo.title,
      icon: routeInfo.icon,
      category: routeInfo.category,
      timestamp: Date.now(),
      params: route.params || {},
      query: route.query || {}
    }

    // 移除已存在的相同路径
    const existingIndex = visitHistory.value.findIndex(item => item.path === route.path)
    if (existingIndex > -1) {
      visitHistory.value.splice(existingIndex, 1)
    }

    // 添加到历史记录开头
    visitHistory.value.unshift(historyItem)

    // 限制历史记录数量
    if (visitHistory.value.length > maxHistorySize.value) {
      visitHistory.value = visitHistory.value.slice(0, maxHistorySize.value)
    }

    // 保存到本地存储
    saveToLocalStorage()
  }

  /**
   * 添加到收藏
   * @param {Object} route - 路由信息
   * @param {String} customTitle - 自定义标题
   */
  const addToFavorites = (route, customTitle = null) => {
    const routeInfo = getRouteInfo(route.path)
    if (!routeInfo) return

    // 检查是否已收藏
    const exists = favoriteRoutes.value.some(item => item.path === route.path)
    if (exists) return false

    const favoriteItem = {
      path: route.path,
      title: customTitle || routeInfo.title,
      icon: routeInfo.icon,
      category: routeInfo.category,
      timestamp: Date.now(),
      params: route.params || {},
      query: route.query || {}
    }

    favoriteRoutes.value.push(favoriteItem)
    saveToLocalStorage()
    return true
  }

  /**
   * 从收藏中移除
   * @param {String} path - 路径
   */
  const removeFromFavorites = (path) => {
    const index = favoriteRoutes.value.findIndex(item => item.path === path)
    if (index > -1) {
      favoriteRoutes.value.splice(index, 1)
      saveToLocalStorage()
      return true
    }
    return false
  }

  /**
   * 检查是否已收藏
   * @param {String} path - 路径
   */
  const isFavorite = (path) => {
    return favoriteRoutes.value.some(item => item.path === path)
  }

  /**
   * 获取路由信息
   * @param {String} path - 路径
   */
  const getRouteInfo = (path) => {
    // 先尝试精确匹配
    if (routeMap.value[path]) {
      return routeMap.value[path]
    }

    // 尝试参数匹配
    for (const [pattern, info] of Object.entries(routeMap.value)) {
      if (pattern.includes(':')) {
        const regex = new RegExp('^' + pattern.replace(/:[^/]+/g, '[^/]+') + '$')
        if (regex.test(path)) {
          return info
        }
      }
    }

    return null
  }

  /**
   * 生成面包屑导航
   * @param {String} currentPath - 当前路径
   */
  const generateBreadcrumb = (currentPath) => {
    const breadcrumbs = []
    const routeInfo = getRouteInfo(currentPath)

    if (!routeInfo) {
      return [{ title: '管理后台', path: '/admin/dashboard' }]
    }

    // 添加首页
    if (currentPath !== '/admin/dashboard') {
      breadcrumbs.push({ title: '管理后台', path: '/admin/dashboard', icon: 'House' })
    }

    // 构建面包屑路径
    const buildPath = (path) => {
      const info = getRouteInfo(path)
      if (!info) return

      // 如果有父级路径，先添加父级
      if (info.parent) {
        buildPath(info.parent)
      }

      // 添加当前路径
      breadcrumbs.push({
        title: info.title,
        path: path,
        icon: info.icon,
        category: info.category
      })
    }

    buildPath(currentPath)
    return breadcrumbs
  }

  /**
   * 清空历史记录
   */
  const clearHistory = () => {
    visitHistory.value = []
    saveToLocalStorage()
  }

  /**
   * 清空收藏
   */
  const clearFavorites = () => {
    favoriteRoutes.value = []
    saveToLocalStorage()
  }

  /**
   * 保存到本地存储
   */
  const saveToLocalStorage = () => {
    try {
      localStorage.setItem('admin_breadcrumb_history', JSON.stringify(visitHistory.value))
      localStorage.setItem('admin_breadcrumb_favorites', JSON.stringify(favoriteRoutes.value))
    } catch (error) {
      console.error('保存面包屑数据到本地存储失败:', error)
    }
  }

  /**
   * 从本地存储加载数据
   */
  const loadFromLocalStorage = () => {
    try {
      const historyData = localStorage.getItem('admin_breadcrumb_history')
      const favoritesData = localStorage.getItem('admin_breadcrumb_favorites')

      if (historyData) {
        visitHistory.value = JSON.parse(historyData)
      }

      if (favoritesData) {
        favoriteRoutes.value = JSON.parse(favoritesData)
      }
    } catch (error) {
      console.error('从本地存储加载面包屑数据失败:', error)
    }
  }

  /**
   * 更新路由映射
   * @param {Object} newRouteMap - 新的路由映射
   */
  const updateRouteMap = (newRouteMap) => {
    routeMap.value = { ...routeMap.value, ...newRouteMap }
  }

  // 初始化时加载本地存储数据
  loadFromLocalStorage()

  return {
    // 状态
    visitHistory,
    favoriteRoutes,
    maxHistorySize,
    routeMap,
    
    // 计算属性
    recentRoutes,
    favoritesByCategory,
    
    // 方法
    addToHistory,
    addToFavorites,
    removeFromFavorites,
    isFavorite,
    getRouteInfo,
    generateBreadcrumb,
    clearHistory,
    clearFavorites,
    saveToLocalStorage,
    loadFromLocalStorage,
    updateRouteMap
  }
})
