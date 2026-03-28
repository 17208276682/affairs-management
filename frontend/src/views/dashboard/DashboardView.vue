<template>
  <div class="dashboard-page">
    <section class="dashboard-split">
      <div class="left-panel">
        <div class="left-panel__inner" :class="{ 'no-stats': !showChartPanel }">
          <!-- 负责人：两栏状态卡片（上级事务 + 部门事务） -->
          <template v-if="role === 'manager'">
            <div class="panel-row panel-status-group">
              <article class="panel-card status-group-card">
                <div class="status-group-card__header">
                  <span>上级事务</span>
                  <span class="status-group-total">事务总数：{{ statusCountMap.recv_total ?? 0 }}</span>
                </div>
                <div class="status-group__cards count-6">
                  <div
                    v-for="item in managerRecvStatuses"
                    :key="item.key"
                    class="status-card status-card--small"
                    :style="{ '--status-color': item.color, '--status-glow': item.glow }"
                  >
                    <div class="status-card__top">
                      <span class="status-dot"></span>
                      <span class="status-name">{{ item.label }}</span>
                    </div>
                    <strong class="status-value">{{ statusCountMap[item.key] ?? 0 }}</strong>
                  </div>
                </div>
              </article>
              <article class="panel-card status-group-card">
                <div class="status-group-card__header">
                  <span>部门事务</span>
                  <span class="status-group-total">事务总数：{{ statusCountMap.dept_total ?? 0 }}</span>
                </div>
                <div class="status-group__cards count-6">
                  <div
                    v-for="item in managerDeptStatuses"
                    :key="item.key"
                    class="status-card status-card--small"
                    :style="{ '--status-color': item.color, '--status-glow': item.glow }"
                  >
                    <div class="status-card__top">
                      <span class="status-dot"></span>
                      <span class="status-name">{{ item.label }}</span>
                    </div>
                    <strong class="status-value">{{ statusCountMap[item.key] ?? 0 }}</strong>
                  </div>
                </div>
              </article>
            </div>
          </template>

          <!-- 其他角色：单栏状态卡片 -->
          <div v-else class="panel-row panel-status-row" :class="`count-${panelConfig.statuses.length}`">
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
                  <h3>{{ panelConfig.activityTitle }}</h3>
                </div>
                <div class="activity-header-right">
                  <template v-if="role === 'manager'">
                    <span
                      v-for="tab in activityTabs"
                      :key="tab.value"
                      class="activity-tab"
                      :class="{ active: activityFilter === tab.value }"
                      @click="activityFilter = tab.value"
                    >{{ tab.label }}</span>
                  </template>
                  <span class="panel-card__badge">{{ displayedActivities.length }} 条</span>
                </div>
              </div>

              <div v-if="displayedActivities.length" class="activity-list">
                <div v-for="activity in displayedActivities" :key="activity.id" class="activity-item">
                  <div class="activity-content">
                    <span class="activity-text">{{ activity.content }}</span>
                    <span class="activity-time">{{ formatRelativeTime(activity.time) }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无动态" :image-size="54" />
            </article>

            <article class="panel-card chart-panel pie-panel">
              <div class="panel-card__header">
                <div>
                  <h3>{{ panelConfig.pieTitle }}</h3>
                </div>
                <div class="pie-header-right">
                  <template v-if="role === 'manager'">
                    <span
                      v-for="tab in pieTabs"
                      :key="tab.value"
                      class="activity-tab"
                      :class="{ active: pieFilter === tab.value }"
                      @click="pieFilter = tab.value"
                    >{{ tab.label }}</span>
                  </template>
                  <span class="panel-card__badge">共 {{ pieTotal }} 项</span>
                </div>
              </div>

              <div class="pie-panel__body">
                <VChart class="chart chart-pie" :option="pieOption" autoresize />
                <div class="pie-legend">
                  <div v-for="item in filteredPieDataset" :key="item.level" class="pie-legend__item">
                    <span class="pie-legend__dot" :style="{ background: item.color }"></span>
                    <span class="pie-legend__label">{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                </div>
              </div>
            </article>
          </div>

          <div v-if="showChartPanel" class="panel-row panel-chart-row">
            <article class="panel-card chart-panel bar-panel">
              <div class="bar-panel__header">
                <!-- 第一行：标题 + 控制区 -->
                <div class="bar-header-top">
                  <div class="bar-header-title">
                    <h3>事务统计</h3>
                  </div>
                  <div v-if="showMonthSelector" class="line-month-selector">
                    <el-button size="small" :type="lineMonthMode === 'prev' ? 'primary' : ''" @click="setLineMonth('prev')">上月</el-button>
                    <el-button size="small" :type="lineMonthMode === 'current' ? 'primary' : ''" @click="setLineMonth('current')">本月</el-button>
                    <el-button size="small" :type="lineMonthMode === 'next' ? 'primary' : ''" @click="setLineMonth('next')">次月</el-button>
                    <el-date-picker
                      v-model="lineCustomMonth"
                      type="month"
                      placeholder="自定义"
                      size="small"
                      style="width: 110px"
                      @change="onCustomMonthChange"
                    />
                  </div>
                  <template v-if="role === 'manager'">
                    <el-radio-group v-model="managerChartMode" size="small" class="bar-mode-switcher">
                      <el-radio-button value="person">部门事务</el-radio-button>
                      <el-radio-button value="time">上级事务</el-radio-button>
                    </el-radio-group>
                  </template>
                </div>
              </div>

              <VChart class="chart chart-bar" :option="role === 'staff' ? lineOption : (role === 'manager' && managerChartMode === 'time') ? managerLineOption : barOption" autoresize />
            </article>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="view-shell">
          <div class="view-shell__header">
            <div>
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
                        >
                          <span class="time-task-dot" :style="{ background: getTaskStatusColor(task) }"></span>
                          <span class="time-task-title">{{ (task.description || task.title).substring(0, 20) }}</span>
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
                        >
                          <span class="time-task-dot" :style="{ background: getTaskStatusColor(task) }"></span>
                          <span class="time-task-title">{{ (task.description || task.title).substring(0, 20) }}</span>
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
                        >
                          <span class="time-task-dot" :style="{ background: getTaskStatusColor(task) }"></span>
                          <span class="time-task-title">{{ (task.description || task.title).substring(0, 16) }}</span>
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
                  <el-tooltip
                    v-for="(cell, idx) in monthCells"
                    :key="idx"
                    :disabled="!cell.tasks.length"
                    placement="top"
                    effect="dark"
                    :show-after="200"
                  >
                    <template #content>
                      <div v-for="task in cell.tasks" :key="task.id" class="month-tip-row">
                        <span class="month-tip-dot" :style="{ background: getTaskStatusColor(task) }"></span>
                        {{ (task.description || task.title).substring(0, 15) }}
                      </div>
                    </template>
                    <div
                      class="month-cell"
                      :class="{ 'other-month': !cell.isCurrentMonth, 'is-today': cell.isToday }"
                    >
                      <div class="month-cell-date">{{ cell.day }}</div>
                      <div v-if="cell.tasks.length" class="month-cell-tasks">
                        <div
                          v-for="task in cell.tasks.slice(0, 4)"
                          :key="task.id"
                          class="month-task-item"
                        >
                          <span class="month-task-dot" :style="{ background: getTaskStatusColor(task) }"></span>
                          <span class="month-task-text">{{ (task.description || task.title).substring(0, 4) }}</span>
                        </div>
                        <span v-if="cell.tasks.length > 4" class="month-task-more">更多 +{{ cell.tasks.length - 4 }}</span>
                      </div>
                    </div>
                  </el-tooltip>
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
                        <span class="quadrant-item-dot" :style="{ background: getTaskStatusColor(task) }"></span>
                        <span class="quadrant-item-desc">{{ (task.description || task.title).substring(0, 20) }}</span>
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
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { ArrowLeftBold, ArrowRightBold } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores'
import { TASK_LEVEL_MAP } from '@/utils/constants'
import { formatRelativeTime } from '@/utils/format'
import { getRecentActivitiesApi, getStatsByDeptApi, getStatsByPersonApi, getStatsOverviewApi } from '@/api/statistics'
import { getSubordinatesApi } from '@/api/organization'
import { getTaskListApi } from '@/api/task'
import type { DeptStats, PersonStats, StatsOverview, Task, TaskLevel, User } from '@/types'

use([CanvasRenderer, PieChart, BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent])

type PanelRole = 'director' | 'manager' | 'staff'
type StatusKey = 'assigned' | 'pending' | 'completed' | 'rejected' | 'cancelled' | 'overdue'
  | 'onTimeCompleted' | 'overdueCompleted' | 'failedReview' | 'todoTasks' | 'overdueUnfinished'
  | 'recv_onTimeCompleted' | 'recv_overdueCompleted' | 'recv_failedReview' | 'recv_todoTasks' | 'recv_overdueUnfinished' | 'recv_cancelled'
  | 'dept_onTimeCompleted' | 'dept_overdueCompleted' | 'dept_failedReview' | 'dept_todoTasks' | 'dept_overdueUnfinished' | 'dept_cancelled'
  | 'recv_total' | 'dept_total'

interface StatusCardConfig {
  key: StatusKey
  label: string
  color: string
  glow: string
  note: string
}

const userStore = useUserStore()

const roleActivities = ref<{ id: string; type: string; content: string; time: string; category?: string }[]>([])
const activityFilter = ref<'all' | 'superior' | 'dept'>('all')
const activityTabs = [
  { label: '全部', value: 'all' as const },
  { label: '上级', value: 'superior' as const },
  { label: '部门', value: 'dept' as const },
]
const displayedActivities = computed(() => {
  if (activityFilter.value === 'all') return roleActivities.value
  return roleActivities.value.filter(a => a.category === activityFilter.value)
})
const assignedTasks = ref<Task[]>([])
const scopeTasks = ref<Task[]>([])
const todoTasks = ref<Task[]>([])
const receivedTasks = ref<Task[]>([])
const deptStats = ref<DeptStats[]>([])
const personStats = ref<PersonStats[]>([])
const statsOverview = ref<StatsOverview | null>(null)
const managedUsers = ref<User[]>([])
const nowLabel = ref(formatNow())

const activeTab = ref('day')
const dayOffset = ref(0)
const threeDayOffset = ref(0)
const weekOffset = ref(0)
const monthOffset = ref(0)
const HOURS = Array.from({ length: 24 }, (_, i) => i)
const showDirectorStats = computed(() => userStore.isDirector)
const showChartPanel = computed(() => true)

let timer: number | undefined

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const submittedCount = computed(() => statsOverview.value?.submittedTasks ?? 0)

const managerRecvStatuses: StatusCardConfig[] = [
  { key: 'recv_onTimeCompleted', label: '按时完成数', color: '#4dbb87', glow: 'rgba(77, 187, 135, 0.18)', note: '按时完成并审核通过' },
  { key: 'recv_overdueCompleted', label: '逾期完成数', color: '#f3a64b', glow: 'rgba(243, 166, 75, 0.18)', note: '逾期完成并审核通过' },
  { key: 'recv_failedReview', label: '审核未通过', color: '#e06472', glow: 'rgba(224, 100, 114, 0.16)', note: '审核未通过的事务' },
  { key: 'recv_todoTasks', label: '待办事务数', color: '#5a8dee', glow: 'rgba(90, 141, 238, 0.16)', note: '未到截止且未完成' },
  { key: 'recv_overdueUnfinished', label: '逾期未完成', color: '#8B5CF6', glow: 'rgba(139, 92, 246, 0.16)', note: '超过截止未完成' },
  { key: 'recv_cancelled', label: '作废事务数', color: '#96a0af', glow: 'rgba(150, 160, 175, 0.16)', note: '已标记作废' },
]
const managerDeptStatuses: StatusCardConfig[] = [
  { key: 'dept_onTimeCompleted', label: '按时完成数', color: '#4dbb87', glow: 'rgba(77, 187, 135, 0.18)', note: '按时完成并审核通过' },
  { key: 'dept_overdueCompleted', label: '逾期完成数', color: '#f3a64b', glow: 'rgba(243, 166, 75, 0.18)', note: '逾期完成并审核通过' },
  { key: 'dept_failedReview', label: '审核未通过', color: '#e06472', glow: 'rgba(224, 100, 114, 0.16)', note: '审核未通过的事务' },
  { key: 'dept_todoTasks', label: '待办事务数', color: '#5a8dee', glow: 'rgba(90, 141, 238, 0.16)', note: '未到截止且未完成' },
  { key: 'dept_overdueUnfinished', label: '逾期未完成', color: '#8B5CF6', glow: 'rgba(139, 92, 246, 0.16)', note: '超过截止未完成' },
  { key: 'dept_cancelled', label: '作废事务数', color: '#96a0af', glow: 'rgba(150, 160, 175, 0.16)', note: '已标记作废' },
]

const role = computed<PanelRole>(() => {
  const cr = userStore.currentRole
  if (cr === 'manager') return 'manager'
  if (cr === 'staff') return 'staff'
  return 'director'
})

const showMonthSelector = computed(() => {
  if (role.value === 'staff') return true
  if (role.value === 'manager' && managerChartMode.value === 'time') return true
  return false
})

const currentChartSubtitle = computed(() => {
  if (role.value === 'manager') return managerChartMode.value === 'person' ? '部门事务' : '上级事务'
  if (role.value === 'staff') return '时间统计'
  return '人员统计'
})

const panelConfig = computed(() => {
  const common = {
    director: {
      title: '高级管理者事务驾驶舱',
      subtitle: '全局协同视角',
      activityTitle: '事务动态',
      pieTitle: '事务级别',
      barTitle: '人员事务状态分布',
      statuses: [
        { key: 'assigned', label: '下达事务数', color: '#4F6EF7', glow: 'rgba(79, 110, 247, 0.16)', note: '下达给下级\n人员的总数' },
        { key: 'onTimeCompleted', label: '按时完成数', color: '#4dbb87', glow: 'rgba(77, 187, 135, 0.18)', note: '按时完成并审\n核通过的事务' },
        { key: 'overdueCompleted', label: '逾期完成数', color: '#f3a64b', glow: 'rgba(243, 166, 75, 0.18)', note: '逾期完成并审\n核通过的事务' },
        { key: 'failedReview', label: '审核未通过', color: '#e06472', glow: 'rgba(224, 100, 114, 0.16)', note: '按时或逾期的\n未通过的事务' },
        { key: 'todoTasks', label: '待办事务数', color: '#5a8dee', glow: 'rgba(90, 141, 238, 0.16)', note: '未到截止时间\n未完成的事务' },
        { key: 'overdueUnfinished', label: '逾期未完成', color: '#8B5CF6', glow: 'rgba(139, 92, 246, 0.16)', note: '逾期并且未按\n时完成的事务' },
        { key: 'cancelled', label: '作废事务数', color: '#96a0af', glow: 'rgba(150, 160, 175, 0.16)', note: '下发任务标记\n为作废的事务' },
      ] satisfies StatusCardConfig[],
    },
    manager: {
      title: '中级管理者事务驾驶舱',
      subtitle: '部门协同视角',
      activityTitle: '事务动态',
      pieTitle: '事务级别',
      barTitle: '人员事务状态分布',
      statuses: [] as StatusCardConfig[],
    },
    staff: {
      title: '普通员工事务驾驶舱',
      subtitle: '个人执行视角',
      activityTitle: '事务动态',
      pieTitle: '事务级别',
      barTitle: '每日事务状态趋势',
      statuses: [
        { key: 'onTimeCompleted', label: '按时完成数', color: '#4dbb87', glow: 'rgba(77, 187, 135, 0.18)', note: '按时完成并审\n核通过的事务' },
        { key: 'overdueCompleted', label: '逾期完成数', color: '#f3a64b', glow: 'rgba(243, 166, 75, 0.18)', note: '逾期完成并审\n核通过的事务' },
        { key: 'failedReview', label: '审核未通过', color: '#e06472', glow: 'rgba(224, 100, 114, 0.16)', note: '按时或逾期的\n未通过的事务' },
        { key: 'todoTasks', label: '待办事务数', color: '#5a8dee', glow: 'rgba(90, 141, 238, 0.16)', note: '未到截止时间\n未完成的事务' },
        { key: 'overdueUnfinished', label: '逾期未完成', color: '#8B5CF6', glow: 'rgba(139, 92, 246, 0.16)', note: '逾期并且未按\n时完成的事务' },
        { key: 'cancelled', label: '作废事务数', color: '#96a0af', glow: 'rgba(150, 160, 175, 0.16)', note: '下发任务标记\n为作废的事务' },
      ] satisfies StatusCardConfig[],
    },
  }

  return common[role.value]
})

const directorRootTasks = computed(() => assignedTasks.value.filter(task => !task.parentTaskId))
const taskPool = computed(() => (role.value === 'director' ? directorRootTasks.value : receivedTasks.value))
const allCalendarTasks = computed(() => {
  const source = role.value === 'director' ? directorRootTasks.value
    : role.value === 'manager' ? [...receivedTasks.value, ...directorRootTasks.value]
    : taskPool.value
  return [...source].sort((a, b) => new Date(a.completionDeadline).getTime() - new Date(b.completionDeadline).getTime())
})

const statusCountMap = computed<Record<StatusKey, number>>(() => {
  const ov = statsOverview.value

  // 通用的任务分类函数
  function classifyTasks(tasks: Task[]) {
    const now = new Date()
    let onTimeCompleted = 0, overdueCompleted = 0, failedReview = 0, todoTasks = 0, overdueUnfinished = 0, cancelled = 0
    for (const task of tasks) {
      const status = task.status
      const deadline = task.completionDeadline ? new Date(task.completionDeadline) : null
      const completionTime = task.completionTime ? new Date(task.completionTime) : null
      if (['approved', 'completed'].includes(status)) {
        if (!deadline || (completionTime && completionTime <= deadline)) onTimeCompleted++
        else overdueCompleted++
      } else if (status === 'rejected') {
        failedReview++
      } else if (status === 'cancelled') {
        cancelled++
      } else if (deadline && deadline < now) {
        overdueUnfinished++
      } else {
        todoTasks++
      }
    }
    return { onTimeCompleted, overdueCompleted, failedReview, todoTasks, overdueUnfinished, cancelled }
  }

  // manager: 分别统计收到的事务和下发的事务
  const recv = classifyTasks(receivedTasks.value)
  const dept = classifyTasks(directorRootTasks.value)

  // director/staff: 用本地任务分类，避免转交链重复计数
  const localPool = role.value === 'director' ? directorRootTasks.value : receivedTasks.value
  const local = classifyTasks(localPool)

  return {
    assigned: assignedTasks.value.length,
    pending: todoTasks.value.length,
    completed: scopeTasks.value.filter(task => isDisplayCompleted(task.status)).length,
    rejected: scopeTasks.value.filter(task => task.status === 'rejected').length,
    cancelled: local.cancelled,
    overdue: scopeTasks.value.filter(task => task.status === 'overdue').length,
    onTimeCompleted: local.onTimeCompleted,
    overdueCompleted: local.overdueCompleted,
    failedReview: local.failedReview,
    todoTasks: local.todoTasks,
    overdueUnfinished: local.overdueUnfinished,
    // manager 上级事务（收到的）
    recv_onTimeCompleted: recv.onTimeCompleted,
    recv_overdueCompleted: recv.overdueCompleted,
    recv_failedReview: recv.failedReview,
    recv_todoTasks: recv.todoTasks,
    recv_overdueUnfinished: recv.overdueUnfinished,
    recv_cancelled: recv.cancelled,
    // manager 部门事务（下发的）
    dept_onTimeCompleted: dept.onTimeCompleted,
    dept_overdueCompleted: dept.overdueCompleted,
    dept_failedReview: dept.failedReview,
    dept_todoTasks: dept.todoTasks,
    dept_overdueUnfinished: dept.overdueUnfinished,
    dept_cancelled: dept.cancelled,
    // manager 事务总数
    recv_total: recv.onTimeCompleted + recv.overdueCompleted + recv.failedReview + recv.todoTasks + recv.overdueUnfinished + recv.cancelled,
    dept_total: dept.onTimeCompleted + dept.overdueCompleted + dept.failedReview + dept.todoTasks + dept.overdueUnfinished + dept.cancelled,
  }
})

const LEVEL_CHINESE_MAP: Record<string, string> = {
  A: '重要紧急',
  B: '不重要紧急',
  C: '重要不紧急',
  D: '不重要不紧急',
}

const pieFilter = ref<'all' | 'superior' | 'dept'>('all')
const pieTabs = [
  { label: '全部', value: 'all' as const },
  { label: '上级', value: 'superior' as const },
  { label: '部门', value: 'dept' as const },
]

const pieDataset = computed(() => {
  const useChinese = true
  const pool = role.value === 'manager'
    ? [...receivedTasks.value, ...directorRootTasks.value]
    : role.value === 'staff' ? receivedTasks.value : taskPool.value
  return (['A', 'B', 'C', 'D'] as TaskLevel[]).map(level => ({
    level,
    label: useChinese ? LEVEL_CHINESE_MAP[level] : `${level}级`,
    value: pool.filter(task => task.level === level).length,
    color: TASK_LEVEL_MAP[level].color,
  }))
})

const filteredPieDataset = computed(() => {
  if (role.value !== 'manager' || pieFilter.value === 'all') return pieDataset.value
  const pool = pieFilter.value === 'superior' ? receivedTasks.value : directorRootTasks.value
  return (['A', 'B', 'C', 'D'] as TaskLevel[]).map(level => ({
    level,
    label: LEVEL_CHINESE_MAP[level],
    value: pool.filter(task => task.level === level).length,
    color: TASK_LEVEL_MAP[level].color,
  }))
})

const pieTotal = computed(() => filteredPieDataset.value.reduce((sum, item) => sum + item.value, 0))

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
        show: false,
      },
      labelLine: {
        show: false,
      },
      itemStyle: {
        borderRadius: 12,
        borderColor: '#fffdfb',
        borderWidth: 4,
      },
      data: filteredPieDataset.value.map(item => ({
        name: item.label,
        value: item.value,
        itemStyle: { color: item.color },
      })),
    },
  ],
}))

