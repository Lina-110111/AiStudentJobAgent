package com.campus.jobagent.agent;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地规则引擎：在没有配置大模型（或大模型调用失败）时提供确定性的兜底结果。
 *
 * <p>它的意义有两个：
 * <ol>
 *   <li>答辩/演示环境可能没有外网，系统仍能完整跑通业务闭环；</li>
 *   <li>面试评分这类需要“可解释、可复现”的场景，规则打分比大模型更稳定，可作为基准分。</li>
 * </ol>
 */
@Component
public class LocalRuleEngine {

    private static final List<String> STRUCTURE_WORDS =
            List.of("首先", "其次", "然后", "最后", "一是", "二是", "第一", "第二", "综上", "总结");

    private static final List<String> FILLER_WORDS =
            List.of("嗯", "那个", "就是", "然后就是", "大概", "可能吧");

    private static final List<String> RESUME_SECTIONS =
            List.of("教育", "实习", "项目", "技能", "证书", "获奖", "校园经历", "自我评价");

    private static final List<String> QUANTIFY_MARKERS =
            List.of("%", "％", "万", "人", "次", "项", "个", "提升", "增长", "降低");

    public Map<String, Object> generate(AgentScene scene, String payload) {
        return switch (scene) {
            case RESUME_DIAGNOSIS -> resumeDiagnosis(payload);
            case JOB_MATCH -> jobMatch(payload);
            case INTERVIEW_EVALUATION -> interviewEvaluation(payload);
            case INTERNSHIP_LOG_REVIEW -> internshipLogReview(payload);
            case POLICY_QA -> policyExtractiveAnswer(payload);
        };
    }

