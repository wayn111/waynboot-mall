package com.wayn.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单管理导出DTO
 */
@Data
public class OrderExportDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3129813461714576208L;
    /**
     * 订单编号
     */
    @ExcelProperty("订单编号")
    private String orderSn;

    /**
     * 订单状态说明
     */
    @ExcelProperty("订单状态")
    private String orderStatusMsg;

    /**
     * 收货人名称
     */
    @ExcelProperty("收货人名称")
    private String consignee;

    /**
     * 收货人手机号
     */
    @ExcelProperty("收货人手机号")
    private String mobile;

    /**
     * 收货具体地址
     */
    @ExcelProperty("收货具体地址")
    private String address;

    /**
     * 用户订单留言
     */
    @ExcelProperty("用户订单留言")
    private String message;

    /**
     * 商品总费用
     */
    @ExcelProperty("商品总费用")
    private BigDecimal goodsPrice;

    /**
     * 订单费用， = goods_price + freight_price - coupon_price
     */
    @ExcelProperty("订单费用")
    private BigDecimal orderPrice;

    /**
     * 实付费用， = order_price - integral_price
     */
    @ExcelProperty("实付费用")
    private BigDecimal actualPrice;

    /**
     * 支付方式说明
     */
    @ExcelProperty("支付方式")
    private String payTypeMsg;

    /**
     * 付款时间
     */
    @ExcelProperty("付款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 发货编号
     */
    @ExcelProperty("发货编号")
    private String shipSn;

    /**
     * 发货快递公司
     */
    @ExcelProperty("发货快递公司")
    private String shipChannel;

    /**
     * 发货开始时间
     */
    @ExcelProperty("发货开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shipTime;

    /**
     * 实际退款金额，（有可能退款金额小于实际支付金额）
     */
    @ExcelProperty("实际退款金额")
    private BigDecimal refundAmount;

    /**
     * 退款方式
     */
    @ExcelProperty("退款方式")
    private String refundTypeMsg;

    /**
     * 退款备注
     */
    @ExcelProperty("退款备注")
    private String refundContent;

    /**
     * 退款时间
     */
    @ExcelProperty("退款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    /**
     * 用户确认收货时间
     */
    @ExcelProperty("用户确认收货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /**
     * 商品名称
     */
    @ExcelProperty("商品名称")
    private String goodsName;
    /**
     * 退款状态说明
     */
    @ExcelProperty("退款状态")
    private String refundStatusMsg;
}