const barSource = computed(() => {
  if (role.value === 'director' || role.value === 'manager') {
    // 总经办/负责人：先用管理的人员列表作为基准（确保无任务的人也显示）
    const initEntry = () => ({ onTimeCompleted: 0, overdueCompleted: 0, failedReview: 0, todoTasks: 0, overdueUnfinished: 0, cancelledTasks: 0 })
    const executorMap = new Map<string, { label: string } & ReturnType<typeof initEntry>>()
    for (const u of managedUsers.value) {
      executorMap.set(u.name, { label: u.name, ...initEntry() })
    }
    const now = new Date()
    for (const task of directorRootTasks.value) {
      const name = task.executorName
      if (!executorMap.has(name)) {
        executorMap.set(name, { label: name, ...initEntry() })
      }
      const entry = executorMap.get(name)!
      const status = task.status
      const deadline = task.completionDeadline ? new Date(task.completionDeadline) : null
      const completionTime = task.completionTime ? new Date(task.completionTime) : null

      if (['approved', 'completed'].includes(status)) {
        if (!deadline || (completionTime && completionTime <= deadline)) {
          entry.onTimeCompleted++
        } else {
          entry.overdueCompleted++
        }
      } else if (status === 'rejected') {
        entry.failedReview++
      } else if (status === 'cancelled') {
        entry.cancelledTasks++
      } else if (deadline && deadline < now) {
        entry.overdueUnfinished++
      } else {
        entry.todoTasks++
      }
    }
    return Array.from(executorMap.values())
  }
  return deptStats.value.map(d => ({
    label: d.deptName,
    total: d.total,
    completed: d.completed,
  }))
})

