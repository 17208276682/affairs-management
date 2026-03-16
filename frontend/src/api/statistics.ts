// ============================================
// 统计 API
// ============================================
import { get } from './request'
import type { StatsOverview, DeptStats, LevelStats, PersonStats, TrendData } from '@/types'

/** 总览数据 */
export function getStatsOverviewApi() {
  return get<StatsOverview>('/stats/overview')
}

/** 部门维度统计 */
export function getStatsByDeptApi() {
  return get<DeptStats[]>('/stats/by-dept')
}

/** 级别维度统计 */
export function getStatsByLevelApi() {
  return get<LevelStats[]>('/stats/by-level')
}

/** 个人维度统计 */
export function getStatsByPersonApi() {
  return get<PersonStats[]>('/stats/by-person')
}

/** 趋势数据 */
export function getStatsTrendApi(range: 'week' | 'month' | 'quarter') {
  return get<TrendData>('/stats/trend', { range })
}

/** 最近动态（角色专属） */
export function getRecentActivitiesApi() {
  return get<{ id: string; type: string; content: string; time: string }[]>('/stats/recent-activities')
}
