package com.campus.jobagent.modules.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 就业数据看板查询。
 *
 * <p>看板的三个核心图表（就业率 / 行业分布 / 薪资分析）直接由 SQL 聚合得到，
 * 每次请求实时计算，保证“数据实时刷新”的要求。
 */
@Mapper
public interface DashboardMapper {

    /** 就业率：实习/就业流程已通过的申请占比。 */
    @Select("""
            SELECT
              COUNT(*) AS total,
              SUM(CASE WHEN status = 'PASSED' THEN 1 ELSE 0 END) AS passed
            FROM internship_apply
            WHERE deleted = 0
            """)
    Map<String, Object> employmentCount();

    /** 行业分布：按岗位类别统计在招岗位数。 */
    @Select("""
            SELECT job_category AS name, COUNT(*) AS value
            FROM job_post
            WHERE deleted = 0 AND status = 1
            GROUP BY job_category
            ORDER BY value DESC
            """)
    List<Map<String, Object>> industryDistribution();

    /** 薪资分析：按岗位类别统计平均月薪。 */
    @Select("""
            SELECT job_category AS name,
                   ROUND(AVG((salary_min + salary_max) / 2)) AS avgSalary,
                   COUNT(*) AS jobCount
            FROM job_post
            WHERE deleted = 0 AND salary_min IS NOT NULL AND salary_max IS NOT NULL
            GROUP BY job_category
            ORDER BY avgSalary DESC
            """)
    List<Map<String, Object>> salaryAnalysis();

    /** 近 7 天投递趋势。 */
    @Select("""
            SELECT DATE_FORMAT(apply_time, '%m-%d') AS day, COUNT(*) AS value
            FROM job_application
            WHERE deleted = 0 AND apply_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
            GROUP BY day
            ORDER BY day
            """)
    List<Map<String, Object>> applicationTrend();

    /** 投递状态分布。 */
    @Select("""
            SELECT status AS name, COUNT(*) AS value
            FROM job_application
            WHERE deleted = 0
            GROUP BY status
            """)
    List<Map<String, Object>> applicationStatusDistribution();

    /** 平台总量指标。 */
    @Select("""
            SELECT
              (SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND role_code = 'STUDENT') AS studentCount,
              (SELECT COUNT(*) FROM job_post WHERE deleted = 0 AND status = 1) AS jobCount,
              (SELECT COUNT(*) FROM job_application WHERE deleted = 0) AS applicationCount,
              (SELECT COUNT(*) FROM interview_session WHERE deleted = 0) AS interviewCount,
              (SELECT COUNT(*) FROM policy_item WHERE deleted = 0 AND status = 1) AS policyCount
            """)
    Map<String, Object> platformStatistics();
}
