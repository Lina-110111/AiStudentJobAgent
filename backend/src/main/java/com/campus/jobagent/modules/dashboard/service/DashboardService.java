package com.campus.jobagent.modules.dashboard.service;

import com.campus.jobagent.modules.dashboard.dto.DashboardVO;
import com.campus.jobagent.modules.dashboard.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 就业数据看板服务。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardVO overview() {
        Map<String, Object> counts = dashboardMapper.employmentCount();
        long total = toLong(counts == null ? null : counts.get("total"));
        long passed = toLong(counts == null ? null : counts.get("passed"));
        double rate = total == 0 ? 0D : Math.round(passed * 1000.0 / total) / 10.0;

        List<Map<String, Object>> industry = dashboardMapper.industryDistribution();
        List<Map<String, Object>> salary = dashboardMapper.salaryAnalysis();
        List<Map<String, Object>> trend = dashboardMapper.applicationTrend();
        List<Map<String, Object>> status = dashboardMapper.applicationStatusDistribution();
        Map<String, Object> statistics = dashboardMapper.platformStatistics();
        return new DashboardVO(rate, industry, salary, trend, status, statistics);
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
