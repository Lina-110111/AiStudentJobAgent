package com.campus.jobagent.modules.interview.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.agent.AgentOrchestrator;
import com.campus.jobagent.agent.AgentScene;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.util.JsonUtils;
import com.campus.jobagent.modules.interview.InterviewQuestionBank;
import com.campus.jobagent.modules.interview.dto.AnswerRequest;
import com.campus.jobagent.modules.interview.dto.StartInterviewRequest;
import com.campus.jobagent.modules.interview.entity.InterviewSession;
import com.campus.jobagent.modules.interview.mapper.InterviewSessionMapper;
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
 * AI 面试训练服务。
 *
 * <p>训练闭环：选择场景出题 -> 逐题作答并由智能体按“逻辑性/完整性/表达力”评分
 * -> 生成训练报告 -> 历史记录对比。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService extends ServiceImpl<InterviewSessionMapper, InterviewSession> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final InterviewQuestionBank questionBank;
    private final AgentOrchestrator agentOrchestrator;

    /** 开始一次训练：生成题目。 */
    @Transactional(rollbackFor = Exception.class)
    public InterviewSession start(Long studentId, StartInterviewRequest request) {
        String category = request.category() == null ? "综合素质" : request.category();
        String difficulty = request.difficulty() == null ? "NORMAL" : request.difficulty().toUpperCase();
        List<String> questions = questionBank.questions(category, difficulty);

        InterviewSession session = new InterviewSession();
        session.setStudentId(studentId);
        session.setCategory(category);
        session.setDifficulty(difficulty);
        session.setQuestionsJson(JsonUtils.toJson(questions));
        session.setAnswersJson("[]");
        session.setStatus("RUNNING");
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setDeleted(0);
        save(session);
        return session;
    }

    /** 提交一道题的回答并立即得到评估。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> answer(Long sessionId, Long studentId, AnswerRequest request) {
        InterviewSession session = getOwned(sessionId, studentId);
        if (!"RUNNING".equals(session.getStatus())) {
            throw BizException.of(ResultCode.BUSINESS_ERROR, "该次训练已结束");
        }
        List<String> questions = readList(session.getQuestionsJson());
        if (request.questionIndex() < 0 || request.questionIndex() >= questions.size()) {
            throw BizException.of(ResultCode.PARAM_INVALID, "题目序号超出范围");
        }
        String question = questions.get(request.questionIndex());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("面试场景", session.getCategory());
        payload.put("面试题目", question);
        payload.put("学生回答", request.answer());
        AgentOrchestrator.AgentResult result =
                agentOrchestrator.run(AgentScene.INTERVIEW_EVALUATION, JsonUtils.toJson(payload));

        List<Map<String, Object>> answers = readAnswers(session.getAnswersJson());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("questionIndex", request.questionIndex());
        item.put("question", question);
        item.put("answer", request.answer());
        item.put("evaluation", result.data());
        item.put("answerLength", request.answer().length());
        answers.removeIf(exist -> request.questionIndex().equals(exist.get("questionIndex")));
        answers.add(item);
        answers.sort((a, b) -> Integer.compare(
                ((Number) a.get("questionIndex")).intValue(),
                ((Number) b.get("questionIndex")).intValue()));
        session.setAnswersJson(JsonUtils.toJson(answers));
        session.setUpdateTime(LocalDateTime.now());
        updateById(session);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", session.getId());
        response.put("questionIndex", request.questionIndex());
        response.put("aiGenerated", result.aiGenerated());
        response.put("engine", result.engine());
        response.put("evaluation", result.data());
        return response;
    }

    /** 结束训练并生成报告。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> finish(Long sessionId, Long studentId) {
        InterviewSession session = getOwned(sessionId, studentId);
        List<String> questions = readList(session.getQuestionsJson());
        List<Map<String, Object>> answers = readAnswers(session.getAnswersJson());
        if (answers.isEmpty()) {
            throw BizException.of(ResultCode.BUSINESS_ERROR, "还没有作答记录，无法生成报告");
        }

        double logic = 0;
        double completeness = 0;
        double expression = 0;
        for (Map<String, Object> answer : answers) {
            @SuppressWarnings("unchecked")
            Map<String, Object> evaluation = (Map<String, Object>) answer.get("evaluation");
            logic += number(evaluation.get("logicScore"));
            completeness += number(evaluation.get("completenessScore"));
            expression += number(evaluation.get("expressionScore"));
        }
        int size = answers.size();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("sessionId", session.getId());
        report.put("category", session.getCategory());
        report.put("questionCount", questions.size());
        report.put("answeredCount", size);
        report.put("dimensions", Map.of(
                "逻辑性", Math.round(logic / size),
                "完整性", Math.round(completeness / size),
                "表达力", Math.round(expression / size)));
        int total = (int) Math.round((logic + completeness + expression) / (3.0 * size));
        report.put("totalScore", total);
        report.put("level", total >= 85 ? "优秀" : total >= 70 ? "良好" : total >= 60 ? "及格" : "待提升");
        report.put("suggestions", List.of(
                "使用“结论先行 + 分点论述 + 实例支撑”的结构回答行为类问题",
                "技术类问题补充排查思路与取舍理由，体现工程思维",
                "控制语速与口头语，可在训练回放中重点复盘表达力维度"));
        report.put("answers", answers);

        session.setReportJson(JsonUtils.toJson(report));
        session.setTotalScore(total);
        session.setStatus("FINISHED");
        session.setUpdateTime(LocalDateTime.now());
        updateById(session);
        return report;
    }

    /** 历史训练记录。 */
    public Page<InterviewSession> history(Long studentId, long pageNum, long pageSize) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<InterviewSession>lambdaQuery()
                .eq(InterviewSession::getStudentId, studentId)
                .orderByDesc(InterviewSession::getId));
    }

    public InterviewSession getOwned(Long sessionId, Long studentId) {
        InterviewSession session = getById(sessionId);
        if (session == null) {
            throw BizException.of(ResultCode.INTERVIEW_NOT_FOUND);
        }
        if (!session.getStudentId().equals(studentId)) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return session;
    }

    private List<String> readList(String json) {
        try {
            return MAPPER.readValue(json == null ? "[]" : json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> readAnswers(String json) {
        try {
            return MAPPER.readValue(json == null ? "[]" : json,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private double number(Object value) {
        return value instanceof Number n ? n.doubleValue() : 0D;
    }
}
