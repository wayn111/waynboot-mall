package com.wayn.mobile.api.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.shop.Order;
import com.wayn.common.core.domain.vo.OrderVO;
import com.wayn.common.util.R;
import com.wayn.message.core.messsage.OrderDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;

/**
 * 订单表 服务类
 *
 * @author wayn
 * @since 2020-08-11
 */
public interface IOrderUnpaidService extends IService<Order> {
    R unpaid(String orderSn);
}
