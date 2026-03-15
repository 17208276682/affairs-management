<template>
  <div class="dashboard-page">
    <header class="dashboard-header">
      <div>
        <p class="dashboard-kicker">事务总览</p>
        <h2>{{ panelConfig.title }}</h2>
        <p class="dashboard-meta">
          {{ userStore.userInfo?.deptName }} · {{ userStore.userInfo?.position }} · {{ panelConfig.subtitle }}
        </p>
      </div>
      <div class="dashboard-clock">
        <span>{{ greeting }}</span>
        <strong>{{ nowLabel }}</strong>
      </div>
    </header>

    <section class="dashboard-split">
      <div class="left-panel">
        <div class="left-panel__inner" :class="{ 'no-stats': !showDirectorStats }">
          <div class="panel-row panel-status-row" :class="`count-${panelConfig.statuses.length}`">
            <article
              v-for="item in panelConfig.statuses"
              :key="item.key"
              class="status-card"
              :style="{ '--status-color': item.color, '--status-glow': item.glow }"
            >
              <div class="status-card__top">
                <span class="status-dot"></span>
                <span class="status-name">{{ item.label }}</span>
              </div>
              <strong class="status-value">{{ statusCountMap[item.key] ?? 0 }}</strong>
              <span class="status-note">{{ item.note }}</span>
            </article>
          </div>

          <div class="panel-row panel-middle-row">
            <article class="panel-card activity-panel">
              <div class="panel-card__header">
                <div>
                  <p class="panel-card__eyebrow">最新动态</p>
                  <h3>{{ panelConfig.activityTitle }}</h3>
                </div>
                <span class="panel-card__badge">{{ roleActivities.length }} 条</span>
              </div>

              <div v-if="roleActivities.length" class="activity-list">
                <div v-for="activity in roleActivities" :key="activity.id" class="activity-item">
                  <span class="activity-icon" :class="`type-${activity.type}`"></span>
                  <div class="activity-content">
                    <p>{{ activity.content }}</p>
                    <span>{{ formatRelativeTime(activity.time) }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无动态" :image-size="54" />
            </article>

            <article class="panel-card chart-panel pie-panel">
              <div class="panel-card__header">
                <div>
                  <p class="panel-card__eyebrow">级别分布</p>
                  <h3>{{ panelConfig.pieTitle }}</h3>
                </div>
                <span class="panel-card__badge">共 {{ pieTotal }} 项</span>
              </div>

              <div class="pie-panel__body">
                <VChart class="chart chart-pie" :option="pieOption" autoresize />
                <div class="pie-legend">
                  <div v-for="item in pieDataset" :key="item.level" class="pie-legend__item">
                    <span class="pie-legend__dot" :style="{ background: item.color }"></span>
                    <span class="pie-legend__label">{{ item.level }}级</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
              </div>
            </article>
          </div>

          <div v-if="showDirectorStats" class="panel-row panel-chart-row">
            <article class="panel-card chart-panel bar-panel">
              <div class="panel-card__header">
                <div>
                  <p class="panel-card__eyebrow">事务统计</p>
                  <h3>{{ panelConfig.barTitle }}</h3>
                </div>
              </div>

              <VChart class="chart chart-bar" :option="barOption" autoresize />
            </article>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="view-shell">
          <div class="view-shell__header">
            <div>
              <p class="panel-card__eyebrow">时间与优先级视图</p>
              <h3>任务全景追踪</h3>
            </div>
          </div>

          <el-tabs v-model="activeTab" class="view-tabs">
            <el-tab-pane label="日视图" name="day">
              <div class="day-header">
                <el-button text :icon="ArrowLeftBold" @click="changeDay(-1)" />
                <span class="day-title">{{ dayLabel }}</span>
                <el-button text :icon="ArrowRightBold" @click="changeDay(1)" />
                <el-button v-if="!isToday" text type="primary" size="small" @click="dayOffset = 0">今天</el-button>
              </div>

              <div class="time-view-shell">
                <div class="time-grid">
                  <div class="time-grid-head" :style="{ '--col-count': String(dayTimeColumns.length) }">
                    <div class="time-head-time">时段</div>
                    <div
                      v-for="col in dayTimeColumns"
                      :key="col.dateStr"
                      class="time-head-day"
                      :class="{ 'is-today': col.isToday }"
                    >
                      <span>{{ col.dayLabel }}</span>
                      <strong>{{ col.dateLabel }}</strong>
                    </div>
                  </div>

                  <div class="time-grid-body" :style="{ '--col-count': String(dayTimeColumns.length) }">
                    <div v-for="hour in HOURS" :key="`day-${hour}`" class="time-row">
                      <div class="time-hour">{{ formatHourLabel(hour) }}</div>
                      <div v-for="col in dayTimeColumns" :key="`${col.dateStr}-${hour}`" class="time-cell">
                        <div
                          v-for="task in col.tasksByHour[hour] || []"
                          :key="task.id"
                          class="time-task"
                          :style="{ borderLeftColor: TASK_LEVEL_MAP[task.level as TaskLevel]?.color }"
                        >
                          <div class="time-task-title">{{ (task.description || task.title).substring(0, 20) }}</div>
                          <div class="time-task-meta">{{ formatWeekTaskTime(task.completionDeadline) }} · {{ task.executorName }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="三日视图" name="three-day">
              <div class="week-header">
                <el-button text :icon="ArrowLeftBold" @click="threeDayOffset--" />
                <span class="week-title">{{ threeDayLabel }}</span>
                <el-button text :icon="ArrowRightBold" @click="threeDayOffset++" />
                <el-button v-if="threeDayOffset !== 0" text type="primary" size="small" @click="threeDayOffset = 0">今天</el-button>
              </div>

              <div class="time-view-shell">
                <div class="time-grid">
                  <div class="time-grid-head" :style="{ '--col-count': String(threeDayColumns.length) }">
                    <div class="time-head-time">时段</div>
                    <div
                      v-for="col in threeDayColumns"
                      :key="col.dateStr"
                      class="time-head-day"
                      :class="{ 'is-today': col.isToday }"
                    >
                      <span>{{ col.dayLabel }}</span>
                      <strong>{{ col.dateLabel }}</strong>
                    </div>
                  </div>

                  <div class="time-grid-body" :style="{ '--col-count': String(threeDayColumns.length) }">
                    <div v-for="hour in HOURS" :key="`threeday-${hour}`" class="time-row">
                      <div class="time-hour">{{ formatHourLabel(hour) }}</div>
                      <div v-for="col in threeDayColumns" :key="`${col.dateStr}-${hour}`" class="time-cell">
                        <div
                          v-for="task in col.tasksByHour[hour] || []"
                          :key="task.id"
                          class="time-task"
                          :style="{ borderLeftColor: TASK_LEVEL_MAP[task.level as TaskLevel]?.color }"
                        >
                          <div class="time-task-title">{{ (task.description || task.title).substring(0, 20) }}</div>
                          <div class="time-task-meta">{{ formatWeekTaskTime(task.completionDeadline) }} · {{ task.executorName }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="周视图" name="week">
              <div class="week-header">
                <el-button text :icon="ArrowLeftBold" @click="weekOffset--" />
                <span class="week-title">{{ weekLabel }}</span>
                <el-button text :icon="ArrowRightBold" @click="weekOffset++" />
                <el-button v-if="weekOffset !== 0" text type="primary" size="small" @click="weekOffset = 0">本周</el-button>
              </div>

              <div class="time-view-shell">
                <div class="time-grid time-grid--week">
                  <div class="time-grid-head" :style="{ '--col-count': String(weekTimeColumns.length) }">
                    <div class="time-head-time">时段</div>
                    <div
                      v-for="col in weekTimeColumns"
                      :key="col.dateStr"
                      class="time-head-day"
                      :class="{ 'is-today': col.isToday }"
                    >
                      <span>{{ col.dayLabel }}</span>
                      <strong>{{ col.dateLabel }}</strong>
                    </div>
                  </div>

                  <div class="time-grid-body" :style="{ '--col-count': String(weekTimeColumns.length) }">
                    <div v-for="hour in HOURS" :key="`week-${hour}`" class="time-row">
                      <div class="time-hour">{{ formatHourLabel(hour) }}</div>
                      <div v-for="col in weekTimeColumns" :key="`${col.dateStr}-${hour}`" class="time-cell">
                        <div
                          v-for="task in col.tasksByHour[hour] || []"
                          :key="task.id"
                          class="time-task"
                          :style="{ borderLeftColor: TASK_LEVEL_MAP[task.level as TaskLevel]?.color }"
                        >
                          <div class="time-task-title">{{ (task.description || task.title).substring(0, 16) }}</div>
                          <div class="time-task-meta">{{ formatWeekTaskTime(task.completionDeadline) }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="月视图" name="month">
              <div class="month-header">
                <el-button text :icon="ArrowLeftBold" @click="monthOffset--" />
                <span class="month-title">{{ monthLabel }}</span>
                <el-button text :icon="ArrowRightBold" @click="monthOffset++" />
                <el-button v-if="monthOffset !== 0" text type="primary" size="small" @click="monthOffset = 0">本月</el-button>
              </div>

              <div class="month-pane">
                <div class="month-weekdays">
                  <span v-for="day in weekDayNames" :key="day">{{ day }}</span>
                </div>

                <div class="month-grid">
                  <div
                    v-for="(cell, idx) in monthCells"
                    :key="idx"
                    class="month-cell"
                    :class="{ 'other-month': !cell.isCurrentMonth, 'is-today': cell.isToday }"
                  >
                    <div class="month-cell-date">{{ cell.day }}</div>
                    <div v-if="cell.tasks.length" class="month-cell-dots">
                      <span
                        v-for="task in cell.tasks.slice(0, 3)"
                        :key="task.id"
                        class="month-dot"
                        :style="{ background: TASK_LEVEL_MAP[task.level as TaskLevel]?.color }"
                      />
                      <span v-if="cell.tasks.length > 3" class="month-more">+{{ cell.tasks.length - 3 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="四象限视图" name="quadrant">
              <div class="quadrant-pane">
                <div class="quadrant-grid">
                  <div
                    v-for="quadrant in quadrants"
                    :key="quadrant.level"
                    class="quadrant-box"
                    :style="{ '--q-color': quadrant.color, '--q-bg': quadrant.bgColor }"
                  >
                    <div class="quadrant-title">
                      <span class="quadrant-badge" :style="{ background: quadrant.color }">{{ quadrant.level }}</span>
                      {{ quadrant.label }}
                      <span class="quadrant-count">{{ quadrant.tasks.length }}</span>
                    </div>

                    <div class="quadrant-list">
                      <div v-for="task in quadrant.tasks" :key="task.id" class="quadrant-item">
                        <div class="quadrant-item-desc">{{ (task.description || task.title).substring(0, 20) }}</div>
                        <div class="quadrant-item-meta">{{ task.executorName }} · {{ statusDisplay(task.status).label }}</div>
                      </div>
                      <el-empty v-if="!quadrant.tasks.length" description="暂无" :image-size="38" />
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { ArrowLeftBold, ArrowRightBold } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores'
import { TASK_LEVEL_MAP } from '@/utils/constants'
import { formatRelativeTime } from '@/utils/format'
import { getRecentActivitiesApi, getStatsByDeptApi, getStatsByPersonApi } from '@/api/statistics'
import { getTaskListApi } from '@/api/task'
import type { DeptStats, PersonStats, Task, TaskLevel } from '@/types'

use([CanvasRenderer, PieChart, BarChart, GridComponent, LegendComponent, TooltipComponent])

type PanelRole = 'director' | 'manager' | 'staff'
type StatusKey = 'assigned' | 'pending' | 'completed' | 'unfinished' | 'overdue'

interface StatusCardConfig {
  key: StatusKey
  label: string
  color: string
  glow: string
  note: string
}

const userStore = useUserStore()

const roleActivities = ref<{ id: string; type: string; content: string; time: string }[]>([])
const assignedTasks = ref<Task[]>([])
const scopeTasks = ref<Task[]>([])
const todoTasks = ref<Task[]>([])
const receivedTasks = ref<Task[]>([])
const deptStats = ref<DeptStats[]>([])
const personStats = ref<PersonStats[]>([])
const nowLabel = ref(formatNow())

const activeTab = ref('day')
const dayOffset = ref(0)
const threeDayOffset = ref(0)
const weekOffset = ref(0)
const monthOffset = ref(0)
const HOURS = Array.from({ length: 24 }, (_, i) => i)
const showDirectorStats = computed(() => userStore.isDirector)

let timer: number | undefined

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const role = computed<PanelRole>(() => {
  const currentRole = userStore.userInfo?.role
  if (currentRole === 'manager') return 'manager'
  if (currentRole === 'staff') return 'staff'
  return 'director'
})

const panelConfig = computed(() => {
  const common = {
    director: {
      title: '陈志远事务驾驶舱',
      subtitle: '总经办全局指挥视角',
      activityTitle: '下级提交动态',
      pieTitle: '已下达事务四个级别',
      barTitle: '部门总事务数 / 已完成数',
      statuses: [
        { key: 'assigned', label: '已下达', color: '#5a8dee', glow: 'rgba(90, 141, 238, 0.16)', note: '已正式下达任务' },
        { key: 'pending', label: '待办理', color: '#f3a64b', glow: 'rgba(243, 166, 75, 0.18)', note: '等待部门处理' },
        { key: 'completed', label: '已完成', color: '#4dbb87', glow: 'rgba(77, 187, 135, 0.18)', note: '流程已闭环' },
        { key: 'unfinished', label: '未完成', color: '#f07b61', glow: 'rgba(240, 123, 97, 0.16)', note: '仍在流转中' },
        { key: 'overdue', label: '已逾期', color: '#de5a6a', glow: 'rgba(222, 90, 106, 0.16)', note: '超过节点时限' },
      ] satisfies StatusCardConfig[],
    },
    manager: {
      title: '王建华事务驾驶舱',
      subtitle: '部门经理协同视角',
      activityTitle: '上下级动态',
      pieTitle: '待办理事务四个级别',
      barTitle: '人员总事务数 / 已完成数',
      statuses: [
        { key: 'assigned', label: '已下达', color: '#5a8dee', glow: 'rgba(90, 141, 238, 0.16)', note: '已分派给下级' },
        { key: 'pending', label: '待办理', color: '#f3a64b', glow: 'rgba(243, 166, 75, 0.18)', note: '本人仍需推进' },
        { key: 'completed', label: '已完成', color: '#4dbb87', glow: 'rgba(77, 187, 135, 0.18)', note: '已完成闭环' },
        { key: 'unfinished', label: '未完成', color: '#f07b61', glow: 'rgba(240, 123, 97, 0.16)', note: '未达到完成态' },
        { key: 'overdue', label: '已逾期', color: '#de5a6a', glow: 'rgba(222, 90, 106, 0.16)', note: '超时未结项' },
      ] satisfies StatusCardConfig[],
    },
    staff: {
      title: '黄晓龙事务驾驶舱',
      subtitle: '个人执行视角',
      activityTitle: '上级下发动态',
      pieTitle: '待办理事务四个级别',
      barTitle: '个人总事务数 / 已完成数',
      statuses: [
        { key: 'assigned', label: '已下达', color: '#5a8dee', glow: 'rgba(90, 141, 238, 0.16)', note: '上级已下达给我' },
        { key: 'pending', label: '待办理', color: '#f3a64b', glow: 'rgba(243, 166, 75, 0.18)', note: '待接收或待处理' },
        { key: 'completed', label: '已完成', color: '#4dbb87', glow: 'rgba(77, 187, 135, 0.18)', note: '审核已通过' },
        { key: 'unfinished', label: '未完成', color: '#f07b61', glow: 'rgba(240, 123, 97, 0.16)', note: '仍需继续处理' },
        { key: 'overdue', label: '已逾期', color: '#de5a6a', glow: 'rgba(222, 90, 106, 0.16)', note: '超过办理时限' },
      ] satisfies StatusCardConfig[],
    },
  }

  return common[role.value]
})

const directorRootTasks = computed(() => assignedTasks.value.filter(task => !task.parentTaskId))
const taskPool = computed(() => (role.value === 'director' ? directorRootTasks.value : receivedTasks.value))
const allCalendarTasks = computed(() => {
  const source = role.value === 'director' ? directorRootTasks.value : taskPool.value
  return [...source].sort((a, b) => new Date(a.completionDeadline).getTime() - new Date(b.completionDeadline).getTime())
})

const statusCountMap = computed<Record<StatusKey, number>>(() => {
  return {
    // 已下达：对应我的下达菜单
    assigned: assignedTasks.value.length,
    // 待办理：对应我的代办菜单
    pending: todoTasks.value.length,
    // 以下三项：对应事务列表菜单里的状态
    completed: scopeTasks.value.filter(task => isDisplayCompleted(task.status)).length,
    unfinished: scopeTasks.value.filter(task => isDisplayUnfinished(task.status)).length,
    overdue: scopeTasks.value.filter(task => task.status === 'overdue').length,
  }
})

const pieDataset = computed(() => {
  return (['A', 'B', 'C', 'D'] as TaskLevel[]).map(level => ({
    level,
    value: taskPool.value.filter(task => task.level === level && (role.value === 'director' ? true : isActiveTask(task.status))).length,
    color: TASK_LEVEL_MAP[level].color,
  }))
})

const pieTotal = computed(() => pieDataset.value.reduce((sum, item) => sum + item.value, 0))

const pieOption = computed<EChartsOption>(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} 项 ({d}%)',
    backgroundColor: 'rgba(36, 50, 74, 0.92)',
    borderWidth: 0,
    textStyle: { color: '#fff' },
  },
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['42%', '52%'],
      avoidLabelOverlap: true,
      label: {
        color: '#617287',
        fontSize: 12,
        formatter: '{b}\n{c}',
      },
      labelLine: {
        lineStyle: { color: 'rgba(108, 126, 151, 0.36)' },
      },
      itemStyle: {
        borderRadius: 12,
        borderColor: '#fffdfb',
        borderWidth: 4,
      },
      data: pieDataset.value.map(item => ({
        name: `${item.level}级`,
        value: item.value,
        itemStyle: { color: item.color },
      })),
    },
  ],
}))

