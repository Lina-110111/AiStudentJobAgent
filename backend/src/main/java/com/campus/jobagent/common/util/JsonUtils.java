package com.campus.jobagent.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON 工具：大模型返回的文本常常包裹 ```json 代码块，这里做统一清洗与解析。
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtils() {
    }

    /** 去掉 markdown 代码块围栏，截取最外层 JSON 对象。 */
    public static String extractJson(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            if (firstLineEnd > 0) {
                text = text.substring(firstLineEnd + 1);
            }
            int fence = text.lastIndexOf("```");
            if (fence >= 0) {
                text = text.substring(0, fence);
            }
            text = text.trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /** 解析为 Map；解析失败时把原文放入 raw 字段，保证接口不因模型输出异常而报错。 */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String raw) {
        String json = extractJson(raw);
        Map<String, Object> result = new LinkedHashMap<>();
        if (json.isBlank()) {
            result.put("raw", "");
            return result;
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            result.put("raw", raw);
            result.put("parseError", e.getMessage());
            return result;
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
