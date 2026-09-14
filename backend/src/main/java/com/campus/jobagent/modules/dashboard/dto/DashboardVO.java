package com.campus.jobagent.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 就业数据看板数据。
 */
@Schema(description = "就业数据看板")
public record DashboardVO(
        @Schema(description = "就业率（%）")
        double employmentRate,

        @Schema(description = "行业分布（饼图）")
        List<Map<String, Object>> industryDistribution,

        @Schema(description = "薪资分析（柱状图）")
        List<Map<String, Object>> salaryAnalysis,

        @Schema(description = "近 7 天投递趋势（折线图）")
        List<Map<String, Object>> applicationTrend,

        @Schema(description = "投递状态分布")
        List<Map<String, Object>> applicationStatus,

        @Schema(description = "平台总量指标")
        Map<String, Object> statistics
) {
}
