package com.campus.jobagent.common.constant;

/**
 * 系统四类角色（对应题目“用户与权限管理模块”要求）。
 */
public enum RoleCode {

    /** 学生 */
    STUDENT("STUDENT", "学生"),

    /** 辅导员 */
    COUNSELOR("COUNSELOR", "辅导员"),

    /** 企业 HR */
    HR("HR", "企业HR"),

    /** 院系管理员 */
    COLLEGE_ADMIN("COLLEGE_ADMIN", "院系管理员");

    private final String code;
    private final String label;

    RoleCode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String code) {
        for (RoleCode role : values()) {
            if (role.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
