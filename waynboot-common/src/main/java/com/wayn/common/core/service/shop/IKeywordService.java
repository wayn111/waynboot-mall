package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Keyword;

/**
 * <p>
 * 关键字表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-11-02
 */
public interface IKeywordService extends IService<Keyword> {

    /**
     * 查询关键字分页列表
     *
     * @param page    分页对象
     * @param keyword 查询参数
     * @return 键字分页列表
     */
    IPage<Keyword> listPage(Page<Keyword> page, Keyword keyword);
}
