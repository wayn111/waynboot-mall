package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IHomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("home")
public class HomeController extends BaseController {

    @Autowired
    private IHomeService IHomeService;

    @PostMapping("index")
    public R index() {
        return IHomeService.getHomeIndexDataCompletableFuture();
    }

    @PostMapping("index1")
    public R index1() {
        return IHomeService.getHomeIndexDataCompletableFuture();
    }

    @GetMapping("goodsList")
    public R getGoodsList() {
        Page<Goods> page = getPage();
        return IHomeService.listGoodsPage(page);
    }
}

