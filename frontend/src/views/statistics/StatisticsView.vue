<template>
  <div class="statistics-page" v-loading="loading">
    <!-- 顶部：标题 + 导出 -->
    <div class="page-header">
      <h4 class="section-title">事务状态</h4>
    </div>

    <!-- 1. 事务状态卡片 -->
    <section class="section-status">
        <div class="status-cards">
          <div class="stat-card stat-card--primary" :style="{ '--card-color': '#4F6EF7' }">
            <span class="stat-card__left">下达事务数：<strong>{{ totalAssigned }}</strong></span>
            <span class="stat-card__right">占比：100%</span>
          </div>
          <div v-for="card in statusCards" :key="card.label" class="stat-card" :style="{ '--card-color': card.color }">
            <span class="stat-card__left">{{ card.label }}：<strong>{{ card.value }}</strong></span>
            <span class="stat-card__right">占比：{{ card.pct }}</span>
          </div>
        </div>
      </section>

      <!-- 2. 事务紧急程度 -->
      <section class="section-urgency">
        <h4 class="section-title">事务级别</h4>
        <div class="urgency-cards">
          <div v-for="item in urgencyCards" :key="item.level" class="urgency-card"
            :style="{ '--uc-color': item.color, '--uc-bg': item.bg }">
            <div class="urgency-card__top">
              <span class="urgency-card__level">{{ item.label }}</span>
              <div class="urgency-card__stats">
                <span>下达 <strong>{{ item.assigned }}</strong></span>
                <span>完成 <strong>{{ item.completed }}</strong></span>
                <span>未通过 <strong>{{ item.failedReview }}</strong></span>
                <span>待办 <strong>{{ item.todo }}</strong></span>
              </div>
            </div>
            <div class="urgency-card__bottom">
              <div class="urgency-card__metric">
                <span class="urgency-card__metric-label">平均完成时长</span>
                <strong>{{ item.avgCompletionHours }}h</strong>
              </div>
              <div class="urgency-card__metric">
                <span class="urgency-card__metric-label">平均逾期时长</span>
                <strong>{{ item.avgOverdueHours }}h</strong>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 3 & 4. 部门事务 + 人员事务 -->
      <section class="section-charts-row">
        <div class="chart-col">
          <h4 class="section-title">部门事务</h4>
          <div class="chart-card">
            <VChart class="chart" :option="deptBarOption" autoresize />
          </div>
        </div>
        <div class="chart-col">
          <h4 class="section-title">人员事务</h4>
          <div class="chart-card">
            <VChart class="chart" :option="personBarOption" autoresize />
          </div>
        </div>
      </section>

      <!-- 5. 每日事务 + 每月事务 并排 -->
      <section class="section-time-charts-row">
        <div class="time-chart-col">
          <div class="section-time-header">
            <h4 class="section-title">每日事务</h4>
            <div class="line-month-selector">
              <el-button size="small" :type="lineMonthMode === 'prev' ? 'primary' : ''" @click="setLineMonth('prev')">上月</el-button>
              <el-button size="small" :type="lineMonthMode === 'current' ? 'primary' : ''" @click="setLineMonth('current')">本月</el-button>
              <el-button size="small" :type="lineMonthMode === 'next' ? 'primary' : ''" @click="setLineMonth('next')">次月</el-button>
              <el-date-picker v-model="lineCustomMonth" type="month" placeholder="自定义" size="small"
                style="width: 110px; margin-left: 4px" @change="onCustomMonthChange" />
            </div>
          </div>
          <div class="chart-card chart-card--full">
            <VChart class="chart" :option="timeLineOption" autoresize />
          </div>
        </div>
        <div class="time-chart-col">
          <div class="section-time-header">
            <h4 class="section-title">每月事务</h4>
            <div class="line-month-selector">
              <el-button size="small" :type="yearMode === 'prev' ? 'primary' : ''" @click="setYear('prev')">上年</el-button>
              <el-button size="small" :type="yearMode === 'current' ? 'primary' : ''" @click="setYear('current')">本年</el-button>
              <el-button size="small" :type="yearMode === 'next' ? 'primary' : ''" @click="setYear('next')">次年</el-button>
              <el-date-picker v-model="yearCustom" type="year" placeholder="自定义" size="small"
                style="width: 100px; margin-left: 4px" @change="onCustomYearChange" />
            </div>
          </div>
          <div class="chart-card chart-card--full">
            <VChart class="chart" :option="monthlyLineOption" autoresize />
          </div>
        </div>
      </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent, DataZoomComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import {
  getStatsOverviewApi, getStatsByPersonApi, exportStatsExcel,
} from '@/api/statistics'
import { getTaskListApi } from '@/api/task'
import type { StatsOverview, PersonStats, Task } from '@/types'

