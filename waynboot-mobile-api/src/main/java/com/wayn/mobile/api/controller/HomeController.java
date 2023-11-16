package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.util.R;
import com.wayn.mobile.api.service.IHomeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("home")
public class HomeController extends BaseController {

    private IHomeService iHomeService;

    /**
     * 商城首页
     *
     * @return R
     */
    @GetMapping("index")
    public R index() {
        return R.success(iHomeService.index());
    }

    /**
     * 获取商城配置
     *
     * @return R
     */
    @GetMapping("mallConfig")
    public R mallConfig() {
        return R.success(iHomeService.mallConfig());
    }

    /**
     * 为你推荐
     *
     * @return R
     */
    @GetMapping("recommonGoodsList")
    public R recommonGoodsList() {
        Page<Goods> page = getPage();
        return R.success(iHomeService.listGoodsPage(page));
    }
}

