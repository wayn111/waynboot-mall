package com.wayn.admin.api.service.shop;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.admin.api.domain.shop.Category;

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
}
