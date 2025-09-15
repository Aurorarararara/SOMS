<template>
  <div class="reports-leave-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('reportsLeave.leaveReport') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportAsImage">
          <el-icon><Picture /></el-icon>
          {{ $t('reportsLeave.exportAsImage') }}
        </el-button>
        <el-button type="success" @click="exportAsExcel">
          <el-icon><Document /></el-icon>
          {{ $t('reportsLeave.exportAsExcel') }}
        </el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item :label="$t('reportsLeave.reportType') + ':'">
          <el-select v-model="filterForm.reportType" @change="handleReportTypeChange">
            <el-option :label="$t('reportsLeave.dailyReport')" value="daily" />
            <el-option :label="$t('reportsLeave.weeklyReport')" value="weekly" />
            <el-option :label="$t('reportsLeave.monthlyReport')" value="monthly" />
            <el-option :label="$t('reportsLeave.quarterlyReport')" value="quarterly" />
            <el-option :label="$t('reportsLeave.yearlyReport')" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('reportsLeave.statisticsRange') + ':'">
          <template v-if="filterForm.reportType === 'daily' || filterForm.reportType === 'weekly' || filterForm.reportType === 'monthly'">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              :range-separator="$t('common.to')"
              :start-placeholder="$t('reportsLeave.startDate')"
              :end-placeholder="$t('reportsLeave.endDate')"
            />
          </template>
          <template v-else-if="filterForm.reportType === 'quarterly'">
            <el-select v-model="filterForm.selectedQuarters" multiple :placeholder="$t('reportsLeave.selectQuarters')" style="width: 300px">
              <el-option
                v-for="quarter in quarterOptions"
                :key="quarter.value"
                :label="quarter.label"
                :value="quarter.value">
              </el-option>
            </el-select>
          </template>
          <template v-else-if="filterForm.reportType === 'yearly'">
            <el-select v-model="filterForm.selectedYears" multiple :placeholder="$t('reportsLeave.selectYears')" style="width: 300px">
              <el-option
                v-for="year in yearOptions"
                :key="year.value"
                :label="year.label"
                :value="year.value">
              </el-option>
            </el-select>
          </template>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReport">{{ $t('reportsLeave.queryReport') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="chart-card">
      <div class="chart-container" v-loading="loading">
        <div v-if="!chartData || chartData.length === 0" class="chart-placeholder">
          <el-icon size="64" color="#c0c4cc"><PieChart /></el-icon>
          <p>{{ $t('reportsLeave.chartPlaceholder') }}</p>
        </div>
        <div v-else>
          <div ref="chartRef" class="chart-wrapper"></div>
          <div class="data-table-container">
            <el-table :data="tableData" style="width: 100%" size="small">
              <el-table-column prop="category" :label="$t('reportsLeave.category')" />
              <el-table-column prop="sickLeaveRate" :label="$t('reportsLeave.sickLeave')">
                <template #default="scope">
                  {{ scope.row.sickLeaveRate }}% ({{ scope.row.sickLeaveCount }})
                </template>
              </el-table-column>
              <el-table-column prop="personalLeaveRate" :label="$t('reportsLeave.personalLeave')">
                <template #default="scope">
                  {{ scope.row.personalLeaveRate }}% ({{ scope.row.personalLeaveCount }})
                </template>
              </el-table-column>
              <el-table-column prop="annualLeaveRate" :label="$t('reportsLeave.annualLeave')">
                <template #default="scope">
                  {{ scope.row.annualLeaveRate }}% ({{ scope.row.annualLeaveCount }})
                </template>
              </el-table-column>
              <el-table-column prop="marriageLeaveRate" :label="$t('reportsLeave.marriageLeave')">
                <template #default="scope">
                  {{ scope.row.marriageLeaveRate }}% ({{ scope.row.marriageLeaveCount }})
                </template>
              </el-table-column>
              <el-table-column prop="maternityLeaveRate" :label="$t('reportsLeave.maternityLeave')">
                <template #default="scope">
                  {{ scope.row.maternityLeaveRate }}% ({{ scope.row.maternityLeaveCount }})
                </template>
              </el-table-column>
              <el-table-column prop="bereavementLeaveRate" :label="$t('reportsLeave.bereavementLeave')">
                <template #default="scope">
                  {{ scope.row.bereavementLeaveRate }}% ({{ scope.row.bereavementLeaveCount }})
                </template>
              </el-table-column>
              <el-table-column prop="totalCount" :label="$t('reportsLeave.totalCount')" />
            </el-table>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Document, Picture, PieChart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { leaveManageApi } from '@/api/leaveManage'