const barSource = computed(() => ([
  { label: '技术研发部', total: 38, completed: 29 },
  { label: '财务部', total: 22, completed: 19 },
  { label: '人力资源部', total: 16, completed: 12 },
  { label: '总经办', total: 12, completed: 9 },
]))

const barOption = computed<EChartsOption>(() => ({
  grid: { top: 20, right: 16, bottom: 6, left: 12, containLabel: true },
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    backgroundColor: 'rgba(36, 50, 74, 0.92)',
    borderWidth: 0,
    textStyle: { color: '#fff' },
  },
  legend: {
    right: 0,
    top: 0,
    itemWidth: 10,
    itemHeight: 10,
    textStyle: { color: '#708097', fontSize: 12 },
  },
  xAxis: {
    type: 'category',
    data: barSource.value.map(item => item.label),
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#dbe4f0' } },
    axisLabel: { color: '#607087', fontSize: 12, interval: 0 },
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'rgba(141, 159, 182, 0.18)' } },
    axisLabel: { color: '#8391a4', fontSize: 12 },
  },
  series: [
    {
      name: '总事务数',
      type: 'bar',
      barMaxWidth: 22,
      itemStyle: {
        borderRadius: [10, 10, 0, 0],
        color: '#93b7ff',
      },
      data: barSource.value.map(item => item.total),
    },
    {
      name: '已完成数',
      type: 'bar',
      barMaxWidth: 22,
      itemStyle: {
        borderRadius: [10, 10, 0, 0],
        color: '#67c8a0',
      },
      data: barSource.value.map(item => item.completed),
    },
  ],
}))

