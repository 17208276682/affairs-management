package com.affairs.management.service;

import com.affairs.management.dto.response.*;

import java.util.List;

public interface StatisticsService {
    StatsOverview getOverview(String userId, String role, List<String> managedDeptIds);
    List<DeptStats> getStatsByDept(String userId, String role, List<String> managedDeptIds);
    List<LevelStats> getStatsByLevel(String userId, String role, List<String> managedDeptIds);
    List<PersonStats> getStatsByPerson(String userId, String role, List<String> managedDeptIds);
    TrendData getTrend(String range, String userId, String role, List<String> managedDeptIds);
    TrendData getMonthlyTrend(String userId, String role, List<String> managedDeptIds);
    List<ActivityVO> getRecentActivities(String userId, String role, List<String> managedDeptIds);
}
