<template>
  <div class="reports-attendance-container">
    <div class="page-header">
      <h2 class="page-title">考勤报表</h2>
      <el-button type="primary" @click="generateReport">
        <el-icon><Document /></el-icon>
        生成报表
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="报表类型:">
          <el-select v-model="filterForm.reportType">
            <el-option label="月度报表" value="monthly" />
            <el-option label="季度报表" value="quarterly" />
            <el-option label="年度报表" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item label="统计范围:">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="monthrange"
            range-separator="至"
            start-placeholder="开始月份"
            end-placeholder="结束月份"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReport">查询报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="chart-card">
      <div class="chart-container">
        <div class="chart-placeholder">
          <el-icon size="64" color="#c0c4cc"><TrendCharts /></el-icon>
          <p>考勤趋势图表展示区域</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Document, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const filterForm = reactive({
  reportType: 'monthly',
  dateRange: []
})

const loadReport = () => {
  ElMessage.success('报表加载功能开发中...')
}

const generateReport = () => {
  ElMessage.success('报表生成功能开发中...')
}
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
}

.page-title {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.chart-card {
  min-height: 400px;
}

.chart-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
}

.chart-placeholder {
  text-align: center;
  color: #c0c4cc;
}
</style>