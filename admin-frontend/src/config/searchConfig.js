// 管理端搜索配置
export const adminSearchConfig = [
  // 仪表盘
  {
    id: 'dashboard',
    title: '综合仪表盘',
    titleKey: 'nav.dashboard',
    path: '/admin/dashboard',
    icon: 'DataBoard',
    category: 'dashboard',
    categoryKey: 'search.category.dashboard',
    keywords: ['仪表盘', 'dashboard', 'overview', '概览', '统计'],
    description: '查看系统整体数据统计和概览信息',
    descriptionKey: 'search.desc.dashboard'
  },
  
  // 用户管理
  {
    id: 'employees',
    title: '员工管理',
    titleKey: 'nav.employees',
    path: '/admin/employees',
    icon: 'User',
    category: 'user',
    categoryKey: 'search.category.user',
    keywords: ['员工', 'employee', 'user', '用户', '人员', '管理'],
    description: '管理员工信息、添加删除员工、编辑员工资料',
    descriptionKey: 'search.desc.employees'
  },
  {
    id: 'departments',
    title: '部门管理',
    titleKey: 'nav.departments',
    path: '/admin/departments',
    icon: 'OfficeBuilding',
    category: 'user',
    categoryKey: 'search.category.user',
    keywords: ['部门', 'department', '组织', 'organization', '管理'],
    description: '管理公司部门结构、添加删除部门',
    descriptionKey: 'search.desc.departments'
  },
  {
    id: 'roles',
    title: '角色管理',
    titleKey: 'nav.roles',
    path: '/admin/roles',
    icon: 'UserFilled',
    category: 'user',
    categoryKey: 'search.category.user',
    keywords: ['角色', 'role', '权限', 'permission', '管理'],
    description: '管理用户角色和权限配置',
    descriptionKey: 'search.desc.roles'
  },
  {
    id: 'organization-chart',
    title: '组织架构图',
    titleKey: 'nav.organizationChart',
    path: '/admin/organization/chart',
    icon: 'OfficeBuilding',
    category: 'user',
    categoryKey: 'search.category.user',
    keywords: ['组织', 'organization', '架构', 'chart', '图表'],
    description: '查看公司组织架构图',
    descriptionKey: 'search.desc.organizationChart'
  },

  // 考勤管理
  {
    id: 'attendance-records',
    title: '考勤记录',
    titleKey: 'nav.attendanceRecords',
    path: '/admin/attendance/records',
    icon: 'Clock',
    category: 'attendance',
    categoryKey: 'search.category.attendance',
    keywords: ['考勤', 'attendance', '记录', 'record', '打卡'],
    description: '查看和管理员工考勤记录',
    descriptionKey: 'search.desc.attendanceRecords'
  },
  {
    id: 'attendance-rules',
    title: '考勤规则',
    titleKey: 'nav.attendanceRules',
    path: '/admin/attendance/rules',
    icon: 'Clock',
    category: 'attendance',
    categoryKey: 'search.category.attendance',
    keywords: ['考勤', 'attendance', '规则', 'rule', '设置'],
    description: '配置考勤规则和时间设置',
    descriptionKey: 'search.desc.attendanceRules'
  },
  {
    id: 'attendance-statistics',
    title: '考勤统计',
    titleKey: 'nav.attendanceStatistics',
    path: '/admin/attendance/statistics',
    icon: 'Clock',
    category: 'attendance',
    categoryKey: 'search.category.attendance',
    keywords: ['考勤', 'attendance', '统计', 'statistics', '报表'],
    description: '查看考勤统计数据和分析',
    descriptionKey: 'search.desc.attendanceStatistics'
  },

  // 请假管理
  {
    id: 'leave-applications',
    title: '请假申请',
    titleKey: 'nav.leaveApplications',
    path: '/admin/leave/applications',
    icon: 'DocumentCopy',
    category: 'leave',
    categoryKey: 'search.category.leave',
    keywords: ['请假', 'leave', '申请', 'application'],
    description: '查看和处理员工请假申请',
    descriptionKey: 'search.desc.leaveApplications'
  },
  {
    id: 'leave-approval',
    title: '审批管理',
    titleKey: 'nav.leaveApproval',
    path: '/admin/leave/approval',
    icon: 'DocumentCopy',
    category: 'leave',
    categoryKey: 'search.category.leave',
    keywords: ['审批', 'approval', '请假', 'leave', '管理'],
    description: '处理请假审批流程',
    descriptionKey: 'search.desc.leaveApproval'
  },
  {
    id: 'leave-statistics',
    title: '请假统计',
    titleKey: 'nav.leaveStatistics',
    path: '/admin/leave/statistics',
    icon: 'DocumentCopy',
    category: 'leave',
    categoryKey: 'search.category.leave',
    keywords: ['请假', 'leave', '统计', 'statistics', '报表'],
    description: '查看请假统计数据',
    descriptionKey: 'search.desc.leaveStatistics'
  },

  // 公告管理
  {
    id: 'announcements',
    title: '公告管理',
    titleKey: 'nav.announcements',
    path: '/admin/announcements',
    icon: 'Bell',
    category: 'content',
    categoryKey: 'search.category.content',
    keywords: ['公告', 'announcement', '通知', 'notice', '管理'],
    description: '发布和管理系统公告通知',
    descriptionKey: 'search.desc.announcements'
  },

  // 审批流程
  {
    id: 'workflow',
    title: '审批流程管理',
    titleKey: 'nav.workflow',
    path: '/admin/workflow',
    icon: 'Connection',
    category: 'workflow',
    categoryKey: 'search.category.workflow',
    keywords: ['审批', 'workflow', '流程', 'process', '管理'],
    description: '配置和管理审批流程',
    descriptionKey: 'search.desc.workflow'
  },
  {
    id: 'my-tasks',
    title: '我的待办任务',
    titleKey: 'nav.myTasks',
    path: '/admin/workflow/my-tasks',
    icon: 'Connection',
    category: 'workflow',
    categoryKey: 'search.category.workflow',
    keywords: ['待办', 'task', '任务', '审批', 'workflow'],
    description: '查看我的待办审批任务',
    descriptionKey: 'search.desc.myTasks'
  },
  {
    id: 'my-initiated',
    title: '我发起的流程',
    titleKey: 'nav.myInitiated',
    path: '/admin/workflow/my-initiated',
    icon: 'Connection',
    category: 'workflow',
    categoryKey: 'search.category.workflow',
    keywords: ['发起', 'initiated', '流程', 'process', '审批'],
    description: '查看我发起的审批流程',
    descriptionKey: 'search.desc.myInitiated'
  },

  // 数据报表
  {
    id: 'reports-attendance',
    title: '考勤报表',
    titleKey: 'nav.attendanceReports',
    path: '/admin/reports/attendance',
    icon: 'DataAnalysis',
    category: 'reports',
    categoryKey: 'search.category.reports',
    keywords: ['报表', 'report', '考勤', 'attendance', '数据'],
    description: '生成考勤数据报表',
    descriptionKey: 'search.desc.reportsAttendance'
  },
  {
    id: 'reports-leave',
    title: '请假报表',
    titleKey: 'nav.leaveReports',
    path: '/admin/reports/leave',
    icon: 'DataAnalysis',
    category: 'reports',
    categoryKey: 'search.category.reports',
    keywords: ['报表', 'report', '请假', 'leave', '数据'],
    description: '生成请假数据报表',
    descriptionKey: 'search.desc.reportsLeave'
  },
  {
    id: 'reports-performance',
    title: '绩效报表',
    titleKey: 'nav.performanceReports',
    path: '/admin/reports/performance',
    icon: 'DataAnalysis',
    category: 'reports',
    categoryKey: 'search.category.reports',
    keywords: ['报表', 'report', '绩效', 'performance', '数据'],
    description: '生成绩效数据报表',
    descriptionKey: 'search.desc.reportsPerformance'
  },

  // 系统设置
  {
    id: 'system-config',
    title: '系统配置',
    titleKey: 'nav.systemConfig',
    path: '/admin/system/config',
    icon: 'Setting',
    category: 'system',
    categoryKey: 'search.category.system',
    keywords: ['系统', 'system', '配置', 'config', '设置'],
    description: '配置系统参数和设置',
    descriptionKey: 'search.desc.systemConfig'
  },
  {
    id: 'system-logs',
    title: '操作日志',
    titleKey: 'nav.operationLogs',
    path: '/admin/system/logs',
    icon: 'Setting',
    category: 'system',
    categoryKey: 'search.category.system',
    keywords: ['日志', 'log', '操作', 'operation', '记录'],
    description: '查看系统操作日志',
    descriptionKey: 'search.desc.systemLogs'
  },
  {
    id: 'system-backup',
    title: '数据备份',
    titleKey: 'nav.dataBackup',
    path: '/admin/system/backup',
    icon: 'Setting',
    category: 'system',
    categoryKey: 'search.category.system',
    keywords: ['备份', 'backup', '数据', 'data', '恢复'],
    description: '数据备份和恢复管理',
    descriptionKey: 'search.desc.systemBackup'
  },

  // 个人信息
  {
    id: 'profile',
    title: '个人信息',
    titleKey: 'nav.profile',
    path: '/admin/profile',
    icon: 'User',
    category: 'profile',
    categoryKey: 'search.category.profile',
    keywords: ['个人', 'profile', '信息', 'info', '设置'],
    description: '查看和编辑个人信息',
    descriptionKey: 'search.desc.profile'
  }
]

// 搜索分类配置
export const adminSearchCategories = [
  { key: 'dashboard', label: '仪表盘', labelKey: 'search.category.dashboard', icon: 'DataBoard' },
  { key: 'user', label: '用户管理', labelKey: 'search.category.user', icon: 'User' },
  { key: 'attendance', label: '考勤管理', labelKey: 'search.category.attendance', icon: 'Clock' },
  { key: 'leave', label: '请假管理', labelKey: 'search.category.leave', icon: 'DocumentCopy' },
  { key: 'content', label: '内容管理', labelKey: 'search.category.content', icon: 'Bell' },
  { key: 'workflow', label: '审批流程', labelKey: 'search.category.workflow', icon: 'Connection' },
  { key: 'reports', label: '数据报表', labelKey: 'search.category.reports', icon: 'DataAnalysis' },
  { key: 'system', label: '系统设置', labelKey: 'search.category.system', icon: 'Setting' },
  { key: 'profile', label: '个人信息', labelKey: 'search.category.profile', icon: 'User' }
]
