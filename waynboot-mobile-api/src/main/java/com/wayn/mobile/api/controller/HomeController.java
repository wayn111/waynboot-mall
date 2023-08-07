package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IHomeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("home")
public class HomeController extends BaseController {

    private IHomeService IHomeService;

    /**
     * 商城首页
     *
     * @return R
     */
    @GetMapping("index")
    public R index() {
        return IHomeService.index();
    }

    /**
     * 获取商城配置
     *
     * @return R
     */
    @GetMapping("mallConfig")
    public R mallConfig() {
        return IHomeService.mallConfig();
    }

    /**
     * 为你推荐
     *
     * @return R
     */
    @GetMapping("goodsList")
    public R getGoodsList() {
        Page<Goods> page = getPage();
        return IHomeService.listGoodsPage(page);
    }
}

