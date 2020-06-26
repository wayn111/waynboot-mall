package com.wayn.admin.api.controller.shop;


import com.wayn.admin.api.domain.shop.Category;
import com.wayn.admin.api.service.shop.ICategoryService;
import com.wayn.common.base.BaseController;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 类目表 前端控制器
 * </p>
 *
 * @author jobob
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

}