const currentDay = computed(() => dayjs().add(dayOffset.value, 'day'))
const isToday = computed(() => dayOffset.value === 0)
const dayLabel = computed(() => {
  const date = currentDay.value
  const prefix = isToday.value ? '今天 · ' : ''
  return `${prefix}${date.format('YYYY年M月D日')}`
})
const dayTasks = computed(() => {
  const target = currentDay.value.format('YYYY-MM-DD')
  return allCalendarTasks.value.filter(task => dayjs(task.completionDeadline).format('YYYY-MM-DD') === target)
})

type TimeGridColumn = {
  dateStr: string
  dayLabel: string
  dateLabel: string
  isToday: boolean
  tasksByHour: Record<number, Task[]>
}

function buildTasksByHour(dateStr: string) {
  const rows: Record<number, Task[]> = {}
  for (let i = 0; i < 24; i += 1) rows[i] = []

  allCalendarTasks.value
    .filter(task => dayjs(task.completionDeadline).format('YYYY-MM-DD') === dateStr)
    .sort((a, b) => dayjs(a.completionDeadline).valueOf() - dayjs(b.completionDeadline).valueOf())
    .forEach(task => {
      const hour = dayjs(task.completionDeadline).hour()
      rows[hour].push(task)
    })

  return rows
}

