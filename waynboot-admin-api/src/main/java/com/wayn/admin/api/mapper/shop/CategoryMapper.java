package com.wayn.admin.api.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.admin.api.domain.shop.Category;

import java.util.List;

/**
 * <p>
 * 类目表 Mapper 接口
 * </p>
 *
 * @author jobob
 * @since 2020-06-26
 */
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectCategoryList(Category category);
}
