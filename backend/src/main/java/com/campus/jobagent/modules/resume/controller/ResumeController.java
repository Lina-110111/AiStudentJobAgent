package com.campus.jobagent.modules.resume.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.resume.entity.Resume;
import com.campus.jobagent.modules.resume.service.ResumeService;
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
 * 简历接口（基础 CRUD 骨架；正式接口建议使用 DTO，权限注解由模块负责人补充）。
 */
@Tag(name = "简历", description = "简历上传登记、AI 诊断结果")
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "简历分页列表")
    @GetMapping
    public Result<Page<Resume>> page(@RequestParam(defaultValue = "1") long pageNum,
                                     @RequestParam(defaultValue = "10") long pageSize,
                                     @RequestParam(required = false) String keyword) {
        return Result.ok(resumeService.pageResumes(pageNum, pageSize, keyword));
    }

    @Operation(summary = "简历详情")
    @GetMapping("/{id}")
    public Result<Resume> detail(@PathVariable Long id) {
        return Result.ok(resumeService.detail(id));
    }

    @Operation(summary = "新增简历")
    @PostMapping
    public Result<Resume> create(@RequestBody Resume resume) {
        return Result.ok("简历已创建", resumeService.create(resume));
    }

    @Operation(summary = "更新简历")
    @PutMapping("/{id}")
    public Result<Resume> update(@PathVariable Long id, @RequestBody Resume resume) {
        return Result.ok("简历已更新", resumeService.update(id, resume));
    }

    @Operation(summary = "删除简历（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        resumeService.remove(id);
        return Result.ok("简历已删除", null);
    }
}
