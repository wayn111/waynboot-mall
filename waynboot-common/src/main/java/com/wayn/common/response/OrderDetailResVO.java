package com.wayn.common.response;

import com.wayn.common.core.vo.MemberVO;
import com.wayn.common.core.vo.OrderGoodsVO;
import com.wayn.common.core.vo.OrderVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2024/4/29 16:51
 */
@Data
public class OrderDetailResVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单详情
     */
    private OrderVO order;

    /**
     * 订单关联商品列表
     */
    private List<OrderGoodsVO> orderGoods;

    /**
     * 用户信息
     */
    private MemberVO user;
}