function formatHourLabel(hour: number) {
  return `${String(hour).padStart(2, '0')}:00`
}

const dayTimeColumns = computed<TimeGridColumn[]>(() => {
  const date = currentDay.value
  const dateStr = date.format('YYYY-MM-DD')
  return [
    {
      dateStr,
      dayLabel: `周${weekDayNames[(date.day() + 6) % 7]}`,
      dateLabel: date.format('M月D日'),
      isToday: dateStr === dayjs().format('YYYY-MM-DD'),
      tasksByHour: buildTasksByHour(dateStr),
    },
  ]
})

const threeDayStart = computed(() => dayjs().startOf('day').add(threeDayOffset.value, 'day'))
const threeDayLabel = computed(() => {
  const start = threeDayStart.value
  const end = start.add(2, 'day')
  return `${start.format('M月D日')} — ${end.format('M月D日')}`
})

const threeDayColumns = computed<TimeGridColumn[]>(() => {
  const todayStr = dayjs().format('YYYY-MM-DD')
  return Array.from({ length: 3 }, (_, idx) => {
    const date = threeDayStart.value.add(idx, 'day')
    const dateStr = date.format('YYYY-MM-DD')
    return {
      dateStr,
      dayLabel: `周${weekDayNames[(date.day() + 6) % 7]}`,
      dateLabel: date.format('M月D日'),
      isToday: dateStr === todayStr,
      tasksByHour: buildTasksByHour(dateStr),
    }
  })
})

