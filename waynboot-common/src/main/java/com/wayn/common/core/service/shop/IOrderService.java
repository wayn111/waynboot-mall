package com.wayn.common.core.service.shop;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.wayn.common.core.entity.shop.Order;
import com.wayn.common.request.OrderManagerReqVO;
import com.wayn.common.request.OrderRefundReqVO;
import com.wayn.common.request.ShipRequestVO;
import com.wayn.common.response.OrderDetailResVO;
import com.wayn.common.response.OrderManagerResVO;

import java.io.UnsupportedEncodingException;

/**
 * 类目表 服务类
 *
 * @author wayn
 * @since 2020-06-26
 */
public interface IOrderService extends IService<Order> {

    /**
     * 获取订单列表
     *
     * @param page  分页对象
     * @param order 订单对象
     * @return r
     */
    IPage<OrderManagerResVO> listPage(IPage<Order> page, OrderManagerReqVO order);

    /**
     * 订单退款
     * <p>
     * 1. 检测当前订单是否能够退款;
     * 2. 微信退款操作;
     * 3. 设置订单退款确认状态；
     * 4. 订单商品库存回库。
     * <p>
     * @param reqVO
     */
    void refund(OrderRefundReqVO reqVO) throws UnsupportedEncodingException, WxPayException, AlipayApiException;

    /**
     * 发货
     * 1. 检测当前订单是否能够发货
     * 2. 设置订单发货状态
     *
     * @param shipVO 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
     * @return r
     */
    void ship(ShipRequestVO shipVO) throws UnsupportedEncodingException;

    /**
     * 获取订单详情（包含订单信息，订单商品信息，用户信息）
     *
     * @param orderId
     * @return OrderDetailResVO
     */
    OrderDetailResVO detail(Long orderId);
}