use([CanvasRenderer, BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent, DataZoomComponent])

// ==================== 基础状态 ====================
const loading = ref(false)
const exporting = ref(false)
const exportType = ref<'month' | 'year'>('month')
const exportPeriod = ref('')

const overview = ref<StatsOverview>({
  totalTasks: 0, pendingTasks: 0, inProgressTasks: 0, completedTasks: 0,
  rejectedTasks: 0, overdueTasks: 0, cancelledTasks: 0,
  overdueRate: 0, completionRate: 0, avgResponseHours: 0, avgCompletionHours: 0,
  onTimeCompleted: 0, overdueCompleted: 0, failedReview: 0,
  todoTasks: 0, overdueUnfinished: 0, submittedTasks: 0,
})
const personStats = ref<PersonStats[]>([])
const allTasks = ref<Task[]>([])

// ==================== 1. 事务状态卡片 ====================
const totalAssigned = computed(() => {
  const ov = overview.value
  return ov.onTimeCompleted + ov.overdueCompleted + ov.failedReview + ov.todoTasks + ov.overdueUnfinished + ov.cancelledTasks
})

function pctStr(val: number) {
  if (!totalAssigned.value) return '0%'
  return (val / totalAssigned.value * 100).toFixed(1) + '%'
}

const statusCards = computed(() => [
  { label: '按时完成数', value: overview.value.onTimeCompleted, color: '#22C55E', pct: pctStr(overview.value.onTimeCompleted) },
  { label: '逾期完成数', value: overview.value.overdueCompleted, color: '#F59E0B', pct: pctStr(overview.value.overdueCompleted) },
  { label: '审核未通过', value: overview.value.failedReview, color: '#EF4444', pct: pctStr(overview.value.failedReview) },
  { label: '待办事务数', value: overview.value.todoTasks, color: '#4F6EF7', pct: pctStr(overview.value.todoTasks) },
  { label: '逾期未完成', value: overview.value.overdueUnfinished, color: '#8B5CF6', pct: pctStr(overview.value.overdueUnfinished) },
  { label: '作废事务数', value: overview.value.cancelledTasks, color: '#9CA3AF', pct: pctStr(overview.value.cancelledTasks) },
])

// ==================== 2. 紧急程度卡片 ====================
function classifyTask(task: Task) {
  const status = task.status
  const deadline = task.completionDeadline ? new Date(task.completionDeadline) : null
  const completionTime = task.completionTime ? new Date(task.completionTime) : null
  const now = new Date()
  if (['approved', 'completed'].includes(status)) {
    if (!deadline || (completionTime && completionTime <= deadline)) return 'onTimeCompleted'
    return 'overdueCompleted'
  }
  if (status === 'rejected') return 'failedReview'
  if (status === 'cancelled') return 'cancelled'
  if (deadline && deadline < now) return 'overdueUnfinished'
  return 'todoTasks'
}

