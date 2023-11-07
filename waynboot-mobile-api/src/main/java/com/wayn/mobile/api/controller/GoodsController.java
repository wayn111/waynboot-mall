package com.wayn.mobile.api.controller;

import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IGoodsDetailService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("goods")
public class GoodsController {

    private IGoodsDetailService iGoodsDetailService;

    @GetMapping("detail/{goodsId}")
    public R detail(@PathVariable Long goodsId) {
        return R.success(iGoodsDetailService.getGoodsDetailData(goodsId));
    }
}
