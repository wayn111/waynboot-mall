package com.wayn.domain.api.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.response.GoodsVO;
import com.wayn.domain.api.goods.request.GoodsSaveRelatedReqVO;
import com.wayn.domain.api.goods.response.GoodsManageDetailResVO;

import java.io.IOException;
import java.util.List;

/**
 * 商品基本信息服务
 *
 * @author wayn
 * @since 2020-07-06
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 分页查询商品列表
     *
     * @param page 分页参数
     * @param goods 查询条件
     * @return 商品分页结果
     */
    IPage<Goods> listPage(Page<Goods> page, Goods goods);

    /**
     * 查询首页商品列表
     *
     * @param goods 查询条件
     * @return 商品列表
     */
    List<Goods> selectHomeIndexGoods(Goods goods);

    IPage<Goods> listColumnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds);

    IPage<Goods> listColumnUnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds);

    /**
     * 获取商品管理详情
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    GoodsManageDetailResVO getGoodsInfoById(Long goodsId);

    /**
     * 保存商品及其关联数据
     *
     * @param goodsSaveRelatedReqVO 商品保存参数
     */
    void saveGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO);

    /**
     * 校验商品名称是否唯一
     *
     * @param goods 商品信息
     * @return 唯一性标识
     */
    String checkGoodsNameUnique(GoodsVO goods);

    /**
     * 根据商品 ID 删除商品及其关联数据
     *
     * @param goodsId 商品 ID
     * @return 处理结果
     */
    boolean deleteGoodsRelatedByGoodsId(Long goodsId) throws IOException;

    /**
     * 更新商品及其关联数据
     *
     * @param goodsSaveRelatedReqVO 商品更新参数
     */
    void updateGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) throws IOException;

    /**
     * 根据二级分类 ID 集合查询商品
     *
     * @param page 分页参数
     * @param l2cateList 二级分类 ID 集合
     * @return 商品列表
     */
    List<Goods> selectListPageByCateIds(Page<Goods> page, List<Long> l2cateList);

    /**
     * 根据搜索结果中的商品 ID 查询商品
     *
     * @param goodsIdList 商品 ID 集合
     * @return 商品列表
     */
    List<Goods> searchResult(List<?> goodsIdList);

    /**
     * 根据栏目 ID 查询栏目下商品
     *
     * @param page 分页参数
     * @param columnId 栏目 ID
     * @return 分页结果
     */
    IPage<Goods> selectColumnGoodsPageByColumnId(Page<Goods> page, Long columnId);

    List<Goods> selectGoodsByIds(List<Long> goodsIdList);

    void updateVirtualSales(Long goodsId, Integer number);

    boolean syncGoodsToEs();
}
