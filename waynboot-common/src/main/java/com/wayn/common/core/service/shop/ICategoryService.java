package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.entity.shop.Category;
import com.wayn.common.core.entity.shop.Goods;
import com.wayn.common.response.CategoryGoodsResponseVO;
import com.wayn.common.response.CategoryIndexResponseVO;
import com.wayn.common.core.vo.VanTreeSelectVO;

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
