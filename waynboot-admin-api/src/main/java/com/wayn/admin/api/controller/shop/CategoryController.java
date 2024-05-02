package com.wayn.admin.api.controller.shop;


import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.entity.shop.Category;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.util.util.R;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 商城分类管理
 *
 * @author wayn
 * @since 2020-07-06
 */
@RestController
@AllArgsConstructor
@RequestMapping("/shop/category")
public class CategoryController extends BaseController {

    private ICategoryService iCategoryService;

    /**
     * 分类列表
     *
     * @param category
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:category:list')")
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        return R.success(iCategoryService.list(category));
    }

    /**
     * 添加分类
     *
     * @param category
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:category:add')")
    @PostMapping
    public R<Boolean> addCategory(@Validated @RequestBody Category category) {
        category.setCreateTime(new Date());
        return R.result(iCategoryService.save(category));
    }

    /**
     * 修改分类
     *
     * @param category
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:category:update')")
    @PutMapping
    public R<Boolean> updateCategory(@Validated @RequestBody Category category) {
        category.setUpdateTime(new Date());
        return R.result(iCategoryService.updateById(category));
    }

    /**
     * 获取分类信息
     *
     * @param categoryId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:category:info')")
    @GetMapping("{categoryId}")
    public R<Category> getCategory(@PathVariable Long categoryId) {
        return R.success(iCategoryService.getById(categoryId));
    }

    /**
     * 删除分类
     *
     * @param categoryIds
     * @return
     */
    @PreAuthorize("@ss.hasPermi('shop:category:delete')")
    @DeleteMapping("{categoryIds}")
    public R<Boolean> deleteCategory(@PathVariable List<Long> categoryIds) {
        return R.result(iCategoryService.removeByIds(categoryIds));
    }
}
