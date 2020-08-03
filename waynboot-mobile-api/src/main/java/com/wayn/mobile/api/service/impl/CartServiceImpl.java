package com.wayn.mobile.api.service.impl;

import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.mapper.CartMapper;
import com.wayn.mobile.api.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车商品表 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-08-03
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

}
