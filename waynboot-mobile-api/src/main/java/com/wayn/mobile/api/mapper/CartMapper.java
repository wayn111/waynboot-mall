package com.wayn.mobile.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.mobile.api.domain.Cart;

/**
 * 购物车商品表 Mapper 接口
 *
 * @author wayn
 * @since 2020-08-03
 */
public interface CartMapper extends BaseMapper<Cart> {

    IPage<Cart> selectCartPageList(Page<Cart> page, Long userId);
}