const weekDayNames = ['一', '二', '三', '四', '五', '六', '日']
const weekStart = computed(() => {
  const today = dayjs().add(weekOffset.value * 7, 'day')
  const dayOfWeek = today.day()
  return today.subtract(dayOfWeek === 0 ? 6 : dayOfWeek - 1, 'day')
})
const weekLabel = computed(() => {
  const start = weekStart.value
  const end = start.add(6, 'day')
  return `${start.format('M月D日')} — ${end.format('M月D日')}`
})
const weekColumns = computed(() => {
  const todayStr = dayjs().format('YYYY-MM-DD')
  return Array.from({ length: 7 }, (_, index) => {
    const date = weekStart.value.add(index, 'day')
    const dateStr = date.format('YYYY-MM-DD')
    return {
      dayLabel: `周${weekDayNames[index]}`,
      dateLabel: date.format('MM-DD'),
      isToday: dateStr === todayStr,
      tasks: allCalendarTasks.value
        .filter(task => dayjs(task.completionDeadline).format('YYYY-MM-DD') === dateStr)
        .sort((a, b) => dayjs(a.completionDeadline).valueOf() - dayjs(b.completionDeadline).valueOf()),
    }
  })
})

const weekTimeColumns = computed<TimeGridColumn[]>(() => {
  const todayStr = dayjs().format('YYYY-MM-DD')
  return Array.from({ length: 7 }, (_, index) => {
    const date = weekStart.value.add(index, 'day')
    const dateStr = date.format('YYYY-MM-DD')
    return {
      dateStr,
      dayLabel: `周${weekDayNames[index]}`,
      dateLabel: date.format('M月D日'),
      isToday: dateStr === todayStr,
      tasksByHour: buildTasksByHour(dateStr),
    }
  })
})

