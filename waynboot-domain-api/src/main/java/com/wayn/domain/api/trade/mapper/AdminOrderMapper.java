package com.wayn.domain.api.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.request.OrderManagerReqVO;
import com.wayn.domain.api.trade.response.OrderManagerResVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 类目表 Mapper 接口
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface AdminOrderMapper extends BaseMapper<Order> {

    IPage<OrderManagerResVO> selectOrderListPage(IPage<Order> page, OrderManagerReqVO order);

    /**
     * 按已支付订单明细聚合热销商品。
     * <p>
     * 看板热销榜必须来自真实订单商品数量，不能直接复用商品表维护的展示销量字段。
     *
     * @param paidStatuses 已支付生命周期订单状态
     * @param limit        返回数量
     * @return 商品 ID 与聚合销量
     */
    List<Map<String, Object>> selectTopGoodsByPaidOrders(@Param("paidStatuses") List<Short> paidStatuses,
                                                         @Param("limit") Integer limit);
}
