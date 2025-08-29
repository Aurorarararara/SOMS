<template>
  <div class="reports-attendance-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('reportsAttendance.attendanceReport') }}</h2>
      <el-button type="primary" @click="generateReport">
        <el-icon><Document /></el-icon>
        {{ $t('reportsAttendance.generateReport') }}
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item :label="$t('reportsAttendance.reportType') + ':'">
          <el-select v-model="filterForm.reportType">
            <el-option :label="$t('reportsAttendance.monthlyReport')" value="monthly" />
            <el-option :label="$t('reportsAttendance.quarterlyReport')" value="quarterly" />
            <el-option :label="$t('reportsAttendance.yearlyReport')" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('reportsAttendance.statisticsRange') + ':'">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="monthrange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('reportsAttendance.startMonth')"
            :end-placeholder="$t('reportsAttendance.endMonth')"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReport">{{ $t('reportsAttendance.queryReport') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="chart-card">
      <div class="chart-container">
        <div class="chart-placeholder">
          <el-icon size="64" color="#c0c4cc"><TrendCharts /></el-icon>
          <p>{{ $t('reportsAttendance.chartPlaceholder') }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { Document, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const { t: $t } = useI18n()

const filterForm = reactive({
  reportType: 'monthly',
  dateRange: []
})

const loadReport = () => {
  ElMessage.success($t('reportsAttendance.reportLoadingInProgress'))
}

const generateReport = () => {
  ElMessage.success($t('reportsAttendance.reportGenerationInProgress'))
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