const urgencyCards = computed(() => {
  const levels = [
    { level: 'A', label: '紧急重要', color: '#EF4444', bg: '#FEF2F2' },
    { level: 'B', label: '紧急不重要', color: '#F59E0B', bg: '#FFFBEB' },
    { level: 'C', label: '不紧急重要', color: '#4F6EF7', bg: '#EEF1FE' },
    { level: 'D', label: '不紧急不重要', color: '#22C55E', bg: '#ECFDF5' },
  ]

  return levels.map(l => {
    const tasks = allTasks.value.filter(t => t.level === l.level)
    const classified = tasks.map(t => ({ task: t, cat: classifyTask(t) }))

    const assigned = tasks.length
    const completed = classified.filter(c => c.cat === 'onTimeCompleted' || c.cat === 'overdueCompleted').length
    const failedReview = classified.filter(c => c.cat === 'failedReview').length
    const todo = classified.filter(c => c.cat === 'todoTasks').length

    // 平均完成时长（小时）
    const completedTasks = tasks.filter(t =>
      ['approved', 'completed'].includes(t.status) && t.completionTime && t.createdAt
    )
    let avgCompletionHours = 0
    if (completedTasks.length) {
      const totalHours = completedTasks.reduce((sum, t) => {
        return sum + (new Date(t.completionTime!).getTime() - new Date(t.createdAt).getTime()) / 3600000
      }, 0)
      avgCompletionHours = Math.round(totalHours / completedTasks.length * 10) / 10
    }

    // 平均逾期时长（小时）
    const overdueTasks = tasks.filter(t => {
      const cat = classifyTask(t)
      return cat === 'overdueCompleted' || cat === 'overdueUnfinished'
    })
    let avgOverdueHours = 0
    if (overdueTasks.length) {
      const now = new Date()
      const totalHours = overdueTasks.reduce((sum, t) => {
        const deadline = new Date(t.completionDeadline)
        const end = t.completionTime ? new Date(t.completionTime) : now
        return sum + Math.max(0, (end.getTime() - deadline.getTime()) / 3600000)
      }, 0)
      avgOverdueHours = Math.round(totalHours / overdueTasks.length * 10) / 10
    }

    return { ...l, assigned, completed, failedReview, todo, avgCompletionHours, avgOverdueHours }
  })
})

// ==================== 3. 部门柱状堆叠图 ====================
const STACKED_SERIES = [
  { key: 'onTimeCompleted', name: '按时完成', color: '#4dbb87' },
  { key: 'overdueCompleted', name: '逾期完成', color: '#f3a64b' },
  { key: 'failedReview', name: '审核未通过', color: '#e06472' },
  { key: 'todoTasks', name: '待办事务', color: '#5a8dee' },
  { key: 'overdueUnfinished', name: '逾期未完成', color: '#8B5CF6' },
  { key: 'cancelledTasks', name: '已作废', color: '#96a0af' },
] as const

const deptData = computed(() => {
  const deptMap = new Map<string, Record<string, number>>()
  for (const p of personStats.value) {
    if (!deptMap.has(p.deptName)) {
      deptMap.set(p.deptName, { onTimeCompleted: 0, overdueCompleted: 0, failedReview: 0, todoTasks: 0, overdueUnfinished: 0, cancelledTasks: 0 })
    }
    const entry = deptMap.get(p.deptName)!
    entry.onTimeCompleted += p.onTimeCompleted
    entry.overdueCompleted += p.overdueCompleted
    entry.failedReview += p.failedReview
    entry.todoTasks += p.todoTasks
    entry.overdueUnfinished += p.overdueUnfinished
    entry.cancelledTasks += p.cancelledTasks
  }
  return Array.from(deptMap.entries()).map(([name, stats]) => ({ label: name, ...stats }))
})

