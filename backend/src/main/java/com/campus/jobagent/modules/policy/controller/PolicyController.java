package com.campus.jobagent.modules.policy.controller;

import com.campus.jobagent.common.api.PageResult;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.policy.dto.PolicyAskRequest;
import com.campus.jobagent.modules.policy.entity.PolicyItem;
import com.campus.jobagent.modules.policy.service.PolicyService;
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
 * 政策问答与就业数据接口。
 */
@Tag(name = "政策问答", description = "政策库检索、多轮问答（带来源引用）、咨询热点")
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "政策列表")
    @GetMapping
    public Result<PageResult<PolicyItem>> page(@RequestParam(defaultValue = "1") long pageNum,
                                               @RequestParam(defaultValue = "10") long pageSize,
                                               @RequestParam(required = false) String category,
                                               @RequestParam(required = false) String keyword) {
        return Result.ok(PageResult.of(policyService.pagePolicies(pageNum, pageSize, category, keyword)));
    }

    @Operation(summary = "政策详情")
    @GetMapping("/{id}")
    public Result<PolicyItem> detail(@PathVariable Long id) {
        return Result.ok(policyService.detail(id));
    }

    @Operation(summary = "政策智能问答", description = "返回回答正文与政策来源引用")
    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@Valid @RequestBody PolicyAskRequest request) {
        return Result.ok(policyService.ask(request.question()));
    }

    @Operation(summary = "咨询热点统计（辅导员 / 管理员）")
    @PreAuthorize("hasAnyRole('COUNSELOR','COLLEGE_ADMIN')")
    @GetMapping("/hot")
    public Result<Map<String, Object>> hot(@RequestParam(defaultValue = "10") int topN) {
        return Result.ok(policyService.hotQuestions(topN));
    }
}
