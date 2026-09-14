package com.campus.jobagent.modules.internship.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.agent.AgentOrchestrator;
import com.campus.jobagent.agent.AgentScene;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.util.JsonUtils;
import com.campus.jobagent.modules.internship.InternshipNode;
import com.campus.jobagent.modules.internship.dto.ApproveRequest;
import com.campus.jobagent.modules.internship.dto.InternshipApplyRequest;
import com.campus.jobagent.modules.internship.dto.InternshipLogRequest;
import com.campus.jobagent.modules.internship.dto.LogReviewRequest;
import com.campus.jobagent.modules.internship.dto.ScoreRequest;
import com.campus.jobagent.modules.internship.entity.InternshipApply;
import com.campus.jobagent.modules.internship.entity.InternshipLog;
import com.campus.jobagent.modules.internship.mapper.InternshipApplyMapper;
import com.campus.jobagent.modules.internship.mapper.InternshipLogMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 实习管理服务：申请、多级审批（含节点回退）、日志、三方评分。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternshipService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final InternshipApplyMapper applyMapper;
    private final InternshipLogMapper logMapper;
    private final AgentOrchestrator agentOrchestrator;

    /** 提交实习申请，进入流程首节点。 */
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply apply(Long studentId, InternshipApplyRequest request) {
        InternshipApply apply = new InternshipApply();
        apply.setStudentId(studentId);
        apply.setCompanyName(request.companyName());
        apply.setPosition(request.position());
        apply.setMentorId(request.mentorId());
        apply.setTeacherId(request.teacherId());
        apply.setStatus("RUNNING");
        apply.setCurrentNode(InternshipNode.MENTOR_REVIEW.name());
        apply.setStartDate(request.startDate());
        apply.setEndDate(request.endDate());
        apply.setRemark(request.remark());
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        apply.setDeleted(0);
        apply.setFlowTraceJson(JsonUtils.toJson(List.of(trace(studentId, "提交申请",
                InternshipNode.MENTOR_REVIEW.name(), "学生提交实习申请"))));
        applyMapper.insert(apply);
        return apply;
    }

    public Page<InternshipApply> pageMy(Long studentId, long pageNum, long pageSize) {
        return applyMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<InternshipApply>lambdaQuery()
                        .eq(InternshipApply::getStudentId, studentId)
                        .orderByDesc(InternshipApply::getId));
    }

    public Page<InternshipApply> pagePending(long pageNum, long pageSize) {
        return applyMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<InternshipApply>lambdaQuery()
                        .eq(InternshipApply::getStatus, "RUNNING")
                        .orderByAsc(InternshipApply::getId));
    }

    /**
     * 流程审批：通过则前进一个节点，驳回则回退到上一节点（首节点驳回即终态）。
     */
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply approve(Long applyId, Long operatorId, ApproveRequest request) {
        InternshipApply apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "实习申请不存在");
        }
        if (!"RUNNING".equals(apply.getStatus())) {
            throw BizException.of(ResultCode.FLOW_NODE_ERROR, "该申请已结束，无法继续审批");
        }
        InternshipNode current = InternshipNode.valueOf(apply.getCurrentNode());
        InternshipNode target = Boolean.TRUE.equals(request.approve())
                ? current.nextOnApprove()
                : current.backOnReject();

        apply.setCurrentNode(target.name());
        if (target == InternshipNode.PASSED) {
            apply.setStatus("PASSED");
        } else if (target == InternshipNode.REJECTED) {
            apply.setStatus("REJECTED");
        }
        apply.setFlowTraceJson(appendTrace(apply.getFlowTraceJson(),
                trace(operatorId, Boolean.TRUE.equals(request.approve()) ? "审批通过" : "驳回",
                        target.name(), request.comment())));
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(apply);
        log.info("实习流程流转：applyId={}, {} -> {}", applyId, current.name(), target.name());
        return apply;
    }

    /** 学生提交日志。 */
    @Transactional(rollbackFor = Exception.class)
    public InternshipLog submitLog(Long applyId, Long studentId, InternshipLogRequest request) {
        InternshipApply apply = applyMapper.selectById(applyId);
        if (apply == null || !apply.getStudentId().equals(studentId)) {
            throw BizException.of(ResultCode.NOT_FOUND, "实习申请不存在");
        }
        if ("REJECTED".equals(apply.getStatus())) {
            throw BizException.of(ResultCode.FLOW_NODE_ERROR, "申请已被驳回，无法提交日志");
        }
        InternshipLog internshipLog = new InternshipLog();
        internshipLog.setApplyId(applyId);
        internshipLog.setStudentId(studentId);
        internshipLog.setWeekNo(request.weekNo());
        internshipLog.setLogDate(request.logDate());
        internshipLog.setContent(request.content());
        internshipLog.setCreateTime(LocalDateTime.now());
        internshipLog.setUpdateTime(LocalDateTime.now());
        internshipLog.setDeleted(0);
        logMapper.insert(internshipLog);
        return internshipLog;
    }

    public List<InternshipLog> listLogs(Long applyId) {
        return logMapper.selectList(Wrappers.<InternshipLog>lambdaQuery()
                .eq(InternshipLog::getApplyId, applyId)
                .orderByAsc(InternshipLog::getWeekNo));
    }

    /** 辅导员批阅日志：保存人工评语 + 智能体辅助点评。 */
    @Transactional(rollbackFor = Exception.class)
    public InternshipLog reviewLog(Long logId, LogReviewRequest request) {
        InternshipLog internshipLog = logMapper.selectById(logId);
        if (internshipLog == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "实习日志不存在");
        }
        internshipLog.setTeacherComment(request.comment());
        internshipLog.setScore(request.score());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("日志内容", internshipLog.getContent());
        payload.put("第几周", internshipLog.getWeekNo());
        AgentOrchestrator.AgentResult result =
                agentOrchestrator.run(AgentScene.INTERNSHIP_LOG_REVIEW, JsonUtils.toJson(payload));
        internshipLog.setAiReviewJson(JsonUtils.toJson(result.data()));
        internshipLog.setUpdateTime(LocalDateTime.now());
        logMapper.updateById(internshipLog);
        return internshipLog;
    }

    /**
     * 三方评分汇总：学生自评 20% + 企业导师 40% + 校方 40%。
     */
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply submitScore(Long applyId, ScoreRequest request) {
        InternshipApply apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "实习申请不存在");
        }
        switch (request.role()) {
            case "SELF" -> apply.setSelfScore(request.score());
            case "MENTOR" -> apply.setMentorScore(request.score());
            case "TEACHER" -> apply.setTeacherScore(request.score());
            default -> throw BizException.of(ResultCode.PARAM_INVALID, "评分方取值不合法");
        }
        apply.setFinalScore(weightedScore(apply));
        apply.setUpdateTime(LocalDateTime.now());
        applyMapper.updateById(apply);
        return apply;
    }

    private Integer weightedScore(InternshipApply apply) {
        int self = apply.getSelfScore() == null ? 0 : apply.getSelfScore();
        int mentor = apply.getMentorScore() == null ? 0 : apply.getMentorScore();
        int teacher = apply.getTeacherScore() == null ? 0 : apply.getTeacherScore();
        return (int) Math.round(self * 0.2 + mentor * 0.4 + teacher * 0.4);
    }

    private Map<String, Object> trace(Long operatorId, String action, String node, String comment) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("operatorId", operatorId);
        item.put("action", action);
        item.put("node", node);
        item.put("comment", comment);
        item.put("time", LocalDateTime.now().toString());
        return item;
    }

    private String appendTrace(String traceJson, Map<String, Object> item) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (traceJson != null && !traceJson.isBlank()) {
            try {
                list = MAPPER.readValue(traceJson, new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (Exception e) {
                log.warn("流程轨迹解析失败，将重新初始化：{}", e.getMessage());
            }
        }
        list.add(item);
        return JsonUtils.toJson(list);
    }
}
