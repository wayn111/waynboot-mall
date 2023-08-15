package com.wayn.admin.api.controller.shop;


import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 商城类目管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("/shop/category")
public class CategoryController extends BaseController {

    private ICategoryService iCategoryService;

    @PreAuthorize("@ss.hasPermi('shop:category:list')")
    @GetMapping("/list")
    public R list(Category category) {
        return R.success().add("data", iCategoryService.list(category));
    }

    @PreAuthorize("@ss.hasPermi('shop:category:add')")
    @PostMapping
    public R addCategory(@Validated @RequestBody Category category) {
        category.setCreateTime(new Date());
        return R.result(iCategoryService.save(category));
    }

    @PreAuthorize("@ss.hasPermi('shop:category:update')")
    @PutMapping
    public R updateCategory(@Validated @RequestBody Category category) {
        category.setUpdateTime(new Date());
        return R.result(iCategoryService.updateById(category));
    }

    @PreAuthorize("@ss.hasPermi('shop:category:info')")
    @GetMapping("{categoryId}")
    public R getCategory(@PathVariable Long categoryId) {
        return R.success().add("data", iCategoryService.getById(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('shop:category:delete')")
    @DeleteMapping("{categoryIds}")
    public R deleteCategory(@PathVariable List<Long> categoryIds) {
        return R.result(iCategoryService.removeByIds(categoryIds));
    }
}
