package com.campus.jobagent.modules.policy.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.policy.entity.PolicyItem;
import com.campus.jobagent.modules.policy.mapper.PolicyItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 就业政策服务（基础 CRUD 骨架；检索问答业务由模块负责人扩展）。
 */
@Slf4j
@Service
public class PolicyItemService extends ServiceImpl<PolicyItemMapper, PolicyItem> {

    /** 政策分页列表（标题/分类/地区关键词筛选）。 */
    public Page<PolicyItem> pageItems(long pageNum, long pageSize, String keyword, String category, String region) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<PolicyItem>lambdaQuery()
                .eq(StringUtils.hasText(category), PolicyItem::getCategory, category)
                .eq(StringUtils.hasText(region), PolicyItem::getRegion, region)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(PolicyItem::getTitle, keyword)
                        .or().like(PolicyItem::getTags, keyword))
                .orderByDesc(PolicyItem::getId));
    }

    /** 按 id 查询，不存在抛异常。 */
    public PolicyItem detail(Long id) {
        PolicyItem item = getById(id);
        if (item == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "政策条目不存在");
        }
        return item;
    }

    /** 新增政策条目。 */
    @Transactional(rollbackFor = Exception.class)
    public PolicyItem create(PolicyItem item) {
        item.setId(null);
        if (item.getViewCount() == null) {
            item.setViewCount(0);
        }
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        item.setDeleted(0);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        save(item);
        return item;
    }

    /** 更新政策条目。 */
    @Transactional(rollbackFor = Exception.class)
    public PolicyItem update(Long id, PolicyItem item) {
        PolicyItem existing = detail(id);
        item.setId(id);
        item.setCreateTime(existing.getCreateTime());
        item.setUpdateTime(LocalDateTime.now());
        updateById(item);
        return getById(id);
    }

    /** 逻辑删除政策条目。 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }
}
