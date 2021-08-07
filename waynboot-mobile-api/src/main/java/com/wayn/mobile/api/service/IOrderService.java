package com.wayn.mobile.api.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.message.core.messsage.OrderDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface IOrderService extends IService<Order> {

    /**
     * 添加订单记录
     *
     * @param orderDTO 订单DTO
     * @return R
     */
    R submit(OrderDTO orderDTO);


    /**
     * 异步下单
     *
     * @param orderVO 订单VO
     */
    R asyncSubmit(OrderVO orderVO);

    /**
     * 微信H5支付
     *
     * @param orderSn 订单编号
     * @param request 请求
     * @return r
     */
    R h5pay(String orderSn, Integer payType, HttpServletRequest request);

    /**
     * 付款订单的预支付会话标识
     * <p>
     * 1. 检测当前订单是否能够付款
     * 2. 微信商户平台返回支付订单ID
     * 3. 设置订单付款状态
     *
     * @param orderSn 订单编号
     * @param request 请求
     * @return r
     */
    R prepay(String orderSn, Integer payType, HttpServletRequest request);

    /**
     * 获取订单列表
     *
     * @param page     分页对象
     * @param showType 展示类型
     * @return r
     */
    R selectListPage(IPage<Order> page, Integer showType);

    /**
     * 支付成功回调处理
     *
     * @param request  请求
     * @param response 响应
     * @return r
     */
    void wxPayNotify(HttpServletRequest request, HttpServletResponse response);

    void aliPayNotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException;

    /**
     * 测试支付成功回调
     *
     * @param orderSn 订单编号
     * @return r
     */
    R searchResult(String orderSn);

    /**
     * 取消订单
     * <p>
     * 1. 检测当前订单是否能够取消；
     * 2. 设置订单取消状态；
     * 3. 商品货品库存恢复；
     * 4. 返还优惠券；
     *
     * @param orderId 订单ID
     * @return r
     */
    R cancel(Long orderId);


    /**
     * 订单退款
     * <p>
     * 1. 检测当前订单是否可以退款；
     * 2. 更改订单状态为已退款。
     *
     * @param orderId 订单ID
     * @return r
     */
    R refund(Long orderId);

    /**
     * 删除订单
     * <p>
     * 1. 检测当前订单是否可以删除；
     * 2. 删除订单。
     *
     * @param orderId 订单ID
     * @return r
     */
    R delete(Long orderId);

    /**
     * 确认订单
     * <p>
     * 1. 检测当前订单是否可以删除；
     * 2. 更改订单状态为已收货。
     *
     * @param orderId 订单ID
     * @return r
     */
    R confirm(Long orderId);

    /**
     * 查询用户订单各状态数量（包含待支付订单数量、代发货订单数量、待收货订单数量、待评价订单数量）
     *
     * @return r
     */
    R statusCount();

}
