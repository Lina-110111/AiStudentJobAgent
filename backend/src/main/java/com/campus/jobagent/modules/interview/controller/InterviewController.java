package com.campus.jobagent.modules.interview.controller;

import com.campus.jobagent.common.api.PageResult;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.util.SecurityUtils;
import com.campus.jobagent.modules.interview.dto.AnswerRequest;
import com.campus.jobagent.modules.interview.dto.StartInterviewRequest;
import com.campus.jobagent.modules.interview.entity.InterviewSession;
import com.campus.jobagent.modules.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 面试训练接口。
 */
@Tag(name = "AI 面试训练", description = "多场景出题、实时评分、训练报告")
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(summary = "开始一次面试训练")
    @PostMapping("/start")
    public Result<InterviewSession> start(@RequestBody StartInterviewRequest request) {
        return Result.ok("训练已开始", interviewService.start(SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "提交作答并获取评分")
    @PostMapping("/{id}/answer")
    public Result<Map<String, Object>> answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest request) {
        return Result.ok(interviewService.answer(id, SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "结束训练并生成报告")
    @PostMapping("/{id}/finish")
    public Result<Map<String, Object>> finish(@PathVariable Long id) {
        return Result.ok("报告已生成", interviewService.finish(id, SecurityUtils.getUserId()));
    }

    @Operation(summary = "历史训练记录")
    @GetMapping("/history")
    public Result<PageResult<InterviewSession>> history(@RequestParam(defaultValue = "1") long pageNum,
                                                        @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(PageResult.of(interviewService.history(SecurityUtils.getUserId(), pageNum, pageSize)));
    }

    @Operation(summary = "训练详情")
    @GetMapping("/{id}")
    public Result<InterviewSession> detail(@PathVariable Long id) {
        return Result.ok(interviewService.getOwned(id, SecurityUtils.getUserId()));
    }
}
