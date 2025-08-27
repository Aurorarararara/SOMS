/**
 * 防抖工具函数
 * @param {Function} func 需要防抖的函数
 * @param {number} wait 等待时间（毫秒）
 * @param {boolean} immediate 是否立即执行
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait = 300, immediate = false) {
  let timeout
  
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func.apply(this, args)
    }
    
    const callNow = immediate && !timeout
    
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    
    if (callNow) func.apply(this, args)
  }
}

/**
 * 节流工具函数
 * @param {Function} func 需要节流的函数
 * @param {number} limit 限制时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit = 100) {
  let inThrottle
  
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 防抖装饰器 - 用于Vue3组合式API
 * @param {Function} fn 原函数
 * @param {number} delay 延迟时间
 * @returns {Function} 防抖后的函数
 */
export function useDebounce(fn, delay = 300) {
  let timer = null
  
  return (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(null, args)
    }, delay)
  }
}

/**
 * 节流装饰器 - 用于Vue3组合式API
 * @param {Function} fn 原函数
 * @param {number} delay 延迟时间
 * @returns {Function} 节流后的函数
 */
export function useThrottle(fn, delay = 100) {
  let timer = null
  let lastExec = 0
  
  return (...args) => {
    const now = Date.now()
    
    if (now - lastExec > delay) {
      fn.apply(null, args)
      lastExec = now
    } else {
      clearTimeout(timer)
      timer = setTimeout(() => {
        fn.apply(null, args)
        lastExec = Date.now()
      }, delay - (now - lastExec))
    }
  }
}

/**
 * 请求防重复提交
 * @param {Function} fn 请求函数
 * @param {number} delay 防重复时间
 * @returns {Function} 防重复提交的函数
 */
export function preventResubmit(fn, delay = 1000) {
  let isSubmitting = false
  
  return async (...args) => {
    if (isSubmitting) {
      console.warn('请求正在进行中，请勿重复提交')
      return
    }
    
    isSubmitting = true
    
    try {
      const result = await fn.apply(null, args)
      return result
    } finally {
      setTimeout(() => {
        isSubmitting = false
      }, delay)
    }
  }
}