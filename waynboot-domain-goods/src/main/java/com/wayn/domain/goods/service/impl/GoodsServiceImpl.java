package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.goods.support.GoodsElasticSyncSupport;
import com.wayn.domain.goods.support.GoodsMutationSupport;
import com.wayn.domain.goods.support.GoodsQuerySupport;
import com.wayn.domain.goods.support.GoodsValidationSupport;
import com.wayn.domain.api.goods.response.GoodsVO;
import com.wayn.domain.api.goods.request.GoodsSaveRelatedReqVO;
import com.wayn.domain.api.goods.response.GoodsManageDetailResVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 商品服务外观层。
 * 对外保留原有 `IGoodsService` 契约，内部把查询、校验、聚合写入和 ES 同步委托给支撑服务。
 */
@Service
@AllArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    private final GoodsQuerySupport goodsQuerySupport;
    private final GoodsMutationSupport goodsMutationSupport;
    private final GoodsValidationSupport goodsValidationSupport;
    private final GoodsElasticSyncSupport goodsElasticSyncSupport;

    /**
     * 委托查询商品分页。
     *
     * @param page 分页参数
     * @param goods 查询条件
     * @return 商品分页结果
     */
    @Override
    public IPage<Goods> listPage(Page<Goods> page, Goods goods) {
        return goodsQuerySupport.listPage(page, goods);
    }

    /**
     * 委托查询首页商品。
     *
     * @param goods 查询条件
     * @return 商品列表
     */
    @Override
    public List<Goods> selectHomeIndexGoods(Goods goods) {
        return goodsQuerySupport.selectHomeIndexGoods(goods);
    }

    /**
     * 委托查询栏目已绑定商品分页。
     *
     * @param page 分页参数
     * @param goods 查询条件
     * @param columnGoodsIds 已绑定商品 ID
     * @return 分页结果
     */
    @Override
    public IPage<Goods> listColumnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds) {
        return goodsQuerySupport.listColumnBindGoodsPage(page, goods, columnGoodsIds);
    }

    /**
     * 委托查询栏目未绑定商品分页。
     *
     * @param page 分页参数
     * @param goods 查询条件
     * @param columnGoodsIds 已绑定商品 ID
     * @return 分页结果
     */
    @Override
    public IPage<Goods> listColumnUnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds) {
        return goodsQuerySupport.listColumnUnBindGoodsPage(page, goods, columnGoodsIds);
    }

    /**
     * 委托查询商品管理详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    @Override
    public GoodsManageDetailResVO getGoodsInfoById(Long goodsId) {
        return goodsQuerySupport.getGoodsInfoById(goodsId);
    }

    /**
     * 委托保存商品聚合数据。
     *
     * @param goodsSaveRelatedReqVO 商品保存参数
     */
    @Override
    public void saveGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) {
        goodsMutationSupport.saveGoodsRelated(goodsSaveRelatedReqVO);
    }

    /**
     * 委托校验商品名称唯一性。
     *
     * @param goods 商品信息
     * @return 唯一性标识
     */
    @Override
    public String checkGoodsNameUnique(GoodsVO goods) {
        return goodsValidationSupport.checkGoodsNameUnique(goods);
    }

    /**
     * 委托删除商品聚合数据。
     *
     * @param goodsId 商品 ID
     * @return 删除结果
     * @throws IOException 删除 ES 文档时的 IO 异常
     */
    @Override
    public boolean deleteGoodsRelatedByGoodsId(Long goodsId) throws IOException {
        return goodsMutationSupport.deleteGoodsRelatedByGoodsId(goodsId);
    }

    /**
     * 委托更新商品聚合数据。
     *
     * @param goodsSaveRelatedReqVO 商品更新参数
     */
    @Override
    public void updateGoodsRelated(GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) {
        goodsMutationSupport.updateGoodsRelated(goodsSaveRelatedReqVO);
    }

    /**
     * 委托按二级分类查询商品。
     *
     * @param page 分页参数
     * @param l2cateList 二级分类 ID 列表
     * @return 商品列表
     */
    @Override
    public List<Goods> selectListPageByCateIds(Page<Goods> page, List<Long> l2cateList) {
        return goodsQuerySupport.selectListPageByCateIds(page, l2cateList);
    }

    /**
     * 委托按搜索结果商品 ID 查询商品。
     *
     * @param goodsIdList 商品 ID 列表
     * @return 商品列表
     */
    @Override
    public List<Goods> searchResult(List<?> goodsIdList) {
        return goodsQuerySupport.searchResult(goodsIdList);
    }

    /**
     * 委托查询栏目商品分页。
     *
     * @param page 分页参数
     * @param columnId 栏目 ID
     * @return 分页结果
     */
    @Override
    public IPage<Goods> selectColumnGoodsPageByColumnId(Page<Goods> page, Long columnId) {
        return goodsQuerySupport.selectColumnGoodsPageByColumnId(page, columnId);
    }

    /**
     * 委托按商品 ID 批量查询商品。
     *
     * @param goodsIdList 商品 ID 列表
     * @return 商品列表
     */
    @Override
    public List<Goods> selectGoodsByIds(List<Long> goodsIdList) {
        return goodsQuerySupport.selectGoodsByIds(goodsIdList);
    }

    /**
     * 委托更新商品虚拟销量。
     *
     * @param goodsId 商品 ID
     * @param number 销量增量
     */
    @Override
    public void updateVirtualSales(Long goodsId, Integer number) {
        goodsMutationSupport.updateVirtualSales(goodsId, number);
    }

    /**
     * 委托执行商品全量 ES 同步。
     *
     * @return 同步结果
     */
    @Override
    public boolean syncGoodsToEs() {
        return goodsElasticSyncSupport.syncGoodsToEs();
    }

    /**
     * 委托执行单商品 ES 同步。
     *
     * @param goods 商品信息
     * @return 同步结果
     * @throws IOException 写入 ES 时的 IO 异常
     */
    public boolean syncGoods2Es(Goods goods) throws IOException {
        return goodsElasticSyncSupport.syncGoods2Es(goods);
    }
}
