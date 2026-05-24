package com.wayn.mobile.api.controller.goods;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.trade.service.IHomeService;
import com.wayn.domain.api.trade.response.HomeIndexResponseVO;
import com.wayn.domain.api.trade.response.MallConfigResponseVO;
import com.wayn.common.model.response.RecommonGoodsItemResVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页接口
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("home")
public class HomeController extends BaseController {

    private final IHomeService iHomeService;

    /**
     * 商城首页
     *
     * @return R
     */
    @GetMapping("index")
    public R<HomeIndexResponseVO> index() {
        log.info("查询首页数据开始");
        HomeIndexResponseVO resVO = iHomeService.index();
        log.info("查询首页数据完成");
        return R.success(resVO);
    }

    /**
     * 获取商城配置
     *
     * @return R
     */
    @GetMapping("mallConfig")
    public R<MallConfigResponseVO> mallConfig() {
        log.info("查询商城配置开始");
        MallConfigResponseVO resVO = iHomeService.mallConfig();
        log.info("查询商城配置完成");
        return R.success(resVO);
    }

    /**
     * 为你推荐商品列表
     *
     * @return R
     */
    @GetMapping("recommonGoodsList")
    public R<List<RecommonGoodsItemResVO>> recommonGoodsList() {
        Page<Goods> page = getPage();
        log.info("查询推荐商品开始, pageNum={}, pageSize={}", page.getCurrent(), page.getSize());
        List<RecommonGoodsItemResVO> goodsList = iHomeService.listGoodsPage(page)
                .stream()
                .map(this::toRecommonGoodsItemResVO)
                .toList();
        log.info("查询推荐商品完成, pageNum={}, pageSize={}, count={}", page.getCurrent(), page.getSize(), goodsList.size());
        return R.success(goodsList);
    }

    private RecommonGoodsItemResVO toRecommonGoodsItemResVO(Goods goods) {
        RecommonGoodsItemResVO resVO = new RecommonGoodsItemResVO();
        resVO.setId(goods.getId());
        resVO.setName(goods.getName());
        resVO.setBrief(goods.getBrief());
        resVO.setPicUrl(goods.getPicUrl());
        resVO.setCounterPrice(goods.getCounterPrice());
        resVO.setRetailPrice(goods.getRetailPrice());
        resVO.setIsNew(goods.getIsNew());
        resVO.setIsHot(goods.getIsHot());
        resVO.setActualSales(goods.getActualSales());
        resVO.setVirtualSales(goods.getVirtualSales());
        return resVO;
    }
}
