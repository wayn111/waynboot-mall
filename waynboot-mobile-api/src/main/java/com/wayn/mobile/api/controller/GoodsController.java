package com.wayn.mobile.api.controller;

import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IGoodsDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private IGoodsDetailService iGoodsDetailService;

    @PostMapping("detail")
    public R detail(@RequestBody Map<String, Long> map) {
        Long goodsId = map.get("goodsId");
        return iGoodsDetailService.getGoodsDetailData(goodsId);
    }
}
