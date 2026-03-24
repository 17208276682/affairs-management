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

/** 月度趋势（管理员统计页） */
export function getMonthlyTrendApi() {
  return get<TrendData>('/stats/monthly-trend')
}

/** 导出事务报表 Excel */
export function exportStatsExcel(period: string) {
  const token = localStorage.getItem('tm_token') || ''
  return fetch(`/api/stats/export?period=${encodeURIComponent(period)}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
    .then(res => res.blob())
    .then(blob => {
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `事务报表_${period}.xlsx`
      a.click()
      URL.revokeObjectURL(url)
    })
}
