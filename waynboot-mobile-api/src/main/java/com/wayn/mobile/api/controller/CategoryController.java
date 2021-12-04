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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("category")
@AllArgsConstructor
public class CategoryController extends BaseController {

    private ICategoryService iCategoryService;
    private IGoodsService iGoodsService;
    private ThreadPoolTaskExecutor categoryThreadPoolTaskExecutor;

    @GetMapping("index")
    public R index(@RequestParam(required = false) Long id) {
        R success = R.success();
        List<VanTreeSelectVo> categoryList = iCategoryService.selectL1Category();
        Callable<Category> currentCategoryCallable;
        Callable<List<VanTreeSelectVo>> subCategoryListCallable;
        if (Objects.isNull(id) && CollectionUtils.isNotEmpty(categoryList)) {
            currentCategoryCallable = () -> iCategoryService.getById(categoryList.get(0).getId());
            subCategoryListCallable = () -> iCategoryService.selectCategoryByPid(categoryList.get(0).getId());
        } else {
            currentCategoryCallable = () -> iCategoryService.getById(id);
            subCategoryListCallable = () -> iCategoryService.selectCategoryByPid(id);
        }
        FutureTask<Category> currentCategoryTask = new FutureTask<>(currentCategoryCallable);
        FutureTask<List<VanTreeSelectVo>> subCategoryListTask = new FutureTask<>(subCategoryListCallable);
        categoryThreadPoolTaskExecutor.submit(currentCategoryTask);
        categoryThreadPoolTaskExecutor.submit(subCategoryListTask);
        try {
            success.add("categoryList", categoryList);
            success.add("currentCategory", currentCategoryTask.get());
            success.add("subCategoryList", subCategoryListTask.get());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return success;
    }

    @GetMapping("content")
    public R content(Long id) {
        long begin = System.currentTimeMillis();
        R success = R.success();
        Callable<Category> currentCategoryCallable = () -> iCategoryService.getById(id);
        Callable<List<VanTreeSelectVo>> subCategoryListCallable = () -> iCategoryService.selectCategoryByPid(id);
        FutureTask<Category> currentCategoryTask = new FutureTask<>(currentCategoryCallable);
        FutureTask<List<VanTreeSelectVo>> subCategoryListTask = new FutureTask<>(subCategoryListCallable);
        categoryThreadPoolTaskExecutor.submit(currentCategoryTask);
        categoryThreadPoolTaskExecutor.submit(subCategoryListTask);
        try {
            success.add("currentCategory", currentCategoryTask.get());
            success.add("subCategoryList", subCategoryListTask.get());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        long end = System.currentTimeMillis();
        log.info("content time:{}", end - begin);
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
        List<Long> cateList = List.of(cateId);
        R success = iGoodsService.selectListPageByCateIds(page, cateList);
        success.add("category", iCategoryService.getById(cateId));
        return success;
    }


}
