package com.wayn.domain.api.goods.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.domain.api.goods.entity.Category;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.response.VanTreeSelectVO;
import com.wayn.domain.api.goods.response.CategoryGoodsResponseVO;
import com.wayn.domain.api.goods.response.CategoryIndexResponseVO;

import java.util.List;

/**
 * 类目表 服务类
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface ICategoryService extends IService<Category> {
    /**
     * 查询分类列表
     *
     * @param category 查询参数
     * @return 分类列表
     */
    List<Category> list(Category category);

    List<VanTreeSelectVO> selectL1Category();

    List<VanTreeSelectVO> selectCategoryByPid(Long id);

    CategoryIndexResponseVO index();

    CategoryIndexResponseVO content(Long id);

    CategoryGoodsResponseVO firstCateGoods(Page<Goods> page, Long pid);

    CategoryGoodsResponseVO secondCateGoods(Page<Goods> page, Long cateId);
}
