package com.campus.jobagent.modules.dashboard.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.dashboard.dto.DashboardVO;
import com.campus.jobagent.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 就业数据看板接口。
 */
@Tag(name = "就业数据看板", description = "就业率、行业分布、薪资分析三大核心图表")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "看板总览（实时统计）")
    @GetMapping("/overview")
    public Result<DashboardVO> overview() {
        return Result.ok(dashboardService.overview());
    }
}
