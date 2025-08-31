// 员工端搜索配置
export const employeeSearchConfig = [
  // 个人工作台
  {
    id: 'dashboard',
    title: '个人工作台',
    titleKey: 'nav.dashboard',
    path: '/dashboard',
    icon: 'Odometer',
    category: 'dashboard',
    categoryKey: 'search.category.dashboard',
    keywords: ['工作台', 'dashboard', 'workspace', '首页', '概览'],
    description: '查看个人工作概览和待办事项',
    descriptionKey: 'search.desc.dashboard'
  },

  // 考勤管理
  {
    id: 'attendance',
    title: '我的考勤',
    titleKey: 'nav.attendance',
    path: '/attendance',
    icon: 'Clock',
    category: 'attendance',
    categoryKey: 'search.category.attendance',
    keywords: ['考勤', 'attendance', '打卡', 'clock', '上班', '下班'],
    description: '进行考勤打卡和查看考勤状态',
    descriptionKey: 'search.desc.attendance'
  },
  {
    id: 'attendance-records',
    title: '考勤记录',
    titleKey: 'nav.attendanceRecords',
    path: '/attendance/records',
    icon: 'Clock',
    category: 'attendance',
    categoryKey: 'search.category.attendance',
    keywords: ['考勤', 'attendance', '记录', 'record', '历史', '统计'],
    description: '查看个人考勤记录和统计',
    descriptionKey: 'search.desc.attendanceRecords'
  },

  // 请假管理
  {
    id: 'leave',
    title: '请假申请',
    titleKey: 'nav.leave',
    path: '/leave',
    icon: 'Calendar',
    category: 'leave',
    categoryKey: 'search.category.leave',
    keywords: ['请假', 'leave', '申请', 'application', '假期'],
    description: '提交请假申请和查看申请状态',
    descriptionKey: 'search.desc.leave'
  },
  {
    id: 'leave-records',
    title: '请假记录',
    titleKey: 'nav.leaveRecords',
    path: '/leave/records',
    icon: 'Calendar',
    category: 'leave',
    categoryKey: 'search.category.leave',
    keywords: ['请假', 'leave', '记录', 'record', '历史', '统计'],
    description: '查看个人请假记录和统计',
    descriptionKey: 'search.desc.leaveRecords'
  },

  // 协同编辑
  {
    id: 'collaborative',
    title: '协同编辑',
    titleKey: 'nav.collaborative',
    path: '/collaborative',
    icon: 'EditPen',
    category: 'collaborative',
    categoryKey: 'search.category.collaborative',
    keywords: ['协同', 'collaborative', '编辑', 'edit', '文档', '合作'],
    description: '协同编辑功能概览',
    descriptionKey: 'search.desc.collaborative'
  },
  {
    id: 'collaborative-richtext',
    title: '富文本编辑器',
    titleKey: 'nav.richtext',
    path: '/collaborative/richtext',
    icon: 'EditPen',
    category: 'collaborative',
    categoryKey: 'search.category.collaborative',
    keywords: ['富文本', 'richtext', '编辑器', 'editor', '文档', '协同'],
    description: '多人协同富文本编辑',
    descriptionKey: 'search.desc.richtext'
  },
  {
    id: 'collaborative-markdown',
    title: 'Markdown编辑器',
    titleKey: 'nav.markdown',
    path: '/collaborative/markdown',
    icon: 'EditPen',
    category: 'collaborative',
    categoryKey: 'search.category.collaborative',
    keywords: ['markdown', 'md', '编辑器', 'editor', '文档', '协同'],
    description: '多人协同Markdown编辑',
    descriptionKey: 'search.desc.markdown'
  },
  {
    id: 'collaborative-code',
    title: '代码编辑器',
    titleKey: 'nav.code',
    path: '/collaborative/code',
    icon: 'EditPen',
    category: 'collaborative',
    categoryKey: 'search.category.collaborative',
    keywords: ['代码', 'code', '编辑器', 'editor', '编程', '协同'],
    description: '多人协同代码编辑',
    descriptionKey: 'search.desc.code'
  },
  {
    id: 'collaborative-table',
    title: '表格编辑器',
    titleKey: 'nav.table',
    path: '/collaborative/table',
    icon: 'EditPen',
    category: 'collaborative',
    categoryKey: 'search.category.collaborative',
    keywords: ['表格', 'table', '编辑器', 'editor', '数据', '协同'],
    description: '多人协同表格编辑',
    descriptionKey: 'search.desc.table'
  },
  {
    id: 'collaborative-demo',
    title: '协同编辑演示',
    titleKey: 'nav.collaborativeDemo',
    path: '/collaborative/demo',
    icon: 'EditPen',
    category: 'collaborative',
    categoryKey: 'search.category.collaborative',
    keywords: ['演示', 'demo', '协同', 'collaborative', '示例'],
    description: '协同编辑功能演示',
    descriptionKey: 'search.desc.collaborativeDemo'
  },

  // 任务管理
  {
    id: 'tasks',
    title: '任务管理',
    titleKey: 'nav.tasks',
    path: '/tasks',
    icon: 'Notebook',
    category: 'tasks',
    categoryKey: 'search.category.tasks',
    keywords: ['任务', 'task', '管理', 'management', '项目'],
    description: '查看和管理个人任务',
    descriptionKey: 'search.desc.tasks'
  },
  {
    id: 'task-create',
    title: '创建任务',
    titleKey: 'nav.createTask',
    path: '/tasks/create',
    icon: 'Notebook',
    category: 'tasks',
    categoryKey: 'search.category.tasks',
    keywords: ['创建', 'create', '任务', 'task', '新建'],
    description: '创建新的任务',
    descriptionKey: 'search.desc.createTask'
  },
  {
    id: 'task-statistics',
    title: '任务统计',
    titleKey: 'nav.taskStatistics',
    path: '/tasks/statistics',
    icon: 'DataAnalysis',
    category: 'tasks',
    categoryKey: 'search.category.tasks',
    keywords: ['任务', 'task', '统计', 'statistics', '数据', 'data', '分析'],
    description: '查看个人任务统计数据',
    descriptionKey: 'search.desc.taskStatistics'
  },

  // 日程管理
  {
    id: 'schedule',
    title: '日程管理',
    titleKey: 'nav.schedule',
    path: '/schedule',
    icon: 'Calendar',
    category: 'schedule',
    categoryKey: 'search.category.schedule',
    keywords: ['日程', 'schedule', '日历', 'calendar', '安排', '计划'],
    description: '管理个人日程安排',
    descriptionKey: 'search.desc.schedule'
  },

  // 公告通知
  {
    id: 'notifications',
    title: '公告通知',
    titleKey: 'nav.notifications',
    path: '/notifications',
    icon: 'Bell',
    category: 'notifications',
    categoryKey: 'search.category.notifications',
    keywords: ['公告', 'announcement', '通知', 'notification', '消息'],
    description: '查看系统公告和通知',
    descriptionKey: 'search.desc.notifications'
  },

  // 个人信息
  {
    id: 'profile',
    title: '个人信息',
    titleKey: 'nav.profile',
    path: '/profile',
    icon: 'User',
    category: 'profile',
    categoryKey: 'search.category.profile',
    keywords: ['个人', 'profile', '信息', 'info', '设置', '资料'],
    description: '查看和编辑个人信息',
    descriptionKey: 'search.desc.profile'
  }
]

// 搜索分类配置
export const employeeSearchCategories = [
  { key: 'dashboard', label: '工作台', labelKey: 'search.category.dashboard', icon: 'Odometer' },
  { key: 'attendance', label: '考勤管理', labelKey: 'search.category.attendance', icon: 'Clock' },
  { key: 'leave', label: '请假管理', labelKey: 'search.category.leave', icon: 'Calendar' },
  { key: 'collaborative', label: '协同编辑', labelKey: 'search.category.collaborative', icon: 'EditPen' },
  { key: 'tasks', label: '任务管理', labelKey: 'search.category.tasks', icon: 'Notebook' },
  { key: 'schedule', label: '日程管理', labelKey: 'search.category.schedule', icon: 'Calendar' },
  { key: 'notifications', label: '公告通知', labelKey: 'search.category.notifications', icon: 'Bell' },
  { key: 'profile', label: '个人信息', labelKey: 'search.category.profile', icon: 'User' }
]
