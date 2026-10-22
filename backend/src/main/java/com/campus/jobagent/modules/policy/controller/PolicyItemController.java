package com.campus.jobagent.modules.policy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.policy.entity.PolicyItem;
import com.campus.jobagent.modules.policy.service.PolicyItemService;
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
 * 就业政策接口（基础 CRUD 骨架；政策问答接口由模块负责人补充）。
 */
@Tag(name = "就业政策", description = "就业政策知识库")
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyItemController {

    private final PolicyItemService policyItemService;

    @Operation(summary = "政策分页列表")
    @GetMapping
    public Result<Page<PolicyItem>> page(@RequestParam(defaultValue = "1") long pageNum,
                                         @RequestParam(defaultValue = "10") long pageSize,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String region) {
        return Result.ok(policyItemService.pageItems(pageNum, pageSize, keyword, category, region));
    }

    @Operation(summary = "政策详情")
    @GetMapping("/{id}")
    public Result<PolicyItem> detail(@PathVariable Long id) {
        return Result.ok(policyItemService.detail(id));
    }

    @Operation(summary = "新增政策条目")
    @PostMapping
    public Result<PolicyItem> create(@RequestBody PolicyItem item) {
        return Result.ok("政策已录入", policyItemService.create(item));
    }

    @Operation(summary = "更新政策条目")
    @PutMapping("/{id}")
    public Result<PolicyItem> update(@PathVariable Long id, @RequestBody PolicyItem item) {
        return Result.ok("政策已更新", policyItemService.update(id, item));
    }

    @Operation(summary = "删除政策条目（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        policyItemService.remove(id);
        return Result.ok("政策已删除", null);
    }
}
