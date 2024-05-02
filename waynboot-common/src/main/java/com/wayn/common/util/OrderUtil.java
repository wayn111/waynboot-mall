package com.wayn.common.util;


import com.wayn.common.core.entity.shop.Order;
import com.wayn.util.enums.OrderStatusEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * 当101用户未付款时，此时用户可以进行的操作是取消或者付款
 * 当201支付完成而商家未发货时，此时用户可以退款
 * 当301商家已发货时，此时用户可以有确认收货
 * 当401用户确认收货以后，此时用户可以进行的操作是退货、删除、去评价或者再次购买
 * 当402系统自动确认收货以后，此时用户可以删除、去评价、或者再次购买
 */
public class OrderUtil {

    public static String orderStatusText(Order order) {
        int status = order.getOrderStatus().intValue();

        if (status == 101) {
            return "未付款";
        }

        if (status == 102) {
            return "已取消";
        }

        if (status == 103) {
            return "已取消(系统)";
        }

        if (status == 201) {
            return "已付款";
        }

        if (status == 202) {
            return "订单取消，退款中";
        }

        if (status == 203) {
            return "已退款";
        }

        if (status == 204) {
            return "已超时团购";
        }

        if (status == 301) {
            return "已发货";
        }

        if (status == 401) {
            return "已收货";
        }

        if (status == 402) {
            return "已收货(系统)";
        }

        throw new IllegalStateException("orderStatus不支持");
    }

    public static String payTypeText(Order order) {
        Integer payType = order.getPayType();
        if (Objects.equals(payType, 1)) {
            return "微信";
        } else if (Objects.equals(payType, 2)) {
            return "支付宝";
        } else if (Objects.equals(payType, 3)) {
            return "TEST";
        }
        return "";
    }


    public static OrderHandleOption build(Order order) {
        int status = order.getOrderStatus().intValue();
        OrderHandleOption handleOption = new OrderHandleOption();

        if (status == 101) {
            // 如果订单没有被取消，且没有支付，则可支付，可取消
            handleOption.setCancel(true);
            handleOption.setPay(true);
        } else if (status == 102 || status == 103) {
            // 如果订单已经取消或是已完成，则可删除
            handleOption.setDelete(true);
        } else if (status == 201) {
            // 如果订单已付款，没有发货，则可退款
            handleOption.setRefund(true);
        } else if (status == 202 || status == 204) {
            // 如果订单申请退款中，没有相关操作
        } else if (status == 203) {
            // 如果订单已经退款，则可删除
            handleOption.setDelete(true);
        } else if (status == 301) {
            // 如果订单已经发货，没有收货，则可收货操作,
            // 此时不能取消订单
            handleOption.setConfirm(true);
        } else if (status == 401 || status == 402) {
            // 如果订单已经支付，且已经收货，则可删除、去评论、申请售后和再次购买
            handleOption.setDelete(true);
            handleOption.setComment(true);
            handleOption.setRebuy(true);
            handleOption.setAftersale(true);
        } else {
            throw new IllegalStateException("status不支持");
        }

        return handleOption;
    }

    public static List<Short> orderStatus(Integer showType) {
        // 全部订单
        if (showType == 0) {
            return null;
        }

        List<Short> status = new ArrayList<>(2);

        if (showType.equals(1)) {
            // 待付款订单
            status.add((short) 101);
        } else if (showType.equals(2)) {
            // 待发货订单
            status.add((short) 201);
        } else if (showType.equals(3)) {
            // 待收货订单
            status.add((short) 301);
        } else if (showType.equals(4)) {
            // 待评价订单
            status.add((short) 401);
//            系统超时自动取消，此时应该不支持评价
//            status.add((short)402);
        } else {
            return null;
        }

        return status;
    }


    public static boolean isCreateStatus(Order order) {
        return OrderStatusEnum.STATUS_CREATE.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean hasPayed(Order order) {
        return OrderStatusEnum.STATUS_CREATE.getStatus() != order.getOrderStatus().shortValue()
                && OrderStatusEnum.STATUS_CANCEL.getStatus() != order.getOrderStatus().shortValue()
                && OrderStatusEnum.STATUS_AUTO_CANCEL.getStatus() != order.getOrderStatus().shortValue();
    }

    public static boolean isPayStatus(Order order) {
        return OrderStatusEnum.STATUS_PAY.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean isShipStatus(Order order) {
        return OrderStatusEnum.STATUS_SHIP.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean isConfirmStatus(Order order) {
        return OrderStatusEnum.STATUS_CONFIRM.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean isCancelStatus(Order order) {
        return OrderStatusEnum.STATUS_CANCEL.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean isAutoCancelStatus(Order order) {
        return OrderStatusEnum.STATUS_AUTO_CANCEL.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean isRefundStatus(Order order) {
        return OrderStatusEnum.STATUS_REFUND.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean isRefundConfirmStatus(Order order) {
        return OrderStatusEnum.STATUS_REFUND_CONFIRM.getStatus() == order.getOrderStatus().shortValue();
    }

    public static boolean isAutoConfirmStatus(Order order) {
        return OrderStatusEnum.STATUS_AUTO_CONFIRM.getStatus() == order.getOrderStatus().shortValue();
    }

}
