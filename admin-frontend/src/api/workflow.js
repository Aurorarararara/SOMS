import request from '@/utils/request'

/**
 * 审批流程模板管理API
 */
export const workflowTemplateApi = {
  /**
   * 获取流程模板列表
   */
  getTemplateList(params) {
    return request({
      url: '/admin/workflow/templates',
      method: 'get',
      params
    })
  },

  /**
   * 获取模板详情
   */
  getTemplateDetail(templateId) {
    return request({
      url: `/admin/workflow/templates/${templateId}`,
      method: 'get'
    })
  },

  /**
   * 创建流程模板
   */
  createTemplate(data) {
    return request({
      url: '/admin/workflow/templates',
      method: 'post',
      data
    })
  },

  /**
   * 更新流程模板
   */
  updateTemplate(templateId, data) {
    return request({
      url: `/admin/workflow/templates/${templateId}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除流程模板
   */
  deleteTemplate(templateId) {
    return request({
      url: `/admin/workflow/templates/${templateId}`,
      method: 'delete'
    })
  },

  /**
   * 启用/禁用模板
   */
  toggleTemplateStatus(templateId, isActive) {
    return request({
      url: `/admin/workflow/templates/${templateId}/toggle-status`,
      method: 'put',
      params: { isActive }
    })
  },

  /**
   * 设置默认模板
   */
  setDefaultTemplate(templateId, businessType) {
    return request({
      url: `/admin/workflow/templates/${templateId}/set-default`,
      method: 'put',
      params: { businessType }
    })
  },

  /**
   * 复制模板
   */
  copyTemplate(templateId, newName) {
    return request({
      url: `/admin/workflow/templates/${templateId}/copy`,
      method: 'post',
      params: { newName }
    })
  },

  /**
   * 获取可用模板
   */
  getAvailableTemplates(businessType) {
    return request({
      url: '/admin/workflow/templates/available',
      method: 'get',
      params: { businessType }
    })
  },

  /**
   * 获取默认模板
   */
  getDefaultTemplate(businessType) {
    return request({
      url: '/admin/workflow/templates/default',
      method: 'get',
      params: { businessType }
    })
  }
}

/**
 * 审批流程引擎API
 */
export const workflowEngineApi = {
  /**
   * 启动审批流程
   */
  startWorkflow(data) {
    return request({
      url: '/admin/workflow/start',
      method: 'post',
      data
    })
  },

  /**
   * 处理审批任务
   */
  processTask(data) {
    return request({
      url: '/admin/workflow/tasks/process',
      method: 'post',
      data
    })
  },

  /**
   * 委托任务
   */
  delegateTask(taskId, delegateToId, reason) {
    return request({
      url: `/admin/workflow/tasks/${taskId}/delegate`,
      method: 'post',
      params: { delegateToId, reason }
    })
  },

  /**
   * 取消流程
   */
  cancelWorkflow(instanceId, reason) {
    return request({
      url: `/admin/workflow/instances/${instanceId}/cancel`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 获取待处理任务
   */
  getPendingTasks(userId) {
    return request({
      url: '/admin/workflow/tasks/pending',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 获取已处理任务
   */
  getProcessedTasks(userId) {
    return request({
      url: '/admin/workflow/tasks/processed',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 获取流程实例详情
   */
  getWorkflowDetail(instanceId) {
    return request({
      url: `/admin/workflow/instances/${instanceId}`,
      method: 'get'
    })
  },

  /**
   * 根据业务键查询流程实例
   */
  getWorkflowByBusinessKey(businessKey, businessType) {
    return request({
      url: '/admin/workflow/instances/by-business-key',
      method: 'get',
      params: { businessKey, businessType }
    })
  },

  /**
   * 获取流程实例列表
   */
  getInstanceList(params) {
    return request({
      url: '/admin/workflow/instances',
      method: 'get',
      params
    })
  },

  /**
   * 检查任务权限
   */
  checkTaskPermission(taskId, userId) {
    return request({
      url: `/admin/workflow/tasks/${taskId}/check-permission`,
      method: 'get',
      params: { userId }
    })
  }
}

/**
 * 审批流程实例执行API
 */
export const workflowInstanceApi = {
  /**
   * 启动审批流程
   */
  startWorkflow(data) {
    return request({
      url: '/workflow/instances/start',
      method: 'post',
      data
    })
  },

  /**
   * 处理审批任务
   */
  processTask(taskId, data) {
    return request({
      url: `/workflow/instances/tasks/${taskId}/process`,
      method: 'post',
      data
    })
  },

  /**
   * 获取我发起的流程实例
   */
  getMyInitiatedInstances(userId) {
    return request({
      url: '/workflow/instances/my-initiated',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 获取我的待处理任务
   */
  getMyPendingTasks(userId) {
    return request({
      url: '/workflow/instances/my-tasks',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 获取流程历史记录
   */
  getInstanceHistory(instanceId) {
    return request({
      url: `/workflow/instances/${instanceId}/history`,
      method: 'get'
    })
  },

  /**
   * 撤回流程实例
   */
  withdrawInstance(instanceId, data) {
    return request({
      url: `/workflow/instances/${instanceId}/withdraw`,
      method: 'post',
      data
    })
  },

  /**
   * 获取流程实例详情
   */
  getInstanceDetail(instanceId) {
    return request({
      url: `/workflow/instances/${instanceId}`,
      method: 'get'
    })
  }
}

// 导出所有API函数 - 模板管理
export const {
  getTemplateList,
  getTemplateDetail,
  createTemplate: createWorkflowTemplate,
  updateTemplate: updateWorkflowTemplate,
  deleteTemplate: deleteWorkflowTemplate,
  toggleTemplateStatus,
  setDefaultTemplate,
  copyTemplate,
  getAvailableTemplates,
  getDefaultTemplate
} = workflowTemplateApi

// 导出所有API函数 - 流程实例执行
export const {
  startWorkflow,
  processTask,
  getMyInitiatedInstances,
  getMyPendingTasks,
  getInstanceHistory,
  withdrawInstance,
  getInstanceDetail
} = workflowInstanceApi