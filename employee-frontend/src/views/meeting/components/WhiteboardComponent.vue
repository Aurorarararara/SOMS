<template>
  <div class="whiteboard-container">
    <!-- 工具栏 -->
    <div class="whiteboard-toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button 
            :type="tool === 'pen' ? 'primary' : 'default'"
            size="small"
            @click="setTool('pen')"
          >
            <el-icon><Edit /></el-icon>
            画笔
          </el-button>
          <el-button 
            :type="tool === 'eraser' ? 'primary' : 'default'"
            size="small"
            @click="setTool('eraser')"
          >
            <el-icon><Delete /></el-icon>
            橡皮
          </el-button>
          <el-button 
            :type="tool === 'text' ? 'primary' : 'default'"
            size="small"
            @click="setTool('text')"
          >
            <el-icon><Document /></el-icon>
            文字
          </el-button>
          <el-button 
            :type="tool === 'shape' ? 'primary' : 'default'"
            size="small"
            @click="setTool('shape')"
          >
            <el-icon><Grid /></el-icon>
            形状
          </el-button>
        </el-button-group>
        
        <!-- 画笔设置 -->
        <div class="pen-settings" v-if="tool === 'pen'">
          <el-select v-model="penSize" size="small" style="width: 80px">
            <el-option label="细" :value="2" />
            <el-option label="中" :value="4" />
            <el-option label="粗" :value="8" />
          </el-select>
          
          <div class="color-picker">
            <div 
              v-for="color in colors"
              :key="color"
              class="color-item"
              :class="{ active: penColor === color }"
              :style="{ backgroundColor: color }"
              @click="setPenColor(color)"
            ></div>
          </div>
        </div>
        
        <!-- 形状设置 -->
        <div class="shape-settings" v-if="tool === 'shape'">
          <el-select v-model="shapeType" size="small" style="width: 100px">
            <el-option label="矩形" value="rect" />
            <el-option label="圆形" value="circle" />
            <el-option label="直线" value="line" />
            <el-option label="箭头" value="arrow" />
          </el-select>
        </div>
      </div>
      
      <div class="toolbar-right">
        <el-button size="small" @click="undo" :disabled="!canUndo">
          <el-icon><RefreshLeft /></el-icon>
          撤销
        </el-button>
        <el-button size="small" @click="redo" :disabled="!canRedo">
          <el-icon><RefreshRight /></el-icon>
          重做
        </el-button>
        <el-button size="small" @click="clearCanvas">
          <el-icon><Delete /></el-icon>
          清空
        </el-button>
        <el-button size="small" @click="saveWhiteboard">
          <el-icon><Download /></el-icon>
          保存
        </el-button>
        <el-button size="small" @click="$emit('close')">
          <el-icon><Close /></el-icon>
          关闭
        </el-button>
      </div>
    </div>
    
    <!-- 画布区域 -->
    <div class="canvas-container" ref="canvasContainer">
      <canvas 
        ref="canvas"
        @mousedown="startDrawing"
        @mousemove="draw"
        @mouseup="stopDrawing"
        @mouseleave="stopDrawing"
        @touchstart="handleTouch"
        @touchmove="handleTouch"
        @touchend="stopDrawing"
      ></canvas>
      
      <!-- 文字输入框 -->
      <div 
        v-if="showTextInput"
        class="text-input-container"
        :style="textInputStyle"
      >
        <el-input 
          ref="textInput"
          v-model="textContent"
          type="textarea"
          :rows="1"
          placeholder="输入文字"
          @blur="addText"
          @keyup.enter="addText"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Edit, Delete, Document, Grid, RefreshLeft, RefreshRight,
  Download, Close
} from '@element-plus/icons-vue'
import meetingApi from '@/api/meeting'

