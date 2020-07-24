package com.wayn.common.core.service.shop.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.shop.Category;
import com.wayn.common.core.mapper.shop.CategoryMapper;
import com.wayn.common.core.service.shop.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 类目表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-06-26
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    @Override
    public List<Category> list(Category category) {
        return categoryMapper.selectCategoryList(category);
    }
}