function formatWeekTaskTime(time: string) {
  return dayjs(time).format('HH:mm')
}

const currentMonth = computed(() => dayjs().add(monthOffset.value, 'month'))
const monthLabel = computed(() => currentMonth.value.format('YYYY年M月'))
const monthCells = computed(() => {
  const first = currentMonth.value.startOf('month')
  const dayOfWeek = first.day()
  const startOffset = dayOfWeek === 0 ? 6 : dayOfWeek - 1
  const gridStart = first.subtract(startOffset, 'day')
  const todayStr = dayjs().format('YYYY-MM-DD')
  const currentMonthIndex = currentMonth.value.month()

  return Array.from({ length: 42 }, (_, index) => {
    const date = gridStart.add(index, 'day')
    const dateStr = date.format('YYYY-MM-DD')
    return {
      day: date.date(),
      isCurrentMonth: date.month() === currentMonthIndex,
      isToday: dateStr === todayStr,
      tasks: allCalendarTasks.value.filter(task => dayjs(task.completionDeadline).format('YYYY-MM-DD') === dateStr),
    }
  })
})

const quadrants = computed(() => {
  const levels: { level: TaskLevel; label: string; color: string; bgColor: string }[] = [
    { level: 'A', label: '重要紧急', color: '#ef6b6b', bgColor: '#fff1f0' },
    { level: 'B', label: '不重要紧急', color: '#f4ad54', bgColor: '#fff7ea' },
    { level: 'C', label: '重要不紧急', color: '#5b8fff', bgColor: '#eff4ff' },
    { level: 'D', label: '不重要不紧急', color: '#59be90', bgColor: '#edf9f2' },
  ]

  return levels.map(level => ({
    ...level,
    tasks: allCalendarTasks.value.filter(task => task.level === level.level).slice(0, 4),
  }))
})

function changeDay(step: number) {
  dayOffset.value += step
}

function statusDisplay(status: Task['status']) {
  if (['completed', 'approved'].includes(status)) {
    return { label: '已完成', color: '#22a66f', bgColor: '#ecfaf2' }
  }
  if (status === 'submitted') {
    return { label: '已提交', color: '#7e68e8', bgColor: '#f2efff' }
  }
  if (status === 'overdue') {
    return { label: '已逾期', color: '#d45767', bgColor: '#fef0f2' }
  }
  if (status === 'cancelled') {
    return { label: '已取消', color: '#9099a8', bgColor: '#f2f4f7' }
  }
  return { label: '未完成', color: '#ec9a39', bgColor: '#fff8e9' }
}

function isCompletedTask(status: Task['status']) {
  return ['completed', 'approved'].includes(status)
}

function isActiveTask(status: Task['status']) {
  return !['completed', 'approved', 'cancelled'].includes(status)
}

function isUnfinishedTask(status: Task['status']) {
  return !['completed', 'approved', 'cancelled'].includes(status)
}

function isPendingForDirector(status: Task['status']) {
  return ['pending', 'accepted', 'in_progress', 'submitted'].includes(status)
}

function isPendingForExecution(status: Task['status']) {
  return ['pending', 'accepted', 'in_progress'].includes(status)
}

function isDisplayCompleted(status: Task['status']) {
  return ['completed', 'approved'].includes(status)
}

function isDisplayUnfinished(status: Task['status']) {
  return !['completed', 'approved', 'overdue', 'cancelled'].includes(status)
}

function formatNow() {
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date())
}

async function loadDashboard() {
  const [activitiesRes, assignedRes, scopeRes, todoRes, receivedRes, deptRes, personRes] = await Promise.all([
    getRecentActivitiesApi(),
    getTaskListApi({ type: 'assigned', page: 1, pageSize: 200 }),
    getTaskListApi({ type: 'scope', page: 1, pageSize: 200 }),
    getTaskListApi({ type: 'todo', page: 1, pageSize: 200 }),
    getTaskListApi({ type: 'received', page: 1, pageSize: 200 }),
    getStatsByDeptApi(),
    getStatsByPersonApi(),
  ])

  roleActivities.value = activitiesRes.data
  assignedTasks.value = assignedRes.data.list
  scopeTasks.value = scopeRes.data.list
  todoTasks.value = todoRes.data.list
  receivedTasks.value = receivedRes.data.list
  deptStats.value = deptRes.data
  personStats.value = personRes.data
}

