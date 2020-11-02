package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Keyword;
import com.wayn.common.core.mapper.shop.KeywordMapper;
import com.wayn.common.core.service.shop.IKeywordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 关键字表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-11-02
 */
@Service
public class KeywordServiceImpl extends ServiceImpl<KeywordMapper, Keyword> implements IKeywordService {

    @Autowired
    private KeywordMapper keywordMapper;

    @Override
    public IPage<Keyword> listPage(Page<Keyword> page, Keyword keyword) {
        return keywordMapper.selectKeywordListPage(page, keyword);
    }
}