    private Map<String, Object> resumeDiagnosis(String payload) {
        String text = safe(payload);
        int score = 55;
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();

        List<String> hitSections = new ArrayList<>();
        for (String section : RESUME_SECTIONS) {
            if (text.contains(section)) {
                hitSections.add(section);
            }
        }
        score += Math.min(20, hitSections.size() * 3);
        if (!hitSections.isEmpty()) {
            strengths.add("简历覆盖了 " + String.join("、", hitSections) + " 等模块，结构较为完整");
        } else {
            weaknesses.add("缺少教育背景、实习经历、项目经历等基本模块，招聘方难以快速定位信息");
        }

        long quantifyCount = QUANTIFY_MARKERS.stream().filter(text::contains).count();
        if (quantifyCount >= 2) {
            score += 12;
            strengths.add("经历描述中出现了量化表达，成果更可信");
        } else {
            weaknesses.add("实习/项目成果缺少量化数据（如“效率提升30%”“服务200名用户”）");
        }

        if (text.length() >= 400) {
            score += 8;
        } else {
            weaknesses.add("简历信息量偏少，建议补充 2-3 段与目标岗位相关的经历细节");
        }

        if (text.contains("负责") || text.contains("主导") || text.contains("独立")) {
            strengths.add("使用了主动性动词，能体现个人贡献");
        } else {
            weaknesses.add("多用“负责/主导/独立完成”等动词替代“参与”，突出个人贡献");
        }

        score = Math.max(40, Math.min(96, score));
        List<Map<String, String>> suggestions = new ArrayList<>();
        suggestions.add(Map.of("point", "成果量化", "advice", "在每段经历末尾补充 1 个可量化结果，例如“接口平均响应时间下降 40%”"));
        suggestions.add(Map.of("point", "岗位针对性", "advice", "把目标岗位 JD 中的关键词（如 Spring Boot、数据分析）自然嵌入技能与项目描述"));
        suggestions.add(Map.of("point", "一页原则", "advice", "校招简历控制在一页内，按“倒序时间 + 结果导向”排列"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overallScore", score);
        result.put("level", levelOf(score));
        result.put("strengths", strengths);
        result.put("weaknesses", weaknesses);
        result.put("suggestions", suggestions);
        result.put("missingKeywords", List.of("量化成果", "岗位关键词", "技术栈细节"));
        result.put("engine", "local-rule");
        return result;
    }

    private Map<String, Object> jobMatch(String payload) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matchLevel", "较为匹配");
        result.put("reasons", List.of("专业/学历满足岗位基本要求", "技能标签与岗位关键词存在交集"));
        result.put("gaps", List.of("项目经历与岗位业务场景的对应关系可以写得更明确"));
        result.put("applicationAdvice", List.of(
                "投递前按岗位 JD 调整简历中的技能关键词顺序，把最相关的放在最前",
                "准备 1 个能体现岗位核心能力的项目案例，按 STAR 结构复述"));
        result.put("engine", "local-rule");
        result.put("payloadDigest", safe(payload).length() > 200 ? safe(payload).substring(0, 200) : safe(payload));
        return result;
    }

    private Map<String, Object> interviewEvaluation(String payload) {
        String text = safe(payload);
        int structureHits = (int) STRUCTURE_WORDS.stream().filter(text::contains).count();
        int fillerHits = (int) FILLER_WORDS.stream().filter(text::contains).count();
        int length = text.length();

        int logic = clamp(60 + structureHits * 8 - fillerHits * 5);
        int completeness = clamp(length >= 300 ? 88 : length >= 150 ? 78 : length >= 80 ? 66 : 52);
        int expression = clamp(84 - fillerHits * 6 + (length > 120 ? 6 : 0));
        int total = Math.round((logic + completeness + expression) / 3.0f);

        List<String> highlights = new ArrayList<>();
        if (structureHits > 0) {
            highlights.add("回答使用了“首先/其次/最后”等结构化表达，逻辑层次清晰");
        }
        if (length >= 150) {
            highlights.add("回答有足够的信息量，能够展开说明细节");
        }
        if (highlights.isEmpty()) {
            highlights.add("态度积极，能够正面回应面试官提问");
        }

        List<Map<String, String>> improvements = new ArrayList<>();
        if (structureHits == 0) {
            improvements.add(Map.of("problem", "回答缺少结构", "betterAnswer", "按“结论先行 → 分点说明 → 举例收尾”组织语言，例如：我认为处理这类问题分三步，首先……"));
        }
        if (length < 150) {
            improvements.add(Map.of("problem", "内容偏短，缺少例证", "betterAnswer", "补充一个真实经历：在某次项目中我遇到……最终结果是……"));
        }
        if (fillerHits > 0) {
            improvements.add(Map.of("problem", "存在口头语", "betterAnswer", "用 2 秒停顿替代“嗯/那个”，让表达更自信"));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("logicScore", logic);
        result.put("completenessScore", completeness);
        result.put("expressionScore", expression);
        result.put("totalScore", total);
        result.put("comment", "本次回答总分 " + total + " 分，处于" + levelOf(total) + "水平。整体表达" +
                (structureHits > 0 ? "有条理" : "偏随意") + "，建议围绕结构与例证继续打磨。");
        result.put("highlights", highlights);
        result.put("improvements", improvements);
        result.put("engine", "local-rule");
        return result;
    }

    private Map<String, Object> internshipLogReview(String payload) {
        String text = safe(payload);
        // 评分梯度：基础分 + 信息量 + 结构完整性 + 量化结果
        int score = 58 + Math.min(20, text.length() / 8);
        int structureHits = 0;
        for (String marker : List.of("本周", "任务", "完成", "问题", "解决", "计划", "反思", "收获")) {
            if (text.contains(marker)) {
                structureHits++;
            }
        }
        score += Math.min(12, structureHits * 3);
        if (text.matches("(?s).*\\d+.*")) {
            score += 5;
        }
        score = clamp(score);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", score);
        result.put("comment", "日志记录了本周实习内容，结构" + (structureHits >= 3 ? "完整" : "较简单")
                + "，信息量" + (text.length() >= 120 ? "充足" : "偏少") + "，建议补充量化结果与个人反思。");
        result.put("suggestions", List.of(
                "补充本周任务的目标、过程、结果三段式描述",
                "记录遇到的问题与解决办法，便于辅导员给出针对性指导",
                "用数据说明产出，如“完成 12 个接口联调，缺陷率下降 30%”"));
        result.put("engine", "local-rule");
        return result;
    }

    private Map<String, Object> policyExtractiveAnswer(String payload) {
        String text = safe(payload);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", "【本地知识库检索结果】\n" + (text.length() > 800 ? text.substring(0, 800) + "……" : text));
        result.put("citations", List.of());
        result.put("followUpQuestions", List.of("需要我帮你整理办理材料清单吗？", "想了解办理地点和办公时间吗？"));
        result.put("engine", "local-rule");
        return result;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static String levelOf(int score) {
        if (score >= 90) {
            return "优秀";
        }
        if (score >= 80) {
            return "良好";
        }
        if (score >= 60) {
            return "及格";
        }
        return "待提升";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