onMounted(async () => {
  await loadDashboard()
  timer = window.setInterval(() => {
    nowLabel.value = formatNow()
  }, 60000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 680px;
}

.dashboard-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;

  h2 {
    margin: 4px 0 6px;
    font-size: 30px;
    line-height: 1.15;
    color: #20304d;
  }
}

.dashboard-kicker {
  color: #6d91db;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.dashboard-meta {
  color: #748398;
  font-size: 14px;
}

.dashboard-clock {
  min-width: 168px;
  padding: 14px 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(248, 251, 255, 0.88));
  border: 1px solid rgba(219, 228, 240, 0.9);
  box-shadow: 0 12px 30px rgba(146, 166, 198, 0.12);
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #6b7c91;

  strong {
    font-size: 22px;
    color: #22324a;
  }
}

.dashboard-split {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(0, 0.98fr);
  gap: 14px;
}

.left-panel,
.right-panel {
  min-height: 0;
}

.left-panel__inner,
.view-shell {
  height: 100%;
  min-height: 0;
}

.left-panel__inner {
  display: grid;
  grid-template-rows: 112px minmax(0, 1fr) minmax(190px, 0.9fr);
  gap: 12px;

  &.no-stats {
    grid-template-rows: 112px minmax(0, 1fr);
  }
}

.panel-status-row {
  display: grid;
  gap: 12px;

  &.count-5 {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }

  &.count-6 {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}

.status-card {
  position: relative;
  overflow: hidden;
  padding: 14px 16px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(248, 251, 255, 0.9));
  border: 1px solid rgba(226, 234, 244, 0.95);
  box-shadow: 0 14px 36px rgba(140, 161, 190, 0.12);
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  &::after {
    content: '';
    position: absolute;
    inset: auto -18px -46px auto;
    width: 110px;
    height: 110px;
    border-radius: 50%;
    background: var(--status-glow);
  }
}

.status-card__top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--status-color);
  box-shadow: 0 0 0 6px color-mix(in srgb, var(--status-color) 16%, white);
}

.status-name {
  color: #66778f;
  font-size: 13px;
}

.status-value {
  font-size: 30px;
  line-height: 1;
  color: #22324a;
}

.status-note {
  font-size: 12px;
  color: #97a4b5;
}

.panel-middle-row {
  display: grid;
  grid-template-columns: minmax(0, 1.06fr) minmax(0, 0.94fr);
  gap: 12px;
  min-height: 0;
}

.panel-card,
.view-shell {
  min-height: 0;
  border-radius: 26px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 254, 0.92));
  border: 1px solid rgba(225, 233, 243, 0.95);
  box-shadow: 0 18px 44px rgba(144, 164, 193, 0.12);
}

.panel-card {
  padding: 16px 16px 14px;
  display: flex;
  flex-direction: column;
}

.panel-card__header,
.view-shell__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;

  h3 {
    margin-top: 4px;
    font-size: 19px;
    color: #22324a;
  }
}

.panel-card__eyebrow {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #8ba0c2;
}

.panel-card__badge {
  padding: 6px 12px;
  border-radius: 999px;
  background: #f4f7fc;
  color: #7a8aa0;
  font-size: 12px;
}

