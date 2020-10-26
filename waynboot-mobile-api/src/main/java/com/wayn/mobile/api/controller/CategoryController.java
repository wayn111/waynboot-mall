package com.wayn.mobile.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.vo.VanTreeSelectVo;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("category")
public class CategoryController extends BaseController {

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private IGoodsService iGoodsService;

    @GetMapping("index")
    public R index(@RequestParam(required = false) Long id) {
        R success = R.success();
        List<VanTreeSelectVo> categoryList = iCategoryService.selectL1Category();
        Category currentCategory;
        List<VanTreeSelectVo> subCategoryList;
        if (Objects.isNull(id) && categoryList.size() > 0) {
            currentCategory = iCategoryService.getById(categoryList.get(0).getId());
            subCategoryList = iCategoryService.selectCategoryByPid(currentCategory.getId());
        } else {
            currentCategory = iCategoryService.getById(id);
            subCategoryList = iCategoryService.selectCategoryByPid(id);
        }
        success.add("categoryList", categoryList);
        success.add("currentCategory", currentCategory);
        success.add("subCategoryList", subCategoryList);
        return success;
    }

    @GetMapping("content")
    public R content(Long id) {
        R success = R.success();
        Category currentCategory = iCategoryService.getById(id);
        List<VanTreeSelectVo> subCategoryList = iCategoryService.selectCategoryByPid(id);
        success.add("currentCategory", currentCategory);
        success.add("subCategoryList", subCategoryList);
        return success;
    }

    @GetMapping("firstCategoryGoods")
    public R firstCateGoods(@RequestParam(defaultValue = "0") Long cateId) {
        Page<Goods> page = getPage();
        List<Category> categoryList = iCategoryService.list(new QueryWrapper<Category>().select("id").eq("pid", cateId));
        List<Long> cateList = categoryList.stream().map(Category::getId).collect(Collectors.toList());
        R success = iGoodsService.selectListPageByCateIds(page, cateList);
        success.add("category", iCategoryService.getById(cateId));
        return success;
    }

    @GetMapping("secondCategoryGoods")
    public R secondCateGoods(@RequestParam(defaultValue = "0") Long cateId) {
        Page<Goods> page = getPage();
        List<Long> cateList = Arrays.asList(cateId);
        R success = iGoodsService.selectListPageByCateIds(page, cateList);
        success.add("category", iCategoryService.getById(cateId));
        return success;
    }


}
