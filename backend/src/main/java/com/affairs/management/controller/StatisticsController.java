package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.response.*;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public ApiResponse<StatsOverview> getOverview() {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                statisticsService.getOverview(user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }

    @GetMapping("/by-dept")
    public ApiResponse<List<DeptStats>> getStatsByDept() {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                statisticsService.getStatsByDept(user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }

    @GetMapping("/by-level")
    public ApiResponse<List<LevelStats>> getStatsByLevel() {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                statisticsService.getStatsByLevel(user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }

    @GetMapping("/by-person")
    public ApiResponse<List<PersonStats>> getStatsByPerson() {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                statisticsService.getStatsByPerson(user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }

    @GetMapping("/trend")
    public ApiResponse<TrendData> getTrend(@RequestParam(defaultValue = "month") String range) {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                statisticsService.getTrend(range, user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }

    @GetMapping("/recent-activities")
    public ApiResponse<List<ActivityVO>> getRecentActivities() {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                statisticsService.getRecentActivities(user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }
}
