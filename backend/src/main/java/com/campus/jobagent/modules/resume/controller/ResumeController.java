package com.campus.jobagent.modules.resume.controller;

import com.campus.jobagent.common.api.PageResult;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.storage.FileStorageService;
import com.campus.jobagent.common.util.SecurityUtils;
import com.campus.jobagent.modules.resume.dto.ResumeCreateRequest;
import com.campus.jobagent.modules.resume.entity.Resume;
import com.campus.jobagent.modules.resume.service.ResumeService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 智能简历接口。
 */
@Tag(name = "智能简历", description = "简历上传、结构化登记、AI 诊断")
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "上传简历附件（PDF / Word / TXT）")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        FileStorageService.StoredFile stored = fileStorageService.store(file, "resume");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileName", stored.originalName());
        data.put("filePath", stored.relativePath());
        data.put("url", stored.url());
        data.put("size", stored.size());
        data.put("fileType", stored.originalName().contains(".")
                ? stored.originalName().substring(stored.originalName().lastIndexOf('.') + 1).toLowerCase()
                : "txt");
        return Result.ok("上传成功", data);
    }

    @Operation(summary = "登记简历")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public Result<Resume> create(@Valid @RequestBody ResumeCreateRequest request) {
        return Result.ok("简历已保存", resumeService.create(SecurityUtils.getUserId(), request));
    }

    @Operation(summary = "我的简历列表")
    @GetMapping
    public Result<PageResult<Resume>> myResumes(@RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(PageResult.of(resumeService.pageByUser(SecurityUtils.getUserId(), pageNum, pageSize)));
    }

    @Operation(summary = "简历详情")
    @GetMapping("/{id}")
    public Result<Resume> detail(@PathVariable Long id) {
        boolean allowAny = !"STUDENT".equals(SecurityUtils.getRoleCode());
        return Result.ok(resumeService.getOwned(id, SecurityUtils.getUserId(), allowAny));
    }

    @Operation(summary = "AI 简历诊断", description = "返回优势 / 不足 / 修改建议 / 缺失关键词")
    @PostMapping("/{id}/diagnose")
    public Result<Map<String, Object>> diagnose(@PathVariable Long id,
                                                @RequestParam(required = false) String targetJob) {
        return Result.ok("诊断完成", resumeService.diagnose(id, SecurityUtils.getUserId(), targetJob));
    }

    @Operation(summary = "查看最近一次诊断报告")
    @GetMapping("/{id}/diagnosis")
    public Result<String> lastDiagnosis(@PathVariable Long id) {
        Resume resume = resumeService.getOwned(id, SecurityUtils.getUserId(), true);
        if (resume.getDiagnosisJson() == null) {
            throw BizException.of(ResultCode.RESUME_NOT_FOUND, "该简历还没有诊断报告，请先执行诊断");
        }
        return Result.ok(resume.getDiagnosisJson());
    }
}
