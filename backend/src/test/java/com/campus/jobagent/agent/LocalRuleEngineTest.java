package com.campus.jobagent.agent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本地规则引擎单元测试：保证离线降级时评分逻辑可用且稳定。
 */
class LocalRuleEngineTest {

    private final LocalRuleEngine engine = new LocalRuleEngine();

    @Test
    @DisplayName("结构化且内容充实的面试回答应得到更高逻辑分")
    void interviewScoreShouldRewardStructure() {
        String good = """
                首先，我会明确问题的目标与约束；其次，拆解为可执行的步骤并排定优先级；
                然后，我会在小范围内验证方案；最后，复盘结果并沉淀为团队规范。
                在实际项目中，我曾负责过类似任务，最终把交付周期缩短了 20%。
                """;
        String poor = "嗯，那个，就是大概可能吧。";

        Map<String, Object> goodResult = engine.generate(AgentScene.INTERVIEW_EVALUATION, good);
        Map<String, Object> poorResult = engine.generate(AgentScene.INTERVIEW_EVALUATION, poor);

        assertNotNull(goodResult.get("totalScore"));
        assertTrue((int) goodResult.get("logicScore") > (int) poorResult.get("logicScore"));
        assertTrue((int) goodResult.get("totalScore") > (int) poorResult.get("totalScore"));
    }

    @Test
    @DisplayName("简历诊断得分应落在 40-96 的合理区间")
    void resumeScoreShouldBeInRange() {
        Map<String, Object> result = engine.generate(AgentScene.RESUME_DIAGNOSIS,
                "教育背景：本科；实习经历：在某公司负责后端开发；技能：Java、Spring Boot；"
                        + "项目经历：主导开发就业服务系统，接口响应时间降低 40%，服务 200 名学生。");

        int score = (int) result.get("overallScore");
        assertTrue(score >= 40 && score <= 96, "得分应在合理区间，实际为 " + score);
        assertNotNull(result.get("strengths"));
    }

    @Test
    @DisplayName("本地规则引擎的面试三维分数都应处于 0-100 之间")
    void interviewDimensionsShouldBeWithinRange() {
        Map<String, Object> result = engine.generate(AgentScene.INTERVIEW_EVALUATION, "首先我要说明背景，其次给出方案。");
        for (String key : new String[]{"logicScore", "completenessScore", "expressionScore", "totalScore"}) {
            int value = (int) result.get(key);
            assertTrue(value >= 0 && value <= 100, key + " 越界：" + value);
        }
    }

    @Test
    @DisplayName("岗位匹配降级结果应包含匹配等级与建议")
    void jobMatchShouldContainAdvice() {
        Map<String, Object> result = engine.generate(AgentScene.JOB_MATCH, "{\"学生档案\":{},\"岗位信息\":{}}");
        assertNotNull(result.get("matchLevel"));
        assertNotNull(result.get("applicationAdvice"));
    }

    @Test
    @DisplayName("政策问答降级结果应返回检索原文")
    void policyAnswerShouldEchoContext() {
        Map<String, Object> result = engine.generate(AgentScene.POLICY_QA, "[1] 标题：求职创业补贴；内容：每人一次性1500元。");
        String answer = String.valueOf(result.get("answer"));
        assertTrue(answer.contains("1500"), "应回显检索到的政策原文");
    }

    @Test
    @DisplayName("内容更丰富的实习日志得分更高")
    void longerInternshipLogShouldScoreHigher() {
        int shortScore = (int) engine.generate(AgentScene.INTERNSHIP_LOG_REVIEW, "本周完成需求评审。").get("score");
        int longScore = (int) engine.generate(AgentScene.INTERNSHIP_LOG_REVIEW,
                "本周完成需求评审与接口设计，主导完成 3 个模块开发，联调通过后接口平均响应时间降低 35%，"
                        + "同时整理了团队接口规范文档，为后续开发节省沟通成本。").get("score");

        assertTrue(longScore > shortScore, "信息量大的日志应获得更高评价分");
    }
}
