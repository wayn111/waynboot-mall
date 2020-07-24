package com.wayn.common.core.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.common.core.domain.shop.Category;

import java.util.List;

/**
 * <p>
 * 类目表 Mapper 接口
 * </p>
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectCategoryList(Category category);
}