function makeHorizontalStackedOption(source: { label: string; [k: string]: any }[]): EChartsOption {
  const dataLen = source.length
  const maxVisible = 8
  const needScroll = dataLen > maxVisible
  const endPercent = needScroll ? Math.round((maxVisible / dataLen) * 100) : 100

  return {
    grid: { top: 30, right: needScroll ? 30 : 16, bottom: 6, left: 12, containLabel: true },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(36, 50, 74, 0.92)',
      borderWidth: 0,
      textStyle: { color: '#fff' },
    },
    legend: {
      right: 0, top: 0, itemWidth: 10, itemHeight: 10,
      textStyle: { color: '#708097', fontSize: 12 },
    },
    yAxis: {
      type: 'category',
      data: source.map(item => item.label),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#dbe4f0' } },
      axisLabel: { color: '#607087', fontSize: 12, interval: 0 },
      inverse: true,
    },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(141, 159, 182, 0.18)' } },
      axisLabel: { color: '#8391a4', fontSize: 12 },
    },
    dataZoom: needScroll
      ? [
          { type: 'slider', yAxisIndex: 0, start: 0, end: endPercent, right: 0, width: 14 },
          { type: 'inside', yAxisIndex: 0, start: 0, end: endPercent },
        ]
      : [],
    series: STACKED_SERIES.map(s => ({
      name: s.name,
      type: 'bar' as const,
      stack: 'total',
      barMaxWidth: 28,
      itemStyle: { color: s.color },
      data: source.map(item => item[s.key] ?? 0),
    })),
  }
}

const deptBarOption = computed<EChartsOption>(() => makeHorizontalStackedOption(deptData.value))

// ==================== 4. 人员柱状堆叠图 ====================
const personData = computed(() =>
  personStats.value.map(p => ({
    label: p.name,
    onTimeCompleted: p.onTimeCompleted,
    overdueCompleted: p.overdueCompleted,
    failedReview: p.failedReview,
    todoTasks: p.todoTasks,
    overdueUnfinished: p.overdueUnfinished,
    cancelledTasks: p.cancelledTasks,
  }))
)

const personBarOption = computed<EChartsOption>(() => makeHorizontalStackedOption(personData.value))

// ==================== 5. 时间折线图 ====================
const lineMonthMode = ref<'prev' | 'current' | 'next' | 'custom'>('current')
const lineCustomMonth = ref<Date | null>(null)

const lineBase = computed(() => {
  if (lineMonthMode.value === 'prev') return dayjs().subtract(1, 'month')
  if (lineMonthMode.value === 'next') return dayjs().add(1, 'month')
  if (lineMonthMode.value === 'custom' && lineCustomMonth.value) return dayjs(lineCustomMonth.value)
  return dayjs()
})

function setLineMonth(mode: 'prev' | 'current' | 'next') {
  lineMonthMode.value = mode
  lineCustomMonth.value = null
}
function onCustomMonthChange(val: Date | null) {
  if (val) lineMonthMode.value = 'custom'
}

const LINE_SERIES_DEF = [
  { key: 'onTimeCompleted', name: '按时完成', color: '#4dbb87' },
  { key: 'overdueCompleted', name: '逾期完成', color: '#f3a64b' },
  { key: 'failedReview', name: '审核未通过', color: '#e06472' },
  { key: 'todoTasks', name: '待办事务', color: '#5a8dee' },
  { key: 'overdueUnfinished', name: '逾期未完成', color: '#8B5CF6' },
  { key: 'cancelledTasks', name: '已作废', color: '#96a0af' },
] as const

const lineChartData = computed(() => {
  const base = lineBase.value.startOf('month')
  const daysCount = base.daysInMonth()
  const dates: string[] = []
  const dataMap: Record<string, number[]> = {}
  for (const s of LINE_SERIES_DEF) dataMap[s.key] = []

  for (let d = 0; d < daysCount; d++) {
    const dayStart = base.add(d, 'day')
    const dateStr = dayStart.format('YYYY-MM-DD')
    dates.push(dayStart.format('M/D'))

    const counts: Record<string, number> = {}
    for (const s of LINE_SERIES_DEF) counts[s.key] = 0

    for (const task of allTasks.value) {
      const taskDate = dayjs(task.createdAt).format('YYYY-MM-DD')
      if (taskDate !== dateStr) continue
      const cat = classifyTask(task)
      if (cat === 'onTimeCompleted') counts.onTimeCompleted++
      else if (cat === 'overdueCompleted') counts.overdueCompleted++
      else if (cat === 'failedReview') counts.failedReview++
      else if (cat === 'cancelled') counts.cancelledTasks++
      else if (cat === 'overdueUnfinished') counts.overdueUnfinished++
      else counts.todoTasks++
    }

    for (const s of LINE_SERIES_DEF) dataMap[s.key].push(counts[s.key])
  }

  return { dates, dataMap }
})

