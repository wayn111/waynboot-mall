package com.wayn.domain.api.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wayn.domain.api.goods.entity.Category;

import java.util.List;

/**
 * 类目表 Mapper 接口
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface CategoryMapper extends BaseMapper<Category> {

    List<Category> selectCategoryList(Category category);
}
