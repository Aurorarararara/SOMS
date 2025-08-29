<template>
  <div class="reports-performance-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('reportsPerformance.performanceReport') }}</h2>
      <el-button type="primary">
        <el-icon><Document /></el-icon>
        {{ $t('reportsPerformance.generatePerformanceReport') }}
      </el-button>
    </div>

    <el-card>
      <div class="chart-placeholder">
        <el-icon size="64" color="#c0c4cc"><DataAnalysis /></el-icon>
        <p>{{ $t('reportsPerformance.chartPlaceholder') }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { Document, DataAnalysis } from '@element-plus/icons-vue'

const { t: $t } = useI18n()
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
}

.page-title {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.chart-placeholder {
  text-align: center;
  color: #c0c4cc;
  padding: 100px 0;
}
</style>