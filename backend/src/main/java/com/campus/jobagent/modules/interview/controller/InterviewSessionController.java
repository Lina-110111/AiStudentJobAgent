package com.campus.jobagent.modules.interview.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.interview.entity.InterviewSession;
import com.campus.jobagent.modules.interview.service.InterviewSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 面试训练接口（基础 CRUD 骨架；开始训练、提交作答接口由模块负责人补充）。
 */
@Tag(name = "面试训练", description = "AI 面试模拟训练")
@RestController
@RequestMapping("/api/interview-sessions")
@RequiredArgsConstructor
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    @Operation(summary = "面试训练分页列表")
    @GetMapping
    public Result<Page<InterviewSession>> page(@RequestParam(defaultValue = "1") long pageNum,
                                               @RequestParam(defaultValue = "10") long pageSize,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String difficulty) {
        return Result.ok(interviewSessionService.pageSessions(pageNum, pageSize, keyword, difficulty));
    }

    @Operation(summary = "面试训练详情")
    @GetMapping("/{id}")
    public Result<InterviewSession> detail(@PathVariable Long id) {
        return Result.ok(interviewSessionService.detail(id));
    }

    @Operation(summary = "新增面试训练记录")
    @PostMapping
    public Result<InterviewSession> create(@RequestBody InterviewSession session) {
        return Result.ok("训练已创建", interviewSessionService.create(session));
    }

    @Operation(summary = "更新面试训练记录")
    @PutMapping("/{id}")
    public Result<InterviewSession> update(@PathVariable Long id, @RequestBody InterviewSession session) {
        return Result.ok("训练已更新", interviewSessionService.update(id, session));
    }

    @Operation(summary = "删除面试训练记录（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        interviewSessionService.remove(id);
        return Result.ok("训练已删除", null);
    }
}
