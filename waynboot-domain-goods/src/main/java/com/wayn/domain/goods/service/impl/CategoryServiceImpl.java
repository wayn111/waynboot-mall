package com.wayn.domain.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.goods.entity.Category;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.mapper.CategoryMapper;
import com.wayn.domain.api.goods.service.ICategoryService;
import com.wayn.domain.api.goods.service.IGoodsService;
import com.wayn.domain.api.goods.response.VanTreeSelectVO;
import com.wayn.domain.api.goods.response.CategoryGoodsResponseVO;
import com.wayn.domain.api.goods.response.CategoryIndexResponseVO;
import com.wayn.util.util.spring.SpringContextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
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
        List<VanTreeSelectVO> categoryList = this.selectL1Category();
        if (CollectionUtils.isEmpty(categoryList)) {
            log.warn("一级分类为空，无法构建分类首页数据");
            return null;
        }
        CategoryContent categoryContent = loadCategoryContent(categoryList.get(0).getId());
        CategoryIndexResponseVO responseVO = buildCategoryIndexResponse(categoryContent);
        if (responseVO == null) {
            return null;
        }
        responseVO.setCategoryList(categoryList);
        return responseVO;
    }

    @Cacheable(value = "category_content_cache#300", unless = "#result == null")
    @Override
    public CategoryIndexResponseVO content(Long id) {
        if (id == null) {
            // 空 ID 属于无效前端请求，直接返回空结构，避免进入异步查询后被宽泛异常吞掉根因。
            return new CategoryIndexResponseVO();
        }
        return buildCategoryIndexResponse(loadCategoryContent(id));
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

    /**
     * 并行加载当前分类和子分类列表。
     * 分类首页和分类内容页都需要同一组数据，统一收口异步编排，避免两个接口分别维护 FutureTask 模板。
     *
     * @param categoryId 当前分类 ID
     * @return 分类内容数据；加载失败时返回 null，保持失败结果不缓存的接口语义
     */
    private CategoryContent loadCategoryContent(Long categoryId) {
        Future<Category> currentCategoryTask = submitAsync(() -> this.getById(categoryId));
        Future<List<VanTreeSelectVO>> subCategoryListTask = submitAsync(() -> this.selectCategoryByPid(categoryId));
        try {
            return new CategoryContent(currentCategoryTask.get(), subCategoryListTask.get());
        } catch (Exception e) {
            log.error("加载分类内容失败, categoryId={}", categoryId, e);
            return null;
        }
    }

    /**
     * 构建分类首页/内容页响应。
     * 响应组装统一收口，避免 index 与 content 对正常内容的字段映射口径分叉。
     *
     * @param categoryContent 分类内容聚合结果
     * @return 分类响应
     */
    private CategoryIndexResponseVO buildCategoryIndexResponse(CategoryContent categoryContent) {
        if (categoryContent == null) {
            // 真实查询失败时继续返回 null，配合 @Cacheable unless 避免把异常瞬间的空数据缓存给后续请求。
            return null;
        }
        CategoryIndexResponseVO responseVO = new CategoryIndexResponseVO();
        responseVO.setCurrentCategory(categoryContent.currentCategory());
        responseVO.setSubCategoryList(categoryContent.subCategoryList());
        return responseVO;
    }

    /**
     * 提交异步分类查询任务。
     *
     * @param callable 分类查询逻辑
     * @param <T> 查询结果类型
     * @return 已提交的异步任务
     */
    private <T> Future<T> submitAsync(Callable<T> callable) {
        return commonThreadPoolTaskExecutor.submit(callable);
    }

    /**
     * 分类内容聚合结果。
     * 用 record 承载两个异步任务的返回值，避免用临时变量在多个接口中重复拼装。
     *
     * @param currentCategory 当前分类
     * @param subCategoryList 子分类列表
     */
    private record CategoryContent(Category currentCategory, List<VanTreeSelectVO> subCategoryList) {
    }
}
