/**
 * 协同编辑光标跟踪服务
 * 支持多种编辑器类型的光标位置计算和同步
 */

class CursorTrackingService {
  constructor() {
    this.editorType = null
    this.editorInstance = null
    this.containerElement = null
    this.cursors = new Map() // sessionId -> cursor data
    this.selections = new Map() // sessionId -> selection data
    this.observers = new Map() // 存储各种观察器
  }

  /**
   * 初始化光标跟踪
   * @param {string} editorType - 编辑器类型 (quill, monaco, markdown, table)
   * @param {Object} editorInstance - 编辑器实例
   * @param {Element} containerElement - 容器元素
   */
  initialize(editorType, editorInstance, containerElement) {
    this.editorType = editorType
    this.editorInstance = editorInstance
    this.containerElement = containerElement

    this.setupEditorListeners()
    this.setupResizeObserver()
  }

  /**
   * 设置编辑器事件监听器
   */
  setupEditorListeners() {
    switch (this.editorType) {
      case 'quill':
        this.setupQuillListeners()
        break
      case 'monaco':
        this.setupMonacoListeners()
        break
      case 'markdown':
        this.setupMarkdownListeners()
        break
      case 'table':
        this.setupTableListeners()
        break
    }
  }

  /**
   * Quill 编辑器监听器
   */
  setupQuillListeners() {
    if (!this.editorInstance) return

    // 监听选择变化
    this.editorInstance.on('selection-change', (range, oldRange, source) => {
      if (source === 'user') {
        this.onLocalCursorChange({
          type: 'quill',
          range: range,
          timestamp: Date.now()
        })
      }
    })

    // 监听内容变化
    this.editorInstance.on('text-change', (delta, oldDelta, source) => {
      if (source === 'user') {
        // 内容变化时需要重新计算所有光标位置
        this.recalculateAllCursors()
      }
    })
  }

  /**
   * Monaco 编辑器监听器
   */
  setupMonacoListeners() {
    if (!this.editorInstance) return

    // 监听光标位置变化
    this.editorInstance.onDidChangeCursorPosition((e) => {
      this.onLocalCursorChange({
        type: 'monaco',
        position: e.position,
        timestamp: Date.now()
      })
    })

    // 监听选择变化
    this.editorInstance.onDidChangeCursorSelection((e) => {
      this.onLocalSelectionChange({
        type: 'monaco',
        selection: e.selection,
        timestamp: Date.now()
      })
    })

    // 监听内容变化
    this.editorInstance.onDidChangeModelContent((e) => {
      this.recalculateAllCursors()
    })
  }

  /**
   * Markdown 编辑器监听器
   */
  setupMarkdownListeners() {
    if (!this.containerElement) return

    // 使用 DOM 事件监听
    const textArea = this.containerElement.querySelector('textarea') || this.containerElement

    textArea.addEventListener('selectionchange', () => {
      this.onLocalCursorChange({
        type: 'markdown',
        selectionStart: textArea.selectionStart,
        selectionEnd: textArea.selectionEnd,
        timestamp: Date.now()
      })
    })

    textArea.addEventListener('input', () => {
      this.recalculateAllCursors()
    })
  }

  /**
   * 表格编辑器监听器
   */
  setupTableListeners() {
    if (!this.containerElement) return

    // 监听单元格选择
    this.containerElement.addEventListener('click', (e) => {
      const cell = e.target.closest('td, th')
      if (cell) {
        const cellIndex = this.getCellIndex(cell)
        this.onLocalCursorChange({
          type: 'table',
          cellIndex: cellIndex,
          timestamp: Date.now()
        })
      }
    })
  }

  /**
   * 设置尺寸变化观察器
   */
  setupResizeObserver() {
    if (!this.containerElement) return

    const resizeObserver = new ResizeObserver(() => {
      this.recalculateAllCursors()
    })

    resizeObserver.observe(this.containerElement)
    this.observers.set('resize', resizeObserver)
  }

  /**
   * 本地光标变化回调
   */
  onLocalCursorChange(cursorData) {
    if (this.onCursorChange) {
      this.onCursorChange(cursorData)
    }
  }

  /**
   * 本地选择变化回调
   */
  onLocalSelectionChange(selectionData) {
    if (this.onSelectionChange) {
      this.onSelectionChange(selectionData)
    }
  }

  /**
   * 更新远程用户光标
   * @param {string} sessionId - 会话ID
   * @param {Object} cursorData - 光标数据
   */
  updateRemoteCursor(sessionId, cursorData) {
    this.cursors.set(sessionId, cursorData)
    this.renderCursor(sessionId, cursorData)
  }

  /**
   * 更新远程用户选择
   * @param {string} sessionId - 会话ID
   * @param {Object} selectionData - 选择数据
   */
  updateRemoteSelection(sessionId, selectionData) {
    this.selections.set(sessionId, selectionData)
    this.renderSelection(sessionId, selectionData)
  }

  /**
   * 移除远程用户光标
   * @param {string} sessionId - 会话ID
   */
  removeRemoteCursor(sessionId) {
    this.cursors.delete(sessionId)
    this.removeRenderedCursor(sessionId)
  }

