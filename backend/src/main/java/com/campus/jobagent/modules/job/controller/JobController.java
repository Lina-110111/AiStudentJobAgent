package com.campus.jobagent.modules.job.controller;

import com.campus.jobagent.common.api.PageResult;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.util.SecurityUtils;
import com.campus.jobagent.modules.job.dto.ApplicationStatusRequest;
import com.campus.jobagent.modules.job.dto.JobMatchVO;
import com.campus.jobagent.modules.job.dto.JobPostCreateRequest;
import com.campus.jobagent.modules.job.entity.JobApplication;
import com.campus.jobagent.modules.job.entity.JobPost;
import com.campus.jobagent.modules.job.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 岗位匹配与申请接口。
 */
@Tag(name = "岗位匹配与申请", description = "岗位发布、智能匹配、一键投递、进度跟踪")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @Operation(summary = "发布岗位（企业HR / 辅导员）")
    @PreAuthorize("hasAnyRole('HR','COUNSELOR','COLLEGE_ADMIN')")
    @PostMapping
    public Result<JobPost> publish(@Valid @RequestBody JobPostCreateRequest request) {
        return Result.ok("岗位发布成功", jobService.publish(SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "岗位列表（支持关键词/类别/城市筛选）")
    @GetMapping
    public Result<PageResult<JobPost>> page(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) String city) {
        return Result.ok(PageResult.of(jobService.pageJobs(pageNum, pageSize, keyword, category, city)));
    }

    @Operation(summary = "岗位详情")
    @GetMapping("/{id}")
    public Result<JobPost> detail(@PathVariable Long id) {
        return Result.ok(jobService.getExisting(id));
    }

    @Operation(summary = "岗位下线（发布人 / 院系管理员）")
    @PreAuthorize("hasAnyRole('HR','COUNSELOR','COLLEGE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> offline(@PathVariable Long id) {
        jobService.offline(id, SecurityUtils.getUserId());
        return Result.ok("岗位已下线", null);
    }

    @Operation(summary = "智能匹配：匹配分数 + 匹配说明")
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{id}/match")
    public Result<JobMatchVO> match(@PathVariable Long id) {
        return Result.ok(jobService.match(id, SecurityUtils.getUserId()));
    }

    @Operation(summary = "一键投递")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{id}/apply")
    public Result<JobApplication> apply(@PathVariable Long id,
                                        @RequestParam(required = false) Long resumeId,
                                        @RequestParam(required = false) Integer matchScore) {
        return Result.ok("投递成功，请留意进度", jobService.apply(id, SecurityUtils.getUserId(), resumeId, matchScore));
    }

    @Operation(summary = "我的投递记录")
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my/applications")
    public Result<List<Map<String, Object>>> myApplications() {
        return Result.ok(jobService.myApplications(SecurityUtils.getUserId()));
    }

    @Operation(summary = "岗位申请列表（企业HR / 辅导员）")
    @PreAuthorize("hasAnyRole('HR','COUNSELOR','COLLEGE_ADMIN')")
    @GetMapping("/applications")
    public Result<PageResult<JobApplication>> applications(@RequestParam(required = false) Long jobId,
                                                           @RequestParam(defaultValue = "1") long pageNum,
                                                           @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(PageResult.of(jobService.pageApplications(jobId, pageNum, pageSize)));
    }

    @Operation(summary = "更新申请状态（企业HR / 辅导员）")
    @PreAuthorize("hasAnyRole('HR','COUNSELOR','COLLEGE_ADMIN')")
    @PutMapping("/applications/{applicationId}/status")
    public Result<JobApplication> updateStatus(@PathVariable Long applicationId,
                                               @Valid @RequestBody ApplicationStatusRequest request) {
        return Result.ok("状态已更新", jobService.updateStatus(applicationId, request));
    }
}
