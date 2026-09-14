package com.campus.jobagent.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 热数据缓存（Redis）。
 *
 * <p>设计要点：Redis 属于“增强能力”，不可用时应降级为直连数据库，不能影响主流程。
 * 因此这里对异常做了吞掉并记日志的处理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotCacheService {

    private static final String HOT_QUESTION_KEY = "jobagent:policy:hot_question";

    private final StringRedisTemplate redisTemplate;

    /** 记录一次政策提问，用于后台统计“咨询热点”。 */
    public void recordQuestion(String question) {
        if (question == null || question.isBlank()) {
            return;
        }
        try {
            redisTemplate.opsForZSet().incrementScore(HOT_QUESTION_KEY, question.trim(), 1D);
        } catch (Exception e) {
            log.debug("Redis 不可用，跳过热点统计：{}", e.getMessage());
        }
    }

    /** 读取最热门的 N 个问题。 */
    public Map<String, Double> topQuestions(int topN) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(HOT_QUESTION_KEY, 0, topN - 1L);
            if (tuples != null) {
                for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                    if (tuple.getValue() != null) {
                        result.put(tuple.getValue(), tuple.getScore() == null ? 0D : tuple.getScore());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Redis 不可用，热点问题返回空：{}", e.getMessage());
        }
        return result;
    }

    /** 带缓存的读操作模板。 */
    public String getOrLoad(String key, java.util.function.Supplier<String> loader) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.debug("Redis 读取失败，降级直查：{}", e.getMessage());
        }
        String value = loader.get();
        try {
            redisTemplate.opsForValue().set(key, value, java.time.Duration.ofMinutes(10));
        } catch (Exception e) {
            log.debug("Redis 写入失败：{}", e.getMessage());
        }
        return value;
    }
}
