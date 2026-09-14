package com.campus.jobagent.modules.interview;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 面试题库：按场景类别内置题目，后续可由管理员在后台维护（写入数据库）。
 */
@Component
public class InterviewQuestionBank {

    private static final Map<String, List<String>> BANK = Map.of(
            "TECH", List.of(
                    "请用 2 分钟介绍你做过的最有挑战的一个技术项目，你在其中的核心贡献是什么？",
                    "谈谈你对常用后端框架（如 Spring Boot）核心原理的理解。",
                    "如果线上接口响应突然变慢，你会如何定位问题？请说明排查顺序。",
                    "介绍一次你解决过的复杂 Bug，问题是如何暴露、分析和修复的？",
                    "你如何保证自己交付的代码质量？请结合具体实践说明。"),
            "MANAGEMENT", List.of(
                    "请描述一次你协调多方完成任务的经历，你是如何推动进度的？",
                    "如果团队成员长期拖延影响交付，你会怎么处理？",
                    "如何在一个学期内组织一次校级活动？请说明目标拆解与风险控制。",
                    "当你与上级意见不一致时，你会如何沟通？",
                    "请举例说明你如何激励团队中状态低落的成员。"),
            "COMPREHENSIVE", List.of(
                    "请做一个 1 分钟的自我介绍。",
                    "你最大的优点和缺点分别是什么？请举例说明。",
                    "为什么选择我们公司/这个岗位？你对未来三年的规划是什么？",
                    "谈谈你遇到过的最大挫折，以及你从中学到了什么。",
                    "你如何看待加班与工作生活平衡？")
    );

    /** 取题；类别不存在时回退到综合素质题库。 */
    public List<String> questions(String category, String difficulty) {
        List<String> questions = BANK.getOrDefault(normalize(category), BANK.get("COMPREHENSIVE"));
        int limit = "HARD".equalsIgnoreCase(difficulty) ? questions.size() : Math.min(3, questions.size());
        return questions.subList(0, limit);
    }

    public boolean supports(String category) {
        return BANK.containsKey(normalize(category));
    }

    private String normalize(String category) {
        if (category == null) {
            return "COMPREHENSIVE";
        }
        return switch (category.trim()) {
            case "技术岗", "TECH" -> "TECH";
            case "管理岗", "MANAGEMENT" -> "MANAGEMENT";
            case "综合素质", "COMPREHENSIVE" -> "COMPREHENSIVE";
            default -> "COMPREHENSIVE";
        };
    }
}
