package com.campus.jobagent.modules.policy.service;

import com.campus.jobagent.modules.policy.entity.PolicyItem;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 轻量级政策检索器（关键词倒排的简化实现）。
 *
 * <p>检索策略：中文按 2-gram 切分 + 显式关键词，命中标题权重 3、标签权重 2、正文权重 1，
 * 按得分排序取 Top-K。相比直接上向量库，这种实现无须额外依赖，适合课程设计阶段；
 * 后续可平滑替换为向量检索（RAG），接口保持不变。
 */
@Component
public class PolicyRetriever {

    private static final int TITLE_WEIGHT = 3;
    private static final int TAG_WEIGHT = 2;
    private static final int CONTENT_WEIGHT = 1;

    public List<PolicyItem> retrieve(String question, List<PolicyItem> candidates, int topK) {
        if (!StringUtils.hasText(question) || candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        Set<String> tokens = tokenize(question);
        List<Scored> scored = new ArrayList<>();
        for (PolicyItem item : candidates) {
            int score = 0;
            String title = safe(item.getTitle());
            String tags = safe(item.getTags());
            String content = safe(item.getContent());
            for (String token : tokens) {
                if (title.contains(token)) {
                    score += TITLE_WEIGHT;
                }
                if (tags.contains(token)) {
                    score += TAG_WEIGHT;
                }
                if (content.contains(token)) {
                    score += CONTENT_WEIGHT;
                }
            }
            if (score > 0) {
                scored.add(new Scored(item, score));
            }
        }
        scored.sort(Comparator.comparingInt(Scored::score).reversed());
        return scored.stream().limit(topK).map(Scored::item).toList();
    }

    /** 中文 2-gram + 英文/数字整词。 */
    Set<String> tokenize(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        String cleaned = text.replaceAll("[\\p{Punct}\\s]+", "");
        for (int i = 0; i + 2 <= cleaned.length(); i++) {
            tokens.add(cleaned.substring(i, i + 2));
        }
        for (String part : text.split("[\\s,，。？?！!、；;：:]+")) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    private record Scored(PolicyItem item, int score) {
    }
}
