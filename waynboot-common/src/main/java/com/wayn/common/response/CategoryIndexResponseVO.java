package com.wayn.common.response;

import com.wayn.common.core.entity.shop.Category;
import com.wayn.common.core.vo.VanTreeSelectVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2023/11/6 22:19
 */
@Data
public class CategoryIndexResponseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -7580503521421359029L;

    /**
     * 当前分类信息
     */
    private Category currentCategory;

    /**
     * 一级分类列表
     */
    private List<VanTreeSelectVO> categoryList;

    /**
     * 二级分类列表
     */
    private List<VanTreeSelectVO> subCategoryList;
}
