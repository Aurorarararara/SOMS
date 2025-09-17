<template>
  <div class="reports-performance-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('reportsPerformance.performanceReport') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportAsImage">
          <el-icon><Picture /></el-icon>
          {{ $t('reportsPerformance.exportAsImage') }}
        </el-button>
        <el-button type="success" @click="exportAsExcel">
          <el-icon><Document /></el-icon>
          {{ $t('reportsPerformance.exportAsExcel') }}
        </el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item :label="$t('reportsPerformance.reportType') + ':'">
          <el-select v-model="filterForm.reportType" @change="handleReportTypeChange">
            <el-option :label="$t('reportsPerformance.monthlyReport')" value="monthly" />
            <el-option :label="$t('reportsPerformance.quarterlyReport')" value="quarterly" />
            <el-option :label="$t('reportsPerformance.yearlyReport')" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('reportsPerformance.statisticsRange') + ':'">
          <template v-if="filterForm.reportType === 'monthly'">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              :range-separator="$t('common.to')"
              :start-placeholder="$t('reportsPerformance.startDate')"
              :end-placeholder="$t('reportsPerformance.endDate')"
            />
          </template>
          <template v-else-if="filterForm.reportType === 'quarterly'">
            <el-select v-model="filterForm.selectedQuarters" multiple :placeholder="$t('reportsPerformance.selectQuarters')" style="width: 300px">
              <el-option
                v-for="quarter in quarterOptions"
                :key="quarter.value"
                :label="quarter.label"
                :value="quarter.value">
              </el-option>
            </el-select>
          </template>
          <template v-else-if="filterForm.reportType === 'yearly'">
            <el-select v-model="filterForm.selectedYears" multiple :placeholder="$t('reportsPerformance.selectYears')" style="width: 300px">
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
          <el-button type="primary" @click="loadReport">{{ $t('reportsPerformance.queryReport') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="chart-card">
      <div class="chart-container" v-loading="loading">
        <div v-if="!chartData || chartData.length === 0" class="chart-placeholder">
          <el-icon size="64" color="#c0c4cc"><TrendCharts /></el-icon>
          <p>{{ $t('reportsPerformance.noData') }}</p>
        </div>
        <div v-else>
          <div ref="chartRef" class="chart-wrapper"></div>
          <div class="data-table-container">
            <el-table :data="tableData" style="width: 100%" size="small">
              <el-table-column prop="category" :label="$t('reportsPerformance.category')" />
              <el-table-column prop="taskCompletionRate" :label="$t('reportsPerformance.taskCompletionRate')">
                <template #default="scope">
                  {{ scope.row.taskCompletionRate }}% ({{ scope.row.taskCompletionCount }})
                </template>
              </el-table-column>
              <el-table-column prop="qualityScore" :label="$t('reportsPerformance.qualityScore')">
                <template #default="scope">
                  {{ scope.row.qualityScore }}% ({{ scope.row.qualityCount }})
                </template>
              </el-table-column>
              <el-table-column prop="efficiencyScore" :label="$t('reportsPerformance.efficiencyScore')">
                <template #default="scope">
                  {{ scope.row.efficiencyScore }}% ({{ scope.row.efficiencyCount }})
                </template>
              </el-table-column>
              <el-table-column prop="totalCount" :label="$t('reportsPerformance.totalCount')" />
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
import { Document, Picture, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { performanceApi } from '@/api/performance'

const { t: $t } = useI18n()

const filterForm = reactive({
  reportType: 'monthly',
  dateRange: [],
  selectedQuarters: [], // 季度选择
  selectedYears: [] // 年份选择
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
        label: `${year} ${$t('reportsPerformance.quarter' + quarter)}`
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
    // 月度报表，默认最近30天
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
      type: filterForm.reportType
    }

    // 根据报表类型设置参数
    if (filterForm.reportType === 'monthly' && filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    } else if (filterForm.reportType === 'quarterly' && filterForm.selectedQuarters && filterForm.selectedQuarters.length > 0) {
      params.quarters = filterForm.selectedQuarters.join(',')
    } else if (filterForm.reportType === 'yearly' && filterForm.selectedYears && filterForm.selectedYears.length > 0) {
      params.years = filterForm.selectedYears.join(',')
    }

    const response = await performanceApi.getPerformanceStatistics(params)

    if (response.data && response.data.data) {
      // 检查数据是否为空或全为0
      const hasData = response.data.data.length > 0 && response.data.data.some(item => 
        (item.taskCompletion || 0) > 0 || 
        (item.quality || 0) > 0 || 
        (item.efficiency || 0) > 0 ||
        (item.taskCompletionRate || 0) > 0 ||
        (item.qualityScore || 0) > 0 ||
        (item.efficiencyScore || 0) > 0
      );
      
      if (hasData) {
        chartData.value = response.data.data
        renderChart()
      } else {
        chartData.value = []
        tableData.value = []
        ElMessage.info($t('reportsPerformance.noData'))
      }
    } else {
      chartData.value = []
      tableData.value = []
      ElMessage.info($t('reportsPerformance.noData'))
    }
  } catch (error) {
    console.error('加载报表失败:', error)
    chartData.value = []
    tableData.value = []
    ElMessage.error($t('reportsPerformance.loadFailed'))
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  // 检查是否有数据
  if (!chartRef.value || !chartData.value || chartData.value.length === 0) {
    return
  }

  // 检查数据是否全为0
  const hasNonZeroData = chartData.value.some(item => 
    (item.taskCompletion || 0) > 0 || 
    (item.quality || 0) > 0 || 
    (item.efficiency || 0) > 0 ||
    (item.taskCompletionRate || 0) > 0 ||
    (item.qualityScore || 0) > 0 ||
    (item.efficiencyScore || 0) > 0
  );

  // 如果没有非零数据，则不渲染图表
  if (!hasNonZeroData) {
    chartData.value = [];
    tableData.value = [];
    return;
  }

  // 初始化或获取图表实例
  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  // 根据报表类型准备图表数据
  let xAxisData = []
  let taskCompletionRate = [] // 任务完成率
  let qualityScore = [] // 质量评分
  let efficiencyScore = [] // 效率评分

  // 对应的数量数据
  let taskCompletionCount = [] // 任务完成数量
  let qualityCount = [] // 质量评分数量
  let efficiencyCount = [] // 效率评分数量
  let totalCount = [] // 总数量

  switch (filterForm.reportType) {
    case 'monthly':
      // 月度报表 - 按周显示
      const weeks = [$t('reportsPerformance.week1'), $t('reportsPerformance.week2'), $t('reportsPerformance.week3'), $t('reportsPerformance.week4')]
      xAxisData = weeks

      // 如果返回的数据是按日统计的，需要转换为按周统计
      if (chartData.value[0] && chartData.value[0].date) {
        // 按周聚合数据
        const weeklyData = Array(4).fill().map(() => ({
          taskCompletion: 0,
          quality: 0,
          efficiency: 0,
          total: 0
        }))

        chartData.value.forEach(item => {
          const date = new Date(item.date)
          const weekIndex = getWeekInMonth(date) - 1
          if (weekIndex >= 0 && weekIndex < 4) {
            weeklyData[weekIndex].taskCompletion += item.taskCompletion || 0
            weeklyData[weekIndex].quality += item.quality || 0
            weeklyData[weekIndex].efficiency += item.efficiency || 0
            weeklyData[weekIndex].total += (item.taskCompletion || 0) + (item.quality || 0) + (item.efficiency || 0)
          }
        })

        // 计算百分比和数量
        weeklyData.forEach(week => {
          const total = week.total
          totalCount.push(total)
          taskCompletionCount.push(week.taskCompletion)
          qualityCount.push(week.quality)
          efficiencyCount.push(week.efficiency)

          taskCompletionRate.push(total > 0 ? Math.round((week.taskCompletion / total) * 10000) / 100 : 0)
          qualityScore.push(total > 0 ? Math.round((week.quality / total) * 10000) / 100 : 0)
          efficiencyScore.push(total > 0 ? Math.round((week.efficiency / total) * 10000) / 100 : 0)
        })
      } else {
        // 如果后端直接返回了周数据
        chartData.value.forEach(item => {
          const total = (item.taskCompletion || 0) + (item.quality || 0) + (item.efficiency || 0)
          totalCount.push(total)
          taskCompletionCount.push(item.taskCompletion || 0)
          qualityCount.push(item.quality || 0)
          efficiencyCount.push(item.efficiency || 0)

          taskCompletionRate.push(total > 0 ? Math.round(((item.taskCompletion || 0) / total) * 10000) / 100 : 0)
          qualityScore.push(total > 0 ? Math.round(((item.quality || 0) / total) * 10000) / 100 : 0)
          efficiencyScore.push(total > 0 ? Math.round(((item.efficiency || 0) / total) * 10000) / 100 : 0)
        })
      }
      break

    case 'quarterly':
      // 季度报表 - 显示选择的季度
      xAxisData = filterForm.selectedQuarters.map(q => {
        const [year, quarter] = q.split('-')
        return `${year} ${$t('reportsPerformance.' + quarter.replace('Q', 'quarter'))}`
      })

      // 如果后端返回的是按季度统计的数据
      if (chartData.value && chartData.value.length > 0 && chartData.value[0].quarter) {
        // 创建季度数据映射
        const quarterDataMap = {}
        chartData.value.forEach(item => {
          quarterDataMap[`${item.year}-Q${item.quarter}`] = item
        })

        // 按选择的季度排序并获取数据
        filterForm.selectedQuarters.forEach(selectedQuarter => {
          const [year, quarter] = selectedQuarter.split('-')
          const quarterNum = parseInt(quarter.replace('Q', ''))
          const key = `${year}-Q${quarterNum}`
          
          const item = quarterDataMap[key] || {
            year,
            quarter: quarterNum,
            taskCompletion: 0,
            quality: 0,
            efficiency: 0,
            total: 0
          }

          const total = (item.taskCompletion || 0) + (item.quality || 0) + (item.efficiency || 0)
          totalCount.push(total)
          taskCompletionCount.push(item.taskCompletion || 0)
          qualityCount.push(item.quality || 0)
          efficiencyCount.push(item.efficiency || 0)

          taskCompletionRate.push(total > 0 ? Math.round(((item.taskCompletion || 0) / total) * 10000) / 100 : 0)
          qualityScore.push(total > 0 ? Math.round(((item.quality || 0) / total) * 10000) / 100 : 0)
          efficiencyScore.push(total > 0 ? Math.round(((item.efficiency || 0) / total) * 10000) / 100 : 0)
        })
      } else if (chartData.value && chartData.value.length > 0 && chartData.value[0].month) {
        // 如果后端返回的是按月统计数据，需要转换为按季度统计
        
        // 创建月度数据映射
        const monthDataMap = {}
        chartData.value.forEach(item => {
          monthDataMap[item.month] = item
        })

        // 处理每个选择的季度
        filterForm.selectedQuarters.forEach(selectedQuarter => {
          const [year, quarter] = selectedQuarter.split('-')
          const quarterNum = parseInt(quarter.replace('Q', ''))

          // 获取该季度的三个月份
          let monthsInQuarter = []
          switch (quarterNum) {
            case 1: monthsInQuarter = ['01', '02', '03']; break
            case 2: monthsInQuarter = ['04', '05', '06']; break
            case 3: monthsInQuarter = ['07', '08', '09']; break
            case 4: monthsInQuarter = ['10', '11', '12']; break
          }

          // 聚合该季度的数据
          const aggregated = {
            taskCompletion: 0,
            quality: 0,
            efficiency: 0,
            total: 0
          }

          monthsInQuarter.forEach(month => {
            const key = `${year}-${month}`
            const item = monthDataMap[key]
            if (item) {
              aggregated.taskCompletion += item.taskCompletion || 0
              aggregated.quality += item.quality || 0
              aggregated.efficiency += item.efficiency || 0
              aggregated.total += (item.taskCompletion || 0) + (item.quality || 0) + (item.efficiency || 0)
            }
          })

          totalCount.push(aggregated.total)
          taskCompletionCount.push(aggregated.taskCompletion)
          qualityCount.push(aggregated.quality)
          efficiencyCount.push(aggregated.efficiency)

          taskCompletionRate.push(aggregated.total > 0 ? Math.round((aggregated.taskCompletion / aggregated.total) * 10000) / 100 : 0)
          qualityScore.push(aggregated.total > 0 ? Math.round((aggregated.quality / aggregated.total) * 10000) / 100 : 0)
          efficiencyScore.push(aggregated.total > 0 ? Math.round((aggregated.efficiency / aggregated.total) * 10000) / 100 : 0)
        })
      } else {
        // 默认数据（当没有数据或数据格式不正确时）
        filterForm.selectedQuarters.forEach(selectedQuarter => {
          const [year, quarter] = selectedQuarter.split('-')
          xAxisData.push(`${year} ${$t('reportsPerformance.' + quarter.replace('Q', 'quarter'))}`)
          
          totalCount.push(0)
          taskCompletionCount.push(0)
          qualityCount.push(0)
          efficiencyCount.push(0)
          
          taskCompletionRate.push(0)
          qualityScore.push(0)
          efficiencyScore.push(0)
        })
      }
      break

    case 'yearly':
      // 年度报表 - 显示选择的年份
      xAxisData = [...filterForm.selectedYears].sort()

      if (chartData.value[0] && chartData.value[0].year) {
        // 按选择的年份排序
        const sortedData = xAxisData.map(selectedYear => {
          const data = chartData.value.find(d => d.year == selectedYear)
          return data || {
            year: selectedYear,
            taskCompletion: 0,
            quality: 0,
            efficiency: 0,
            total: 0
          }
        })

        sortedData.forEach(item => {
          const total = (item.taskCompletion || 0) + (item.quality || 0) + (item.efficiency || 0)
          totalCount.push(total)
          taskCompletionCount.push(item.taskCompletion || 0)
          qualityCount.push(item.quality || 0)
          efficiencyCount.push(item.efficiency || 0)

          taskCompletionRate.push(total > 0 ? Math.round(((item.taskCompletion || 0) / total) * 10000) / 100 : 0)
          qualityScore.push(total > 0 ? Math.round(((item.quality || 0) / total) * 10000) / 100 : 0)
          efficiencyScore.push(total > 0 ? Math.round(((item.efficiency || 0) / total) * 10000) / 100 : 0)
        })
      } else {
        // 默认处理
        xAxisData = chartData.value.map(item => {
          if (item.date) return item.date
          if (item.month) return item.month
          if (item.week) return $t('reportsPerformance.week') + item.week
          return ''
        })

        chartData.value.forEach(item => {
          const total = (item.taskCompletion || 0) + (item.quality || 0) + (item.efficiency || 0)
          totalCount.push(total)
          taskCompletionCount.push(item.taskCompletion || 0)
          qualityCount.push(item.quality || 0)
          efficiencyCount.push(item.efficiency || 0)

          taskCompletionRate.push(total > 0 ? Math.round(((item.taskCompletion || 0) / total) * 10000) / 100 : 0)
          qualityScore.push(total > 0 ? Math.round(((item.quality || 0) / total) * 10000) / 100 : 0)
          efficiencyScore.push(total > 0 ? Math.round(((item.efficiency || 0) / total) * 10000) / 100 : 0)
        })
      }
      break

    default:
      // 默认按日期显示
      xAxisData = chartData.value.map(item => {
        if (item.date) return item.date
        if (item.month) return item.month
        if (item.week) return $t('reportsPerformance.week') + item.week
        return ''
      })

      chartData.value.forEach(item => {
        const total = (item.taskCompletion || 0) + (item.quality || 0) + (item.efficiency || 0)
        totalCount.push(total)
        taskCompletionCount.push(item.taskCompletion || 0)
        qualityCount.push(item.quality || 0)
        efficiencyCount.push(item.efficiency || 0)

        taskCompletionRate.push(total > 0 ? Math.round(((item.taskCompletion || 0) / total) * 10000) / 100 : 0)
        qualityScore.push(total > 0 ? Math.round(((item.quality || 0) / total) * 10000) / 100 : 0)
        efficiencyScore.push(total > 0 ? Math.round(((item.efficiency || 0) / total) * 10000) / 100 : 0)
      })
  }

  // 检查处理后的数据是否全为0
  const hasValidChartData = taskCompletionRate.some(val => val > 0) || 
                           qualityScore.some(val => val > 0) || 
                           efficiencyScore.some(val => val > 0);

  // 如果处理后的数据也全为0，则不渲染图表
  if (!hasValidChartData) {
    chartData.value = [];
    tableData.value = [];
    return;
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
          // 显示原始值而不是调整后的值
          let value = param.value;
          if (param.seriesIndex === 1) {
            value = value - 20; // 质量评分调整值
          } else if (param.seriesIndex === 2) {
            value = value - 40; // 效率评分调整值
          }
          tooltip += `${param.marker} ${param.seriesName}: ${value.toFixed(2)}%<br/>`
        })
        return tooltip
      }
    },
    legend: {
      data: [
        $t('reportsPerformance.taskCompletionRate'),
        $t('reportsPerformance.qualityScore'),
        $t('reportsPerformance.efficiencyScore')
      ]
    },
    grid: {
      left: '5%',
      right: '5%',
      top: '15%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => {
          // 根据Y轴值显示正确的标签
          if (value <= 100) {
            return value + ' %';
          } else if (value <= 120) {
            return (value - 20) + ' %';
          } else {
            return (value - 40) + ' %';
          }
        }
      },
      splitLine: {
        show: true
      },
      min: 0,
      max: 140
    },
    series: [
      {
        name: $t('reportsPerformance.taskCompletionRate'),
        type: 'bar',
        barGap: 0,
        data: taskCompletionRate,
        itemStyle: { color: '#52c41a' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value.toFixed(2) + '%' : ''
          }
        }
      },
      {
        name: $t('reportsPerformance.qualityScore'),
        type: 'bar',
        barGap: 0,
        data: qualityScore.map(value => value + 20), // 将质量评分上移20个单位
        itemStyle: { color: '#faad14' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            // 显示原始值而不是调整后的值
            const originalValue = params.value - 20;
            return originalValue > 0 ? originalValue.toFixed(2) + '%' : '';
          }
        }
      },
      {
        name: $t('reportsPerformance.efficiencyScore'),
        type: 'bar',
        barGap: 0,
        data: efficiencyScore.map(value => value + 40), // 将效率评分上移40个单位
        itemStyle: { color: '#1890ff' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            // 显示原始值而不是调整后的值
            const originalValue = params.value - 40;
            return originalValue > 0 ? originalValue.toFixed(2) + '%' : '';
          }
        }
      }
    ]
  }

  // 渲染图表
  chartInstance.value.setOption(option)

  // 更新表格数据
  tableData.value = xAxisData.map((item, index) => ({
    category: item,
    taskCompletionRate: taskCompletionRate[index],
    qualityScore: qualityScore[index],
    efficiencyScore: efficiencyScore[index],
    taskCompletionCount: taskCompletionCount[index],
    qualityCount: qualityCount[index],
    efficiencyCount: efficiencyCount[index],
    totalCount: totalCount[index]
  }))

  // 窗口大小改变时重置图表大小
  window.addEventListener('resize', () => {
    if (chartInstance.value) {
      chartInstance.value.resize()
    }
  })
}

