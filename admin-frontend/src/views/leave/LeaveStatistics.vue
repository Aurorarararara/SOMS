<template>
  <div class="leave-statistics-container">
    <div class="page-header">
      <h2 class="page-title">{{ $t('nav.leaveStatistics') }}</h2>
      <el-button type="primary" @click="exportData">
        <el-icon><Download /></el-icon>
        {{ $t('leaveStatistics.exportStatistics') }}
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6" v-for="stat in statsData" :key="stat.type">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" :class="stat.type">
                <el-icon size="24" :class="stat.icon"></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 统计表格 -->
    <el-card class="table-card">
      <div class="table-header">
        <h3>{{ $t('leaveStatistics.statisticsDetails') }}</h3>
        <el-form :model="filterForm" inline>
          <el-form-item :label="$t('leaveStatistics.statisticsPeriod') + ':'">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="monthrange"
              :range-separator="$t('common.to')"
              :start-placeholder="$t('leaveStatistics.startMonth')"
              :end-placeholder="$t('leaveStatistics.endMonth')"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadStatistics">{{ $t('common.search') }}</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="statistics" v-loading="loading" stripe>
        <el-table-column prop="employeeName" :label="$t('leaveStatistics.employeeName')" width="120" />
        <el-table-column prop="departmentName" :label="$t('leaveStatistics.department')" width="120" />
        <el-table-column prop="sickLeave" :label="$t('leaveStatistics.sickLeaveDays')" width="100" align="center" />
        <el-table-column prop="personalLeave" :label="$t('leaveStatistics.personalLeaveDays')" width="100" align="center" />
        <el-table-column prop="annualLeave" :label="$t('leaveStatistics.annualLeaveDays')" width="100" align="center" />
        <el-table-column prop="totalLeave" :label="$t('leaveStatistics.totalLeaveDays')" width="120" align="center" />
        <el-table-column prop="leaveRate" :label="$t('leaveStatistics.leaveRate')" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 
              'high-rate': row.leaveRate > 10, 
              'normal-rate': row.leaveRate <= 10
            }">
              {{ row.leaveRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.actions')" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetails(row)">{{ $t('leaveStatistics.viewDetails') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Download, User, Clock, DocumentRemove, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const { t: $t } = useI18n()

const loading = ref(false)
const statistics = ref([])

const statsData = computed(() => [
  { type: 'primary', icon: 'User', value: 156, label: $t('leaveStatistics.totalEmployees') },
  { type: 'warning', icon: 'Clock', value: 89, label: $t('leaveStatistics.monthlyLeaveCount') },
  { type: 'success', icon: 'DocumentRemove', value: 234, label: $t('leaveStatistics.totalLeaveDays') },
  { type: 'info', icon: 'TrendCharts', value: '8.5%', label: $t('leaveStatistics.averageLeaveRate') }
])

const filterForm = reactive({
  dateRange: []
})

const loadStatistics = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    statistics.value = [
      {
        employeeName: '张三',
        departmentName: '技术部',
        sickLeave: 3,
        personalLeave: 2,
        annualLeave: 5,
        totalLeave: 10,
        leaveRate: 8.5
      }
    ]
  } finally {
    loading.value = false
  }
}

const viewDetails = (row) => {
  ElMessage.info(`查看${row.employeeName}的详细请假记录`)
}

const exportData = () => {
  ElMessage.success('导出功能开发中...')
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.leave-statistics-container {
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

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.primary {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #f0c474);
}

.stat-icon.success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon.info {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.high-rate {
  color: #f56c6c;
  font-weight: 600;
}

.normal-rate {
  color: #67c23a;
  font-weight: 600;
}
</style>