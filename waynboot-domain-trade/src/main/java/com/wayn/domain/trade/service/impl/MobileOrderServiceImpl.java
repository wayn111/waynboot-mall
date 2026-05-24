package com.wayn.domain.trade.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.api.trade.mapper.OrderMapper;
import com.wayn.domain.api.trade.service.IMobileOrderService;
import com.wayn.domain.trade.support.order.OrderLifecycleSupport;
import com.wayn.domain.trade.support.order.OrderQuerySupport;
import com.wayn.domain.trade.support.order.OrderSubmitSupport;
import com.wayn.domain.trade.support.order.OrderValidationSupport;
import com.wayn.domain.api.trade.response.OrderDetailVO;
import com.wayn.domain.api.trade.request.OrderCommitReqVO;
import com.wayn.domain.api.trade.response.OrderListResVO;
import com.wayn.domain.api.trade.response.OrderStatusCountResVO;
import com.wayn.domain.api.trade.response.SubmitOrderResVO;
import com.wayn.message.core.dto.OrderDTO;
import com.wayn.util.enums.ReturnCodeEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * 移动端订单服务外观层。
 * 保留原有 Service 接口，内部只负责把请求分发到查询、下单和生命周期支撑服务，
 * 避免入口层继续堆叠交易编排细节。
 */
@Service
@AllArgsConstructor
public class MobileOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IMobileOrderService {

    private final OrderQuerySupport orderQuerySupport;
    private final OrderSubmitSupport orderSubmitSupport;
    private final OrderLifecycleSupport orderLifecycleSupport;
    private final OrderValidationSupport orderValidationSupport;

    /**
     * 委托查询移动端订单列表。
     *
     * @param page 分页参数
     * @param showType 展示类型
     * @param userId 用户 ID
     * @return 订单列表结果
     */
    @Override
    public OrderListResVO selectListPage(IPage<Order> page, Integer showType, Long userId) {
        return orderQuerySupport.selectListPage(page, showType, userId);
    }

    /**
     * 委托统计订单状态数量。
     *
     * @param userId 用户 ID
     * @return 状态统计
     */
    @Override
    public OrderStatusCountResVO statusCount(Long userId) {
        return orderQuerySupport.statusCount(userId);
    }

    /**
     * 委托查询订单详情。
     *
     * @param orderSn 订单号
     * @return 订单详情
     */
    @Override
    public OrderDetailVO getOrderDetailByOrderSn(String orderSn) {
        return orderQuerySupport.getOrderDetailByOrderSn(orderSn);
    }

    /**
     * 委托异步下单。
     *
     * @param orderCommitReqVO 下单请求
     * @param userId 用户 ID
     * @return 下单结果
     */
    @Override
    public SubmitOrderResVO asyncSubmit(OrderCommitReqVO orderCommitReqVO, Long userId) {
        return orderSubmitSupport.asyncSubmit(orderCommitReqVO, userId);
    }

    /**
     * 委托消费端真正落单。
     *
     * @param orderDTO 下单 DTO
     * @throws UnsupportedEncodingException 编码异常
     */
    @Override
    public void submit(OrderDTO orderDTO) throws UnsupportedEncodingException {
        orderSubmitSupport.submit(orderDTO);
    }

    /**
     * 委托查询异步下单结果。
     *
     * @param orderSn 订单号
     * @return 下单结果
     */
    @Override
    public String searchResult(String orderSn) {
        return orderQuerySupport.searchResult(orderSn);
    }

    /**
     * 委托发起退款申请。
     *
     * @param orderId 订单 ID
     */
    @Override
    public void refund(Long orderId) {
        orderLifecycleSupport.refund(orderId);
    }

    /**
     * 委托取消订单。
     *
     * @param orderId 订单 ID
     */
    @Override
    public void cancel(Long orderId) {
        orderLifecycleSupport.cancel(orderId);
    }

    /**
     * 委托删除订单。
     *
     * @param orderId 订单 ID
     */
    @Override
    public void delete(Long orderId) {
        orderLifecycleSupport.delete(orderId);
    }

    /**
     * 委托确认收货。
     *
     * @param orderId 订单 ID
     */
    @Override
    public void confirm(Long orderId) {
        orderLifecycleSupport.confirm(orderId);
    }

    /**
     * 委托校验订单操作权限。
     *
     * @param order 订单对象
     * @return 校验结果码
     */
    @Override
    public ReturnCodeEnum checkOrderOperator(Order order) {
        return orderValidationSupport.checkOrderOperator(order);
    }
}