const STACKED_SERIES = [
  { key: 'onTimeCompleted', name: '按时完成', color: '#4dbb87' },
  { key: 'overdueCompleted', name: '逾期完成', color: '#f3a64b' },
  { key: 'failedReview', name: '审核未通过', color: '#e06472' },
  { key: 'todoTasks', name: '待办事务', color: '#5a8dee' },
  { key: 'overdueUnfinished', name: '逾期未完成', color: '#8B5CF6' },
  { key: 'cancelledTasks', name: '已作废', color: '#96a0af' },
] as const

const barOption = computed<EChartsOption>(() => {
  const dataLen = barSource.value.length
  const maxVisible = 8
  const needScroll = dataLen > maxVisible
  const endPercent = needScroll ? Math.round((maxVisible / dataLen) * 100) : 100

  if (role.value === 'director' || role.value === 'manager') {
    return {
      grid: { top: 10, right: 100, bottom: 6, left: 12, containLabel: true },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(36, 50, 74, 0.92)',
        borderWidth: 0,
        textStyle: { color: '#fff' },
      },
      legend: {
        orient: 'vertical',
        right: 0,
        top: 'middle',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 14,
        textStyle: { color: '#708097', fontSize: 11 },
      },
      yAxis: {
        type: 'category',
        data: barSource.value.map(item => item.label),
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
        barMaxWidth: 32,
        itemStyle: { color: s.color },
        data: barSource.value.map(item => (item as any)[s.key] ?? 0),
      })),
    }
  }

  return {
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
    yAxis: {
      type: 'category',
      data: barSource.value.map(item => item.label),
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
    series: [
      {
        name: '总事务数',
        type: 'bar',
        barMaxWidth: 22,
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
          color: '#93b7ff',
        },
        data: barSource.value.map(item => (item as any).total ?? 0),
      },
      {
        name: '已完成数',
        type: 'bar',
        barMaxWidth: 22,
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
          color: '#67c8a0',
        },
        data: barSource.value.map(item => (item as any).completed ?? 0),
      },
    ],
  }
})

