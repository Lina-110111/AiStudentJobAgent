package com.campus.jobagent.modules.policy.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.agent.AgentOrchestrator;
import com.campus.jobagent.agent.AgentScene;
import com.campus.jobagent.common.cache.HotCacheService;
import com.campus.jobagent.common.util.JsonUtils;
import com.campus.jobagent.modules.policy.entity.PolicyItem;
import com.campus.jobagent.modules.policy.mapper.PolicyItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 政策问答与就业政策库服务。
 *
 * <p>实现“检索增强问答”的最小闭环：先检索政策库 -> 把命中的政策原文交给智能体
 * -> 智能体基于原文作答并在结论后标注来源编号 -> 接口返回引用列表，便于学生核对。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService extends ServiceImpl<PolicyItemMapper, PolicyItem> {

    private static final int TOP_K = 5;

    private final PolicyRetriever policyRetriever;
    private final AgentOrchestrator agentOrchestrator;
    private final HotCacheService hotCacheService;

    public Page<PolicyItem> pagePolicies(long pageNum, long pageSize, String category, String keyword) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<PolicyItem>lambdaQuery()
                .eq(PolicyItem::getStatus, 1)
                .eq(StringUtils.hasText(category), PolicyItem::getCategory, category)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(PolicyItem::getTitle, keyword)
                        .or().like(PolicyItem::getTags, keyword))
                .orderByDesc(PolicyItem::getPublishDate));
    }

    @Transactional(rollbackFor = Exception.class)
    public PolicyItem detail(Long id) {
        PolicyItem item = getById(id);
        if (item != null) {
            item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
            updateById(item);
        }
        return item;
    }

    /** 多轮问答：检索 + 生成 + 引用来源。 */
    public Map<String, Object> ask(String question) {
        List<PolicyItem> candidates = list(Wrappers.<PolicyItem>lambdaQuery()
                .eq(PolicyItem::getStatus, 1)
                .select(PolicyItem::getId, PolicyItem::getTitle, PolicyItem::getCategory,
                        PolicyItem::getPublishOrg, PolicyItem::getPublishDate,
                        PolicyItem::getContent, PolicyItem::getTags));
        List<PolicyItem> hits = policyRetriever.retrieve(question, candidates, TOP_K);

        StringBuilder context = new StringBuilder();
        List<Map<String, Object>> citations = new ArrayList<>();
        int index = 1;
        for (PolicyItem hit : hits) {
            context.append("[").append(index).append("] 标题：").append(hit.getTitle())
                    .append("；发布单位：").append(safe(hit.getPublishOrg()))
                    .append("；内容：").append(trim(hit.getContent(), 600)).append("\n");
            Map<String, Object> citation = new LinkedHashMap<>();
            citation.put("index", index);
            citation.put("policyId", hit.getId());
            citation.put("title", hit.getTitle());
            citation.put("publishOrg", hit.getPublishOrg());
            citation.put("publishDate", hit.getPublishDate());
            citation.put("category", hit.getCategory());
            citations.add(citation);
            index++;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("学生提问", question);
        payload.put("政策资料", context.isEmpty() ? "（未检索到相关政策条目）" : context.toString());
        payload.put("资料条数", hits.size());

        AgentOrchestrator.AgentResult result =
                agentOrchestrator.run(AgentScene.POLICY_QA, JsonUtils.toJson(payload));
        hotCacheService.recordQuestion(question);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("question", question);
        response.put("answer", result.data().get("answer"));
        response.put("citations", citations);
        response.put("followUpQuestions", result.data().getOrDefault("followUpQuestions", List.of()));
        response.put("matchedPolicies", hits.size());
        response.put("aiGenerated", result.aiGenerated());
        response.put("engine", result.engine());
        return response;
    }

    /** 后台：咨询热点统计。 */
    public Map<String, Object> hotQuestions(int topN) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hotQuestions", hotCacheService.topQuestions(topN));
        result.put("totalPolicies", count(Wrappers.<PolicyItem>lambdaQuery().eq(PolicyItem::getStatus, 1)));
        return result;
    }

    private static String trim(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "……";
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