const { t: $t } = useI18n()

const filterForm = reactive({
  reportType: 'monthly',
  dateRange: [],
  selectedQuarters: [],
  selectedYears: []
})

// 季度选项
const quarterOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  const options = []

  // 生成最近几年的季度选项
  for (let year = currentYear - 2; year <= currentYear + 1; year++) {
    for (let quarter = 1; quarter <= 4; quarter++) {
      options.push({
        value: `${year}-Q${quarter}`,
        label: `${year} ${$t('reportsLeave.quarter' + quarter)}`
      })
    }
  }

  return options
})

// 年份选项
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  const options = []

  // 生成从2020年到当前年份的选项
  for (let year = 2020; year <= currentYear; year++) {
    options.push({
      value: year.toString(),
      label: year.toString()
    })
  }

  return options
})

// 表格数据
const tableData = ref([])

const chartRef = ref(null)
const chartInstance = ref(null)
const chartData = ref([])
const loading = ref(false)

// 处理报表类型变化
const handleReportTypeChange = () => {
  // 设置默认值
  if (filterForm.reportType === 'quarterly') {
    // 设置当前年份的四个季度为默认选项
    const currentYear = new Date().getFullYear()
    filterForm.selectedQuarters = [
      `${currentYear}-Q1`,
      `${currentYear}-Q2`,
      `${currentYear}-Q3`,
      `${currentYear}-Q4`
    ]
  } else if (filterForm.reportType === 'yearly') {
    // 设置最近5年为默认选项
    const currentYear = new Date().getFullYear()
    filterForm.selectedYears = []
    for (let i = 0; i < 5; i++) {
      const year = (currentYear - i).toString()
      if (yearOptions.value.some(option => option.value === year)) {
        filterForm.selectedYears.push(year)
      }
    }
    // 按年份排序
    filterForm.selectedYears.sort()
  } else {
    // 日期范围报表，默认最近30天
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - 30)

    filterForm.dateRange = [
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    ]
  }
}