  /**
   * 渲染光标
   * @param {string} sessionId - 会话ID
   * @param {Object} cursorData - 光标数据
   */
  renderCursor(sessionId, cursorData) {
    const position = this.calculateCursorPosition(cursorData)
    if (!position) return

    // 移除旧的光标元素
    this.removeRenderedCursor(sessionId)

    // 创建新的光标元素
    const cursorElement = this.createCursorElement(sessionId, cursorData, position)
    this.appendCursorToContainer(cursorElement)
  }

  /**
   * 渲染选择范围
   * @param {string} sessionId - 会话ID
   * @param {Object} selectionData - 选择数据
   */
  renderSelection(sessionId, selectionData) {
    const positions = this.calculateSelectionPositions(selectionData)
    if (!positions || positions.length === 0) return

    // 移除旧的选择元素
    this.removeRenderedSelection(sessionId)

    // 创建新的选择元素
    positions.forEach((position, index) => {
      const selectionElement = this.createSelectionElement(sessionId, selectionData, position, index)
      this.appendSelectionToContainer(selectionElement)
    })
  }

  /**
   * 计算光标位置
   * @param {Object} cursorData - 光标数据
   * @returns {Object|null} 位置信息
   */
  calculateCursorPosition(cursorData) {
    switch (cursorData.type || this.editorType) {
      case 'quill':
        return this.calculateQuillCursorPosition(cursorData)
      case 'monaco':
        return this.calculateMonacoCursorPosition(cursorData)
      case 'markdown':
        return this.calculateMarkdownCursorPosition(cursorData)
      case 'table':
        return this.calculateTableCursorPosition(cursorData)
      default:
        return null
    }
  }

  /**
   * Quill 光标位置计算
   */
  calculateQuillCursorPosition(cursorData) {
    if (!this.editorInstance || !cursorData.range) return null

    try {
      const bounds = this.editorInstance.getBounds(cursorData.range.index || 0)
      const containerRect = this.containerElement.getBoundingClientRect()

      return {
        left: bounds.left,
        top: bounds.top,
        height: bounds.height || 20
      }
    } catch (error) {
      console.warn('计算 Quill 光标位置失败:', error)
      return null
    }
  }

  /**
   * Monaco 光标位置计算
   */
  calculateMonacoCursorPosition(cursorData) {
    if (!this.editorInstance || !cursorData.position) return null

    try {
      const position = cursorData.position
      const coordinates = this.editorInstance.getScrolledVisiblePosition(position)
      
      if (!coordinates) return null

      return {
        left: coordinates.left,
        top: coordinates.top,
        height: coordinates.height || 18
      }
    } catch (error) {
      console.warn('计算 Monaco 光标位置失败:', error)
      return null
    }
  }

  /**
   * Markdown 光标位置计算
   */
  calculateMarkdownCursorPosition(cursorData) {
    if (!this.containerElement) return null

    try {
      const textArea = this.containerElement.querySelector('textarea') || this.containerElement
      const style = window.getComputedStyle(textArea)
      const lineHeight = parseInt(style.lineHeight) || 20
      const charWidth = this.getCharacterWidth(textArea, style)

      // 计算行和列
      const text = textArea.value.substring(0, cursorData.selectionStart)
      const lines = text.split('\n')
      const line = lines.length - 1
      const column = lines[lines.length - 1].length

      return {
        left: column * charWidth + parseInt(style.paddingLeft),
        top: line * lineHeight + parseInt(style.paddingTop),
        height: lineHeight
      }
    } catch (error) {
      console.warn('计算 Markdown 光标位置失败:', error)
      return null
    }
  }

  /**
   * 表格光标位置计算
   */
  calculateTableCursorPosition(cursorData) {
    if (!this.containerElement || !cursorData.cellIndex) return null

    try {
      const cell = this.getCellByIndex(cursorData.cellIndex)
      if (!cell) return null

      const rect = cell.getBoundingClientRect()
      const containerRect = this.containerElement.getBoundingClientRect()

      return {
        left: rect.left - containerRect.left + rect.width / 2,
        top: rect.top - containerRect.top + rect.height / 2,
        height: rect.height
      }
    } catch (error) {
      console.warn('计算表格光标位置失败:', error)
      return null
    }
  }

  /**
   * 创建光标元素
   */
  createCursorElement(sessionId, cursorData, position) {
    const cursorDiv = document.createElement('div')
    cursorDiv.className = 'remote-cursor'
    cursorDiv.dataset.sessionId = sessionId
    
    cursorDiv.style.cssText = `
      position: absolute;
      left: ${position.left}px;
      top: ${position.top}px;
      width: 2px;
      height: ${position.height}px;
      background-color: ${cursorData.userColor || '#1890ff'};
      z-index: 1000;
      pointer-events: none;
      animation: blink 1s infinite;
    `

    // 添加用户标签
    if (cursorData.userName) {
      const label = document.createElement('div')
      label.className = 'cursor-label'
      label.textContent = cursorData.userName
      label.style.cssText = `
        position: absolute;
        top: -20px;
        left: 0;
        background-color: ${cursorData.userColor || '#1890ff'};
        color: white;
        padding: 2px 6px;
        border-radius: 3px;
        font-size: 10px;
        white-space: nowrap;
        transform: translateX(-50%);
      `
      cursorDiv.appendChild(label)
    }

    return cursorDiv
  }