// ——— 员工折线图：月度选择器 + 每日六线 ———
const lineMonthMode = ref<'prev' | 'current' | 'next' | 'custom'>('current')
const lineCustomMonth = ref<Date | null>(null)
const managerChartMode = ref<'time' | 'person'>('person')
const managerSelectedUser = ref('')

const lineMonthBase = computed(() => {
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
  const base = lineMonthBase.value.startOf('month')
  const daysInMonth = base.daysInMonth()
  const dates: string[] = []
  const dataMap: Record<string, number[]> = {}
  for (const s of LINE_SERIES_DEF) dataMap[s.key] = []

  for (let d = 0; d < daysInMonth; d++) {
    const dateStr = base.add(d, 'day').format('YYYY-MM-DD')
    dates.push(base.add(d, 'day').format('M/D'))
    const counts: Record<string, number> = {}
    for (const s of LINE_SERIES_DEF) counts[s.key] = 0

    for (const task of receivedTasks.value) {
      // 按事务创建日期归类到当天
      const taskDate = dayjs(task.createdAt).format('YYYY-MM-DD')
      if (taskDate !== dateStr) continue

      const status = task.status
      const deadline = task.completionDeadline ? new Date(task.completionDeadline) : null
      const completionTime = task.completionTime ? new Date(task.completionTime) : null
      const now = new Date()

      if (['approved', 'completed'].includes(status)) {
        if (!deadline || (completionTime && completionTime <= deadline)) {
          counts.onTimeCompleted++
        } else {
          counts.overdueCompleted++
        }
      } else if (status === 'rejected') {
        counts.failedReview++
      } else if (status === 'cancelled') {
        counts.cancelledTasks++
      } else if (deadline && deadline < now) {
        counts.overdueUnfinished++
      } else {
        counts.todoTasks++
      }
    }

    for (const s of LINE_SERIES_DEF) dataMap[s.key].push(counts[s.key])
  }

  return { dates, dataMap }
})

