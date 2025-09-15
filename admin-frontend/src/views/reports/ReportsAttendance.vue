<template>
  <div class="reports-attendance-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('reportsAttendance.attendanceReport') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportAsImage">
          <el-icon><Picture /></el-icon>
          {{ $t('reportsAttendance.exportAsImage') }}
        </el-button>
        <el-button type="success" @click="exportAsExcel">
          <el-icon><Document /></el-icon>
          {{ $t('reportsAttendance.exportAsExcel') }}
        </el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item :label="$t('reportsAttendance.reportType') + ':'">
          <el-select v-model="filterForm.reportType" @change="handleReportTypeChange">
            <el-option :label="$t('reportsAttendance.monthlyReport')" value="monthly" />
            <el-option :label="$t('reportsAttendance.quarterlyReport')" value="quarterly" />
            <el-option :label="$t('reportsAttendance.yearlyReport')" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('reportsAttendance.statisticsRange') + ':'">
          <template v-if="filterForm.reportType === 'monthly'">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              :range-separator="$t('common.to')"
              :start-placeholder="$t('reportsAttendance.startDate')"
              :end-placeholder="$t('reportsAttendance.endDate')"
            />
          </template>
          <template v-else-if="filterForm.reportType === 'quarterly'">
            <el-select v-model="filterForm.selectedQuarters" multiple :placeholder="$t('reportsAttendance.selectQuarters')" style="width: 300px">
              <el-option
                v-for="quarter in quarterOptions"
                :key="quarter.value"
                :label="quarter.label"
                :value="quarter.value">
              </el-option>
            </el-select>
          </template>
          <template v-else-if="filterForm.reportType === 'yearly'">
            <el-select v-model="filterForm.selectedYears" multiple :placeholder="$t('reportsAttendance.selectYears')" style="width: 300px">
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
          <el-button type="primary" @click="loadReport">{{ $t('reportsAttendance.queryReport') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="chart-card">
      <div class="chart-container" v-loading="loading">
        <div v-if="!chartData || chartData.length === 0" class="chart-placeholder">
          <el-icon size="64" color="#c0c4cc"><TrendCharts /></el-icon>
          <p>{{ $t('reportsAttendance.chartPlaceholder') }}</p>
        </div>
        <div v-else>
          <div ref="chartRef" class="chart-wrapper"></div>
          <div class="data-table-container">
            <el-table :data="tableData" style="width: 100%" size="small">
              <el-table-column prop="category" :label="$t('reportsAttendance.category')" />
              <el-table-column prop="attendanceRate" :label="$t('reportsAttendance.attendanceRate')">
                <template #default="scope">
                  {{ scope.row.attendanceRate }}% ({{ scope.row.attendanceCount }})
                </template>
              </el-table-column>
              <el-table-column prop="absenceRate" :label="$t('reportsAttendance.absenceRate')">
                <template #default="scope">
                  {{ scope.row.absenceRate }}% ({{ scope.row.absenceCount }})
                </template>
              </el-table-column>
              <el-table-column prop="lateRate" :label="$t('reportsAttendance.lateRate')">
                <template #default="scope">
                  {{ scope.row.lateRate }}% ({{ scope.row.lateCount }})
                </template>
              </el-table-column>
              <el-table-column prop="earlyLeaveRate" :label="$t('reportsAttendance.earlyLeaveRate')">
                <template #default="scope">
                  {{ scope.row.earlyLeaveRate }}% ({{ scope.row.earlyLeaveCount }})
                </template>
              </el-table-column>
              <el-table-column prop="totalCount" :label="$t('reportsAttendance.totalCount')" />
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
import { attendanceStatsApi } from '@/api/attendanceStats'

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
        label: `${year} ${$t('reportsAttendance.quarter' + quarter)}`
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

    const response = await attendanceStatsApi.getAttendanceStatistics(params)

    if (response.data && response.data.data) {
      chartData.value = response.data.data
      renderChart()
    } else {
      chartData.value = []
      tableData.value = []
      ElMessage.warning($t('reportsAttendance.noData'))
    }
  } catch (error) {
    console.error('加载报表失败:', error)
    ElMessage.error($t('reportsAttendance.loadFailed'))
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value || !chartData.value || chartData.value.length === 0) {
    return
  }

  // 初始化或获取图表实例
  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  // 根据报表类型准备图表数据
  let xAxisData = []
  let attendanceRate = [] // 出勤率
  let absenceRate = [] // 缺勤率
  let lateRate = [] // 迟到率
  let earlyLeaveRate = [] // 早退率

  // 对应的人数数据
  let attendanceCount = [] // 出勤人数
  let absenceCount = [] // 缺勤人数
  let lateCount = [] // 迟到人数
  let earlyLeaveCount = [] // 早退人数
  let totalCount = [] // 总人数

  switch (filterForm.reportType) {
    case 'monthly':
      // 月度报表 - 按周显示
      const weeks = ['第1周', '第2周', '第3周', '第4周']
      xAxisData = weeks

      // 如果返回的数据是按日统计的，需要转换为按周统计
      if (chartData.value[0] && chartData.value[0].date) {
        // 按周聚合数据
        const weeklyData = Array(4).fill().map(() => ({
          normal: 0,
          late: 0,
          early: 0,
          absent: 0,
          total: 0
        }))

        chartData.value.forEach(item => {
          const date = new Date(item.date)
          const weekIndex = getWeekInMonth(date) - 1
          if (weekIndex >= 0 && weekIndex < 4) {
            weeklyData[weekIndex].normal += item.normal || 0
            weeklyData[weekIndex].late += item.late || 0
            weeklyData[weekIndex].early += item.early || 0
            weeklyData[weekIndex].absent += item.absent || 0
            weeklyData[weekIndex].total += (item.normal || 0) + (item.late || 0) + (item.early || 0) + (item.absent || 0)
          }
        })

        // 计算百分比和人数
        weeklyData.forEach(week => {
          const total = week.total
          totalCount.push(total)
          attendanceCount.push(week.normal)
          absenceCount.push(week.absent)
          lateCount.push(week.late)
          earlyLeaveCount.push(week.early)

          attendanceRate.push(total > 0 ? Math.round((week.normal / total) * 10000) / 100 : 0)
          absenceRate.push(total > 0 ? Math.round((week.absent / total) * 10000) / 100 : 0)
          lateRate.push(total > 0 ? Math.round((week.late / total) * 10000) / 100 : 0)
          earlyLeaveRate.push(total > 0 ? Math.round((week.early / total) * 10000) / 100 : 0)
        })
      } else {
        // 如果后端直接返回了周数据
        chartData.value.forEach(item => {
          const total = (item.normal || 0) + (item.late || 0) + (item.early || 0) + (item.absent || 0)
          totalCount.push(total)
          attendanceCount.push(item.normal || 0)
          absenceCount.push(item.absent || 0)
          lateCount.push(item.late || 0)
          earlyLeaveCount.push(item.early || 0)

          attendanceRate.push(total > 0 ? Math.round(((item.normal || 0) / total) * 10000) / 100 : 0)
          absenceRate.push(total > 0 ? Math.round(((item.absent || 0) / total) * 10000) / 100 : 0)
          lateRate.push(total > 0 ? Math.round(((item.late || 0) / total) * 10000) / 100 : 0)
          earlyLeaveRate.push(total > 0 ? Math.round(((item.early || 0) / total) * 10000) / 100 : 0)
        })
      }
      break

    case 'quarterly':
      // 季度报表 - 显示选择的季度
      xAxisData = filterForm.selectedQuarters.map(q => {
        const [year, quarter] = q.split('-')
        return `${year} ${$t('reportsAttendance.' + quarter.replace('Q', 'quarter'))}`
      })

      // 如果后端返回的是按季度统计的数据
      if (chartData.value[0] && chartData.value[0].quarter) {
        // 按选择的季度排序
        const sortedData = filterForm.selectedQuarters.map(selectedQuarter => {
          const [year, quarter] = selectedQuarter.split('-')
          const quarterNum = parseInt(quarter.replace('Q', ''))
          return chartData.value.find(d => d.year == year && d.quarter == quarterNum) || {
            year,
            quarter: quarterNum,
            normal: 0,
            late: 0,
            early: 0,
            absent: 0,
            total: 0
          }
        })

        sortedData.forEach(item => {
          const total = (item.normal || 0) + (item.late || 0) + (item.early || 0) + (item.absent || 0)
          totalCount.push(total)
          attendanceCount.push(item.normal || 0)
          absenceCount.push(item.absent || 0)
          lateCount.push(item.late || 0)
          earlyLeaveCount.push(item.early || 0)

          attendanceRate.push(total > 0 ? Math.round(((item.normal || 0) / total) * 10000) / 100 : 0)
          absenceRate.push(total > 0 ? Math.round(((item.absent || 0) / total) * 10000) / 100 : 0)
          lateRate.push(total > 0 ? Math.round(((item.late || 0) / total) * 10000) / 100 : 0)
          earlyLeaveRate.push(total > 0 ? Math.round(((item.early || 0) / total) * 10000) / 100 : 0)
        })
      } else {
        // 如果后端返回的是按月统计数据，需要转换为按季度统计
        filterForm.selectedQuarters.forEach(selectedQuarter => {
          const [year, quarter] = selectedQuarter.split('-')
          const quarterNum = parseInt(quarter.replace('Q', ''))

          // 查找该季度的数据
          const quarterMonths = chartData.value.filter(item => {
            if (item.month) {
              const itemYear = item.month.split('-')[0]
              const itemMonth = parseInt(item.month.split('-')[1])
              const itemQuarter = getQuarter(itemMonth)
              return itemYear == year && itemQuarter == quarterNum
            }
            return false
          })

          // 聚合该季度的数据
          const aggregated = {
            normal: 0,
            late: 0,
            early: 0,
            absent: 0,
            total: 0
          }

          quarterMonths.forEach(item => {
            aggregated.normal += item.normal || 0
            aggregated.late += item.late || 0
            aggregated.early += item.early || 0
            aggregated.absent += item.absent || 0
            aggregated.total += (item.normal || 0) + (item.late || 0) + (item.early || 0) + (item.absent || 0)
          })

          totalCount.push(aggregated.total)
          attendanceCount.push(aggregated.normal)
          absenceCount.push(aggregated.absent)
          lateCount.push(aggregated.late)
          earlyLeaveCount.push(aggregated.early)

          attendanceRate.push(aggregated.total > 0 ? Math.round((aggregated.normal / aggregated.total) * 10000) / 100 : 0)
          absenceRate.push(aggregated.total > 0 ? Math.round((aggregated.absent / aggregated.total) * 10000) / 100 : 0)
          lateRate.push(aggregated.total > 0 ? Math.round((aggregated.late / aggregated.total) * 10000) / 100 : 0)
          earlyLeaveRate.push(aggregated.total > 0 ? Math.round((aggregated.early / aggregated.total) * 10000) / 100 : 0)
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
            normal: 0,
            late: 0,
            early: 0,
            absent: 0,
            total: 0
          }
        })

        sortedData.forEach(item => {
          const total = (item.normal || 0) + (item.late || 0) + (item.early || 0) + (item.absent || 0)
          totalCount.push(total)
          attendanceCount.push(item.normal || 0)
          absenceCount.push(item.absent || 0)
          lateCount.push(item.late || 0)
          earlyLeaveCount.push(item.early || 0)

          attendanceRate.push(total > 0 ? Math.round(((item.normal || 0) / total) * 10000) / 100 : 0)
          absenceRate.push(total > 0 ? Math.round(((item.absent || 0) / total) * 10000) / 100 : 0)
          lateRate.push(total > 0 ? Math.round(((item.late || 0) / total) * 10000) / 100 : 0)
          earlyLeaveRate.push(total > 0 ? Math.round(((item.early || 0) / total) * 10000) / 100 : 0)
        })
      } else {
        // 默认处理
        xAxisData = chartData.value.map(item => {
          if (item.date) return item.date
          if (item.month) return item.month
          if (item.week) return $t('reportsAttendance.week') + item.week
          return ''
        })

        chartData.value.forEach(item => {
          const total = (item.normal || 0) + (item.late || 0) + (item.early || 0) + (item.absent || 0)
          totalCount.push(total)
          attendanceCount.push(item.normal || 0)
          absenceCount.push(item.absent || 0)
          lateCount.push(item.late || 0)
          earlyLeaveCount.push(item.early || 0)

          attendanceRate.push(total > 0 ? Math.round(((item.normal || 0) / total) * 10000) / 100 : 0)
          absenceRate.push(total > 0 ? Math.round(((item.absent || 0) / total) * 10000) / 100 : 0)
          lateRate.push(total > 0 ? Math.round(((item.late || 0) / total) * 10000) / 100 : 0)
          earlyLeaveRate.push(total > 0 ? Math.round(((item.early || 0) / total) * 10000) / 100 : 0)
        })
      }
      break

    default:
      // 默认按日期显示
      xAxisData = chartData.value.map(item => {
        if (item.date) return item.date
        if (item.month) return item.month
        if (item.week) return $t('reportsAttendance.week') + item.week
        return ''
      })

      chartData.value.forEach(item => {
        const total = (item.normal || 0) + (item.late || 0) + (item.early || 0) + (item.absent || 0)
        totalCount.push(total)
        attendanceCount.push(item.normal || 0)
        absenceCount.push(item.absent || 0)
        lateCount.push(item.late || 0)
        earlyLeaveCount.push(item.early || 0)

        attendanceRate.push(total > 0 ? Math.round(((item.normal || 0) / total) * 10000) / 100 : 0)
        absenceRate.push(total > 0 ? Math.round(((item.absent || 0) / total) * 10000) / 100 : 0)
        lateRate.push(total > 0 ? Math.round(((item.late || 0) / total) * 10000) / 100 : 0)
        earlyLeaveRate.push(total > 0 ? Math.round(((item.early || 0) / total) * 10000) / 100 : 0)
      })
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
        $t('reportsAttendance.attendanceRate'),
        $t('reportsAttendance.absenceRate'),
        $t('reportsAttendance.lateRate'),
        $t('reportsAttendance.earlyLeaveRate')
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
        name: $t('reportsAttendance.attendanceRate'),
        type: 'bar',
        barGap: '5%',
        data: attendanceRate,
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
        name: $t('reportsAttendance.absenceRate'),
        type: 'bar',
        barGap: '5%',
        data: absenceRate,
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
        name: $t('reportsAttendance.lateRate'),
        type: 'bar',
        barGap: '5%',
        data: lateRate,
        itemStyle: { color: '#faad14' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value > 0 ? params.value + '%' : ''
          }
        }
      },
      {
        name: $t('reportsAttendance.earlyLeaveRate'),
        type: 'bar',
        barGap: '5%',
        data: earlyLeaveRate,
        itemStyle: { color: '#1890ff' },
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
  chartInstance.value.setOption(option)

  // 更新表格数据
  tableData.value = xAxisData.map((item, index) => ({
    category: item,
    attendanceRate: attendanceRate[index],
    absenceRate: absenceRate[index],
    lateRate: lateRate[index],
    earlyLeaveRate: earlyLeaveRate[index],
    attendanceCount: attendanceCount[index],
    absenceCount: absenceCount[index],
    lateCount: lateCount[index],
    earlyLeaveCount: earlyLeaveCount[index],
    totalCount: totalCount[index]
  }))

  // 窗口大小改变时重置图表大小
  window.addEventListener('resize', () => {
    if (chartInstance.value) {
      chartInstance.value.resize()
    }
  })
}