const timeLineOption = computed<EChartsOption>(() => ({
  grid: { top: 36, right: 16, bottom: 6, left: 12, containLabel: true },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(36, 50, 74, 0.92)',
    borderWidth: 0,
    textStyle: { color: '#fff' },
  },
  legend: {
    right: 0, top: 0, itemWidth: 16, itemHeight: 3,
    textStyle: { color: '#708097', fontSize: 12 },
  },
  xAxis: {
    type: 'category',
    data: lineChartData.value.dates,
    boundaryGap: false,
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#dbe4f0' } },
    axisLabel: { color: '#607087', fontSize: 11, interval: 'auto' },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: 'rgba(141, 159, 182, 0.18)' } },
    axisLabel: { color: '#8391a4', fontSize: 12 },
  },
  series: LINE_SERIES_DEF.map(s => ({
    name: s.name,
    type: 'line' as const,
    smooth: true,
    symbol: 'circle',
    symbolSize: 4,
    lineStyle: { width: 2, color: s.color },
    itemStyle: { color: s.color },
    data: lineChartData.value.dataMap[s.key],
  })),
}))

// ==================== 6. 每月事务折线图 ====================
const yearMode = ref<'prev' | 'current' | 'next' | 'custom'>('current')
const yearCustom = ref<Date | null>(null)

const yearBase = computed(() => {
  if (yearMode.value === 'prev') return dayjs().subtract(1, 'year')
  if (yearMode.value === 'next') return dayjs().add(1, 'year')
  if (yearMode.value === 'custom' && yearCustom.value) return dayjs(yearCustom.value)
  return dayjs()
})

function setYear(mode: 'prev' | 'current' | 'next') {
  yearMode.value = mode
  yearCustom.value = null
}
function onCustomYearChange(val: Date | null) {
  if (val) yearMode.value = 'custom'
}

const monthlyChartData = computed(() => {
  const year = yearBase.value.year()
  const months: string[] = []
  const dataMap: Record<string, number[]> = {}
  for (const s of LINE_SERIES_DEF) dataMap[s.key] = []

  for (let m = 0; m < 12; m++) {
    months.push(`${m + 1}月`)
    const counts: Record<string, number> = {}
    for (const s of LINE_SERIES_DEF) counts[s.key] = 0

    for (const task of allTasks.value) {
      const d = dayjs(task.createdAt)
      if (d.year() !== year || d.month() !== m) continue
      const cat = classifyTask(task)
      if (cat === 'onTimeCompleted') counts.onTimeCompleted++
      else if (cat === 'overdueCompleted') counts.overdueCompleted++
      else if (cat === 'failedReview') counts.failedReview++
      else if (cat === 'cancelled') counts.cancelledTasks++
      else if (cat === 'overdueUnfinished') counts.overdueUnfinished++
      else counts.todoTasks++
    }

    for (const s of LINE_SERIES_DEF) dataMap[s.key].push(counts[s.key])
  }

  return { months, dataMap }
})

const monthlyLineOption = computed<EChartsOption>(() => ({
  grid: { top: 36, right: 16, bottom: 6, left: 12, containLabel: true },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(36, 50, 74, 0.92)',
    borderWidth: 0,
    textStyle: { color: '#fff' },
  },
  legend: {
    right: 0, top: 0, itemWidth: 16, itemHeight: 3,
    textStyle: { color: '#708097', fontSize: 12 },
  },
  xAxis: {
    type: 'category',
    data: monthlyChartData.value.months,
    boundaryGap: false,
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#dbe4f0' } },
    axisLabel: { color: '#607087', fontSize: 11 },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: { lineStyle: { color: 'rgba(141, 159, 182, 0.18)' } },
    axisLabel: { color: '#8391a4', fontSize: 12 },
  },
  series: LINE_SERIES_DEF.map(s => ({
    name: s.name,
    type: 'line' as const,
    smooth: true,
    symbol: 'circle',
    symbolSize: 4,
    lineStyle: { width: 2, color: s.color },
    itemStyle: { color: s.color },
    data: monthlyChartData.value.dataMap[s.key],
  })),
}))

