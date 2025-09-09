<template>
  <div class="organization-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">组织架构图</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索部门或员工"
          clearable
          style="width: 300px; margin-right: 10px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon dept-icon">
              <el-icon size="30"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalDepartments }}</div>
              <div class="stat-label">部门总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon emp-icon">
              <el-icon size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalEmployees }}</div>
              <div class="stat-label">员工总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon level-icon">
              <el-icon size="30"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ maxLevel }}</div>
              <div class="stat-label">最大层级</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon avg-icon">
              <el-icon size="30"><Histogram /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ avgEmployeesPerDept }}</div>
              <div class="stat-label">平均部门人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 组织架构图 -->
    <el-card class="chart-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>组织架构图</span>
          <div class="header-controls">
            <el-button-group>
              <el-button :type="viewMode === 'tree' ? 'primary' : 'default'" @click="changeViewMode('tree')">
                树状图
              </el-button>
              <el-button :type="viewMode === 'org' ? 'primary' : 'default'" @click="changeViewMode('org')">
                组织图
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <div class="chart-container">
        <!-- 树状图视图 -->
        <div v-if="viewMode === 'tree'" class="tree-view">
          <el-tree
            ref="treeRef"
            :data="organizationTree"
            :props="treeProps"
            :expand-on-click-node="false"
            :default-expand-all="true"
            node-key="id"
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-header">
                  <el-icon class="node-icon"><OfficeBuilding /></el-icon>
                  <span class="node-name">{{ data.name }}</span>
                  <el-tag size="small" type="info">{{ data.employeeCount }}人</el-tag>
                </div>
                <div v-if="data.employees && data.employees.length > 0" class="node-employees">
                  <div 
                    v-for="employee in data.employees" 
                    :key="employee.id" 
                    class="employee-item"
                    @click.stop="viewEmployeeDetail(employee)"
                  >
                    <el-icon><User /></el-icon>
                    <span>{{ employee.user?.realName || '未知' }}</span>
                    <span class="employee-position">({{ employee.position || '未设置职位' }})</span>
                  </div>
                </div>
              </div>
            </template>
          </el-tree>
        </div>

        <!-- 组织图视图 -->
        <div v-else class="org-chart-view">
          <div ref="orgChartContainer" class="org-chart-container"></div>
        </div>
      </div>
    </el-card>

    <!-- 部门详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="selectedDepartment?.name + ' 详情'"
      width="600px"
    >
      <div class="department-detail" v-if="selectedDepartment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="部门名称">{{ selectedDepartment.name }}</el-descriptions-item>
          <el-descriptions-item label="部门描述">{{ selectedDepartment.description }}</el-descriptions-item>
          <el-descriptions-item label="部门层级">{{ selectedDepartment.level }}</el-descriptions-item>
          <el-descriptions-item label="员工数量">{{ selectedDepartment.employeeCount }}人</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedDepartment.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="employee-list" v-if="selectedDepartment.employees && selectedDepartment.employees.length > 0">
          <h4>部门员工 ({{ selectedDepartment.employees.length }}人)</h4>
          <el-table :data="selectedDepartment.employees" size="small">
            <el-table-column prop="user.realName" label="姓名" width="100" />
            <el-table-column prop="employeeNo" label="工号" width="100" />
            <el-table-column prop="position" label="职位" width="150" />
            <el-table-column prop="hireDate" label="入职日期" width="120">
              <template #default="scope">
                {{ scope.row.hireDate ? formatDate(scope.row.hireDate) : '' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 员工详情对话框 -->
    <el-dialog
      v-model="showEmployeeDialog"
      :title="selectedEmployee?.user?.realName + ' 详情'"
      width="500px"
    >
      <div class="employee-detail" v-if="selectedEmployee && selectedEmployee.user">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">{{ selectedEmployee.user.realName }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ selectedEmployee.employeeNo }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ selectedEmployee.position }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ selectedEmployee.department?.name }}</el-descriptions-item>
          <el-descriptions-item label="入职日期">{{ formatDate(selectedEmployee.hireDate) }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedEmployee.user.email }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ selectedEmployee.user.phone }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, Refresh, OfficeBuilding, User, DataAnalysis, 
  Histogram 
} from '@element-plus/icons-vue'
import * as d3 from 'd3'
import { organizationApi } from '@/api/organization'

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const viewMode = ref('tree') // 'tree' 或 'org'

const organizationTree = ref([])
const stats = reactive({
  totalDepartments: 0,
  totalEmployees: 0,
  departmentStats: [],
  levelStats: []
})

const showDetailDialog = ref(false)
const showEmployeeDialog = ref(false)
const selectedDepartment = ref(null)
const selectedEmployee = ref(null)

const treeRef = ref(null)
const orgChartContainer = ref(null)

