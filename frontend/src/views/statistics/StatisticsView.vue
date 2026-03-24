<template>
  <div class="statistics-page" v-loading="loading">
    <!-- 顶部：标题 + 导出 -->
    <div class="page-header">
      <h3>事务统计</h3>
      <div class="export-area">
        <el-date-picker
          v-model="exportPeriod"
          :type="exportType"
          :format="exportType === 'month' ? 'YYYY-MM' : 'YYYY'"
          :value-format="exportType === 'month' ? 'YYYY-MM' : 'YYYY'"
          placeholder="选择时间"
          style="width: 160px"
        />
        <el-radio-group v-model="exportType" size="small">
          <el-radio-button value="month">按月</el-radio-button>
          <el-radio-button value="year">按年</el-radio-button>
        </el-radio-group>
        <el-button type="primary" :loading="exporting" :disabled="!exportPeriod" @click="handleExport">
          <el-icon><Download /></el-icon>导出报表
        </el-button>
      </div>
    </div>

    <!-- 状态卡片 -->
    <div class="status-cards">
      <div class="stat-card" v-for="card in statusCards" :key="card.label" :style="{ '--card-color': card.color }">
        <div class="stat-card__icon">
          <el-icon :size="22"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-card__info">
          <span class="stat-card__value">{{ card.value }}</span>
          <span class="stat-card__label">{{ card.label }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 2x2 网格 -->
    <div class="charts-grid">
      <!-- 柱状图：部门事务 -->
      <div class="chart-card">
        <div class="chart-card__header">
          <h4>部门事务统计</h4>
          <span class="chart-card__sub">各部门已下达 / 已完成事务数量</span>
        </div>
        <VChart class="chart" :option="barOption" autoresize />
      </div>

      <!-- 折线图：月度趋势 -->
      <div class="chart-card">
        <div class="chart-card__header">
          <h4>月度事务趋势</h4>
          <span class="chart-card__sub">每月已下达 / 已完成事务数量</span>
        </div>
        <VChart class="chart" :option="lineOption" autoresize />
      </div>

      <!-- 饼图：紧急性分布 -->
      <div class="chart-card">
        <div class="chart-card__header">
          <h4>事务紧急性分布</h4>
          <span class="chart-card__sub">按四象限级别分布（A/B/C/D）</span>
        </div>
        <VChart class="chart" :option="pieOption" autoresize />
      </div>

      <!-- 综合信息卡 -->
      <div class="chart-card summary-card">
        <div class="chart-card__header">
          <h4>综合指标</h4>
          <span class="chart-card__sub">效率与完成情况</span>
        </div>
        <div class="summary-grid">
          <div class="summary-item">
            <span class="summary-item__value">{{ overview.completionRate }}%</span>
            <span class="summary-item__label">完成率</span>
          </div>
          <div class="summary-item">
            <span class="summary-item__value">{{ overview.overdueRate }}%</span>
            <span class="summary-item__label">逾期率</span>
          </div>
          <div class="summary-item">
            <span class="summary-item__value">{{ overview.avgResponseHours }}h</span>
            <span class="summary-item__label">平均响应时长</span>
          </div>
          <div class="summary-item">
            <span class="summary-item__value">{{ overview.avgCompletionHours }}h</span>
            <span class="summary-item__label">平均完成时长</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Document, CircleCheck, CircleClose, Clock, Delete } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { EChartsOption } from 'echarts'
import {
  getStatsOverviewApi, getStatsByDeptApi, getStatsByLevelApi, getMonthlyTrendApi, exportStatsExcel,
} from '@/api/statistics'
import { TASK_LEVEL_MAP } from '@/utils/constants'
import type { StatsOverview, DeptStats, LevelStats, TrendData } from '@/types'

use([CanvasRenderer, BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent])

const loading = ref(false)
const exporting = ref(false)
const exportType = ref<'month' | 'year'>('month')
const exportPeriod = ref('')

const overview = ref<StatsOverview>({
  totalTasks: 0, pendingTasks: 0, inProgressTasks: 0, completedTasks: 0,
  rejectedTasks: 0, overdueTasks: 0, cancelledTasks: 0,
  overdueRate: 0, completionRate: 0, avgResponseHours: 0, avgCompletionHours: 0,
})
const deptStats = ref<DeptStats[]>([])
const levelStats = ref<LevelStats[]>([])
const monthlyTrend = ref<TrendData>({ dates: [], created: [], completed: [] })

const statusCards = computed(() => [
  { label: '已下达', value: overview.value.totalTasks, color: '#4F6EF7', icon: Document },
  { label: '已完成', value: overview.value.completedTasks, color: '#22C55E', icon: CircleCheck },
  { label: '不通过', value: overview.value.rejectedTasks, color: '#EF4444', icon: CircleClose },
  { label: '已逾期', value: overview.value.overdueTasks, color: '#F59E0B', icon: Clock },
  { label: '已作废', value: overview.value.cancelledTasks, color: '#9CA3AF', icon: Delete },
])

