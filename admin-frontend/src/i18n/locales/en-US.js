export default {
  // Common
  common: {
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    reset: 'Reset',
    submit: 'Submit',
    back: 'Back',
    next: 'Next',
    previous: 'Previous',
    loading: 'Loading...',
    noData: 'No Data',
    success: 'Success',
    error: 'Error',
    warning: 'Warning',
    info: 'Info',
    yes: 'Yes',
    no: 'No',
    close: 'Close',
    refresh: 'Refresh',
    export: 'Export',
    import: 'Import',
    clear: 'Clear',
    manage: 'Manage',
    view: 'View',
    download: 'Download',
    upload: 'Upload'
  },

  // Layout
  layout: {
    refreshPage: 'Refresh Page',
    toggleFullscreen: 'Toggle Fullscreen'
  },

  // Navigation Menu
  nav: {
    dashboard: 'Dashboard',
    employees: 'Employee Management',
    departments: 'Department Management',
    roles: 'Role Management',
    organizationChart: 'Organization Chart',
    attendanceRecords: 'Attendance Records',
    attendanceRules: 'Attendance Rules',
    attendanceStatistics: 'Attendance Statistics',
    leaveApplications: 'Leave Applications',
    leaveApproval: 'Leave Approval',
    leaveStatistics: 'Leave Statistics',
    announcements: 'Announcement Management',
    workflow: 'Workflow Management',
    myTasks: 'My Tasks',
    myInitiated: 'My Initiated',
    taskManagement: 'Task Management',
    taskList: 'Task List',
    createTask: 'Create Task',
    taskStatistics: 'Task Statistics',
    notificationManagement: 'Notification Management',
    notificationList: 'Notification List',
    sendNotification: 'Send Notification',
    notificationTemplates: 'Notification Templates',
    attendanceReports: 'Attendance Reports',
    leaveReports: 'Leave Reports',
    performanceReports: 'Performance Reports',
    systemConfig: 'System Configuration',
    operationLogs: 'Operation Logs',
    dataBackup: 'Data Backup',
    profile: 'Profile',
    adminBackend: 'Admin Backend'
  },

  // Breadcrumb Navigation
  breadcrumb: {
    home: 'Admin Dashboard',
    recentVisits: 'Recent Visits',
    favorites: 'My Favorites',
    addToFavorites: 'Add to Favorites',
    removeFromFavorites: 'Remove from Favorites',
    visitHistory: 'Visit History',
    clearHistory: 'Clear History',
    clearFavorites: 'Clear Favorites',
    noHistory: 'No visit history',
    noFavorites: 'No favorite pages',
    favoriteAdded: 'Added to favorites',
    favoriteRemoved: 'Removed from favorites',
    favoriteExists: 'Page already in favorites',
    historyCleared: 'Visit history cleared',
    favoritesCleared: 'Favorites cleared',
    justNow: 'Just now',
    minutesAgo: 'minutes ago',
    hoursAgo: 'hours ago',
    daysAgo: 'days ago',
    // Task management breadcrumbs
    taskManagement: 'Task Management',
    taskList: 'Task List',
    createTask: 'Create Task',
    taskDetail: 'Task Detail',
    taskStatistics: 'Task Statistics',
    // Notification management breadcrumbs
    notificationManagement: 'Notification Management',
    notificationList: 'Notification List',
    sendNotification: 'Send Notification',
    notificationTemplates: 'Notification Templates'
  },

  // User Related
  user: {
    login: 'Login',
    logout: 'Logout',
    username: 'Username',
    password: 'Password',
    rememberMe: 'Remember Me',
    forgotPassword: 'Forgot Password',
    profile: 'Profile',
    settings: 'Settings',
    changePassword: 'Change Password',
    personalInfo: 'Personal Information',
    avatar: 'Avatar',
    nickname: 'Nickname',
    email: 'Email',
    phone: 'Phone',
    department: 'Department',
    position: 'Position',
    joinDate: 'Join Date',
    status: 'Status',
    active: 'Active',
    inactive: 'Inactive',
    permissions: 'Permissions'
  },

  // Dashboard
  dashboard: {
    totalEmployees: 'Total Employees',
    todayAttendance: 'Today\'s Attendance',
    pendingLeave: 'Pending Leave',
    totalDepartments: 'Total Departments',
    attendanceStats: 'Attendance Statistics',
    departmentDistribution: 'Department Personnel Distribution',
    selectMonth: 'Select Month',
    quickActions: 'Quick Actions',
    latestAnnouncements: 'Latest Announcements',
    viewMore: 'View More',
    pendingTasks: 'Pending Tasks',
    systemMessages: 'System Messages',
    noPendingTasks: 'No pending tasks',
    noSystemMessages: 'No system messages'
  },

  // Employee Management
  employees: {
    addEmployee: 'Add Employee',
    employeeName: 'Employee Name',
    enterEmployeeName: 'Enter employee name',
    department: 'Department',
    selectDepartment: 'Select department',
    employeeStatus: 'Employee Status',
    selectStatus: 'Select status',
    active: 'Active',
    inactive: 'Inactive',
    employeeId: 'Employee ID',
    employeeCode: 'Employee Code',
    name: 'Name',
    email: 'Email',
    phone: 'Phone',
    position: 'Position',
    status: 'Status'
  },

  // Department Management
  departments: {
    addDepartment: 'Add Department',
    editDepartment: 'Edit Department',
    departmentId: 'Department ID',
    departmentName: 'Department Name',
    description: 'Description',
    manager: 'Manager',
    employeeCount: 'Employee Count',
    status: 'Status',
    createTime: 'Create Time',
    normal: 'Normal',
    disabled: 'Disabled',
    enterDepartmentName: 'Enter department name'
  },

  // Role Management
  roles: {
    addRole: 'Add Role',
    editRole: 'Edit Role',
    roleId: 'Role ID',
    roleName: 'Role Name',
    roleCode: 'Role Code',
    description: 'Description',
    status: 'Status',
    createTime: 'Create Time',
    enabled: 'Enabled',
    disabled: 'Disabled',
    permissions: 'Permissions',
    enterRoleName: 'Enter role name',
    enterRoleCode: 'Enter role code'
  },

  // Attendance Rules
  attendanceRules: {
    addRule: 'Add Rule',
    editRule: 'Edit Rule',
    ruleId: 'Rule ID',
    ruleName: 'Rule Name',
    workStartTime: 'Work Start Time',
    workEndTime: 'Work End Time',
    lateThreshold: 'Late Threshold',
    earlyThreshold: 'Early Threshold',
    applicableDepartments: 'Applicable Departments',
    status: 'Status',
    enabled: 'Enabled',
    disabled: 'Disabled',
    enterRuleName: 'Enter rule name'
  },

  // Attendance Statistics
  attendanceStatistics: {
    exportStatistics: 'Export Statistics',
    normalAttendance: 'Normal Attendance',
    lateCount: 'Late Count',
    absentCount: 'Absent Count',
    leaveCount: 'Leave Count',
    statisticsRange: 'Statistics Range'
  },

  // Leave Applications
  leaveApplications: {
    exportApplications: 'Export Applications',
    applicant: 'Applicant',
    enterApplicantName: 'Enter applicant name',
    leaveType: 'Leave Type',
    selectLeaveType: 'Select leave type',
    sickLeave: 'Sick Leave',
    personalLeave: 'Personal Leave',
    annualLeave: 'Annual Leave',
    marriageLeave: 'Marriage Leave',
    maternityLeave: 'Maternity Leave',
    bereavementLeave: 'Bereavement Leave',
    applicationStatus: 'Application Status',
    selectStatus: 'Select status',
    pending: 'Pending',
    approved: 'Approved',
    rejected: 'Rejected',
    cancelled: 'Cancelled',
    applicationDate: 'Application Date',
    startDate: 'Start Date',
    endDate: 'End Date',
    applicationId: 'Application ID',
    employeeCode: 'Employee Code',
    department: 'Department',
    leaveDays: 'Leave Days',
    status: 'Status',
    applicationTime: 'Application Time',
    reason: 'Reason',
    viewDetails: 'View Details',
    workflowProgress: 'Workflow Progress'
  },

  // Leave Approval
  leaveApproval: {
    showAll: 'Show All',
    pending: 'Pending',
    approved: 'Approved',
    rejected: 'Rejected',
    totalApplications: 'Total Applications',
    applicationId: 'Application ID',
    applicant: 'Applicant',
    department: 'Department',
    leaveType: 'Leave Type',
    leaveDuration: 'Leave Duration',
    days: 'Days',
    priority: 'Priority',
    applicationTime: 'Application Time',
    status: 'Status',
    details: 'Details',
    approve: 'Approve',
    reject: 'Reject'
  },

  // Leave Statistics
  leaveStatistics: {
    exportStatistics: 'Export Statistics',
    statisticsDetails: 'Leave Statistics Details',
    statisticsPeriod: 'Statistics Period',
    startMonth: 'Start Month',
    endMonth: 'End Month',
    employeeName: 'Employee Name',
    department: 'Department',
    sickLeaveDays: 'Sick Leave (Days)',
    personalLeaveDays: 'Personal Leave (Days)',
    annualLeaveDays: 'Annual Leave (Days)',
    totalLeaveDays: 'Total Leave (Days)',
    leaveRate: 'Leave Rate',
    viewDetails: 'View Details',
    totalEmployees: 'Total Employees',
    monthlyLeaveCount: 'Monthly Leave Count',
    averageLeaveRate: 'Average Leave Rate'
  },

  // Announcements Management
  announcementsManage: {
    publishAnnouncement: 'Publish Announcement',
    announcementTitle: 'Announcement Title',
    enterTitle: 'Enter announcement title',
    announcementType: 'Announcement Type',
    selectType: 'Select type',
    notice: 'Notice',
    news: 'News',
    policy: 'Policy',
    other: 'Other',
    publishStatus: 'Publish Status',
    selectStatus: 'Select status',
    published: 'Published',
    unpublished: 'Unpublished',
    contentSummary: 'Content Summary',
    publisher: 'Publisher',
    publishTime: 'Publish Time',
    readCount: 'Read Count',
    status: 'Status'
  },

  // System Configuration
  systemConfig: {
    saveConfig: 'Save Configuration',
    basicSettings: 'Basic Settings',
    systemName: 'System Name',
    systemVersion: 'System Version',
    adminEmail: 'Admin Email',
    attendanceSettings: 'Attendance Settings',
    standardWorkHours: 'Standard Work Hours',
    hoursPerDay: 'hours/day',
    lateThreshold: 'Late Threshold',
    minutes: 'minutes'
  },

  // System Logs
  systemLogs: {
    exportLogs: 'Export Logs',
    logId: 'Log ID',
    operationUser: 'Operation User',
    operationType: 'Operation Type',
    operationDescription: 'Operation Description',
    ipAddress: 'IP Address',
    operationTime: 'Operation Time'
  },

  // System Backup
  systemBackup: {
    createBackup: 'Create Backup',
    backupId: 'Backup ID',
    backupName: 'Backup Name',
    fileSize: 'File Size',
    createTime: 'Create Time',
    backupDescription: 'Backup Description',
    download: 'Download',
    restore: 'Restore'
  },

  // Attendance Reports
  reportsAttendance: {
    attendanceReport: 'Attendance Report',
    generateReport: 'Generate Report',
    reportType: 'Report Type',
    monthlyReport: 'Monthly Report',
    quarterlyReport: 'Quarterly Report',
    yearlyReport: 'Yearly Report',
    statisticsRange: 'Statistics Range',
    startMonth: 'Start Month',
    endMonth: 'End Month',
    queryReport: 'Query Report',
    chartPlaceholder: 'Attendance trend chart display area',
    reportLoadingInProgress: 'Report loading function in development...',
    reportGenerationInProgress: 'Report generation function in development...'
  },

  // Leave Reports
  reportsLeave: {
    leaveReport: 'Leave Report',
    exportReport: 'Export Report',
    chartPlaceholder: 'Leave statistics chart display area'
  },

  // Performance Reports
  reportsPerformance: {
    performanceReport: 'Performance Report',
    generatePerformanceReport: 'Generate Performance Report',
    chartPlaceholder: 'Performance analysis chart display area'
  },

  // Login Page
  login: {
    systemTitle: 'Smart Office Management System',
    adminPlatform: 'Admin Platform',
    adminLogin: 'Administrator Login',
    adminLoginPrompt: 'Login with administrator account',
    adminAccount: 'Admin Account',
    enterCaptcha: 'Enter Captcha',
    clickRefresh: 'Click to Refresh',
    loggingIn: 'Logging in...',
    loginSuccess: 'Login successful!',
    loginFailed: 'Login failed, please check account and password',
    captchaError: 'Captcha error'
  },

  // Employee Management
  employee: {
    list: 'Employee List',
    add: 'Add Employee',
    edit: 'Edit Employee',
    detail: 'Employee Detail',
    employeeId: 'Employee ID',
    name: 'Name',
    gender: 'Gender',
    male: 'Male',
    female: 'Female',
    age: 'Age',
    idCard: 'ID Card',
    address: 'Address',
    emergencyContact: 'Emergency Contact',
    emergencyPhone: 'Emergency Phone',
    hireDate: 'Hire Date',
    probationEndDate: 'Probation End Date',
    contractType: 'Contract Type',
    salary: 'Salary',
    bankAccount: 'Bank Account',
    socialSecurity: 'Social Security',
    workLocation: 'Work Location'
  },

  // Department Management
  department: {
    list: 'Department List',
    add: 'Add Department',
    edit: 'Edit Department',
    detail: 'Department Detail',
    name: 'Department Name',
    code: 'Department Code',
    manager: 'Department Manager',
    parentDepartment: 'Parent Department',
    description: 'Description',
    employeeCount: 'Employee Count',
    subDepartments: 'Sub Departments',
    organizationChart: 'Organization Chart'
  },

  // Role Management
  role: {
    list: 'Role List',
    add: 'Add Role',
    edit: 'Edit Role',
    detail: 'Role Detail',
    name: 'Role Name',
    code: 'Role Code',
    description: 'Description',
    permissions: 'Permissions',
    users: 'Associated Users',
    systemRole: 'System Role',
    customRole: 'Custom Role'
  },

  // Attendance Management
  attendance: {
    records: 'Attendance Records',
    rules: 'Attendance Rules',
    statistics: 'Attendance Statistics',
    clockIn: 'Clock In',
    clockOut: 'Clock Out',
    status: 'Status',
    time: 'Time',
    location: 'Location',
    workDays: 'Work Days',
    lateDays: 'Late Days',
    earlyLeaveDays: 'Early Leave Days',
    absentDays: 'Absent Days',
    overtimeHours: 'Overtime Hours',
    clockInTime: 'Clock In Time',
    clockOutTime: 'Clock Out Time',
    workHours: 'Work Hours',
    normal: 'Normal',
    late: 'Late',
    earlyLeave: 'Early Leave',
    absent: 'Absent',
    overtime: 'Overtime',
    workingDays: 'Working Days',
    restDays: 'Rest Days',
    holidays: 'Holidays'
  },

  // Leave Management
  leave: {
    applications: 'Leave Applications',
    approval: 'Leave Approval',
    statistics: 'Leave Statistics',
    type: 'Leave Type',
    startDate: 'Start Date',
    endDate: 'End Date',
    days: 'Leave Days',
    reason: 'Leave Reason',
    status: 'Status',
    pending: 'Pending',
    approved: 'Approved',
    rejected: 'Rejected',
    cancelled: 'Cancelled',
    applicant: 'Applicant',
    approver: 'Approver',
    approvalTime: 'Approval Time',
    approvalComments: 'Approval Comments',
    annualLeave: 'Annual Leave',
    sickLeave: 'Sick Leave',
    personalLeave: 'Personal Leave',
    maternityLeave: 'Maternity Leave',
    paternityLeave: 'Paternity Leave',
    marriageLeave: 'Marriage Leave',
    bereavementLeave: 'Bereavement Leave'
  },

  // Announcement Management
  announcement: {
    list: 'Announcement List',
    add: 'Publish Announcement',
    edit: 'Edit Announcement',
    detail: 'Announcement Detail',
    title: 'Title',
    content: 'Content',
    publishTime: 'Publish Time',
    publisher: 'Publisher',
    status: 'Status',
    draft: 'Draft',
    published: 'Published',
    archived: 'Archived',
    priority: 'Priority',
    high: 'High',
    medium: 'Medium',
    low: 'Low',
    targetAudience: 'Target Audience',
    allEmployees: 'All Employees',
    specificDepartments: 'Specific Departments',
    specificEmployees: 'Specific Employees'
  },

  // Workflow
  workflow: {
    management: 'Workflow Management',
    myTasks: 'My Tasks',
    myInitiated: 'My Initiated',
    processName: 'Process Name',
    processType: 'Process Type',
    initiator: 'Initiator',
    currentStep: 'Current Step',
    status: 'Status',
    createTime: 'Create Time',
    completeTime: 'Complete Time',
    approve: 'Approve',
    reject: 'Reject',
    transfer: 'Transfer',
    comments: 'Comments',
    history: 'Process History',
    pending: 'Pending',
    processing: 'Processing',
    completed: 'Completed',
    terminated: 'Terminated'
  },

  // Reports
  report: {
    attendance: 'Attendance Report',
    leave: 'Leave Report',
    performance: 'Performance Report',
    dateRange: 'Date Range',
    department: 'Department',
    employee: 'Employee',
    generateReport: 'Generate Report',
    exportReport: 'Export Report',
    chartView: 'Chart View',
    tableView: 'Table View',
    summary: 'Summary',
    detail: 'Detail'
  },

  // System Settings
  system: {
    config: 'System Configuration',
    logs: 'Operation Logs',
    backup: 'Data Backup',
    basicSettings: 'Basic Settings',
    securitySettings: 'Security Settings',
    emailSettings: 'Email Settings',
    smsSettings: 'SMS Settings',
    backupNow: 'Backup Now',
    restoreData: 'Restore Data',
    logLevel: 'Log Level',
    logRetention: 'Log Retention',
    operation: 'Operation',
    operator: 'Operator',
    operationTime: 'Operation Time',
    ipAddress: 'IP Address',
    result: 'Result'
  },

  // Language Switch
  language: {
    switch: 'Switch Language',
    chinese: '中文',
    english: 'English',
    current: 'Current Language'
  },

  // Form Validation
  validation: {
    required: 'This field is required',
    email: 'Please enter a valid email address',
    phone: 'Please enter a valid phone number',
    password: 'Password must be at least 6 characters',
    confirmPassword: 'Passwords do not match',
    minLength: 'Length cannot be less than {min} characters',
    maxLength: 'Length cannot exceed {max} characters',
    numeric: 'Please enter a number',
    positive: 'Please enter a positive number',
    integer: 'Please enter an integer',
    idCard: 'Please enter a valid ID card number',
    bankAccount: 'Please enter a valid bank account'
  },

  // Operation Messages
  message: {
    saveSuccess: 'Save successful',
    saveFailed: 'Save failed',
    deleteSuccess: 'Delete successful',
    deleteFailed: 'Delete failed',
    updateSuccess: 'Update successful',
    updateFailed: 'Update failed',
    createSuccess: 'Create successful',
    createFailed: 'Create failed',
    loginSuccess: 'Login successful',
    loginFailed: 'Login failed',
    logoutSuccess: 'Logout successful',
    networkError: 'Network error, please try again later',
    serverError: 'Server error',
    permissionDenied: 'Permission denied',
    operationSuccess: 'Operation successful',
    operationFailed: 'Operation failed',
    confirmDelete: 'Are you sure you want to delete?',
    confirmLogout: 'Are you sure you want to logout?',
    unsavedChanges: 'You have unsaved changes, are you sure you want to leave?',
    approveSuccess: 'Approval successful',
    rejectSuccess: 'Rejection successful',
    transferSuccess: 'Transfer successful',
    backupSuccess: 'Backup successful',
    restoreSuccess: 'Restore successful'
  },

  // Time Related
  time: {
    today: 'Today',
    yesterday: 'Yesterday',
    tomorrow: 'Tomorrow',
    thisWeek: 'This Week',
    lastWeek: 'Last Week',
    nextWeek: 'Next Week',
    thisMonth: 'This Month',
    lastMonth: 'Last Month',
    nextMonth: 'Next Month',
    thisYear: 'This Year',
    lastYear: 'Last Year',
    nextYear: 'Next Year',
    morning: 'Morning',
    afternoon: 'Afternoon',
    evening: 'Evening',
    night: 'Night'
  }
}
