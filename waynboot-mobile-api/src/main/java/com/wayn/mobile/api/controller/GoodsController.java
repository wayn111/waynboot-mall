package com.wayn.mobile.api.controller;

import com.wayn.common.core.service.shop.IGoodsDetailService;
import com.wayn.common.core.vo.GoodsDetailResponseVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品接口
 *
 * @author wayn
 * @since 2024/1/15
 */
@RestController
@AllArgsConstructor
@RequestMapping("goods")
public class GoodsController {

    private IGoodsDetailService iGoodsDetailService;

    /**
     * 商品详情
     *
     * @param goodsId 商品id
     * @return R
     */
    @GetMapping("detail/{goodsId}")
    public R<GoodsDetailResponseVO> detail(@PathVariable Long goodsId) {
        return R.success(iGoodsDetailService.getGoodsDetailData(goodsId));
    }
}