const loadReport = async () => {
  try {
    loading.value = true

    const params = {
      reportType: filterForm.reportType
    }

    // 根据报表类型设置参数
    if ((filterForm.reportType === 'daily' || filterForm.reportType === 'weekly' || filterForm.reportType === 'monthly')
        && filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    } else if (filterForm.reportType === 'quarterly' && filterForm.selectedQuarters && filterForm.selectedQuarters.length > 0) {
      params.quarters = filterForm.selectedQuarters.join(',')
    } else if (filterForm.reportType === 'yearly' && filterForm.selectedYears && filterForm.selectedYears.length > 0) {
      params.years = filterForm.selectedYears.join(',')
    }

    // 调用请假统计API
    const response = await leaveManageApi.getLeaveStatisticsReport(params)

    if (response.data && response.data.data) {
      chartData.value = response.data.data
      renderChart()
    } else {
      chartData.value = []
      tableData.value = []
      // 清空现有图表
      if (chartInstance.value) {
        chartInstance.value.clear()
      }
      ElMessage.warning($t('reportsLeave.noData'))
    }
  } catch (error) {
    console.error('加载报表失败:', error)
    chartData.value = []
    tableData.value = []
    // 清空现有图表
    if (chartInstance.value) {
        chartInstance.value.clear()
    }
    ElMessage.error($t('reportsLeave.loadFailed'))
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  // 检查必要数据是否存在
  if (!chartRef.value || !chartData.value || chartData.value.length === 0) {
    // 清空现有图表
    if (chartInstance.value) {
      chartInstance.value.clear()
    }
    return
  }

  // 初始化或获取图表实例
  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  // 根据报表类型准备图表数据
  let xAxisData = []
  let sickLeaveRate = []
  let personalLeaveRate = []
  let annualLeaveRate = []
  let marriageLeaveRate = []
  let maternityLeaveRate = []
  let bereavementLeaveRate = []

  // 对应的人数数据
  let sickLeaveCount = []
  let personalLeaveCount = []
  let annualLeaveCount = []
  let marriageLeaveCount = []
  let maternityLeaveCount = []
  let bereavementLeaveCount = []
  let totalCount = []

  switch (filterForm.reportType) {
    case 'daily':
    case 'weekly':
    case 'monthly':
      xAxisData = chartData.value.map(item => item.date || item.week || item.month)

      chartData.value.forEach(item => {
        const total = (item.sickLeave || 0) + (item.personalLeave || 0) + (item.annualLeave || 0) +
                     (item.marriageLeave || 0) + (item.maternityLeave || 0) + (item.bereavementLeave || 0)

        totalCount.push(total)
        sickLeaveCount.push(item.sickLeave || 0)
        personalLeaveCount.push(item.personalLeave || 0)
        annualLeaveCount.push(item.annualLeave || 0)
        marriageLeaveCount.push(item.marriageLeave || 0)
        maternityLeaveCount.push(item.maternityLeave || 0)
        bereavementLeaveCount.push(item.bereavementLeave || 0)

        sickLeaveRate.push(total > 0 ? Math.round(((item.sickLeave || 0) / total) * 10000) / 100 : 0)
        personalLeaveRate.push(total > 0 ? Math.round(((item.personalLeave || 0) / total) * 10000) / 100 : 0)
        annualLeaveRate.push(total > 0 ? Math.round(((item.annualLeave || 0) / total) * 10000) / 100 : 0)
        marriageLeaveRate.push(total > 0 ? Math.round(((item.marriageLeave || 0) / total) * 10000) / 100 : 0)
        maternityLeaveRate.push(total > 0 ? Math.round(((item.maternityLeave || 0) / total) * 10000) / 100 : 0)
        bereavementLeaveRate.push(total > 0 ? Math.round(((item.bereavementLeave || 0) / total) * 10000) / 100 : 0)
      })
      break

    case 'quarterly':
      xAxisData = filterForm.selectedQuarters.map(q => {
        const [year, quarter] = q.split('-')
        return `${year} ${$t('reportsLeave.' + quarter.replace('Q', 'quarter'))}`
      })

      chartData.value.forEach(item => {
        const total = (item.sickLeave || 0) + (item.personalLeave || 0) + (item.annualLeave || 0) +
                     (item.marriageLeave || 0) + (item.maternityLeave || 0) + (item.bereavementLeave || 0)

        totalCount.push(total)
        sickLeaveCount.push(item.sickLeave || 0)
        personalLeaveCount.push(item.personalLeave || 0)
        annualLeaveCount.push(item.annualLeave || 0)
        marriageLeaveCount.push(item.marriageLeave || 0)
        maternityLeaveCount.push(item.maternityLeave || 0)
        bereavementLeaveCount.push(item.bereavementLeave || 0)

        sickLeaveRate.push(total > 0 ? Math.round(((item.sickLeave || 0) / total) * 10000) / 100 : 0)
        personalLeaveRate.push(total > 0 ? Math.round(((item.personalLeave || 0) / total) * 10000) / 100 : 0)
        annualLeaveRate.push(total > 0 ? Math.round(((item.annualLeave || 0) / total) * 10000) / 100 : 0)
        marriageLeaveRate.push(total > 0 ? Math.round(((item.marriageLeave || 0) / total) * 10000) / 100 : 0)
        maternityLeaveRate.push(total > 0 ? Math.round(((item.maternityLeave || 0) / total) * 10000) / 100 : 0)
        bereavementLeaveRate.push(total > 0 ? Math.round(((item.bereavementLeave || 0) / total) * 10000) / 100 : 0)
      })
      break

    case 'yearly':
      xAxisData = [...filterForm.selectedYears].sort()

      chartData.value.forEach(item => {
        const total = (item.sickLeave || 0) + (item.personalLeave || 0) + (item.annualLeave || 0) +
                     (item.marriageLeave || 0) + (item.maternityLeave || 0) + (item.bereavementLeave || 0)

        totalCount.push(total)
        sickLeaveCount.push(item.sickLeave || 0)
        personalLeaveCount.push(item.personalLeave || 0)
        annualLeaveCount.push(item.annualLeave || 0)
        marriageLeaveCount.push(item.marriageLeave || 0)
        maternityLeaveCount.push(item.maternityLeave || 0)
        bereavementLeaveCount.push(item.bereavementLeave || 0)

        sickLeaveRate.push(total > 0 ? Math.round(((item.sickLeave || 0) / total) * 10000) / 100 : 0)
        personalLeaveRate.push(total > 0 ? Math.round(((item.personalLeave || 0) / total) * 10000) / 100 : 0)
        annualLeaveRate.push(total > 0 ? Math.round(((item.annualLeave || 0) / total) * 10000) / 100 : 0)
        marriageLeaveRate.push(total > 0 ? Math.round(((item.marriageLeave || 0) / total) * 10000) / 100 : 0)
        maternityLeaveRate.push(total > 0 ? Math.round(((item.maternityLeave || 0) / total) * 10000) / 100 : 0)
        bereavementLeaveRate.push(total > 0 ? Math.round(((item.bereavementLeave || 0) / total) * 10000) / 100 : 0)
      })
      break
    default:
      // 默认情况，清空图表
      if (chartInstance.value) {
        chartInstance.value.clear()
      }
      return
  }

  // 图表配置
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        let tooltip = params[0].name + '<br/>'
        params.forEach(param => {
          tooltip += `${param.marker} ${param.seriesName}: ${param.value}%<br/>`
        })
        return tooltip
      }
    },
    legend: {
      data: [
        $t('reportsLeave.sickLeave'),
        $t('reportsLeave.personalLeave'),
        $t('reportsLeave.annualLeave'),
        $t('reportsLeave.marriageLeave'),
        $t('reportsLeave.maternityLeave'),
        $t('reportsLeave.bereavementLeave')
      ]
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value} %'
      }
    },
    series: [
      {
        name: $t('reportsLeave.sickLeave'),
        type: 'bar',
        barGap: '5%',
        data: sickLeaveRate,
        itemStyle: { color: '#ff4d4f' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value + '%' : ''
          }
        }
      },
      {
        name: $t('reportsLeave.personalLeave'),
        type: 'bar',
        barGap: '5%',
        data: personalLeaveRate,
        itemStyle: { color: '#fa8c16' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value + '%' : ''
          }
        }
      },
      {
        name: $t('reportsLeave.annualLeave'),
        type: 'bar',
        barGap: '5%',
        data: annualLeaveRate,
        itemStyle: { color: '#52c41a' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value + '%' : ''
          }
        }
      },
      {
        name: $t('reportsLeave.marriageLeave'),
        type: 'bar',
        barGap: '5%',
        data: marriageLeaveRate,
        itemStyle: { color: '#722ed1' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value + '%' : ''
          }
        }
      },
      {
        name: $t('reportsLeave.maternityLeave'),
        type: 'bar',
        barGap: '5%',
        data: maternityLeaveRate,
        itemStyle: { color: '#eb2f96' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value + '%' : ''
          }
        }
      },
      {
        name: $t('reportsLeave.bereavementLeave'),
        type: 'bar',
        barGap: '5%',
        data: bereavementLeaveRate,
        itemStyle: { color: '#13c2c2' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value + '%' : ''
          }
        }
      }
    ]
  }

  // 渲染图表
  chartInstance.value.setOption(option, true)

  // 更新表格数据
  tableData.value = xAxisData.map((item, index) => ({
    category: item,
    sickLeaveRate: sickLeaveRate[index],
    personalLeaveRate: personalLeaveRate[index],
    annualLeaveRate: annualLeaveRate[index],
    marriageLeaveRate: marriageLeaveRate[index],
    maternityLeaveRate: maternityLeaveRate[index],
    bereavementLeaveRate: bereavementLeaveRate[index],
    sickLeaveCount: sickLeaveCount[index],
    personalLeaveCount: personalLeaveCount[index],
    annualLeaveCount: annualLeaveCount[index],
    marriageLeaveCount: marriageLeaveCount[index],
    maternityLeaveCount: maternityLeaveCount[index],
    bereavementLeaveCount: bereavementLeaveCount[index],
    totalCount: totalCount[index]
  }))
}

