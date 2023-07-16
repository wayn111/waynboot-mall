package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Keyword;
import com.wayn.common.core.mapper.shop.KeywordMapper;
import com.wayn.common.core.service.shop.IKeywordService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 关键字表 服务实现类
 *
 * @author wayn
 * @since 2020-11-02
 */
@Service
@AllArgsConstructor
public class KeywordServiceImpl extends ServiceImpl<KeywordMapper, Keyword> implements IKeywordService {

    private KeywordMapper keywordMapper;

    @Override
    public IPage<Keyword> listPage(Page<Keyword> page, Keyword keyword) {
        return keywordMapper.selectKeywordListPage(page, keyword);
    }
}
