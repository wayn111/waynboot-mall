package com.wayn.domain.trade.support.order.submit.chain;

import com.wayn.domain.api.trade.entity.Order;
import com.wayn.domain.trade.support.order.OrderSubmitContext;
import com.wayn.message.core.dto.OrderDTO;

/**
 * 下单责任链上下文。
 * 贯穿订单查重、上下文计算、库存扣减、订单组装和持久化步骤，承载链路中间产物和中断状态。
 */
public final class OrderSubmitChainContext {

    private final OrderDTO orderDTO;
    private final OrderSubmitMode mode;
    private final OrderSubmitContextFactory contextFactory;
    private OrderSubmitContext submitContext;
    private Order order;
    private boolean stopped;
    private boolean existingOrder;
    private String stopReason;

    /**
     * 构造下单责任链上下文。
     *
     * @param orderDTO 下单 DTO
     * @param mode 责任链执行模式
     * @param contextFactory 下单上下文工厂
     */
    private OrderSubmitChainContext(OrderDTO orderDTO, OrderSubmitMode mode, OrderSubmitContextFactory contextFactory) {
        this.orderDTO = orderDTO;
        this.mode = mode;
        this.contextFactory = contextFactory;
    }

    /**
     * 创建单笔订单完整提交上下文。
     *
     * @param orderDTO 下单 DTO
     * @param contextFactory 下单上下文工厂
     * @return 单笔提交责任链上下文
     */
    public static OrderSubmitChainContext single(OrderDTO orderDTO, OrderSubmitContextFactory contextFactory) {
        return new OrderSubmitChainContext(orderDTO, OrderSubmitMode.SINGLE, contextFactory);
    }

    /**
     * 获取下单 DTO。
     *
     * @return 下单 DTO
     */
    public OrderDTO getOrderDTO() {
        return orderDTO;
    }

    /**
     * 获取责任链执行模式。
     *
     * @return 执行模式
     */
    public OrderSubmitMode getMode() {
        return mode;
    }

    /**
     * 构建并保存下单上下文。
     *
     * @return 下单上下文
     */
    public OrderSubmitContext buildSubmitContext() {
        // 上下文构建延迟到查重之后执行，重复订单不会再次查询购物车、校验优惠券或计算金额。
        submitContext = contextFactory.create(orderDTO);
        return submitContext;
    }

    /**
     * 获取下单上下文。
     *
     * @return 下单上下文
     */
    public OrderSubmitContext getSubmitContext() {
        return submitContext;
    }

    /**
     * 保存责任链组装出的订单对象。
     *
     * @param order 待落库订单对象
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    /**
     * 获取责任链组装出的订单对象。
     *
     * @return 待落库订单对象
     */
    public Order getOrder() {
        return order;
    }

    /**
     * 标记订单已存在。
     * 重复消费命中已落库订单时使用该标记，让外层把该订单视为成功处理。
     */
    public void markExistingOrder() {
        // 外层可通过该状态区分“重复消息已处理”和“业务失败”，避免重复副作用。
        this.existingOrder = true;
    }

    /**
     * 判断是否命中已存在订单。
     *
     * @return true=订单已存在
     */
    public boolean isExistingOrder() {
        return existingOrder;
    }

    /**
     * 中断责任链后续步骤。
     *
     * @param stopReason 中断原因
     */
    public void stop(String stopReason) {
        // stopReason 只用于链路诊断；业务结果仍由调用方根据具体状态决定如何返回。
        this.stopped = true;
        this.stopReason = stopReason;
    }

    /**
     * 判断责任链是否已中断。
     *
     * @return true=已中断
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * 获取责任链中断原因。
     *
     * @return 中断原因
     */
    public String getStopReason() {
        return stopReason;
    }
}
