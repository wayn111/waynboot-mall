package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("home")
public class HomeController extends BaseController {

    @Autowired
    private HomeService homeService;

    @PostMapping("index")
    public R index() {
        return homeService.getHomeIndexData();
    }

    @GetMapping("goodsList")
    public R getGoodsList() {
        Page<Goods> page = getPage();
        return homeService.listGoodsPage(page);
    }
}

