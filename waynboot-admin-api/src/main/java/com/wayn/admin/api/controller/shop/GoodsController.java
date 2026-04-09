package com.wayn.admin.api.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.model.request.GoodsSaveRelatedReqVO;
import com.wayn.common.model.response.GoodsManageDetailResVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
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
    public R<IPage<Goods>> list(Goods goods) {
        Page<Goods> page = getPage();
        return R.success(iGoodsService.listPage(page, goods));
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
        return R.success(iGoodsService.getGoodsInfoById(goodsId));
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
        return R.result(iGoodsService.deleteGoodsRelatedByGoodsId(goodsId));
    }

    /**
     * 同步商品索引到 ES
     *
     * @return 处理结果
     */
    @PreAuthorize("@ss.hasPermi('shop:goods:syncEs')")
    @PostMapping("syncEs")
    public R<Boolean> syncEs() {
        return R.result(iGoodsService.syncGoodsToEs());
    }
}
