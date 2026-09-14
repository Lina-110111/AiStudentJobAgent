package com.campus.jobagent.agent;

/**
 * 智能体应用场景，每个场景对应一套提示词与输出结构。
 */
public enum AgentScene {

    /** 简历诊断 */
    RESUME_DIAGNOSIS("简历诊断"),

    /** 岗位匹配说明 */
    JOB_MATCH("岗位匹配"),

    /** 面试回答评估 */
    INTERVIEW_EVALUATION("面试评估"),

    /** 政策多轮问答 */
    POLICY_QA("政策问答"),

    /** 实习日志点评 */
    INTERNSHIP_LOG_REVIEW("实习日志点评");

    private final String label;

    AgentScene(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
