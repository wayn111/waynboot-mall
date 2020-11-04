package com.wayn.admin.api.controller.shop;


import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 类目表 前端控制器
 * </p>
 *
 * @author wayn
 * @since 2020-06-26
 */
@RestController
@RequestMapping("/shop/category")
public class CategoryController extends BaseController {

    @Autowired
    private ICategoryService iCategoryService;

    @GetMapping("/list")
    public R list(Category category) {
        return R.success().add("data", iCategoryService.list(category));
    }

    @PostMapping
    public R addCategory(@Validated @RequestBody Category category) {
        category.setCreateTime(new Date());
        return R.result(iCategoryService.save(category));
    }

    @PutMapping
    public R updateCategory(@Validated @RequestBody Category category) {
        category.setUpdateTime(new Date());
        return R.result(iCategoryService.updateById(category));
    }

    @GetMapping("{categoryId}")
    public R getCategory(@PathVariable Long categoryId) {
        return R.success().add("data", iCategoryService.getById(categoryId));
    }

    @DeleteMapping("{categoryIds}")
    public R deleteCategory(@PathVariable List<Long> categoryIds) {
        return R.result(iCategoryService.removeByIds(categoryIds));
    }
}