// 获取季度信息
const getQuarter = (month) => {
  return Math.floor((month - 1) / 3) + 1
}

// 获取月份在季度中的周数
const getWeekInMonth = (date) => {
  const firstDay = new Date(date.getFullYear(), date.getMonth(), 1)
  const firstDayWeek = firstDay.getDay() || 7
  const day = date.getDate()
  return Math.ceil((day + firstDayWeek - 1) / 7)
}

// 导出为图片
const exportAsImage = () => {
  if (!chartInstance.value) {
    ElMessage.warning($t('reportsPerformance.noChartToExport'))
    return
  }

  const dataUrl = chartInstance.value.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff'
  })

  const link = document.createElement('a')
  link.download = `绩效报表_${new Date().toLocaleDateString()}.png`
  link.href = dataUrl
  link.click()

  ElMessage.success($t('reportsPerformance.exportImageSuccess'))
}

// 导出为Excel
const exportAsExcel = () => {
  if (!tableData.value || tableData.value.length === 0) {
    ElMessage.warning($t('reportsPerformance.noDataToExport'))
    return
  }

  // 创建Excel内容
  let excelContent = `${$t('reportsPerformance.category')},${$t('reportsPerformance.taskCompletionRate')},${$t('reportsPerformance.qualityScore')},${$t('reportsPerformance.efficiencyScore')},${$t('reportsPerformance.totalCount')}\n`

  tableData.value.forEach(row => {
    excelContent += `${row.category},${row.taskCompletionRate}% (${row.taskCompletionCount}),${row.qualityScore}% (${row.qualityCount}),${row.efficiencyScore}% (${row.efficiencyCount}),${row.totalCount}\n`
  })

  // 创建Blob对象
  const blob = new Blob(['\uFEFF' + excelContent], {
    type: 'application/vnd.ms-excel;charset=utf-8'
  })

  // 创建下载链接
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `绩效报表_${new Date().toLocaleDateString()}.csv`
  link.click()

  ElMessage.success($t('reportsPerformance.exportExcelSuccess'))
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
.reports-performance-container {
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
  height: 70%;
  min-height: 400px;
}

.data-table-container {
  height: 30%;
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