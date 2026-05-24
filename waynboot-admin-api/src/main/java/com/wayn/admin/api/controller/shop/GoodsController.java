package com.wayn.admin.api.controller.shop;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.goods.request.GoodsSaveRelatedReqVO;
import com.wayn.domain.api.goods.response.GoodsManageDetailResVO;
import com.wayn.common.model.response.GoodsManageListItemResVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 商品管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/shop/goods")
public class GoodsController extends BaseController {

    private IGoodsService iGoodsService;

    /**
     * 商品列表
     *
     * @param goods 查询条件
     * @return 商品分页列表
     */
    @PreAuthorize("@ss.hasPermi('shop:goods:list')")
    @GetMapping("/list")
    public R<IPage<GoodsManageListItemResVO>> list(Goods goods) {
        Page<Goods> page = getPage();
        IPage<Goods> goodsPage = iGoodsService.listPage(page, goods);
        Page<GoodsManageListItemResVO> resPage = new Page<>(goodsPage.getCurrent(), goodsPage.getSize(), goodsPage.getTotal());
        resPage.setRecords(BeanUtil.copyToList(goodsPage.getRecords(), GoodsManageListItemResVO.class));
        return R.success(resPage);
    }

    /**
     * 新增商品
     *
     * @param goodsSaveRelatedReqVO 商品及关联信息
     * @return 处理结果
     */
    @PreAuthorize("@ss.hasPermi('shop:goods:add')")
    @PostMapping
    public R<Boolean> addGoods(@Validated @RequestBody GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) {
        iGoodsService.saveGoodsRelated(goodsSaveRelatedReqVO);
        log.info("新增商品完成, goodsId={}, goodsName={}",
                goodsSaveRelatedReqVO.getGoods().getId(), goodsSaveRelatedReqVO.getGoods().getName());
        return R.success();
    }

    /**
     * 更新商品
     *
     * @param goodsSaveRelatedReqVO 商品及关联信息
     * @return 处理结果
     */
    @PreAuthorize("@ss.hasPermi('shop:goods:update')")
    @PutMapping
    public R<Boolean> updateGoods(@Validated @RequestBody GoodsSaveRelatedReqVO goodsSaveRelatedReqVO) throws IOException {
        iGoodsService.updateGoodsRelated(goodsSaveRelatedReqVO);
        log.info("更新商品完成, goodsId={}, goodsName={}",
                goodsSaveRelatedReqVO.getGoods().getId(), goodsSaveRelatedReqVO.getGoods().getName());
        return R.success();
    }

    /**
     * 获取商品详情
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    @PreAuthorize("@ss.hasPermi('shop:goods:info')")
    @GetMapping("{goodsId}")
    public R<GoodsManageDetailResVO> getGoods(@PathVariable Long goodsId) {
        GoodsManageDetailResVO resVO = iGoodsService.getGoodsInfoById(goodsId);
        return R.success(resVO);
    }

    /**
     * 删除商品
     *
     * @param goodsId 商品 ID
     * @return 处理结果
     */
    @PreAuthorize("@ss.hasPermi('shop:goods:delete')")
    @DeleteMapping("{goodsId}")
    public R<Boolean> deleteGoods(@PathVariable Long goodsId) throws IOException {
        Boolean deleted = iGoodsService.deleteGoodsRelatedByGoodsId(goodsId);
        log.info("删除商品完成, goodsId={}, result={}", goodsId, deleted);
        return R.result(deleted);
    }

    /**
     * 同步商品索引到 ES
     *
     * @return 处理结果
     */
    @PreAuthorize("@ss.hasPermi('shop:goods:syncEs')")
    @PostMapping("syncEs")
    public R<Boolean> syncEs() {
        Boolean synced = iGoodsService.syncGoodsToEs();
        log.info("同步商品索引完成, result={}", synced);
        return R.result(synced);
    }
}