// ==================== 导出 ====================
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

// ==================== 数据加载 ====================
onMounted(async () => {
  loading.value = true
  try {
    const [overviewRes, personRes, taskRes] = await Promise.all([
      getStatsOverviewApi(),
      getStatsByPersonApi(),
      getTaskListApi({ type: 'scope', page: 1, pageSize: 1000 }),
    ])
    overview.value = overviewRes.data
    personStats.value = personRes.data
    allTasks.value = taskRes.data.list
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.statistics-page {
  height: 100%;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: auto auto auto 1fr 1fr;
  gap: $spacing-md;
  overflow: hidden;
  padding: $spacing-sm 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  h3 { font-size: 20px; font-weight: 600; margin: 0; }
  .export-area { display: flex; align-items: center; gap: $spacing-sm; }
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  margin: 0 0 $spacing-sm 0;
}

/* ========== 1. 事务状态卡片 ========== */
.status-cards {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: $spacing-sm;
}

.stat-card {
  background: color-mix(in srgb, var(--card-color) 12%, $bg-card);
  border-left: 4px solid var(--card-color);
  border-radius: $radius-sm;
  box-shadow: $shadow-sm;
  padding: $spacing-md $spacing-md;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  transition: box-shadow 0.2s, background 0.2s;
  white-space: nowrap;
  min-height: 100px;
  &:hover { box-shadow: $shadow-hover; background: color-mix(in srgb, var(--card-color) 18%, $bg-card); }

  &--primary {
    background: linear-gradient(135deg, #4F6EF7 0%, #6583F9 100%);
    border-left: none;
    .stat-card__left, .stat-card__right { color: #000; }
    .stat-card__left strong { color: #000; }
  }

  &__left {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
    strong { font-size: 22px; font-weight: 700; color: #1a1a1a; margin-left: 4px; }
  }
  &__right {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
    text-align: right;
  }
}

/* ========== 2. 紧急程度卡片 ========== */
.urgency-cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: $spacing-sm;
}

.urgency-card {
  background: var(--uc-bg);
  border-radius: $radius-sm;
  box-shadow: $shadow-sm;
  padding: $spacing-md $spacing-md;
  border-left: 4px solid var(--uc-color);
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  min-height: 120px;

  &__top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: $spacing-xs;
  }

  &__level {
    font-size: 14px;
    font-weight: 700;
    color: var(--uc-color);
  }

  &__stats {
    display: flex;
    flex-wrap: nowrap;
    gap: 6px 12px;
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
    strong { color: #1a1a1a; margin-left: 2px; }
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    margin-top: auto;
    padding-top: $spacing-xs;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
  }

  &__metric {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 6px;
    strong { font-size: 16px; color: #1a1a1a; font-weight: 700; }
  }

  &__metric-label {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
  }
}

/* ========== 3 & 4. 图表行（部门+人员并排） ========== */
.section-charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-sm;
  min-height: 0;
}

.chart-col {
  display: flex;
  flex-direction: column;
  min-height: 0;

  .chart-card {
    flex: 1;
  }
}

.section-time-charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-sm;
  min-height: 0;
}

.time-chart-col {
  display: flex;
  flex-direction: column;
  min-height: 0;

  .chart-card { flex: 1; }
}

.section-time-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-sm;

  .section-title {
    margin: 0;
  }
}

.chart-card {
  background: $bg-card;
  border-radius: $radius-sm;
  box-shadow: $shadow-sm;
  padding: $spacing-sm $spacing-md;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  &--full { min-height: 0; flex: 1; }

  &__header {
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-xs;
    h4 { font-size: 14px; font-weight: 600; margin: 0; color: $text-primary; }
    .chart-card__sub { font-size: $font-size-xs; color: $text-secondary; }
  }

  .chart {
    flex: 1;
    min-height: 0;
    width: 100%;
  }
}

/* ========== 5. 时间折线图 ========== */
.line-month-selector {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
