package com.campus.jobagent.modules.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * 岗位匹配结果。
 */
@Schema(description = "岗位匹配结果")
public record JobMatchVO(
        Long jobId,
        String jobTitle,
        String companyName,
        int matchScore,
        String matchLevel,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> scoreReasons,
        Map<String, Object> agentExplanation
) {
}
