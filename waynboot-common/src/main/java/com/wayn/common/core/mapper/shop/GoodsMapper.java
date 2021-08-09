package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.SearchVO;

import java.util.List;

/**
 * <p>
 * 商品基本信息表 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    IPage<Goods> selectGoodsListPage(Page<Goods> page, Goods goods);

    IPage<Goods> selectGoodsListPageByl2CateId(Page<Goods> page, List<Long> cateList);

    List<Goods> searchResult(Page<SearchVO> page, SearchVO searchVO);

    IPage<Goods> selectColumnBindGoodsListPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds);

    IPage<Goods> selectColumnUnBindGoodsListPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds);

    List<Goods> selectHomeIndex(Goods goods);

    IPage<Goods> selectColumnGoodsPage(Page<Goods> page, Long columnId);
}
