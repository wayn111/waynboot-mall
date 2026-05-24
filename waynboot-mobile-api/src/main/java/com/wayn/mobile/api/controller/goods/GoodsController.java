package com.wayn.mobile.api.controller.goods;

import com.wayn.domain.api.goods.service.IGoodsDetailService;
import com.wayn.domain.api.goods.response.GoodsDetailResponseVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("查询商品详情开始, goodsId={}", goodsId);
        GoodsDetailResponseVO resVO = iGoodsDetailService.getGoodsDetailData(goodsId);
        log.info("查询商品详情完成, goodsId={}", goodsId);
        return R.success(resVO);
    }
}
