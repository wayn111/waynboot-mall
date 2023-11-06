package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.util.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品分类接口
 */
@Slf4j
@RestController
@RequestMapping("category")
@AllArgsConstructor
public class CategoryController extends BaseController {

    private ICategoryService iCategoryService;

    /**
     * 商品分类首页接口，返回分类列表、当前分类信息、下级分类列表
     *
     * @return R
     */
    @GetMapping("index")
    public R index() {
        return R.success(iCategoryService.index());
    }

    /**
     * 根据一级分类id，获取一级分类详情以及二级分类列表
     *
     * @param id 一级分类id
     * @return R
     */
    @GetMapping("content")
    public R content(Long id) {
        return R.success(iCategoryService.content(id));
    }

    /**
     * 根据一级分类id，查询商品分页列表并带上分类详情信息
     *
     * @param cateId 一级分类id
     * @return R
     */
    @GetMapping("firstCategoryGoods")
    public R firstCateGoods(@RequestParam(defaultValue = "0") Long cateId) {
        Page<Goods> page = getPage();
        return R.success(iCategoryService.firstCateGoods(page, cateId));
    }

    /**
     * 根据二级分类id，查询商品分页列表并带上分类详情信息
     *
     * @param cateId 二级分类id
     * @return R
     */
    @GetMapping("secondCategoryGoods")
    public R secondCateGoods(@RequestParam(defaultValue = "0") Long cateId) {
        Page<Goods> page = getPage();
        return R.success(iCategoryService.secondCateGoods(page, cateId));
    }
}
