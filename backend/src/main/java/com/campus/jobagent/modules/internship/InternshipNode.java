package com.campus.jobagent.modules.internship;

/**
 * 实习审批流程节点（流程模型）。
 *
 * <p>流转规则：
 * <pre>
 * 提交申请 -> MENTOR_REVIEW(企业导师) -> COUNSELOR_REVIEW(辅导员) -> COLLEGE_REVIEW(院系管理员) -> PASSED
 *                 ^-- 驳回时回退到上一节点（节点回退），首节点驳回则进入 REJECTED
 * </pre>
 */
public enum InternshipNode {

    MENTOR_REVIEW("企业导师审批"),

    COUNSELOR_REVIEW("辅导员审批"),

    COLLEGE_REVIEW("院系管理员审批"),

    PASSED("审批通过"),

    REJECTED("已驳回");

    private final String label;

    InternshipNode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 审批通过后的下一节点。 */
    public InternshipNode nextOnApprove() {
        return switch (this) {
            case MENTOR_REVIEW -> COUNSELOR_REVIEW;
            case COUNSELOR_REVIEW -> COLLEGE_REVIEW;
            case COLLEGE_REVIEW -> PASSED;
            default -> this;
        };
    }

    /** 审批驳回时的回退节点（支持节点回退）。 */
    public InternshipNode backOnReject() {
        return switch (this) {
            case MENTOR_REVIEW -> REJECTED;
            case COUNSELOR_REVIEW -> MENTOR_REVIEW;
            case COLLEGE_REVIEW -> COUNSELOR_REVIEW;
            default -> this;
        };
    }
}