  /**
   * 创建选择元素
   */
  createSelectionElement(sessionId, selectionData, position, index) {
    const selectionDiv = document.createElement('div')
    selectionDiv.className = 'remote-selection'
    selectionDiv.dataset.sessionId = sessionId
    selectionDiv.dataset.index = index
    
    selectionDiv.style.cssText = `
      position: absolute;
      left: ${position.left}px;
      top: ${position.top}px;
      width: ${position.width}px;
      height: ${position.height}px;
      background-color: ${selectionData.userColor || '#1890ff'}33;
      z-index: 999;
      pointer-events: none;
    `

    return selectionDiv
  }

  /**
   * 获取字符宽度
   */
  getCharacterWidth(element, style) {
    const canvas = document.createElement('canvas')
    const context = canvas.getContext('2d')
    context.font = `${style.fontSize} ${style.fontFamily}`
    return context.measureText('M').width
  }

  /**
   * 获取单元格索引
   */
  getCellIndex(cell) {
    const row = cell.parentElement
    const table = row.parentElement.parentElement
    const rows = Array.from(table.querySelectorAll('tr'))
    const cells = Array.from(row.querySelectorAll('td, th'))
    
    return {
      row: rows.indexOf(row),
      column: cells.indexOf(cell)
    }
  }

  /**
   * 根据索引获取单元格
   */
  getCellByIndex(cellIndex) {
    if (!this.containerElement) return null
    
    const table = this.containerElement.querySelector('table')
    if (!table) return null
    
    const rows = table.querySelectorAll('tr')
    if (cellIndex.row >= rows.length) return null
    
    const cells = rows[cellIndex.row].querySelectorAll('td, th')
    if (cellIndex.column >= cells.length) return null
    
    return cells[cellIndex.column]
  }

  /**
   * 添加光标到容器
   */
  appendCursorToContainer(cursorElement) {
    if (!this.containerElement) return
    
    // 确保有光标容器
    let cursorContainer = this.containerElement.querySelector('.cursor-overlay')
    if (!cursorContainer) {
      cursorContainer = document.createElement('div')
      cursorContainer.className = 'cursor-overlay'
      cursorContainer.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        pointer-events: none;
        z-index: 1000;
      `
      this.containerElement.style.position = 'relative'
      this.containerElement.appendChild(cursorContainer)
    }
    
    cursorContainer.appendChild(cursorElement)
  }

  /**
   * 添加选择到容器
   */
  appendSelectionToContainer(selectionElement) {
    if (!this.containerElement) return
    
    // 确保有选择容器
    let selectionContainer = this.containerElement.querySelector('.selection-overlay')
    if (!selectionContainer) {
      selectionContainer = document.createElement('div')
      selectionContainer.className = 'selection-overlay'
      selectionContainer.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        pointer-events: none;
        z-index: 999;
      `
      this.containerElement.style.position = 'relative'
      this.containerElement.appendChild(selectionContainer)
    }
    
    selectionContainer.appendChild(selectionElement)
  }

  /**
   * 移除渲染的光标
   */
  removeRenderedCursor(sessionId) {
    if (!this.containerElement) return
    
    const cursors = this.containerElement.querySelectorAll(`[data-session-id="${sessionId}"].remote-cursor`)
    cursors.forEach(cursor => cursor.remove())
  }

  /**
   * 移除渲染的选择
   */
  removeRenderedSelection(sessionId) {
    if (!this.containerElement) return
    
    const selections = this.containerElement.querySelectorAll(`[data-session-id="${sessionId}"].remote-selection`)
    selections.forEach(selection => selection.remove())
  }

  /**
   * 重新计算所有光标位置
   */
  recalculateAllCursors() {
    this.cursors.forEach((cursorData, sessionId) => {
      this.renderCursor(sessionId, cursorData)
    })
    
    this.selections.forEach((selectionData, sessionId) => {
      this.renderSelection(sessionId, selectionData)
    })
  }

  /**
   * 销毁服务
   */
  destroy() {
    // 清理观察器
    this.observers.forEach(observer => {
      if (observer.disconnect) {
        observer.disconnect()
      }
    })
    this.observers.clear()

    // 清理光标和选择
    this.cursors.clear()
    this.selections.clear()

    // 移除渲染的元素
    if (this.containerElement) {
      const cursorOverlay = this.containerElement.querySelector('.cursor-overlay')
      const selectionOverlay = this.containerElement.querySelector('.selection-overlay')
      
      if (cursorOverlay) cursorOverlay.remove()
      if (selectionOverlay) selectionOverlay.remove()
    }

    // 重置属性
    this.editorType = null
    this.editorInstance = null
    this.containerElement = null
  }
}

export default CursorTrackingService