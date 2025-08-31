<template>
  <div class="task-statistics-page">
    <div class="page-header">
      <h1>任务统计分析</h1>
      <p>查看全局任务统计和分析报告</p>
    </div>

    <!-- 引入统计组件 -->
    <TaskStatistics />
  </div>
</template>

<script setup>
import TaskStatistics from '@/components/TaskStatistics.vue'
</script>

<style scoped>
.task-statistics-page {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}
</style>