.activity-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-item {
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
  border-radius: 18px;
  background: linear-gradient(135deg, #fcfdff 0%, #f7f9fd 100%);
  border: 1px solid #edf1f7;
}

.activity-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin-top: 2px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: 5px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.9);
  }

  &.type-submit {
    background: linear-gradient(180deg, #79a2ff 0%, #4d7cfe 100%);
  }

  &.type-assign {
    background: linear-gradient(180deg, #ffbf97 0%, #ff8e72 100%);
  }

  &.type-overdue {
    background: linear-gradient(180deg, #f38c96 0%, #de5a6a 100%);
  }
}

.activity-content {
  min-width: 0;

  p {
    color: #24324a;
    line-height: 1.6;
    font-size: 13px;
  }

  span {
    display: inline-block;
    margin-top: 6px;
    color: #95a3b6;
    font-size: 12px;
  }
}

.pie-panel__body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 118px;
  gap: 8px;
  align-items: center;
}

.chart {
  width: 100%;
  min-height: 0;
}

.chart-pie {
  height: 100%;
  min-height: 208px;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pie-legend__item {
  padding: 10px;
  border-radius: 14px;
  background: #f7f9fc;
  border: 1px solid #eef2f7;
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;

  strong {
    color: #24324a;
    font-size: 14px;
  }
}

.pie-legend__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.pie-legend__label {
  color: #6f8097;
  font-size: 12px;
}

.chart-bar {
  flex: 1;
  height: 100%;
  min-height: 188px;
}

.view-shell {
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.view-shell__header {
  margin-bottom: 8px;
}

.view-tabs {
  flex: 1;
  min-height: 0;

  :deep(.el-tabs__content) {
    height: calc(100% - 44px);
  }

  :deep(.el-tab-pane) {
    height: 100%;
    display: flex;
    flex-direction: column;
    min-height: 0;
  }
}

.day-header,
.week-header,
.month-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;

  .day-title,
  .week-title,
  .month-title {
    min-width: 180px;
    text-align: center;
    font-size: 15px;
    font-weight: 600;
    color: #24324a;
  }
}

.time-view-shell {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.time-grid {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #e1e8f2;
  border-radius: 14px;
  overflow: hidden;
  background: #fbfdff;
}

.time-grid-head {
  display: grid;
  grid-template-columns: 64px repeat(var(--col-count), minmax(0, 1fr));
  background: #f5f8fc;
  border-bottom: 1px solid #e1e8f2;
}

.time-head-time,
.time-head-day {
  min-height: 54px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-right: 1px solid #e1e8f2;
}

.time-head-day {
  gap: 2px;

  span {
    font-size: 12px;
    color: #7f8fa3;
  }

  strong {
    font-size: 16px;
    color: #24324a;
    line-height: 1;
  }

  &.is-today {
    background: #edf4ff;
  }
}

.time-head-time {
  font-size: 12px;
  color: #8494a9;
  font-weight: 600;
}

.time-grid-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.time-row {
  display: grid;
  grid-template-columns: 64px repeat(var(--col-count), minmax(0, 1fr));
  min-height: 66px;
  border-bottom: 1px solid #e9eef6;
}

.time-hour {
  font-size: 12px;
  color: #7b8ca2;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 6px;
  border-right: 1px solid #e1e8f2;
  background: #f8fbff;
}

.time-cell {
  border-right: 1px solid #e1e8f2;
  padding: 3px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.time-task {
  border-left: 3px solid;
  border-radius: 8px;
  background: #f2f6fd;
  padding: 4px 6px;
}

.time-task-title {
  font-size: 12px;
  color: #24324a;
  line-height: 1.25;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-task-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #8a99ac;
  line-height: 1.2;
}

.month-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 6px;
  text-align: center;
  font-size: 12px;
  color: #8695a8;
  font-weight: 600;
}

.month-pane {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 0;
  flex: 1;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  flex: 1;
  min-height: 0;
  grid-auto-rows: minmax(0, 1fr);
}

.month-cell {
  min-height: 0;
  padding: 4px 6px;
  border-radius: 12px;
  background: #fbfcfe;
  border: 1px solid #edf1f6;

  &.other-month {
    background: #f5f7fa;

    .month-cell-date {
      color: #bcc6d2;
    }
  }

  &.is-today {
    border-color: #7da4ff;

    .month-cell-date {
      color: #5d8bff;
      font-weight: 700;
    }
  }
}

.month-cell-date {
  font-size: 12px;
  color: #607087;
  margin-bottom: 4px;
}

.month-cell-dots {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
  align-items: center;
}

.month-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.month-more {
  font-size: 10px;
  color: #8b98aa;
}

.quadrant-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  min-height: 0;
  height: 100%;
  flex: 1;
}

.quadrant-pane {
  flex: 1;
  min-height: 0;
  display: flex;
}

.quadrant-box {
  min-height: 0;
  padding: 12px;
  border-radius: 18px;
  background: var(--q-bg);
  display: flex;
  flex-direction: column;
}

.quadrant-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #24324a;
  font-size: 13px;
  font-weight: 700;
}

.quadrant-badge {
  width: 22px;
  height: 22px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
}

.quadrant-count {
  margin-left: auto;
  color: #8190a2;
  font-weight: 500;
}

.quadrant-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 0;
  flex: 1;
}

.quadrant-item {
  padding: 8px 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
}

.quadrant-item-desc {
  font-size: 12px;
  color: #24324a;
  font-weight: 600;
}

.quadrant-item-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #8997aa;
}

@media (max-width: 1400px) {
  .dashboard-page {
    height: auto;
  }

  .dashboard-split {
    grid-template-columns: 1fr;
  }

  .left-panel__inner {
    grid-template-rows: auto auto auto;
  }
}

@media (max-width: 1200px) {
  .panel-status-row.count-5,
  .panel-status-row.count-6 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .panel-middle-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;

    h2 {
      font-size: 26px;
    }
  }

  .dashboard-clock {
    width: 100%;
  }

  .panel-status-row.count-5,
  .panel-status-row.count-6,
  .quadrant-grid,
  .time-grid-head,
  .time-row {
    grid-template-columns: 1fr;
  }

  .time-head-time,
  .time-hour {
    display: none;
  }

  .pie-panel__body {
    grid-template-columns: 1fr;
  }
}
</style>