export default {
  name: 'WhiteboardComponent',
  components: {
    Edit, Delete, Document, Grid, RefreshLeft, RefreshRight,
    Download, Close
  },
  props: {
    meetingId: {
      type: String,
      required: true
    }
  },
  emits: ['close'],
  setup(props) {
    const canvas = ref()
    const canvasContainer = ref()
    const textInput = ref()
    const ctx = ref(null)
    
    // 工具状态
    const tool = ref('pen')
    const penSize = ref(4)
    const penColor = ref('#000000')
    const shapeType = ref('rect')
    
    // 绘制状态
    const isDrawing = ref(false)
    const lastX = ref(0)
    const lastY = ref(0)
    const currentPath = ref([])
    
    // 历史记录
    const history = ref([])
    const historyIndex = ref(-1)
    
    // 文字输入
    const showTextInput = ref(false)
    const textContent = ref('')
    const textInputStyle = ref({})
    
    // 颜色选项
    const colors = [
      '#000000', '#FF0000', '#00FF00', '#0000FF',
      '#FFFF00', '#FF00FF', '#00FFFF', '#FFA500',
      '#800080', '#008000', '#800000', '#808080'
    ]
    
    // 计算属性
    const canUndo = computed(() => historyIndex.value > 0)
    const canRedo = computed(() => historyIndex.value < history.value.length - 1)
    
    // 初始化画布
    const initCanvas = () => {
      const canvasEl = canvas.value
      const container = canvasContainer.value
      
      if (!canvasEl || !container) return
      
      // 设置画布尺寸
      const rect = container.getBoundingClientRect()
      canvasEl.width = rect.width
      canvasEl.height = rect.height
      
      ctx.value = canvasEl.getContext('2d')
      
      // 设置画布样式
      ctx.value.lineCap = 'round'
      ctx.value.lineJoin = 'round'
      ctx.value.strokeStyle = penColor.value
      ctx.value.lineWidth = penSize.value
      
      // 保存初始状态
      saveState()
    }
    
    // 获取鼠标位置
    const getMousePos = (e) => {
      const rect = canvas.value.getBoundingClientRect()
      return {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      }
    }
    
    // 获取触摸位置
    const getTouchPos = (e) => {
      const rect = canvas.value.getBoundingClientRect()
      const touch = e.touches[0] || e.changedTouches[0]
      return {
        x: touch.clientX - rect.left,
        y: touch.clientY - rect.top
      }
    }
    
    // 开始绘制
    const startDrawing = (e) => {
      if (tool.value === 'text') {
        showTextInputAt(e)
        return
      }
      
      isDrawing.value = true
      const pos = getMousePos(e)
      lastX.value = pos.x
      lastY.value = pos.y
      
      if (tool.value === 'pen') {
        ctx.value.beginPath()
        ctx.value.moveTo(pos.x, pos.y)
        currentPath.value = [{ x: pos.x, y: pos.y, type: 'move' }]
      }
    }
    
    // 绘制
    const draw = (e) => {
      if (!isDrawing.value) return
      
      const pos = getMousePos(e)
      
      if (tool.value === 'pen') {
        ctx.value.strokeStyle = penColor.value
        ctx.value.lineWidth = penSize.value
        ctx.value.lineTo(pos.x, pos.y)
        ctx.value.stroke()
        
        currentPath.value.push({ x: pos.x, y: pos.y, type: 'line' })
      } else if (tool.value === 'eraser') {
        ctx.value.globalCompositeOperation = 'destination-out'
        ctx.value.beginPath()
        ctx.value.arc(pos.x, pos.y, penSize.value * 2, 0, Math.PI * 2)
        ctx.value.fill()
        ctx.value.globalCompositeOperation = 'source-over'
      }
      
      lastX.value = pos.x
      lastY.value = pos.y
    }
    
    // 停止绘制
    const stopDrawing = () => {
      if (!isDrawing.value) return
      
      isDrawing.value = false
      
      if (tool.value === 'pen' && currentPath.value.length > 0) {
        // 保存路径到历史记录
        saveState()
      }
      
      currentPath.value = []
    }
    
    // 处理触摸事件
    const handleTouch = (e) => {
      e.preventDefault()
      
      const touch = e.touches[0] || e.changedTouches[0]
      const mouseEvent = new MouseEvent(e.type === 'touchstart' ? 'mousedown' : 
                                       e.type === 'touchmove' ? 'mousemove' : 'mouseup', {
        clientX: touch.clientX,
        clientY: touch.clientY
      })
      
      canvas.value.dispatchEvent(mouseEvent)
    }
    
    // 显示文字输入框
    const showTextInputAt = (e) => {
      const pos = getMousePos(e)
      textInputStyle.value = {
        position: 'absolute',
        left: pos.x + 'px',
        top: pos.y + 'px',
        zIndex: 1000
      }
      showTextInput.value = true
      
      nextTick(() => {
        textInput.value?.focus()
      })
    }
    
    // 添加文字
    const addText = () => {
      if (!textContent.value.trim()) {
        showTextInput.value = false
        textContent.value = ''
        return
      }
      
      const x = parseInt(textInputStyle.value.left)
      const y = parseInt(textInputStyle.value.top)
      
      ctx.value.font = '16px Arial'
      ctx.value.fillStyle = penColor.value
      ctx.value.fillText(textContent.value, x, y)
      
      showTextInput.value = false
      textContent.value = ''
      saveState()
    }
    
    // 设置工具
    const setTool = (newTool) => {
      tool.value = newTool
      showTextInput.value = false
    }
    
    // 设置画笔颜色
    const setPenColor = (color) => {
      penColor.value = color
    }
    
    // 保存状态
    const saveState = () => {
      // 检查画布是否已正确初始化
      if (!canvas.value || canvas.value.width === 0 || canvas.value.height === 0) {
        return
      }
      
      const imageData = ctx.value.getImageData(0, 0, canvas.value.width, canvas.value.height)
      
      // 删除当前索引之后的历史记录
      history.value = history.value.slice(0, historyIndex.value + 1)
      
      // 添加新状态
      history.value.push(imageData)
      historyIndex.value = history.value.length - 1
      
      // 限制历史记录数量
      if (history.value.length > 50) {
        history.value.shift()
        historyIndex.value--
      }
    }
    
    // 撤销
    const undo = () => {
      if (canUndo.value) {
        historyIndex.value--
        const imageData = history.value[historyIndex.value]
        ctx.value.putImageData(imageData, 0, 0)
      }
    }
    
    // 重做
    const redo = () => {
      if (canRedo.value) {
        historyIndex.value++
        const imageData = history.value[historyIndex.value]
        ctx.value.putImageData(imageData, 0, 0)
      }
    }
    
    // 清空画布
    const clearCanvas = () => {
      ctx.value.clearRect(0, 0, canvas.value.width, canvas.value.height)
      saveState()
    }
    
    // 保存白板
    const saveWhiteboard = async () => {
      try {
        const imageData = canvas.value.toDataURL('image/png')
        await meetingApi.saveWhiteboardData(props.meetingId, {
          imageData,
          timestamp: new Date().toISOString()
        })
        ElMessage.success('白板已保存')
      } catch (error) {
        console.error('保存白板失败:', error)
        ElMessage.error('保存白板失败')
      }
    }
    
    // 加载白板数据
    const loadWhiteboard = async () => {
      try {
        const response = await meetingApi.getWhiteboardData(props.meetingId)
        if (response.data && response.data.imageData) {
          const img = new Image()
          img.onload = () => {
            ctx.value.drawImage(img, 0, 0)
            saveState()
          }
          img.src = response.data.imageData
        }
      } catch (error) {
        console.error('加载白板数据失败:', error)
      }
    }
    
    // 窗口大小改变处理
    const handleResize = () => {
      // 保存当前画布内容
      const imageData = ctx.value.getImageData(0, 0, canvas.value.width, canvas.value.height)
      
      // 重新设置画布尺寸
      initCanvas()
      
      // 恢复画布内容
      ctx.value.putImageData(imageData, 0, 0)
    }
    
    // 组件挂载
    onMounted(() => {
      nextTick(() => {
        initCanvas()
        loadWhiteboard()
      })
      window.addEventListener('resize', handleResize)
    })
    
    // 组件卸载
    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
    })
    
    return {
      canvas,
      canvasContainer,
      textInput,
      tool,
      penSize,
      penColor,
      shapeType,
      colors,
      showTextInput,
      textContent,
      textInputStyle,
      canUndo,
      canRedo,
      startDrawing,
      draw,
      stopDrawing,
      handleTouch,
      setTool,
      setPenColor,
      addText,
      undo,
      redo,
      clearCanvas,
      saveWhiteboard
    }
  }
}
</script>

<style scoped>
.whiteboard-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
}

.whiteboard-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.pen-settings {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-picker {
  display: flex;
  gap: 4px;
}

.color-item {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.color-item:hover {
  transform: scale(1.1);
}

.color-item.active {
  border-color: #409eff;
  transform: scale(1.2);
}

.shape-settings {
  display: flex;
  align-items: center;
  gap: 8px;
}

.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.canvas-container canvas {
  display: block;
  width: 100%;
  height: 100%;
  cursor: crosshair;
}

.text-input-container {
  position: absolute;
  min-width: 100px;
  z-index: 1000;
}

.text-input-container .el-textarea {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
}

/* 工具特定的光标 */
.canvas-container canvas[data-tool="pen"] {
  cursor: crosshair;
}

.canvas-container canvas[data-tool="eraser"] {
  cursor: grab;
}

.canvas-container canvas[data-tool="text"] {
  cursor: text;
}

.canvas-container canvas[data-tool="shape"] {
  cursor: crosshair;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .whiteboard-toolbar {
    flex-direction: column;
    gap: 12px;
    padding: 8px;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: center;
  }
  
  .pen-settings {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .color-picker {
    flex-wrap: wrap;
  }
}
</style>