const lineOption = computed<EChartsOption>(() => ({
  grid: { top: 10, right: 100, bottom: 6, left: 12, containLabel: true },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(36, 50, 74, 0.92)',
    borderWidth: 0,
    textStyle: { color: '#fff' },
  },
  legend: {
    orient: 'vertical',
    right: 0,
    top: 'middle',
    itemWidth: 16,
    itemHeight: 3,
    itemGap: 14,
    textStyle: { color: '#708097', fontSize: 11 },
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

// ——— 负责人折线图：按人员筛选 + 每日六线 ———
const managerLineChartData = computed(() => {
  const base = lineMonthBase.value.startOf('month')
  const daysInMonth = base.daysInMonth()
  const dates: string[] = []
  const dataMap: Record<string, number[]> = {}
  for (const s of LINE_SERIES_DEF) dataMap[s.key] = []

  // 负责人时间统计：统计上级下发给自己的事务
  const taskSource = receivedTasks.value

  for (let d = 0; d < daysInMonth; d++) {
    const dateStr = base.add(d, 'day').format('YYYY-MM-DD')
    dates.push(base.add(d, 'day').format('M/D'))
    const counts: Record<string, number> = {}
    for (const s of LINE_SERIES_DEF) counts[s.key] = 0

    for (const task of taskSource) {
      const taskDate = dayjs(task.createdAt).format('YYYY-MM-DD')
      if (taskDate !== dateStr) continue

      const status = task.status
      const deadline = task.completionDeadline ? new Date(task.completionDeadline) : null
      const completionTime = task.completionTime ? new Date(task.completionTime) : null
      const now = new Date()

      if (['approved', 'completed'].includes(status)) {
        if (!deadline || (completionTime && completionTime <= deadline)) {
          counts.onTimeCompleted++
        } else {
          counts.overdueCompleted++
        }
      } else if (status === 'rejected') {
        counts.failedReview++
      } else if (status === 'cancelled') {
        counts.cancelledTasks++
      } else if (deadline && deadline < now) {
        counts.overdueUnfinished++
      } else {
        counts.todoTasks++
      }
    }

    for (const s of LINE_SERIES_DEF) dataMap[s.key].push(counts[s.key])
  }

  return { dates, dataMap }
})

const managerLineOption = computed<EChartsOption>(() => ({
  grid: { top: 10, right: 100, bottom: 6, left: 12, containLabel: true },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(36, 50, 74, 0.92)',
    borderWidth: 0,
    textStyle: { color: '#fff' },
  },
  legend: {
    orient: 'vertical',
    right: 0,
    top: 'middle',
    itemWidth: 16,
    itemHeight: 3,
    itemGap: 14,
    textStyle: { color: '#708097', fontSize: 11 },
  },
  xAxis: {
    type: 'category',
    data: managerLineChartData.value.dates,
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
    data: managerLineChartData.value.dataMap[s.key],
  })),
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
    return { label: '待审核', color: '#7e68e8', bgColor: '#f2efff' }
  }
  if (status === 'rejected') {
    return { label: '不通过', color: '#d45767', bgColor: '#fef0f2' }
  }
  if (status === 'overdue') {
    return { label: '已逾期', color: '#d45767', bgColor: '#fef0f2' }
  }
  if (status === 'cancelled') {
    return { label: '已作废', color: '#9099a8', bgColor: '#f2f4f7' }
  }
  return { label: '待处理', color: '#ec9a39', bgColor: '#fff8e9' }
}

/** 根据事务状态返回总览卡片对应颜色（按时完成/逾期完成/审核未通过/待办/逾期未完成/作废） */
function getTaskStatusColor(task: Task): string {
  const status = task.status
  const deadline = task.completionDeadline ? new Date(task.completionDeadline) : null
  const completionTime = task.completionTime ? new Date(task.completionTime) : null
  const now = new Date()
  if (['approved', 'completed'].includes(status)) {
    if (!deadline || (completionTime && completionTime <= deadline)) return '#4dbb87' // 按时完成
    return '#f3a64b' // 逾期完成
  }
  if (status === 'rejected') return '#e06472'
  if (status === 'cancelled') return '#96a0af'
  if (deadline && deadline < now) return '#8B5CF6' // 逾期未完成
  return '#5a8dee' // 待办
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
  const [activitiesRes, assignedRes, scopeRes, todoRes, receivedRes, deptRes, personRes, overviewRes, subsRes] = await Promise.all([
    getRecentActivitiesApi(),
    getTaskListApi({ type: 'assigned', page: 1, pageSize: 200 }),
    getTaskListApi({ type: 'scope', page: 1, pageSize: 200 }),
    getTaskListApi({ type: 'todo', page: 1, pageSize: 200 }),
    getTaskListApi({ type: 'received', page: 1, pageSize: 200 }),
    getStatsByDeptApi(),
    getStatsByPersonApi(),
    getStatsOverviewApi(),
    getSubordinatesApi().catch(() => ({ data: [] as User[] })),
  ])

  roleActivities.value = activitiesRes.data
  assignedTasks.value = assignedRes.data.list
  scopeTasks.value = scopeRes.data.list
  todoTasks.value = todoRes.data.list
  receivedTasks.value = receivedRes.data.list
  deptStats.value = deptRes.data
  personStats.value = personRes.data
  statsOverview.value = overviewRes.data
  managedUsers.value = subsRes.data
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
  overflow: hidden;
}

.dashboard-split {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
}

.left-panel,
.right-panel {
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.left-panel__inner,
.view-shell {
  flex: 1;
  height: 100%;
  min-height: 0;
}

.left-panel__inner {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;

  &.no-stats {
    grid-template-rows: auto minmax(0, 1fr);
  }

  > .panel-row {
    min-height: 0;
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

  &.count-7 {
    grid-template-columns: repeat(7, minmax(0, 1fr));
  }
}

.panel-status-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.status-group-card {
  padding: 16px;
}

.status-group-card__header {
  font-size: 15px;
  font-weight: 600;
  color: #22324a;
  margin-bottom: 12px;
  padding-left: 2px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-group-total {
  font-size: 14px;
  font-weight: 600;
  color: #5a8dee;
}

.status-group__cards {
  display: grid;
  gap: 8px;

  &.count-6 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.status-card--small {
  padding: 10px 12px;
  border-radius: 16px;

  .status-value {
    font-size: 22px;
  }

  .status-name {
    font-size: 11px;
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
  gap: 6px;

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
  white-space: pre-line;
  line-height: 1.5;
}

.panel-middle-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
  min-height: 0;
}

.panel-chart-row {
  display: flex;
  flex-direction: column;
  min-height: 0;

  > .panel-card {
    flex: 1;
    min-height: 0;
  }
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

.line-month-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.manager-chart-controls {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  flex-shrink: 0;
}

.bar-panel__header {
  padding: 0 0 4px;
}

.bar-header-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-right: auto;

  h3 {
    font-size: 16px;
    font-weight: 700;
    color: #22324a;
    white-space: nowrap;
  }
}

.bar-mode-switcher {
  flex-shrink: 0;
}

.bar-header-sub {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.bar-subtitle {
  font-size: 12px;
  color: #8ba0c2;
  flex-shrink: 0;
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

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f0f2f5;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c0c8d4;
    border-radius: 3px;

    &:hover {
      background: #a0aab6;
    }
  }
}

.activity-item {
  display: block;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  .activity-text {
    color: #24324a;
    font-size: 13px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
    min-width: 0;
  }

  .activity-time {
    color: #95a3b6;
    font-size: 12px;
    white-space: nowrap;
    flex-shrink: 0;
  }
}

.activity-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pie-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-tab {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
  cursor: pointer;
  color: #8ba0c2;
  background: #f0f4f8;
  transition: all 0.2s;

  &:hover {
    color: #5a8dee;
  }

  &.active {
    color: #fff;
    background: #5a8dee;
  }
}

.pie-panel__body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 150px;
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
  white-space: nowrap;
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
  display: flex;
  align-items: center;
  gap: 5px;
  border-radius: 6px;
  background: #f2f6fd;
  padding: 3px 6px;
}

.time-task-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.time-task-title {
  font-size: 11px;
  color: #24324a;
  line-height: 1.3;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  position: relative;

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
  font-size: 13px;
  color: #607087;
  margin-bottom: 4px;
  font-weight: 500;
}

.month-cell-tasks {
  display: flex;
  flex-direction: column;
  gap: 1px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.month-task-item {
  display: flex;
  align-items: center;
  gap: 3px;
}

.month-task-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.month-task-text {
  font-size: 13px;
  color: #475669;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.month-task-more {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 10px;
  color: #5a8dee;
  font-weight: 600;
}

.month-tip-row {
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1.8;
  font-size: 12px;
}

.month-tip-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
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
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
}

.quadrant-item-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.quadrant-item-desc {
  font-size: 12px;
  color: #24324a;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 统一到系统页面风格（轻量卡片 + 统一间距） ===== */
.dashboard-page {
  gap: $spacing-md;
}

.dashboard-header {
  align-items: center;
  padding: 0;

  h2 {
    margin: 2px 0 4px;
    font-size: $font-size-xl;
    line-height: 1.3;
    color: $text-primary;
    font-weight: 600;
  }
}

.dashboard-kicker {
  color: $primary;
  font-size: $font-size-xs;
  letter-spacing: 0.08em;
  text-transform: none;
}

.dashboard-meta {
  color: $text-secondary;
  font-size: $font-size-sm;
}

.dashboard-clock {
  min-width: 176px;
  padding: 10px 14px;
  border-radius: $radius-md;
  background: $bg-card;
  border: 1px solid $border-light;
  box-shadow: $shadow-sm;
  gap: 4px;
  color: $text-secondary;

  strong {
    font-size: $font-size-lg;
    color: $text-primary;
  }
}

.dashboard-split {
  gap: $spacing-md;
}

.left-panel__inner {
  gap: $spacing-sm;
}

.panel-status-row {
  gap: $spacing-sm;
}

.status-card {
  padding: 12px 14px;
  border-radius: $radius-md;
  background: $bg-card;
  border: 1px solid $border-light;
  box-shadow: $shadow-sm;

  &::after {
    content: none;
  }
}

.status-dot {
  box-shadow: none;
}

.status-name {
  color: $text-regular;
  font-size: $font-size-sm;
}

.status-value {
  font-size: 26px;
  color: $text-primary;
}

.status-note {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.panel-middle-row {
  gap: $spacing-sm;
}

.panel-card,
.view-shell {
  border-radius: $radius-md;
  background: $bg-card;
  border: 1px solid $border-light;
  box-shadow: $shadow-card;
}

.panel-card {
  padding: $spacing-md;
}

.panel-card__header,
.view-shell__header {
  margin-bottom: $spacing-sm;

  h3 {
    margin-top: 2px;
    font-size: $font-size-md;
    color: $text-primary;
    font-weight: 600;
  }
}

.panel-card__eyebrow {
  font-size: $font-size-xs;
  letter-spacing: 0.06em;
  text-transform: none;
  color: $text-secondary;
}

.panel-card__badge {
  padding: 4px 10px;
  border-radius: $radius-round;
  background: $bg-hover;
  color: $text-secondary;
  font-size: $font-size-xs;
}

.activity-item {
  padding: 10px 12px;
  border-radius: $radius-sm;
  background: $bg-card;
  border: 1px solid $border-lighter;
}

.activity-content {
  p {
    color: $text-primary;
    font-size: $font-size-sm;
  }

  span {
    color: $text-secondary;
    font-size: $font-size-xs;
  }
}

.pie-legend__item {
  border-radius: $radius-sm;
  background: $bg-card;
  border: 1px solid $border-lighter;

  strong {
    color: $text-primary;
  }
}

.pie-legend__label {
  color: $text-regular;
}

.view-shell {
  padding: $spacing-md;
}

.day-header,
.week-header,
.month-header {
  margin-bottom: $spacing-sm;

  .day-title,
  .week-title,
  .month-title {
    color: $text-primary;
    font-size: $font-size-md;
  }
}

.time-grid {
  border: 1px solid $border-light;
  border-radius: $radius-sm;
  background: $bg-card;
}

.time-grid-head {
  background: $bg-page;
  border-bottom: 1px solid $border-light;
}

.time-head-time,
.time-head-day {
  border-right: 1px solid $border-light;
}

.time-head-day {
  span {
    color: $text-secondary;
  }

  strong {
    color: $text-primary;
  }

  &.is-today {
    background: $primary-light;
  }
}

.time-head-time {
  color: $text-secondary;
}

.time-row {
  border-bottom: 1px solid $border-lighter;
}

.time-hour {
  color: $text-secondary;
  border-right: 1px solid $border-light;
  background: $bg-page;
}

.time-cell {
  border-right: 1px solid $border-light;
}

.time-task {
  border-radius: $radius-xs;
  background: $bg-page;
}

.time-task-title {
  color: $text-primary;
}

.month-cell {
  border-radius: $radius-sm;
  background: $bg-card;
  border: 1px solid $border-lighter;

  &.other-month {
    background: $bg-page;

    .month-cell-date {
      color: $text-placeholder;
    }
  }

  &.is-today {
    border-color: $primary;

    .month-cell-date {
      color: $primary;
    }
  }
}

.month-cell-date {
  color: $text-regular;
}

.month-task-more {
  color: $text-secondary;
}

.month-task-text {
  color: $text-regular;
}

.quadrant-box {
  border-radius: $radius-sm;
  border: 1px solid $border-lighter;
}

.quadrant-title {
  color: $text-primary;
}

.quadrant-count {
  color: $text-secondary;
}

.quadrant-item {
  border-radius: $radius-xs;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.quadrant-item-desc {
  color: $text-primary;
}

@media (max-width: 1400px) {
  .dashboard-split {
    grid-template-columns: 1fr;
    overflow-y: auto;
  }

  .dashboard-page {
    overflow: auto;
  }

  .left-panel,
  .right-panel {
    overflow: visible;
  }

  .left-panel__inner {
    grid-template-rows: auto auto auto;
  }

  .view-shell {
    min-height: 600px;
  }
}

@media (max-width: 1200px) {
  .panel-status-row.count-5,
  .panel-status-row.count-6,
  .panel-status-row.count-7 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .panel-middle-row {
    grid-template-columns: 1fr;
  }

  .panel-status-group {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-split {
    padding: 8px;
    gap: 10px;
  }

  .panel-status-row.count-5,
  .panel-status-row.count-6,
  .panel-status-row.count-7,
  .quadrant-grid,
  .time-grid-head,
  .time-row {
    grid-template-columns: 1fr;
  }

  .status-group__cards.count-6 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .time-head-time,
  .time-hour {
    display: none;
  }

  .pie-panel__body {
    grid-template-columns: 1fr;
  }

  .manager-chart-controls {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
