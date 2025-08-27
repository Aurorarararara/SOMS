import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useBreadcrumbStore = defineStore('breadcrumb', () => {
  // 状态
  const visitHistory = ref([]) // 访问历史记录
  const favoriteRoutes = ref([]) // 收藏的路径
  const maxHistorySize = ref(20) // 最大历史记录数量
  
  // 路径映射配置 - 员工端
  const routeMap = ref({
    '/dashboard': { title: '个人工作台', icon: 'Odometer', category: '工作台' },
    '/attendance': { title: '我的考勤', icon: 'Clock', category: '考勤管理' },
    '/attendance/records': { title: '考勤记录', icon: 'Clock', category: '考勤管理', parent: '/attendance' },
    '/leave': { title: '请假申请', icon: 'Calendar', category: '请假管理' },
    '/leave/records': { title: '请假记录', icon: 'Calendar', category: '请假管理', parent: '/leave' },
    '/schedule': { title: '日程管理', icon: 'Calendar', category: '日程' },
    '/notifications': { title: '公告通知', icon: 'Bell', category: '通知' },
    '/notifications/:id': { title: '通知详情', icon: 'Bell', category: '通知', parent: '/notifications' },
    '/profile': { title: '个人信息', icon: 'User', category: '个人' },
    '/collaborative': { title: '协同编辑', icon: 'Edit', category: '协同' },
    '/collaborative/demo': { title: '协同编辑功能演示', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/richtext': { title: '富文本编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/richtext/:id': { title: '富文本编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/markdown': { title: 'Markdown编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/markdown/:id': { title: 'Markdown编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/code': { title: '代码编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/code/:id': { title: '代码编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/table': { title: '表格编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/collaborative/table/:id': { title: '表格编辑器', icon: 'Edit', category: '协同', parent: '/collaborative' },
    '/tasks': { title: '任务管理', icon: 'List', category: '任务' },
    '/tasks/create': { title: '创建任务', icon: 'Plus', category: '任务', parent: '/tasks' },
    '/tasks/:id': { title: '任务详情', icon: 'Document', category: '任务', parent: '/tasks' }
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
      return [{ title: '首页', path: '/dashboard' }]
    }

    // 添加首页
    if (currentPath !== '/dashboard') {
      breadcrumbs.push({ title: '首页', path: '/dashboard', icon: 'House' })
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
      localStorage.setItem('employee_breadcrumb_history', JSON.stringify(visitHistory.value))
      localStorage.setItem('employee_breadcrumb_favorites', JSON.stringify(favoriteRoutes.value))
    } catch (error) {
      console.error('保存面包屑数据到本地存储失败:', error)
    }
  }

  /**
   * 从本地存储加载数据
   */
  const loadFromLocalStorage = () => {
    try {
      const historyData = localStorage.getItem('employee_breadcrumb_history')
      const favoritesData = localStorage.getItem('employee_breadcrumb_favorites')

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
