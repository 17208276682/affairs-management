package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.response.*;
import com.affairs.management.mapper.TaskMapper;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final TaskMapper taskMapper;

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

    @GetMapping("/monthly-trend")
    public ApiResponse<TrendData> getMonthlyTrend() {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                statisticsService.getMonthlyTrend(user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }

    @GetMapping("/export")
    public void exportExcel(@RequestParam String period, HttpServletResponse response) throws Exception {
        // period: "2026-03" (月) 或 "2026" (年)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startDate;
        String endDate;
        String fileName;

        if (period.length() == 7) {
            YearMonth ym = YearMonth.parse(period);
            startDate = ym.atDay(1).atStartOfDay().format(dtf);
            endDate = ym.atEndOfMonth().atTime(23, 59, 59).format(dtf);
            fileName = "事务报表_" + period + ".xlsx";
        } else {
            int year = Integer.parseInt(period);
            startDate = LocalDate.of(year, 1, 1).atStartOfDay().format(dtf);
            endDate = LocalDate.of(year, 12, 31).atTime(23, 59, 59).format(dtf);
            fileName = "事务报表_" + year + "年.xlsx";
        }

        List<Map<String, Object>> data = taskMapper.selectExportData(startDate, endDate);

        String[] statusMap = {"pending", "待接收", "accepted", "已接收", "in_progress", "执行中",
                "submitted", "已提交", "approved", "已通过", "rejected", "不通过",
                "completed", "已完成", "overdue", "已逾期", "cancelled", "已作废"};
        Map<String, String> statusLabel = new java.util.LinkedHashMap<>();
        for (int i = 0; i < statusMap.length; i += 2) statusLabel.put(statusMap[i], statusMap[i + 1]);

        String[] urgencyLabel = {"", "不紧急", "紧急", "非常紧急"};
        String[] importanceLabel = {"", "一般", "重要", "非常重要"};

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("事务明细");

            // 标题行
            String[] headers = {"事务编号", "标题", "下达人", "下达部门", "执行人", "执行部门",
                    "重要性", "紧急度", "级别", "状态", "创建时间", "截止时间", "完成时间"};
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            for (int r = 0; r < data.size(); r++) {
                Map<String, Object> row = data.get(r);
                Row dataRow = sheet.createRow(r + 1);
                dataRow.createCell(0).setCellValue(str(row.get("taskId")));
                dataRow.createCell(1).setCellValue(str(row.get("title")));
                dataRow.createCell(2).setCellValue(str(row.get("assignerName")));
                dataRow.createCell(3).setCellValue(str(row.get("assignerDept")));
                dataRow.createCell(4).setCellValue(str(row.get("executorName")));
                dataRow.createCell(5).setCellValue(str(row.get("executorDept")));

                int imp = row.get("importance") != null ? ((Number) row.get("importance")).intValue() : 0;
                dataRow.createCell(6).setCellValue(imp > 0 && imp < importanceLabel.length ? importanceLabel[imp] : "");
                int urg = row.get("urgency") != null ? ((Number) row.get("urgency")).intValue() : 0;
                dataRow.createCell(7).setCellValue(urg > 0 && urg < urgencyLabel.length ? urgencyLabel[urg] : "");

                dataRow.createCell(8).setCellValue(str(row.get("level")));
                dataRow.createCell(9).setCellValue(statusLabel.getOrDefault(str(row.get("status")), str(row.get("status"))));
                dataRow.createCell(10).setCellValue(str(row.get("createdAt")));
                dataRow.createCell(11).setCellValue(str(row.get("completionDeadline")));
                dataRow.createCell(12).setCellValue(str(row.get("completionTime")));
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            try (OutputStream out = response.getOutputStream()) {
                wb.write(out);
            }
        }
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
