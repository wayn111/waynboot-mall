package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.domain.shop.Goods;
import com.wayn.common.core.domain.shop.vo.CategoryGoodsResponseVO;
import com.wayn.common.core.domain.shop.vo.CategoryIndexResponseVO;
import com.wayn.common.core.domain.vo.VanTreeSelectVO;
import com.wayn.common.core.mapper.shop.CategoryMapper;
import com.wayn.common.core.service.shop.ICategoryService;
import com.wayn.common.core.service.shop.IGoodsService;
import com.wayn.common.util.spring.SpringContextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

/**
 * 类目表 服务实现类
 *
 * @author wayn
 * @since 2020-06-26
 */
@Slf4j
@Service
@CacheConfig(keyGenerator = "cacheKeyGenerator")
@AllArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private CategoryMapper categoryMapper;
    private ThreadPoolTaskExecutor commonThreadPoolTaskExecutor;

    @Override
    public List<Category> list(Category category) {
        return categoryMapper.selectCategoryList(category);
    }

    @Override
    public List<VanTreeSelectVO> selectL1Category() {
        List<Category> categoryList = list(new QueryWrapper<Category>().eq("level", "L1").orderByAsc("sort"));
        return categoryList.stream().map(VanTreeSelectVO::new).collect(Collectors.toList());
    }

    @Override
    public List<VanTreeSelectVO> selectCategoryByPid(Long id) {
        List<Category> categoryList = list(new QueryWrapper<Category>().eq("pid", id).orderByAsc("sort"));
        return categoryList.stream().map(VanTreeSelectVO::new).collect(Collectors.toList());
    }

    @Cacheable(value = "category_index_cache#300", unless = "#result == null")
    @Override
    public CategoryIndexResponseVO index() {
        CategoryIndexResponseVO responseVO = new CategoryIndexResponseVO();
        List<VanTreeSelectVO> categoryList = this.selectL1Category();
        Callable<Category> currentCategoryCallable = () -> this.getById(categoryList.get(0).getId());
        Callable<List<VanTreeSelectVO>> subCategoryListCallable = () -> this.selectCategoryByPid(categoryList.get(0).getId());
        FutureTask<Category> currentCategoryTask = new FutureTask<>(currentCategoryCallable);
        FutureTask<List<VanTreeSelectVO>> subCategoryListTask = new FutureTask<>(subCategoryListCallable);
        commonThreadPoolTaskExecutor.submit(currentCategoryTask);
        commonThreadPoolTaskExecutor.submit(subCategoryListTask);

        try {
            Category category = currentCategoryTask.get();
            List<VanTreeSelectVO> vanTreeSelectVOS = subCategoryListTask.get();
            responseVO.setCategoryList(categoryList);
            responseVO.setCurrentCategory(category);
            responseVO.setSubCategoryList(vanTreeSelectVOS);
            return responseVO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Cacheable(value = "category_content_cache#300", unless = "#result == null")
    @Override
    public CategoryIndexResponseVO content(Long id) {
        CategoryIndexResponseVO responseVO = new CategoryIndexResponseVO();
        Callable<Category> currentCategoryCallable = () -> this.getById(id);
        Callable<List<VanTreeSelectVO>> subCategoryListCallable = () -> this.selectCategoryByPid(id);
        FutureTask<Category> currentCategoryTask = new FutureTask<>(currentCategoryCallable);
        FutureTask<List<VanTreeSelectVO>> subCategoryListTask = new FutureTask<>(subCategoryListCallable);
        commonThreadPoolTaskExecutor.submit(currentCategoryTask);
        commonThreadPoolTaskExecutor.submit(subCategoryListTask);
        try {
            Category category = currentCategoryTask.get();
            List<VanTreeSelectVO> vanTreeSelectVOS = subCategoryListTask.get();
            responseVO.setCurrentCategory(category);
            responseVO.setSubCategoryList(vanTreeSelectVOS);
            return responseVO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Cacheable(value = "category_first_cache#300", unless = "#result == null")
    @Override
    public CategoryGoodsResponseVO firstCateGoods(Page<Goods> page, Long cateId) {
        CategoryGoodsResponseVO responseVO = new CategoryGoodsResponseVO();
        List<Category> categoryList = this.list(new QueryWrapper<Category>()
                .select("id")
                .eq("pid", cateId));
        List<Long> cateList = categoryList.stream().map(Category::getId).collect(Collectors.toList());
        return getCategoryGoodsResponseVO(cateId, responseVO, page, cateList);
    }

    @Cacheable(value = "category_second_cache#300", unless = "#result == null")
    @Override
    public CategoryGoodsResponseVO secondCateGoods(Page<Goods> page, Long cateId) {
        CategoryGoodsResponseVO responseVO = new CategoryGoodsResponseVO();
        List<Long> cateList = List.of(cateId);
        return getCategoryGoodsResponseVO(cateId, responseVO, page, cateList);
    }

    @NotNull
    private CategoryGoodsResponseVO getCategoryGoodsResponseVO(Long cateId, CategoryGoodsResponseVO responseVO, Page<Goods> page, List<Long> cateList) {
        IGoodsService iGoodsService = SpringContextUtil.getBean(IGoodsService.class);
        List<Goods> goods = iGoodsService.selectListPageByCateIds(page, cateList);
        Category category = this.getById(cateId);
        responseVO.setCategory(category);
        responseVO.setGoods(goods);
        return responseVO;
    }
}
