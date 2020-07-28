package com.wayn.mobile.api.controller;

import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.domain.vo.VanTreeSelectVo;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private ICategoryService iCategoryService;

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
}
