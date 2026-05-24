package com.wayn.domain.trade.service.impl;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.mapper.AdminOrderMapper;
import com.wayn.domain.api.trade.service.IOrderService;
import com.wayn.domain.trade.support.admin.order.AdminOrderQuerySupport;
import com.wayn.domain.trade.support.admin.order.AdminOrderRefundSupport;
import com.wayn.domain.trade.support.admin.order.AdminOrderShipmentSupport;
import com.wayn.domain.api.trade.request.OrderManagerReqVO;
import com.wayn.domain.api.trade.request.OrderRefundReqVO;
import com.wayn.domain.api.trade.request.ShipRequestVO;
import com.wayn.domain.api.trade.response.OrderDetailResVO;
import com.wayn.domain.api.trade.response.OrderManagerResVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * 管理端订单服务外观层。
 * 对外继续暴露 `IOrderService` 原有能力，内部把查询、退款、发货分别委托给独立支撑服务处理。
 */
@Service
@AllArgsConstructor
public class OrderServiceImpl extends ServiceImpl<AdminOrderMapper, Order> implements IOrderService {

    private final AdminOrderQuerySupport adminOrderQuerySupport;
    private final AdminOrderRefundSupport adminOrderRefundSupport;
    private final AdminOrderShipmentSupport adminOrderShipmentSupport;

    /**
     * 委托查询管理端订单分页。
     *
     * @param page 分页参数
     * @param order 查询条件
     * @return 订单分页结果
     */
    @Override
    public IPage<OrderManagerResVO> listPage(IPage<Order> page, OrderManagerReqVO order) {
        return adminOrderQuerySupport.listPage(page, order);
    }

    /**
     * 委托执行订单退款。
     *
     * @param reqVO 退款请求
     * @throws UnsupportedEncodingException 编码异常
     * @throws WxPayException 微信退款异常
     * @throws AlipayApiException 支付宝退款异常
     */
    @Override
    public void refund(OrderRefundReqVO reqVO) throws UnsupportedEncodingException, WxPayException, AlipayApiException {
        adminOrderRefundSupport.refund(reqVO);
    }

    /**
     * 委托执行订单发货。
     *
     * @param shipVO 发货请求
     */
    @Override
    public void ship(ShipRequestVO shipVO) {
        adminOrderShipmentSupport.ship(shipVO);
    }

    /**
     * 委托查询订单详情。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    @Override
    public OrderDetailResVO detail(Long orderId) {
        return adminOrderQuerySupport.detail(orderId);
    }
}
