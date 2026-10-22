package com.campus.jobagent.modules.internship.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.internship.entity.InternshipApply;
import com.campus.jobagent.modules.internship.service.InternshipApplyService;
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
 * 实习申请接口（基础 CRUD 骨架；审批流转接口由模块负责人补充）。
 */
@Tag(name = "实习申请", description = "实习申请提交、审批流转与评分")
@RestController
@RequestMapping("/api/internship-applies")
@RequiredArgsConstructor
public class InternshipApplyController {

    private final InternshipApplyService internshipApplyService;

    @Operation(summary = "实习申请分页列表")
    @GetMapping
    public Result<Page<InternshipApply>> page(@RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String status) {
        return Result.ok(internshipApplyService.pageApplies(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "实习申请详情")
    @GetMapping("/{id}")
    public Result<InternshipApply> detail(@PathVariable Long id) {
        return Result.ok(internshipApplyService.detail(id));
    }

    @Operation(summary = "新增实习申请")
    @PostMapping
    public Result<InternshipApply> create(@RequestBody InternshipApply apply) {
        return Result.ok("实习申请已提交", internshipApplyService.create(apply));
    }

    @Operation(summary = "更新实习申请")
    @PutMapping("/{id}")
    public Result<InternshipApply> update(@PathVariable Long id, @RequestBody InternshipApply apply) {
        return Result.ok("实习申请已更新", internshipApplyService.update(id, apply));
    }

    @Operation(summary = "删除实习申请（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        internshipApplyService.remove(id);
        return Result.ok("实习申请已删除", null);
    }
}
