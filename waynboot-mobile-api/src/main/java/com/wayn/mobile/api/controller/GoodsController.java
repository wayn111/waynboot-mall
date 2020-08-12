package com.wayn.mobile.api.controller;

import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IGoodsDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private IGoodsDetailService iGoodsDetailService;

    @GetMapping("detail/{goodsId}")
    public R detail(@PathVariable Long goodsId) {
        return iGoodsDetailService.getGoodsDetailData(goodsId);
    }
}
