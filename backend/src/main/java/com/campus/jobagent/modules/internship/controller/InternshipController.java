package com.campus.jobagent.modules.internship.controller;

import com.campus.jobagent.common.api.PageResult;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.util.SecurityUtils;
import com.campus.jobagent.modules.internship.dto.ApproveRequest;
import com.campus.jobagent.modules.internship.dto.InternshipApplyRequest;
import com.campus.jobagent.modules.internship.dto.InternshipLogRequest;
import com.campus.jobagent.modules.internship.dto.LogReviewRequest;
import com.campus.jobagent.modules.internship.dto.ScoreRequest;
import com.campus.jobagent.modules.internship.entity.InternshipApply;
import com.campus.jobagent.modules.internship.entity.InternshipLog;
import com.campus.jobagent.modules.internship.service.InternshipService;
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

import java.util.List;

/**
 * 实习管理接口。
 */
@Tag(name = "实习管理", description = "实习申请、多级审批、日志批阅、三方评分")
@RestController
@RequestMapping("/api/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;

    @Operation(summary = "提交实习申请（学生）")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/apply")
    public Result<InternshipApply> apply(@Valid @RequestBody InternshipApplyRequest request) {
        return Result.ok("申请已提交，等待企业导师审批", internshipService.apply(SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "我的实习申请（学生）")
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public Result<PageResult<InternshipApply>> my(@RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(PageResult.of(internshipService.pageMy(SecurityUtils.getUserId(), pageNum, pageSize)));
    }

    @Operation(summary = "待审批列表（辅导员 / 企业导师 / 院系管理员）")
    @PreAuthorize("hasAnyRole('COUNSELOR','COLLEGE_ADMIN','HR')")
    @GetMapping("/pending")
    public Result<PageResult<InternshipApply>> pending(@RequestParam(defaultValue = "1") long pageNum,
                                                       @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(PageResult.of(internshipService.pagePending(pageNum, pageSize)));
    }

    @Operation(summary = "审批（通过则流转到下一节点，驳回则回退上一节点）")
    @PreAuthorize("hasAnyRole('COUNSELOR','COLLEGE_ADMIN','HR')")
    @PostMapping("/{id}/approve")
    public Result<InternshipApply> approve(@PathVariable Long id, @Valid @RequestBody ApproveRequest request) {
        return Result.ok("审批已提交", internshipService.approve(id, SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "提交实习日志（学生）")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{id}/logs")
    public Result<InternshipLog> submitLog(@PathVariable Long id,
                                           @Valid @RequestBody InternshipLogRequest request) {
        return Result.ok("日志已提交", internshipService.submitLog(id, SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "查看实习日志")
    @GetMapping("/{id}/logs")
    public Result<List<InternshipLog>> logs(@PathVariable Long id) {
        return Result.ok(internshipService.listLogs(id));
    }

    @Operation(summary = "批阅日志（辅导员），同时生成智能体点评")
    @PreAuthorize("hasAnyRole('COUNSELOR','COLLEGE_ADMIN')")
    @PostMapping("/logs/{logId}/review")
    public Result<InternshipLog> reviewLog(@PathVariable Long logId, @RequestBody LogReviewRequest request) {
        return Result.ok("批阅完成", internshipService.reviewLog(logId, request));
    }

    @Operation(summary = "三方评分（学生自评 / 企业导师 / 校方）")
    @PostMapping("/{id}/scores")
    public Result<InternshipApply> submitScore(@PathVariable Long id, @Valid @RequestBody ScoreRequest request) {
        return Result.ok("评分已提交", internshipService.submitScore(id, request));
    }
}
