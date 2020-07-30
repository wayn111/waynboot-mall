package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.wayn.common.core.domain.shop.GoodsSpecification;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.core.service.shop.IGoodsSpecificationService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private IGoodsService iGoodsService;

    @Autowired
    private IGoodsSpecificationService iGoodsSpecificationService;

    @PostMapping("detail")
    public R detail(@RequestBody Map<String, Long> map) {
        R success = R.success();
        Long goodsId = map.get("goodsId");
        List<GoodsSpecification> list = iGoodsSpecificationService.list(new QueryWrapper<GoodsSpecification>().eq("goods_id", goodsId));
        success.add("goods", iGoodsService.getById(goodsId));
        success.add("skuList", list);
        return success;
    }
}
