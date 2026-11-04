package com.campus.jobagent.modules.internship.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.internship.entity.InternshipLog;
import com.campus.jobagent.modules.internship.service.InternshipLogService;
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
 * 实习日志接口（基础 CRUD 骨架；批阅与点评接口由模块负责人补充）。
 */
@Tag(name = "实习日志", description = "学生周日志、辅导员批阅与智能体点评")
@RestController
@RequestMapping("/api/internship-logs")
@RequiredArgsConstructor
public class InternshipLogController {

    private final InternshipLogService internshipLogService;

    @Operation(summary = "实习日志分页列表（可按实习申请筛选）")
    @GetMapping
    public Result<Page<InternshipLog>> page(@RequestParam(required = false) Long applyId,
                                            @RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(internshipLogService.pageLogs(applyId, pageNum, pageSize));
    }

    @Operation(summary = "实习日志详情")
    @GetMapping("/{id}")
    public Result<InternshipLog> detail(@PathVariable Long id) {
        return Result.ok(internshipLogService.detail(id));
    }

    @Operation(summary = "新增实习日志")
    @PostMapping
    public Result<InternshipLog> create(@RequestBody InternshipLog log) {
        return Result.ok("日志已提交", internshipLogService.create(log));
    }

    @Operation(summary = "更新实习日志")
    @PutMapping("/{id}")
    public Result<InternshipLog> update(@PathVariable Long id, @RequestBody InternshipLog log) {
        return Result.ok("日志已更新", internshipLogService.update(id, log));
    }

    @Operation(summary = "删除实习日志（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        internshipLogService.remove(id);
        return Result.ok("日志已删除", null);
    }
}
