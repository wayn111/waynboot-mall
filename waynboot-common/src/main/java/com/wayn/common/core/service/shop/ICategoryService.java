package com.wayn.common.core.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.domain.vo.VanTreeSelectVo;

import java.util.List;

/**
 * <p>
 * 类目表 服务类
 * </p>
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

    List<VanTreeSelectVo> selectL1Category();

    List<VanTreeSelectVo> selectCategoryByPid(Long id);
}
