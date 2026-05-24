package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.goods.entity.Keyword;
import com.wayn.domain.api.goods.mapper.KeywordMapper;
import com.wayn.domain.api.goods.service.IKeywordService;
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
