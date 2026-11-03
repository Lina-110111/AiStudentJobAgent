package com.campus.jobagent.modules.job.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "岗位匹配与申请", description = "岗位发布、智能匹配、一键投递、进度跟踪")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @Operation(summary = "发布岗位")
    @PreAuthorize("hasAuthority('job:add')")          // ← 只有配了 job:add 权限的人能调
    @PostMapping
    public Result<JobPost> publish(@Valid @RequestBody JobPostCreateRequest request) {
        return Result.ok("岗位发布成功", jobService.publish(SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "岗位列表（关键词/类别/城市筛选）")
    @PreAuthorize("hasAuthority('job:view')")         // ← 能看岗位列表
    @GetMapping
    public Result<Page<JobPost>> page(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String city) {
        return Result.ok(jobService.pageJobs(pageNum, pageSize, keyword, category, city));
    }

    @Operation(summary = "岗位详情")
    @PreAuthorize("hasAuthority('job:view')")         // ← 能看岗位详情
    @GetMapping("/{id}")
    public Result<JobPost> detail(@PathVariable Long id) {
        return Result.ok(jobService.getExisting(id));
    }

    @Operation(summary = "岗位下线")
    @PreAuthorize("hasAuthority('job:delete')")       // ← 只有配了 job:delete 权限的人能调
    @DeleteMapping("/{id}")
    public Result<Void> offline(@PathVariable Long id) {
        jobService.offline(id, SecurityUtils.getUserId());
        return Result.ok("岗位已下线", null);
    }

    @Operation(summary = "智能匹配：匹配分数 + 匹配说明")
    @PreAuthorize("hasAuthority('job:view')")
    @GetMapping("/{id}/match")
    public Result<JobMatchVO> match(@PathVariable Long id) {
        return Result.ok(jobService.match(id, SecurityUtils.getUserId()));
    }

    @Operation(summary = "一键投递")
    @PreAuthorize("hasAuthority('job:view')")
    @PostMapping("/{id}/apply")
    public Result<JobApplication> apply(@PathVariable Long id,
                                        @RequestParam(required = false) Long resumeId,
                                        @RequestParam(required = false) Integer matchScore) {
        return Result.ok("投递成功，请留意进度",
                jobService.apply(id, SecurityUtils.getUserId(), resumeId, matchScore));
    }

    @Operation(summary = "我的投递记录")
    @PreAuthorize("hasAuthority('job:view')")
    @GetMapping("/my/applications")
    public Result<List<Map<String, Object>>> myApplications() {
        return Result.ok(jobService.myApplications(SecurityUtils.getUserId()));
    }

    @Operation(summary = "岗位申请列表（企业HR/辅导员/管理员）")
    @PreAuthorize("hasAuthority('resume:view')")
    @GetMapping("/applications")
    public Result<Page<JobApplication>> applications(@RequestParam(required = false) Long jobId,
                                                     @RequestParam(defaultValue = "1") long pageNum,
                                                     @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(jobService.pageApplications(jobId, pageNum, pageSize));
    }

    @Operation(summary = "更新申请状态")
    @PreAuthorize("hasAuthority('job:edit')")         // ← 只有配了 job:edit 权限的人能调
    @PutMapping("/applications/{applicationId}/status")
    public Result<JobApplication> updateStatus(@PathVariable Long applicationId,
                                               @Valid @RequestBody ApplicationStatusRequest request) {
        return Result.ok("状态已更新", jobService.updateStatus(applicationId, request));
    }
}
