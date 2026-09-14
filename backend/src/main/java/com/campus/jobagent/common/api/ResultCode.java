package com.campus.jobagent.common.api;

/**
 * 统一业务状态码。
 *
 * <p>编码规范：2xxxx 成功；4xxxx 客户端错误；5xxxx 服务端错误；6xxxx 业务特定错误。
 */
public enum ResultCode {

    SUCCESS(20000, "操作成功"),

    PARAM_INVALID(40000, "请求参数不合法"),
    UNAUTHORIZED(40100, "登录状态已失效，请重新登录"),
    FORBIDDEN(40300, "当前角色无权访问该资源"),
    NOT_FOUND(40400, "请求的资源不存在"),

    BUSINESS_ERROR(60000, "业务处理失败"),
    USERNAME_EXISTS(60001, "用户名已被占用"),
    USER_NOT_FOUND(60002, "用户不存在"),
    PASSWORD_ERROR(60003, "用户名或密码错误"),
    USER_DISABLED(60004, "账号已被停用"),
    RESUME_NOT_FOUND(60010, "简历不存在"),
    JOB_NOT_FOUND(60020, "岗位不存在"),
    JOB_OFFLINE(60021, "岗位已停止招聘"),
    ALREADY_APPLIED(60022, "你已申请过该岗位"),
    FLOW_NODE_ERROR(60030, "当前流程节点不允许该操作"),
    INTERVIEW_NOT_FOUND(60040, "面试训练记录不存在"),
    AI_SERVICE_ERROR(60050, "AI 服务调用失败，请稍后重试"),

    SYSTEM_ERROR(50000, "系统开小差了，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
