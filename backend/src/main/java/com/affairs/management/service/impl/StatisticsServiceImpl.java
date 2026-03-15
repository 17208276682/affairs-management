package com.affairs.management.service.impl;

import com.affairs.management.dto.response.*;
import com.affairs.management.entity.TaskProcessRecord;
import com.affairs.management.entity.User;
import com.affairs.management.mapper.DeptMapper;
import com.affairs.management.mapper.TaskMapper;
import com.affairs.management.mapper.TaskProcessRecordMapper;
import com.affairs.management.mapper.UserMapper;
import com.affairs.management.mapper.UserManagedDeptMapper;
import com.affairs.management.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TaskMapper taskMapper;
    private final TaskProcessRecordMapper recordMapper;
    private final UserMapper userMapper;
    private final UserManagedDeptMapper userManagedDeptMapper;
    private final DeptMapper deptMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public StatsOverview getOverview(String userId, String role, List<String> managedDeptIds) {
        List<String> scopeUserIds = getScopeUserIds(userId, role, managedDeptIds);

        Map<String, Object> raw = taskMapper.selectStatsOverview(scopeUserIds);
        StatsOverview overview = new StatsOverview();
        overview.setTotalTasks(toInt(raw.get("totalTasks")));
        overview.setPendingTasks(toInt(raw.get("pendingTasks")));
        overview.setInProgressTasks(toInt(raw.get("inProgressTasks")));
        overview.setCompletedTasks(toInt(raw.get("completedTasks")));
        overview.setOverdueTasks(toInt(raw.get("overdueTasks")));

        int total = overview.getTotalTasks();
        if (total > 0) {
            overview.setOverdueRate(Math.round(overview.getOverdueTasks() * 10000.0 / total) / 100.0);
            overview.setCompletionRate(Math.round(overview.getCompletedTasks() * 10000.0 / total) / 100.0);
        }

        Object avgRsp = raw.get("avgResponseHours");
        overview.setAvgResponseHours(avgRsp != null ? Math.round(((Number) avgRsp).doubleValue() * 100) / 100.0 : 0);
        Object avgCmp = raw.get("avgCompletionHours");
        overview.setAvgCompletionHours(avgCmp != null ? Math.round(((Number) avgCmp).doubleValue() * 100) / 100.0 : 0);

        return overview;
    }

    @Override
    public List<DeptStats> getStatsByDept(String userId, String role, List<String> managedDeptIds) {
        List<String> scopeUserIds = getScopeUserIds(userId, role, managedDeptIds);
        return taskMapper.selectStatsByDept(scopeUserIds);
    }

    @Override
    public List<LevelStats> getStatsByLevel(String userId, String role, List<String> managedDeptIds) {
        List<String> scopeUserIds = getScopeUserIds(userId, role, managedDeptIds);
        return taskMapper.selectStatsByLevel(scopeUserIds);
    }

    @Override
    public List<PersonStats> getStatsByPerson(String userId, String role, List<String> managedDeptIds) {
        List<String> scopeUserIds = getScopeUserIds(userId, role, managedDeptIds);
        List<PersonStats> stats = taskMapper.selectStatsByPerson(scopeUserIds);
        // 计算完成率
        for (PersonStats ps : stats) {
            if (ps.getTotalTasks() > 0) {
                ps.setCompletionRate(Math.round(ps.getCompletedTasks() * 10000.0 / ps.getTotalTasks()) / 100.0);
            }
        }
        return stats;
    }

    @Override
    public TrendData getTrend(String range, String userId, String role, List<String> managedDeptIds) {
        List<String> scopeUserIds = getScopeUserIds(userId, role, managedDeptIds);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        switch (range) {
            case "week" -> startDate = endDate.minusWeeks(1);
            case "quarter" -> startDate = endDate.minusMonths(3);
            default -> startDate = endDate.minusMonths(1);
        }

        List<Map<String, Object>> rawData = taskMapper.selectTrend(
                startDate.atStartOfDay().format(DTF),
                endDate.atTime(23, 59, 59).format(DTF),
                scopeUserIds);

        // 生成完整日期序列
        Map<String, int[]> dateMap = new LinkedHashMap<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dateMap.put(d.format(DATE_FMT), new int[]{0, 0});
        }

        for (Map<String, Object> row : rawData) {
            String date = row.get("date").toString();
            if (date.length() > 10) date = date.substring(0, 10);
            int[] values = dateMap.get(date);
            if (values != null) {
                values[0] = toInt(row.get("created"));
                values[1] = toInt(row.get("completed"));
            }
        }

        TrendData trend = new TrendData();
        trend.setDates(new ArrayList<>(dateMap.keySet()));
        trend.setCreated(dateMap.values().stream().map(v -> v[0]).collect(Collectors.toList()));
        trend.setCompleted(dateMap.values().stream().map(v -> v[1]).collect(Collectors.toList()));
        return trend;
    }

    @Override
    public List<ActivityVO> getRecentActivities(String userId, String role, List<String> managedDeptIds) {
        List<String> scopeUserIds = getScopeUserIds(userId, role, managedDeptIds);
        List<TaskProcessRecord> records = recordMapper.selectRecent(20, scopeUserIds);

        return records.stream().map(r -> {
            ActivityVO vo = new ActivityVO();
            vo.setId(r.getId());
            vo.setType(r.getAction());
            vo.setContent(r.getContent());
            if (r.getCreatedAt() != null) {
                vo.setTime(r.getCreatedAt().format(DTF));
            }

            // 拼接操作人信息
            User operator = userMapper.selectById(r.getOperatorId());
            if (operator != null) {
                vo.setContent(operator.getName() + " " + r.getContent());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据角色获取统计数据范围的用户ID列表
     */
    private List<String> getScopeUserIds(String userId, String role, List<String> managedDeptIds) {
        if ("director".equals(role)) {
            // 总经办：全局范围，不传 userIds 则不做过滤
            return null;
        } else if ("manager".equals(role)) {
            // 部门经理：管辖部门内的用户
            if (managedDeptIds != null && !managedDeptIds.isEmpty()) {
                List<User> users = userMapper.selectByDeptIds(managedDeptIds);
                return users.stream().map(User::getId).collect(Collectors.toList());
            }
            return Collections.singletonList(userId);
        } else {
            // 普通员工：仅个人
            return Collections.singletonList(userId);
        }
    }

    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }
}