// 导出为图片
const exportAsImage = () => {
  if (!chartInstance.value) {
    ElMessage.warning($t('reportsLeave.noChartToExport'))
    return
  }

  const dataUrl = chartInstance.value.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff'
  })

  const link = document.createElement('a')
  link.download = `请假报表_${new Date().toLocaleDateString()}.png`
  link.href = dataUrl
  link.click()

  ElMessage.success($t('reportsLeave.exportImageSuccess'))
}

// 导出为Excel
const exportAsExcel = () => {
  if (!tableData.value || tableData.value.length === 0) {
    ElMessage.warning($t('reportsLeave.noDataToExport'))
    return
  }

  // 创建Excel内容
  let excelContent = `${$t('reportsLeave.category')},${$t('reportsLeave.sickLeave')},${$t('reportsLeave.personalLeave')},${$t('reportsLeave.annualLeave')},${$t('reportsLeave.marriageLeave')},${$t('reportsLeave.maternityLeave')},${$t('reportsLeave.bereavementLeave')},${$t('reportsLeave.totalCount')}\n`

  tableData.value.forEach(row => {
    excelContent += `${row.category},${row.sickLeaveRate}% (${row.sickLeaveCount}),${row.personalLeaveRate}% (${row.personalLeaveCount}),${row.annualLeaveRate}% (${row.annualLeaveCount}),${row.marriageLeaveRate}% (${row.marriageLeaveCount}),${row.maternityLeaveRate}% (${row.maternityLeaveCount}),${row.bereavementLeaveRate}% (${row.bereavementLeaveCount}),${row.totalCount}\n`
  })

  // 创建Blob对象
  const blob = new Blob(['\uFEFF' + excelContent], {
    type: 'application/vnd.ms-excel;charset=utf-8'
  })

  // 创建下载链接
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `请假报表_${new Date().toLocaleDateString()}.csv`
  link.click()

  ElMessage.success($t('reportsLeave.exportExcelSuccess'))
}