// 导出为图片
const exportAsImage = () => {
  if (!chartInstance.value) {
    ElMessage.warning($t('reportsAttendance.noChartToExport'))
    return
  }

  const dataUrl = chartInstance.value.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff'
  })

  const link = document.createElement('a')
  link.download = `考勤报表_${new Date().toLocaleDateString()}.png`
  link.href = dataUrl
  link.click()

  ElMessage.success($t('reportsAttendance.exportImageSuccess'))
}

// 导出为Excel
const exportAsExcel = () => {
  if (!tableData.value || tableData.value.length === 0) {
    ElMessage.warning($t('reportsAttendance.noDataToExport'))
    return
  }

  // 创建Excel内容
  let excelContent = `${$t('reportsAttendance.category')},${$t('reportsAttendance.attendanceRate')},${$t('reportsAttendance.absenceRate')},${$t('reportsAttendance.lateRate')},${$t('reportsAttendance.earlyLeaveRate')},${$t('reportsAttendance.totalCount')}\n`

  tableData.value.forEach(row => {
    excelContent += `${row.category},${row.attendanceRate}% (${row.attendanceCount}),${row.absenceRate}% (${row.absenceCount}),${row.lateRate}% (${row.lateCount}),${row.earlyLeaveRate}% (${row.earlyLeaveCount}),${row.totalCount}\n`
  })

  // 创建Blob对象
  const blob = new Blob(['\uFEFF' + excelContent], {
    type: 'application/vnd.ms-excel;charset=utf-8'
  })

  // 创建下载链接
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `考勤报表_${new Date().toLocaleDateString()}.csv`
  link.click()

  ElMessage.success($t('reportsAttendance.exportExcelSuccess'))
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
.reports-attendance-container {
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