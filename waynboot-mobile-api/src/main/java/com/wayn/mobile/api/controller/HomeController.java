package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.core.service.shop.IHomeService;
import com.wayn.common.core.vo.HomeIndexResponseVO;
import com.wayn.common.core.vo.MallConfigResponseVO;
import com.wayn.common.core.vo.RecommonGoodsResponseVO;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页接口
 */
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
    public R<HomeIndexResponseVO> index() {
        return R.success(iHomeService.index());
    }

    /**
     * 获取商城配置
     *
     * @return R
     */
    @GetMapping("mallConfig")
    public R<MallConfigResponseVO> mallConfig() {
        return R.success(iHomeService.mallConfig());
    }

    /**
     * 为你推荐
     *
     * @return R
     */
    @GetMapping("recommonGoodsList")
    public R<List<Goods>> recommonGoodsList() {
        Page<Goods> page = getPage();
        return R.success(iHomeService.listGoodsPage(page));
    }
}