// 组件挂载时初始化
onMounted(() => {
  // 设置默认时间范围为最近30天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)

  filterForm.dateRange = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]

  // 设置季度报表的默认选项
  const currentYear = new Date().getFullYear()
  filterForm.selectedQuarters = [
    `${currentYear}-Q1`,
    `${currentYear}-Q2`,
    `${currentYear}-Q3`,
    `${currentYear}-Q4`
  ]

  // 设置年度报表的默认选项
  filterForm.selectedYears = []
  for (let i = 0; i < 5; i++) {
    const year = (currentYear - i).toString()
    if (yearOptions.value.some(option => option.value === year)) {
      filterForm.selectedYears.push(year)
    }
  }
  // 按年份排序
  filterForm.selectedYears.sort()

  // 加载默认报表
  loadReport()
})
</script>

<style scoped>
.reports-leave-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.page-title {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-card {
  margin-bottom: 20px;
}

.chart-card {
  min-height: 500px;
}

.chart-container {
  height: 100%;
  min-height: 500px;
  position: relative;
}

.chart-wrapper {
  height: 60%;
  min-height: 300px;
}

.data-table-container {
  height: 40%;
  padding: 20px 0;
}

.chart-placeholder {
  text-align: center;
  color: #c0c4cc;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: center;
  }
}
</style>