// 柱状图
const barOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['已下达', '已完成'], top: 4, right: 16 },
  grid: { left: 50, right: 20, top: 40, bottom: 30 },
  xAxis: {
    type: 'category',
    data: deptStats.value.map(d => d.deptName),
    axisLabel: { interval: 0, rotate: deptStats.value.length > 6 ? 30 : 0 },
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    { name: '已下达', type: 'bar', data: deptStats.value.map(d => d.total), barMaxWidth: 36, itemStyle: { color: '#4F6EF7', borderRadius: [4, 4, 0, 0] } },
    { name: '已完成', type: 'bar', data: deptStats.value.map(d => d.completed), barMaxWidth: 36, itemStyle: { color: '#22C55E', borderRadius: [4, 4, 0, 0] } },
  ],
}))

// 折线图
const lineOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['已下达', '已完成'], top: 4, right: 16 },
  grid: { left: 50, right: 20, top: 40, bottom: 30 },
  xAxis: { type: 'category', data: monthlyTrend.value.dates, boundaryGap: false },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    { name: '已下达', type: 'line', data: monthlyTrend.value.created, smooth: true, itemStyle: { color: '#4F6EF7' }, areaStyle: { color: 'rgba(79,110,247,0.08)' } },
    { name: '已完成', type: 'line', data: monthlyTrend.value.completed, smooth: true, itemStyle: { color: '#22C55E' }, areaStyle: { color: 'rgba(34,197,94,0.08)' } },
  ],
}))

// 饼图
const levelColors: Record<string, string> = { A: '#EF4444', B: '#F59E0B', C: '#4F6EF7', D: '#22C55E' }
const pieOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', right: 16, top: 'center' },
  series: [{
    type: 'pie',
    radius: ['42%', '70%'],
    center: ['40%', '50%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { show: true, formatter: '{b}\n{d}%' },
    data: levelStats.value.map(l => ({
      name: TASK_LEVEL_MAP[l.level as keyof typeof TASK_LEVEL_MAP]?.label || l.level,
      value: l.total,
      itemStyle: { color: levelColors[l.level] || '#999' },
    })),
  }],
}))

async function handleExport() {
  if (!exportPeriod.value) return
  exporting.value = true
  try {
    await exportStatsExcel(exportPeriod.value)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const [overviewRes, deptRes, levelRes, trendRes] = await Promise.all([
      getStatsOverviewApi(),
      getStatsByDeptApi(),
      getStatsByLevelApi(),
      getMonthlyTrendApi(),
    ])
    overview.value = overviewRes.data
    deptStats.value = deptRes.data
    levelStats.value = levelRes.data
    monthlyTrend.value = trendRes.data
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.statistics-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: $spacing-md;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  h3 { font-size: 20px; font-weight: 600; margin: 0; }
  .export-area { display: flex; align-items: center; gap: $spacing-sm; }
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $spacing-md;
  flex-shrink: 0;
}

.stat-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  border-left: 3px solid var(--card-color);
  transition: box-shadow 0.2s;
  &:hover { box-shadow: $shadow-hover; }

  &__icon {
    width: 44px; height: 44px;
    border-radius: $radius-sm;
    background: color-mix(in srgb, var(--card-color) 12%, transparent);
    color: var(--card-color);
    display: flex; align-items: center; justify-content: center;
  }
  &__info { display: flex; flex-direction: column; }
  &__value { font-size: 26px; font-weight: 700; color: $text-primary; line-height: 1.2; }
  &__label { font-size: $font-size-xs; color: $text-secondary; margin-top: 2px; }
}

.charts-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: $spacing-md;
}

.chart-card {
  background: $bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
  padding: $spacing-md;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &__header {
    flex-shrink: 0;
    margin-bottom: $spacing-sm;
    h4 { font-size: 15px; font-weight: 600; margin: 0; color: $text-primary; }
    .chart-card__sub { font-size: $font-size-xs; color: $text-secondary; }
  }

  .chart {
    flex: 1;
    min-height: 0;
    width: 100%;
  }
}

.summary-card {
  .summary-grid {
    flex: 1;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
    gap: $spacing-md;
    align-content: center;
  }
  .summary-item {
    background: $bg-page;
    border-radius: $radius-sm;
    padding: $spacing-lg;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    &__value {
      font-size: 28px;
      font-weight: 700;
      color: $primary;
      line-height: 1.2;
    }
    &__label {
      font-size: 13px;
      color: $text-secondary;
      margin-top: $spacing-xs;
    }
  }
}
</style>