// 树组件配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 计算属性
const maxLevel = computed(() => {
  if (!stats.levelStats || stats.levelStats.length === 0) return 0
  return Math.max(...stats.levelStats.map(item => item.level || 0))
})

const avgEmployeesPerDept = computed(() => {
  if (stats.totalDepartments === 0) return 0
  return (stats.totalEmployees / stats.totalDepartments).toFixed(1)
})

// 方法
const loadOrganizationData = async () => {
  loading.value = true
  try {
    // 并行加载组织架构树和统计信息
    const [treeRes, statsRes] = await Promise.all([
      organizationApi.getOrganizationTree(),
      organizationApi.getOrganizationStats()
    ])

    if (treeRes.data) {
      organizationTree.value = treeRes.data
    }

    if (statsRes.data) {
      Object.assign(stats, statsRes.data)
    }
  } catch (error) {
    console.error('加载组织架构数据失败:', error)
    ElMessage.error('加载组织架构数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    // 如果搜索关键词为空，刷新完整数据
    await loadOrganizationData()
    return
  }

  try {
    const response = await organizationApi.searchOrganization(searchKeyword.value)
    if (response.data) {
      ElMessage.info(`找到 ${response.data.length} 条相关记录`)
      // 这里可以根据搜索结果进行相应处理
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败')
  }
}

const refreshData = () => {
  searchKeyword.value = ''
  loadOrganizationData()
}

const changeViewMode = (mode) => {
  viewMode.value = mode
  if (mode === 'org') {
    nextTick(() => {
      renderOrgChart()
    })
  }
}

const handleNodeClick = (data) => {
  selectedDepartment.value = data
  showDetailDialog.value = true
}

const viewEmployeeDetail = (employee) => {
  selectedEmployee.value = employee
  showEmployeeDialog.value = true
}

const renderOrgChart = () => {
  if (!orgChartContainer.value || organizationTree.value.length === 0) return

  // 清空容器
  orgChartContainer.value.innerHTML = ''

  // 设置图表尺寸
  const width = orgChartContainer.value.clientWidth
  const height = 600

  // 创建SVG容器
  const svg = d3.select(orgChartContainer.value)
    .append('svg')
    .attr('width', width)
    .attr('height', height)

  // 创建层级布局
  const root = d3.hierarchy(organizationTree.value[0])
  const treeLayout = d3.tree().size([height - 100, width - 200])
  treeLayout(root)

  // 绘制连线
  const links = svg.selectAll('.link')
    .data(root.links())
    .enter()
    .append('line')
    .attr('class', 'link')
    .attr('x1', d => d.source.x)
    .attr('y1', d => d.source.y)
    .attr('x2', d => d.target.x)
    .attr('y2', d => d.target.y)
    .attr('stroke', '#ccc')
    .attr('stroke-width', 2)

  // 绘制节点
  const nodes = svg.selectAll('.node')
    .data(root.descendants())
    .enter()
    .append('g')
    .attr('class', 'node')
    .attr('transform', d => `translate(${d.x},${d.y})`)

  // 节点圆形
  nodes.append('circle')
    .attr('r', 20)
    .attr('fill', '#409eff')
    .attr('stroke', '#fff')
    .attr('stroke-width', 2)

  // 节点文本
  nodes.append('text')
    .attr('dy', 5)
    .attr('text-anchor', 'middle')
    .attr('fill', '#333')
    .attr('font-size', '12px')
    .text(d => d.data.name)

  // 员工数量标签
  nodes.append('text')
    .attr('dy', 25)
    .attr('text-anchor', 'middle')
    .attr('fill', '#666')
    .attr('font-size', '10px')
    .text(d => `${d.data.employeeCount || 0}人`)
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 组件挂载
onMounted(() => {
  loadOrganizationData()
})
</script>

<style scoped>
.organization-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
}

.dept-icon { background-color: #409eff; }
.emp-icon { background-color: #67c23a; }
.level-icon { background-color: #e6a23c; }
.avg-icon { background-color: #f56c6c; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  min-height: 500px;
}

.tree-view {
  padding: 20px 0;
}

.tree-node {
  padding: 8px 0;
}

.node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.node-icon {
  color: #409eff;
}

.node-name {
  font-weight: 500;
  color: #333;
}

.node-employees {
  margin-left: 24px;
  margin-top: 5px;
}

.employee-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 3px 0;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.employee-item:hover {
  color: #409eff;
  background-color: #f5f7fa;
}

.employee-position {
  color: #999;
  font-size: 12px;
}

.org-chart-view {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 500px;
}

.org-chart-container {
  width: 100%;
  height: 100%;
}

.department-detail,
.employee-detail {
  padding: 10px 0;
}

.employee-list {
  margin-top: 20px;
}

.employee-list h4 {
  margin: 0 0 10px 0;
  color: #333;
}
</style>