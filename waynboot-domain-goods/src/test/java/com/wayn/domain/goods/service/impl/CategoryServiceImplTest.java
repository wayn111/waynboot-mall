package com.wayn.domain.goods.service.impl;

import com.wayn.domain.api.goods.entity.Category;
import com.wayn.domain.api.goods.mapper.CategoryMapper;
import com.wayn.domain.api.goods.response.VanTreeSelectVO;
import com.wayn.domain.api.goods.response.CategoryIndexResponseVO;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CategoryServiceImplTest {

    /**
     * 验证分类首页会复用一级分类的首个分类加载当前分类和子分类。
     */
    @Test
    void indexBuildsCurrentAndSubCategoriesFromFirstLevelCategory() {
        CategoryServiceImpl service = spy(new CategoryServiceImpl(mock(CategoryMapper.class),
                new DirectThreadPoolTaskExecutor()));
        VanTreeSelectVO firstCategory = buildVanTreeSelect(10L);
        Category category = buildCategory(10L);
        List<VanTreeSelectVO> subCategories = List.of(buildVanTreeSelect(11L));
        doReturn(List.of(firstCategory)).when(service).selectL1Category();
        doReturn(category).when(service).getById(10L);
        doReturn(subCategories).when(service).selectCategoryByPid(10L);

        CategoryIndexResponseVO responseVO = service.index();

        assertSame(category, responseVO.getCurrentCategory());
        assertSame(subCategories, responseVO.getSubCategoryList());
        assertEquals(List.of(firstCategory), responseVO.getCategoryList());
    }

    /**
     * 验证一级分类为空时直接返回 null，保持原有“无结果不缓存”的接口语义。
     */
    @Test
    void indexReturnsNullWhenFirstLevelCategoryIsEmpty() {
        CategoryServiceImpl service = spy(new CategoryServiceImpl(mock(CategoryMapper.class),
                new DirectThreadPoolTaskExecutor()));
        doReturn(List.of()).when(service).selectL1Category();

        CategoryIndexResponseVO responseVO = service.index();

        assertNull(responseVO);
        verify(service, never()).getById(org.mockito.ArgumentMatchers.anyLong());
    }

    /**
     * 验证分类内容页只组装当前分类和子分类列表，不写入一级分类列表。
     */
    @Test
    void contentBuildsCurrentAndSubCategories() {
        CategoryServiceImpl service = spy(new CategoryServiceImpl(mock(CategoryMapper.class),
                new DirectThreadPoolTaskExecutor()));
        Category category = buildCategory(20L);
        List<VanTreeSelectVO> subCategories = List.of(buildVanTreeSelect(21L));
        doReturn(category).when(service).getById(20L);
        doReturn(subCategories).when(service).selectCategoryByPid(20L);

        CategoryIndexResponseVO responseVO = service.content(20L);

        assertSame(category, responseVO.getCurrentCategory());
        assertSame(subCategories, responseVO.getSubCategoryList());
        assertNull(responseVO.getCategoryList());
    }

    /**
     * 分类 ID 为空时直接返回空响应，避免把无效 ID 提交到异步查询链路。
     */
    @Test
    void contentReturnsEmptyResponseWhenCategoryIdIsNull() {
        CategoryServiceImpl service = spy(new CategoryServiceImpl(mock(CategoryMapper.class),
                new DirectThreadPoolTaskExecutor()));

        CategoryIndexResponseVO responseVO = service.content(null);

        assertNull(responseVO.getCurrentCategory());
        assertNull(responseVO.getSubCategoryList());
        assertNull(responseVO.getCategoryList());
        verify(service, never()).getById(org.mockito.ArgumentMatchers.any());
        verify(service, never()).selectCategoryByPid(org.mockito.ArgumentMatchers.any());
    }

    /**
     * 分类内容加载异常时保持返回 null。
     * 该接口配置了“结果为 null 不缓存”，失败路径必须避免把空响应写入缓存。
     */
    @Test
    void contentReturnsNullWhenAsyncLoadFails() {
        CategoryServiceImpl service = spy(new CategoryServiceImpl(mock(CategoryMapper.class),
                new DirectThreadPoolTaskExecutor()));
        doReturn(List.of()).when(service).selectCategoryByPid(30L);
        org.mockito.Mockito.doThrow(new IllegalStateException("db down")).when(service).getById(30L);

        CategoryIndexResponseVO responseVO = service.content(30L);

        assertNull(responseVO);
    }

    /**
     * 构建分类实体。
     *
     * @param categoryId 分类 ID
     * @return 分类实体
     */
    private Category buildCategory(Long categoryId) {
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }

    /**
     * 构建 Vant 树选择分类对象。
     *
     * @param categoryId 分类 ID
     * @return Vant 树选择分类对象
     */
    private VanTreeSelectVO buildVanTreeSelect(Long categoryId) {
        VanTreeSelectVO vanTreeSelectVO = new VanTreeSelectVO();
        vanTreeSelectVO.setId(categoryId);
        return vanTreeSelectVO;
    }

    /**
     * 测试用同步线程池。
     * 分类服务生产环境异步查询当前分类和子分类，单测中同步执行可以稳定验证组装逻辑。
     */
    private static final class DirectThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        /**
         * 直接在当前线程执行 Callable。
         *
         * @param task 待执行任务
         * @param <T> 任务返回类型
         * @return 已完成的 Future
         */
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            FutureTask<T> futureTask = new FutureTask<>(task);
            futureTask.run();
            return futureTask;
        }

        /**
         * 直接在当前线程执行 Runnable。
         *
         * @param task 待执行任务
         * @return 已完成的 Future
         */
        @Override
        public Future<?> submit(Runnable task) {
            FutureTask<?> futureTask = new FutureTask<>(task, null);
            futureTask.run();
            return futureTask;
        }
    }
}
