package com.wayn.domain.goods.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.domain.api.goods.entity.Category;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.entity.GoodsAttribute;
import com.wayn.domain.api.goods.entity.GoodsProduct;
import com.wayn.domain.api.goods.entity.GoodsSpecification;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.domain.api.goods.service.ICategoryService;
import com.wayn.domain.api.goods.service.IGoodsAttributeService;
import com.wayn.domain.api.goods.service.IGoodsProductService;
import com.wayn.domain.api.goods.service.IGoodsSpecificationService;
import com.wayn.domain.api.goods.response.GoodsManageDetailResVO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商品查询支撑服务。
 * 聚合商品管理详情、栏目分页和首页/搜索商品查询，避免商品写路径继续夹带只读拼装逻辑。
 */
@Service
@AllArgsConstructor
public class GoodsQuerySupport {

    private final GoodsMapper goodsMapper;
    private final IGoodsProductService goodsProductService;
    private final IGoodsAttributeService goodsAttributeService;
    private final IGoodsSpecificationService goodsSpecificationService;
    private final ICategoryService categoryService;

    /**
     * 分页查询商品列表。
     *
     * @param page 分页参数
     * @param goods 查询条件
     * @return 商品分页结果
     */
    public IPage<Goods> listPage(Page<Goods> page, Goods goods) {
        return goodsMapper.selectGoodsListPage(page, goods);
    }

    /**
     * 查询首页展示商品。
     *
     * @param goods 查询条件
     * @return 首页商品列表
     */
    public List<Goods> selectHomeIndexGoods(Goods goods) {
        return goodsMapper.selectHomeIndex(goods);
    }

    /**
     * 查询已绑定到栏目的商品分页。
     *
     * @param page 分页参数
     * @param goods 查询条件
     * @param columnGoodsIds 已绑定商品 ID
     * @return 分页结果
     */
    public IPage<Goods> listColumnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds) {
        return goodsMapper.selectColumnBindGoodsListPage(page, goods, columnGoodsIds);
    }

    /**
     * 查询未绑定到栏目的商品分页。
     *
     * @param page 分页参数
     * @param goods 查询条件
     * @param columnGoodsIds 已绑定商品 ID
     * @return 分页结果
     */
    public IPage<Goods> listColumnUnBindGoodsPage(Page<Goods> page, Goods goods, List<Long> columnGoodsIds) {
        return goodsMapper.selectColumnUnBindGoodsListPage(page, goods, columnGoodsIds);
    }

    /**
     * 查询商品管理详情。
     * 该方法会一次性聚合商品、分类、规格、属性和货品信息，供管理端编辑页面直接使用。
     *
     * @param goodsId 商品 ID
     * @return 商品管理详情
     */
    public GoodsManageDetailResVO getGoodsInfoById(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException(ReturnCodeEnum.PARAMETER_TYPE_ERROR);
        }
        List<GoodsProduct> goodsProducts = goodsProductService.list(new QueryWrapper<GoodsProduct>().eq("goods_id", goodsId));
        List<GoodsAttribute> goodsAttributes = goodsAttributeService.list(new QueryWrapper<GoodsAttribute>().eq("goods_id", goodsId));
        List<GoodsSpecification> goodsSpecifications = goodsSpecificationService.list(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        Category category = categoryService.getById(goods.getCategoryId());
        List<Long> categoryIds = new ArrayList<>();
        if (category != null) {
            categoryIds.add(category.getPid());
            categoryIds.add(goods.getCategoryId());
        }
        GoodsManageDetailResVO data = new GoodsManageDetailResVO();
        data.setGoods(goods);
        data.setSpecifications(goodsSpecifications);
        data.setProducts(goodsProducts);
        data.setAttributes(goodsAttributes);
        data.setCategoryIds(categoryIds);
        data.setCategory(category);
        return data;
    }

    /**
     * 根据二级分类分页查询商品。
     *
     * @param page 分页参数
     * @param l2cateList 二级分类 ID 列表
     * @return 商品列表
     */
    public List<Goods> selectListPageByCateIds(Page<Goods> page, List<Long> l2cateList) {
        return goodsMapper.selectGoodsListPageByl2CateId(page, l2cateList).getRecords();
    }

    /**
     * 根据搜索结果中的商品 ID 批量查询商品。
     *
     * @param goodsIdList 商品 ID 列表
     * @return 商品列表
     */
    public List<Goods> searchResult(List<?> goodsIdList) {
        if (goodsIdList == null || goodsIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return goodsMapper.selectHomeGoodsListByIds(goodsIdList);
    }

    /**
     * 查询栏目下商品分页。
     *
     * @param page 分页参数
     * @param columnId 栏目 ID
     * @return 分页结果
     */
    public IPage<Goods> selectColumnGoodsPageByColumnId(Page<Goods> page, Long columnId) {
        return goodsMapper.selectColumnGoodsPage(page, columnId);
    }

    /**
     * 根据商品 ID 列表批量查询商品。
     *
     * @param goodsIdList 商品 ID 列表
     * @return 商品列表
     */
    public List<Goods> selectGoodsByIds(List<Long> goodsIdList) {
        if (goodsIdList == null || goodsIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return goodsMapper.selectGoodsListByIds(goodsIdList);
    }
}
